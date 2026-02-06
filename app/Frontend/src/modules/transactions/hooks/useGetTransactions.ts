import { useQuery } from '@tanstack/react-query';
import { getTransactions } from '../services/transactionService';

export const useGetTransactions = (period?: string) => {
    return useQuery({
        queryKey: ['transactions', period],
        queryFn: () => getTransactions(period),
        staleTime: 1000 * 60 * 5,
    });
};