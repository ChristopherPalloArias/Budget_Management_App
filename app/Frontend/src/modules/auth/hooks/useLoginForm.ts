import { useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { loginWithEmail, loginWithGoogle } from '../services/authService';
import { formatAuthError } from '@/shared/utils/errorHandler';
import type { LoginFormData } from '../schemas/loginSchema';

interface LoginFormState {
  isLoading: boolean;
  isGoogleLoading: boolean;
  error: string | null;
}

interface UseLoginFormReturn {
  state: LoginFormState;
  login: (data: LoginFormData) => Promise<void>;
  loginWithGoogleProvider: () => Promise<void>;
  clearError: () => void;
}

const executeAuthAction = async <T>(
  action: () => Promise<T>,
  setError: (error: string | null) => void,
  onSuccess?: () => void
): Promise<void> => {
  setError(null);
  try {
    await action();
    onSuccess?.();
  } catch (err) {
    setError(formatAuthError(err));
  }
};

export const useLoginForm = (): UseLoginFormReturn => {
  const navigate = useNavigate();
  const [isLoading, setIsLoading] = useState(false);
  const [isGoogleLoading, setIsGoogleLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const clearError = useCallback(() => {
    setError(null);
  }, []);

  const login = useCallback(async (data: LoginFormData): Promise<void> => {
    setIsLoading(true);
    await executeAuthAction(
      () => loginWithEmail(data.email, data.password),
      setError,
      () => navigate('/dashboard')
    );
    setIsLoading(false);
  }, [navigate]);

  const loginWithGoogleProvider = useCallback(async (): Promise<void> => {
    setIsGoogleLoading(true);
    await executeAuthAction(
      loginWithGoogle,
      setError,
      () => navigate('/dashboard')
    );
    setIsGoogleLoading(false);
  }, [navigate]);

  return {
    state: {
      isLoading,
      isGoogleLoading,
      error,
    },
    login,
    loginWithGoogleProvider,
    clearError,
  };
};
