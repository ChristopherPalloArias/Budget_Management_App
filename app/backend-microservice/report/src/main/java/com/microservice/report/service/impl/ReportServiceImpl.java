package com.microservice.report.service.impl;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import com.microservice.report.dto.PaginatedResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.microservice.report.dto.ReportResponse;
import com.microservice.report.dto.ReportSummary;
import com.microservice.report.mapper.ReportMapper;
import com.microservice.report.exception.ReportNotFoundException;
import com.microservice.report.infrastructure.dto.TransactionMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.microservice.report.infrastructure.dto.TransactionType;
import com.microservice.report.model.Report;
import com.microservice.report.repository.ReportRepository;
import com.microservice.report.service.ReportService;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Implementación principal del servicio de reportes financieros.
 *
 * <p>Esta clase es el componente central del microservicio de reportes dentro de la
 * arquitectura Event-Driven del sistema. Su responsabilidad principal es agregar
 * datos financieros por período mensual ({@code "yyyy-MM"}) a partir de los mensajes
 * de transacción consumidos asíncronamente desde RabbitMQ.</p>
 *
 * <h3>Rol en la Arquitectura Event-Driven</h3>
 * <p>Esta clase actúa como el <strong>procesador final</strong> de la cadena de eventos:</p>
 * <pre>
 *   TransactionServiceImpl (produce evento)
 *     → TransactionEventListener (intercepta async)
 *       → TransactionMessageProducer (publica a RabbitMQ)
 *         → ReportConsumer (consume de RabbitMQ)
 *           → <strong>ReportServiceImpl.updateReport()</strong> (agrega datos)
 * </pre>
 *
 * <h3>Lógica de Negocio</h3>
 * <ul>
 *   <li><strong>Agregación por período:</strong> Cada transacción se acumula en un
 *       reporte mensual único por usuario. El período se deriva del campo {@code date}
 *       del mensaje con formato {@code "yyyy-MM"}.</li>
 *   <li><strong>Fórmula de balance:</strong> {@code balance = totalIncome - totalExpense}</li>
 *   <li><strong>Get-or-Create:</strong> Si no existe un reporte para el período, se crea
 *       automáticamente con totales en {@code BigDecimal.ZERO}.</li>
 * </ul>
 *
 * <h3>Deuda Técnica Identificada</h3>
 * <ul>
 *   <li><strong>DT-DOC-01:</strong> Los métodos de solo lectura ({@code getReport},
 *       {@code getReportsByUserId}, {@code getReportsByPeriodRange}) están marcados
 *       con {@code @Transactional} pero deberían usar {@code @Transactional(readOnly = true)}
 *       para optimizar la conexión a BD.</li>
 *   <li><strong>DT-DOC-02:</strong> No hay mecanismo de idempotencia — si un mismo
 *       mensaje se procesa dos veces, los totales se acumulan incorrectamente.</li>
 *   <li><strong>DT-DOC-03:</strong> El contrato {@link ReportService} importa
 *       {@code TransactionMessage} del paquete {@code infrastructure.dto}, acoplando
 *       la interfaz del servicio al DTO de infraestructura en lugar de un Port del dominio.</li>
 * </ul>
 *
 * @see ReportConsumer Consumidor RabbitMQ que invoca {@code updateReport()}
 * @see ReportService  Contrato (interfaz) que esta clase implementa
 * @see Report         Entidad JPA que almacena los totales agregados
 */
@RequiredArgsConstructor
@Service
public class ReportServiceImpl implements ReportService {
    
    private static final String PERIOD_FORMAT = "yyyy-MM";
    private static final Pattern PERIOD_PATTERN = Pattern.compile("^\\d{4}-(0[1-9]|1[0-2])$");
    
    private final ReportRepository reportRepository;

    /**
     * Busca un reporte existente para el usuario y período derivado del mensaje,
     * o crea uno nuevo con totales inicializados en cero.
     *
     * <p>El período se calcula a partir del campo {@code date} del mensaje
     * usando el formato {@code "yyyy-MM"} (ejemplo: {@code "2026-02"}).</p>
     *
     * <p><strong>Nota:</strong> Si el reporte no existe, se persiste inmediatamente vía
     * {@code reportRepository.save()} dentro del {@code orElseGet}, lo que ejecuta un
     * INSERT antes de retornar. Esto asegura que el reporte tenga un ID asignado
     * para la posterior actualización en {@link #updateReport}.</p>
     *
     * @param transactionMessage mensaje de transacción consumido desde RabbitMQ,
     *                           del cual se extraen {@code userId} y {@code date}
     * @return la entidad {@link Report} existente o recién creada, nunca {@code null}
     */
    private Report getOrCreateReport(TransactionMessage transactionMessage) {
        validateTransactionMessage(transactionMessage);
        String userId = transactionMessage.userId();
        String period = extractPeriodFromDate(transactionMessage.date());
        return reportRepository.findByUserIdAndPeriod(userId, period)
                .orElseGet(() -> createNewReport(userId, period));
    }

