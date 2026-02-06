package com.microservice.report.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ReportNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleReportNotFound(ReportNotFoundException ex) {
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .message(ex.getMessage())
                .status(HttpStatus.NOT_FOUND.value())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
}
