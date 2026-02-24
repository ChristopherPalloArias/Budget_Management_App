import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from "../../../components/ui/dialog";
import { Skeleton } from "../../../components/ui/skeleton";
import { Button } from "@/components/ui/button";
import { useTransactionPage } from "../hooks/useTransactionPage";
import { DataTable } from "../components/DataTable";
import { TransactionForm } from "../components/TransactionForm";

export function TransactionPage() {
  const {
    state,
    userId,
    transactions,
    isLoading,
    fetchError,
    isCreating,
    isEditing,
    operationError,
    openCreateDialog,
    closeCreateDialog,
    openEditDialog,
    closeEditDialog,
    handleCreateTransaction,
    handleEditTransaction,
  } = useTransactionPage();

  const editDefaultValues = state.selectedTransaction
    ? {
        description: state.selectedTransaction.description,
        amount: state.selectedTransaction.amount,
        category: state.selectedTransaction.category,
        type: state.selectedTransaction.type,
        date: new Date(state.selectedTransaction.date)
          .toISOString()
          .split("T")[0],
      }
    : undefined;

  if (!userId) return null;

  if (isLoading) {
    return <TransactionPageSkeleton />;
  }

  if (fetchError) {
    return <TransactionPageError message={fetchError?.message} />;
  }

  return (
    <div className="space-y-6">
      {operationError && (
        <div className="bg-destructive/15 text-destructive px-4 py-3 rounded-md">
          {operationError}
        </div>
      )}

      <DataTable
        data={transactions}
        onCreateTransaction={openCreateDialog}
        onEditTransaction={openEditDialog}
      />

      <Dialog open={state.isCreateDialogOpen} onOpenChange={closeCreateDialog}>
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

      <Dialog open={state.isEditDialogOpen} onOpenChange={closeEditDialog}>
        <DialogContent className="sm:max-w-[425px]">
          <DialogHeader>
            <DialogTitle>Editar Transacción</DialogTitle>
          </DialogHeader>
          {editDefaultValues && (
            <TransactionForm
              onSubmit={handleEditTransaction}
              isLoading={isEditing}
              defaultValues={editDefaultValues}
              submitLabel="Guardar cambios"
              loadingLabel="Guardando..."
            />
          )}
        </DialogContent>
      </Dialog>
    </div>
  );
}

function TransactionPageSkeleton() {
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

function TransactionPageError({ message }: { message?: string }) {
  return (
    <div className="flex flex-col items-center justify-center py-8 text-center">
      <p className="text-lg font-medium">Error al cargar transacciones</p>
      <p className="text-muted-foreground">
        {message || "Inténtalo de nuevo más tarde"}
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
