package com.microservice.report.service;

import com.microservice.report.exception.ReportNotFoundException;
import com.microservice.report.model.Report;
import com.microservice.report.repository.ReportRepository;
import com.microservice.report.service.impl.ReportCommandServiceImpl;
import com.microservice.report.service.TransactionClient;
import com.microservice.report.repository.ProcessedMessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para el servicio de reportes siguiendo TDD (Fase RED).
 */
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

    private String userId;
    private String period;
    private Report mockReport;

    @BeforeEach
    void setUp() {
        userId = "user123";
        period = "2024-03";
        mockReport = new Report();
        mockReport.setReportId(1L);
        mockReport.setUserId(userId);
        mockReport.setPeriod(period);
    }

    @Test
    @DisplayName("test: US-017-E1 [delete financial report successfully when exists]")
    void deleteReport_WhenReportExistsAndBelongsToUser_ShouldDeleteSuccessfully() {
        // GIVEN: El reporte existe para el usuario y periodo especificado
        when(reportRepository.findByUserIdAndPeriod(userId, period))
                .thenReturn(Optional.of(mockReport));

        // WHEN: Se ejecuta la eliminación (Falla aquí: el método no existe aún)
        reportCommandService.deleteReport(userId, period);

        // THEN: Se valida que se buscó y eliminó correctamente en el repositorio
        verify(reportRepository, times(1)).findByUserIdAndPeriod(userId, period);
        verify(reportRepository, times(1)).delete(mockReport);
    }

    @Test
    @DisplayName("US-017-E4: Lanzar ReportNotFoundException cuando el reporte no existe")
    void deleteReport_WhenReportDoesNotExist_ShouldThrowException() {
        // GIVEN: El reporte no existe en la base de datos
        when(reportRepository.findByUserIdAndPeriod(userId, period))
                .thenReturn(Optional.empty());

        // WHEN & THEN: Se espera que lance ReportNotFoundException
        assertThrows(ReportNotFoundException.class, () -> {
            reportCommandService.deleteReport(userId, period);
        });

        // Validar que NO se intentó eliminar nada
        verify(reportRepository, never()).delete(any(Report.class));
    }
}
