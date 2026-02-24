package com.microservice.report.service;

import java.util.List;
import com.microservice.report.dto.PaginatedResponse;
import com.microservice.report.dto.ReportResponse;
import com.microservice.report.dto.ReportSummary;
import com.microservice.report.infrastructure.dto.TransactionMessage;
import org.springframework.data.domain.Pageable;

/**
 * Interfaz unificada para el servicio de reportes financieros.
 * Define el contrato para las operaciones de consulta, actualización y mantenimiento de reportes.
 */
public interface ReportService {
    
    /**
     * Actualiza el reporte financiero acumulando el monto de una transacción.
     * @param transactionMessage mensaje de transacción recibido vía mensajería
     */
    void updateReport(TransactionMessage transactionMessage);
    
    /**
     * Obtiene el reporte de un usuario para un periodo específico.
     * @param userId ID del usuario
     * @param period periodo en formato yyyy-MM
     * @return respuesta con los totales del periodo
     */
    ReportResponse getReport(String userId, String period);
    
    /**
     * Obtiene todos los reportes de un usuario con paginación.
     * @param userId ID del usuario
     * @param pageable parámetros de paginación
     * @return respuesta paginada de reportes
     */
    PaginatedResponse<ReportResponse> getReportsByUserId(String userId, Pageable pageable);
    
    /**
     * Genera un resumen financiero para un rango de periodos.
     * @param userId ID del usuario
     * @param startPeriod periodo inicial (yyyy-MM)
     * @param endPeriod periodo final (yyyy-MM)
     * @return resumen acumulado
     */
    ReportSummary getReportsByPeriodRange(String userId, String startPeriod, String endPeriod);
    
    /**
     * Elimina el reporte de un periodo.
     * @param userId ID del usuario
     * @param period periodo a eliminar
     */
    void deleteReport(String userId, String period);
    
    /**
     * Elimina un reporte por su identificador único.
     * @param userId ID del usuario (para validación de propiedad)
     * @param reportId ID único del reporte
     */
    void deleteReportById(String userId, Long reportId);
    
    /**
     * Recalcula los totales de un reporte para un periodo.
     * @param userId ID del usuario
     * @param period periodo a recalcular
     * @return reporte actualizado
     */
    ReportResponse recalculateReport(String userId, String period);
}
