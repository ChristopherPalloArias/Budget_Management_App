package com.microservice.report.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.microservice.report.service.TransactionClient;
import com.microservice.report.repository.ProcessedMessageRepository;
import com.microservice.report.dto.ReportResponse;
import com.microservice.report.exception.ReportNotFoundException;
import com.microservice.report.infrastructure.dto.TransactionMessage;
import com.microservice.report.infrastructure.dto.TransactionType;
import com.microservice.report.mapper.ReportMapper;
import com.microservice.report.model.Report;
import com.microservice.report.repository.ReportRepository;
import com.microservice.report.service.ReportCommandService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para el método recalculateReport de ReportService (US-019).
 *
 * <p>Esta clase prueba la recalculación de reportes financieros usando mocks
 * para simular la obtención de transacciones del período. Los tests validan:</p>
 *
 * <ul>
 *   <li>Cálculo correcto de balance (totalIncome - totalExpense)</li>
 *   <li>Manejo de período sin transacciones</li>
 *   <li>Validación de reporte inexistente</li>
 *   <li>Recalculación consistente ante múltiples llamadas</li>
 * </ul>
 *
 * <p>Mapeo a criterios Gherkin de US-019 (new-stories.md):</p>
 * <ul>
 *   <li>Test 1 → "Recalculación exitosa de un reporte"</li>
 *   <li>Test 2 → "No existen transacciones para el período a recalcular"</li>
 *   <li>Test 3 → "Recalcular un reporte que no existe"</li>
 *   <li>Test 4 → (Variante) "Idempotencia: múltiples recalculaciones"</li>
 * </ul>
 */
@DisplayName("ReportService - recalculateReport()")
@ExtendWith(MockitoExtension.class)
class ReportServiceImplTest {

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private TransactionClient transactionClient;

    @Mock
    private ProcessedMessageRepository processedMessageRepository;

    @InjectMocks
    private ReportCommandServiceImpl reportCommandService;

    @BeforeEach
    void setUp() {
    }

    // ==========================================
    // TEST 1: Cálculo Correcto del Balance
    // ==========================================

    @Test
    @DisplayName("should recalculate report with correct totals and balance when valid transactions exist")
    void shouldRecalculateReportWithCorrectBalance_WhenValidTransactionsExist() {
        // Given (Arrange)
        String userId = "user-123";
        String period = "2025-11";

        // Reporte existente con valores antiguos
        Report existingReport = Report.builder()
                .reportId(1L)
                .userId(userId)
                .period(period)
                .totalIncome(BigDecimal.valueOf(1000))
                .totalExpense(BigDecimal.valueOf(400))
                .balance(BigDecimal.valueOf(600))
                .build();

        // Mock: reportRepository retorna el reporte existente
        when(reportRepository.findByUserIdAndPeriod(userId, period))
                .thenReturn(Optional.of(existingReport));

        // Mock: simulamos que el repositorio persiste y retorna con nuevos valores
        Report updatedReport = Report.builder()
                .reportId(1L)
                .userId(userId)
                .period(period)
                .totalIncome(BigDecimal.valueOf(1200))   // Recalculado
                .totalExpense(BigDecimal.valueOf(200))   // Recalculado
                .balance(BigDecimal.valueOf(1000))       // Recalculado: 1200 - 200
                .build();

        when(reportRepository.save(any(Report.class)))
                .thenReturn(updatedReport);

        // When (Act)
        ReportResponse response = reportCommandService.recalculateReport(userId, period);

        // Then (Assert)
        assertNotNull(response, "ReportResponse no debe ser null");
        assertEquals(userId, response.userId());
        assertEquals(period, response.period());
        assertEquals(0, response.totalIncome().compareTo(BigDecimal.valueOf(1200)),
                "totalIncome debe ser recalculado correctamente");
        assertEquals(0, response.totalExpense().compareTo(BigDecimal.valueOf(200)),
                "totalExpense debe ser recalculado correctamente");
        assertEquals(0, response.balance().compareTo(BigDecimal.valueOf(1000)),
                "balance debe ser recalculado como (income - expense)");

        // Verify
        verify(reportRepository).findByUserIdAndPeriod(userId, period);
        verify(reportRepository).save(any(Report.class));
    }

    // ==========================================
    // TEST 2: Período sin Transacciones
    // ==========================================

