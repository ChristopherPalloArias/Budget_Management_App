package com.microservice.transaction.dto;

import com.microservice.transaction.model.TransactionType;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionRequest(
                @NotBlank String userId,
                @NotNull TransactionType type,
                @NotNull @Digits(integer = 19, fraction = 2) @Positive(message = "Amount must be positive") BigDecimal amount,
                @NotBlank(message = "Category is required") @Size(max = 100, message = "Category must be less than 100 characters") String category,
                @NotNull LocalDate date,
                @Size(max = 500) String description) {
}
