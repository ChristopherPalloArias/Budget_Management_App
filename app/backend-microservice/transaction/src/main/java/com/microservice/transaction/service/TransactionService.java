package com.microservice.transaction.service;

import java.util.List;

import com.microservice.transaction.dto.TransactionRequest;
import com.microservice.transaction.dto.TransactionResponse;
import com.microservice.transaction.model.Transaction;

public interface TransactionService {
    TransactionResponse create(TransactionRequest transactionRequest);
    TransactionResponse getById(Long id);
    List<TransactionResponse> getAll();
    List<TransactionResponse> getByUserId(String userId);
}