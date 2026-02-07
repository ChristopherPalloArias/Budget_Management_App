package com.microservice.transaction.service.impl;

import java.util.List;
import java.util.Optional;

import com.microservice.transaction.exception.EntityNotFoundException;
import org.springframework.stereotype.Service;

import com.microservice.transaction.model.Transaction;
import com.microservice.transaction.repository.TransactionRepository;
import com.microservice.transaction.service.TransactionService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class TransactionServiceImpl implements TransactionService {
    private final TransactionRepository transactionRepository;

    @Override
    public Transaction create(Transaction transaction) {
        return transactionRepository.save(transaction);
    }

    @Override
    public Transaction getById(Integer id) {
        Optional<Transaction> opt = transactionRepository.findById(id);
        return opt.orElseThrow(() -> new EntityNotFoundException("Transaction not found"));
    }

    @Override
    public List<Transaction> getAll() {
        return transactionRepository.findAll();
    }
}
