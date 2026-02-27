package com.microservice.report.controller;

import com.microservice.report.dto.PaginatedResponse;
import com.microservice.report.dto.RecalculateReportRequest;
import com.microservice.report.dto.ReportResponse;
import com.microservice.report.dto.ReportSummary;
import com.microservice.report.service.ReportCommandService;
import com.microservice.report.service.ReportQueryService;
import com.microservice.report.util.PaginationUtils;
import com.microservice.report.validation.ValidPeriod;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

/**
 * Controlador REST para gestionar reportes financieros.
 * 
 * Todos los endpoints requieren autenticación JWT.
 * El userId se extrae automáticamente del usuario autenticado,
 * garantizando que cada usuario solo puede ver/modificar sus propios reportes.
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/reports")
@Validated
public class ReportController {

    private final ReportCommandService reportCommandService;
    private final ReportQueryService reportQueryService;
    private final MeterRegistry meterRegistry;

    private Counter reportsGeneratedCounter;
    private Counter reportsRecalculatedCounter;

    @PostConstruct
    void registerMetrics() {
        this.reportsGeneratedCounter = Counter
                .builder("app_reports_generated_total")
                .description("Total de reportes generados")
                .register(meterRegistry);

        this.reportsRecalculatedCounter = Counter
                .builder("app_reports_recalculated_total")
                .description("Total de reportes recalculados")
                .register(meterRegistry);
    }

    /**
     * Obtiene un reporte financiero para el usuario autenticado en un periodo específico.
     *
     * @param principal Usuario autenticado (inyectado por Spring Security)
     * @param period Periodo mensual (yyyy-MM), opcional
     * @return El reporte solicitado
     */
    @GetMapping
    public ResponseEntity<ReportResponse> getReport(
            Principal principal,
            @RequestParam(required = false) @ValidPeriod String period) {
        String userId = principal.getName();
        ReportResponse report = reportQueryService.getReport(userId, period);
        reportsGeneratedCounter.increment();
        return ResponseEntity.ok(report);
    }

    /**
     * Lista todos los reportes del usuario autenticado con paginación.
     *
     * @param principal Usuario autenticado
     * @param pageable Parámetros de paginación
     * @return Respuesta paginada con los reportes en orden descendente por período
     */
    @GetMapping("/all")
    public ResponseEntity<PaginatedResponse<ReportResponse>> getReportsByUser(
            Principal principal,
            @PageableDefault(size = 10, page = 0, sort = "period", direction = Sort.Direction.DESC) Pageable pageable) {
        String userId = principal.getName();
        Pageable safePageable = PaginationUtils.ensureSafePageSize(pageable);
        return ResponseEntity.ok(reportQueryService.getReportsByUserId(userId, safePageable));
    }

    /**
     * Genera un resumen financiero del usuario autenticado basado en un rango de períodos.
     *
     * @param principal Usuario autenticado
     * @param startPeriod Período inicial (yyyy-MM)
     * @param endPeriod Período final (yyyy-MM)
     * @return Resumen agregado de ingresos, gastos y balance
     */
    @GetMapping("/summary")
    public ResponseEntity<ReportSummary> getReportSummary(
            Principal principal,
            @RequestParam @ValidPeriod String startPeriod,
            @RequestParam @ValidPeriod String endPeriod) {
        String userId = principal.getName();
        ReportSummary summary = reportQueryService.getReportsByPeriodRange(userId, startPeriod, endPeriod);
        return ResponseEntity.ok(summary);
    }

    /**
     * Recalcula el reporte financiero para el usuario autenticado en un período específico.
     *
     * <p>Este endpoint actualiza un reporte existente obteniendo todas las
     * transacciones del período y recalculando los totales de ingresos, gastos y balance.</p>
     *
     * <p>Esta operación es idempotente: múltiples llamadas con los mismos parámetros
     * producen el mismo resultado.</p>
     *
     * <p><strong>Seguridad:</strong> El userId del usuario autenticado se valida contra
     * el userId en el request. Si no coinciden, se retorna un error de Acceso Denegado.</p>
     *
     * @param principal Usuario autenticado
     * @param request DTO con el period requerido (userId debe coincidir con el autenticado)
     * @return Respuesta con el reporte recalculado (200 OK)
     */
    @PutMapping("/recalculate")
    public ResponseEntity<ReportResponse> recalculateReport(
            Principal principal,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String token,
            @Valid @RequestBody RecalculateReportRequest request) {
        String userId = principal.getName();
        ReportResponse response = reportCommandService.recalculateReport(userId, request.getPeriod(), token);
        reportsRecalculatedCounter.increment();
        return ResponseEntity.ok(response);
    }

    /**
     * Elimina permanentemente un reporte por su ID para el usuario autenticado.
     *
     * @param reportId ID del reporte a eliminar
     * @param principal Usuario autenticado
     */
    @DeleteMapping("/{reportId:[0-9]+}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReportById(
            @PathVariable Long reportId,
            Principal principal) {
        String userId = principal.getName();
        reportCommandService.deleteReportById(userId, reportId);
    }

    /**
     * Elimina permanentemente el reporte de un periodo para el usuario autenticado.
     *
     * @param period Periodo a eliminar (yyyy-MM)
     * @param principal Usuario autenticado
     */
    @DeleteMapping("/{period:[0-9]{4}-[0-9]{2}}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReport(
            @PathVariable String period,
            Principal principal) {
        String userId = principal.getName();
        reportCommandService.deleteReport(userId, period);
    }
}