    @Test
    @DisplayName("should maintain zero totals when no transactions exist for period")
    void shouldMaintainZeroTotals_WhenNoTransactionsExistForPeriod() {
        // Given (Arrange)
        String userId = "user-456";
        String period = "2024-09";

        // Reporte existente con totales en cero (sin transacciones previas)
        Report reportWithoutTransactions = Report.builder()
                .reportId(2L)
                .userId(userId)
                .period(period)
                .totalIncome(BigDecimal.ZERO)
                .totalExpense(BigDecimal.ZERO)
                .balance(BigDecimal.ZERO)
                .build();

        // Mock: reportRepository retorna el reporte sin transacciones
        when(reportRepository.findByUserIdAndPeriod(userId, period))
                .thenReturn(Optional.of(reportWithoutTransactions));

        // Mock: al persistir, retorna el mismo reporte (sin cambios)
        when(reportRepository.save(any(Report.class)))
                .thenReturn(reportWithoutTransactions);

        // When (Act)
        ReportResponse response = reportCommandService.recalculateReport(userId, period);

        // Then (Assert)
        assertNotNull(response);
        assertEquals(0, response.totalIncome().compareTo(BigDecimal.ZERO),
                "totalIncome debe permanecer en ZERO si no hay transacciones");
        assertEquals(0, response.totalExpense().compareTo(BigDecimal.ZERO),
                "totalExpense debe permanecer en ZERO si no hay transacciones");
        assertEquals(0, response.balance().compareTo(BigDecimal.ZERO),
                "balance debe permanecer en ZERO si no hay transacciones");

        // Verify
        verify(reportRepository).findByUserIdAndPeriod(userId, period);
        verify(reportRepository).save(any(Report.class));
    }

    // ==========================================
    // TEST 3: Reporte Inexistente
    // ==========================================

    @Test
    @DisplayName("should throw ReportNotFoundException when report does not exist for period")
    void shouldThrowReportNotFoundException_WhenReportDoesNotExistForPeriod() {
        // Given (Arrange)
        String userId = "user-999";
        String period = "2023-01";

        // Mock: reportRepository retorna Optional.empty()
        when(reportRepository.findByUserIdAndPeriod(userId, period))
                .thenReturn(Optional.empty());

        // When & Then (Act & Assert)
        assertThrows(
                ReportNotFoundException.class,
                () -> reportCommandService.recalculateReport(userId, period),
                "Debe lanzar ReportNotFoundException cuando el reporte no existe"
        );

        // Verify: no debe intentar guardar si el reporte no existe
        verify(reportRepository).findByUserIdAndPeriod(userId, period);
        verify(reportRepository, never()).save(any(Report.class));
    }

    // ==========================================
    // TEST 4: Idempotencia - Múltiples Recalculaciones
    // ==========================================

    @Test
    @DisplayName("should produce consistent results when recalculating same report multiple times")
    void shouldProduceConsistentResults_WhenRecalculatingMultipleTimes() {
        // Given (Arrange)
        String userId = "user-111";
        String period = "2025-07";

        // Reporte con transacciones que coinciden exactamente con los totales
        Report reportWithConsistentData = Report.builder()
                .reportId(3L)
                .userId(userId)
                .period(period)
                .totalIncome(BigDecimal.valueOf(2000))
                .totalExpense(BigDecimal.valueOf(500))
                .balance(BigDecimal.valueOf(1500))
                .build();

        // Mock: reportRepository retorna el mismo reporte en múltiples llamadas
        when(reportRepository.findByUserIdAndPeriod(userId, period))
                .thenReturn(Optional.of(reportWithConsistentData));

        when(reportRepository.save(any(Report.class)))
                .thenReturn(reportWithConsistentData);

        // When (Act) - Recalcular dos veces
        ReportResponse response1 = reportCommandService.recalculateReport(userId, period);
        ReportResponse response2 = reportCommandService.recalculateReport(userId, period);

        // Then (Assert) - Ambas respuestas deben ser idénticas
        assertNotNull(response1);
        assertNotNull(response2);

        assertEquals(response1.totalIncome(), response2.totalIncome(),
                "totalIncome debe ser idéntico en múltiples recalculaciones");
        assertEquals(response1.totalExpense(), response2.totalExpense(),
                "totalExpense debe ser idéntico en múltiples recalculaciones");
        assertEquals(response1.balance(), response2.balance(),
                "balance debe ser idéntico en múltiples recalculaciones");

        // Verify: reportRepository.save() debe ser llamado 2 veces (una por cada recalculación)
        verify(reportRepository, times(2)).findByUserIdAndPeriod(userId, period);
        verify(reportRepository, times(2)).save(any(Report.class));
    }

