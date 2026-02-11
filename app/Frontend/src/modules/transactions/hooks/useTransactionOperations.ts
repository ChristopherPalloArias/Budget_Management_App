import { useState } from "react";
import { useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";
import {
  transactionBusinessLogic,
  ValidationError,
} from "@/modules/transactions/services/transactionBusinessLogic";
import type { TransactionFormData } from "@/modules/transactions/types/transaction.types";
import { useTransactionStore } from "@/modules/transactions/store/useTransactionStore";

interface TransactionOperationResult {
  success: boolean;
  error?: string;
}

export function useTransactionOperations() {
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const queryClient = useQueryClient();
  const addTransaction = useTransactionStore((state) => state.addTransaction);
  const updateTransaction = useTransactionStore(
    (state) => state.updateTransaction,
  );

  const createTransaction = async (
    formData: TransactionFormData,
    userId: string,
  ): Promise<TransactionOperationResult> => {
    setIsLoading(true);
    setError(null);

    try {
      const transaction = await transactionBusinessLogic.createTransaction(
        formData,
        userId,
      );
      addTransaction(transaction);

      // Invalidate queries to refresh the transaction list
      await queryClient.invalidateQueries({ queryKey: ["transactions"] });
      toast.success("Transacción creada con éxito");

      return { success: true };
    } catch (err) {
      const errorMessage =
        err instanceof ValidationError
          ? err.message
          : "Failed to create transaction";
      setError(errorMessage);
      toast.error(errorMessage);
      return { success: false, error: errorMessage };
    } finally {
      setIsLoading(false);
    }
  };

  const editTransaction = async (
    id: string,
    formData: TransactionFormData,
  ): Promise<TransactionOperationResult> => {
    setIsLoading(true);
    setError(null);

    try {
      const transaction = await transactionBusinessLogic.updateTransaction(
        id,
        formData,
      );
      updateTransaction(transaction);

      // Invalidate queries to refresh the transaction list
      await queryClient.invalidateQueries({ queryKey: ["transactions"] });
      toast.success("Transacción actualizada con éxito");

      return { success: true };
    } catch (err) {
      const errorMessage =
        err instanceof ValidationError
          ? err.message
          : "Failed to update transaction";
      setError(errorMessage);
      toast.error(errorMessage);
      return { success: false, error: errorMessage };
    } finally {
      setIsLoading(false);
    }
  };

  const clearError = () => setError(null);

  return {
    createTransaction,
    editTransaction,
    isLoading,
    error,
    clearError,
  };
}
