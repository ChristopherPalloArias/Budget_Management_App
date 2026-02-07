package com.microservice.report.controller;

import com.microservice.report.dto.ReportSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.microservice.report.model.Report;
import com.microservice.report.service.ReportService;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {
    private final ReportService reportService;

    @GetMapping("/{userId}")
    public ResponseEntity<Report> getReport(
            @PathVariable String userId,
            @RequestParam(required = false) String period) {
        Report report = reportService.getReport(userId, period);
        return report != null ? ResponseEntity.ok(report) : ResponseEntity.notFound().build();
    }

    @GetMapping("/{userId}/all")
    public ResponseEntity<List<Report>> getReportsByUser(@PathVariable String userId) {
        return ResponseEntity.ok(reportService.getReportsByUserId(userId));
    }

    @GetMapping("/{userId}/summary")
    public ResponseEntity<ReportSummary> getReportSummary(
            @PathVariable String userId,
            @RequestParam String startPeriod,
            @RequestParam String endPeriod) {
        ReportSummary summary = reportService.getReportsByPeriodRange(userId, startPeriod, endPeriod);
        return ResponseEntity.ok(summary);
    }
}
