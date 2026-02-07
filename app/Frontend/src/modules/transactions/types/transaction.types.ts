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
    transactionId: number;
    userId: string;
    amount: number;
    type: 'INCOME' | 'EXPENSE';
    category: string;
    description: string;
    date: string;
}

export interface TransactionModel {
    id: number;
    userId: string;
    amount: number;
    type: 'INCOME' | 'EXPENSE';
    category: string;
    description: string;
    date: Date;
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

export type TransactionFormData = Omit<TransactionModel, 'id' | 'userId'>;