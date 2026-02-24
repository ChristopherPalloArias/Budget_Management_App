package com.microservice.report.service;

import com.microservice.report.dto.RecordTransactionCommand;
import com.microservice.report.dto.ReportResponse;

public interface ReportCommandService {
    void updateReport(RecordTransactionCommand command, String messageId);
    void deleteReport(String userId, String period);
    void deleteReportById(String userId, Long reportId);
    ReportResponse recalculateReport(String userId, String period, String token);
}
