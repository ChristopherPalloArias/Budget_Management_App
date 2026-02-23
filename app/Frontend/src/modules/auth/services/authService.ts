import { authRepository } from '@/core/config/dependencies';
import type { IAuthUser } from '@/core/auth/interfaces/IAuthRepository';
import { useUserStore } from '../store/useUserStore';

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
        useUserStore.getState().setUser(user);
        return user;
    } catch (error: any) {
        console.error('[Auth Service] Login error:', error);
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
        // El usuario debe iniciar sesión manualmente para obtener el token
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
        useUserStore.getState().setUser(null);
        console.log('[Auth Service] Logout successful');
    } catch (error: any) {
        console.error('[Auth Service] Logout error:', error);
        throw new Error('Error al cerrar sesión');
    }
};
