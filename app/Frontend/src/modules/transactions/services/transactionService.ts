import type { TransactionModel } from '../types/transaction.types';
import { transactionAdapter } from '../adapters/transaction.adapter';
import httpClient from '../../../core/api/httpClient';
import type { TransactionResponse } from '../types/transaction.types';

export const getTransactions = async (period?: string): Promise<TransactionModel> => {
    const endpoint = period ? `/transactions?period=${period}` : '/transactions';
    
    const response = await httpClient.get<TransactionResponse>(endpoint);
    
    return transactionAdapter(response.data);
};