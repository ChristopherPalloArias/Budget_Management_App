package com.microservice.report.service;

import com.microservice.report.dto.ReportSummary;
import com.microservice.report.infrastructure.dto.TransactionMessage;
import com.microservice.report.model.Report;

import java.util.List;

public interface ReportService {
    void updateReport(TransactionMessage transactionMessage);
    Report getReport(String userId, String period);
    List<Report> getReportsByUserId(String userId);
    ReportSummary getReportsByPeriodRange(String userId, String startPeriod, String endPeriod);
}
