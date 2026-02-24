package com.microservice.report.controller;

import com.microservice.report.dto.PaginatedResponse;
import com.microservice.report.dto.ReportResponse;
import com.microservice.report.service.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for unified GET /api/v1/reports endpoint.
 * 
 * Tests validate conditional routing behavior:
 * - With period parameter: single ReportResponse
 * - Without period parameter: paginated PaginatedResponse<ReportResponse>
 * 
 * Ensures backward compatibility while consolidating endpoints.
 */
@DisplayName("ReportController - Unified GET /api/v1/reports Endpoint")
@ExtendWith(MockitoExtension.class)
class ReportControllerUnifiedGetTest {

    private MockMvc mockMvc;

    @Mock
    private ReportService reportService;

    @InjectMocks
    private ReportController reportController;

    private Principal principal;
    private String userId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(reportController).build();
        userId = "user-123";
        principal = mock(Principal.class);
        when(principal.getName()).thenReturn(userId);
    }

    @Nested
    @DisplayName("GET /api/v1/reports?period=yyyy-MM - Single Report (period provided)")
    class GetReportWithPeriod {

        private String period = "2026-02";
        private ReportResponse mockReport;

        @BeforeEach
        void setUp() {
            mockReport = ReportResponse.builder()
                    .reportId(1L)
                    .userId(userId)
                    .period(period)
                    .totalIncome(BigDecimal.valueOf(1000))
                    .totalExpense(BigDecimal.valueOf(400))
                    .balance(BigDecimal.valueOf(600))
                    .build();
        }

        @Test
        @DisplayName("Should return single ReportResponse when period query param is provided")
        void getReport_WithPeriod_ShouldReturnSingleReport() throws Exception {
            // Given
            when(reportService.getReport(userId, period))
                    .thenReturn(mockReport);

            // When & Then
            mockMvc.perform(get("/api/v1/reports")
                            .principal(principal)
                            .param("period", period))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.reportId").value(1))
                    .andExpect(jsonPath("$.userId").value(userId))
                    .andExpect(jsonPath("$.period").value(period))
                    .andExpect(jsonPath("$.totalIncome").value(1000))
                    .andExpect(jsonPath("$.totalExpense").value(400))
                    .andExpect(jsonPath("$.balance").value(600));

            // Verify service was called with correct period
            verify(reportService).getReport(userId, period);
            // Ensure paginated method was NOT called
            verify(reportService, never()).getReportsByUserId(anyString(), any(Pageable.class));
        }

        @Test
        @DisplayName("Should call getReport() service method exactly once when period is provided")
        void getReport_WithPeriod_ShouldCallGetReportServiceMethod() throws Exception {
            // Given
            when(reportService.getReport(userId, period))
                    .thenReturn(mockReport);

            // When
            mockMvc.perform(get("/api/v1/reports")
                    .principal(principal)
                    .param("period", period))
                    .andExpect(status().isOk());

            // Then
            verify(reportService, times(1)).getReport(userId, period);
        }

        @Test
        @DisplayName("Should handle empty period parameter as absent (no period)")
        void getReport_WithEmptyPeriod_ShouldTreatAsAbsent() throws Exception {
            // Given: paginated response for collection
            PaginatedResponse<ReportResponse> paginatedResponse = new PaginatedResponse<>(
                    List.of(mockReport), 0, 10, 1, 1, true);
            when(reportService.getReportsByUserId(eq(userId), any(Pageable.class)))
                    .thenReturn(paginatedResponse);

            // When: period param is empty string
            mockMvc.perform(get("/api/v1/reports")
                    .principal(principal)
                    .param("period", ""))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());

            // Then: should call paginated method, not single report method
            verify(reportService).getReportsByUserId(eq(userId), any(Pageable.class));
            verify(reportService, never()).getReport(anyString(), anyString());
        }

        @Test
        @DisplayName("Should validate period format when provided")
        void getReport_WithInvalidPeriodFormat_ShouldReturnBadRequest() throws Exception {
            // When: invalid period format
            mockMvc.perform(get("/api/v1/reports")
                    .principal(principal)
                    .param("period", "invalid-format"))
                    .andExpect(status().isBadRequest());

            // Then: service should not be called
            verify(reportService, never()).getReport(anyString(), anyString());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/reports - Collection (period NOT provided)")
    class GetReportsWithoutPeriod {

        private PaginatedResponse<ReportResponse> paginatedResponse;

        @BeforeEach
        void setUp() {
            List<ReportResponse> reports = List.of(
                    ReportResponse.builder()
                            .reportId(1L)
                            .userId(userId)
                            .period("2026-02")
                            .totalIncome(BigDecimal.valueOf(1000))
                            .totalExpense(BigDecimal.valueOf(400))
                            .balance(BigDecimal.valueOf(600))
                            .build(),
                    ReportResponse.builder()
                            .reportId(2L)
                            .userId(userId)
                            .period("2026-01")
                            .totalIncome(BigDecimal.valueOf(800))
                            .totalExpense(BigDecimal.valueOf(300))
                            .balance(BigDecimal.valueOf(500))
                            .build()
            );
            paginatedResponse = new PaginatedResponse<>(
                    reports, 0, 10, 2, 1, true);
        }

        @Test
        @DisplayName("Should return paginated collection when period query param is absent")
        void getReports_WithoutPeriod_ShouldReturnPaginatedCollection() throws Exception {
            // Given
            when(reportService.getReportsByUserId(eq(userId), any(Pageable.class)))
                    .thenReturn(paginatedResponse);

            // When & Then
            mockMvc.perform(get("/api/v1/reports")
                    .principal(principal))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(2))
                    .andExpect(jsonPath("$.content[0].reportId").value(1))
                    .andExpect(jsonPath("$.content[0].period").value("2026-02"))
                    .andExpect(jsonPath("$.content[1].reportId").value(2))
                    .andExpect(jsonPath("$.content[1].period").value("2026-01"))
                    .andExpect(jsonPath("$.page").value(0))
                    .andExpect(jsonPath("$.size").value(10))
                    .andExpect(jsonPath("$.totalElements").value(2))
                    .andExpect(jsonPath("$.totalPages").value(1))
                    .andExpect(jsonPath("$.isLast").value(true));

            // Verify service was called with correct method
            verify(reportService).getReportsByUserId(eq(userId), any(Pageable.class));
            // Ensure single report method was NOT called
            verify(reportService, never()).getReport(anyString(), anyString());
        }

        @Test
        @DisplayName("Should call getReportsByUserId() service method when period is absent")
        void getReports_WithoutPeriod_ShouldCallGetReportsByUserIdMethod() throws Exception {
            // Given
            when(reportService.getReportsByUserId(eq(userId), any(Pageable.class)))
                    .thenReturn(paginatedResponse);

            // When
            mockMvc.perform(get("/api/v1/reports")
                    .principal(principal))
                    .andExpect(status().isOk());

            // Then
            verify(reportService, times(1)).getReportsByUserId(eq(userId), any(Pageable.class));
        }

        @Test
        @DisplayName("Should use default pagination when no pagination params provided")
        void getReports_WithoutPeriod_ShouldUseDefaultPagination() throws Exception {
            // Given
            when(reportService.getReportsByUserId(eq(userId), any(Pageable.class)))
                    .thenReturn(paginatedResponse);

            // When
            mockMvc.perform(get("/api/v1/reports")
                    .principal(principal))
                    .andExpect(status().isOk());

            // Then: verify pagination params were used (default: size=10, page=0, sort=period DESC)
            verify(reportService).getReportsByUserId(
                    eq(userId),
                    argThat(pageable -> pageable.getPageSize() <= 100 && 
                                       pageable.getPageNumber() >= 0));
        }

        @Test
        @DisplayName("Should accept custom pagination parameters")
        void getReports_WithCustomPagination_ShouldApplyCustomParams() throws Exception {
            // Given
            when(reportService.getReportsByUserId(eq(userId), any(Pageable.class)))
                    .thenReturn(paginatedResponse);

            // When
            mockMvc.perform(get("/api/v1/reports")
                    .principal(principal)
                    .param("page", "0")
                    .param("size", "20"))
                    .andExpect(status().isOk());

            // Then: custom pagination should have been applied
            verify(reportService).getReportsByUserId(
                    eq(userId),
                    any(Pageable.class));
        }

        @Test
        @DisplayName("Should return empty collection when user has no reports")
        void getReports_WithoutPeriod_EmptyCollection_ShouldReturnEmptyList() throws Exception {
            // Given: empty paginated response
            PaginatedResponse<ReportResponse> emptyResponse = new PaginatedResponse<>(
                    List.of(), 0, 10, 0, 0, true);
            when(reportService.getReportsByUserId(eq(userId), any(Pageable.class)))
                    .thenReturn(emptyResponse);

            // When & Then
            mockMvc.perform(get("/api/v1/reports")
                    .principal(principal))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(0))
                    .andExpect(jsonPath("$.totalElements").value(0));

            verify(reportService).getReportsByUserId(eq(userId), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("Conditional Routing Tests")
    class ConditionalRoutingTests {

        @Test
        @DisplayName("Should route to single report when ONLY period is provided")
        void shouldRouteToSingleReportWhenOnlyPeriodProvided() throws Exception {
            // Given
            String period = "2026-02";
            ReportResponse mockReport = ReportResponse.builder()
                    .reportId(1L)
                    .userId(userId)
                    .period(period)
                    .build();
            when(reportService.getReport(userId, period))
                    .thenReturn(mockReport);

            // When
            mockMvc.perform(get("/api/v1/reports")
                    .principal(principal)
                    .param("period", period))
                    .andExpect(status().isOk());

            // Then: only getReport should be called
            verify(reportService).getReport(userId, period);
            verify(reportService, never()).getReportsByUserId(anyString(), any(Pageable.class));
        }

        @Test
        @DisplayName("Should route to collection when period is missing but pagination params exist")
        void shouldRouteToCollectionWhenPaginationParamsExist() throws Exception {
            // Given
            PaginatedResponse<ReportResponse> paginatedResponse = new PaginatedResponse<>(
                    List.of(), 0, 5, 0, 0, true);
            when(reportService.getReportsByUserId(eq(userId), any(Pageable.class)))
                    .thenReturn(paginatedResponse);

            // When: pagination params without period
            mockMvc.perform(get("/api/v1/reports")
                    .principal(principal)
                    .param("page", "0")
                    .param("size", "5"))
                    .andExpect(status().isOk());

            // Then: getReportsByUserId should be called
            verify(reportService).getReportsByUserId(eq(userId), any(Pageable.class));
            verify(reportService, never()).getReport(anyString(), anyString());
        }

        @Test
        @DisplayName("Period parameter takes precedence over pagination params")
        void periodTakesPrecedenceOverPaginationParams() throws Exception {
            // Given
            String period = "2026-02";
            ReportResponse mockReport = ReportResponse.builder()
                    .reportId(1L)
                    .userId(userId)
                    .period(period)
                    .build();
            when(reportService.getReport(userId, period))
                    .thenReturn(mockReport);

            // When: both period and pagination params
            mockMvc.perform(get("/api/v1/reports")
                    .principal(principal)
                    .param("period", period)
                    .param("page", "0")
                    .param("size", "20"))
                    .andExpect(status().isOk());

            // Then: getReport (single) should be called, NOT getReportsByUserId (collection)
            verify(reportService).getReport(userId, period);
            verify(reportService, never()).getReportsByUserId(anyString(), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("REST Semantics Tests")
    class RestSemanticsTests {

        @Test
        @DisplayName("Should return 200 OK for both single and collection responses")
        void shouldReturn200OkForBothResponses() throws Exception {
            // Single report case
            when(reportService.getReport(anyString(), anyString()))
                    .thenReturn(ReportResponse.builder().build());
            mockMvc.perform(get("/api/v1/reports")
                    .principal(principal)
                    .param("period", "2026-02"))
                    .andExpect(status().isOk());

            // Collection case
            when(reportService.getReportsByUserId(anyString(), any(Pageable.class)))
                    .thenReturn(new PaginatedResponse<>(List.of(), 0, 10, 0, 0, true));
            mockMvc.perform(get("/api/v1/reports")
                    .principal(principal))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should support GET method for resource retrieval")
        void shouldSupportGetMethod() throws Exception {
            // Both single and collection should use GET (not POST, PUT, DELETE)
            when(reportService.getReportsByUserId(anyString(), any(Pageable.class)))
                    .thenReturn(new PaginatedResponse<>(List.of(), 0, 10, 0, 0, true));

            mockMvc.perform(get("/api/v1/reports")
                    .principal(principal))
                    .andExpect(status().isOk());
        }
    }
}
