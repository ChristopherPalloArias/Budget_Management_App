package com.microservice.report.service;

import java.math.BigDecimal;
import java.util.List;

public interface TransactionClient {
    List<TransactionData> fetchTransactions(String period);
    
    record TransactionData(String type, BigDecimal amount) {}
}
