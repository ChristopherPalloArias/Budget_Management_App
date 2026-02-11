package com.microservice.transaction.infrastructure.listener;

import com.microservice.transaction.event.TransactionCreatedEvent;
import com.microservice.transaction.infrastructure.TransactionMessageProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionEventListener {

    private final TransactionMessageProducer transactionMessageProducer;

    @Async
    @EventListener
    public void handleTransactionCreatedEvent(TransactionCreatedEvent event) {
        log.info("Handling TransactionCreatedEvent for transaction: {}", event.getTransaction().getTransactionId());
        transactionMessageProducer.sendCreated(event.getTransaction());
    }
}
