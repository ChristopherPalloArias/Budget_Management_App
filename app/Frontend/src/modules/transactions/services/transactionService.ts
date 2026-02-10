import type { TransactionModel, TransactionFormData } from '../types/transaction.types';
import { transactionAdapter } from '../adapters/transaction.adapter';
import HttpClient from '../../../core/api/HttpClient';
import type { TransactionItemResponse } from '../types/transaction.types';

// Obtener instancia específica para el microservicio de transactions
const transactionsHttpClient = HttpClient.getInstance('transactions');

export const getTransactionsByUser = async (userId: string, period?: string): Promise<TransactionModel[]> => {
    const endpoint = period ? `/v1/transactions?period=${period}` : `/v1/transactions?userId=${userId}`;
    const response = await transactionsHttpClient.get<TransactionItemResponse[]>(endpoint);
    return response.data.map(transactionAdapter);
};

export const createTransaction = async (data: TransactionFormData): Promise<TransactionModel> => {
    const response = await transactionsHttpClient.post<TransactionItemResponse>('/v1/transactions', data);
    return transactionAdapter(response.data);
};