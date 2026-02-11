import type { IAuthUser } from './IAuthUser';

export type { IAuthUser };

export const AuthProvider = {
  GOOGLE: 'GOOGLE',
  EMAIL: 'EMAIL',
} as const;

export type AuthProvider = typeof AuthProvider[keyof typeof AuthProvider];

export interface IAuthCredentials {
  email: string;
  password: string;
}

export interface IRegisterCredentials extends IAuthCredentials {
  displayName: string;
}

export type Unsubscribe = () => void;

export interface IAuthRepository {
  signIn(credentials: IAuthCredentials): Promise<IAuthUser>;
  signInWithProvider(provider: AuthProvider): Promise<IAuthUser>;
  signOut(): Promise<void>;
  register(credentials: IRegisterCredentials): Promise<IAuthUser>;
  onAuthStateChanged(callback: (user: IAuthUser | null) => void): Unsubscribe;
}