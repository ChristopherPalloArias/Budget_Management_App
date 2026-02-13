import { useState, useCallback } from 'react';
import { useTransactions } from '../hooks/useTransactions';
import { useTransactionOperations } from '../hooks/useTransactionOperations';
import { useUserStore } from '@/modules/auth';
import type { TransactionFormData, TransactionFormInput } from '../types/transaction.types';

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
  handleCreateTransaction: (data: TransactionFormInput) => Promise<boolean>;
}

const transformFormInputToFormData = (input: TransactionFormInput): TransactionFormData => {
  return {
    ...input,
    date: new Date(input.date),
  };
};

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

  const handleCreateTransaction = useCallback(async (input: TransactionFormInput): Promise<boolean> => {
    if (!user) return false;
    
    const formData = transformFormInputToFormData(input);
    const result = await createTransactionOperation(formData, user.id);
    
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
    handleCreateTransaction,
  };
};