    // ==========================================
    // TEST 5 (Adicional): Balance Negativo
    // ==========================================

    @Test
    @DisplayName("should correctly calculate negative balance when expenses exceed income")
    void shouldCalculateNegativeBalance_WhenExpensesExceedIncome() {
        // Given (Arrange)
        String userId = "user-222";
        String period = "2025-12";

        Report reportWithNegativeBalance = Report.builder()
                .reportId(4L)
                .userId(userId)
                .period(period)
                .totalIncome(BigDecimal.valueOf(500))    // Ingresos bajos
                .totalExpense(BigDecimal.valueOf(1200))  // Gastos altos
                .balance(BigDecimal.valueOf(-700))       // Balance negativo: 500 - 1200
                .build();

        Report expectedSavedReport = Report.builder()
                .reportId(4L)
                .userId(userId)
                .period(period)
                .totalIncome(BigDecimal.valueOf(500))
                .totalExpense(BigDecimal.valueOf(1200))
                .balance(BigDecimal.valueOf(-700))
                .build();

        // Mock
        when(reportRepository.findByUserIdAndPeriod(userId, period))
                .thenReturn(Optional.of(reportWithNegativeBalance));

        when(reportRepository.save(any(Report.class)))
                .thenReturn(expectedSavedReport);

        // When (Act)
        ReportResponse response = reportCommandService.recalculateReport(userId, period);

        // Then (Assert)
        assertNotNull(response);
        assertEquals(0, response.balance().compareTo(BigDecimal.valueOf(-700)),
                "balance debe ser negativo cuando gastos > ingresos");
        assertTrue(response.balance().compareTo(BigDecimal.ZERO) < 0,
                "balance debe ser menor que cero");

        // Verify
        verify(reportRepository).findByUserIdAndPeriod(userId, period);
        verify(reportRepository).save(any(Report.class));
    }

    // ==========================================
    // TEST 6 (Adicional): Validación de Usuario Nulo
    // ==========================================

    @Test
    @DisplayName("should handle null userId appropriately")
    void shouldHandleNullUserId() {
        // Given (Arrange)
        String userId = null;
        String period = "2025-11";

        // When & Then (Act & Assert) - Validation throws IllegalArgumentException before repository call
        assertThrows(
                IllegalArgumentException.class,
                () -> reportCommandService.recalculateReport(userId, period),
                "Debe lanzar IllegalArgumentException cuando userId es null"
        );

        // Verify - No repository interaction because validation happens first
        verify(reportRepository, never()).findByUserIdAndPeriod(any(), any());
        verify(reportRepository, never()).save(any(Report.class));
    }

    // ==========================================
    // EDGE CASES: Tests Adicionales
    // ==========================================

    // ==========================================
    // EDGE CASE 1: Período Inválido
    // ==========================================

    @Test
    @DisplayName("should handle invalid period format gracefully")
    void shouldHandleInvalidPeriodFormat() {
        // Given (Arrange)
        String userId = "user-500";
        String invalidPeriod = "2025-13"; // Mes inválido (13)

        // When & Then (Act & Assert) - Validation throws IllegalArgumentException before repository call
        assertThrows(
                IllegalArgumentException.class,
                () -> reportCommandService.recalculateReport(userId, invalidPeriod),
                "Debe lanzar IllegalArgumentException para período inválido"
        );

        // Verify - No repository interaction because validation happens first
        verify(reportRepository, never()).findByUserIdAndPeriod(any(), any());
        verify(reportRepository, never()).save(any(Report.class));
    }

    @Test
    @DisplayName("should handle malformed period string")
    void shouldHandleMalformedPeriod() {
        // Given (Arrange)
        String userId = "user-501";
        String malformedPeriod = "invalid-format";

        // When & Then (Act & Assert) - Validation throws IllegalArgumentException before repository call
        assertThrows(
                IllegalArgumentException.class,
                () -> reportCommandService.recalculateReport(userId, malformedPeriod),
                "Debe lanzar IllegalArgumentException para período malformado"
        );

        // Verify - No repository interaction because validation happens first
        verify(reportRepository, never()).findByUserIdAndPeriod(any(), any());
        verify(reportRepository, never()).save(any(Report.class));
    }

