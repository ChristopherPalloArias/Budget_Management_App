package com.microservice.report.infrastructure.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO que representa un mensaje de transacción consumido desde RabbitMQ.
 * 
 * <p>Implementa idempotencia mediante el campo {@code messageId}:
 * <ul>
 *   <li>{@code messageId}: UUID único que identifica este mensaje.
 *       Se utiliza para detectar y descartar mensajes duplicados (reentregas de RabbitMQ).</li>
 * </ul>
 * 
 * <p>El procesamiento idempotente garantiza que si RabbitMQ reentrega el mismo mensaje
 * (por timeout, reinicio del consumidor, etc.), el monto no se acumulará múltiples veces
 * en el reporte.</p>
 */
public record TransactionMessage(
        @NotNull(message = "Message ID cannot be null") String messageId,
        @NotNull(message = "Transaction ID cannot be null") Long transactionId,
        @NotBlank(message = "User ID cannot be null or empty") String userId,
        @NotNull(message = "Transaction type cannot be null") TransactionType type,
        @NotNull(message = "Amount cannot be null") @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than 0") BigDecimal amount,
        @NotNull(message = "Date cannot be null") LocalDate date,
        @NotBlank(message = "Category cannot be null or empty") String category,
        String description,
        BigDecimal previousAmount,
        LocalDate previousDate) {
}