import { useState, useCallback } from 'react';
import { useTransactions } from '../hooks/useTransactions';
import { useTransactionOperations } from '../hooks/useTransactionOperations';
import { useUserStore } from '@/modules/auth';
import type { TransactionFormData } from '../types/transaction.types';

interface TransactionPageState {
  isCreateDialogOpen: boolean;
}

interface UseTransactionPageReturn {
  state: TransactionPageState;
  userId: string | null;
  transactions: ReturnType<typeof useTransactions>['transactions'];
  isLoading: ReturnType<typeof useTransactions>['isLoading'];
  fetchError: ReturnType<typeof useTransactions>['error'];
  isCreating: boolean;
  operationError: string | null;
  openCreateDialog: () => void;
  closeCreateDialog: () => void;
  createTransaction: (data: TransactionFormData) => Promise<boolean>;
}

export const useTransactionPage = (): UseTransactionPageReturn => {
  const [isCreateDialogOpen, setIsCreateDialogOpen] = useState(false);
  
  const { user } = useUserStore();
  const { transactions, isLoading, error: fetchError } = useTransactions();
  
  const {
    createTransaction: createTransactionOperation,
    isLoading: isCreating,
    error: operationError,
  } = useTransactionOperations();

  const openCreateDialog = useCallback(() => {
    setIsCreateDialogOpen(true);
  }, []);

  const closeCreateDialog = useCallback(() => {
    setIsCreateDialogOpen(false);
  }, []);

  const createTransaction = useCallback(async (data: TransactionFormData): Promise<boolean> => {
    if (!user) return false;
    
    const result = await createTransactionOperation(data, user.id);
    if (result.success) {
      setIsCreateDialogOpen(false);
      return true;
    }
    return false;
  }, [user, createTransactionOperation]);

  return {
    state: {
      isCreateDialogOpen,
    },
    userId: user?.id ?? null,
    transactions,
    isLoading,
    fetchError,
    isCreating,
    operationError: operationError ?? null,
    openCreateDialog,
    closeCreateDialog,
    createTransaction,
  };
};
