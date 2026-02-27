package com.microservice.transaction.service.impl;

import java.util.List;

import com.microservice.transaction.dto.PaginatedResponse;
import com.microservice.transaction.dto.TransactionMapper;
import com.microservice.transaction.dto.TransactionRequest;
import com.microservice.transaction.dto.TransactionResponse;
import com.microservice.transaction.exception.NotFoundException;
import com.microservice.transaction.exception.ValidationException;
import com.microservice.transaction.service.port.TransactionEventPublisherPort;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.microservice.transaction.model.Transaction;
import com.microservice.transaction.repository.TransactionRepository;
import com.microservice.transaction.service.TransactionService;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

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
 * @see TransactionService
 * @see JwtAuthenticationFilter
 */
@RequiredArgsConstructor
@Service
public class TransactionServiceImpl implements TransactionService {
    private final TransactionRepository transactionRepository;
    private final TransactionEventPublisherPort eventPublisher;

    @Override
    public TransactionResponse create(String userId, TransactionRequest dto) {
        validateAmount(dto.amount());
        
        Transaction entity = TransactionMapper.toRequest(userId, dto);
        
        Transaction saved = transactionRepository.save(entity);
        eventPublisher.publishCreated(saved);
        return TransactionMapper.toResponse(saved);
    }

    @Override
    public TransactionResponse updateTransaction(String userId, Long id, TransactionRequest dto) {
        validateAmount(dto.amount());

        Transaction existing = transactionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Transaction not found with id: " + id));

        validateTransactionOwnership(userId, existing, id);
        applyUpdates(existing, dto, userId);

        Transaction saved = transactionRepository.save(existing);
        eventPublisher.publishUpdated(saved);
        return TransactionMapper.toResponse(saved);
    }

    /**
     * Valida que el monto sea positivo.
     *
     * @param amount monto a validar
     * @throws ValidationException si el monto es menor o igual a cero
     */
    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Amount must be greater than zero");
        }
    }

    /**
     * Aplica actualizaciones a una transacción existente.
     * Actualiza solo los campos mutables, preservando id y campos de auditoría.
     *
     * @param transaction transacción a actualizar
     * @param dto nuevos datos de la transacción
     * @param userId ID del usuario autenticado (del token, no del DTO)
     */
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
                .orElseThrow(() -> new NotFoundException("Transaction not found with id: " + id));
        
        validateTransactionOwnership(userId, found, id);
        
        return TransactionMapper.toResponse(found);
    }

    /**
     * Lista todas las transacciones del usuario autenticado (paginadas).
     *
     * @param userId ID del usuario autenticado
     * @param pageable parámetros de paginación y ordering
     * @return respuesta paginada con transacciones del usuario
     */
    @Override
    public PaginatedResponse<TransactionResponse> getAll(String userId, Pageable pageable) {
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
     *
     * @param userId ID del usuario autenticado
     * @param period período en formato yyyy-MM
     * @param pageable parámetros de paginación y ordering
     * @return respuesta paginada con transacciones del período
     */
    @Override
    public PaginatedResponse<TransactionResponse> getByPeriod(String userId, String period, Pageable pageable) {
        YearMonth yearMonth = YearMonth.parse(period);
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();

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
     * Lanza una excepción si no es así (para no divulgar IDs de otros usuarios).
     *
     * @param userId ID del usuario autenticado
     * @param transaction entidad a validar
     * @param id ID de la transacción (para mensajes de error)
     * @throws NotFoundException si la transacción no pertenece al usuario
     */
    private void validateTransactionOwnership(String userId, Transaction transaction, Long id) {
        if (!transaction.getUserId().equals(userId)) {
            throw new NotFoundException("Transaction not found with id: " + id);
        }
    }

    @Override
    public void delete(String userId, Long id) {
        Transaction existing = transactionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Transaction not found with id: " + id));

        validateTransactionOwnership(userId, existing, id);

        transactionRepository.delete(existing);
        eventPublisher.publishDeleted(existing);
    }
}
