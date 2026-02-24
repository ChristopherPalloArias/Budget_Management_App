package com.microservice.auth.exception;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler for the Auth microservice.
 * 
 * Maps application exceptions to standardized RFC 7807-inspired HTTP error responses.
 * All errors are returned in the StandardErrorResponse format to ensure consistency
 * across the microservices architecture.
 * 
 * Adheres to:
 * - Hexagonal Architecture (adapter layer)
 * - Single Responsibility Principle (error translation only)
 * - REST conventions (appropriate HTTP status codes)
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * Builds a standardized error response with all required fields.
     * 
     * @param status HTTP status code
     * @param ex Exception with message
     * @param request HTTP request containing the path
     * @return ResponseEntity with StandardErrorResponse
     */
    private ResponseEntity<StandardErrorResponse> buildErrorResponse(
            HttpStatus status,
            Exception ex,
            HttpServletRequest request) {
        StandardErrorResponse body = StandardErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<StandardErrorResponse> handleAuthException(
            AuthException ex, 
            HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, ex, request);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<StandardErrorResponse> handleEmailAlreadyExists(
            EmailAlreadyExistsException ex,
            HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.CONFLICT, ex, request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<StandardErrorResponse> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining(", "));

        StandardErrorResponse body = StandardErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(errors)
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<StandardErrorResponse> handleGeneric(
            Exception ex, 
            HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex, request);
    }
}
