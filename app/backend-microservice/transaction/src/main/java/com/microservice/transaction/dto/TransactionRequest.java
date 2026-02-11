package com.microservice.transaction.dto;

import com.microservice.transaction.model.TransactionType;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionRequest(
        @NotBlank String userId,
        @NotNull TransactionType type,
        @NotNull @Digits(integer = 19, fraction = 2) BigDecimal amount,
        String category,
        @NotNull LocalDate date,
        @Size(max = 500) String description) {
}
