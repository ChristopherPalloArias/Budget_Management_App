package com.microservice.report.controller;

import com.microservice.report.exception.ReportNotFoundException;
import com.microservice.report.model.Report;
import com.microservice.report.repository.ReportRepository;
import com.microservice.report.service.PdfGeneratorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests GREEN — US-021: Endpoint REST de Descarga de PDF
 * Fase GREEN: el controlador existe, se testea directamente con MockMvc en modo standalone.
 */
@SuppressWarnings("java:S100")
@ExtendWith(MockitoExtension.class)
@DisplayName("US-021 — ReportPdfController (GREEN)")
class ReportPdfControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private PdfGeneratorService pdfGeneratorService;

    @InjectMocks
    private ReportPdfController reportPdfController;

    private static final String USER_ID = "user-001";
    private Principal mockPrincipal;

    @BeforeEach
    void setUp() {
        mockPrincipal = () -> USER_ID;
        mockMvc = MockMvcBuilders.standaloneSetup(reportPdfController).build();
    }

    @Test
    @DisplayName("E1 — GET /api/v1/reports/pdf returns 200 with PDF bytes and correct headers")
    void downloadPdf_ShouldReturnOkWithPdfContent_WhenReportExists() throws Exception {
        String period = "2025-10";
        byte[] pdfBytes = new byte[]{37, 80, 68, 70, 45}; // %PDF- header bytes

        Report existingReport = Report.builder()
                .reportId(1L)
                .userId(USER_ID)
                .period(period)
                .totalIncome(new BigDecimal("5000.00"))
                .totalExpense(new BigDecimal("2000.00"))
                .balance(new BigDecimal("3000.00"))
                .build();

        when(reportRepository.findByUserIdAndPeriod(eq(USER_ID), eq(period)))
                .thenReturn(Optional.of(existingReport));
        when(pdfGeneratorService.generatePdf(existingReport))
                .thenReturn(pdfBytes);

        mockMvc.perform(get("/api/v1/reports/pdf")
                .param("period", period)
                .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                        containsString("attachment; filename=\"reporte-2025-10.pdf\"")));
    }

    @Test
    @DisplayName("E3 — GET /api/v1/reports/pdf throws ReportNotFoundException when report does not exist")
    void downloadPdf_ShouldThrowReportNotFoundException_WhenReportDoesNotExist() {
        String period = "2023-01";

        when(reportRepository.findByUserIdAndPeriod(eq(USER_ID), eq(period)))
                .thenReturn(Optional.empty());

        // In Spring Boot 4 (Spring 6), unhandled controller exceptions propagate from perform()
        Throwable thrown = assertThrows(Throwable.class, () ->
                mockMvc.perform(get("/api/v1/reports/pdf")
                        .param("period", period)
                        .principal(mockPrincipal)));

        // Handle both: direct throw and ServletException wrapping
        Throwable root = thrown.getCause() != null ? thrown.getCause() : thrown;
        assertInstanceOf(ReportNotFoundException.class, root);
    }
}
