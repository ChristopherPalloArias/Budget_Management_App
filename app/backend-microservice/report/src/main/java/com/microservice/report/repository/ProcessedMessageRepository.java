package com.microservice.report.repository;

import com.microservice.report.model.ProcessedMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository para acceder a la tabla {@code processed_messages}.
 * 
 * Permite consultar y persistir registros de mensajes ya procesados,
 * implementando el patrón de idempotencia para consumidores RabbitMQ.
 */
@Repository
public interface ProcessedMessageRepository extends JpaRepository<ProcessedMessage, Long> {
    
    /**
     * Busca un mensaje procesado por su ID único (messageId).
     * 
     * @param messageId UUID único del mensaje
     * @return Optional con el ProcessedMessage si existe, vacío en caso contrario
     */
    Optional<ProcessedMessage> findByMessageId(String messageId);
    
    /**
     * Verifica si un mensaje con el dado messageId ya fue procesado.
     * 
     * @param messageId UUID único del mensaje
     * @return true si existe el mensaje, false en caso contrario
     */
    boolean existsByMessageId(String messageId);
}
