package com.microservice.transaction.controller;

import java.util.List;

import com.microservice.transaction.dto.TransactionMapper;
import com.microservice.transaction.dto.TransactionRequest;
import com.microservice.transaction.dto.TransactionResponse;
import com.microservice.transaction.infrastructure.TransactionMessageProducer;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.microservice.transaction.model.Transaction;
import com.microservice.transaction.service.TransactionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/transactions")
public class TransactionController {
    private final TransactionService transactionService;
    private final TransactionMessageProducer transactionMessageProducer;

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
    public ResponseEntity<List<TransactionResponse>> getAll(@RequestParam String userId) {
        List<TransactionResponse> transactions = transactionService.getByUserId(userId);
        return ResponseEntity.ok(transactions);
    }
}
