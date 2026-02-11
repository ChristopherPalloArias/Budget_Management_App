package com.microservice.report.dto;

import java.math.BigDecimal;
import java.util.List;

public record ReportSummary(
        String userId,
        String startPeriod,
        String endPeriod,
        List<ReportResponse> reports,
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal balance) {
}
