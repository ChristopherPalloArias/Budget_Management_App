package com.microservice.report.controller;

import com.microservice.report.dto.PaginatedResponse;
import com.microservice.report.dto.ReportResponse;
import com.microservice.report.dto.ReportSummary;
import com.microservice.report.service.ReportCommandService;
import com.microservice.report.service.ReportQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas unitarias del controlador de reportes.
 * Se usa el enfoque 'Standalone' de MockMvc para testear el controlador en aislamiento.
 */
@ExtendWith(MockitoExtension.class)
class ReportControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ReportCommandService reportCommandService;

    @Mock
    private ReportQueryService reportQueryService;

    private final String mockToken = "Bearer mock-jwt";
    private final String userId = "QHlms0DALUgLnnXMffUBMP14v5m1";
    private Principal mockPrincipal;

    @InjectMocks
    private ReportController reportController;

    @BeforeEach
    void setUp() {
        mockPrincipal = () -> userId;
        mockMvc = MockMvcBuilders.standaloneSetup(reportController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    @DisplayName("test: US-017-E1 add failing controller test for successful report deletion")
    void deleteReport_ShouldReturnNoContent_WhenSuccessful() throws Exception {
        String period = "2024-03";

        mockMvc.perform(delete("/api/v1/reports/{period}", period)
                .principal(mockPrincipal)
                .header(HttpHeaders.AUTHORIZATION, mockToken))
                .andExpect(status().isNoContent());

        verify(reportCommandService).deleteReport(userId, period);
    }

    @Test
    @DisplayName("DELETE /api/v1/reports/{reportId} returns 204 No Content when report deleted by id")
    void deleteReportById_ShouldReturnNoContent_WhenSuccessful() throws Exception {
        Long reportId = 1L;

        mockMvc.perform(delete("/api/v1/reports/{reportId}", reportId)
                .principal(mockPrincipal))
                .andExpect(status().isNoContent());

        verify(reportCommandService).deleteReportById(userId, reportId);
    }

    @Test
    @DisplayName("GET /api/v1/reports returns 200 OK with report for a given period")
    void getReport_ShouldReturnOk_WhenSuccessful() throws Exception {
        String period = "2024-03";
        ReportResponse mockResponse = new ReportResponse(
                1L, userId, period,
                BigDecimal.valueOf(1000), BigDecimal.valueOf(500), BigDecimal.valueOf(500),
                null, null);

        when(reportQueryService.getReport(userId, period)).thenReturn(mockResponse);

        mockMvc.perform(get("/api/v1/reports")
                .param("period", period)
                .principal(mockPrincipal))
                .andExpect(status().isOk());

        verify(reportQueryService).getReport(userId, period);
    }

    @Test
    @DisplayName("GET /api/v1/reports/all returns 200 OK with paginated reports")
    void getReportsByUser_ShouldReturnOk_WhenSuccessful() throws Exception {
        PaginatedResponse<ReportResponse> mockResponse = new PaginatedResponse<>(
                List.of(), 0, 10, 0L, 0, true);

        when(reportQueryService.getReportsByUserId(eq(userId), any(Pageable.class)))
                .thenReturn(mockResponse);

        mockMvc.perform(get("/api/v1/reports/all")
                .principal(mockPrincipal))
                .andExpect(status().isOk());

        verify(reportQueryService).getReportsByUserId(eq(userId), any(Pageable.class));
    }

    @Test
    @DisplayName("GET /api/v1/reports/summary returns 200 OK with aggregated summary")
    void getReportSummary_ShouldReturnOk_WhenSuccessful() throws Exception {
        String startPeriod = "2024-01";
        String endPeriod = "2024-03";
        ReportSummary mockSummary = new ReportSummary(
                userId, startPeriod, endPeriod, List.of(),
                BigDecimal.valueOf(3000), BigDecimal.valueOf(1500), BigDecimal.valueOf(1500));

        when(reportQueryService.getReportsByPeriodRange(userId, startPeriod, endPeriod))
                .thenReturn(mockSummary);

        mockMvc.perform(get("/api/v1/reports/summary")
                .param("startPeriod", startPeriod)
                .param("endPeriod", endPeriod)
                .principal(mockPrincipal))
                .andExpect(status().isOk());

        verify(reportQueryService).getReportsByPeriodRange(userId, startPeriod, endPeriod);
    }

    @Test
    @DisplayName("POST /api/v1/reports/recalculate returns 200 OK with recalculated report")
    void recalculateReport_ShouldReturnOk_WhenSuccessful() throws Exception {
        String period = "2024-03";
        ReportResponse mockResponse = new ReportResponse(
                1L, userId, period,
                BigDecimal.valueOf(1000), BigDecimal.valueOf(500), BigDecimal.valueOf(500),
                null, null);

        when(reportCommandService.recalculateReport(eq(userId), eq(period), any()))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/reports/recalculate")
                .principal(mockPrincipal)
                .header(HttpHeaders.AUTHORIZATION, mockToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"period\":\"2024-03\"}"))
                .andExpect(status().isOk());

        verify(reportCommandService).recalculateReport(eq(userId), eq(period), any());
    }
}
