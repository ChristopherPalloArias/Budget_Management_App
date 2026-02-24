import { createContext, useContext, useState, type ReactNode } from "react";
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { DeleteTransactionDialog } from "./DeleteTransactionDialog";
import { TransactionForm } from "./TransactionForm";
import { useTransactionOperations } from "../hooks/useTransactionOperations";
import { useDeleteTransaction } from "../hooks/useDeleteTransaction";
import { useUserStore } from "@/modules/auth";
import type { TransactionModel, TransactionFormData, TransactionFormInput } from "../types/transaction.types";

interface TransactionModalsContextType {
    openCreateModal: () => void;
    openEditModal: (transaction: TransactionModel) => void;
    openDeleteModal: (transaction: TransactionModel) => void;
}

const TransactionModalsContext = createContext<TransactionModalsContextType | null>(null);

export function useTransactionModals() {
    const context = useContext(TransactionModalsContext);
    if (!context) {
        throw new Error("useTransactionModals must be used within a TransactionModalsProvider");
    }
    return context;
}

const transformFormInputToFormData = (input: TransactionFormInput): TransactionFormData => {
    return {
        ...input,
        date: new Date(input.date),
    };
};

export function TransactionModalsProvider({ children }: { children: ReactNode }) {
    // --- States ---
    const [isCreateOpen, setIsCreateOpen] = useState(false);
    const [isEditOpen, setIsEditOpen] = useState(false);
    const [transactionToEdit, setTransactionToEdit] = useState<TransactionModel | null>(null);
    const [transactionToDelete, setTransactionToDelete] = useState<TransactionModel | null>(null);

    // --- External Hooks ---
    const { user } = useUserStore();
    const {
        createTransaction,
        editTransaction,
        isLoading: isOperating,
    } = useTransactionOperations();

    const {
        mutate: deleteTransactionMutate,
        isPending: isDeleting,
    } = useDeleteTransaction();

    // --- Handlers ---
    const handleCreateSubmit = async (input: TransactionFormInput) => {
        if (!user) return false;
        const formData = transformFormInputToFormData(input);
        const result = await createTransaction(formData, user.id);
        if (result.success) setIsCreateOpen(false);
        return result.success;
    };

    const handleEditSubmit = async (input: TransactionFormInput) => {
        if (!transactionToEdit) return false;
        const formData = transformFormInputToFormData(input);
        const result = await editTransaction(String(transactionToEdit.id), formData);
        if (result.success) {
            setIsEditOpen(false);
            setTransactionToEdit(null);
        }
        return result.success;
    };

    const handleConfirmDelete = () => {
        if (!transactionToDelete) return;
        deleteTransactionMutate(transactionToDelete.id, {
            onSuccess: () => setTransactionToDelete(null),
        });
    };

    return (
        <TransactionModalsContext.Provider
            value={{
                openCreateModal: () => setIsCreateOpen(true),
                openEditModal: (tx) => {
                    setTransactionToEdit(tx);
                    setIsEditOpen(true);
                },
                openDeleteModal: (tx) => setTransactionToDelete(tx),
            }}
        >
            {children}

            {/* CREATE MODAL */}
            <Dialog open={isCreateOpen} onOpenChange={setIsCreateOpen}>
                <DialogContent className="sm:max-w-[425px]">
                    <DialogHeader>
                        <DialogTitle>Nueva Transacción</DialogTitle>
                    </DialogHeader>
                    <TransactionForm onSubmit={handleCreateSubmit} isLoading={isOperating && isCreateOpen} />
                </DialogContent>
            </Dialog>

            {/* EDIT MODAL */}
            <Dialog open={isEditOpen} onOpenChange={(open) => { setIsEditOpen(open); if (!open) setTransactionToEdit(null); }}>
                <DialogContent className="sm:max-w-[425px]">
                    <DialogHeader>
                        <DialogTitle>Editar Transacción</DialogTitle>
                    </DialogHeader>
                    {transactionToEdit && (
                        <TransactionForm
                            onSubmit={handleEditSubmit}
                            isLoading={isOperating && isEditOpen}
                            isEditing
                            defaultValues={{
                                description: transactionToEdit.description,
                                amount: transactionToEdit.amount,
                                category: transactionToEdit.category,
                                type: transactionToEdit.type,
                                date: transactionToEdit.date instanceof Date
                                    ? transactionToEdit.date.toISOString().split("T")[0]
                                    : String(transactionToEdit.date).split("T")[0],
                            }}
                        />
                    )}
                </DialogContent>
            </Dialog>

            {/* DELETE MODAL */}
            <DeleteTransactionDialog
                isOpen={!!transactionToDelete}
                onOpenChange={(open) => !open && setTransactionToDelete(null)}
                onConfirm={handleConfirmDelete}
                isPending={isDeleting}
                transactionDescription={transactionToDelete?.description || ""}
            />
        </TransactionModalsContext.Provider>
    );
}
