package com.microservice.report.util;

/**
 * Utilidad para generar nombres de archivo PDF con formato estandarizado.
 *
 * <p>Formato: {@code reporte-yyyy-MM.pdf}</p>
 * <p>Ejemplo: Para el período {@code "2025-01"} → {@code "reporte-2025-01.pdf"}</p>
 *
 * <p><strong>Historia de usuario:</strong> US-021 — Valor Límite BV3</p>
 * <p><strong>Fase TDD:</strong> 🟢 GREEN — Implementación real</p>
 */
public class PdfFileNameGenerator {

    private PdfFileNameGenerator() {
        // Utility class — prevent instantiation
    }

    /**
     * Genera el nombre de archivo PDF para un reporte de período individual.
     *
     * @param period período en formato {@code "yyyy-MM"}
     * @return nombre del archivo con formato {@code "reporte-yyyy-MM.pdf"}
     */
    public static String generateFileName(String period) {
        return "reporte-" + period + ".pdf";
    }
}
