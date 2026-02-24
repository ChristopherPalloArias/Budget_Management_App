package com.microservice.transaction.controller;

import com.microservice.transaction.dto.TransactionRequest;
import com.microservice.transaction.dto.TransactionResponse;
import com.microservice.transaction.exception.NotFoundException;
import com.microservice.transaction.model.TransactionType;
import com.microservice.transaction.service.TransactionService;
import com.microservice.transaction.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import java.security.Principal;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private TransactionController transactionController;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(transactionController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    @DisplayName("update — happy path: returns 200 OK and delegates to service")
    void shouldUpdateTransactionSuccessfully_andPublishEvent() throws Exception {
        Long transactionId = 123L;
        TransactionResponse response = new TransactionResponse(
                transactionId,
                "user-123",
                TransactionType.EXPENSE,
                new BigDecimal("150.00"),
                "Alimentacion",
                LocalDate.of(2025, 3, 10),
                "Compra semanal",
                OffsetDateTime.parse("2025-03-10T10:00:00-05:00")
        );

        when(transactionService.updateTransaction(eq("user-123"), eq(transactionId), any(TransactionRequest.class)))
                .thenReturn(response);

        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("user-123");

        mockMvc.perform(put("/api/v1/transactions/{id}", transactionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .principal(principal)
                        .content("{"
                                + "\"type\":\"EXPENSE\","
                                + "\"amount\":150.00,"
                                + "\"category\":\"Alimentacion\","
                                + "\"date\":\"2025-03-10\","
                                + "\"description\":\"Compra semanal\""
                                + "}"))
                .andExpect(status().isOk());

        verify(transactionService).updateTransaction(eq("user-123"), eq(transactionId), any(TransactionRequest.class));
    }

    @Test
    @DisplayName("update — not found: returns 404 when transaction does not exist")
    void shouldReturn404_whenTransactionNotFound() throws Exception {
        Long transactionId = 999L;

        when(transactionService.updateTransaction(eq("user-123"), eq(transactionId), any(TransactionRequest.class)))
                .thenThrow(new NotFoundException("Transaction not found"));

        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("user-123");

        mockMvc.perform(put("/api/v1/transactions/{id}", transactionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .principal(principal)
                        .content("{"
                                + "\"type\":\"EXPENSE\","
                                + "\"amount\":150.00,"
                                + "\"category\":\"Alimentacion\","
                                + "\"date\":\"2025-03-10\","
                                + "\"description\":\"Compra semanal\""
                                + "}"))
                .andExpect(status().isNotFound());

        verify(transactionService).updateTransaction(eq("user-123"), eq(transactionId), any(TransactionRequest.class));
    }

    @Test
    @DisplayName("update — bad request: returns 400 when amount is negative")
    void shouldReturn400_whenAmountIsNegative() throws Exception {
        Long transactionId = 123L;

        mockMvc.perform(put("/api/v1/transactions/{id}", transactionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"
                                + "\"type\":\"EXPENSE\","
                                + "\"amount\":-10.00,"
                                + "\"category\":\"Alimentacion\","
                                + "\"date\":\"2025-03-10\","
                                + "\"description\":\"Compra semanal\""
                                + "}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(transactionService);
    }
}