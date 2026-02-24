package com.microservice.report.infrastructure.mapper;

import com.microservice.report.domain.TransactionEvent;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TransactionUpdateMapper {

    public List<TransactionEvent> toUpdateOperations(TransactionEvent event) {
        if (!hasPreviousValues(event)) {
            return List.of(event);
        }

        TransactionEvent reversal = new TransactionEvent(
                event.messageId(),
                event.transactionId(),
                event.userId(),
                event.type(),
                event.previousAmount().negate(),
                event.previousDate(),
                event.category(),
                event.description(),
                null,
                null
        );

        return List.of(reversal, event);
    }

    private boolean hasPreviousValues(TransactionEvent event) {
        return event.previousAmount() != null && event.previousDate() != null;
    }
}