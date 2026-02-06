package com.microservice.transaction.controller;

import java.util.List;

import com.microservice.transaction.infrastructure.TransactionMessageProducer;
import jakarta.validation.Valid;
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
    public ResponseEntity<Transaction> create(@Valid @RequestBody Transaction transaction) {
        Transaction created = transactionService.create(transaction);
        transactionMessageProducer.sendCreated(created);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Transaction> getById(@PathVariable Integer id) {
        Transaction found = transactionService.getById(id);
        return ResponseEntity.ok(found);
    }

    @GetMapping
    public ResponseEntity<List<Transaction>> getAll() {
        return ResponseEntity.ok(transactionService.getAll());
    }
}
