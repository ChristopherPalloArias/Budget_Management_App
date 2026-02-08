package com.microservice.transaction.infrastructure;

import com.microservice.transaction.infrastructure.dto.TransactionMessage;
import com.microservice.transaction.model.Transaction;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class TransactionMessageProducer {
    private final RabbitTemplate rabbitTemplate;

    public void sendCreated(Transaction transaction) {
        TransactionMessage message = toMessage(transaction);
        rabbitTemplate.convertAndSend("transaction-exchange", "transaction.created", message);
    }

    public void sendUpdated(Transaction transaction) {
        TransactionMessage message = toMessage(transaction);
        rabbitTemplate.convertAndSend("transaction-exchange", "transaction.updated", message);
    }

    private TransactionMessage toMessage(Transaction transaction) {
        return TransactionMessage.builder()
                .transactionId(transaction.getTransactionId())
                .userId(transaction.getUserId())
                .amount(transaction.getAmount())
                .category(transaction.getCategory())
                .date(transaction.getDate())
                .description(transaction.getDescription())
                .type(transaction.getType())
                .build();
    }
}
