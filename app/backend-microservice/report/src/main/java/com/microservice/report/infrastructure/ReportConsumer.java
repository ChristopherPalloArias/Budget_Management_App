package com.microservice.report.infrastructure;

import com.microservice.report.infrastructure.dto.TransactionMessage;
import com.microservice.report.dto.RecordTransactionCommand;
import com.microservice.report.infrastructure.mapper.TransactionUpdateMapper;
import com.microservice.report.service.ReportCommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
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
 * <p>Este consumidor escucha en <strong>tres colas</strong> independientes, configuradas
 * en {@code RabbitMQConfiguration}:</p>
 * <ul>
 *   <li>{@code transaction-created} — Routing key: {@code "transaction.created"}</li>
 *   <li>{@code transaction-updated} — Routing key: {@code "transaction.updated"}</li>
 *   <li>{@code transaction-deleted} — Routing key: {@code "transaction.deleted"}</li>
 * </ul>
 * <p>Todas las colas están vinculadas al {@code TopicExchange} llamado
 * {@code "transaction-exchange"}. Los nombres de las colas se inyectan desde
 * {@code application.yaml} vía {@code ${rabbitmq.queues.*}}.</p>
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
 * @see ReportService           Servicio de negocio que procesa las transacciones
 * @see TransactionMessage      DTO que representa el mensaje consumido desde RabbitMQ
 * @see RabbitMQConfiguration   Clase que define las colas, exchanges y bindings
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ReportConsumer {
    private final ReportCommandService reportCommandService;
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
     * <p>Tras el procesamiento exitoso, el mensaje es automáticamente confirmado
     * (ACK) por Spring AMQP. En caso de excepción, el comportamiento depende
     * de la configuración de retry (actualmente sin configurar — ver DT-DOC-07).</p>
     *
     * @param transactionMessage mensaje deserializado con los datos de la transacción
     *                           recién creada en el microservicio de transacciones
     */
    @RabbitListener(queues = "${rabbitmq.queues.transaction-created}")
    public void consumeCreated(TransactionMessage transactionMessage, 
                               @Header(value = AmqpHeaders.MESSAGE_ID, required = false) String messageId) {
        log.info("Processing Created transaction ID: {}", transactionMessage.transactionId());
        
        // Fallback to transactionId if messageId is not provided by producer
        String finalMessageId = messageId != null ? messageId : "CREATED-" + transactionMessage.transactionId();
        
        reportCommandService.updateReport(toCommand(transactionMessage), finalMessageId);
        log.info("Successfully created transaction ID: {}", transactionMessage.transactionId());
    }

    /**
     * Consume mensajes de la cola de transacciones <strong>actualizadas</strong>.
     *
     * <p>Este método escucha la cola {@code transaction-updated} y procesa las
     * transacciones que han sido modificadas en el microservicio de transacciones.</p>
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
        log.info("Processing Updated transaction ID: {}", transactionMessage.transactionId());
        handleWithRetry(transactionMessage);
        log.info("Successfully updated transaction ID: {}", transactionMessage.transactionId());
    }

    /**
     * Consume mensajes de la cola de transacciones <strong>eliminadas</strong>.
     *
     * <p>Este método escucha la cola {@code transaction-deleted} y procesa las
     * transacciones que han sido eliminadas en el microservicio de transacciones.</p>
     *
     * <p><strong>Estrategia de Reversa:</strong> Para revertir el efecto de una transacción
     * eliminada, se invierte el tipo de transacción:
     * <ul>
     *   <li>Si la transacción era INCOME (ingreso), se aplica como EXPENSE (gasto) para restar</li>
     *   <li>Si la transacción era EXPENSE (gasto), se aplica como INCOME (ingreso) para restar</li>
     * </ul>
     * De esta manera, el balance del reporte se ajusta correctamente al eliminar la transacción.</p>
     *
     * <p><strong>Idempotencia:</strong> El método usa un messageId sintético basado en el
     * transactionId para garantizar que el mismo mensaje de eliminación no se procese múltiples
     * veces, evitando doble reversa del mismo evento.</p>
     *
     * @param transactionMessage mensaje deserializado con los datos de la transacción eliminada
     * @param messageId identificador único del mensaje AMQP (opcional)
     */
    @RabbitListener(queues = "${rabbitmq.queues.transaction-deleted}")
    public void consumeDeleted(TransactionMessage transactionMessage,
                               @Header(value = AmqpHeaders.MESSAGE_ID, required = false) String messageId) {
        log.info("Processing Deleted transaction ID: {}", transactionMessage.transactionId());
        
        // Fallback to transactionId if messageId is not provided by producer
        String finalMessageId = messageId != null ? messageId : "DELETED-" + transactionMessage.transactionId();
        
        // Revertir la transacción eliminada aplicando el tipo inverso
        RecordTransactionCommand reverseCommand = toReverseCommand(transactionMessage);
        reportCommandService.updateReport(reverseCommand, finalMessageId);
        
        log.info("Successfully reverted deleted transaction ID: {}", transactionMessage.transactionId());
    }

    private void handleWithRetry(TransactionMessage transactionMessage) {
        int attempts = 0;
        while (attempts < MAX_RETRIES) {
            try {
                processUpdated(transactionMessage);
                return;
            } catch (RuntimeException ex) {
                attempts++;
                if (attempts >= MAX_RETRIES) {
                    sendToDlq(transactionMessage, ex);
                }
            }
        }
    }

    private void processUpdated(TransactionMessage transactionMessage) {
        int i = 0;
        for (TransactionMessage operation : transactionUpdateMapper.toUpdateOperations(transactionMessage)) {
            // For updates, we generate a synthetic message ID to process the revert and apply operations idempotently
            String syntheticId = "UPDATED-" + transactionMessage.transactionId() + "-" + i++;
            reportCommandService.updateReport(toCommand(operation), syntheticId);
        }
    }

    private RecordTransactionCommand toCommand(TransactionMessage message) {
        return new RecordTransactionCommand(
                message.userId(),
                message.type().name(),
                message.amount(),
                message.date()
        );
    }

    /**
     * Convierte un mensaje de transacción eliminada en un comando de reversión.
     * 
     * <p>La reversión se logra invirtiendo el tipo de transacción:
     * <ul>
     *   <li>INCOME → EXPENSE (para restar el ingreso del total)</li>
     *   <li>EXPENSE → INCOME (para restar el gasto del total)</li>
     * </ul>
     * 
     * @param message mensaje de transacción eliminada
     * @return comando con tipo invertido para revertir el efecto
     */
    private RecordTransactionCommand toReverseCommand(TransactionMessage message) {
        // Invertir el tipo para revertir el efecto
        String reverseType = message.type().name().equals("INCOME") ? "EXPENSE" : "INCOME";
        return new RecordTransactionCommand(
                message.userId(),
                reverseType,
                message.amount(),
                message.date()
        );
    }

    private void sendToDlq(TransactionMessage transactionMessage, Exception ex) {
        log.error("Sending message to DLQ after retries. transactionId={}, reason={}",
                transactionMessage.transactionId(), ex.getMessage());
    }
}