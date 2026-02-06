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

export interface TransactionModel {
    id: number;
    userId: string;
    period: string;
    totalIncome: number;
    totalExpense: number;
    balance: number;
    createdAt: Date;
    updatedAt: Date;
}