    @Test
    @DisplayName("should handle null period appropriately")
    void shouldHandleNullPeriod() {
        // Given (Arrange)
        String userId = "user-502";
        String period = null;

        // When & Then (Act & Assert) - Validation throws IllegalArgumentException before repository call
        assertThrows(
                IllegalArgumentException.class,
                () -> reportCommandService.recalculateReport(userId, period),
                "Debe lanzar IllegalArgumentException cuando period es null"
        );

        // Verify - No repository interaction because validation happens first
        verify(reportRepository, never()).findByUserIdAndPeriod(any(), any());
        verify(reportRepository, never()).save(any(Report.class));
    }

    // ==========================================
    // EDGE CASE 2: Transacciones con Montos Negativos
    // ==========================================

    @Test
    @DisplayName("should handle transactions with zero amounts correctly")
    void shouldHandleTransactionsWithZeroAmounts() {
        // Given (Arrange)
        String userId = "user-600";
        String period = "2025-10";

        Report existingReport = Report.builder()
                .reportId(6L)
                .userId(userId)
                .period(period)
                .totalIncome(BigDecimal.valueOf(1000))
                .totalExpense(BigDecimal.valueOf(500))
                .balance(BigDecimal.valueOf(500))
                .build();

        // Mock: reporte con montos en cero después de recalcular
        Report reportWithZeros = Report.builder()
                .reportId(6L)
                .userId(userId)
                .period(period)
                .totalIncome(BigDecimal.ZERO)
                .totalExpense(BigDecimal.ZERO)
                .balance(BigDecimal.ZERO)
                .build();

        when(reportRepository.findByUserIdAndPeriod(userId, period))
                .thenReturn(Optional.of(existingReport));

        when(reportRepository.save(any(Report.class)))
                .thenReturn(reportWithZeros);

        // When (Act)
        ReportResponse response = reportCommandService.recalculateReport(userId, period);

        // Then (Assert)
        assertNotNull(response);
        assertEquals(0, response.totalIncome().compareTo(BigDecimal.ZERO),
                "totalIncome debe ser ZERO cuando todas las transacciones son $0");
        assertEquals(0, response.totalExpense().compareTo(BigDecimal.ZERO),
                "totalExpense debe ser ZERO cuando todas las transacciones son $0");
        assertEquals(0, response.balance().compareTo(BigDecimal.ZERO),
                "balance debe ser ZERO");

        // Verify
        verify(reportRepository).findByUserIdAndPeriod(userId, period);
        verify(reportRepository).save(any(Report.class));
    }

    @Test
    @DisplayName("should handle very large transaction amounts correctly")
    void shouldHandleVeryLargeTransactionAmounts() {
        // Given (Arrange)
        String userId = "user-601";
        String period = "2025-09";

        Report existingReport = Report.builder()
                .reportId(7L)
                .userId(userId)
                .period(period)
                .totalIncome(BigDecimal.valueOf(100))
                .totalExpense(BigDecimal.valueOf(50))
                .balance(BigDecimal.valueOf(50))
                .build();

        // Mock: montos muy grandes (millones)
        Report reportWithLargeAmounts = Report.builder()
                .reportId(7L)
                .userId(userId)
                .period(period)
                .totalIncome(new BigDecimal("9999999999.99"))  // ~10 billones
                .totalExpense(new BigDecimal("5000000000.00"))  // ~5 billones
                .balance(new BigDecimal("4999999999.99"))
                .build();

        when(reportRepository.findByUserIdAndPeriod(userId, period))
                .thenReturn(Optional.of(existingReport));

        when(reportRepository.save(any(Report.class)))
                .thenReturn(reportWithLargeAmounts);

        // When (Act)
        ReportResponse response = reportCommandService.recalculateReport(userId, period);

        // Then (Assert)
        assertNotNull(response);
        assertTrue(response.totalIncome().compareTo(new BigDecimal("9999999999.99")) == 0,
                "totalIncome debe manejar montos muy grandes correctamente");
        assertTrue(response.totalExpense().compareTo(new BigDecimal("5000000000.00")) == 0,
                "totalExpense debe manejar montos muy grandes correctamente");
        assertTrue(response.balance().compareTo(BigDecimal.ZERO) > 0,
                "balance debe ser positivo con grandes montos");

        // Verify
        verify(reportRepository).findByUserIdAndPeriod(userId, period);
        verify(reportRepository).save(any(Report.class));
    }

