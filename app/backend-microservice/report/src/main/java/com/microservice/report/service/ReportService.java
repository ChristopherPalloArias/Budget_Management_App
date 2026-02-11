package com.microservice.report.service;

import com.microservice.report.dto.ReportResponse;
import com.microservice.report.dto.ReportSummary;
import com.microservice.report.infrastructure.dto.TransactionMessage;

import java.util.List;

public interface ReportService {
    void updateReport(TransactionMessage transactionMessage);
    ReportResponse getReport(String userId, String period);
    List<ReportResponse> getReportsByUserId(String userId);
    ReportSummary getReportsByPeriodRange(String userId, String startPeriod, String endPeriod);
}
