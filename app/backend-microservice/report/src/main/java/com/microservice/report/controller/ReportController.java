package com.microservice.report.controller;

import com.microservice.report.dto.PaginatedResponse;
import com.microservice.report.dto.ReportResponse;
import com.microservice.report.dto.ReportSummary;
import com.microservice.report.util.PaginationUtils;
import com.microservice.report.validation.ValidPeriod;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.microservice.report.service.ReportService;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/reports")
@Validated
public class ReportController {
    private final ReportService reportService;

    @GetMapping("/{userId}")
    public ResponseEntity<ReportResponse> getReport(
            @PathVariable String userId,
            @RequestParam(required = false) @ValidPeriod String period) {
        ReportResponse report = reportService.getReport(userId, period);
        return report != null ? ResponseEntity.ok(report) : ResponseEntity.notFound().build();
    }

    @GetMapping("/{userId}/all")
    public ResponseEntity<PaginatedResponse<ReportResponse>> getReportsByUser(
            @PathVariable String userId,
            @PageableDefault(size = 10, page = 0, sort = "period", direction = Sort.Direction.DESC) Pageable pageable) {
        Pageable safePageable = PaginationUtils.ensureSafePageSize(pageable);
        return ResponseEntity.ok(reportService.getReportsByUserId(userId, safePageable));
    }

    @GetMapping("/{userId}/summary")
    public ResponseEntity<ReportSummary> getReportSummary(
            @PathVariable String userId,
            @RequestParam @ValidPeriod String startPeriod,
            @RequestParam @ValidPeriod String endPeriod) {
        ReportSummary summary = reportService.getReportsByPeriodRange(userId, startPeriod, endPeriod);
        return ResponseEntity.ok(summary);
    }

    @DeleteMapping("/{period}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReport(@PathVariable @ValidPeriod String period) {
        // En esta fase GREEN simulamos el userId = "1" como se solicitó
        reportService.deleteReport("1", period);
    }
}
