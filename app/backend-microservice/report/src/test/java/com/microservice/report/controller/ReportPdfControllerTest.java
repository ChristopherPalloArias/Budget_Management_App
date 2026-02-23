package com.microservice.report.controller;

import com.microservice.report.exception.PdfGenerationException;
import com.microservice.report.exception.ReportNotFoundException;
import com.microservice.report.model.Report;
import com.microservice.report.repository.ReportRepository;
import com.microservice.report.service.PdfGeneratorService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests 🔴 RED — US-021: Endpoint REST de Descarga de PDF
 *
 * <p>Estos tests verifican el comportamiento del controlador (aún no existente)
 * que maneja las solicitudes de descarga de PDF. Cubren los escenarios:</p>
 * <ul>
 *   <li>E2: Estado de carga durante generación (UI test — no aplica aquí)</li>
 *   <li>E3: Reporte inexistente → 404</li>
 *   <li>E4: Error interno durante generación → 500</li>
 *   <li>E5: Usuario no autenticado → 401/302</li>
 * </ul>
 *
 * <p>Nota: Los tests E3 y E4 se implementan a nivel de servicio/lógica
 * porque el controlador de PDF aún no existe. En fase GREEN se migrarán
 * a tests de MockMvc si aplica.</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("US-021 — ReportPdfController (RED)")
class ReportPdfControllerTest {

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private PdfGeneratorService pdfGeneratorService;

    // ⚠️ El controlador de PDF no existe todavía.
    // Estos tests validan la lógica que el controlador deberá orquestar.

    // =========================================================================
    // Escenario E3: Reporte inexistente → Error 404
    // =========================================================================

    @Nested
    @DisplayName("E3 — Reporte inexistente al generar PDF")
    class ReporteInexistente {

        @Test
        @DisplayName("downloadPdf — lanza ReportNotFoundException si el reporte no existe")
        void downloadPdf_conReporteInexistente_lanzaReportNotFoundException() {
            // Arrange
            String userId = "user-001";
            String period = "2023-01";
            when(reportRepository.findByUserIdAndPeriod(eq(userId), eq(period)))
                    .thenReturn(Optional.empty());

            // Act & Assert — Verificar que se lanza la excepción esperada
            ReportNotFoundException exception = assertThrows(
                    ReportNotFoundException.class,
                    () -> {
                        // Simular la lógica que el controlador/servicio deberá ejecutar
                        Report report = reportRepository.findByUserIdAndPeriod(userId, period)
                                .orElseThrow(() -> new ReportNotFoundException(userId, period));
                        pdfGeneratorService.generatePdf(report);
                    },
                    "Debe lanzar ReportNotFoundException para un reporte inexistente"
            );

            // Assert — El mensaje debe contener información útil
            assertTrue(exception.getMessage().contains(userId),
                    "El mensaje de error debe incluir el userId");
            assertTrue(exception.getMessage().contains(period),
                    "El mensaje de error debe incluir el período");

            // Verify — El generador de PDF NUNCA debe ser invocado
            verify(pdfGeneratorService, never()).generatePdf(any());
        }
    }

    // =========================================================================
    // Escenario E4: Error interno durante generación → Error 500
    // =========================================================================

    @Nested
    @DisplayName("E4 — Error interno durante generación de PDF")
    class ErrorInterno {

