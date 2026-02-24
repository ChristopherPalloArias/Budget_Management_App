package com.microservice.report.exception;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Standardized RFC 7807-inspired error response for all microservices.
 * 
 * Ensures consistent error contract across the distributed system,
 * reducing client-side complexity (single error parser).
 * 
 * Fields:
 * - timestamp: ISO-8601 formatted moment of error occurrence
 * - status: HTTP status code (404, 400, 500, etc.)
 * - error: Human-readable error type (e.g., "Not Found", "Bad Request")
 * - message: Detailed error message for debugging
 * - path: API path that triggered the error
 * 
 * Immutable design ensures thread-safety and prevents accidental modification.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StandardErrorResponse {
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    
    private int status;
    
    private String error;
    
    private String message;
    
    private String path;
}
