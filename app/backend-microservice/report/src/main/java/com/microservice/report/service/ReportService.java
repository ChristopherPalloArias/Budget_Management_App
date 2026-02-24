package com.microservice.report.service;

import com.microservice.report.dto.ReportResponse;
import com.microservice.report.dto.ReportSummary;
import com.microservice.report.domain.TransactionEvent;

import java.util.List;

import com.microservice.report.dto.PaginatedResponse;
import org.springframework.data.domain.Pageable;

public interface ReportService {
    void updateReport(TransactionEvent transactionEvent);

    ReportResponse getReport(String userId, String period);

    PaginatedResponse<ReportResponse> getReportsByUserId(String userId, Pageable pageable);

    ReportSummary getReportsByPeriodRange(String userId, String startPeriod, String endPeriod);

    void deleteReport(String userId, String period);

    void deleteReportById(String userId, Long reportId);

    ReportResponse recalculateReport(String userId, String period);
}