package com.microservice.transaction.event;

import com.microservice.transaction.model.Transaction;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class TransactionUpdatedEvent extends ApplicationEvent {
    private final Transaction transaction;

    public TransactionUpdatedEvent(Object source, Transaction transaction) {
        super(source);
        this.transaction = transaction;
    }
}
