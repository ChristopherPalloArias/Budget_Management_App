import { authRepository } from '@/core/config/dependencies';
import type { IAuthUser } from '@/core/auth/interfaces/IAuthRepository';

/**
 * Login with Email and Password
 */
export const loginWithEmail = async (
    email: string,
    password: string
): Promise<IAuthUser> => {
    try {
        const user = await authRepository.signIn({ email, password });
        console.log('[Auth Service] Login successful:', user.email);
        return user;
    } catch (error: any) {
        console.error('[Auth Service] Login error:', error);
        throw error;
    }
};

/**
 * Login with Google
 */
export const loginWithGoogle = async (): Promise<IAuthUser> => {
    try {
        const user = await authRepository.signInWithProvider('GOOGLE');
        console.log('[Auth Service] Google login successful:', user.email);
        return user;
    } catch (error: any) {
        console.error('[Auth Service] Google login error:', error);
        throw error;
    }
};

/**
 * Register with Email and Password
 */
export const registerWithEmail = async (
    displayName: string,
    email: string,
    password: string
): Promise<IAuthUser> => {
    try {
        const user = await authRepository.register({
            displayName,
            email,
            password,
        });
        console.log('[Auth Service] Registration successful:', user.email);
        return user;
    } catch (error: any) {
        console.error('[Auth Service] Registration error:', error);
        throw error;
    }
};

/**
 * Logout
 */
export const logout = async (): Promise<void> => {
    try {
        await authRepository.signOut();
        console.log('[Auth Service] Logout successful');
    } catch (error: any) {
        console.error('[Auth Service] Logout error:', error);
        throw new Error('Error al cerrar sesión');
    }
};
