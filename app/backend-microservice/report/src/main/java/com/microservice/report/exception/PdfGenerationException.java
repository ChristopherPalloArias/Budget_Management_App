package com.microservice.report.exception;

/**
 * Excepción lanzada cuando la generación de un PDF falla por un error del sistema.
 *
 * <p><strong>Historia de usuario:</strong> US-021 — Escenario E4 (Error interno durante generación)</p>
 */
public class PdfGenerationException extends RuntimeException {

    public PdfGenerationException(String message) {
        super(message);
    }

    public PdfGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
