package com.microservice.transaction.service.impl;

import java.util.List;

import com.microservice.transaction.dto.PaginatedResponse;
import com.microservice.transaction.dto.TransactionMapper;
import com.microservice.transaction.dto.TransactionRequest;
import com.microservice.transaction.dto.TransactionResponse;
import com.microservice.transaction.exception.NotFoundException;
import com.microservice.transaction.exception.ValidationException;
import com.microservice.transaction.service.port.TransactionEventPublisherPort;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.microservice.transaction.model.Transaction;
import com.microservice.transaction.repository.TransactionRepository;
import com.microservice.transaction.service.TransactionService;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

/**
 * Implementación del servicio de transacciones con aislamiento de datos.
 *
 * <p>Esta clase garantiza que cada usuario solo puede acceder a sus propias
 * transacciones mediante validación en cada operación CRUD. El userId se
 * extrae del token JWT en el controlador y se pasa explícitamente a cada
 * método del servicio.</p>
 *
 * <h3>Flujo de Seguridad</h3>
 * <ol>
 *   <li>JwtAuthenticationFilter valida el token y establece el SecurityContext</li>
 *   <li>TransactionController extrae el userId de Principal</li>
 *   <li>TransactionServiceImpl recibe el userId y lo utiliza para:
 *       <ul>
 *         <li>Inyectar automáticamente en nuevas transacciones</li>
 *         <li>Validar que las transacciones existentes pertenecen al usuario</li>
 *         <li>Filtrar listas por userId</li>
 *       </ul>
 *   </li>
 * </ol>
 *
 * <h3>Transaccionalidad y Publicación de Eventos</h3>
 * <p>El método {@code create()} está anotado con {@code @Transactional} para garantizar
 * consistencia de datos entre el almacenamiento de la transacción y la publicación del evento.
 * Si {@code eventPublisher.publishCreated()} falla con una excepción en tiempo de ejecución,
 * la transacción completa (incluyendo el {@code save()}) se revierte automáticamente.</p>
 * 
 * <p><strong>Nota:</strong> Esta implementación asume que RabbitMQ está disponible y la 
 * publicación es relativamente rápida. Para garantizar consistencia eventual en caso de 
 * fallos prolongados de RabbitMQ, considerar implementar el Patrón Outbox en futuras mejoras.</p>
 *
 * @see TransactionService
 * @see JwtAuthenticationFilter
 */
@RequiredArgsConstructor
@Service
public class TransactionServiceImpl implements TransactionService {
    private static final Logger logger = LoggerFactory.getLogger(TransactionServiceImpl.class);
    
    private final TransactionRepository transactionRepository;
    private final TransactionEventPublisherPort eventPublisher;

    @Override
    @Transactional
    public TransactionResponse create(String userId, TransactionRequest dto) {
        validateAmount(dto.amount());
        
        Transaction entity = TransactionMapper.toRequest(userId, dto);
        
        Transaction saved = transactionRepository.save(entity);
        
        // Publicar evento. Si falla, la transacción se revierte automáticamente por @Transactional.
        // No capturamos la excepción para permitir que se propague y active el rollback.
        try {
            eventPublisher.publishCreated(saved);
        } catch (RuntimeException ex) {
            // Logging defensivo: registrar el error de publicación para debugging.
            // La excepción se propaga para que Spring revierte la transacción.
            logger.error("Failed to publish transaction creation event for transaction ID: {} (userId: {}). " +
                    "Database transaction will be rolled back.", saved.getId(), userId, ex);
            throw ex;
        }
        
        return TransactionMapper.toResponse(saved);
    }

    @Override
    public TransactionResponse updateTransaction(String userId, Long id, TransactionRequest dto) {
        validateAmount(dto.amount());

        Transaction existing = transactionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Transaction not found"));

        // Validar que la transacción pertenece al usuario autenticado
        validateTransactionOwnership(userId, existing);

        applyUpdates(existing, dto, userId);

        Transaction saved = transactionRepository.save(existing);
        eventPublisher.publishUpdated(saved);
        return TransactionMapper.toResponse(saved);
    }

