package com.microservice.report.infrastructure;

import com.microservice.report.infrastructure.dto.TransactionMessage;
import com.microservice.report.infrastructure.dto.TransactionType;
import com.microservice.report.infrastructure.mapper.TransactionUpdateMapper;
import com.microservice.report.dto.RecordTransactionCommand;
import com.microservice.report.repository.ReportRepository;
import com.microservice.report.service.ReportCommandService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class ReportConsumerTest {

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private ReportCommandService reportCommandService;

    @Test
    @DisplayName("should recalculate totals when transaction amount is updated")
    void shouldRecalculateTotals_whenTransactionUpdated() {
        ReportConsumer consumer = new ReportConsumer(reportCommandService, new TransactionUpdateMapper());

        TransactionMessage updatedMessage = new TransactionMessage(
                10L,
                "user-123",
                TransactionType.EXPENSE,
                new BigDecimal("150.00"),
                LocalDate.of(2025, 3, 10),
                "Food",
                "Updated amount",
                new BigDecimal("100.00"),
                LocalDate.of(2025, 3, 9)
        );

        consumer.consumeUpdated(updatedMessage);

        ArgumentCaptor<RecordTransactionCommand> messageCaptor = ArgumentCaptor.forClass(RecordTransactionCommand.class);
        InOrder inOrder = inOrder(reportCommandService);
        inOrder.verify(reportCommandService, times(2)).updateReport(messageCaptor.capture(), anyString());

        RecordTransactionCommand reversal = messageCaptor.getAllValues().get(0);
        RecordTransactionCommand applied = messageCaptor.getAllValues().get(1);

        assertEquals(new BigDecimal("-100.00"), reversal.amount(),
                "Reversal should negate previous amount");
        assertEquals(LocalDate.of(2025, 3, 9), reversal.date(),
                "Reversal should use previous date");
        assertEquals(new BigDecimal("150.00"), applied.amount(),
                "Applied amount should be the updated amount");
        assertEquals(LocalDate.of(2025, 3, 10), applied.date(),
                "Applied date should be the updated date");

        verifyNoInteractions(reportRepository);
    }

    @Test
    @DisplayName("should move amounts between periods when transaction date changes")
    void shouldMoveAmountsBetweenPeriods_whenPeriodChanges() {
        ReportConsumer consumer = new ReportConsumer(reportCommandService, new TransactionUpdateMapper());

        TransactionMessage updatedMessage = new TransactionMessage(
                20L,
                "user-456",
                TransactionType.EXPENSE,
                new BigDecimal("150.00"),
                LocalDate.of(2025, 4, 5),
                "Rent",
                "Moved to new period",
                new BigDecimal("100.00"),
                LocalDate.of(2025, 3, 10)
        );

        consumer.consumeUpdated(updatedMessage);

        ArgumentCaptor<RecordTransactionCommand> messageCaptor = ArgumentCaptor.forClass(RecordTransactionCommand.class);
        InOrder inOrder = inOrder(reportCommandService);
        inOrder.verify(reportCommandService, times(2)).updateReport(messageCaptor.capture(), anyString());

        RecordTransactionCommand reversal = messageCaptor.getAllValues().get(0);
        RecordTransactionCommand applied = messageCaptor.getAllValues().get(1);

        assertEquals(LocalDate.of(2025, 3, 10), reversal.date(),
                "Reversal should use the previous date");
        assertEquals(new BigDecimal("-100.00"), reversal.amount(),
                "Reversal should negate the previous amount");
        assertEquals(LocalDate.of(2025, 4, 5), applied.date(),
                "Applied message should use the updated date");
        assertEquals(new BigDecimal("150.00"), applied.amount(),
                "Applied message should use the updated amount");

        verifyNoInteractions(reportRepository);
    }

    @Test
    @DisplayName("should send to DLQ after retries when event is invalid")
    void shouldSendToDLQ_whenEventIsInvalid() {
        ReportConsumer consumer = new ReportConsumer(reportCommandService, new TransactionUpdateMapper());
        TransactionMessage invalidMessage = new TransactionMessage(
                99L,
                "user-999",
                TransactionType.EXPENSE,
                new BigDecimal("10.00"),
                LocalDate.of(2025, 3, 10),
                "Invalid",
                "Invalid payload",
                null,
                null
        );

        doThrow(new IllegalArgumentException("invalid message"))
                .when(reportCommandService)
                .updateReport(any(RecordTransactionCommand.class), anyString());

        assertDoesNotThrow(() -> consumer.consumeUpdated(invalidMessage),
                "Consumer should handle retries and route invalid messages to the DLQ");
        verify(reportCommandService, times(3)).updateReport(any(RecordTransactionCommand.class), anyString());
        verifyNoInteractions(reportRepository);
    }

    @Test
    @DisplayName("should reverse INCOME as EXPENSE when transaction is deleted")
    void shouldReverseIncome_whenTransactionDeleted() {
        ReportConsumer consumer = new ReportConsumer(reportCommandService, new TransactionUpdateMapper());

        TransactionMessage deletedMessage = new TransactionMessage(
                30L,
                "user-789",
                TransactionType.INCOME,
                new BigDecimal("500.00"),
                LocalDate.of(2025, 3, 15),
                "Salary",
                "Deleted income transaction",
                null,
                null
        );

        consumer.consumeDeleted(deletedMessage, "msg-123");

        ArgumentCaptor<RecordTransactionCommand> commandCaptor = ArgumentCaptor.forClass(RecordTransactionCommand.class);
        ArgumentCaptor<String> messageIdCaptor = ArgumentCaptor.forClass(String.class);

        verify(reportCommandService).updateReport(commandCaptor.capture(), messageIdCaptor.capture());

        RecordTransactionCommand reverseCommand = commandCaptor.getValue();

        // INCOME se revierte como EXPENSE para restar el ingreso del total
        assertEquals("EXPENSE", reverseCommand.type(),
                "INCOME should be reversed as EXPENSE");
        assertEquals(new BigDecimal("500.00"), reverseCommand.amount(),
                "Amount should remain the same");
        assertEquals(LocalDate.of(2025, 3, 15), reverseCommand.date(),
                "Date should remain the same");
        assertEquals("user-789", reverseCommand.userId(),
                "UserId should remain the same");

        // Verificar que se usa el messageId proporcionado
        assertEquals("msg-123", messageIdCaptor.getValue(),
                "Should use provided messageId");

        verifyNoInteractions(reportRepository);
    }

    @Test
    @DisplayName("should reverse EXPENSE as INCOME when transaction is deleted")
    void shouldReverseExpense_whenTransactionDeleted() {
        ReportConsumer consumer = new ReportConsumer(reportCommandService, new TransactionUpdateMapper());

        TransactionMessage deletedMessage = new TransactionMessage(
                40L,
                "user-456",
                TransactionType.EXPENSE,
                new BigDecimal("250.00"),
                LocalDate.of(2025, 3, 20),
                "Shopping",
                "Deleted expense transaction",
                null,
                null
        );

        consumer.consumeDeleted(deletedMessage, null);  // Sin messageId

        ArgumentCaptor<RecordTransactionCommand> commandCaptor = ArgumentCaptor.forClass(RecordTransactionCommand.class);
        ArgumentCaptor<String> messageIdCaptor = ArgumentCaptor.forClass(String.class);

        verify(reportCommandService).updateReport(commandCaptor.capture(), messageIdCaptor.capture());

        RecordTransactionCommand reverseCommand = commandCaptor.getValue();

        // EXPENSE se revierte como INCOME para restar el gasto del total
        assertEquals("INCOME", reverseCommand.type(),
                "EXPENSE should be reversed as INCOME");
        assertEquals(new BigDecimal("250.00"), reverseCommand.amount(),
                "Amount should remain the same");
        assertEquals(LocalDate.of(2025, 3, 20), reverseCommand.date(),
                "Date should remain the same");
        assertEquals("user-456", reverseCommand.userId(),
                "UserId should remain the same");

        // Verificar que se genera un messageId sintético cuando no se proporciona
        assertEquals("DELETED-40", messageIdCaptor.getValue(),
                "Should generate synthetic messageId when not provided");

        verifyNoInteractions(reportRepository);
    }

    @Test
    @DisplayName("should use synthetic messageId when not provided for deleted transaction")
    void shouldUseSyntheticMessageId_whenNotProvided() {
        ReportConsumer consumer = new ReportConsumer(reportCommandService, new TransactionUpdateMapper());

        TransactionMessage deletedMessage = new TransactionMessage(
                50L,
                "user-123",
                TransactionType.INCOME,
                new BigDecimal("100.00"),
                LocalDate.of(2025, 3, 25),
                "Bonus",
                "Deleted",
                null,
                null
        );

        consumer.consumeDeleted(deletedMessage, null);

        ArgumentCaptor<String> messageIdCaptor = ArgumentCaptor.forClass(String.class);
        verify(reportCommandService).updateReport(any(RecordTransactionCommand.class), messageIdCaptor.capture());

        assertEquals("DELETED-50", messageIdCaptor.getValue(),
                "Should generate messageId as 'DELETED-{transactionId}' when not provided");

        verifyNoInteractions(reportRepository);
    }
}