package com.microservice.report.service;

import com.microservice.report.infrastructure.dto.TransactionMessage;
import com.microservice.report.infrastructure.dto.TransactionType;
import com.microservice.report.model.ProcessedMessage;
import com.microservice.report.repository.ProcessedMessageRepository;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for IdempotencyService.
 * 
 * Validates that duplicate messages are correctly detected and discarded
 * without affecting report totals.
 */
@DisplayName("IdempotencyService - Message Deduplication")
@ExtendWith(MockitoExtension.class)
class IdempotencyServiceTest {

    @Mock
    private ProcessedMessageRepository processedMessageRepository;

    @InjectMocks
    private IdempotencyService idempotencyService;

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
                .amount(BigDecimal.valueOf(100))
                .category("Salary")
                .date(LocalDate.of(2026, 2, 24))
                .description("Test transaction")
                .build();
    }

    @Nested
    @DisplayName("First-Time Processing")
    class FirstTimeProcessing {

        @Test
        @DisplayName("Should return true when message is new (not processed before)")
        void isFirstTimeProcessing_WhenMessageIsNew_ShouldReturnTrue() {
            // Given: message does not exist
            when(processedMessageRepository.existsByMessageId(messageId))
                    .thenReturn(false);
            when(processedMessageRepository.save(any(ProcessedMessage.class)))
                    .thenReturn(new ProcessedMessage());

            // When
            boolean result = idempotencyService.isFirstTimeProcessing(testMessage, "transaction.created");

            // Then
            assertTrue(result);
            verify(processedMessageRepository).existsByMessageId(messageId);
            verify(processedMessageRepository).save(any(ProcessedMessage.class));
        }

        @Test
        @DisplayName("Should register the message in repository when first time processing")
        void isFirstTimeProcessing_WhenNew_ShouldRegisterMessage() {
            // Given
            when(processedMessageRepository.existsByMessageId(messageId))
                    .thenReturn(false);
            when(processedMessageRepository.save(any(ProcessedMessage.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // When
            idempotencyService.isFirstTimeProcessing(testMessage, "transaction.created");

            // Then: verify save was called with correct data
            verify(processedMessageRepository).save(argThat(msg ->
                    msg.getMessageId().equals(messageId) &&
                    msg.getTransactionId().equals(100L) &&
                    msg.getUserId().equals("user-123") &&
                    msg.getEventType().equals("transaction.created")
            ));
        }
    }

    @Nested
    @DisplayName("Duplicate Detection")
    class DuplicateDetection {

        @Test
        @DisplayName("Should return false when message already processed (duplicate)")
        void isFirstTimeProcessing_WhenDuplicate_ShouldReturnFalse() {
            // Given: message already exists
            when(processedMessageRepository.existsByMessageId(messageId))
                    .thenReturn(true);

            // When
            boolean result = idempotencyService.isFirstTimeProcessing(testMessage, "transaction.created");

            // Then
            assertFalse(result);
            verify(processedMessageRepository).existsByMessageId(messageId);
            verify(processedMessageRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should not attempt to save when duplicate detected")
        void isFirstTimeProcessing_WhenDuplicate_ShouldNotSave() {
            // Given
            when(processedMessageRepository.existsByMessageId(messageId))
                    .thenReturn(true);

            // When
            idempotencyService.isFirstTimeProcessing(testMessage, "transaction.created");

            // Then
            verify(processedMessageRepository, never()).save(any(ProcessedMessage.class));
        }
    }

    @Nested
    @DisplayName("Event Type Handling")
    class EventTypeHandling {

        @Test
        @DisplayName("Should register transaction.created event type correctly")
        void isFirstTimeProcessing_WithCreatedEvent_ShouldRegisterCorrectly() {
            // Given
            when(processedMessageRepository.existsByMessageId(messageId))
                    .thenReturn(false);
            when(processedMessageRepository.save(any(ProcessedMessage.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // When
            idempotencyService.isFirstTimeProcessing(testMessage, "transaction.created");

            // Then
            verify(processedMessageRepository).save(argThat(msg ->
                    msg.getEventType().equals("transaction.created")
            ));
        }

        @Test
        @DisplayName("Should register transaction.updated event type correctly")
        void isFirstTimeProcessing_WithUpdatedEvent_ShouldRegisterCorrectly() {
            // Given
            when(processedMessageRepository.existsByMessageId(messageId))
                    .thenReturn(false);
            when(processedMessageRepository.save(any(ProcessedMessage.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // When
            idempotencyService.isFirstTimeProcessing(testMessage, "transaction.updated");

            // Then
            verify(processedMessageRepository).save(argThat(msg ->
                    msg.getEventType().equals("transaction.updated")
            ));
        }
    }

    @Nested
    @DisplayName("Error Handling")
    class ErrorHandling {

        @Test
        @DisplayName("Should return false if save throws exception (likely duplicate constraint violation)")
        void isFirstTimeProcessing_WhenSaveThrows_ShouldReturnFalse() {
            // Given: unique constraint violation
            when(processedMessageRepository.existsByMessageId(messageId))
                    .thenReturn(false);
            when(processedMessageRepository.save(any(ProcessedMessage.class)))
                    .thenThrow(new RuntimeException("Duplicate key value violates unique constraint"));

            // When
            boolean result = idempotencyService.isFirstTimeProcessing(testMessage, "transaction.created");

            // Then: treat constraint violation as duplicate
            assertFalse(result);
        }

        @Test
        @DisplayName("Should handle concurrent duplicate insertions gracefully")
        void isFirstTimeProcessing_WithConcurrentDuplicates_ShouldHandleGracefully() {
            // Scenario: Two threads check simultaneously, both see message doesn't exist
            // but only one can insert (constraint violation on second)
            
            // Given: first check shows not exists
            when(processedMessageRepository.existsByMessageId(messageId))
                    .thenReturn(false)
                    .thenReturn(true); // second call after save fails

            when(processedMessageRepository.save(any(ProcessedMessage.class)))
                    .thenThrow(new RuntimeException("Duplicate key"));

            // When
            boolean result = idempotencyService.isFirstTimeProcessing(testMessage, "transaction.created");

            // Then: gracefully treat as duplicate
            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("Integration Scenarios")
    class IntegrationScenarios {

        @Test
        @DisplayName("Same messageId processed twice should only succeed first time")
        void multipleCallsSameMessage_ShouldOnlyProcessFirstTime() {
            // Given: first call succeeds, second call detects duplicate
            when(processedMessageRepository.existsByMessageId(messageId))
                    .thenReturn(false)  // First call
                    .thenReturn(true);  // Second call (after registration)
            
            when(processedMessageRepository.save(any(ProcessedMessage.class)))
                    .thenReturn(new ProcessedMessage());

            // When: process same message twice
            boolean firstResult = idempotencyService.isFirstTimeProcessing(testMessage, "transaction.created");
            boolean secondResult = idempotencyService.isFirstTimeProcessing(testMessage, "transaction.created");

            // Then
            assertTrue(firstResult);
            assertFalse(secondResult);
        }

        @Test
        @DisplayName("Different messages with different messageIds should both process")
        void differentMessages_ShouldBothProcess() {
            // Given: both messages are new
            when(processedMessageRepository.existsByMessageId(anyString()))
                    .thenReturn(false);
            when(processedMessageRepository.save(any(ProcessedMessage.class)))
                    .thenReturn(new ProcessedMessage());

            // When
            TransactionMessage msg1 = testMessage;
            TransactionMessage msg2 = TransactionMessage.builder()
                    .messageId(UUID.randomUUID().toString())
                    .transactionId(101L)
                    .userId("user-456")
                    .type(TransactionType.EXPENSE)
                    .amount(BigDecimal.valueOf(50))
                    .category("Food")
                    .date(LocalDate.of(2026, 2, 24))
                    .build();

            boolean result1 = idempotencyService.isFirstTimeProcessing(msg1, "transaction.created");
            boolean result2 = idempotencyService.isFirstTimeProcessing(msg2, "transaction.created");

            // Then
            assertTrue(result1);
            assertTrue(result2);
            verify(processedMessageRepository, times(2)).save(any(ProcessedMessage.class));
        }
    }
}
