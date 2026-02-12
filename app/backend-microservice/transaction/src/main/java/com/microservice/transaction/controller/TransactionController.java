package com.microservice.transaction.controller;

import com.microservice.transaction.dto.PaginatedResponse;
import com.microservice.transaction.dto.TransactionRequest;
import com.microservice.transaction.dto.TransactionResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;

import com.microservice.transaction.service.TransactionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/transactions")
public class TransactionController {
    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<TransactionResponse> create(@Valid @RequestBody TransactionRequest dto) {
        TransactionResponse created = transactionService.create(dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getById(@PathVariable Long id) {
        TransactionResponse found = transactionService.getById(id);
        return ResponseEntity.ok(found);
    }

    @GetMapping
    public ResponseEntity<PaginatedResponse<TransactionResponse>> getAll(
            @PageableDefault(size = 10, page = 0, sort = "date", direction = Sort.Direction.DESC) Pageable pageable) {
        PaginatedResponse<TransactionResponse> transactions = transactionService.getAll(pageable);
        return ResponseEntity.ok(transactions);
    }
}
