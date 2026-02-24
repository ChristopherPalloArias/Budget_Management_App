package com.microservice.transaction.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.microservice.transaction.model.Transaction;

import java.util.List;

/**
 * Repositorio JPA para acceso a datos de transacciones.
 * 
 * Proporciona métodos especializados para filtrar transacciones
 * por userId, garantizando aislamiento de datos por usuario.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    /**
     * Busca todas las transacciones de un usuario sin paginación.
     * 
     * @param userId ID del usuario
     * @return lista de transacciones del usuario
     */
    List<Transaction> findByUserId(String userId);

    /**
     * Busca todas las transacciones de un usuario, ordenadas por fecha descendente,
     * con soporte para paginación.
     *
     * @param userId ID del usuario
     * @param pageable parámetros de paginación y ordenamiento
     * @return página de transacciones del usuario ordenadas por fecha (más recientes primero)
     */
    Page<Transaction> findByUserIdOrderByDateDesc(String userId, Pageable pageable);

    /**
     * Busca transacciones dentro de un rango de fechas para un usuario.
     */
    Page<Transaction> findByUserIdAndDateBetweenOrderByDateDesc(String userId, java.time.LocalDate start, java.time.LocalDate end, Pageable pageable);
}