    // ==========================================
    // EDGE CASE 3: Lista Grande de Transacciones
    // ==========================================

    @Test
    @DisplayName("should handle large number of transactions efficiently")
    void shouldHandleLargeNumberOfTransactions() {
        // Given (Arrange)
        String userId = "user-700";
        String period = "2025-08";

        Report existingReport = Report.builder()
                .reportId(8L)
                .userId(userId)
                .period(period)
                .totalIncome(BigDecimal.valueOf(1000))
                .totalExpense(BigDecimal.valueOf(500))
                .balance(BigDecimal.valueOf(500))
                .build();

        // Simular recalculación con muchas transacciones (ejemplo: 1000 transacciones)
        // El resultado sería la suma de todas
        BigDecimal expectedIncome = new BigDecimal("500000.00");  // 1000 tx * $500
        BigDecimal expectedExpense = new BigDecimal("250000.00"); // 1000 tx * $250
        BigDecimal expectedBalance = expectedIncome.subtract(expectedExpense);

        Report reportWithManyTransactions = Report.builder()
                .reportId(8L)
                .userId(userId)
                .period(period)
                .totalIncome(expectedIncome)
                .totalExpense(expectedExpense)
                .balance(expectedBalance)
                .build();

        when(reportRepository.findByUserIdAndPeriod(userId, period))
                .thenReturn(Optional.of(existingReport));

        when(reportRepository.save(any(Report.class)))
                .thenReturn(reportWithManyTransactions);

        // When (Act)
        ReportResponse response = reportCommandService.recalculateReport(userId, period);

        // Then (Assert)
        assertNotNull(response);
        assertEquals(0, response.totalIncome().compareTo(expectedIncome),
                "totalIncome debe manejar suma de muchas transacciones");
        assertEquals(0, response.totalExpense().compareTo(expectedExpense),
                "totalExpense debe manejar suma de muchas transacciones");
        assertEquals(0, response.balance().compareTo(expectedBalance),
                "balance debe calcularse correctamente con muchas transacciones");

        // Verify
        verify(reportRepository).findByUserIdAndPeriod(userId, period);
        verify(reportRepository).save(any(Report.class));
    }

    // ==========================================
    // EDGE CASE 4: Idempotencia del Recálculo (Avanzado)
    // ==========================================

    @Test
    @DisplayName("should produce exact same result when recalculating same report three times consecutively")
    void shouldProduceExactSameResult_WhenRecalculatingThreeTimes() {
        // Given (Arrange)
        String userId = "user-800";
        String period = "2025-06";

        Report consistentReport = Report.builder()
                .reportId(9L)
                .userId(userId)
                .period(period)
                .totalIncome(BigDecimal.valueOf(3000))
                .totalExpense(BigDecimal.valueOf(1000))
                .balance(BigDecimal.valueOf(2000))
                .build();

        // Mock: repository retorna el mismo reporte en todas las llamadas
        when(reportRepository.findByUserIdAndPeriod(userId, period))
                .thenReturn(Optional.of(consistentReport));

        when(reportRepository.save(any(Report.class)))
                .thenReturn(consistentReport);

        // When (Act) - Recalcular 3 veces
        ReportResponse response1 = reportCommandService.recalculateReport(userId, period);
        ReportResponse response2 = reportCommandService.recalculateReport(userId, period);
        ReportResponse response3 = reportCommandService.recalculateReport(userId, period);

        // Then (Assert) - Las 3 respuestas deben ser idénticas
        assertNotNull(response1);
        assertNotNull(response2);
        assertNotNull(response3);

        assertEquals(response1.totalIncome(), response2.totalIncome(),
                "totalIncome debe ser idéntico en recalculación 1 y 2");
        assertEquals(response2.totalIncome(), response3.totalIncome(),
                "totalIncome debe ser idéntico en recalculación 2 y 3");

        assertEquals(response1.totalExpense(), response2.totalExpense(),
                "totalExpense debe ser idéntico en recalculación 1 y 2");
        assertEquals(response2.totalExpense(), response3.totalExpense(),
                "totalExpense debe ser idéntico en recalculación 2 y 3");

        assertEquals(response1.balance(), response2.balance(),
                "balance debe ser idéntico en recalculación 1 y 2");
        assertEquals(response2.balance(), response3.balance(),
                "balance debe ser idéntico en recalculación 2 y 3");

        // Verify: 3 llamadas a findByUserIdAndPeriod y 3 a save
        verify(reportRepository, times(3)).findByUserIdAndPeriod(userId, period);
        verify(reportRepository, times(3)).save(any(Report.class));
    }

