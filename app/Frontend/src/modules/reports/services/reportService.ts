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

export const deleteReport = async (id: string | number): Promise<void> => {
    await reportsHttpClient.delete(`/v1/reports/${id}`);
};