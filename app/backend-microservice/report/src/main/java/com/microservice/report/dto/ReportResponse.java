package com.microservice.report.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ReportResponse(
        Long reportId,
        String userId,
        String period,
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal balance,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {
}
