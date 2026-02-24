package com.microservice.report.service;

import com.microservice.report.domain.TransactionEvent;
import com.microservice.report.model.ProcessedMessage;
import com.microservice.report.repository.ProcessedMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de idempotencia para procesamiento de eventos RabbitMQ.
 * 
 * <p>Implementa el patrón de idempotencia para garantizar que los mensajes duplicados
 * (por reentrega de RabbitMQ) se detecten y descarten sin afectar los reportes.</p>
 * 
 * <p>Flujo:
 * <ol>
 *   <li>Verificar si el {@code messageId} ya fue procesado.</li>
 *   <li>Si ya existe → retornar false (no procesar).</li>
 *   <li>Si no existe → registrar el {@code messageId} como procesado.</li>
 *   <li>Permitir que el consumidor procese el mensaje.</li>
 * </ol>
 * 
 * <p>La verificación y el registro ocurren en la misma transacción, garantizando
 * consistencia incluso en escenarios de alta concurrencia.</p>
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class IdempotencyService {
    
    private final ProcessedMessageRepository processedMessageRepository;
    
    /**
     * Verifica si un mensaje ya fue procesado y lo registra si es nuevo.
     * 
     * <p>Esta operación es atómica: la verificación y el registro ocurren en la
     * misma transacción, permitiendo que la base de datos rechace intentos de
     * inserción duplicada mediante la constraint de unicidad.</p>
     * 
     * @param transactionEvent evento a verificar
     * @param eventType tipo de evento ("transaction.created", "transaction.updated", etc.)
     * @return true si el mensaje es nuevo y debe ser procesado, false si es un duplicado
     */
    @Transactional
    public boolean isFirstTimeProcessing(TransactionEvent transactionEvent, String eventType) {
        // Verificar si el messageId ya existe
        if (processedMessageRepository.existsByMessageId(transactionEvent.messageId())) {
            log.warn("Duplicate message detected. messageId={}, eventType={}, transactionId={}. " +
                    "Message will be discarded to maintain idempotence.",
                    transactionEvent.messageId(), eventType, transactionEvent.transactionId());
            return false;
        }
        
        // Registrar el messageId como procesado
        ProcessedMessage processedMessage = ProcessedMessage.builder()
                .messageId(transactionEvent.messageId())
                .eventType(eventType)
                .transactionId(transactionEvent.transactionId())
                .userId(transactionEvent.userId())
                .build();
        
        try {
            processedMessageRepository.save(processedMessage);
            log.debug("Message registered as processed. messageId={}, eventType={}, transactionId={}",
                    transactionEvent.messageId(), eventType, transactionEvent.transactionId());
            return true;
        } catch (Exception ex) {
            // Si hay error al guardar (ej: constraint violation), asumir que es un duplicado
            log.warn("Error registering processed message (likely duplicate). messageId={}, error={}",
                    transactionEvent.messageId(), ex.getMessage());
            return false;
        }
    }
}
