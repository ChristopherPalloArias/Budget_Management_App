package com.microservice.report.infrastructure.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionMessage {
    @NotNull(message = "Transaction ID cannot be null")
    private Long transactionId;
    @NotBlank(message = "User ID cannot be null or empty")
    private String userId;
    @NotNull(message = "Transaction type cannot be null")
    private TransactionType type;
    @NotNull(message = "Amount cannot be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than 0")
    private BigDecimal amount;
    @NotNull(message = "Date cannot be null")
    private LocalDate date;
    @NotBlank(message = "User ID cannot be null or empty")
    private String category;
    private String description;
}
