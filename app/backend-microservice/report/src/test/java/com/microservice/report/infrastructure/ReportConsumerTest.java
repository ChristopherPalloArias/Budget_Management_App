package com.microservice.report.infrastructure;

import com.microservice.report.infrastructure.dto.TransactionMessage;
import com.microservice.report.infrastructure.dto.TransactionType;
import com.microservice.report.infrastructure.mapper.TransactionUpdateMapper;
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

        ArgumentCaptor<TransactionMessage> messageCaptor = ArgumentCaptor.forClass(TransactionMessage.class);
        InOrder inOrder = inOrder(reportCommandService);
        inOrder.verify(reportCommandService, times(2)).updateReport(messageCaptor.capture(), anyString());

        TransactionMessage reversal = messageCaptor.getAllValues().get(0);
        TransactionMessage applied = messageCaptor.getAllValues().get(1);

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

        ArgumentCaptor<TransactionMessage> messageCaptor = ArgumentCaptor.forClass(TransactionMessage.class);
        InOrder inOrder = inOrder(reportCommandService);
        inOrder.verify(reportCommandService, times(2)).updateReport(messageCaptor.capture(), anyString());

        TransactionMessage reversal = messageCaptor.getAllValues().get(0);
        TransactionMessage applied = messageCaptor.getAllValues().get(1);

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
                .updateReport(any(TransactionMessage.class), anyString());

        assertDoesNotThrow(() -> consumer.consumeUpdated(invalidMessage),
                "Consumer should handle retries and route invalid messages to the DLQ");
        verify(reportCommandService, times(3)).updateReport(eq(invalidMessage), anyString());
        verifyNoInteractions(reportRepository);
    }
}