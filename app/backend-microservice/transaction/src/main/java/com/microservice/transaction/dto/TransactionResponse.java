package com.microservice.transaction.dto;

import com.microservice.transaction.model.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record TransactionResponse(
        Long transactionId,
        String userId,
        TransactionType type,
        BigDecimal amount,
        String category,
        LocalDate date,
        String description,
        OffsetDateTime createdAt) {
}
