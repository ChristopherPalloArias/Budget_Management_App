package com.microservice.report.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionEvent(
        String messageId,
        Long transactionId,
        String userId,
        TransactionType type,
        BigDecimal amount,
        LocalDate date,
        String category,
        String description,
        BigDecimal previousAmount,
        LocalDate previousDate) {
}
