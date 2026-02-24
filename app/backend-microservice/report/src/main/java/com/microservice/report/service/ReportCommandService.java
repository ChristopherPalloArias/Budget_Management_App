package com.microservice.report.service;

import com.microservice.report.infrastructure.dto.TransactionMessage;
import com.microservice.report.dto.ReportResponse;

public interface ReportCommandService {
    void updateReport(TransactionMessage transactionMessage, String messageId);
    void deleteReport(String userId, String period);
    void deleteReportById(String userId, Long reportId);
    ReportResponse recalculateReport(String userId, String period);
}
