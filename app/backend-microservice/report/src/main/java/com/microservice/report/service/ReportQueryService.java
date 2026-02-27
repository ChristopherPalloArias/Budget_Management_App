package com.microservice.report.service;

import com.microservice.report.dto.ReportResponse;
import com.microservice.report.dto.ReportSummary;
import com.microservice.report.dto.PaginatedResponse;
import org.springframework.data.domain.Pageable;

public interface ReportQueryService {
    ReportResponse getReport(String userId, String period);
    PaginatedResponse<ReportResponse> getReportsByUserId(String userId, Pageable pageable);
    ReportSummary getReportsByPeriodRange(String userId, String startPeriod, String endPeriod);
}
