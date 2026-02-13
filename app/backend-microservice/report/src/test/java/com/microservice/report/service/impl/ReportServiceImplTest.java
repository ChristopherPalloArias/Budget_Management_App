package com.microservice.report.service.impl;

import com.microservice.report.infrastructure.dto.TransactionMessage;
import com.microservice.report.infrastructure.dto.TransactionType;
import com.microservice.report.model.Report;
import com.microservice.report.repository.ReportRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceImplTest {
        @Mock
        private ReportRepository reportRepository;
        @InjectMocks
        private ReportServiceImpl reportService;
        @Captor
        private ArgumentCaptor<Report> reportCaptor;

        @Test
        @DisplayName("updateReport — Happy Path: acumula un ingreso y un gasto sobre un reporte existente y calcula el balance correctamente")
        void updateReport_conIngresoYGasto_acumulaTotalesYCalculaBalanceCorrectamente() {
                TransactionMessage ingresoMessage = new TransactionMessage(
                                1L,
                                "user-001",
                                TransactionType.INCOME,
                                new BigDecimal("3000.00"),
                                LocalDate.of(2026, 2, 15),
                                "Salario",
                                "Pago quincenal de salario"
                );

                TransactionMessage gastoMessage = new TransactionMessage(
                                2L,
                                "user-001",
                                TransactionType.EXPENSE,
                                new BigDecimal("1200.50"),
                                LocalDate.of(2026, 2, 20),
                                "Alquiler",
                                "Pago mensual de alquiler"
                );

                Report reporteExistente = Report.builder()
                                .reportId(10L)
                                .userId("user-001")
                                .period("2026-02")
                                .totalIncome(BigDecimal.ZERO)
                                .totalExpense(BigDecimal.ZERO)
                                .balance(BigDecimal.ZERO)
                                .build();

                when(reportRepository.findByUserIdAndPeriod(eq("user-001"), eq("2026-02")))
                                .thenReturn(Optional.of(reporteExistente));
                when(reportRepository.save(any(Report.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));
                reportService.updateReport(ingresoMessage);
                reportService.updateReport(gastoMessage);

                verify(reportRepository, times(2)).save(reportCaptor.capture());
                assertEquals(2, reportCaptor.getAllValues().size(),
                                "save() debe haberse invocado exactamente 2 veces");

                Report reporteFinal = reportCaptor.getAllValues().get(1);
                assertAll("Estado FINAL del reporte después de procesar INGRESO + GASTO",
                                () -> assertEquals(10L, reporteFinal.getReportId(),
                                                "El reportId debe mantenerse (se actualiza, no se crea uno nuevo)"),
                                () -> assertEquals("user-001", reporteFinal.getUserId(),
                                                "El userId debe permanecer intacto"),
                                () -> assertEquals("2026-02", reporteFinal.getPeriod(),
                                                "El período debe permanecer intacto"),
                                () -> assertEquals(new BigDecimal("3000.00"), reporteFinal.getTotalIncome(),
                                                "totalIncome debe ser $3,000.00 (solo se acumuló un ingreso)"),
                                () -> assertEquals(new BigDecimal("1200.50"), reporteFinal.getTotalExpense(),
                                                "totalExpense debe ser $1,200.50 (solo se acumuló un gasto)"),
                                () -> assertEquals(new BigDecimal("1799.50"), reporteFinal.getBalance(),
                                                "balance debe ser $1,799.50 (3000.00 - 1200.50)"));
                verify(reportRepository, times(2)).findByUserIdAndPeriod("user-001", "2026-02");
                verifyNoMoreInteractions(reportRepository);
        }
}
