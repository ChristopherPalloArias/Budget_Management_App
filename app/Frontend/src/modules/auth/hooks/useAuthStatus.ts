import { useUserStore } from '../store/useUserStore.ts';

export const useAuthStatus = () => {
    const isAuthenticated = useUserStore((state) => state.isAuthenticated);
    const isLoading = useUserStore((state) => state.isLoading);

    return {
        isAuthenticated,
        isChecking: isLoading,
    };
};