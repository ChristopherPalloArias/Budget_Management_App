import { useAuthInitialization } from '../hooks/useAuthInitialization';

interface AuthProviderProps {
  children: React.ReactNode;
}

export const AuthProvider = ({ children }: AuthProviderProps) => {
  useAuthInitialization();

  return <>{children}</>;
};
