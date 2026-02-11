package com.microservice.report.infrastructure.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TransactionMessage(
        @NotNull(message = "Transaction ID cannot be null") Long transactionId,
        @NotBlank(message = "User ID cannot be null or empty") String userId,
        @NotNull(message = "Transaction type cannot be null") TransactionType type,
        @NotNull(message = "Amount cannot be null") @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than 0") BigDecimal amount,
        @NotNull(message = "Date cannot be null") LocalDate date,
        @NotBlank(message = "Category cannot be null or empty") String category,
        String description) {
}
