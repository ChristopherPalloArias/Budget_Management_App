import { useState, useCallback } from 'react';
import { useTransactions } from '../hooks/useTransactions';
import { useTransactionOperations } from '../hooks/useTransactionOperations';
import { useUserStore } from '@/modules/auth';
import type { TransactionFormData, TransactionFormInput, TransactionModel } from '../types/transaction.types';

interface TransactionPageState {
  isCreateDialogOpen: boolean;
  isEditDialogOpen: boolean;
  selectedTransaction: TransactionModel | null;
}

interface UseTransactionPageReturn {
  state: TransactionPageState;
  userId: string | null;
  transactions: ReturnType<typeof useTransactions>['transactions'];
  isLoading: ReturnType<typeof useTransactions>['isLoading'];
  fetchError: ReturnType<typeof useTransactions>['error'];
  isCreating: boolean;
  isEditing: boolean;
  operationError: string | null;
  openCreateDialog: () => void;
  closeCreateDialog: () => void;
  openEditDialog: (transaction: TransactionModel) => void;
  closeEditDialog: () => void;
  handleCreateTransaction: (data: TransactionFormInput) => Promise<boolean>;
  handleEditTransaction: (data: TransactionFormInput) => Promise<boolean>;
}

const transformFormInputToFormData = (input: TransactionFormInput): TransactionFormData => {
  return {
    ...input,
    date: new Date(input.date),
  };
};

export const useTransactionPage = (): UseTransactionPageReturn => {
  const [isCreateDialogOpen, setIsCreateDialogOpen] = useState(false);
  const [isEditDialogOpen, setIsEditDialogOpen] = useState(false);
  const [selectedTransaction, setSelectedTransaction] = useState<TransactionModel | null>(null);
  
  const { user } = useUserStore();
  const { transactions, isLoading, error: fetchError } = useTransactions();
  
  const {
    createTransaction: createTransactionOperation,
    editTransaction: editTransactionOperation,
    isLoading: isCreating,
    error: operationError,
  } = useTransactionOperations();

  const openCreateDialog = useCallback(() => {
    setIsCreateDialogOpen(true);
  }, []);

  const closeCreateDialog = useCallback(() => {
    setIsCreateDialogOpen(false);
  }, []);

  const openEditDialog = useCallback((transaction: TransactionModel) => {
    setSelectedTransaction(transaction);
    setIsEditDialogOpen(true);
  }, []);

  const closeEditDialog = useCallback(() => {
    setIsEditDialogOpen(false);
    setSelectedTransaction(null);
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

  const handleEditTransaction = useCallback(async (input: TransactionFormInput): Promise<boolean> => {
    if (!selectedTransaction) return false;

    const formData = transformFormInputToFormData(input);
    const result = await editTransactionOperation(String(selectedTransaction.id), formData);

    if (result.success) {
      setIsEditDialogOpen(false);
      setSelectedTransaction(null);
      return true;
    }
    return false;
  }, [selectedTransaction, editTransactionOperation]);

  return {
    state: {
      isCreateDialogOpen,
      isEditDialogOpen,
      selectedTransaction,
    },
    userId: user?.id ?? null,
    transactions,
    isLoading,
    fetchError,
    isCreating: isCreating && isCreateDialogOpen,
    isEditing: isCreating && isEditDialogOpen,
    operationError: operationError ?? null,
    openCreateDialog,
    closeCreateDialog,
    openEditDialog,
    closeEditDialog,
    handleCreateTransaction,
    handleEditTransaction,
  };
};
