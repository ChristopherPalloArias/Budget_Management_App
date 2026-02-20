package com.microservice.report.service.impl;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

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
        String userId = transactionMessage.userId();
        String period = transactionMessage.date()
                .format(DateTimeFormatter.ofPattern("yyyy-MM"));
        return reportRepository.findByUserIdAndPeriod(userId, period)
                .orElseGet(() -> reportRepository.save(
                        Report.builder()
                                .userId(userId)
                                .period(period)
                                .totalIncome(BigDecimal.ZERO)
                                .totalExpense(BigDecimal.ZERO)
                                .balance(BigDecimal.ZERO)
                                .build()));
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
        if (transactionMessage.type() == TransactionType.INCOME) {
            report.setTotalIncome(report.getTotalIncome().add(amount));
        } else if (transactionMessage.type() == TransactionType.EXPENSE) {
            report.setTotalExpense(report.getTotalExpense().add(amount));
        }
        report.setBalance(report.getTotalIncome().subtract(report.getTotalExpense()));
        reportRepository.save(report);
    }

    /**
     * Obtiene el reporte financiero de un usuario para un período específico.
     *
     * @param userId identificador del usuario (Firebase UID)
     * @param period período mensual en formato {@code "yyyy-MM"} (ejemplo: {@code "2026-02"})
     * @return respuesta mapeada con los totales del período
     * @throws ReportNotFoundException si no existe un reporte para la combinación usuario/período
     */
    @Transactional
    @Override
    public ReportResponse getReport(String userId, String period) {
        Report report = reportRepository.findByUserIdAndPeriod(userId, period)
                .orElseThrow(() -> new ReportNotFoundException(userId, period));
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
    @Transactional
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
    @Transactional
    @Override
    public ReportSummary getReportsByPeriodRange(String userId, String startPeriod, String endPeriod) {
        List<Report> reports = reportRepository.findByUserIdAndPeriodBetweenOrderByPeriodAsc(
                userId, startPeriod, endPeriod);
        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;

        for (Report report : reports) {
            totalIncome = totalIncome.add(report.getTotalIncome());
            totalExpense = totalExpense.add(report.getTotalExpense());
        }

        return ReportMapper.toSummary(
                userId,
                startPeriod,
                endPeriod,
                ReportMapper.toResponseList(reports),
                totalIncome,
                totalExpense,
                totalIncome.subtract(totalExpense));
    }

    /**
     * Recalcula el reporte financiero para un usuario y período específico.
     * 
     * TODO: Implementar en GREEN phase
     *
     * @param userId identificador del usuario propietario del reporte
     * @param period período en formato "yyyy-MM" (ejemplo: "2025-11")
     * @return reporte recalculado con totales actualizados
     */
    @Override
    public ReportResponse recalculateReport(String userId, String period) {
        // 🔴 RED — Stub vacío para que tests fallen inicialmente
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
