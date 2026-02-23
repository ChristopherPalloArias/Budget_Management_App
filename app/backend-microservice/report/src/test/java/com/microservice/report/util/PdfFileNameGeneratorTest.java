package com.microservice.report.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests 🔴 RED — US-021: Generación de nombre de archivo PDF
 *
 * <p>Valor Límite BV3: Nombre de archivo con período "2025-01"
 * → Resultado esperado: {@code "reporte-2025-01.pdf"}</p>
 */
@DisplayName("US-021 — PdfFileNameGenerator (RED)")
class PdfFileNameGeneratorTest {

    @Test
    @DisplayName("BV3 — generateFileName('2025-01') retorna 'reporte-2025-01.pdf'")
    void generateFileName_conPeriodo2025_01_retornaFormatoCorrecto() {
        // Arrange
        String period = "2025-01";

        // Act — DEBE FALLAR: el stub lanza UnsupportedOperationException
        String fileName = PdfFileNameGenerator.generateFileName(period);

        // Assert
        assertEquals("reporte-2025-01.pdf", fileName,
                "El nombre del archivo debe seguir el formato 'reporte-yyyy-MM.pdf'");
    }

    @Test
    @DisplayName("BV3 — generateFileName('2026-12') retorna 'reporte-2026-12.pdf'")
    void generateFileName_conPeriodo2026_12_retornaFormatoCorrecto() {
        // Arrange
        String period = "2026-12";

        // Act
        String fileName = PdfFileNameGenerator.generateFileName(period);

        // Assert
        assertEquals("reporte-2026-12.pdf", fileName,
                "El nombre del archivo debe seguir el formato 'reporte-yyyy-MM.pdf'");
    }

    @Test
    @DisplayName("BV3 — generateFileName('2025-06') retorna 'reporte-2025-06.pdf'")
    void generateFileName_conPeriodoIntermedio_retornaFormatoCorrecto() {
        // Arrange
        String period = "2025-06";

        // Act
        String fileName = PdfFileNameGenerator.generateFileName(period);

        // Assert
        assertEquals("reporte-2025-06.pdf", fileName,
                "El nombre del archivo debe seguir el formato 'reporte-yyyy-MM.pdf'");
    }
}
