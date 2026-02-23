package com.microservice.report.controller;

import com.microservice.report.service.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Fase RED: Prueba de controlador para la US-017.
 * Se utiliza el enfoque 'Standalone' de MockMvc debido a restricciones 
 * en las dependencias del proyecto que impiden el uso de @WebMvcTest.
 */
@ExtendWith(MockitoExtension.class)
class ReportControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ReportService reportService;

    @InjectMocks
    private ReportController reportController;

    @BeforeEach
    void setUp() {
        // Configuración manual del entorno MockMvc para testear el controlador en aislamiento
        mockMvc = MockMvcBuilders.standaloneSetup(reportController).build();
    }

    @Test
    @DisplayName("test: US-017-E1 add failing controller test for successful report deletion")
    void deleteReport_ShouldReturnNoContent_WhenSuccessful() throws Exception {
        // GIVEN
        String period = "2024-03";
        String userId = "QHlms0DALUgLnnXMffUBMP14v5m1"; // Fallback userId when principal is null

        // WHEN & THEN: Se espera 204 No Content
        mockMvc.perform(delete("/api/v1/reports/{period}", period))
                .andExpect(status().isNoContent());

        // THEN: Verificar que se llame al servicio
        verify(reportService).deleteReport(userId, period);
    }
}