    /**
     * Extrae el período en formato "yyyy-MM" de una fecha.
     *
     * @param date fecha de la transacción
     * @return período formateado (ejemplo: "2026-02")
     */
    private String extractPeriodFromDate(java.time.LocalDate date) {
        return date.format(DateTimeFormatter.ofPattern(PERIOD_FORMAT));
    }

    /**
     * Crea un nuevo reporte inicializado con valores en cero.
     *
     * @param userId identificador del usuario
     * @param period período del reporte
     * @return reporte persistido con ID generado
     */
    private Report createNewReport(String userId, String period) {
        return reportRepository.save(
                Report.builder()
                        .userId(userId)
                        .period(period)
                        .totalIncome(BigDecimal.ZERO)
                        .totalExpense(BigDecimal.ZERO)
                        .balance(BigDecimal.ZERO)
                        .build());
    }

    /**
     * Actualiza el reporte financiero mensual acumulando el monto de una transacción.
     *
     * <p>Este es el método central de la cadena Event-Driven. Es invocado por
     * {@link ReportConsumer} cada vez que un mensaje {@link TransactionMessage}
     * llega a la cola de RabbitMQ.</p>
     *
     * <h4>Flujo de ejecución:</h4>
     * <ol>
     *   <li>Busca o crea el reporte para el usuario y período (vía {@link #getOrCreateReport}).</li>
     *   <li>Según el {@link TransactionType}:
     *     <ul>
     *       <li>{@code INCOME} → Suma el monto a {@code totalIncome}.</li>
     *       <li>{@code EXPENSE} → Suma el monto a {@code totalExpense}.</li>
     *     </ul>
     *   </li>
     *   <li>Recalcula el balance: {@code balance = totalIncome - totalExpense}.</li>
     *   <li>Persiste el reporte actualizado.</li>
     * </ol>
     *
     * <p><strong>⚠️ Deuda técnica (DT-DOC-02):</strong> Este método no es idempotente.
     * Si un mensaje se entrega más de una vez (escenario de retry sin DLQ), los totales
     * se acumularán incorrectamente. Considerar agregar un registro de {@code transactionId}
     * procesados para lograr idempotencia.</p>
     *
     * @param transactionMessage mensaje deserializado desde la cola de RabbitMQ
     *                           con los datos de la transacción creada
     */
    @Transactional
    @Override
    public void updateReport(TransactionMessage transactionMessage) {
        Report report = getOrCreateReport(transactionMessage);
        BigDecimal amount = transactionMessage.amount();
        
        accumulateTransactionAmount(report, transactionMessage.type(), amount);
        recalculateBalance(report);
        
        reportRepository.save(report);
    }

    /**
     * Acumula el monto de una transacción en el total correspondiente.
     *
     * @param report reporte a actualizar
     * @param type tipo de transacción (INCOME o EXPENSE)
     * @param amount monto a acumular
     */
    private void accumulateTransactionAmount(Report report, TransactionType type, BigDecimal amount) {
        if (type == TransactionType.INCOME) {
            report.setTotalIncome(report.getTotalIncome().add(amount));
        } else if (type == TransactionType.EXPENSE) {
            report.setTotalExpense(report.getTotalExpense().add(amount));
        }
    }

    /**
     * Recalcula el balance de un reporte.
     * 
     * <p>Fórmula: {@code balance = totalIncome - totalExpense}</p>
     *
     * @param report reporte cuyo balance se recalculará
     */
    private void recalculateBalance(Report report) {
        BigDecimal balance = calculateBalance(report.getTotalIncome(), report.getTotalExpense());
        report.setBalance(balance);
    }

    /**
     * Calcula el balance financiero.
     *
     * @param totalIncome total de ingresos
     * @param totalExpense total de gastos
     * @return balance (ingresos - gastos)
     */
    private BigDecimal calculateBalance(BigDecimal totalIncome, BigDecimal totalExpense) {
        return totalIncome.subtract(totalExpense);
    }

    /**
     * Obtiene el reporte financiero de un usuario para un período específico.
     *
     * @param userId identificador del usuario (Firebase UID)
     * @param period período mensual en formato {@code "yyyy-MM"} (ejemplo: {@code "2026-02"})
     * @return respuesta mapeada con los totales del período
     * @throws ReportNotFoundException si no existe un reporte para la combinación usuario/período
     */
    @Transactional(readOnly = true)
    @Override
    public ReportResponse getReport(String userId, String period) {
        Report report = findReportOrThrow(userId, period);
        return ReportMapper.toResponse(report);
    }

