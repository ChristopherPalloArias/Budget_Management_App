package com.microservice.report.controller;

import com.microservice.report.dto.PaginatedResponse;
import com.microservice.report.dto.RecalculateReportRequest;
import com.microservice.report.dto.ReportResponse;
import com.microservice.report.dto.ReportSummary;
import com.microservice.report.service.ReportService;
import com.microservice.report.util.PaginationUtils;
import com.microservice.report.validation.ValidPeriod;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
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

    private final ReportService reportService;

    /**
     * Obtiene reportes financieros del usuario autenticado.
     *
     * <p><strong>Comportamiento condicional:</strong></p>
     * <ul>
     *   <li>Si se proporciona `period` (yyyy-MM): retorna un único {@link ReportResponse}</li>
     *   <li>Si `period` NO se proporciona: retorna una colección paginada {@link PaginatedResponse}</li>
     * </ul>
     *
     * <p><strong>Ejemplos:</strong></p>
     * <ul>
     *   <li><code>GET /api/v1/reports?period=2026-02</code> → ReportResponse (single)</li>
     *   <li><code>GET /api/v1/reports</code> → PaginatedResponse (collection)</li>
     *   <li><code>GET /api/v1/reports?page=0&size=20</code> → PaginatedResponse (custom pagination)</li>
     * </ul>
     *
     * @param principal Usuario autenticado (inyectado por Spring Security)
     * @param period Período mensual (yyyy-MM), OPCIONAL. Si presente, retorna single report.
     * @param pageable Parámetros de paginación, usado solo si `period` NO se proporciona.
     *                 Defecto: size=10, page=0, sort=period DESC
     * @return {@link ReportResponse} si `period` es provided; 
     *         {@link PaginatedResponse} si `period` es absent
     */
    @GetMapping
    public ResponseEntity<?> getReports(
            Principal principal,
            @RequestParam(required = false) @ValidPeriod String period,
            @PageableDefault(size = 10, page = 0, sort = "period", direction = Sort.Direction.DESC) Pageable pageable) {
        
        String userId = principal.getName();
        
        // Conditional routing: period presence determines response type
        if (period != null && !period.isBlank()) {
            // Single resource: return single ReportResponse
            ReportResponse report = reportService.getReport(userId, period);
            return ResponseEntity.ok(report);
        } else {
            // Collection: return paginated ReportResponse collection
            Pageable safePageable = PaginationUtils.ensureSafePageSize(pageable);
            PaginatedResponse<ReportResponse> reports = reportService.getReportsByUserId(userId, safePageable);
            return ResponseEntity.ok(reports);
        }
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
        ReportSummary summary = reportService.getReportsByPeriodRange(userId, startPeriod, endPeriod);
        return ResponseEntity.ok(summary);
    }

    /**
     * Recalcula el reporte financiero para el usuario autenticado en un período específico.
     *
     * <p>Este endpoint procesa una solicitud de recalculación obteniendo todas las
     * transacciones del período y recalculando los totales de ingresos, gastos y balance.</p>
     *
     * <p><strong>Seguridad:</strong> El userId del usuario autenticado se valida contra
     * el userId en el request. Si no coinciden, se retorna un error de Acceso Denegado.</p>
     *
     * @param principal Usuario autenticado
     * @param request DTO con el period requerido (userId debe coincidir con el autenticado)
     * @return Respuesta con el reporte recalculado (balance, period)
     */
    @PostMapping("/recalculate")
    public ResponseEntity<ReportResponse> recalculateReport(
            Principal principal,
            @Valid @RequestBody RecalculateReportRequest request) {
        String userId = principal.getName();
        ReportResponse response = reportService.recalculateReport(userId, request.getPeriod());
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
        reportService.deleteReportById(userId, reportId);
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
        reportService.deleteReport(userId, period);
    }
}

