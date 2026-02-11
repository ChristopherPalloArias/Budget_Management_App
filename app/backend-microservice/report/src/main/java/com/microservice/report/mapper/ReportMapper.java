package com.microservice.report.mapper;

import com.microservice.report.dto.ReportResponse;
import com.microservice.report.dto.ReportSummary;
import com.microservice.report.model.Report;

import java.math.BigDecimal;
import java.util.List;

public class ReportMapper {

    public static ReportResponse toResponse(Report entity) {
        if (entity == null) {
            return null;
        }
        return new ReportResponse(
                entity.getReportId(),
                entity.getUserId(),
                entity.getPeriod(),
                entity.getTotalIncome(),
                entity.getTotalExpense(),
                entity.getBalance(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    public static List<ReportResponse> toResponseList(List<Report> entities) {
        if (entities == null)
            return List.of();
        return entities.stream()
                .map(ReportMapper::toResponse)
                .toList();
    }

    public static ReportSummary toSummary(String userId, String startPeriod, String endPeriod,
            List<ReportResponse> reports,
            BigDecimal totalIncome, BigDecimal totalExpense, BigDecimal balance) {
        return new ReportSummary(
                userId,
                startPeriod,
                endPeriod,
                reports,
                totalIncome,
                totalExpense,
                balance);
    }
}
