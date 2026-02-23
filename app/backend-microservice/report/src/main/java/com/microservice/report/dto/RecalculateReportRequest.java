package com.microservice.report.dto;

import com.microservice.report.validation.ValidPeriod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO para la solicitud de recalculación de un reporte financiero.
 *
 * <p>Encapsula los parámetros requeridos para recalcular un reporte específico
 * de un usuario para un período determinado.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecalculateReportRequest {

    /**
     * Identificador del usuario propietario del reporte.
     */
    @NotBlank(message = "userId cannot be blank")
    private String userId;

    /**
     * Período mensual en formato yyyy-MM (ejemplo: 2025-11).
     */
    @NotBlank(message = "period cannot be blank")
    @ValidPeriod(message = "period must be in format yyyy-MM")
    private String period;
}
