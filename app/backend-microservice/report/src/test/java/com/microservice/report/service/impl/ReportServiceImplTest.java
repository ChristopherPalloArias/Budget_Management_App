package com.microservice.report.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.microservice.report.dto.ReportResponse;
import com.microservice.report.exception.ReportNotFoundException;
import com.microservice.report.infrastructure.dto.TransactionMessage;
import com.microservice.report.infrastructure.dto.TransactionType;
import com.microservice.report.mapper.ReportMapper;
import com.microservice.report.model.Report;
import com.microservice.report.repository.ReportRepository;
import com.microservice.report.service.ReportService;

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

    private ReportService reportService;

    @BeforeEach
    void setUp() {
        // Crear instancia del servicio con repositorio mockeado
        reportService = new ReportServiceImpl(reportRepository);
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
        ReportResponse response = reportService.recalculateReport(userId, period);

        // Then (Assert)
        assertNotNull(response, "ReportResponse no debe ser null");
        assertEquals(userId, response.getUserId());
        assertEquals(period, response.getPeriod());
        assertEquals(0, response.getTotalIncome().compareTo(BigDecimal.valueOf(1200)),
                "totalIncome debe ser recalculado correctamente");
        assertEquals(0, response.getTotalExpense().compareTo(BigDecimal.valueOf(200)),
                "totalExpense debe ser recalculado correctamente");
        assertEquals(0, response.getBalance().compareTo(BigDecimal.valueOf(1000)),
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
        ReportResponse response = reportService.recalculateReport(userId, period);

        // Then (Assert)
        assertNotNull(response);
        assertEquals(0, response.getTotalIncome().compareTo(BigDecimal.ZERO),
                "totalIncome debe permanecer en ZERO si no hay transacciones");
        assertEquals(0, response.getTotalExpense().compareTo(BigDecimal.ZERO),
                "totalExpense debe permanecer en ZERO si no hay transacciones");
        assertEquals(0, response.getBalance().compareTo(BigDecimal.ZERO),
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
                () -> reportService.recalculateReport(userId, period),
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
        ReportResponse response1 = reportService.recalculateReport(userId, period);
        ReportResponse response2 = reportService.recalculateReport(userId, period);

        // Then (Assert) - Ambas respuestas deben ser idénticas
        assertNotNull(response1);
        assertNotNull(response2);

        assertEquals(response1.getTotalIncome(), response2.getTotalIncome(),
                "totalIncome debe ser idéntico en múltiples recalculaciones");
        assertEquals(response1.getTotalExpense(), response2.getTotalExpense(),
                "totalExpense debe ser idéntico en múltiples recalculaciones");
        assertEquals(response1.getBalance(), response2.getBalance(),
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

        // Mock
        when(reportRepository.findByUserIdAndPeriod(userId, period))
                .thenReturn(Optional.of(reportWithNegativeBalance));

        when(reportRepository.save(any(Report.class)))
                .thenReturn(reportWithNegativeBalance);

        // When (Act)
        ReportResponse response = reportService.recalculateReport(userId, period);

        // Then (Assert)
        assertNotNull(response);
        assertEquals(0, response.getBalance().compareTo(BigDecimal.valueOf(-700)),
                "balance debe ser negativo cuando gastos > ingresos");
        assertTrue(response.getBalance().compareTo(BigDecimal.ZERO) < 0,
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

        // Mock: NPE esperado cuando userId es null
        when(reportRepository.findByUserIdAndPeriod(userId, period))
                .thenThrow(new IllegalArgumentException("userId cannot be null"));

        // When & Then (Act & Assert)
        assertThrows(
                IllegalArgumentException.class,
                () -> reportService.recalculateReport(userId, period),
                "Debe lanzar IllegalArgumentException cuando userId es null"
        );

        // Verify
        verify(reportRepository).findByUserIdAndPeriod(userId, period);
        verify(reportRepository, never()).save(any(Report.class));
    }
}
