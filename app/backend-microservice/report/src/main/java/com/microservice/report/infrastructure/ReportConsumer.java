package com.microservice.report.infrastructure;

import com.microservice.report.infrastructure.dto.TransactionMessage;
import com.microservice.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

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
 * @see ReportService           Servicio de negocio que procesa las transacciones
 * @see TransactionMessage      DTO que representa el mensaje consumido desde RabbitMQ
 * @see RabbitMQConfiguration   Clase que define las colas, exchanges y bindings
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ReportConsumer {
    private final ReportService reportService;

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
    public void consumeCreated(TransactionMessage transactionMessage) {
        log.info("Processing Created transaction ID: {}", transactionMessage.transactionId());
        reportService.updateReport(transactionMessage);
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
    public void consumeUpdated(TransactionMessage transactionMessage) {
        log.info("Processing Updated transaction ID: {}", transactionMessage.transactionId());
        reportService.updateReport(transactionMessage);
        log.info("Successfully updated transaction ID: {}", transactionMessage.transactionId());
    }
}

