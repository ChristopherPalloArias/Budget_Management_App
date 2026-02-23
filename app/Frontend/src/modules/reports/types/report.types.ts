export interface ReportResponse {
    balance: number;
    totalIncome: number;
    totalExpenses: number;
    savings: number;
    reports: ReportItemResponse[];
}

export interface ReportItemResponse {
    reportId: number;
    userId: string;
    period: string;
    balance: number;
    totalIncome: number;
    totalExpenses: number;
    savings: number;
    createdAt: string;
    updatedAt: string;
}

export interface ReportModel {
    id: number;
    userId: string;
    period: string;
    balance: number;
    totalIncome: number;
    totalExpenses: number;
    savings: number;
    createdAt: Date;
    updatedAt: Date;
}

export interface ReportsSummaryModel {
    balance: number;
    totalIncome: number;
    totalExpenses: number;
    savings: number;
    reports: ReportModel[];
}

export interface ReportFilters {
    period?: string;
    startPeriod?: string;
    endPeriod?: string;
}

// Recalculate Report Types
export interface RecalculateReportRequest {
    userId: string;
    period: string;
}

export interface RecalculateReportResponse {
    balance: number;
    period: string;
}