    /**
     * Obtiene todos los reportes de un usuario con paginación.
     *
     * <p>Los reportes se ordenan por defecto de forma descendente por período
     * (configurado en {@code ReportController} vía {@code @PageableDefault}).</p>
     *
     * @param userId   identificador del usuario (Firebase UID)
     * @param pageable parámetros de paginación (page, size, sort)
     * @return respuesta paginada con la lista de reportes del usuario
     */
    @Transactional(readOnly = true)
    @Override
    public PaginatedResponse<ReportResponse> getReportsByUserId(String userId, Pageable pageable) {
        Page<Report> page = reportRepository.findByUserId(userId, pageable);
        List<ReportResponse> content = page.map(ReportMapper::toResponse).getContent();

        return new PaginatedResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast());
    }

    /**
     * Genera un resumen financiero agregado para un rango de períodos mensuales.
     *
     * <p>Recorre todos los reportes del usuario dentro del rango [{@code startPeriod},
     * {@code endPeriod}] y acumula los totales de ingresos, gastos y balance.</p>
     *
     * <p><strong>Ejemplo:</strong> Para {@code startPeriod="2026-01"} y
     * {@code endPeriod="2026-06"}, retorna la suma de los 6 meses con el desglose
     * individual de cada mes en la lista {@code reports}.</p>
     *
     * @param userId      identificador del usuario (Firebase UID)
     * @param startPeriod período inicial del rango en formato {@code "yyyy-MM"}
     * @param endPeriod   período final del rango en formato {@code "yyyy-MM"}
     * @return resumen con totales acumulados y la lista de reportes individuales del rango
     */
    @Transactional(readOnly = true)
    @Override
    public ReportSummary getReportsByPeriodRange(String userId, String startPeriod, String endPeriod) {
        List<Report> reports = reportRepository.findByUserIdAndPeriodBetweenOrderByPeriodAsc(
                userId, startPeriod, endPeriod);
        
        AccumulatedTotals totals = accumulateTotalsFromReports(reports);

        return ReportMapper.toSummary(
                userId,
                startPeriod,
                endPeriod,
                ReportMapper.toResponseList(reports),
                totals.totalIncome,
                totals.totalExpense,
                calculateBalance(totals.totalIncome, totals.totalExpense));
    }

    /**
     * Acumula los totales de ingresos y gastos de una lista de reportes.
     *
     * @param reports lista de reportes a acumular
     * @return totales acumulados
     */
    private AccumulatedTotals accumulateTotalsFromReports(List<Report> reports) {
        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;

        for (Report report : reports) {
            totalIncome = totalIncome.add(report.getTotalIncome());
            totalExpense = totalExpense.add(report.getTotalExpense());
        }

        return new AccumulatedTotals(totalIncome, totalExpense);
    }

    /**
     * Elimina un reporte financiero para un usuario y período específico.
     *
     * <p>Este método implementa idempotencia según RFC 9110 §9.3.5.
     * Si el reporte no existe, el método completa silenciosamente sin lanzar excepción.
     * Esto asegura que múltiples solicitudes DELETE al mismo recurso devuelvan 204 NO CONTENT.</p>
     *
     * @param userId identificador del usuario
     * @param period período mensual (yyyy-MM)
     */
    @Transactional
    @Override
    public void deleteReport(String userId, String period) {
        validateUserId(userId);
        validatePeriod(period);
        // Buscar y eliminar si existe; si no existe, completar silenciosamente (idempotencia)
        reportRepository.findByUserIdAndPeriod(userId, period)
                .ifPresent(reportRepository::delete);
    }

    /**
     * Elimina un reporte financiero por su ID.
     *
     * <p>Este método implementa idempotencia según RFC 9110 §9.3.5.
     * Si el reporte no existe o no pertenece al usuario, el método completa 
     * silenciosamente sin lanzar excepción. Esto asegura que múltiples solicitudes 
     * DELETE al mismo ID devuelvan 204 NO CONTENT.</p>
     *
     * <p><strong>Validación de propiedad:</strong> Si el reporte existe pero pertenece
     * a otro usuario, se ignora silenciosamente para mantener seguridad (no revelar
     * existencia de reportes de otros usuarios).</p>
     *
     * @param userId identificador del usuario propietario del reporte
     * @param reportId identificador único del reporte
     * @throws IllegalArgumentException si userId o reportId son inválidos
     */
    @Transactional
    @Override
    public void deleteReportById(String userId, Long reportId) {
        validateUserId(userId);
        if (reportId == null || reportId <= 0) {
            throw new IllegalArgumentException("reportId must be a positive number");
        }

        // Buscar y eliminar si existe y pertenece al usuario; si no existe o no pertenece,
        // completar silenciosamente (idempotencia + seguridad)
        reportRepository.findById(reportId)
                .filter(report -> report.getUserId().equals(userId))
                .ifPresent(reportRepository::delete);
    }

    /**
     * Record inmutable para encapsular totales acumulados.
     */
    private record AccumulatedTotals(BigDecimal totalIncome, BigDecimal totalExpense) {
    }

    private final RestTemplate restTemplate;

    /**
     * Recalcula el reporte financiero para un usuario y período específico.
     * 
     * <p>Busca transacciones reales en el microservicio de transacciones y
     * actualiza los totales del reporte. Si el reporte no existe, lo crea.</p>
     */
    @Transactional
    @Override
    public ReportResponse recalculateReport(String userId, String period) {
        validateUserId(userId);
        validatePeriod(period);
        // 1. Obtener el reporte o crearlo si no existe (con valores en cero)
        Report report = reportRepository.findByUserIdAndPeriod(userId, period)
                .orElseThrow(() -> new ReportNotFoundException(userId, period));
        
        // 2. Consultar transacciones del microservicio (usando comunicación inter-service)
        String jwt = getJwtFromContext();
        String url = "http://transaction:8081/api/v1/transactions?period=" + period + "&size=1000";
        
        HttpHeaders headers = new HttpHeaders();
        if (jwt != null) {
            headers.set("Authorization", "Bearer " + jwt);
        }
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<PaginatedTransactionResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<PaginatedTransactionResponse>() {}
            );

            // 3. Procesar y sumar transacciones
            BigDecimal totalIncome = BigDecimal.ZERO;
            BigDecimal totalExpense = BigDecimal.ZERO;
            
            if (response.getBody() != null && response.getBody().content() != null) {
                for (TransactionData tx : response.getBody().content()) {
                    if ("INCOME".equals(tx.type())) {
                        totalIncome = totalIncome.add(tx.amount());
                    } else if ("EXPENSE".equals(tx.type())) {
                        totalExpense = totalExpense.add(tx.amount());
                    }
                }
            }

            // 4. Actualizar estado del reporte
            report.setTotalIncome(totalIncome);
            report.setTotalExpense(totalExpense);
            report.setBalance(totalIncome.subtract(totalExpense));
            
            Report savedReport = reportRepository.save(report);
            return ReportMapper.toResponse(savedReport);
            
        } catch (Exception e) {
            // Log error and propagate or handle
            throw new RuntimeException("Error al conectar con el microservicio de transacciones: " + e.getMessage(), e);
        }
    }

    private String getJwtFromContext() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                return authHeader.substring(7);
            }
        }
        return null;
    }

    // DTOs auxiliares para el consumo del microservicio de transacciones
    record PaginatedTransactionResponse(List<TransactionData> content) {}
    record TransactionData(String type, BigDecimal amount) {}

    /**
     * Busca un reporte por usuario y período, lanzando excepción si no existe.
     *
     * @param userId identificador del usuario
     * @param period período del reporte
     * @return reporte encontrado
     * @throws ReportNotFoundException si el reporte no existe
     */
    private Report findReportOrThrow(String userId, String period) {
        validateUserId(userId);
        validatePeriod(period);
        return reportRepository.findByUserIdAndPeriod(userId, period)
                .orElseThrow(() -> new ReportNotFoundException(userId, period));
    }

    /**
     * Valida que el userId sea válido (no nulo ni vacío).
     *
     * @param userId identificador del usuario
     */
    private void validateUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId cannot be null or blank");
        }
    }

    /**
     * Valida que el período tenga formato yyyy-MM.
     *
     * @param period período a validar
     */
    private void validatePeriod(String period) {
        if (period == null) {
            throw new IllegalArgumentException("period cannot be null");
        }
        if (!PERIOD_PATTERN.matcher(period).matches()) {
            throw new IllegalArgumentException(
                    String.format("Invalid period format: %s. Expected format: yyyy-MM", period));
        }
    }

    /**
     * Valida el mensaje de transacción para evitar NPE en el flujo event-driven.
     *
     * @param transactionMessage mensaje recibido desde mensajería
     */
    private void validateTransactionMessage(TransactionMessage transactionMessage) {
        Objects.requireNonNull(transactionMessage, "transactionMessage cannot be null");
        validateUserId(transactionMessage.userId());
        Objects.requireNonNull(transactionMessage.date(), "transactionMessage.date cannot be null");
        Objects.requireNonNull(transactionMessage.amount(), "transactionMessage.amount cannot be null");
        Objects.requireNonNull(transactionMessage.type(), "transactionMessage.type cannot be null");
    }
}
