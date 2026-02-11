package com.microservice.transaction.service;

import com.microservice.transaction.dto.PaginatedResponse;
import com.microservice.transaction.dto.TransactionRequest;
import com.microservice.transaction.dto.TransactionResponse;

import org.springframework.data.domain.Pageable;

public interface TransactionService {
    TransactionResponse create(TransactionRequest transactionRequest);

    TransactionResponse getById(Long id);

    PaginatedResponse<TransactionResponse> getAll(Pageable pageable);
}