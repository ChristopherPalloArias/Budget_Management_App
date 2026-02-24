package com.microservice.report.service;

import com.microservice.report.model.Report;
import java.math.BigDecimal;
import java.util.List;

public interface ReportService {
    List<Report> getAllReports();
    Report getReportById(Long id);
    void processTransaction(Long transactionId, BigDecimal amount, String type, String category);
}
