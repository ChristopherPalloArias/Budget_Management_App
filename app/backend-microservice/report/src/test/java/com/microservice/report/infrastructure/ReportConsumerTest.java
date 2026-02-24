package com.microservice.report.infrastructure;

import com.microservice.report.infrastructure.dto.TransactionMessage;
import com.microservice.report.infrastructure.dto.TransactionType;
import com.microservice.report.infrastructure.mapper.TransactionUpdateMapper;
import com.microservice.report.service.IdempotencyService;
import com.microservice.report.service.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ReportConsumer message processing with idempotence.
 * 
 * Validates that:
 * - New messages are processed and update reports
 * - Duplicate messages are detected and discarded
 * - Report totals are not affected by redelivered messages
 */
@DisplayName("ReportConsumer - Event Processing with Idempotence")
@ExtendWith(MockitoExtension.class)
class ReportConsumerTest {

    @Mock
    private ReportService reportService;

    @Mock
    private IdempotencyService idempotencyService;

    @Mock
    private TransactionUpdateMapper transactionUpdateMapper;

    @InjectMocks
    private ReportConsumer reportConsumer;

    private TransactionMessage testMessage;
    private String messageId;

    @BeforeEach
    void setUp() {
        messageId = UUID.randomUUID().toString();
        testMessage = TransactionMessage.builder()
                .messageId(messageId)
                .transactionId(100L)
                .userId("user-123")
                .type(TransactionType.INCOME)
                .amount(BigDecimal.valueOf(1000))
                .category("Salary")
                .date(LocalDate.of(2026, 2, 24))
                .description("Monthly salary")
                .build();
    }

    @Nested
    @DisplayName("consumeCreated - New Messages")
    class ConsumeCreatedNewMessages {

        @Test
        @DisplayName("Should process created message and update report when first time")
        void consumeCreated_WhenNewMessage_ShouldUpdateReport() {
            // Given: message is new (not duplicate)
            when(idempotencyService.isFirstTimeProcessing(testMessage, "transaction.created"))
                    .thenReturn(true);

            // When
            reportConsumer.consumeCreated(testMessage);

            // Then
            verify(idempotencyService).isFirstTimeProcessing(testMessage, "transaction.created");
            verify(reportService).updateReport(testMessage);
        }

        @Test
        @DisplayName("Should verify idempotency before processing")
        void consumeCreated_ShouldCheckIdempotencyFirst() {
            // Given
            when(idempotencyService.isFirstTimeProcessing(testMessage, "transaction.created"))
                    .thenReturn(true);

            // When
            reportConsumer.consumeCreated(testMessage);

            // Then: idempotency check should happen before service call
            InOrder inOrder = inOrder(idempotencyService, reportService);
            inOrder.verify(idempotencyService).isFirstTimeProcessing(testMessage, "transaction.created");
            inOrder.verify(reportService).updateReport(testMessage);
        }
    }

    @Nested
    @DisplayName("consumeCreated - Duplicate Messages")
    class ConsumeCreatedDuplicates {

        @Test
        @DisplayName("Should discard duplicate message without updating report")
        void consumeCreated_WhenDuplicate_ShouldNotUpdateReport() {
            // Given: message is duplicate
            when(idempotencyService.isFirstTimeProcessing(testMessage, "transaction.created"))
                    .thenReturn(false);

            // When
            reportConsumer.consumeCreated(testMessage);

            // Then: report service should NOT be called
            verify(reportService, never()).updateReport(any());
        }

        @Test
        @DisplayName("Should return early when duplicate detected")
        void consumeCreated_WhenDuplicate_ShouldReturnEarly() {
            // Given
            when(idempotencyService.isFirstTimeProcessing(testMessage, "transaction.created"))
                    .thenReturn(false);

            // When
            reportConsumer.consumeCreated(testMessage);

            // Then: verify idempotency was checked
            verify(idempotencyService).isFirstTimeProcessing(testMessage, "transaction.created");
            // But reportService was never called
            verifyNoInteractions(reportService);
        }
    }

    @Nested
    @DisplayName("consumeUpdated - Idempotence")
    class ConsumeUpdatedIdempotence {

