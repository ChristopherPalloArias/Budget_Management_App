import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query"
import { getTransactionItems, createTransaction } from "../services/transactionService"
import type { TransactionFormData } from "../types/transaction.types"
import { toast } from "sonner"

export function useTransactions(period?: string) {
  const queryClient = useQueryClient()

  const transactionsQuery = useQuery({
    queryKey: ["transactions", period],
    queryFn: () => getTransactionItems(period),
    staleTime: 1000 * 60 * 5,
    retry: 2,
  })

  const createTransactionMutation = useMutation({
    mutationFn: (data: TransactionFormData) => createTransaction(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["transactions"] })
      toast.success("Transacción creada con éxito")
    },
    onError: (error: any) => {
      toast.error(`Error al crear transacción: ${error.message}`)
    },
  })

  return {
    transactions: transactionsQuery.data || [],
    isLoading: transactionsQuery.isLoading,
    error: transactionsQuery.error,
    createTransaction: createTransactionMutation.mutate,
    isCreating: createTransactionMutation.isPending,
  }
}