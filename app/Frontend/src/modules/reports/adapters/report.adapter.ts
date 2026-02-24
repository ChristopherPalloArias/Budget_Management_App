import type {
    ReportResponse,
    ReportItemResponse,
    ReportModel,
    ReportsSummaryModel,
} from '../types/report.types';
import { toSafeNumber } from '../../../shared/utils/currency';

const reportItemAdapter = (response: ReportItemResponse): ReportModel => {
    return {
        id: response.reportId,
        userId: response.userId,
        period: response.period,
        balance: toSafeNumber(response.balance),
        totalIncome: toSafeNumber(response.totalIncome),
        totalExpenses: toSafeNumber(response.totalExpense), // Fixed property mapping from singular to plural
        savings: toSafeNumber(response.savings),
        createdAt: response.createdAt ? new Date(response.createdAt) : new Date(),
        updatedAt: response.updatedAt ? new Date(response.updatedAt) : new Date(),
    };
};

export const reportAdapter = (response: ReportResponse): ReportsSummaryModel => {
    return {
        balance: toSafeNumber(response.balance),
        totalIncome: toSafeNumber(response.totalIncome),
        totalExpenses: toSafeNumber(response.totalExpense), // Fixed property mapping
        savings: toSafeNumber(response.savings),
        reports: response.reports ? response.reports.map(reportItemAdapter) : [],
    };
};

export const reportListAdapter = (response: ReportItemResponse[]): ReportModel[] => {
    return response.map(reportItemAdapter);
};