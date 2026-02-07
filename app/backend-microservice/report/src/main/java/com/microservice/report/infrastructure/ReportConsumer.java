package com.microservice.report.infrastructure;

import com.microservice.report.infrastructure.dto.TransactionMessage;
import com.microservice.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class ReportConsumer {
    private final ReportService reportService;

    @RabbitListener(queues = RabbitMQConfiguration.TRANSACTION_CREATED_QUEUE)
    public void consumeCreated(TransactionMessage transactionMessage) {
        log.info("Processing Created transaction ID: {}", transactionMessage.getTransactionId());
        reportService.updateReport(transactionMessage);
        log.info("Successfully created transaction ID: {}", transactionMessage.getTransactionId());
    }

    @RabbitListener(queues = RabbitMQConfiguration.TRANSACTION_UPDATED_QUEUE)
    public void consumeUpdated(TransactionMessage transactionMessage) {
        log.info("Processing Updated transaction ID: {}", transactionMessage.getTransactionId());
        reportService.updateReport(transactionMessage);
        log.info("Successfully updated transaction ID: {}", transactionMessage.getTransactionId());
    }
}
