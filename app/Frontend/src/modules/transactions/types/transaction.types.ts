export interface TransactionResponse {
    reportId: number;
    userId: string;
    period: string;
    totalIncome: number;
    totalExpense: number;
    balance: number;
    createdAt: string;
    updatedAt: string;
}

export interface TransactionItemResponse {
    id: number;
    userId: string;
    amount: number;
    type: 'income' | 'expense';
    category: string;
    description: string;
    date: string;
    createdAt: string;
    updatedAt: string;
}

export interface TransactionModel {
    id: number;
    userId: string;
    amount: number;
    type: 'income' | 'expense';
    category: string;
    description: string;
    date: Date;
    createdAt: Date;
    updatedAt: Date;
}

export interface TransactionReportModel {
    id: number;
    userId: string;
    period: string;
    totalIncome: number;
    totalExpense: number;
    balance: number;
    createdAt: Date;
    updatedAt: Date;
}

export type TransactionFormData = Omit<TransactionModel, 'id' | 'userId' | 'createdAt' | 'updatedAt'>;