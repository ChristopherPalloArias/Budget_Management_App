import { useState } from "react";

import { Button } from "../../../components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from "../../../components/ui/dialog";
import { Skeleton } from "../../../components/ui/skeleton";
import { useTransactions } from "../hooks/useTransactions";
import { useTransactionOperations } from "../hooks/useTransactionOperations";
import { DataTable } from "../components/DataTable";
import { TransactionForm } from "../components/TransactionForm";
import { useUserStore } from "@/modules/auth";
import type { TransactionFormData } from "../types/transaction.types";

export function TransactionPage() {
  const [isCreateDialogOpen, setIsCreateDialogOpen] = useState(false);

  const { user } = useUserStore();
  const { transactions, isLoading, error: fetchError } = useTransactions();

  const {
    createTransaction: createTransactionOperation,
    isLoading: isCreating,
    error: operationError,
  } = useTransactionOperations();

  if (!user) return null;

  const handleCreateTransaction = async (data: {
    description: string;
    amount: number;
    category: string;
    type: "INCOME" | "EXPENSE";
    date: string;
  }) => {
    const formData: TransactionFormData = {
      ...data,
      date: new Date(data.date),
    };

    const result = await createTransactionOperation(formData, user.id);
    if (result.success) {
      setIsCreateDialogOpen(false);
    }
  };

  if (isLoading) {
    return (
      <div className="space-y-4">
        <div className="flex items-center justify-between">
          <div>
            <Skeleton className="h-8 w-48" />
            <Skeleton className="mt-2 h-4 w-64" />
          </div>
          <Skeleton className="h-10 w-40" />
        </div>
        <div className="space-y-2">
          {[...Array(5)].map((_, i) => (
            <div key={i} className="flex items-center space-x-4">
              <Skeleton className="h-12 w-24" />
              <Skeleton className="h-12 flex-1" />
              <Skeleton className="h-12 w-24" />
              <Skeleton className="h-12 w-32" />
              <Skeleton className="h-12 w-20" />
            </div>
          ))}
        </div>
      </div>
    );
  }

  if (fetchError) {
    return (
      <div className="flex flex-col items-center justify-center py-8 text-center">
        <p className="text-lg font-medium">Error al cargar transacciones</p>
        <p className="text-muted-foreground">
          {(fetchError as Error).message || "Inténtalo de nuevo más tarde"}
        </p>
        <Button
          variant="outline"
          onClick={() => window.location.reload()}
          className="mt-4"
        >
          Reintentar
        </Button>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {operationError && (
        <div className="bg-destructive/15 text-destructive px-4 py-3 rounded-md">
          {operationError}
        </div>
      )}

      {/* Tabla con paginación y filtrado */}
      <DataTable
        data={transactions}
        onCreateTransaction={() => setIsCreateDialogOpen(true)}
      />

      {/* Modal para crear transacción */}
      <Dialog open={isCreateDialogOpen} onOpenChange={setIsCreateDialogOpen}>
        <DialogContent className="sm:max-w-[425px]">
          <DialogHeader>
            <DialogTitle>Nueva Transacción</DialogTitle>
          </DialogHeader>
          <TransactionForm
            onSubmit={handleCreateTransaction}
            isLoading={isCreating}
          />
        </DialogContent>
      </Dialog>
    </div>
  );
}
