package com.microservice.report.dto;

import com.microservice.report.model.Report;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class ReportSummary {
    private String userId;
    private String startPeriod;
    private String endPeriod;
    private List<Report> reports;
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal balance;
}
