package com.microservice.transaction.infrastructure;

import com.microservice.transaction.event.TransactionCreatedEvent;
import com.microservice.transaction.event.TransactionUpdatedEvent;
import com.microservice.transaction.model.Transaction;
import com.microservice.transaction.service.port.TransactionEventPublisherPort;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class SpringTransactionEventPublisher implements TransactionEventPublisherPort {
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void publishCreated(Transaction transaction) {
        eventPublisher.publishEvent(new TransactionCreatedEvent(this, transaction));
    }

    @Override
    public void publishUpdated(Transaction transaction) {
        eventPublisher.publishEvent(new TransactionUpdatedEvent(this, transaction));
    }
}
