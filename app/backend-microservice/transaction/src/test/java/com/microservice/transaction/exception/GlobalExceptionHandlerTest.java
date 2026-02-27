package com.microservice.transaction.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests para el GlobalExceptionHandler del Transaction Service.
 *
 * Verifica que todos los tipos de excepciones se manejan correctamente:
 * - NotFoundException → 404
 * - ValidationException → 400
 * - MethodArgumentNotValidException → 400 (con detalles de campos)
 * - ConstraintViolationException → 400 (con detalles de restricciones)
 * - Exception genérica → 500
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalExceptionHandler - Transaction Service")
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @Mock
    private HttpServletRequest mockRequest;

    @Test
    @DisplayName("handleNotFound — NotFoundException returns 404")
    void testHandleNotFoundException() {
        // Given
        String errorMessage = "Transaction not found with id: 123";
        NotFoundException exception = new NotFoundException(errorMessage);
        when(mockRequest.getRequestURI()).thenReturn("/api/v1/transactions/123");

        // When
        ResponseEntity<CustomErrorResponse> response = globalExceptionHandler.handleNotFound(exception, mockRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(errorMessage, response.getBody().getMessage());
        assertEquals("/api/v1/transactions/123", response.getBody().getPath());
        assertNotNull(response.getBody().getDateTime());
    }

    @Test
    @DisplayName("handleValidation — ValidationException returns 400")
    void testHandleValidationException() {
        // Given
        String errorMessage = "Amount must be greater than zero";
        ValidationException exception = new ValidationException(errorMessage);
        when(mockRequest.getRequestURI()).thenReturn("/api/v1/transactions");

        // When
        ResponseEntity<CustomErrorResponse> response = globalExceptionHandler.handleValidation(exception, mockRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(errorMessage, response.getBody().getMessage());
        assertEquals("/api/v1/transactions", response.getBody().getPath());
    }

    @Test
    @DisplayName("handleMethodArgumentNotValid — MethodArgumentNotValidException returns 400 with field errors")
    void testHandleMethodArgumentNotValidException() {
        // Given
        BindingResult bindingResult = mock(BindingResult.class);
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(
                mock(org.springframework.core.MethodParameter.class),
                bindingResult
        );
        
        FieldError fieldError1 = new FieldError("transactionRequest", "amount", "must be greater than zero");
        FieldError fieldError2 = new FieldError("transactionRequest", "type", "must not be null");
        
        when(bindingResult.getFieldErrors()).thenReturn(java.util.List.of(fieldError1, fieldError2));
        when(mockRequest.getRequestURI()).thenReturn("/api/v1/transactions");

        // When
        ResponseEntity<CustomErrorResponse> response = globalExceptionHandler.handleMethodArgumentNotValid(exception, mockRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        
        String message = response.getBody().getMessage();
        assertTrue(message.contains("Validation failed:"), "Message should contain 'Validation failed:'");
        assertTrue(message.contains("amount") && message.contains("must be greater than zero"),
                "Message should contain field name and error message for amount");
        assertTrue(message.contains("type") && message.contains("must not be null"),
                "Message should contain field name and error message for type");
        assertEquals("/api/v1/transactions", response.getBody().getPath());
    }

    @Test
    @DisplayName("handleConstraintViolation — ConstraintViolationException returns 400 with violation details")
    void testHandleConstraintViolationException() {
        // Given
        Set<ConstraintViolation<?>> violations = new HashSet<>();
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        
        when(violation.getPropertyPath()).thenReturn(
                new MockPropertyPath("amount")
        );
        when(violation.getMessage()).thenReturn("must be greater than 0");
        
        violations.add(violation);
        
        ConstraintViolationException exception = new ConstraintViolationException("Constraint violation", violations);
        when(mockRequest.getRequestURI()).thenReturn("/api/v1/transactions");

        // When
        ResponseEntity<CustomErrorResponse> response = globalExceptionHandler.handleConstraintViolation(exception, mockRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        
        String message = response.getBody().getMessage();
        assertTrue(message.contains("Constraint validation failed:"),
                "Message should contain 'Constraint validation failed:'");
        assertTrue(message.contains("amount") && message.contains("must be greater than 0"),
                "Message should contain property path and violation message");
        assertEquals("/api/v1/transactions", response.getBody().getPath());
    }

    @Test
    @DisplayName("handleConstraintViolation — Multiple violations are formatted correctly")
    void testHandleConstraintViolationExceptionMultiple() {
        // Given
        Set<ConstraintViolation<?>> violations = new HashSet<>();
        
        ConstraintViolation<?> violation1 = mock(ConstraintViolation.class);
        when(violation1.getPropertyPath()).thenReturn(new MockPropertyPath("amount"));
        when(violation1.getMessage()).thenReturn("must be greater than 0");
        violations.add(violation1);
        
        ConstraintViolation<?> violation2 = mock(ConstraintViolation.class);
        when(violation2.getPropertyPath()).thenReturn(new MockPropertyPath("type"));
        when(violation2.getMessage()).thenReturn("must not be null");
        violations.add(violation2);
        
        ConstraintViolationException exception = new ConstraintViolationException("Constraint violation", violations);
        when(mockRequest.getRequestURI()).thenReturn("/api/v1/transactions");

        // When
        ResponseEntity<CustomErrorResponse> response = globalExceptionHandler.handleConstraintViolation(exception, mockRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        
        String message = response.getBody().getMessage();
        assertTrue(message.contains("Constraint validation failed:"),
                "Message should contain 'Constraint validation failed:'");
        assertTrue(message.contains("amount") && message.contains("must be greater than 0"));
        assertTrue(message.contains("type") && message.contains("must not be null"));
    }

    @Test
    @DisplayName("handleGeneric — Generic Exception returns 500")
    void testHandleGenericException() {
        // Given
        Exception exception = new RuntimeException("Unexpected error");
        when(mockRequest.getRequestURI()).thenReturn("/api/v1/transactions");

        // When
        ResponseEntity<CustomErrorResponse> response = globalExceptionHandler.handleGeneric(exception, mockRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Unexpected error", response.getBody().getMessage());
    }

    /**
     * Mock implementation of PropertyPath for testing.
     */
    private static class MockPropertyPath implements jakarta.validation.Path {
        private final String name;

        MockPropertyPath(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }

        @Override
        public java.util.Iterator<Node> iterator() {
            return java.util.Collections.emptyIterator();
        }
    }
}
