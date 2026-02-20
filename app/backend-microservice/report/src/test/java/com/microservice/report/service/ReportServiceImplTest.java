package com.microservice.report.service;

import com.microservice.report.model.Report;
import com.microservice.report.repository.ReportRepository;
import com.microservice.report.service.impl.ReportServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceImplTest {

    @Mock
    private ReportRepository reportRepository;

    @InjectMocks
    private ReportServiceImpl reportService;

    private String userId;
    private String period;
    private Report mockReport;

    @BeforeEach
    void setUp() {
        userId = "user123";
        period = "2024-01";
        mockReport = new Report();
        // Asumiendo que Report tiene estos campos por la estructura del repo
        mockReport.setId(1L);
        mockReport.setUserId(userId);
        mockReport.setPeriod(period);
    }

    @Test
    @DisplayName("US-017-E1: Eliminar reporte exitosamente cuando existe y pertenece al usuario")
    void deleteReport_WhenReportExistsAndBelongsToUser_ShouldDeleteSuccessfully() {
        // GIVEN: El reporte existe en la base de datos para ese usuario y periodo
        when(reportRepository.findByUserIdAndPeriod(userId, period))
                .thenReturn(Optional.of(mockReport));

        // WHEN: Se intenta eliminar el reporte
        // Nota: deleteReport aún no está en la interfaz ni en la impl (esto causará error de compilación/RED)
        reportService.deleteReport(userId, period);

        // THEN: Se debe verificar que se llamó al repositorio para buscar y eliminar
        verify(reportRepository, times(1)).findByUserIdAndPeriod(userId, period);
        verify(reportRepository, times(1)).delete(mockReport);
    }
}
