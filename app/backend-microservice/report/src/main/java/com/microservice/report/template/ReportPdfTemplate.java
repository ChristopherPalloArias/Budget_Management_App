package com.microservice.report.template;

import com.microservice.report.model.Report;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Template reutilizable para el diseño visual de reportes financieros en PDF.
 *
 * <p>Responsabilidad única: definir el <strong>layout</strong> (fuentes, márgenes,
 * posiciones, formato de moneda) y renderizar los datos de un {@link Report}
 * dentro de un {@link PDDocument} existente.</p>
 *
 * <p>Esta clase <strong>no</strong> gestiona el ciclo de vida del documento
 * (creación, serialización, cierre). Eso lo maneja
 * {@code PdfGeneratorServiceImpl}.</p>
 *
 * <p><strong>Historia de usuario:</strong> US-021</p>
 * <p><strong>Fase TDD:</strong> 🔵 REFACTOR — Separación de responsabilidades</p>
 */
public class ReportPdfTemplate {

    // ─── Configuración de layout ───
    private static final float PAGE_MARGIN = 60f;
    private static final float CONTENT_START_Y = 700f;
    private static final float LINE_END_X = 550f;
    private static final float BALANCE_LINE_END_X = 350f;
    private static final float VALUE_OFFSET_X = 180f;

    // ─── Espaciado vertical ───
    private static final float TITLE_SPACING = 35f;
    private static final float SUBTITLE_SPACING = 25f;
    private static final float SECTION_SPACING = 40f;
    private static final float ROW_SPACING = 22f;
    private static final float SEPARATOR_GAP = 5f;

    // ─── Tamaños de fuente ───
    private static final float FONT_SIZE_TITLE = 20f;
    private static final float FONT_SIZE_SUBTITLE = 14f;
    private static final float FONT_SIZE_BODY = 12f;
    private static final float FONT_SIZE_DETAIL = 11f;
    private static final float FONT_SIZE_FOOTER = 9f;

    // ─── Formato ───
    private static final NumberFormat CURRENCY_FORMATTER;
    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    static {
        CURRENCY_FORMATTER = NumberFormat.getCurrencyInstance(Locale.US);
        CURRENCY_FORMATTER.setMinimumFractionDigits(2);
        CURRENCY_FORMATTER.setMaximumFractionDigits(2);
    }

    // ─── Fuentes ───
    private final PDType1Font titleFont;
    private final PDType1Font bodyFont;

    public ReportPdfTemplate() {
        this.titleFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        this.bodyFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    }

    /**
     * Renderiza los datos del reporte dentro del documento PDF proporcionado.
     *
     * @param document documento PDF ya creado al que se agregará una página
     * @param report   entidad con los datos financieros a renderizar
     * @throws IOException si ocurre un error al escribir contenido en la página
     */
    public void render(PDDocument document, Report report) throws IOException {
        PDPage page = new PDPage(PDRectangle.LETTER);
        document.addPage(page);

        try (PDPageContentStream content = new PDPageContentStream(document, page)) {
            float y = CONTENT_START_Y;

            y = renderHeader(content, report, y);
            y = renderSeparator(content, y, LINE_END_X, 1f);
            y = renderFinancialDetails(content, report, y);
            y = renderSeparator(content, y - SEPARATOR_GAP, BALANCE_LINE_END_X, 0.5f);
            y = renderBalance(content, report, y);
            renderFooter(content, y - SECTION_SPACING);
        }
    }

    // ─── Secciones de renderizado ────────────────────────────────────────────

    private float renderHeader(PDPageContentStream content, Report report, float y) throws IOException {
        writeText(content, titleFont, FONT_SIZE_TITLE, PAGE_MARGIN, y, "Reporte Financiero");
        y -= TITLE_SPACING;

        writeText(content, titleFont, FONT_SIZE_SUBTITLE, PAGE_MARGIN, y,
                "Período: " + report.getPeriod());
        y -= SUBTITLE_SPACING;

        writeText(content, bodyFont, FONT_SIZE_DETAIL, PAGE_MARGIN, y,
                "Usuario: " + report.getUserId());
        y -= SECTION_SPACING;

        return y;
    }

    private float renderFinancialDetails(PDPageContentStream content, Report report, float y) throws IOException {
        y = renderCurrencyRow(content, y, "Total Ingresos:", report.getTotalIncome());
        y = renderCurrencyRow(content, y, "Total Gastos:", report.getTotalExpense());
        return y;
    }

    private float renderBalance(PDPageContentStream content, Report report, float y) throws IOException {
        return renderCurrencyRow(content, y, "Balance:", report.getBalance());
    }

    private void renderFooter(PDPageContentStream content, float y) throws IOException {
        String timestamp = OffsetDateTime.now().format(TIMESTAMP_FORMATTER);
        writeText(content, bodyFont, FONT_SIZE_FOOTER, PAGE_MARGIN, y,
                "Documento generado el: " + timestamp);
    }

    // ─── Helpers de bajo nivel ───────────────────────────────────────────────

    private float renderCurrencyRow(PDPageContentStream content, float y,
                                     String label, BigDecimal amount) throws IOException {
        writeText(content, titleFont, FONT_SIZE_BODY, PAGE_MARGIN, y, label);
        writeText(content, bodyFont, FONT_SIZE_BODY, PAGE_MARGIN + VALUE_OFFSET_X, y,
                formatCurrency(amount));
        return y - ROW_SPACING;
    }

    private float renderSeparator(PDPageContentStream content, float y,
                                   float endX, float lineWidth) throws IOException {
        content.setLineWidth(lineWidth);
        content.moveTo(PAGE_MARGIN, y);
        content.lineTo(endX, y);
        content.stroke();
        return y - SUBTITLE_SPACING;
    }

    private void writeText(PDPageContentStream content, PDType1Font font,
                           float fontSize, float x, float y, String text) throws IOException {
        content.beginText();
        content.setFont(font, fontSize);
        content.newLineAtOffset(x, y);
        content.showText(text);
        content.endText();
    }

    /**
     * Formatea un monto {@link BigDecimal} como moneda USD.
     */
    static String formatCurrency(BigDecimal amount) {
        return CURRENCY_FORMATTER.format(amount);
    }
}
