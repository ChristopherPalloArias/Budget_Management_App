import { create } from 'zustand';
import { devtools } from 'zustand/middleware';
import type { TransactionModel } from '../types/transaction.types';

interface TransactionState {
    currentTransaction: TransactionModel | null;
    selectedPeriod: string;

    setCurrentTransaction: (transaction: TransactionModel | null) => void;
    setSelectedPeriod: (period: string) => void;
    clearTransactionData: () => void;
}

export const useTransactionStore = create<TransactionState>()(
    devtools(
        (set) => ({
            currentTransaction: null,
            selectedPeriod: new Date().toISOString().slice(0, 7),

            setCurrentTransaction: (transaction) =>
                set({ currentTransaction: transaction }, false, 'setCurrentTransaction'),

            setSelectedPeriod: (period) =>
                set({ selectedPeriod: period }, false, 'setSelectedPeriod'),

            clearTransactionData: () =>
                set(
                    { currentTransaction: null, selectedPeriod: new Date().toISOString().slice(0, 7) },
                    false,
                    'clearTransactionData'
                ),
        }),
        { name: 'Transaction Store' }
    )
);