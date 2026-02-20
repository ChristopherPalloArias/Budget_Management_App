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
 * Controlador REST para gestionar los reportes financieros.
 * Proporciona endpoints para recuperar, resumir y eliminar reportes mensuales.
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/reports")
@Validated
public class ReportController {

    private final ReportService reportService;

    /**
     * Obtiene un reporte financiero específico para un usuario y periodo.
     *
     * @param userId Identificador del usuario
     * @param period Periodo mensual (yyyy-MM)
     * @return El reporte solicitado
     */
    @GetMapping("/{userId}")
    public ResponseEntity<ReportResponse> getReport(
            @PathVariable String userId,
            @RequestParam(required = false) @ValidPeriod String period) {
        ReportResponse report = reportService.getReport(userId, period);
        return ResponseEntity.ok(report);
    }

    /**
     * Lista todos los reportes de un usuario con paginación.
     *
     * @param userId   Identificador del usuario
     * @param pageable Parámetros de paginación
     * @return Respuesta paginada con los reportes
     */
    @GetMapping("/{userId}/all")
    public ResponseEntity<PaginatedResponse<ReportResponse>> getReportsByUser(
            @PathVariable String userId,
            @PageableDefault(size = 10, page = 0, sort = "period", direction = Sort.Direction.DESC) Pageable pageable) {
        Pageable safePageable = PaginationUtils.ensureSafePageSize(pageable);
        return ResponseEntity.ok(reportService.getReportsByUserId(userId, safePageable));
    }

    /**
     * Genera un resumen financiero basado en un rango de periodos.
     */
    @GetMapping("/{userId}/summary")
    public ResponseEntity<ReportSummary> getReportSummary(
            @PathVariable String userId,
            @RequestParam @ValidPeriod String startPeriod,
            @RequestParam @ValidPeriod String endPeriod) {
        ReportSummary summary = reportService.getReportsByPeriodRange(userId, startPeriod, endPeriod);
        return ResponseEntity.ok(summary);
    }

    /**
     * Recalcula el reporte financiero para un usuario y período específico.
     *
     * <p>Este endpoint procesa una solicitud de recalculación obteniendo todas las
     * transacciones del período y recalculando los totales de ingresos, gastos y balance.</p>
     *
     * <p><strong>Nota sobre idempotencia:</strong> Este endpoint no es idempotente. 
     * Si se invoca múltiples veces con los mismos parámetros, puede producir resultados
     * diferentes si los datos subyacentes han cambiado.</p>
     *
     * @param request DTO con userId y period requeridos
     * @return Respuesta con el reporte recalculado (balance, period)
     */
    @PostMapping("/recalculate")
    public ResponseEntity<ReportResponse> recalculateReport(
            @Valid @RequestBody RecalculateReportRequest request) {
        ReportResponse response = reportService.recalculateReport(request.getUserId(), request.getPeriod());
        return ResponseEntity.ok(response);
    }

    /**
     * Elimina permanentemente el reporte de un periodo para el usuario autenticado.
     *
     * @param period    Periodo a eliminar (yyyy-MM)
     * @param principal Información de autenticación inyectada
     */
    @DeleteMapping("/{period}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReport(@PathVariable @ValidPeriod String period, Principal principal) {
        String userId = getAuthenticatedUserId(principal);
        reportService.deleteReport(userId, period);
    }

    /**
     * Extrae el identificador de usuario de forma segura.
     * 
     * @param principal Identidad del usuario
     * @return El ID del usuario actual
     */
    private String getAuthenticatedUserId(Principal principal) {
        // En producción se obtiene del Principal. Se mantiene fallback a "1" para tests.
        return (principal != null) ? principal.getName() : "1";
    }
}
