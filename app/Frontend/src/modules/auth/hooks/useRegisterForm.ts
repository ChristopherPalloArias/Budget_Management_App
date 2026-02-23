import { useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'sonner';
import { registerWithEmail } from '../services/authService';
import { formatAuthError } from '@/shared/utils/errorHandler';
import type { RegisterFormData } from '../schemas/registerSchema';

interface RegisterFormState {
    isLoading: boolean;
    error: string | null;
}

interface UseRegisterFormReturn {
    state: RegisterFormState;
    registerUser: (data: RegisterFormData) => Promise<void>;
    clearError: () => void;
}

export const useRegisterForm = (): UseRegisterFormReturn => {
    const navigate = useNavigate();
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const clearError = useCallback(() => {
        setError(null);
    }, []);

    const registerUser = useCallback(async (data: RegisterFormData): Promise<void> => {
        setIsLoading(true);
        setError(null);

        try {
            await registerWithEmail(data.displayName, data.email, data.password);
            toast.success('¡Cuenta creada con éxito! Por favor, inicia sesión.');
            navigate('/login');
        } catch (err) {
            const message = formatAuthError(err);
            setError(message);
            toast.error(message);
        } finally {
            setIsLoading(false);
        }
    }, [navigate]);

    return {
        state: {
            isLoading,
            error,
        },
        registerUser,
        clearError,
    };
};
