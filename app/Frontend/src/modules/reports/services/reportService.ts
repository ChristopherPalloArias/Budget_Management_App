import type { ReportsSummaryModel, ReportModel, ReportFilters } from '../types/report.types';
import { reportAdapter, reportListAdapter } from '../adapters/report.adapter';
import HttpClient from '../../../core/api/HttpClient';
import type { ReportResponse, ReportItemResponse } from '../types/report.types';

// Obtener instancia específica para el microservicio de reports
const reportsHttpClient = HttpClient.getInstance('reports');

export const getReportsSummary = async (userId: string, filters: Omit<ReportFilters, 'period'>): Promise<ReportsSummaryModel> => {
    const params = new URLSearchParams();
    if (filters.startPeriod) params.append('startPeriod', filters.startPeriod);
    if (filters.endPeriod) params.append('endPeriod', filters.endPeriod);

    const endpoint = `/v1/reports/${userId}/summary${params.toString() ? `?${params.toString()}` : ''}`;
    const response = await reportsHttpClient.get<ReportResponse>(endpoint);
    return reportAdapter(response.data);
};

export const getReportByPeriod = async (userId: string, filters: Required<Pick<ReportFilters, 'period'>>): Promise<ReportModel> => {
    const endpoint = `/v1/reports/${userId}?period=${filters.period}`;
    const response = await reportsHttpClient.get<ReportItemResponse>(endpoint);
    return reportListAdapter([response.data])[0];
};

/**
 * Descarga el reporte financiero de un período como archivo PDF.
 * Dispara la descarga automática en el navegador.
 *
 * US-021 — Descargar Reporte de un Período como PDF
 * Fase TDD: 🔵 REFACTOR — Separación de responsabilidades
 */
export const downloadReportPdf = async (userId: string, period: string): Promise<void> => {
    const endpoint = `/v1/reports/${userId}/pdf?period=${period}`;
    const response = await reportsHttpClient.get(endpoint, {
        responseType: 'blob',
    });

    const fileName = `reporte-${period}.pdf`;
    triggerBlobDownload(response.data, fileName, 'application/pdf');
};

/**
 * Dispara la descarga de un blob en el navegador.
 * Reutilizable para cualquier tipo de archivo descargable.
 */
const triggerBlobDownload = (data: BlobPart, fileName: string, mimeType: string): void => {
    const blob = new Blob([data], { type: mimeType });
    const url = globalThis.URL.createObjectURL(blob);

    const link = document.createElement('a');
    link.href = url;
    link.download = fileName;
    document.body.appendChild(link);
    link.click();

    link.remove();
    globalThis.URL.revokeObjectURL(url);
};

/**
 * Elimina un reporte financiero por su ID.
 * Requiere confirmación del usuario antes de ser invocado.
 */
export const deleteReport = async (id: string | number): Promise<void> => {
    await reportsHttpClient.delete(`/v1/reports/${id}`);
};
