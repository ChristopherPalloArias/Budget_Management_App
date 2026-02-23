package com.microservice.report.service;

import com.microservice.report.model.Report;

/**
 * Contrato para la generación de reportes financieros en formato PDF.
 *
 * <p>Define la operación de generación de PDF a partir de una entidad {@link Report}.
 * La implementación debe producir un documento PDF válido con los datos financieros
 * del reporte (período, totalIncome, totalExpense, balance, usuario, fecha de generación).</p>
 *
 * <p><strong>Historia de usuario:</strong> US-021 — Descargar Reporte de un Período como PDF</p>
 *
 * @see Report Entidad fuente de datos para el PDF
 */
public interface PdfGeneratorService {

    /**
     * Genera un documento PDF con los datos del reporte financiero.
     *
     * @param report entidad {@link Report} con los datos del período a exportar
     * @return arreglo de bytes representando el contenido del archivo PDF
     * @throws com.microservice.report.exception.PdfGenerationException si ocurre un error durante la generación
     */
    byte[] generatePdf(Report report);
}
