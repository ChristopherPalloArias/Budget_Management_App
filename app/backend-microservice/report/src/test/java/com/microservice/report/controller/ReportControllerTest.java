package com.microservice.report.controller;

import com.microservice.report.service.ReportCommandService;
import com.microservice.report.service.ReportQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.http.HttpHeaders;

import java.security.Principal;

import java.security.Principal;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ReportControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ReportCommandService reportCommandService;

    @Mock
    private ReportQueryService reportQueryService;

    private final String mockToken = "Bearer mock-jwt";

    @InjectMocks
    private ReportController reportController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(reportController).build();
    }

    @Test
    @DisplayName("test: US-017-E1 add failing controller test for successful report deletion")
    void deleteReport_ShouldReturnNoContent_WhenSuccessful() throws Exception {
        // GIVEN
        String period = "2024-03";
        String userId = "QHlms0DALUgLnnXMffUBMP14v5m1";

        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(userId);

        // WHEN & THEN
        mockMvc.perform(delete("/api/v1/reports/{period}", period)
                        .principal(principal))
                .andExpect(status().isNoContent());

        // THEN
        verify(reportService).deleteReport(userId, period);
    }
}
