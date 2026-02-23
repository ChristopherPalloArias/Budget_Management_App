import { create } from 'zustand';
import { devtools, persist } from 'zustand/middleware';
import { authRepository } from '@/core/config/dependencies';
import type { IAuthUser } from '@/core/auth/interfaces';

interface UserState {
    user: IAuthUser | null;
    isAuthenticated: boolean;
    isLoading: boolean;

    setUser: (user: IAuthUser | null) => void;
    logout: () => void;
    initAuthListener: () => () => void;
}

export const useUserStore = create<UserState>()(
    devtools(
        persist(
            (set, get) => ({
                user: null,
                isAuthenticated: false,
                isLoading: true,

                setUser: (user) =>
                    set({
                        user,
                        isAuthenticated: !!user,
                        isLoading: false,
                    }),

                logout: async () => {
                    try {
                        await authRepository.signOut();
                        set({ user: null, isAuthenticated: false });
                    } catch (error) {
                        console.error('[Auth] Logout error:', error);
                    }
                },

                /**
                 * Inicializa la verificación de autenticación.
                 *
                 * Lee el token JWT de localStorage y, si existe,
                 * llama a GET /me para validar que siga vigente.
                 * Ejecuta el callback con el usuario o null.
                 */
                initAuthListener: () => {
                    const unsubscribe = authRepository.onAuthStateChanged((user) => {
                        get().setUser(user);
                    });
                    return unsubscribe;
                },
            }),
            {
                name: 'user-storage',
                partialize: (state) => ({
                    user: state.user ? {
                        id: state.user.id,
                        email: state.user.email,
                        displayName: state.user.displayName,
                        photoURL: state.user.photoURL,
                    } : null,
                }),
            }
        ),
        { name: 'User Store' }
    )
);

