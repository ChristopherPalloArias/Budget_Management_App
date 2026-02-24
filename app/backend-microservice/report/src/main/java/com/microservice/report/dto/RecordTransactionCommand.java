package com.microservice.report.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RecordTransactionCommand(
        String userId,
        String type,
        BigDecimal amount,
        LocalDate date
) {}
