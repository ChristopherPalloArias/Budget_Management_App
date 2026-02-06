package com.microservice.report.service.impl;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.microservice.report.dto.ReportSummary;
import com.microservice.report.exception.ReportNotFoundException;
import com.microservice.report.infrastructure.dto.TransactionMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.microservice.report.infrastructure.dto.TransactionType;
import com.microservice.report.model.Report;
import com.microservice.report.repository.ReportRepository;
import com.microservice.report.service.ReportService;


@RequiredArgsConstructor
@Service
public class ReportServiceImpl implements ReportService {
    private final ReportRepository reportRepository;

    private Report getOrCreateReport(TransactionMessage transactionMessage) {
        String userId = transactionMessage.getUserId();
        String period = transactionMessage.getDate()
                .format(DateTimeFormatter.ofPattern("yyyy-MM"));
        return reportRepository.findByUserIdAndPeriod(userId, period)
                .orElseGet(() -> reportRepository.save(
                        Report.builder()
                                .userId(userId)
                                .period(period)
                                .totalIncome(BigDecimal.ZERO)
                                .totalExpense(BigDecimal.ZERO)
                                .balance(BigDecimal.ZERO)
                                .build()
                ));
    }

    @Transactional
    @Override
    public void updateReport(TransactionMessage transactionMessage) {
        Report report = getOrCreateReport(transactionMessage);
        BigDecimal amount = transactionMessage.getAmount();
        if (transactionMessage.getType() == TransactionType.INCOME) {
            report.setTotalIncome(report.getTotalIncome().add(amount));
        } else if (transactionMessage.getType() == TransactionType.EXPENSE) {
            report.setTotalExpense(report.getTotalExpense().add(amount));
        }
        report.setBalance(report.getTotalIncome().subtract(report.getTotalExpense()));
        reportRepository.save(report);
    }

    @Transactional
    @Override
    public Report getReport(String userId, String period) {
        return reportRepository.findByUserIdAndPeriod(userId, period)
                .orElseThrow(() -> new ReportNotFoundException(userId, period));
    }

    @Transactional
    @Override
    public List<Report> getReportsByUserId(String userId) {
        return reportRepository.findByUserId(userId);
    }

    @Transactional
    @Override
    public ReportSummary getReportsByPeriodRange(String userId, String startPeriod, String endPeriod) {
        List<Report> reports = reportRepository.findByUserIdAndPeriodBetweenOrderByPeriodAsc(
                userId, startPeriod, endPeriod
        );
        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;

        for (Report report : reports) {
            totalIncome = totalIncome.add(report.getTotalIncome());
            totalExpense = totalExpense.add(report.getTotalExpense());
        }
        return ReportSummary.builder()
                .userId(userId)
                .startPeriod(startPeriod)
                .endPeriod(endPeriod)
                .reports(reports)
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .balance(totalIncome.subtract(totalExpense))
                .build();
    }
}
