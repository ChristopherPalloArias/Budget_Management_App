package com.microservice.report.service.impl;

import com.microservice.report.exception.PdfGenerationException;
import com.microservice.report.model.Report;
import com.microservice.report.service.PdfGeneratorService;
import com.microservice.report.template.ReportPdfTemplate;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

/**
 * Implementación del servicio de generación de PDF para reportes financieros.
 *
 * <p><strong>Responsabilidad única:</strong> Orquestar el ciclo de vida del documento
 * PDF (crear, delegar renderizado al template, serializar y retornar bytes).
 * La lógica de presentación/layout se encuentra en {@link ReportPdfTemplate}.</p>
 *
 * <p><strong>Historia de usuario:</strong> US-021 — Descargar Reporte de un Período como PDF</p>
 * <p><strong>Fase TDD:</strong> 🔵 REFACTOR — SRP aplicado</p>
 *
 * @see PdfGeneratorService contrato que implementa esta clase
 * @see ReportPdfTemplate template de presentación del reporte
 */
@Service
public class PdfGeneratorServiceImpl implements PdfGeneratorService {

    private final ReportPdfTemplate reportTemplate;

    public PdfGeneratorServiceImpl() {
        this.reportTemplate = new ReportPdfTemplate();
    }

    /**
     * Genera un documento PDF con los datos del reporte financiero.
     *
     * <p>Flujo:</p>
     * <ol>
     *   <li>Crear un {@link PDDocument} vacío</li>
     *   <li>Delegar la renderización al {@link ReportPdfTemplate}</li>
     *   <li>Serializar el documento a {@code byte[]}</li>
     * </ol>
     *
     * @param report entidad {@link Report} con los datos del período a exportar
     * @return arreglo de bytes representando el contenido del archivo PDF
     * @throws PdfGenerationException si ocurre un error durante la generación
     */
    @Override
    public byte[] generatePdf(Report report) {
        try (PDDocument document = new PDDocument()) {

            reportTemplate.render(document, report);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.save(outputStream);
            return outputStream.toByteArray();

        } catch (Exception e) {
            throw new PdfGenerationException(
                    "No fue posible generar el PDF. Inténtalo de nuevo más tarde.", e);
        }
    }
}
