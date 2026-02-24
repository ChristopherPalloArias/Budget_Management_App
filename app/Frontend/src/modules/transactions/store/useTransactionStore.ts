import { create, type StateCreator } from "zustand";
import { devtools } from "zustand/middleware";

interface TransactionState {
  selectedPeriod: string;

  setSelectedPeriod: (period: string) => void;
  reset: () => void;
}

const getInitialState = () => ({
  selectedPeriod: new Date().toISOString().slice(0, 7),
});

const storeCreator: StateCreator<TransactionState, [["zustand/devtools", never]]> = (set) => ({
  ...getInitialState(),

  setSelectedPeriod: (period) =>
    set({ selectedPeriod: period }, false, "setSelectedPeriod"),

  /**
   * Resetea el store completo a su estado inicial.
   * Debe invocarse durante el logout.
   */
  reset: () =>
    set(getInitialState(), false, "reset"),
});

export const useTransactionStore = create<TransactionState>()(
  devtools(storeCreator, { name: "Transaction Store" })
);