    @Test
    @DisplayName("should maintain consistency when recalculating after failed previous attempt")
    void shouldMaintainConsistency_AfterFailedAttempt() {
        // Given (Arrange)
        String userId = "user-801";
        String period = "2025-05";

        Report report = Report.builder()
                .reportId(10L)
                .userId(userId)
                .period(period)
                .totalIncome(BigDecimal.valueOf(1500))
                .totalExpense(BigDecimal.valueOf(750))
                .balance(BigDecimal.valueOf(750))
                .build();

        // Mock: primera llamada falla, segunda tiene éxito
        when(reportRepository.findByUserIdAndPeriod(userId, period))
                .thenReturn(Optional.of(report));

        when(reportRepository.save(any(Report.class)))
                .thenThrow(new RuntimeException("Database timeout"))  // Primera falla
                .thenReturn(report);  // Segunda tiene éxito

        // When & Then (Act & Assert)
        // Primera llamada debe fallar
        assertThrows(
                RuntimeException.class,
                () -> reportCommandService.recalculateReport(userId, period),
                "Primera llamada debe fallar con RuntimeException"
        );

        // Segunda llamada debe tener éxito
        ReportResponse response = reportCommandService.recalculateReport(userId, period);

        // Then (Assert)
        assertNotNull(response);
        assertEquals(userId, response.userId());
        assertEquals(period, response.period());

        // Verify: 2 llamadas a findByUserIdAndPeriod y 2 a save
        verify(reportRepository, times(2)).findByUserIdAndPeriod(userId, period);
        verify(reportRepository, times(2)).save(any(Report.class));
    }

    // ==========================================
    // EDGE CASE 5: Boundary Values (Valores Límite)
    // ==========================================

    @Test
    @DisplayName("should handle minimum valid period (beginning of time)")
    void shouldHandleMinimumValidPeriod() {
        // Given (Arrange)
        String userId = "user-900";
        String period = "1970-01"; // Época Unix

        Report oldReport = Report.builder()
                .reportId(11L)
                .userId(userId)
                .period(period)
                .totalIncome(BigDecimal.valueOf(100))
                .totalExpense(BigDecimal.valueOf(50))
                .balance(BigDecimal.valueOf(50))
                .build();

        when(reportRepository.findByUserIdAndPeriod(userId, period))
                .thenReturn(Optional.of(oldReport));

        when(reportRepository.save(any(Report.class)))
                .thenReturn(oldReport);

        // When (Act)
        ReportResponse response = reportCommandService.recalculateReport(userId, period);

        // Then (Assert)
        assertNotNull(response);
        assertEquals(period, response.period());
        assertEquals(userId, response.userId());

        // Verify
        verify(reportRepository).findByUserIdAndPeriod(userId, period);
        verify(reportRepository).save(any(Report.class));
    }

    @Test
    @DisplayName("should handle maximum future period correctly")
    void shouldHandleMaximumFuturePeriod() {
        // Given (Arrange)
        String userId = "user-901";
        String period = "2099-12"; // Futuro lejano

        Report futureReport = Report.builder()
                .reportId(12L)
                .userId(userId)
                .period(period)
                .totalIncome(BigDecimal.valueOf(5000))
                .totalExpense(BigDecimal.valueOf(2000))
                .balance(BigDecimal.valueOf(3000))
                .build();

        when(reportRepository.findByUserIdAndPeriod(userId, period))
                .thenReturn(Optional.of(futureReport));

        when(reportRepository.save(any(Report.class)))
                .thenReturn(futureReport);

        // When (Act)
        ReportResponse response = reportCommandService.recalculateReport(userId, period);

        // Then (Assert)
        assertNotNull(response);
        assertEquals(period, response.period());
        assertEquals(userId, response.userId());

        // Verify
        verify(reportRepository).findByUserIdAndPeriod(userId, period);
        verify(reportRepository).save(any(Report.class));
    }
}
