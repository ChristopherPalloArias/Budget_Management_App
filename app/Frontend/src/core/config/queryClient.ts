import { QueryClient, type DefaultOptions } from '@tanstack/react-query';


const queryConfig: DefaultOptions = {
    queries: {
        staleTime: 1000 * 30,
        gcTime: 1000 * 60 * 5,
        retry: 2,
        retryDelay: (attemptIndex) => Math.min(1000 * 2 ** attemptIndex, 30000),
        refetchOnWindowFocus: false,
        refetchOnReconnect: true,
        refetchOnMount: false,
    },
    mutations: {
        retry: 1,
    },
};

export const queryClient = new QueryClient({
    defaultOptions: queryConfig,
});