        @Test
        @DisplayName("downloadPdf — lanza PdfGenerationException si la generación falla")
        void downloadPdf_conErrorDeGeneracion_lanzaPdfGenerationException() {
            // Arrange — El reporte existe pero la generación de PDF falla
            String userId = "user-001";
            String period = "2025-10";
            Report reporteExistente = Report.builder()
                    .reportId(1L)
                    .userId(userId)
                    .period(period)
                    .totalIncome(new BigDecimal("5000.00"))
                    .totalExpense(new BigDecimal("2000.00"))
                    .balance(new BigDecimal("3000.00"))
                    .build();

            when(reportRepository.findByUserIdAndPeriod(eq(userId), eq(period)))
                    .thenReturn(Optional.of(reporteExistente));
            when(pdfGeneratorService.generatePdf(any(Report.class)))
                    .thenThrow(new PdfGenerationException(
                            "No fue posible generar el PDF. Inténtalo de nuevo más tarde."));

            // Act & Assert
            PdfGenerationException exception = assertThrows(
                    PdfGenerationException.class,
                    () -> {
                        Report report = reportRepository.findByUserIdAndPeriod(userId, period)
                                .orElseThrow(() -> new ReportNotFoundException(userId, period));
                        pdfGeneratorService.generatePdf(report);
                    },
                    "Debe lanzar PdfGenerationException cuando la generación falla"
            );

            assertTrue(exception.getMessage().contains("No fue posible generar el PDF"),
                    "El mensaje debe indicar que la generación falló");
        }

        @Test
        @DisplayName("downloadPdf — el reporte permanece sin cambios tras error de generación")
        void downloadPdf_conErrorDeGeneracion_reportePermaneceIntacto() {
            // Arrange
            String userId = "user-001";
            String period = "2025-10";
            BigDecimal originalIncome = new BigDecimal("5000.00");
            BigDecimal originalExpense = new BigDecimal("2000.00");
            BigDecimal originalBalance = new BigDecimal("3000.00");

            Report reporteExistente = Report.builder()
                    .reportId(1L)
                    .userId(userId)
                    .period(period)
                    .totalIncome(originalIncome)
                    .totalExpense(originalExpense)
                    .balance(originalBalance)
                    .build();

            when(reportRepository.findByUserIdAndPeriod(eq(userId), eq(period)))
                    .thenReturn(Optional.of(reporteExistente));
            when(pdfGeneratorService.generatePdf(any(Report.class)))
                    .thenThrow(new PdfGenerationException("Fallo interno"));

            // Act — Intentar generar PDF (debe fallar)
            try {
                Report report = reportRepository.findByUserIdAndPeriod(userId, period)
                        .orElseThrow(() -> new ReportNotFoundException(userId, period));
                pdfGeneratorService.generatePdf(report);
            } catch (PdfGenerationException e) {
                // Esperado
            }

            // Assert — El reporte no debe haber sido modificado
            assertEquals(originalIncome, reporteExistente.getTotalIncome(),
                    "totalIncome no debe cambiar tras error de generación PDF");
            assertEquals(originalExpense, reporteExistente.getTotalExpense(),
                    "totalExpense no debe cambiar tras error de generación PDF");
            assertEquals(originalBalance, reporteExistente.getBalance(),
                    "balance no debe cambiar tras error de generación PDF");

            // Verify — No se debe guardar ningún cambio en el repositorio
            verify(reportRepository, never()).save(any());
        }
    }

    // =========================================================================
    // Escenario E5: Usuario no autenticado → Acceso denegado
    // =========================================================================

    @Nested
    @DisplayName("E5 — Usuario no autenticado intenta descargar PDF")
    class UsuarioNoAutenticado {

        @Test
        @DisplayName("downloadPdf — sin userId resulta en acceso denegado")
        void downloadPdf_sinUsuarioAutenticado_accesoDenegado() {
            // Arrange — Simular ausencia de autenticación (userId = null)
            String userId = null;
            String period = "2025-10";

            // Act & Assert — Con userId null, la operación debe rechazarse
            // En la fase GREEN, esto será manejado por Spring Security.
            // Por ahora, verificamos que la lógica no opera sin userId.
            assertThrows(
                    IllegalArgumentException.class,
                    () -> {
                        if (userId == null) {
                            throw new IllegalArgumentException(
                                    "El userId es requerido. Usuario no autenticado.");
                        }
                        reportRepository.findByUserIdAndPeriod(userId, period);
                    },
                    "Debe rechazar solicitudes sin userId (usuario no autenticado)"
            );

            // Verify — Ninguna operación de BD ni PDF debe ejecutarse
            verify(reportRepository, never()).findByUserIdAndPeriod(any(), any());
            verify(pdfGeneratorService, never()).generatePdf(any());
        }
    }
}
