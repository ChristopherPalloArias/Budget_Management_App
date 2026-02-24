import { useState, useCallback } from 'react';
import { useTransactions } from '../hooks/useTransactions';
import { useTransactionOperations } from '../hooks/useTransactionOperations';
import { useDeleteTransaction } from '../hooks/useDeleteTransaction';
import { useUserStore } from '@/modules/auth';
import type { TransactionModel, TransactionFormData, TransactionFormInput } from '../types/transaction.types';

interface TransactionPageState {
  isCreateDialogOpen: boolean;
  isEditDialogOpen: boolean;
  transactionToEdit: TransactionModel | null;
  transactionToDelete: TransactionModel | null;
}

interface UseTransactionPageReturn {
  state: TransactionPageState;
  userId: string | null;
  transactions: ReturnType<typeof useTransactions>['transactions'];
  isLoading: ReturnType<typeof useTransactions>['isLoading'];
  fetchError: ReturnType<typeof useTransactions>['error'];
  isCreating: boolean;
  isEditing: boolean;
  isDeletingTransaction: boolean;
  operationError: string | null;
  openCreateDialog: () => void;
  closeCreateDialog: () => void;
  openEditDialog: (transaction: TransactionModel) => void;
  closeEditDialog: () => void;
  openDeleteDialog: (transaction: TransactionModel) => void;
  closeDeleteDialog: () => void;
  handleCreateTransaction: (data: TransactionFormInput) => Promise<boolean>;
  handleEditTransaction: (data: TransactionFormInput) => Promise<boolean>;
  handleConfirmDelete: () => void;
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
  const [transactionToEdit, setTransactionToEdit] = useState<TransactionModel | null>(null);
  const [transactionToDelete, setTransactionToDelete] = useState<TransactionModel | null>(null);

  const { user } = useUserStore();
  const { transactions, isLoading, error: fetchError } = useTransactions();

  const {
    createTransaction: createTransactionOperation,
    editTransaction: editTransactionOperation,
    isLoading: isOperating,
    error: operationError,
  } = useTransactionOperations();

  const {
    mutate: deleteTransactionMutate,
    isPending: isDeletingTransaction,
  } = useDeleteTransaction();

  // --- Create ---
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

  // --- Edit ---
  const openEditDialog = useCallback((transaction: TransactionModel) => {
    setTransactionToEdit(transaction);
    setIsEditDialogOpen(true);
  }, []);

  const closeEditDialog = useCallback(() => {
    setIsEditDialogOpen(false);
    setTransactionToEdit(null);
  }, []);

  const handleEditTransaction = useCallback(async (input: TransactionFormInput): Promise<boolean> => {
    if (!transactionToEdit) return false;

    const formData = transformFormInputToFormData(input);
    const result = await editTransactionOperation(
      String(transactionToEdit.id),
      formData,
    );

    if (result.success) {
      setIsEditDialogOpen(false);
      setTransactionToEdit(null);
      return true;
    }
    return false;
  }, [transactionToEdit, editTransactionOperation]);

  // --- Delete ---
  const openDeleteDialog = useCallback((transaction: TransactionModel) => {
    setTransactionToDelete(transaction);
  }, []);

  const closeDeleteDialog = useCallback(() => {
    setTransactionToDelete(null);
  }, []);

  const handleConfirmDelete = useCallback(() => {
    if (!transactionToDelete) return;

    deleteTransactionMutate(transactionToDelete.id, {
      onSuccess: () => setTransactionToDelete(null),
    });
  }, [transactionToDelete, deleteTransactionMutate]);

  return {
    state: {
      isCreateDialogOpen,
      isEditDialogOpen,
      transactionToEdit,
      transactionToDelete,
    },
    userId: user?.id ?? null,
    transactions,
    isLoading,
    fetchError,
    isCreating: isOperating && isCreateDialogOpen,
    isEditing: isOperating && isEditDialogOpen,
    isDeletingTransaction,
    operationError: operationError ?? null,
    openCreateDialog,
    closeCreateDialog,
    openEditDialog,
    closeEditDialog,
    openDeleteDialog,
    closeDeleteDialog,
    handleCreateTransaction,
    handleEditTransaction,
    handleConfirmDelete,
  };
};
