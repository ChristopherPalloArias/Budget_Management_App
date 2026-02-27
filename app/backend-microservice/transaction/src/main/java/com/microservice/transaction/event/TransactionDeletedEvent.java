package com.microservice.transaction.event;

import com.microservice.transaction.model.Transaction;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class TransactionDeletedEvent extends ApplicationEvent {
    private final Transaction transaction;

    public TransactionDeletedEvent(Object source, Transaction transaction) {
        super(source);
        this.transaction = transaction;
    }
}
