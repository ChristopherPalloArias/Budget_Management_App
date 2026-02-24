package com.microservice.report.controller;

import com.microservice.report.service.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas de integración para idempotencia en endpoints DELETE (RFC 9110 §9.3.5)
 */
@DisplayName("ReportController - DELETE Idempotencia (RFC 9110)")
@ExtendWith(MockitoExtension.class)
class ReportControllerIdempotenceTest {

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
        userId = "test-user-idempotence";
        principal = mock(Principal.class);
        when(principal.getName()).thenReturn(userId);
    }

    @Nested
    @DisplayName("DELETE /api/v1/reports/{period} - Idempotencia por período")
    class DeleteByPeriodIdempotence {

        private String period = "2026-02";

        @Test
        @DisplayName("Primera llamada: debe devolver 204 NO CONTENT")
        void firstCall_ShouldReturn204_NoContent() throws Exception {
            // GIVEN: reportService no lanza excepción
            doNothing().when(reportService).deleteReport(userId, period);

            // WHEN & THEN: primer DELETE devuelve 204
            mockMvc.perform(delete("/api/v1/reports/{period}", period)
                    .principal(principal))
                    .andExpect(status().isNoContent());

            // VERIFY
            verify(reportService).deleteReport(userId, period);
        }

        @Test
        @DisplayName("RFC 9110: Segunda llamada al mismo período - debe devolver 204 NO CONTENT")
        void secondCall_SamePeriod_ShouldReturn204_NoContent() throws Exception {
            // GIVEN: reportService no lanza excepción (ambas llamadas)
            doNothing().when(reportService).deleteReport(userId, period);

            // WHEN: primera llamada
            mockMvc.perform(delete("/api/v1/reports/{period}", period)
                    .principal(principal))
                    .andExpect(status().isNoContent());

            // WHEN: segunda llamada al mismo período
            mockMvc.perform(delete("/api/v1/reports/{period}", period)
                    .principal(principal))
                    .andExpect(status().isNoContent());

            // THEN: ambas devuelven 204 (NO EXCEPTION THROWN)
            verify(reportService, times(2)).deleteReport(userId, period);
        }

        @Test
        @DisplayName("Múltiples llamadas: todas deben devolver 204 NO CONTENT")
        void multipleCalls_SamePeriod_AllShouldReturn204() throws Exception {
            // GIVEN: reportService no lanza excepción
            doNothing().when(reportService).deleteReport(userId, period);

            // WHEN & THEN: múltiples DELETE al mismo período
            for (int i = 0; i < 5; i++) {
                mockMvc.perform(delete("/api/v1/reports/{period}", period)
                        .principal(principal))
                        .andExpect(status().isNoContent());
            }

            // VERIFY: Se llamó 5 veces
            verify(reportService, times(5)).deleteReport(userId, period);
        }

        @Test
        @DisplayName("Formato inválido de período: debe validarse en controlador")
        void invalidPeriodFormat_ShouldFailValidation() throws Exception {
            // GIVEN: formato incorrecto de período
            String invalidPeriod = "2026/02"; // "/" en lugar de "-"

            // WHEN & THEN: debe fallar o procesarse sin error (depende de regex)
            // El patrón del controlador es: [0-9]{4}-[0-9]{2}
            // Este patrón NO coincide con "2026/02"
            mockMvc.perform(delete("/api/v1/reports/{period}", invalidPeriod)
                    .principal(principal))
                    .andExpect(status().isNotFound()); // 404 porque el patrón no coincide

            // VERIFY: NO debe llamar al servicio
            verify(reportService, never()).deleteReport(anyString(), anyString());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/reports/{reportId} - Idempotencia por ID")
    class DeleteByIdIdempotence {

        private Long reportId = 123L;

        @Test
        @DisplayName("Primera llamada: debe devolver 204 NO CONTENT")
        void firstCall_ShouldReturn204_NoContent() throws Exception {
            // GIVEN: reportService no lanza excepción
            doNothing().when(reportService).deleteReportById(userId, reportId);

            // WHEN & THEN: primer DELETE devuelve 204
            mockMvc.perform(delete("/api/v1/reports/{reportId}", reportId)
                    .principal(principal))
                    .andExpect(status().isNoContent());

            // VERIFY
            verify(reportService).deleteReportById(userId, reportId);
        }

        @Test
        @DisplayName("RFC 9110: Segunda llamada al mismo ID - debe devolver 204 NO CONTENT")
        void secondCall_SameId_ShouldReturn204_NoContent() throws Exception {
            // GIVEN: reportService no lanza excepción
            doNothing().when(reportService).deleteReportById(userId, reportId);

            // WHEN: primera llamada
            mockMvc.perform(delete("/api/v1/reports/{reportId}", reportId)
                    .principal(principal))
                    .andExpect(status().isNoContent());

            // WHEN: segunda llamada al mismo ID
            mockMvc.perform(delete("/api/v1/reports/{reportId}", reportId)
                    .principal(principal))
                    .andExpect(status().isNoContent());

            // THEN: ambas devuelven 204
            verify(reportService, times(2)).deleteReportById(userId, reportId);
        }

        @Test
        @DisplayName("Múltiples llamadas: todas deben devolver 204 NO CONTENT")
        void multipleCalls_SameId_AllShouldReturn204() throws Exception {
            // GIVEN: reportService no lanza excepción
            doNothing().when(reportService).deleteReportById(userId, reportId);

            // WHEN & THEN: múltiples DELETE al mismo ID
            for (int i = 0; i < 5; i++) {
                mockMvc.perform(delete("/api/v1/reports/{reportId}", reportId)
                        .principal(principal))
                        .andExpect(status().isNoContent());
            }

            // VERIFY: Se llamó 5 veces
            verify(reportService, times(5)).deleteReportById(userId, reportId);
        }

        @Test
        @DisplayName("ID no numérico: debe fallar en la validación del patrón")
        void nonNumericId_ShouldFailPathValidation() throws Exception {
            // GIVEN: ID con caracteres no numéricos
            String invalidId = "abc123";

            // WHEN & THEN: debe devolver 404 porque no coincide con [0-9]+
            mockMvc.perform(delete("/api/v1/reports/{reportId}", invalidId)
                    .principal(principal))
                    .andExpect(status().isNotFound());

            // VERIFY: NO debe llamar al servicio
            verify(reportService, never()).deleteReportById(anyString(), anyLong());
        }

        @Test
        @DisplayName("ID negativo: técnicamente válido en patrón pero validación interna debe rechazar")
        void negativeId_ShouldBeHandled() throws Exception {
            // GIVEN: El patrón [0-9]+ solo acepta dígitos positivos
            Long negativeId = -1L;

            // WHEN & THEN: No coincide con [0-9]+ por el signo "-"
            mockMvc.perform(delete("/api/v1/reports/{reportId}", "-1")
                    .principal(principal))
                    .andExpect(status().isNotFound());

            // VERIFY: NO debe llamar al servicio
            verify(reportService, never()).deleteReportById(anyString(), anyLong());
        }
    }

    @Nested
    @DisplayName("Comportamiento General de Idempotencia")
    class GeneralIdempotentBehavior {

        @Test
        @DisplayName("Patrón de rutas: /api/v1/reports/{period:[0-9]{4}-[0-9]{2}} vs /api/v1/reports/{reportId:[0-9]+}")
        void pathPatternResolution() throws Exception {
            // GIVEN: dos patrones diferentes para rutas similares
            String period = "2026-02";     // Coincide con patrón de período
            String reportId = "12345";     // Coincide con patrón de ID

            doNothing().when(reportService).deleteReport(userId, period);
            doNothing().when(reportService).deleteReportById(userId, Long.parseLong(reportId));

            // WHEN & THEN: deleteReport por período
            mockMvc.perform(delete("/api/v1/reports/{period}", period)
                    .principal(principal))
                    .andExpect(status().isNoContent());
            verify(reportService).deleteReport(userId, period);

            // WHEN & THEN: deleteReportById por ID numérico
            mockMvc.perform(delete("/api/v1/reports/{reportId}", reportId)
                    .principal(principal))
                    .andExpect(status().isNoContent());
            verify(reportService).deleteReportById(userId, Long.parseLong(reportId));
        }

        @Test
        @DisplayName("Autenticación requerida: Sin principal debe causar error")
        void authenticationRequired_NoPrincipal_ShouldFail() throws Exception {
            // GIVEN: Sin usuario autenticado
            // WHEN & THEN: debe fallar
            mockMvc.perform(delete("/api/v1/reports/{period}", "2026-02"))
                    .andExpect(status().is4xxClientError()); // Unauthorized o similar
        }
    }
}
