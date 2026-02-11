package com.microservice.transaction.dto;

import com.microservice.transaction.model.Transaction;

import java.util.List;

public class TransactionMapper {
    public static Transaction toRequest(TransactionRequest dto) {
        Transaction entity = new Transaction();
        entity.setUserId(dto.userId());
        entity.setType(dto.type());
        entity.setAmount(dto.amount());
        entity.setCategory(dto.category());
        entity.setDate(dto.date());
        entity.setDescription(dto.description());
        return entity;
    }

    public static TransactionResponse toResponse(Transaction entity) {
        return new TransactionResponse(
                entity.getTransactionId(),
                entity.getUserId(),
                entity.getType(),
                entity.getAmount(),
                entity.getCategory(),
                entity.getDate(),
                entity.getDescription(),
                entity.getCreatedAt()
        );
    }

    public static List<TransactionResponse> toResponseDTOList(List<Transaction> entities) {
        return entities.stream()
                .map(TransactionMapper::toResponse)
                .toList();
    }

}
