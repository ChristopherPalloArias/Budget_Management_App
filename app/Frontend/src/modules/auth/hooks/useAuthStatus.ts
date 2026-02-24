import { useUserStore } from '../store/useUserStore';

export const useAuthStatus = () => {
    const isAuthenticated = useUserStore((state) => state.isAuthenticated);
    const isLoading = useUserStore((state) => state.isLoading);

    return {
        isAuthenticated,
        isChecking: isLoading,
    };
};