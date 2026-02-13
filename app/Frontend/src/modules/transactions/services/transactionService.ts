import type {
  TransactionModel,
  TransactionFormData,
} from "../types/transaction.types";
import { transactionAdapter } from "../adapters/transaction.adapter";
import HttpClient from "../../../core/api/HttpClient";
import { API_ENDPOINTS } from "@/core/constants/app.constants";
import type { TransactionItemResponse } from "../types/transaction.types";

const transactionsHttpClient = HttpClient.getInstance("transactions");

export const getTransactionsByUser = async (
  userId: string,
  period?: string,
): Promise<TransactionModel[]> => {
  const endpoint = period
    ? `${API_ENDPOINTS.TRANSACTIONS}?period=${period}`
    : `${API_ENDPOINTS.TRANSACTIONS}?userId=${userId}`;
  const response =
    await transactionsHttpClient.get<TransactionItemResponse[]>(endpoint);
  return response.data.map(transactionAdapter);
};

export const createTransaction = async (
  data: TransactionFormData,
  userId: string,
): Promise<TransactionModel> => {
  const response = await transactionsHttpClient.post<TransactionItemResponse>(
    API_ENDPOINTS.TRANSACTIONS,
    { ...data, userId },
  );
  return transactionAdapter(response.data);
};

export const updateTransaction = async (
  id: string,
  data: TransactionFormData,
): Promise<TransactionModel> => {
  const response = await transactionsHttpClient.put<TransactionItemResponse>(
    `${API_ENDPOINTS.TRANSACTIONS}/${id}`,
    data,
  );
  return transactionAdapter(response.data);
};
