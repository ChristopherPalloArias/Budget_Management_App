package com.microservice.report.model;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad que persiste los identificadores de mensajes ya procesados.
 * 
 * <p>Implementa el patrón de idempotencia para consumidores de eventos RabbitMQ.
 * Cuando un mensaje es procesado, su {@code messageId} se registra en esta tabla.
 * Antes de procesar un nuevo mensaje, se verifica si su {@code messageId} ya existe.
 * Si existe, el mensaje es descartado sin modificar el reporte.</p>
 * 
 * <p>Garantiza que:
 * <ul>
 *   <li>Los mensajes duplicados (reentregas de RabbitMQ) se detectan y descartan.</li>
 *   <li>No hay acumulación múltiple de montos en los reportes.</li>
 *   <li>El sistema es resiliente a fallos de RabbitMQ sin afectar la consistencia.</li>
 * </ul>
 * 
 * <p><strong>Constraint Único:</strong> El campo {@code messageId} tiene una constraint
 * de unicidad, permitiendo que la base de datos rechace intentos de inserción duplicada
 * en escenarios de alta concurrencia.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(
    name = "processed_messages",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_processed_messages_message_id",
            columnNames = {"message_id"}
        )
    }
)
public class ProcessedMessage {
    
    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    /**
     * Identificador único del mensaje (UUID).
     * Este ID se genera en el microservicio de transacciones y se envía en cada mensaje RabbitMQ.
     * Usado para detectar reentregas y duplicados.
     */
    @Column(name = "message_id", nullable = false, length = 36, updatable = false)
    private String messageId;
    
    /**
     * Tipo de evento del mensaje procesado.
     * Valores esperados: "transaction.created", "transaction.updated", "transaction.deleted".
     */
    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;
    
    /**
     * ID de la transacción asociada al mensaje.
     * Permite rastrear qué transacción fue procesada.
     */
    @Column(name = "transaction_id", nullable = false)
    private Long transactionId;
    
    /**
     * ID del usuario propietario de la transacción.
     * Permite rastrear y auditar quién afectó los reportes.
     */
    @Column(name = "user_id", nullable = false, length = 255)
    private String userId;
    
    /**
     * Timestamp de cuando se procesó y registró el mensaje.
     */
    @Column(name = "processed_at", nullable = false, updatable = false)
    private OffsetDateTime processedAt;
    
    @PrePersist
    public void prePersist() {
        if (this.processedAt == null) {
            this.processedAt = OffsetDateTime.now();
        }
    }
}
