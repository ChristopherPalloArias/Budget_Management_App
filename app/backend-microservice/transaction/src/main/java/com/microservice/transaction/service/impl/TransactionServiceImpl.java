package com.microservice.transaction.service.impl;

import java.util.List;

import com.microservice.transaction.dto.PaginatedResponse;
import com.microservice.transaction.dto.TransactionMapper;
import com.microservice.transaction.dto.TransactionRequest;
import com.microservice.transaction.dto.TransactionResponse;
import com.microservice.transaction.event.TransactionCreatedEvent;
import com.microservice.transaction.exception.EntityNotFoundException;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.microservice.transaction.model.Transaction;
import com.microservice.transaction.repository.TransactionRepository;
import com.microservice.transaction.service.TransactionService;

import lombok.RequiredArgsConstructor;

/**
 * Implementación principal del servicio de transacciones financieras.
 *
 * <p>Esta clase es el <strong>punto de entrada</strong> de la cadena Event-Driven
 * del sistema. Cuando se crea una nueva transacción, esta clase orquesta tres
 * operaciones en secuencia:</p>
 * <ol>
 *   <li>Mapeo del DTO de entrada a la entidad JPA vía {@link TransactionMapper}.</li>
 *   <li>Persistencia de la transacción en MySQL.</li>
 *   <li>Publicación de un {@link TransactionCreatedEvent} a través del
 *       {@link ApplicationEventPublisher} de Spring.</li>
 * </ol>
 *
 * <h3>Rol en la Arquitectura Event-Driven</h3>
 * <p>Esta clase actúa como el <strong>productor originario</strong> de la cadena:</p>
 * <pre>
 *   <strong>TransactionServiceImpl.create()</strong> (publica ApplicationEvent)
 *     → TransactionEventListener (intercepta @Async)
 *       → TransactionMessageProducer (envía a RabbitMQ)
 *         → ReportConsumer (consume)
 *           → ReportServiceImpl.updateReport() (agrega datos)
 * </pre>
 *
 * <p><strong>Desacoplamiento clave:</strong> Esta clase NO conoce RabbitMQ. Solo
 * publica un evento Spring interno ({@link TransactionCreatedEvent}), que es
 * interceptado asíncronamente por {@code TransactionEventListener} en un hilo
 * separado. Esto mantiene el servicio desacoplado del broker de mensajería.</p>
 *
 * <h3>Deuda Técnica Identificada</h3>
 * <ul>
 *   <li><strong>DT-DOC-04:</strong> El método {@code create()} no tiene
 *       {@code @Transactional}. Si {@code save()} ocurre pero el evento falla
 *       antes de publicarse (error en la misma transacción), NO hay rollback
 *       automático. Sin embargo, dado que el evento es {@code @Async}, esta
 *       omisión es menos crítica ya que el evento se procesa en otro hilo.</li>
 *   <li><strong>DT-DOC-05:</strong> Faltan operaciones {@code update()} y
 *       {@code delete()} — la interfaz {@link TransactionService} solo define
 *       {@code create}, {@code getById} y {@code getAll}. Esto limita la
 *       funcionalidad CRUD completa del microservicio.</li>
 *   <li><strong>DT-DOC-06:</strong> El método {@code getById()} lanza
 *       {@link EntityNotFoundException} con un mensaje genérico hardcodeado
 *       {@code "Transaction not found"}. Debería incluir el ID buscado para
 *       facilitar el debugging.</li>
 * </ul>
 *
 * @see TransactionService         Contrato (interfaz) que esta clase implementa
 * @see TransactionCreatedEvent    Evento Spring publicado tras crear una transacción
 * @see TransactionMapper          Utilidad de mapeo entre DTOs y entidades
 */
@RequiredArgsConstructor
@Service
public class TransactionServiceImpl implements TransactionService {
    private final TransactionRepository transactionRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Crea una nueva transacción financiera, la persiste y dispara el evento
     * que inicia la cadena Event-Driven hacia el microservicio de reportes.
     *
     * <h4>Flujo de ejecución:</h4>
     * <ol>
     *   <li>Convierte {@link TransactionRequest} → {@link Transaction} vía
     *       {@link TransactionMapper#toRequest}.</li>
     *   <li>Persiste la entidad en la base de datos MySQL
     *       ({@code transactionRepository.save()}).</li>
     *   <li>Publica {@link TransactionCreatedEvent} con la entidad persistida.
     *       Este evento es capturado asíncronamente por
     *       {@code TransactionEventListener}, que a su vez delega al
     *       {@code TransactionMessageProducer} para enviar el mensaje a RabbitMQ.</li>
     *   <li>Retorna la entidad mapeada a {@link TransactionResponse}.</li>
     * </ol>
     *
     * @param dto datos de la transacción validados desde el controller
     *            (validación Bean ocurre en {@code @Valid} del controller)
     * @return respuesta con los datos de la transacción creada, incluyendo el ID generado
     */
    @Override
    public TransactionResponse create(TransactionRequest dto) {
        Transaction entity = TransactionMapper.toRequest(dto);
        Transaction saved = transactionRepository.save(entity);
        eventPublisher.publishEvent(new TransactionCreatedEvent(this, saved));
        return TransactionMapper.toResponse(saved);
    }

    /**
     * Busca una transacción por su identificador único.
     *
     * @param id identificador de la transacción (PK auto-generado)
     * @return respuesta con los datos de la transacción encontrada
     * @throws EntityNotFoundException si no existe una transacción con el ID proporcionado
     */
    @Override
    public TransactionResponse getById(Long id) {
        Transaction found = transactionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found"));
        return TransactionMapper.toResponse(found);
    }

    /**
     * Obtiene todas las transacciones del sistema con paginación.
     *
     * <p><strong>Nota:</strong> Este método retorna las transacciones de <em>todos</em>
     * los usuarios, no filtradas por {@code userId}. El filtrado por usuario se
     * hace actualmente en el frontend. Esto representa un posible problema de
     * seguridad si no se implementa filtrado por usuario en el backend.</p>
     *
     * @param pageable parámetros de paginación (page, size, sort) inyectados por Spring
     * @return respuesta paginada con la lista de transacciones
     */
    @Override
    public PaginatedResponse<TransactionResponse> getAll(Pageable pageable) {
        Page<Transaction> page = transactionRepository.findAll(pageable);
        List<TransactionResponse> content = page.map(TransactionMapper::toResponse).getContent();

        return new PaginatedResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast());
    }

}