    private void validateAmount(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Amount must be greater than zero");
        }
    }

    private void applyUpdates(Transaction transaction, TransactionRequest dto, String userId) {
        transaction.setType(dto.type());
        transaction.setAmount(dto.amount());
        transaction.setCategory(dto.category());
        transaction.setDate(dto.date());
        transaction.setDescription(dto.description());
        // El userId SIEMPRE viene del token, NUNCA del DTO
        transaction.setUserId(userId);
    }

    /**
     * Busca una transacción por su ID, validando que pertenece al usuario autenticado.
     *
     * @param userId ID del usuario autenticado
     * @param id ID de la transacción
     * @return respuesta con los datos de la transacción
     * @throws NotFoundException si la transacción no existe o no pertenece al usuario
     */
    @Override
    public TransactionResponse getById(String userId, Long id) {
        Transaction found = transactionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Transaction not found"));
        
        // Si la transacción no pertenece al usuario, lanzar excepción
        validateTransactionOwnership(userId, found);
        
        return TransactionMapper.toResponse(found);
    }

    @Override
    public PaginatedResponse<TransactionResponse> getAll(String userId, Pageable pageable) {
        // Filtrar por userId en la query de la base de datos
        Page<Transaction> page = transactionRepository.findByUserIdOrderByDateDesc(userId, pageable);
        List<TransactionResponse> content = page.map(TransactionMapper::toResponse).getContent();

        return new PaginatedResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast());
    }

    /**
     * Lista todas las transacciones del usuario filtradas por periodo (yyyy-MM).
     */
    @Override
    public PaginatedResponse<TransactionResponse> getByPeriod(String userId, String period, Pageable pageable) {
        java.time.YearMonth yearMonth = java.time.YearMonth.parse(period);
        java.time.LocalDate start = yearMonth.atDay(1);
        java.time.LocalDate end = yearMonth.atEndOfMonth();

        Page<Transaction> page = transactionRepository.findByUserIdAndDateBetweenOrderByDateDesc(userId, start, end, pageable);
        List<TransactionResponse> content = page.map(TransactionMapper::toResponse).getContent();

        return new PaginatedResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast());
    }

    /**
     * Valida que una transacción pertenece al usuario autenticado.
     * Lanza una excepción si no es así (para mantener concepto de no divulgar IDs de otros usuarios).
     *
     * @param userId ID del usuario autenticado
     * @param transaction entidad a validar
     * @throws NotFoundException si la transacción no pertenece al usuario
     */
    private void validateTransactionOwnership(String userId, Transaction transaction) {
        if (!transaction.getUserId().equals(userId)) {
            throw new NotFoundException("Transaction not found");
        }
    }

    @Override
    public void delete(String userId, Long id) {
        Transaction existing = transactionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Transaction not found"));

        // Validar que la transacción pertenece al usuario autenticado
        validateTransactionOwnership(userId, existing);

        transactionRepository.delete(existing);
        eventPublisher.publishDeleted(existing);
    }

    /**
     * TODO (Future Enhancement): Implement Outbox Pattern for Event Publishing
     * 
     * <p>Current Limitation:</p>
     * The current implementation ensures transactional consistency through @Transactional,
     * but only as long as RabbitMQ is available and responsive. If RabbitMQ is down,
     * the entire create() operation fails and rolls back, potentially causing poor user experience.
     * 
     * <p>Outbox Pattern Solution:</p>
     * Create an OutboxEvent table in the database and:
     * 1. Store the domain event in OutboxEvent table within the same @Transactional boundary
     * 2. Use a separate background job/scheduler to:
     *    - Poll OutboxEvent table for unpublished events
     *    - Publish events to RabbitMQ
     *    - Mark events as published
     *    - Retry with exponential backoff on failures
     * 
     * <p>Benefits:</p>
     * - Decouples database transaction from RabbitMQ availability
     * - Guarantees eventual consistency (all events are eventually published)
     * - Improves resilience to temporary RabbitMQ downtime
     * - Better user experience (create succeeds even if RabbitMQ is momentarily down)
     * 
     * <p>References:</p>
     * - Event Sourcing and CQRS patterns
     * - Spring Framework: SubscribableChannel, MessageHandler
     * - Temporal coupling vs eventual consistency
     */
}
