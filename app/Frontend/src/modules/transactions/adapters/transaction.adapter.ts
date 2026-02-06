import type { 
    TransactionResponse, 
    TransactionItemResponse, 
    TransactionModel, 
    TransactionReportModel 
} from '../types/transaction.types';

export const transactionAdapter = (response: TransactionItemResponse): TransactionModel => {
    return {
        id: response.id,
        userId: response.userId || '',
        amount: response.amount || 0,
        type: response.type || 'expense',
        category: response.category || '',
        description: response.description || '',
        date: response.date ? new Date(response.date) : new Date(),
        createdAt: response.createdAt ? new Date(response.createdAt) : new Date(),
        updatedAt: response.updatedAt ? new Date(response.updatedAt) : new Date(),
    };
};

export const transactionReportAdapter = (response: TransactionResponse): TransactionReportModel => {
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