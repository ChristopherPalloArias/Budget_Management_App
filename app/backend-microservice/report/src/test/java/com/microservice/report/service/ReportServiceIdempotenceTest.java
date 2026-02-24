package com.microservice.report.service;

import com.microservice.report.exception.ReportNotFoundException;
import com.microservice.report.model.Report;
import com.microservice.report.repository.ReportRepository;
import com.microservice.report.service.impl.ReportServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
 * Pruebas de idempotencia para DELETE según RFC 9110 §9.3.5
 */
@DisplayName("ReportService - DELETE Idempotencia (RFC 9110 §9.3.5)")
@ExtendWith(MockitoExtension.class)
class ReportServiceIdempotenceTest {

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ReportServiceImpl reportService;

    private String userId;
    private String period;
    private Report mockReport;

    @BeforeEach
    void setUp() {
        userId = "user-idempotence-test";
        period = "2026-02";
        mockReport = new Report();
        mockReport.setReportId(1L);
        mockReport.setUserId(userId);
        mockReport.setPeriod(period);
    }

    @Nested
    @DisplayName("deleteReport() - Idempotencia por período")
    class DeleteReportByPeriodIdempotence {

        @Test
        @DisplayName("Primera llamada: reporte existe - debe eliminarse correctamente")
        void firstCall_WhenReportExists_ShouldDeleteSuccessfully() {
            // GIVEN: El reporte existe
            when(reportRepository.findByUserIdAndPeriod(userId, period))
                    .thenReturn(Optional.of(mockReport));

            // WHEN: Se ejecuta la primera eliminación
            assertDoesNotThrow(() -> reportService.deleteReport(userId, period));

            // THEN: Se verificó que se eliminó correctamente
            verify(reportRepository, times(1)).findByUserIdAndPeriod(userId, period);
            verify(reportRepository, times(1)).delete(mockReport);
        }

        @Test
        @DisplayName("RFC 9110 §9.3.5: Segunda llamada con el mismo período - no debe lanzar excepción")
        void secondCall_WhenReportAlreadyDeleted_ShouldNotThrowException() {
            // GIVEN: El reporte ya fue eliminado (Optional.empty())
            when(reportRepository.findByUserIdAndPeriod(userId, period))
                    .thenReturn(Optional.empty());

            // WHEN & THEN: Debe completar sin excepción (idempotencia)
            assertDoesNotThrow(() -> reportService.deleteReport(userId, period));

            // VERIFY: No debe intentar eliminar nada
            verify(reportRepository, times(1)).findByUserIdAndPeriod(userId, period);
            verify(reportRepository, never()).delete(any(Report.class));
        }

        @Test
        @DisplayName("Múltiples llamadas: todas deben devolver 204 silenciosamente")
        void multipleCalls_AllShouldComplete_WithoutException() {
            // GIVEN: Primera llamada tiene el reporte, siguientes no
            when(reportRepository.findByUserIdAndPeriod(userId, period))
                    .thenReturn(Optional.of(mockReport))      // 1ª llamada
                    .thenReturn(Optional.empty())             // 2ª llamada
                    .thenReturn(Optional.empty());            // 3ª llamada

            // WHEN: Se ejecutan múltiples DELETE al mismo período
            assertDoesNotThrow(() -> reportService.deleteReport(userId, period)); // 1ª
            assertDoesNotThrow(() -> reportService.deleteReport(userId, period)); // 2ª
            assertDoesNotThrow(() -> reportService.deleteReport(userId, period)); // 3ª

            // THEN: Se verificó búsqueda 3 veces pero eliminación solo 1 vez
            verify(reportRepository, times(3)).findByUserIdAndPeriod(userId, period);
            verify(reportRepository, times(1)).delete(mockReport);
        }

        @Test
        @DisplayName("Validación de parámetros: userId inválido aún debe validarse")
        void deleteReport_WithInvalidUserId_ShouldThrowIllegalArgumentException() {
            // GIVEN: userId nulo
            String invalidUserId = null;

            // WHEN & THEN: Debe fallar con IllegalArgumentException (validación)
            assertThrows(IllegalArgumentException.class, 
                    () -> reportService.deleteReport(invalidUserId, period));

            // Nunca debe intentar acceder al repositorio
            verify(reportRepository, never()).findByUserIdAndPeriod(any(), any());
        }

