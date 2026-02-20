package com.microservice.report.service;

import com.microservice.report.dto.ReportResponse;
import com.microservice.report.dto.ReportSummary;
import com.microservice.report.infrastructure.dto.TransactionMessage;

import java.util.List;

import com.microservice.report.dto.PaginatedResponse;
import org.springframework.data.domain.Pageable;

public interface ReportService {
    void updateReport(TransactionMessage transactionMessage);

    ReportResponse getReport(String userId, String period);

    PaginatedResponse<ReportResponse> getReportsByUserId(String userId, Pageable pageable);

    ReportSummary getReportsByPeriodRange(String userId, String startPeriod, String endPeriod);

    /**
     * Recalcula el reporte financiero para un usuario y período específico.
     * Obtiene todas las transacciones del período y recalcula los totales.
     *
     * @param userId identificador del usuario propietario del reporte
     * @param period período en formato "yyyy-MM" (ejemplo: "2025-11")
     * @return reporte recalculado con totales actualizados
     */
    ReportResponse recalculateReport(String userId, String period);
}
