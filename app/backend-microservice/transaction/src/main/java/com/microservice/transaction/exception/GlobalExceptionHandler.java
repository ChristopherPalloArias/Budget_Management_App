package com.microservice.transaction.exception;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<CustomErrorResponse> handleNotFound(NotFoundException ex, HttpServletRequest request) {
        CustomErrorResponse body = CustomErrorResponse.builder()
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .dateTime(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<CustomErrorResponse> handleValidation(ValidationException ex, HttpServletRequest request) {
        CustomErrorResponse body = CustomErrorResponse.builder()
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .dateTime(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<CustomErrorResponse> handleNotFound(EntityNotFoundException ex, HttpServletRequest request) {
        CustomErrorResponse body = CustomErrorResponse.builder()
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .dateTime(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CustomErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
        CustomErrorResponse body = CustomErrorResponse.builder()
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .dateTime(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}