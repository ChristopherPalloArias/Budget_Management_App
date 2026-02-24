import { create } from "zustand";
import { devtools } from "zustand/middleware";
import type { TransactionModel } from "../types/transaction.types";

interface TransactionState {
  currentTransaction: TransactionModel | null;
  selectedPeriod: string;

  setCurrentTransaction: (transaction: TransactionModel | null) => void;
  setSelectedPeriod: (period: string) => void;
  clearTransactionData: () => void;
  addTransaction: (transaction: TransactionModel) => void;
  updateTransaction: (transaction: TransactionModel) => void;
  reset: () => void;
}

const getInitialState = () => ({
  currentTransaction: null as TransactionModel | null,
  selectedPeriod: new Date().toISOString().slice(0, 7),
});

export const useTransactionStore = create<TransactionState>()(
  devtools(
    (set) => ({
      ...getInitialState(),

      setCurrentTransaction: (transaction) =>
        set(
          { currentTransaction: transaction },
          false,
          "setCurrentTransaction",
        ),

      setSelectedPeriod: (period) =>
        set({ selectedPeriod: period }, false, "setSelectedPeriod"),

      clearTransactionData: () =>
        set(
          getInitialState(),
          false,
          "clearTransactionData",
        ),

      addTransaction: (transaction) =>
        set({ currentTransaction: transaction }, false, "addTransaction"),

      updateTransaction: (transaction) =>
        set({ currentTransaction: transaction }, false, "updateTransaction"),

      /**
       * Resetea el store completo a su estado inicial.
       * Debe invocarse durante el logout para evitar phantom data.
       */
      reset: () =>
        set(getInitialState(), false, "reset"),
    }),
    { name: "Transaction Store" },
  ),
);
