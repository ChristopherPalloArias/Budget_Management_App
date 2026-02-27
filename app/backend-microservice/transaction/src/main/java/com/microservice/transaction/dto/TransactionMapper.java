package com.microservice.transaction.dto;

import com.microservice.transaction.model.Transaction;

import java.util.List;

/**
 * Mapper entre DTOs y entidades Transaction.
 * 
 * Nota: El método toRequest ahora recibe separadamente el userId
 * (extraído del token JWT) para garantizar that el DTO no pueda
 * especificar un userId diferente.
 */
public class TransactionMapper {
    public static Transaction toRequest(String userId, TransactionRequest dto) {
        Transaction entity = new Transaction();
        entity.setUserId(userId);
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
