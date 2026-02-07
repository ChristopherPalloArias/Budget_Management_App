import type { 
    TransactionResponse, 
    TransactionItemResponse, 
    TransactionModel, 
    TransactionReportModel 
} from '../types/transaction.types';

export const transactionAdapter = (response: TransactionItemResponse): TransactionModel => {
    return {
        id: response.transactionId,
        userId: response.userId || '',
        amount: response.amount || 0,
        type: response.type || 'EXPENSE',
        category: response.category || '',
        description: response.description || '',
        date: response.date ? new Date(response.date) : new Date(),
    };
};

export const createTransactionAdapter = (data: TransactionModel): any => {
    return {
        transactionId: data.id, 
        userId: data.userId,
        amount: data.amount,
        type: data.type,
        category: data.category,
        description: data.description,
        date: data.date.toISOString().split('T')[0],
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