package com.microservice.report.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.microservice.report.dto.RecordTransactionCommand;
import com.microservice.report.model.Report;
import com.microservice.report.repository.ReportRepository;
import com.microservice.report.service.TransactionClient;
import com.microservice.report.repository.ProcessedMessageRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("ReportCommandService - updateReport() Characterization Tests")
@ExtendWith(MockitoExtension.class)
class ReportServiceImplUpdateReportTest {

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private TransactionClient transactionClient;

    @Mock
    private ProcessedMessageRepository processedMessageRepository;

    private ReportCommandServiceImpl reportCommandService;

    @BeforeEach
    void setUp() {
        reportCommandService = new ReportCommandServiceImpl(reportRepository, transactionClient, processedMessageRepository);
    }

    @Test
    @DisplayName("should accumulate INCOME and increase balance for existing report")
    void shouldAccumulateIncomeAndIncreaseBalance() {
        String userId = "user-1";
        LocalDate date = LocalDate.of(2026, 2, 15);
        String period = "2026-02";
        BigDecimal txAmount = BigDecimal.valueOf(500);
        
        RecordTransactionCommand command = new RecordTransactionCommand(userId, "INCOME", txAmount, date);
        
        Report existingReport = Report.builder()
                .reportId(1L)
                .userId(userId)
                .period(period)
                .totalIncome(BigDecimal.valueOf(1000))
                .totalExpense(BigDecimal.valueOf(200))
                .balance(BigDecimal.valueOf(800))
                .build();
                
        when(processedMessageRepository.existsById("msg-1")).thenReturn(false);
        when(reportRepository.findByUserIdAndPeriod(userId, period)).thenReturn(Optional.of(existingReport));
        
        reportCommandService.updateReport(command, "msg-1");
        
        ArgumentCaptor<Report> reportCaptor = ArgumentCaptor.forClass(Report.class);
        verify(reportRepository).save(reportCaptor.capture());
        
        Report savedReport = reportCaptor.getValue();
        assertEquals(BigDecimal.valueOf(1500), savedReport.getTotalIncome());
        assertEquals(BigDecimal.valueOf(200), savedReport.getTotalExpense());
        assertEquals(BigDecimal.valueOf(1300), savedReport.getBalance());
        verify(processedMessageRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("should accumulate EXPENSE and decrease balance for existing report")
    void shouldAccumulateExpenseAndDecreaseBalance() {
        String userId = "user-1";
        LocalDate date = LocalDate.of(2026, 2, 15);
        String period = "2026-02";
        BigDecimal txAmount = BigDecimal.valueOf(100);
        
        RecordTransactionCommand command = new RecordTransactionCommand(userId, "EXPENSE", txAmount, date);
        
        Report existingReport = Report.builder()
                .reportId(1L)
                .userId(userId)
                .period(period)
                .totalIncome(BigDecimal.valueOf(1000))
                .totalExpense(BigDecimal.valueOf(200))
                .balance(BigDecimal.valueOf(800))
                .build();
                
        when(processedMessageRepository.existsById("msg-2")).thenReturn(false);
        when(reportRepository.findByUserIdAndPeriod(userId, period)).thenReturn(Optional.of(existingReport));
        
        reportCommandService.updateReport(command, "msg-2");
        
        ArgumentCaptor<Report> reportCaptor = ArgumentCaptor.forClass(Report.class);
        verify(reportRepository).save(reportCaptor.capture());
        
        Report savedReport = reportCaptor.getValue();
        assertEquals(BigDecimal.valueOf(1000), savedReport.getTotalIncome());
        assertEquals(BigDecimal.valueOf(300), savedReport.getTotalExpense());
        assertEquals(BigDecimal.valueOf(700), savedReport.getBalance());
        verify(processedMessageRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("should create new report and accumulate INCOME if report does not exist")
    void shouldCreateNewReportAndAccumulateIncome() {
        String userId = "user-2";
        LocalDate date = LocalDate.of(2026, 3, 10);
        String period = "2026-03";
        BigDecimal txAmount = BigDecimal.valueOf(2000);
        
        RecordTransactionCommand command = new RecordTransactionCommand(userId, "INCOME", txAmount, date);
        
        when(processedMessageRepository.existsById("msg-3")).thenReturn(false);
        when(reportRepository.findByUserIdAndPeriod(userId, period)).thenReturn(Optional.empty());
        
        Report newlyCreated = Report.builder()
                .reportId(2L)
                .userId(userId)
                .period(period)
                .totalIncome(BigDecimal.ZERO)
                .totalExpense(BigDecimal.ZERO)
                .balance(BigDecimal.ZERO)
                .build();
                
        // Mock getOrCreateReport behavior (it saves the empty report first!)
        when(reportRepository.save(any(Report.class))).thenReturn(newlyCreated);
        
        reportCommandService.updateReport(command, "msg-3");
        
        // It calls save twice actually: once in createNewReport, once at the end of updateReport
        ArgumentCaptor<Report> reportCaptor = ArgumentCaptor.forClass(Report.class);
        verify(reportRepository, times(2)).save(reportCaptor.capture());
        
        Report savedReport = reportCaptor.getAllValues().get(1); // The second save
        assertEquals(BigDecimal.valueOf(2000), savedReport.getTotalIncome());
        assertEquals(BigDecimal.ZERO, savedReport.getTotalExpense());
        assertEquals(BigDecimal.valueOf(2000), savedReport.getBalance());
        verify(processedMessageRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("should skip processing if messageId is already in ProcessedMessageRepository")
    void shouldSkipProcessingIfIdempotencyKeyExists() {
        String userId = "user-1";
        LocalDate date = LocalDate.of(2026, 2, 15);
        BigDecimal txAmount = BigDecimal.valueOf(500);
        
        RecordTransactionCommand command = new RecordTransactionCommand(userId, "INCOME", txAmount, date);

        when(processedMessageRepository.existsById("msg-dup")).thenReturn(true);

        reportCommandService.updateReport(command, "msg-dup");

        verify(reportRepository, never()).findByUserIdAndPeriod(any(), any());
        verify(reportRepository, never()).save(any());
    }
}
