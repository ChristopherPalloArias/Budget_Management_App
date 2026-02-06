import type { TransactionResponse, TransactionModel } from '../types/transaction.types';

export const transactionAdapter = (response: TransactionResponse): TransactionModel => {
    return {
        id: response.reportId,
        userId: response.userId || '',
        period: response.period || '',
        totalIncome: response.totalIncome || 0,
        totalExpense: response.totalExpense || 0,
        balance: response.balance || 0,
        createdAt: response.createdAt ? new Date(response.createdAt) : new Date(),
        updatedAt: response.updatedAt ? new Date(response.updatedAt) : new Date(),
    };
};