        @Test
        @DisplayName("Validación de período: formato inválido debe rechazarse")
        void deleteReport_WithInvalidPeriod_ShouldThrowIllegalArgumentException() {
            // GIVEN: período con formato incorrecto
            String invalidPeriod = "2026/02"; // Separador incorrecto

            // WHEN & THEN: Debe fallar con IllegalArgumentException
            assertThrows(IllegalArgumentException.class,
                    () -> reportService.deleteReport(userId, invalidPeriod));

            // Nunca debe intentar acceder al repositorio
            verify(reportRepository, never()).findByUserIdAndPeriod(any(), any());
        }
    }

    @Nested
    @DisplayName("deleteReportById() - Idempotencia por ID")
    class DeleteReportByIdIdempotence {

        private Long reportId = 1L;

        @Test
        @DisplayName("Primera llamada: reporte existe y pertenece al usuario - debe eliminarse")
        void firstCall_WhenReportExistsAndBelongsToUser_ShouldDelete() {
            // GIVEN: El reporte existe y pertenece al usuario
            when(reportRepository.findById(reportId))
                    .thenReturn(Optional.of(mockReport));

            // WHEN: Se ejecuta la primera eliminación
            assertDoesNotThrow(() -> reportService.deleteReportById(userId, reportId));

            // THEN: Se verificó eliminación correcta
            verify(reportRepository, times(1)).findById(reportId);
            verify(reportRepository, times(1)).delete(mockReport);
        }

        @Test
        @DisplayName("RFC 9110 §9.3.5: Segunda llamada con el mismo ID - no debe lanzar excepción")
        void secondCall_WhenReportAlreadyDeleted_ShouldNotThrowException() {
            // GIVEN: El reporte no existe (ya fue eliminado)
            when(reportRepository.findById(reportId))
                    .thenReturn(Optional.empty());

            // WHEN & THEN: Debe completar sin excepción (idempotencia)
            assertDoesNotThrow(() -> reportService.deleteReportById(userId, reportId));

            // VERIFY: No debe intentar eliminar nada
            verify(reportRepository, times(1)).findById(reportId);
            verify(reportRepository, never()).delete(any(Report.class));
        }

        @Test
        @DisplayName("Seguridad: Si el reporte pertenece a otro usuario, completar silenciosamente")
        void secondCall_WhenReportBelongsToAnotherUser_ShouldCompleteSilently() {
            // GIVEN: El reporte existe pero pertenece a otro usuario
            Report otherUserReport = new Report();
            otherUserReport.setReportId(1L);
            otherUserReport.setUserId("other-user");
            otherUserReport.setPeriod("2026-02");

            when(reportRepository.findById(reportId))
                    .thenReturn(Optional.of(otherUserReport));

            // WHEN & THEN: Debe completar sin excepción (no revelar existencia)
            assertDoesNotThrow(() -> reportService.deleteReportById(userId, reportId));

            // VERIFY: No debe intentar eliminar
            verify(reportRepository, times(1)).findById(reportId);
            verify(reportRepository, never()).delete(any(Report.class));
        }

        @Test
        @DisplayName("Múltiples llamadas: todas deben devolver 204 silenciosamente")
        void multipleCalls_AllShouldComplete_WithoutException() {
            // GIVEN: Primera llamada tiene el reporte, siguientes no
            when(reportRepository.findById(reportId))
                    .thenReturn(Optional.of(mockReport))      // 1ª llamada
                    .thenReturn(Optional.empty())             // 2ª llamada
                    .thenReturn(Optional.empty());            // 3ª llamada

            // WHEN: Se ejecutan múltiples DELETE al mismo ID
            assertDoesNotThrow(() -> reportService.deleteReportById(userId, reportId)); // 1ª
            assertDoesNotThrow(() -> reportService.deleteReportById(userId, reportId)); // 2ª
            assertDoesNotThrow(() -> reportService.deleteReportById(userId, reportId)); // 3ª

            // THEN: Se verificó búsqueda 3 veces pero eliminación solo 1 vez
            verify(reportRepository, times(3)).findById(reportId);
            verify(reportRepository, times(1)).delete(mockReport);
        }

