import { useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'sonner';
import { loginWithEmail } from '../services/authService';
import { formatAuthError } from '@/shared/utils/errorHandler';
import type { LoginFormData } from '../schemas/loginSchema';

interface LoginFormState {
  isLoading: boolean;
  error: string | null;
}

interface UseLoginFormReturn {
  state: LoginFormState;
  login: (data: LoginFormData) => Promise<void>;
  clearError: () => void;
}

export const useLoginForm = (): UseLoginFormReturn => {
  const navigate = useNavigate();
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const clearError = useCallback(() => {
    setError(null);
  }, []);

  const login = useCallback(async (data: LoginFormData): Promise<void> => {
    setIsLoading(true);
    setError(null);

    try {
      await loginWithEmail(data.email, data.password);
      toast.success('¡Bienvenido de nuevo!');
      // La redirección ocurrirá automáticamente por el AppRouter al detectar isAuthenticated
      // o forzamos por si acaso:
      navigate('/dashboard');
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
    login,
    clearError,
  };
};
