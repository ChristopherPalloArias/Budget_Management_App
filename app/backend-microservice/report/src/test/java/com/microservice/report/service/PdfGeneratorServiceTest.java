package com.microservice.report.service;

import com.microservice.report.exception.PdfGenerationException;
import com.microservice.report.model.Report;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests 🔴 RED — US-021: Descargar Reporte de un Período como PDF
 *
 * <p>Estos tests verifican el contrato de {@link PdfGeneratorService}.
 * En la fase RED, no existe implementación — todos los tests deben FALLAR.</p>
 *
 * <p>Cubre:</p>
 * <ul>
 *   <li>Escenario E1: Descarga exitosa del PDF</li>
 *   <li>Valor Límite BV1: Reporte con campos en $0.00</li>
 *   <li>Valor Límite BV2: Reporte con montos muy grandes ($9,999,999.99)</li>
 *   <li>Valor Límite BV4: PDF refleja datos recalculados más recientes</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("US-021 — PdfGeneratorService (RED)")
class PdfGeneratorServiceTest {

    // ⚠️ NO HAY @InjectMocks ni implementación concreta.
    // En fase RED, instanciamos directamente una implementación que AÚN NO EXISTE.
    // Esto causa la falla esperada.

    /**
     * Helper: Crea un reporte con los datos proporcionados.
     */
    private Report buildReport(Long id, String userId, String period,
                               BigDecimal income, BigDecimal expense) {
        return Report.builder()
                .reportId(id)
                .userId(userId)
                .period(period)
                .totalIncome(income)
                .totalExpense(expense)
                .balance(income.subtract(expense))
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    // =========================================================================
    // Escenario E1: Descarga exitosa del PDF
    // =========================================================================

    @Nested
    @DisplayName("E1 — Descarga exitosa del PDF")
    class DescargaExitosa {

        @Test
        @DisplayName("generatePdf — genera un PDF no vacío para un reporte válido")
        void generatePdf_conReporteValido_retornaPdfNoVacio() {
            // Arrange
            Report report = buildReport(
                    1L, "user-001", "2025-10",
                    new BigDecimal("5000.00"),
                    new BigDecimal("2000.00")
            );

            // Act — Esto DEBE fallar: no hay implementación de PdfGeneratorService
            PdfGeneratorService pdfService = getPdfGeneratorServiceImpl();
            byte[] pdfBytes = pdfService.generatePdf(report);

            // Assert
            assertNotNull(pdfBytes, "El PDF generado no debe ser null");
            assertTrue(pdfBytes.length > 0, "El PDF generado no debe estar vacío");
        }

        @Test
        @DisplayName("generatePdf — el PDF comienza con la firma PDF '%PDF-'")
        void generatePdf_conReporteValido_comienzaConFirmaPdf() {
            // Arrange
            Report report = buildReport(
                    2L, "user-001", "2025-10",
                    new BigDecimal("3000.00"),
                    new BigDecimal("1200.50")
            );

            // Act
            PdfGeneratorService pdfService = getPdfGeneratorServiceImpl();
            byte[] pdfBytes = pdfService.generatePdf(report);

            // Assert — Todos los PDFs válidos comienzan con "%PDF-"
            assertNotNull(pdfBytes);
            String header = new String(pdfBytes, 0, Math.min(5, pdfBytes.length));
            assertEquals("%PDF-", header,
                    "El archivo generado debe comenzar con la firma PDF '%PDF-'");
        }
    }

    // =========================================================================
    // Valor Límite BV1: Reporte con todos los campos en $0.00
    // =========================================================================

    @Nested
    @DisplayName("BV1 — Reporte con todos los campos en $0.00")
    class ContenidoMinimo {

        @Test
        @DisplayName("generatePdf — genera PDF válido con totales en $0.00")
        void generatePdf_conTotalesEnCero_generaPdfValido() {
            // Arrange — Reporte con contenido mínimo: todos los campos en $0.00
            Report report = buildReport(
                    10L, "user-zero", "2025-06",
                    BigDecimal.ZERO,
                    BigDecimal.ZERO
            );

            // Act
            PdfGeneratorService pdfService = getPdfGeneratorServiceImpl();
            byte[] pdfBytes = pdfService.generatePdf(report);

            // Assert
            assertNotNull(pdfBytes, "El PDF debe generarse incluso con valores en $0.00");
            assertTrue(pdfBytes.length > 0,
                    "El PDF no debe estar vacío aunque todos los montos sean $0.00");
        }
    }

    // =========================================================================
    // Valor Límite BV2: Montos muy grandes ($9,999,999.99)
    // =========================================================================

    @Nested
    @DisplayName("BV2 — Reporte con montos muy grandes ($9,999,999.99)")
    class MontosGrandes {

        @Test
        @DisplayName("generatePdf — genera PDF sin desbordamiento con montos de $9,999,999.99")
        void generatePdf_conMontosGrandes_generaPdfSinDesbordamiento() {
            // Arrange — Límite superior de representación monetaria
            BigDecimal montoGrande = new BigDecimal("9999999.99");
            Report report = buildReport(
                    20L, "user-large", "2025-03",
                    montoGrande,
                    montoGrande
            );

            // Act
            PdfGeneratorService pdfService = getPdfGeneratorServiceImpl();
            byte[] pdfBytes = pdfService.generatePdf(report);

            // Assert
            assertNotNull(pdfBytes,
                    "El PDF debe generarse correctamente con montos de $9,999,999.99");
            assertTrue(pdfBytes.length > 0,
                    "El PDF no debe estar vacío con montos grandes");
            // Verificar que no se produce una excepción de desbordamiento
            // (el hecho de llegar aquí sin excepción ya lo confirma)
        }

        @Test
        @DisplayName("generatePdf — genera PDF con balance negativo grande")
        void generatePdf_conBalanceNegativoGrande_generaPdfValido() {
            // Arrange — Balance extremadamente negativo
            Report report = buildReport(
                    21L, "user-deficit", "2025-04",
                    new BigDecimal("100.00"),
                    new BigDecimal("9999999.99")
            );

            // Act
            PdfGeneratorService pdfService = getPdfGeneratorServiceImpl();
            byte[] pdfBytes = pdfService.generatePdf(report);

            // Assert
            assertNotNull(pdfBytes,
                    "El PDF debe generarse incluso con balance negativo grande");
            assertTrue(pdfBytes.length > 0);
        }
    }

    // =========================================================================
    // Valor Límite BV4: PDF refleja datos recalculados más recientes
    // =========================================================================

    @Nested
    @DisplayName("BV4 — PDF refleja datos recalculados más recientes")
    class DatosRecalculados {

        @Test
        @DisplayName("generatePdf — el PDF usa los valores actualizados del reporte (post-recalculación)")
        void generatePdf_conDatosRecalculados_reflejaDatosRecientes() {
            // Arrange — Simular un reporte con valores POST-recalculación
            // (los valores originales eran: income=1000, expense=400, balance=600)
            // (los valores recalculados son: income=1200, expense=400, balance=800)
            Report reporteRecalculado = Report.builder()
                    .reportId(30L)
                    .userId("user-recalc")
                    .period("2025-08")
                    .totalIncome(new BigDecimal("1200.00"))
                    .totalExpense(new BigDecimal("400.00"))
                    .balance(new BigDecimal("800.00"))
                    .createdAt(OffsetDateTime.now().minusDays(30))
                    .updatedAt(OffsetDateTime.now()) // updatedAt = ahora (recién recalculado)
                    .build();

            // Act — El PDF debe generarse con los datos ACTUALES, no cacheados
            PdfGeneratorService pdfService = getPdfGeneratorServiceImpl();
            byte[] pdfBytes = pdfService.generatePdf(reporteRecalculado);

            // Assert
            assertNotNull(pdfBytes,
                    "El PDF debe generarse con datos recalculados");
            assertTrue(pdfBytes.length > 0,
                    "El PDF generado tras recalculación no debe estar vacío");
            // Nota: la validación completa del contenido del PDF (que contenga $1,200.00 
            // y no $1,000.00) se hará en la fase GREEN cuando exista el generador real.
            // Por ahora, verificamos que la operación no lanza excepción con datos recientes.
        }
    }

    // =========================================================================
    // Método helper — Obtener implementación (NO EXISTE → forzar fallo RED)
    // =========================================================================

    /**
     * Intenta obtener una implementación concreta de {@link PdfGeneratorService}.
     *
     * <p>En la fase 🔴 RED, este método fuerza el fallo porque la clase
     * {@code PdfGeneratorServiceImpl} aún no existe.</p>
     *
     * @return instancia del servicio (nunca retorna en fase RED)
     * @throws RuntimeException siempre — la implementación no existe
     */
    private PdfGeneratorService getPdfGeneratorServiceImpl() {
        try {
            Class<?> implClass = Class.forName(
                    "com.microservice.report.service.impl.PdfGeneratorServiceImpl");
            return (PdfGeneratorService) implClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            fail("🔴 RED: La implementación PdfGeneratorServiceImpl no existe todavía. " +
                 "Implementar en la fase GREEN. Error: " + e.getMessage());
            return null; // Nunca se alcanza
        }
    }
}
