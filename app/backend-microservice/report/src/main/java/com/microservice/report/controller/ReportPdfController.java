package com.microservice.report.controller;

import com.microservice.report.exception.ReportNotFoundException;
import com.microservice.report.model.Report;
import com.microservice.report.repository.ReportRepository;
import com.microservice.report.service.PdfGeneratorService;
import com.microservice.report.util.PdfFileNameGenerator;
import com.microservice.report.validation.ValidPeriod;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para la descarga de reportes en formato PDF.
 *
 * <p><strong>Historia de usuario:</strong> US-021 — Descargar Reporte de un Período como PDF</p>
 * <p><strong>Fase TDD:</strong> 🟢 GREEN</p>
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/reports")
@Validated
public class ReportPdfController {

    private final ReportRepository reportRepository;
    private final PdfGeneratorService pdfGeneratorService;

    /**
     * Descarga el reporte financiero de un período como archivo PDF.
     *
     * @param userId identificador del usuario
     * @param period período en formato {@code "yyyy-MM"}
     * @return respuesta HTTP con el archivo PDF como {@code application/pdf}
     * @throws ReportNotFoundException si no existe reporte para el usuario y período
     */
    @GetMapping("/{userId}/pdf")
    public ResponseEntity<byte[]> downloadPdf(
            @PathVariable String userId,
            @RequestParam @ValidPeriod String period) {

        Report report = reportRepository.findByUserIdAndPeriod(userId, period)
                .orElseThrow(() -> new ReportNotFoundException(userId, period));

        byte[] pdfBytes = pdfGeneratorService.generatePdf(report);
        String fileName = PdfFileNameGenerator.generateFileName(period);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdfBytes.length)
                .body(pdfBytes);
    }
}
