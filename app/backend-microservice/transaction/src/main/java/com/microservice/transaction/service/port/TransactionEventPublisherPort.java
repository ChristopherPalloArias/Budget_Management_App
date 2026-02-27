package com.microservice.transaction.service.port;

import com.microservice.transaction.model.Transaction;

public interface TransactionEventPublisherPort {
    void publishCreated(Transaction transaction);
    void publishUpdated(Transaction transaction);
    void publishDeleted(Transaction transaction);
}
