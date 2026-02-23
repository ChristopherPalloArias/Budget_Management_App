package com.microservice.transaction.service.impl;

import com.microservice.transaction.dto.TransactionRequest;
import com.microservice.transaction.dto.TransactionResponse;
import com.microservice.transaction.event.TransactionCreatedEvent;
import com.microservice.transaction.model.Transaction;
import com.microservice.transaction.model.TransactionType;
import com.microservice.transaction.repository.TransactionRepository;
import com.microservice.transaction.exception.EntityNotFoundException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @InjectMocks
    private TransactionServiceImpl transactionService;
    @Captor
    private ArgumentCaptor<Transaction> transactionCaptor;
    @Captor
    private ArgumentCaptor<TransactionCreatedEvent> eventCaptor;

    @Test
    @DisplayName("create — Happy Path: persiste la transacción, publica el evento y retorna la respuesta correcta")
    void create_conRequestValido_persisteTransaccionYPublicaEvento() {
        TransactionRequest mockRequest = mock(TransactionRequest.class);
        when(mockRequest.userId()).thenReturn("user-001");
        when(mockRequest.type()).thenReturn(TransactionType.INCOME);
        when(mockRequest.amount()).thenReturn(new BigDecimal("2500.50"));
        when(mockRequest.category()).thenReturn("Salario");
        when(mockRequest.date()).thenReturn(LocalDate.of(2026, 2, 12));
        when(mockRequest.description()).thenReturn("Pago quincenal de salario");

        Transaction mockSavedTransaction = mock(Transaction.class);
        when(mockSavedTransaction.getTransactionId()).thenReturn(42L);
        when(mockSavedTransaction.getUserId()).thenReturn("user-001");
        when(mockSavedTransaction.getType()).thenReturn(TransactionType.INCOME);
        when(mockSavedTransaction.getAmount()).thenReturn(new BigDecimal("2500.50"));
        when(mockSavedTransaction.getCategory()).thenReturn("Salario");
        when(mockSavedTransaction.getDate()).thenReturn(LocalDate.of(2026, 2, 12));
        when(mockSavedTransaction.getDescription()).thenReturn("Pago quincenal de salario");
        when(mockSavedTransaction.getCreatedAt()).thenReturn(OffsetDateTime.parse("2026-02-12T20:00:00-05:00"));

        when(transactionRepository.save(any(Transaction.class)))
                .thenReturn(mockSavedTransaction);

        TransactionResponse response = transactionService.create(mockRequest);

        verify(transactionRepository).save(transactionCaptor.capture());
        Transaction entidadEnviada = transactionCaptor.getValue();

        assertAll("Mapeo correcto del DTO → Entidad antes de persistir",
                () -> assertEquals("user-001", entidadEnviada.getUserId(),
                        "El userId debe mapearse desde el request"),
                () -> assertEquals(TransactionType.INCOME, entidadEnviada.getType(),
                        "El tipo de transacción debe mapearse desde el request"),
                () -> assertEquals(new BigDecimal("2500.50"), entidadEnviada.getAmount(),
                        "El monto debe mapearse desde el request"),
                () -> assertEquals("Salario", entidadEnviada.getCategory(),
                        "La categoría debe mapearse desde el request"),
                () -> assertEquals(LocalDate.of(2026, 2, 12), entidadEnviada.getDate(),
                        "La fecha debe mapearse desde el request"),
                () -> assertEquals("Pago quincenal de salario", entidadEnviada.getDescription(),
                        "La descripción debe mapearse desde el request")
        );

        verify(eventPublisher).publishEvent(eventCaptor.capture());
        TransactionCreatedEvent eventoPublicado = eventCaptor.getValue();

        assertSame(mockSavedTransaction, eventoPublicado.getTransaction(),
                "El evento debe contener la misma instancia de Transaction retornada por el repositorio");

        assertAll("Respuesta mapeada correctamente desde la entidad guardada",
                () -> assertNotNull(response,
                        "La respuesta no debe ser nula"),
                () -> assertEquals(42L, response.transactionId(),
                        "El ID debe ser el generado por la base de datos"),
                () -> assertEquals("user-001", response.userId(),
                        "El userId de la respuesta debe coincidir"),
                () -> assertEquals(TransactionType.INCOME, response.type(),
                        "El tipo de la respuesta debe coincidir"),
                () -> assertEquals(new BigDecimal("2500.50"), response.amount(),
                        "El monto de la respuesta debe coincidir"),
                () -> assertEquals("Salario", response.category(),
                        "La categoría de la respuesta debe coincidir"),
                () -> assertEquals(LocalDate.of(2026, 2, 12), response.date(),
                        "La fecha de la respuesta debe coincidir"),
                () -> assertEquals("Pago quincenal de salario", response.description(),
                        "La descripción de la respuesta debe coincidir"),
                () -> assertEquals(OffsetDateTime.parse("2026-02-12T20:00:00-05:00"), response.createdAt(),
                        "El timestamp de creación debe ser el asignado por la persistencia")
        );

        verifyNoMoreInteractions(transactionRepository, eventPublisher);
    }

    @Test
    @DisplayName("update — Happy Path: actualiza la transacción y publica el evento")
    void shouldUpdateTransactionSuccessfully_andPublishEvent() {
        Long transactionId = 123L;
        TransactionRequest request = new TransactionRequest(
                "user-123",
                TransactionType.EXPENSE,
                new BigDecimal("150.00"),
                "Alimentacion",
                LocalDate.of(2025, 3, 10),
                "Compra semanal"
        );

        Transaction existing = Transaction.builder()
                .transactionId(transactionId)
                .userId("user-123")
                .type(TransactionType.EXPENSE)
                .amount(new BigDecimal("100.00"))
                .category("Hogar")
                .date(LocalDate.of(2025, 3, 9))
                .description("Compra anterior")
                .createdAt(OffsetDateTime.parse("2025-03-09T10:00:00-05:00"))
                .build();

        Transaction saved = Transaction.builder()
                .transactionId(transactionId)
                .userId("user-123")
                .type(TransactionType.EXPENSE)
                .amount(new BigDecimal("150.00"))
                .category("Alimentacion")
                .date(LocalDate.of(2025, 3, 10))
                .description("Compra semanal")
                .createdAt(existing.getCreatedAt())
                .build();

        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(existing));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(saved);

        TransactionResponse response = transactionService.update(transactionId, request);

        assertAll("Respuesta de actualizacion",
                () -> assertNotNull(response, "La respuesta no debe ser nula"),
                () -> assertEquals(transactionId, response.transactionId(), "El ID debe coincidir"),
                () -> assertEquals(new BigDecimal("150.00"), response.amount(), "El monto debe actualizarse"),
                () -> assertEquals("Alimentacion", response.category(), "La categoria debe actualizarse")
        );

        verify(transactionRepository).findById(transactionId);
        verify(transactionRepository).save(any(Transaction.class));
        verify(eventPublisher).publishEvent(any());
    }

    @Test
    @DisplayName("update — Not Found: lanza EntityNotFoundException y no publica evento")
    void shouldReturn404_whenTransactionNotFound() {
        Long transactionId = 999L;
        TransactionRequest request = new TransactionRequest(
                "user-123",
                TransactionType.EXPENSE,
                new BigDecimal("150.00"),
                "Alimentacion",
                LocalDate.of(2025, 3, 10),
                "Compra semanal"
        );

        when(transactionRepository.findById(transactionId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> transactionService.update(transactionId, request),
                "Debe lanzar EntityNotFoundException cuando no existe"
        );

        verify(transactionRepository).findById(transactionId);
        verify(transactionRepository, never()).save(any(Transaction.class));
        verifyNoInteractions(eventPublisher);
    }

    @Test
    @DisplayName("update — Bad Request: rechaza monto negativo y no publica evento")
    void shouldReturn400_whenAmountIsNegative() {
        TransactionRequest request = new TransactionRequest(
                "user-123",
                TransactionType.EXPENSE,
                new BigDecimal("-10.00"),
                "Alimentacion",
                LocalDate.of(2025, 3, 10),
                "Compra semanal"
        );

        assertThrows(IllegalArgumentException.class,
                () -> transactionService.update(123L, request),
                "Debe rechazar monto negativo"
        );

        verifyNoInteractions(transactionRepository, eventPublisher);
    }
}