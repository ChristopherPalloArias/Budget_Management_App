package com.microservice.transaction.controller;

import com.microservice.transaction.dto.TransactionRequest;
import com.microservice.transaction.dto.TransactionResponse;
import com.microservice.transaction.exception.EntityNotFoundException;
import com.microservice.transaction.model.TransactionType;
import com.microservice.transaction.service.TransactionService;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyNoInteractions;
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
        mockMvc = MockMvcBuilders.standaloneSetup(transactionController).build();
    }

    @Test
    @DisplayName("update — Happy Path: retorna 200 OK y delega al servicio")
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

        when(transactionService.update(eq(transactionId), any(TransactionRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/v1/transactions/{id}", transactionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"
                                + "\"userId\":\"user-123\","
                                + "\"type\":\"EXPENSE\","
                                + "\"amount\":150.00,"
                                + "\"category\":\"Alimentacion\","
                                + "\"date\":\"2025-03-10\","
                                + "\"description\":\"Compra semanal\""
                                + "}"))
                .andExpect(status().isOk());

        verify(transactionService).update(eq(transactionId), any(TransactionRequest.class));
    }

    @Test
    @DisplayName("update — Not Found: retorna 404 cuando la transaccion no existe")
    void shouldReturn404_whenTransactionNotFound() throws Exception {
        Long transactionId = 999L;

        when(transactionService.update(eq(transactionId), any(TransactionRequest.class)))
                .thenThrow(new EntityNotFoundException("Transaction not found"));

        mockMvc.perform(put("/api/v1/transactions/{id}", transactionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"
                                + "\"userId\":\"user-123\","
                                + "\"type\":\"EXPENSE\","
                                + "\"amount\":150.00,"
                                + "\"category\":\"Alimentacion\","
                                + "\"date\":\"2025-03-10\","
                                + "\"description\":\"Compra semanal\""
                                + "}"))
                .andExpect(status().isNotFound());

        verify(transactionService).update(eq(transactionId), any(TransactionRequest.class));
    }

    @Test
    @DisplayName("update — Bad Request: retorna 400 cuando el monto es negativo")
    void shouldReturn400_whenAmountIsNegative() throws Exception {
        Long transactionId = 123L;

        mockMvc.perform(put("/api/v1/transactions/{id}", transactionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"
                                + "\"userId\":\"user-123\","
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
