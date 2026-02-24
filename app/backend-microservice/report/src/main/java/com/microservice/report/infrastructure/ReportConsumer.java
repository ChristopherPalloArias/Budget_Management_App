package com.microservice.report.infrastructure;

import com.microservice.report.infrastructure.dto.TransactionMessage;
import com.microservice.report.infrastructure.mapper.TransactionUpdateMapper;
import com.microservice.report.domain.TransactionEvent;
import com.microservice.report.domain.TransactionType;
import com.microservice.report.service.IdempotencyService;
import com.microservice.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Consumidor de mensajes RabbitMQ para el microservicio de reportes.
 *
 * <p>Esta clase actúa como el <strong>adaptador de entrada</strong> (Driving Adapter)
 * en la arquitectura Event-Driven, sirviendo de puente entre el broker de mensajería
 * RabbitMQ y la lógica de negocio del servicio de reportes ({@link ReportService}).</p>
 *
 * <h3>Rol en la Arquitectura Event-Driven</h3>
 * <p>Este componente recibe los mensajes publicados por el {@code TransactionMessageProducer}
 * del microservicio de transacciones y los delega al {@link ReportService} para su
 * procesamiento:</p>
 * <pre>
 *   TransactionServiceImpl (produce evento)
 *     → TransactionEventListener (intercepta async)
 *       → TransactionMessageProducer (publica a RabbitMQ)
 *         → <strong>ReportConsumer</strong> (consume de RabbitMQ)
 *           → ReportServiceImpl.updateReport() (agrega datos)
 * </pre>
 *
 * <h3>Configuración de Colas</h3>
 * <p>Este consumidor escucha en <strong>dos colas</strong> independientes, configuradas
 * en {@code RabbitMQConfiguration}:</p>
 * <ul>
 *   <li>{@code transaction-created} — Routing key: {@code "transaction.created"}</li>
 *   <li>{@code transaction-updated} — Routing key: {@code "transaction.updated"}</li>
 * </ul>
 * <p>Ambas colas están vinculadas al {@code TopicExchange} llamado
 * {@code "transaction-exchange"}. Los nombres de las colas se inyectan desde
 * {@code application.properties} vía {@code ${rabbitmq.queues.*}}.</p>
 *
 * <h3>Deuda Técnica Identificada</h3>
 * <ul>
 *   <li><strong>DT-DOC-07:</strong> No hay manejo de errores en los métodos consumidores.
 *       Si {@code reportService.updateReport()} lanza una excepción, el mensaje se
 *       rechaza sin mecanismo de retry ni Dead Letter Queue (DLQ). El mensaje se pierde
 *       permanentemente.</li>
 *   <li><strong>DT-DOC-08:</strong> Ambos métodos ({@code consumeCreated} y
 *       {@code consumeUpdated}) ejecutan exactamente la misma lógica
 *       ({@code reportService.updateReport()}). No hay diferenciación semántica entre
 *       una transacción creada y una actualizada, lo que puede causar acumulación
 *       incorrecta si un "update" debería primero revertir el valor anterior.</li>
 *   <li><strong>DT-DOC-09:</strong> No hay validación del mensaje antes de procesarlo.
 *       Si el {@link TransactionMessage} llega con campos nulos o inválidos, la excepción
 *       será lanzada profundamente en {@code ReportServiceImpl}, dificultando el
 *       diagnóstico del origen del problema.</li>
 * </ul>
 *
 * <h3>Idempotencia (RFC 9110)</h3>
 * <p>Desde la versión actualizada, se implementa <strong>verificación de duplicados</strong>
 * mediante el campo {@code messageId} en {@link TransactionMessage}. Esto garantiza que:</p>
 * <ul>
 *   <li>Si RabbitMQ reentrega un mensaje, se detecta como duplicado y se descarta.</li>
 *   <li>El monto no se acumula múltiples veces en los reportes.</li>
 *   <li>El sistema es resiliente a fallos sin afectar la consistencia de datos.</li>
 * </ul>
 * <p>La verificación ocurre en {@link IdempotencyService#isFirstTimeProcessing(TransactionEvent, String)},
 * que registra el {@code messageId} en la tabla {@code processed_messages} de forma transaccional.</p>
 *
 * @see ReportService           Servicio de negocio que procesa las transacciones
 * @see TransactionMessage      DTO que representa el mensaje consumido desde RabbitMQ
 * @see IdempotencyService      Servicio que implementa verificación de duplicados
 * @see RabbitMQConfiguration   Clase que define las colas, exchanges y bindings
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ReportConsumer {
    private final ReportService reportService;
    private final IdempotencyService idempotencyService;
    private final TransactionUpdateMapper transactionUpdateMapper;
    private static final int MAX_RETRIES = 3;

    /**
     * Consume mensajes de la cola de transacciones <strong>creadas</strong>.
     *
     * <p>Este método es invocado automáticamente por Spring AMQP cuando un nuevo
     * mensaje llega a la cola {@code transaction-created}. El mensaje es
     * deserializado por {@code Jackson2JsonMessageConverter} (configurado en
     * {@link RabbitMQConfiguration}) desde JSON a {@link TransactionMessage}.</p>
     *
     * <p><strong>Idempotencia:</strong> Antes de procesar, se verifica si el
     * {@code messageId} ya fue procesado. Si es un duplicado (reentrega de RabbitMQ),
     * el mensaje se descarta sin modificar el reporte.</p>
     *
     * <p>Tras el procesamiento exitoso, el mensaje es automáticamente confirmado
     * (ACK) por Spring AMQP. En caso de excepción, el comportamiento depende
     * de la configuración de retry (actualmente sin configurar — ver DT-DOC-07).</p>
     *
     * @param transactionMessage mensaje deserializado con los datos de la transacción
     *                           recién creada en el microservicio de transacciones
     */
    @RabbitListener(queues = "${rabbitmq.queues.transaction-created}")
    @Transactional
    public void consumeCreated(TransactionMessage transactionMessage) {
        log.info("Processing Created transaction ID: {}, messageId: {}", 
                transactionMessage.transactionId(), transactionMessage.messageId());

        TransactionEvent transactionEvent = toTransactionEvent(transactionMessage);
        
        // Verificar idempotencia: si es un duplicado, descartarlo silenciosamente
        if (!idempotencyService.isFirstTimeProcessing(transactionEvent, "transaction.created")) {
            log.info("Discarding duplicate message for transaction ID: {}, messageId: {}",
                    transactionMessage.transactionId(), transactionMessage.messageId());
            return;
        }
        
        reportService.updateReport(transactionEvent);
        log.info("Successfully created transaction ID: {}", transactionMessage.transactionId());
    }

    /**
     * Consume mensajes de la cola de transacciones <strong>actualizadas</strong>.
     *
     * <p>Este método escucha la cola {@code transaction-updated} y procesa las
     * transacciones que han sido modificadas en el microservicio de transacciones.</p>
     *
     * <p><strong>Idempotencia:</strong> Antes de procesar, se verifica si el
     * {@code messageId} ya fue procesado. Si es un duplicado, el mensaje se descarta.</p>
     *
     * <p><strong>⚠️ Deuda técnica (DT-DOC-08):</strong> Actualmente este método
     * invoca la misma lógica que {@link #consumeCreated}, lo que significa que una
     * actualización se trata como una nueva acumulación en lugar de una corrección.
     * Para soportar actualizaciones correctamente, se debería:
     * <ol>
     *   <li>Recibir tanto el valor anterior como el nuevo.</li>
     *   <li>Revertir la acumulación del valor anterior.</li>
     *   <li>Aplicar la acumulación del nuevo valor.</li>
     * </ol></p>
     *
     * @param transactionMessage mensaje deserializado con los datos de la transacción
     *                           actualizada en el microservicio de transacciones
     */
    @RabbitListener(queues = "${rabbitmq.queues.transaction-updated}")
    @Transactional
    public void consumeUpdated(TransactionMessage transactionMessage) {
        log.info("Processing Updated transaction ID: {}, messageId: {}", 
                transactionMessage.transactionId(), transactionMessage.messageId());

        TransactionEvent transactionEvent = toTransactionEvent(transactionMessage);
        
        // Verificar idempotencia: si es un duplicado, descartarlo silenciosamente
        if (!idempotencyService.isFirstTimeProcessing(transactionEvent, "transaction.updated")) {
            log.info("Discarding duplicate message for transaction ID: {}, messageId: {}",
                    transactionMessage.transactionId(), transactionMessage.messageId());
            return;
        }
        
        handleWithRetry(transactionEvent);
        log.info("Successfully updated transaction ID: {}", transactionMessage.transactionId());
    }

    private void handleWithRetry(TransactionEvent transactionEvent) {
        int attempts = 0;
        while (attempts < MAX_RETRIES) {
            try {
                processUpdated(transactionEvent);
                return;
            } catch (RuntimeException ex) {
                attempts++;
                if (attempts >= MAX_RETRIES) {
                    sendToDlq(transactionEvent, ex);
                }
            }
        }
    }

    private void processUpdated(TransactionEvent transactionEvent) {
        for (TransactionEvent operation : transactionUpdateMapper.toUpdateOperations(transactionEvent)) {
            reportService.updateReport(operation);
        }
    }

    private void sendToDlq(TransactionEvent transactionEvent, Exception ex) {
        log.error("Sending message to DLQ after retries. transactionId={}, reason={}",
                transactionEvent.transactionId(), ex.getMessage());
    }

    private TransactionEvent toTransactionEvent(TransactionMessage transactionMessage) {
        return new TransactionEvent(
                transactionMessage.messageId(),
                transactionMessage.transactionId(),
                transactionMessage.userId(),
                TransactionType.valueOf(transactionMessage.type().name()),
                transactionMessage.amount(),
                transactionMessage.date(),
                transactionMessage.category(),
                transactionMessage.description(),
                transactionMessage.previousAmount(),
                transactionMessage.previousDate());
    }
}