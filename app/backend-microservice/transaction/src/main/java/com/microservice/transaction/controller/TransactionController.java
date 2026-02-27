package com.microservice.transaction.controller;

import com.microservice.transaction.dto.PaginatedResponse;
import com.microservice.transaction.dto.TransactionRequest;
import com.microservice.transaction.dto.TransactionResponse;
import com.microservice.transaction.service.TransactionService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

/**
 * Controlador REST para gestionar transacciones financieras.
 * 
 * Todos los endpoints requieren autenticación JWT.
 * El userId se extrae automáticamente del usuario autenticado,
 * garantizando que cada usuario solo puede ver/modificar sus propias transacciones.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/transactions")
public class TransactionController {
    private final TransactionService transactionService;
    private final MeterRegistry meterRegistry;
    private Counter transactionsCreatedCounter;
    private Counter transactionsUpdatedCounter;
    private Counter transactionsDeletedCounter;

    @PostConstruct
    void registerMetrics() {
        this.transactionsCreatedCounter = Counter
                .builder("app_transactions_created_total")
                .description("Total de transacciones creadas")
                .register(meterRegistry);
        this.transactionsUpdatedCounter = Counter
                .builder("app_transactions_updated_total")
                .description("Total de transacciones actualizadas")
                .register(meterRegistry);
        this.transactionsDeletedCounter = Counter
                .builder("app_transactions_deleted_total")
                .description("Total de transacciones eliminadas")
                .register(meterRegistry);
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> create(
            @Valid @RequestBody TransactionRequest dto,
            Principal principal) {
        String userId = principal.getName();
        TransactionResponse created = transactionService.create(userId, dto);
        transactionsCreatedCounter.increment();
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getById(
            @PathVariable Long id,
            Principal principal) {
        String userId = principal.getName();
        TransactionResponse found = transactionService.getById(userId, id);
        return ResponseEntity.ok(found);
    }

    @GetMapping
    public ResponseEntity<PaginatedResponse<TransactionResponse>> getAll(
            Principal principal,
            @RequestParam(required = false) String period,
            @PageableDefault(size = 10, page = 0, sort = "date", direction = Sort.Direction.DESC) Pageable pageable) {
        String userId = principal.getName();
        if (period != null && !period.isBlank()) {
            return ResponseEntity.ok(transactionService.getByPeriod(userId, period, pageable));
        }
        return ResponseEntity.ok(transactionService.getAll(userId, pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody TransactionRequest dto,
            Principal principal) {
        String userId = principal.getName();
        TransactionResponse updated = transactionService.updateTransaction(userId, id, dto);
        transactionsUpdatedCounter.increment();
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            Principal principal) {
        String userId = principal.getName();
        transactionService.delete(userId, id);
        transactionsDeletedCounter.increment();
        return ResponseEntity.noContent().build();
    }
}
