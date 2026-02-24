package com.microservice.transaction.service;

import com.microservice.transaction.dto.PaginatedResponse;
import com.microservice.transaction.dto.TransactionRequest;
import com.microservice.transaction.dto.TransactionResponse;

import org.springframework.data.domain.Pageable;

/**
 * Servicio de transacciones con aislamiento de datos por usuario.
 * 
 * Todos los métodos reciben el userId del usuario autenticado,
 * garantizando que:
 * - Solo se crean/actualizan transacciones del usuario autenticado
 * - Solo se pueden recuperar transacciones propias
 * - Se previene el acceso cruzado a datos de otros usuarios
 */
public interface TransactionService {
    /**
     * Crea una nueva transacción para el usuario autenticado.
     * 
     * @param userId ID del usuario autenticado (extraído del token JWT)
     * @param transactionRequest datos de la transacción sin userId
     * @return respuesta con la transacción creada
     */
    TransactionResponse create(String userId, TransactionRequest transactionRequest);

    /**
     * Actualiza una transacción existente.
     * 
     * @param userId ID del usuario autenticado
     * @param id ID de la transacción a actualizar
     * @param transactionRequest nuevos datos
     * @return respuesta con la transacción actualizada
     * @throws AccessDeniedException si la transacción pertenece a otro usuario
     */
    TransactionResponse updateTransaction(String userId, Long id, TransactionRequest transactionRequest);

    /**
     * Obtiene una transacción específica del usuario autenticado.
     * 
     * @param userId ID del usuario autenticado
     * @param id ID de la transacción
     * @return respuesta con los datos de la transacción
     * @throws AccessDeniedException si la transacción pertenece a otro usuario
     * @throws NotFoundException si la transacción no existe
     */
    TransactionResponse getById(String userId, Long id);

    /**
     * Lista todas las transacciones del usuario autenticado (paginadas).
     * 
     * @param userId ID del usuario autenticado
     * @param pageable parámetros de paginación y ordering
     * @return respuesta paginada con transacciones del usuario
     */
    PaginatedResponse<TransactionResponse> getAll(String userId, Pageable pageable);

    /**
     * Lista todas las transacciones del usuario filtradas por periodo (yyyy-MM).
     */
    PaginatedResponse<TransactionResponse> getByPeriod(String userId, String period, Pageable pageable);

    /**
     * Elimina una transacción específica.
     */
    void delete(String userId, Long id);
}
