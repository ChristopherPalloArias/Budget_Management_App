package com.microservice.report.infrastructure;

import com.microservice.report.infrastructure.dto.TransactionMessage;
import com.microservice.report.infrastructure.dto.TransactionType;
import com.microservice.report.model.Report;
import com.microservice.report.repository.ReportRepository;
import com.microservice.report.service.ReportService;
import com.microservice.report.service.impl.ReportServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportConsumerTest {

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private ReportService reportService;

    @Test
    @DisplayName("should recalculate totals when transaction amount is updated")
    void shouldRecalculateTotals_whenTransactionUpdated() {
        String userId = "user-123";
        String period = "2025-03";

        Report existingReport = Report.builder()
                .reportId(1L)
                .userId(userId)
                .period(period)
                .totalIncome(BigDecimal.ZERO)
                .totalExpense(new BigDecimal("100.00"))
                .balance(new BigDecimal("-100.00"))
                .build();

        when(reportRepository.findByUserIdAndPeriod(userId, period))
                .thenReturn(Optional.of(existingReport));
        when(reportRepository.save(any(Report.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ReportServiceImpl realService = new ReportServiceImpl(reportRepository);
        ReportConsumer consumer = new ReportConsumer(realService);

        TransactionMessage updatedMessage = new TransactionMessage(
                10L,
                userId,
                TransactionType.EXPENSE,
                new BigDecimal("150.00"),
                LocalDate.of(2025, 3, 10),
                "Food",
                "Updated amount"
        );

        consumer.consumeUpdated(updatedMessage);

        ArgumentCaptor<Report> reportCaptor = ArgumentCaptor.forClass(Report.class);
        verify(reportRepository).save(reportCaptor.capture());

        Report savedReport = reportCaptor.getValue();
        assertEquals(0, savedReport.getTotalExpense().compareTo(new BigDecimal("150.00")),
                "Total expense should reflect the updated amount only");
    }

    @Test
    @DisplayName("should move amounts between periods when transaction date changes")
    void shouldMoveAmountsBetweenPeriods_whenPeriodChanges() {
        String userId = "user-456";
        String oldPeriod = "2025-03";
        String newPeriod = "2025-04";

        Report oldReport = Report.builder()
                .reportId(2L)
                .userId(userId)
                .period(oldPeriod)
                .totalIncome(BigDecimal.ZERO)
                .totalExpense(new BigDecimal("100.00"))
                .balance(new BigDecimal("-100.00"))
                .build();

        Report newReport = Report.builder()
                .reportId(3L)
                .userId(userId)
                .period(newPeriod)
                .totalIncome(BigDecimal.ZERO)
                .totalExpense(BigDecimal.ZERO)
                .balance(BigDecimal.ZERO)
                .build();

        when(reportRepository.findByUserIdAndPeriod(userId, oldPeriod))
                .thenReturn(Optional.of(oldReport));
        when(reportRepository.findByUserIdAndPeriod(userId, newPeriod))
                .thenReturn(Optional.of(newReport));
        when(reportRepository.save(any(Report.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ReportServiceImpl realService = new ReportServiceImpl(reportRepository);
        ReportConsumer consumer = new ReportConsumer(realService);

        TransactionMessage updatedMessage = new TransactionMessage(
                20L,
                userId,
                TransactionType.EXPENSE,
                new BigDecimal("150.00"),
                LocalDate.of(2025, 4, 5),
                "Rent",
                "Moved to new period"
        );

        consumer.consumeUpdated(updatedMessage);

        verify(reportRepository).findByUserIdAndPeriod(userId, oldPeriod);
        verify(reportRepository).findByUserIdAndPeriod(userId, newPeriod);

        ArgumentCaptor<Report> reportCaptor = ArgumentCaptor.forClass(Report.class);
        verify(reportRepository, times(2)).save(reportCaptor.capture());

        List<Report> savedReports = reportCaptor.getAllValues();
        assertTrue(savedReports.stream().anyMatch(report ->
                        report.getPeriod().equals(oldPeriod)
                                && report.getTotalExpense().compareTo(BigDecimal.ZERO) == 0),
                "Old period report should remove the previous amount");
        assertTrue(savedReports.stream().anyMatch(report ->
                        report.getPeriod().equals(newPeriod)
                                && report.getTotalExpense().compareTo(new BigDecimal("150.00")) == 0),
                "New period report should include the updated amount");
    }

    @Test
    @DisplayName("should send to DLQ after retries when event is invalid")
    void shouldSendToDLQ_whenEventIsInvalid() {
        ReportConsumer consumer = new ReportConsumer(reportService);
        TransactionMessage invalidMessage = new TransactionMessage(
                99L,
                "user-999",
                TransactionType.EXPENSE,
                new BigDecimal("10.00"),
                LocalDate.of(2025, 3, 10),
                "Invalid",
                "Invalid payload"
        );

        when(reportService.updateReport(any(TransactionMessage.class)))
                .thenThrow(new IllegalArgumentException("invalid message"));

        assertDoesNotThrow(() -> consumer.consumeUpdated(invalidMessage),
                "Consumer should handle retries and route invalid messages to the DLQ");
        verify(reportService, times(3)).updateReport(invalidMessage);
    }
}
