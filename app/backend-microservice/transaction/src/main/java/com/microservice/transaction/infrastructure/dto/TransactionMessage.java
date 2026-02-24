package com.microservice.transaction.infrastructure.dto;

import com.microservice.transaction.model.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * DTO que representa un mensaje de transacción publicado a RabbitMQ.
 * 
 * <p>Implementa idempotencia mediante el campo {@code messageId}:
 * <ul>
 *   <li>{@code messageId}: UUID único que identifica este mensaje.
 *       Se genera en el productor y se envía en cada mensaje.
 *       Usado por el consumidor para detectar reentregas/duplicados.</li>
 * </ul>
 * 
 * <p>El UUID se genera una única vez en el productor y viaja con el mensaje
 * a través de RabbitMQ, permitiendo que el consumidor (ReportConsumer) verifique
 * si el mensaje ya fue procesado.</p>
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class TransactionMessage {
    /**
     * Identificador único del mensaje (UUID).
     * Se genera en el productor y se utiliza para detectar reentregas.
     * Default: UUID.randomUUID().toString() si no se proporciona.
     */
    @Builder.Default
    private String messageId = UUID.randomUUID().toString();
    
    private Long transactionId;
    private String userId;
    private TransactionType type;
    private BigDecimal amount;
    private String category;
    private LocalDate date;
    private String description;
    private OffsetDateTime createdAt;
}