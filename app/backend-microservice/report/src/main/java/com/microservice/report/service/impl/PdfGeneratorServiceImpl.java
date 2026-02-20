package com.microservice.report.service.impl;

import com.microservice.report.exception.PdfGenerationException;
import com.microservice.report.model.Report;
import com.microservice.report.service.PdfGeneratorService;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Implementación del servicio de generación de PDF para reportes financieros.
 *
 * <p>Utiliza Apache PDFBox para generar documentos PDF con los datos del reporte:
 * usuario, período, totalIncome, totalExpense, balance y fecha de generación.</p>
 *
 * <p><strong>Historia de usuario:</strong> US-021 — Descargar Reporte de un Período como PDF</p>
 * <p><strong>Fase TDD:</strong> 🟢 GREEN — Código mínimo para pasar los tests</p>
 *
 * @see PdfGeneratorService Contrato que implementa esta clase
 */
@Service
public class PdfGeneratorServiceImpl implements PdfGeneratorService {

    private static final NumberFormat CURRENCY_FORMAT;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    static {
        CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(Locale.US);
        CURRENCY_FORMAT.setMinimumFractionDigits(2);
        CURRENCY_FORMAT.setMaximumFractionDigits(2);
    }

    /**
     * Genera un documento PDF con los datos del reporte financiero.
     *
     * @param report entidad {@link Report} con los datos del período a exportar
     * @return arreglo de bytes representando el contenido del archivo PDF
     * @throws PdfGenerationException si ocurre un error durante la generación
     */
    @Override
    public byte[] generatePdf(Report report) {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.LETTER);
            document.addPage(page);

            PDType1Font fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font fontRegular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                float yPosition = 700;
                float margin = 60;

                // ─── Título ───
                content.beginText();
                content.setFont(fontBold, 20);
                content.newLineAtOffset(margin, yPosition);
                content.showText("Reporte Financiero");
                content.endText();
                yPosition -= 35;

                // ─── Período ───
                content.beginText();
                content.setFont(fontBold, 14);
                content.newLineAtOffset(margin, yPosition);
                content.showText("Período: " + report.getPeriod());
                content.endText();
                yPosition -= 25;

                // ─── Usuario ───
                content.beginText();
                content.setFont(fontRegular, 11);
                content.newLineAtOffset(margin, yPosition);
                content.showText("Usuario: " + report.getUserId());
                content.endText();
                yPosition -= 40;

                // ─── Línea separadora ───
                content.setLineWidth(1f);
                content.moveTo(margin, yPosition);
                content.lineTo(550, yPosition);
                content.stroke();
                yPosition -= 30;

                // ─── Detalle financiero ───
                yPosition = addFinancialRow(content, fontBold, fontRegular, margin, yPosition,
                        "Total Ingresos:", report.getTotalIncome());
                yPosition = addFinancialRow(content, fontBold, fontRegular, margin, yPosition,
                        "Total Gastos:", report.getTotalExpense());

                // ─── Línea separadora antes del balance ───
                yPosition -= 5;
                content.setLineWidth(0.5f);
                content.moveTo(margin, yPosition);
                content.lineTo(350, yPosition);
                content.stroke();
                yPosition -= 25;

                // ─── Balance ───
                yPosition = addFinancialRow(content, fontBold, fontRegular, margin, yPosition,
                        "Balance:", report.getBalance());
                yPosition -= 40;

                // ─── Fecha de generación ───
                content.beginText();
                content.setFont(fontRegular, 9);
                content.newLineAtOffset(margin, yPosition);
                String generatedAt = OffsetDateTime.now().format(DATE_FORMAT);
                content.showText("Documento generado el: " + generatedAt);
                content.endText();
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            return out.toByteArray();

        } catch (Exception e) {
            throw new PdfGenerationException(
                    "No fue posible generar el PDF. Inténtalo de nuevo más tarde.", e);
        }
    }

    /**
     * Agrega una fila con etiqueta y monto formateado al contenido del PDF.
     */
    private float addFinancialRow(PDPageContentStream content,
                                   PDType1Font fontBold, PDType1Font fontRegular,
                                   float margin, float y,
                                   String label, BigDecimal amount) throws java.io.IOException {
        content.beginText();
        content.setFont(fontBold, 12);
        content.newLineAtOffset(margin, y);
        content.showText(label);
        content.endText();

        content.beginText();
        content.setFont(fontRegular, 12);
        content.newLineAtOffset(margin + 180, y);
        content.showText(formatCurrency(amount));
        content.endText();

        return y - 22;
    }

    /**
     * Formatea un monto BigDecimal como moneda USD.
     * Maneja montos grandes (e.g., $9,999,999.99) sin desbordamiento.
     */
    private String formatCurrency(BigDecimal amount) {
        return CURRENCY_FORMAT.format(amount);
    }
}