        @Test
        @DisplayName("Validación: reportId nulo debe lanzar IllegalArgumentException")
        void deleteReportById_WithNullReportId_ShouldThrowIllegalArgumentException() {
            // WHEN & THEN: Debe fallar con IllegalArgumentException
            assertThrows(IllegalArgumentException.class,
                    () -> reportService.deleteReportById(userId, null));

            // Nunca debe acceder al repositorio
            verify(reportRepository, never()).findById(any());
        }

        @Test
        @DisplayName("Validación: reportId negativo debe lanzar IllegalArgumentException")
        void deleteReportById_WithNegativeReportId_ShouldThrowIllegalArgumentException() {
            // GIVEN: reportId negativo
            Long negativeId = -1L;

            // WHEN & THEN: Debe fallar
            assertThrows(IllegalArgumentException.class,
                    () -> reportService.deleteReportById(userId, negativeId));

            // Nunca debe acceder al repositorio
            verify(reportRepository, never()).findById(any());
        }

        @Test
        @DisplayName("Validación: reportId cero debe lanzar IllegalArgumentException")
        void deleteReportById_WithZeroReportId_ShouldThrowIllegalArgumentException() {
            // WHEN & THEN: Debe fallar
            assertThrows(IllegalArgumentException.class,
                    () -> reportService.deleteReportById(userId, 0L));

            // Nunca debe acceder al repositorio
            verify(reportRepository, never()).findById(any());
        }
    }

    @Nested
    @DisplayName("Comparativa: Behavior Between Both Delete Methods")
    class DeleteMethodsBehaviorComparison {

        @Test
        @DisplayName("Ambos métodos completan sin excepción cuando el recurso no existe")
        void bothMethods_ShouldCompleteWithoutException_WhenResourceDoesNotExist() {
            // GIVEN: Reportes no existen
            when(reportRepository.findByUserIdAndPeriod(userId, period))
                    .thenReturn(Optional.empty());
            when(reportRepository.findById(1L))
                    .thenReturn(Optional.empty());

            // WHEN & THEN: Ambos deben completar sin excepción
            assertDoesNotThrow(() -> reportService.deleteReport(userId, period));
            assertDoesNotThrow(() -> reportService.deleteReportById(userId, 1L));

            // VERIFY: Búsquedas pero sin eliminaciones
            verify(reportRepository).findByUserIdAndPeriod(userId, period);
            verify(reportRepository).findById(1L);
            verify(reportRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Ambos métodos son idempotentes (múltiples llamadas = mismo resultado)")
        void bothMethods_AreIdempotent_SameBehaviorOnRepeatedCalls() {
            // GIVEN: Setup para ambos métodos
            mockReport.setReportId(1L);
            when(reportRepository.findByUserIdAndPeriod(userId, period))
                    .thenReturn(Optional.of(mockReport))
                    .thenReturn(Optional.empty());
            when(reportRepository.findById(1L))
                    .thenReturn(Optional.of(mockReport))
                    .thenReturn(Optional.empty());

            // WHEN: Primera llamada a cada método
            assertDoesNotThrow(() -> reportService.deleteReport(userId, period));
            assertDoesNotThrow(() -> reportService.deleteReportById(userId, 1L));

            // THEN: Segunda llamada también debe completar sin excepción
            assertDoesNotThrow(() -> reportService.deleteReport(userId, period));
            assertDoesNotThrow(() -> reportService.deleteReportById(userId, 1L));

            // VERIFY: Cada método se llamó 2 veces en el repositorio
            verify(reportRepository, times(2)).findByUserIdAndPeriod(userId, period);
            verify(reportRepository, times(2)).findById(1L);
        }
    }
}
