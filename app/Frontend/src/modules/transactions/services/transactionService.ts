import type { TransactionModel, TransactionReportModel } from '../types/transaction.types';
import { transactionAdapter, transactionReportAdapter } from '../adapters/transaction.adapter';
import httpClient from '../../../core/api/httpClient';
import type { TransactionResponse, TransactionItemResponse } from '../types/transaction.types';

export const getTransactions = async (period?: string): Promise<TransactionReportModel> => {
    const endpoint = period ? `/transactions?period=${period}` : '/transactions';
    const response = await httpClient.get<TransactionResponse>(endpoint);
    return transactionReportAdapter(response.data);
};

export const getTransactionItems = async (period?: string): Promise<TransactionModel[]> => {
    const endpoint = period ? `/transactions/items?period=${period}` : '/transactions/items';
    const response = await httpClient.get<TransactionItemResponse[]>(endpoint);
    return response.data.map(transactionAdapter);
};

export const createTransaction = async (data: Omit<TransactionModel, 'id' | 'userId' | 'createdAt' | 'updatedAt'>): Promise<TransactionModel> => {
    const response = await httpClient.post<TransactionItemResponse>('/transactions', data);
    return transactionAdapter(response.data);
};

export const updateTransaction = async (id: number, data: Omit<TransactionModel, 'id' | 'userId' | 'createdAt' | 'updatedAt'>): Promise<TransactionModel> => {
    const response = await httpClient.put<TransactionItemResponse>(`/transactions/${id}`, data);
    return transactionAdapter(response.data);
};

export const deleteTransaction = async (id: number): Promise<void> => {
    await httpClient.delete(`/transactions/${id}`);
};