package com.microservice.report.infrastructure.mapper;

import com.microservice.report.infrastructure.dto.TransactionMessage;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TransactionUpdateMapper {

    public List<TransactionMessage> toUpdateOperations(TransactionMessage message) {
        if (!hasPreviousValues(message)) {
            return List.of(message);
        }

        TransactionMessage reversal = new TransactionMessage(
                message.transactionId(),
                message.userId(),
                message.type(),
                message.previousAmount().negate(),
                message.previousDate(),
                message.category(),
                message.description(),
                null,
                null
        );

        return List.of(reversal, message);
    }

    private boolean hasPreviousValues(TransactionMessage message) {
        return message.previousAmount() != null && message.previousDate() != null;
    }
}