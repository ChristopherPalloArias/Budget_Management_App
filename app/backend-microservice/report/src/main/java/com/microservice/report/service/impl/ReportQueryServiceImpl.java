package com.microservice.report.service.impl;

import com.microservice.report.dto.PaginatedResponse;
import com.microservice.report.dto.ReportResponse;
import com.microservice.report.dto.ReportSummary;
import com.microservice.report.exception.ReportNotFoundException;
import com.microservice.report.mapper.ReportMapper;
import com.microservice.report.model.Report;
import com.microservice.report.repository.ReportRepository;
import com.microservice.report.service.ReportQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Pattern;

@RequiredArgsConstructor
@Service
public class ReportQueryServiceImpl implements ReportQueryService {

    private static final Pattern PERIOD_PATTERN = Pattern.compile("^\\d{4}-(0[1-9]|1[0-2])$");
    private final ReportRepository reportRepository;

    @Transactional(readOnly = true)
    @Override
    public ReportResponse getReport(String userId, String period) {
        Report report = findReportOrThrow(userId, period);
        return ReportMapper.toResponse(report);
    }

    @Transactional(readOnly = true)
    @Override
    public PaginatedResponse<ReportResponse> getReportsByUserId(String userId, Pageable pageable) {
        Page<Report> page = reportRepository.findByUserId(userId, pageable);
        List<ReportResponse> content = page.map(ReportMapper::toResponse).getContent();

        return new PaginatedResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast());
    }

    @Transactional(readOnly = true)
    @Override
    public ReportSummary getReportsByPeriodRange(String userId, String startPeriod, String endPeriod) {
        List<Report> reports = reportRepository.findByUserIdAndPeriodBetweenOrderByPeriodAsc(
                userId, startPeriod, endPeriod);

        AccumulatedTotals totals = accumulateTotalsFromReports(reports);

        return ReportMapper.toSummary(
                userId,
                startPeriod,
                endPeriod,
                ReportMapper.toResponseList(reports),
                totals.totalIncome(),
                totals.totalExpense(),
                totals.totalIncome().subtract(totals.totalExpense()));
    }

    private AccumulatedTotals accumulateTotalsFromReports(List<Report> reports) {
        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;

        for (Report report : reports) {
            totalIncome = totalIncome.add(report.getTotalIncome());
            totalExpense = totalExpense.add(report.getTotalExpense());
        }

        return new AccumulatedTotals(totalIncome, totalExpense);
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

    private record AccumulatedTotals(BigDecimal totalIncome, BigDecimal totalExpense) {
    }
}