        @Test
        @DisplayName("Should process updated message when first time")
        void consumeUpdated_WhenNewMessage_ShouldProcess() {
            // Given: message is new
            when(idempotencyService.isFirstTimeProcessing(testMessage, "transaction.updated"))
                    .thenReturn(true);
            when(transactionUpdateMapper.toUpdateOperations(testMessage))
                    .thenReturn(java.util.List.of(testMessage));

            // When
            reportConsumer.consumeUpdated(testMessage);

            // Then
            verify(idempotencyService).isFirstTimeProcessing(testMessage, "transaction.updated");
            verify(transactionUpdateMapper).toUpdateOperations(testMessage);
        }

        @Test
        @DisplayName("Should discard duplicate updated message")
        void consumeUpdated_WhenDuplicate_ShouldNotProcess() {
            // Given: message is duplicate
            when(idempotencyService.isFirstTimeProcessing(testMessage, "transaction.updated"))
                    .thenReturn(false);

            // When
            reportConsumer.consumeUpdated(testMessage);

            // Then
            verify(idempotencyService).isFirstTimeProcessing(testMessage, "transaction.updated");
            verify(transactionUpdateMapper, never()).toUpdateOperations(any());
            verify(reportService, never()).updateReport(any());
        }
    }

    @Nested
    @DisplayName("Scenario: RabbitMQ Redelivery")
    class RabbitMQRedeliveryScenarios {

        @Test
        @DisplayName("Same messageId redelivered should only process once")
        void scenario_RabbitMQRedelivery_ShouldOnlyProcessOnce() {
            // Scenario: RabbitMQ redelivers the same message due to consumer crash/timeout
            // First delivery: processed
            when(idempotencyService.isFirstTimeProcessing(testMessage, "transaction.created"))
                    .thenReturn(true);

            reportConsumer.consumeCreated(testMessage);
            verify(reportService, times(1)).updateReport(testMessage);

            // Second delivery: detected as duplicate
            when(idempotencyService.isFirstTimeProcessing(testMessage, "transaction.created"))
                    .thenReturn(false);

            reportConsumer.consumeCreated(testMessage);
            
            // Then: reportService called only once (first delivery)
            verify(reportService, times(1)).updateReport(testMessage);
        }

        @Test
        @DisplayName("Multiple redeliveries should not accumulate amount")
        void scenario_MultipleRedeliveries_ShouldNotDuplicateAmount() {
            // Scenario: Message redelivered 3 times
            String sameMessageId = UUID.randomUUID().toString();
            TransactionMessage msg = TransactionMessage.builder()
                    .messageId(sameMessageId)
                    .transactionId(200L)
                    .userId("user-456")
                    .type(TransactionType.EXPENSE)
                    .amount(BigDecimal.valueOf(500))
                    .category("Food")
                    .date(LocalDate.of(2026, 2, 24))
                    .build();

            // First delivery: success
            when(idempotencyService.isFirstTimeProcessing(msg, "transaction.created"))
                    .thenReturn(true);
            reportConsumer.consumeCreated(msg);

            // Second redelivery: duplicate
            when(idempotencyService.isFirstTimeProcessing(msg, "transaction.created"))
                    .thenReturn(false);
            reportConsumer.consumeCreated(msg);

            // Third redelivery: duplicate
            when(idempotencyService.isFirstTimeProcessing(msg, "transaction.created"))
                    .thenReturn(false);
            reportConsumer.consumeCreated(msg);

            // Then: reportService called only once
            // Amount 500 accumulated once, not 3 times (1500)
            verify(reportService, times(1)).updateReport(msg);
        }
    }

    @Nested
    @DisplayName("Event Type Logging")
    class EventTypeLogging {

        @Test
        @DisplayName("Should register created event type correctly")
        void consumeCreated_ShouldRegisterCorrectEventType() {
            // Given
            when(idempotencyService.isFirstTimeProcessing(testMessage, "transaction.created"))
                    .thenReturn(true);

            // When
            reportConsumer.consumeCreated(testMessage);

            // Then: verify "transaction.created" event type is used
            verify(idempotencyService).isFirstTimeProcessing(testMessage, "transaction.created");
        }

        @Test
        @DisplayName("Should register updated event type correctly")
        void consumeUpdated_ShouldRegisterCorrectEventType() {
            // Given
            when(idempotencyService.isFirstTimeProcessing(testMessage, "transaction.updated"))
                    .thenReturn(true);
            when(transactionUpdateMapper.toUpdateOperations(testMessage))
                    .thenReturn(java.util.List.of());

            // When
            reportConsumer.consumeUpdated(testMessage);

            // Then: verify "transaction.updated" event type is used
            verify(idempotencyService).isFirstTimeProcessing(testMessage, "transaction.updated");
        }
    }
}
