package com.microservice.report.service.impl;

import com.microservice.report.dto.ReportResponse;
import com.microservice.report.exception.ReportNotFoundException;
import com.microservice.report.dto.RecordTransactionCommand;
import com.microservice.report.infrastructure.dto.TransactionType;
import com.microservice.report.mapper.ReportMapper;
import com.microservice.report.model.Report;
import com.microservice.report.repository.ReportRepository;
import com.microservice.report.service.ReportCommandService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.microservice.report.service.TransactionClient;
import com.microservice.report.repository.ProcessedMessageRepository;
import com.microservice.report.model.ProcessedMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

@RequiredArgsConstructor
@Service
@Slf4j
public class ReportCommandServiceImpl implements ReportCommandService {

    private static final String PERIOD_FORMAT = "yyyy-MM";
    private static final Pattern PERIOD_PATTERN = Pattern.compile("^\\d{4}-(0[1-9]|1[0-2])$");

    private final ReportRepository reportRepository;
    private final TransactionClient transactionClient;
    private final ProcessedMessageRepository processedMessageRepository;

    @Transactional
    @Override
    public void updateReport(RecordTransactionCommand command, String messageId) {
        if (messageId != null && processedMessageRepository.existsById(messageId)) {
            log.warn("Mensaje iterativo ignorado, ya ha sido procesado. ID: {}", messageId);
            return;
        }

        String userId = command.userId();
        String period = extractPeriodFromDate(command.date());

        Report report = reportRepository.findByUserIdAndPeriod(userId, period)
                .orElseGet(() -> createNewReport(userId, period));

        // Actualizar totales usando el modelo de dominio rico
        if ("INCOME".equalsIgnoreCase(command.type())) {
            report.addIncome(command.amount());
        } else if ("EXPENSE".equalsIgnoreCase(command.type())) {
            report.addExpense(command.amount());
        }

        reportRepository.save(report);

        if (messageId != null) {
            processedMessageRepository.save(new ProcessedMessage(messageId, OffsetDateTime.now()));
        }
    }

    @Transactional
    @Override
    public void deleteReport(String userId, String period) {
        Report report = findReportOrThrow(userId, period);
        reportRepository.delete(report);
    }

    @Transactional
    @Override
    public void deleteReportById(String userId, Long reportId) {
        validateUserId(userId);
        if (reportId == null || reportId <= 0) {
            throw new IllegalArgumentException("reportId must be a positive number");
        }

        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ReportNotFoundException(
                        String.format("Report not found with id: %d", reportId)));

        if (!report.getUserId().equals(userId)) {
            throw new ReportNotFoundException(
                    String.format("Report with id %d does not belong to user %s", reportId, userId));
        }

        reportRepository.delete(report);
    }

    @Transactional
    @Override
    public ReportResponse recalculateReport(String userId, String period, String token) {
        validateUserId(userId);
        validatePeriod(period);

        Report report = reportRepository.findByUserIdAndPeriod(userId, period)
                .orElseThrow(() -> new ReportNotFoundException(userId, period));

        List<TransactionClient.TransactionData> transactions = transactionClient.fetchTransactions(period, token);

        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;

        for (TransactionClient.TransactionData tx : transactions) {
            if ("INCOME".equals(tx.type())) {
                totalIncome = totalIncome.add(tx.amount());
            } else if ("EXPENSE".equals(tx.type())) {
                totalExpense = totalExpense.add(tx.amount());
            }
        }

        report.setTotalIncome(totalIncome);
        report.setTotalExpense(totalExpense);

        Report savedReport = reportRepository.save(report);
        return ReportMapper.toResponse(savedReport);
    }

    private String extractPeriodFromDate(java.time.LocalDate date) {
        return date.format(DateTimeFormatter.ofPattern(PERIOD_FORMAT));
    }

    private Report createNewReport(String userId, String period) {
        return reportRepository.save(
                Report.builder()
                        .userId(userId)
                        .period(period)
                        .totalIncome(BigDecimal.ZERO)
                        .totalExpense(BigDecimal.ZERO)
                        .balance(BigDecimal.ZERO)
                        .build());
    }

    private Report findReportOrThrow(String userId, String period) {
        validateUserId(userId);
        validatePeriod(period);
        return reportRepository.findByUserIdAndPeriod(userId, period)
                .orElseThrow(() -> new ReportNotFoundException(userId, period));
    }

    private void validateUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId cannot be null or blank");
        }
    }

    private void validatePeriod(String period) {
        if (period == null) {
            throw new IllegalArgumentException("period cannot be null");
        }
        if (!PERIOD_PATTERN.matcher(period).matches()) {
            throw new IllegalArgumentException(
                    String.format("Invalid period format: %s. Expected format: yyyy-MM", period));
        }
    }
}
