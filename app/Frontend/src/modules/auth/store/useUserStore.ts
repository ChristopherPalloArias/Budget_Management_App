import { create } from 'zustand';
import { devtools, persist } from 'zustand/middleware';
import { authRepository } from '@/core/config/dependencies';
import { queryClient } from '@/core/config/queryClient';
import type { IAuthUser } from '@/core/auth/interfaces';
import { useTransactionStore } from '@/modules/transactions/store/useTransactionStore';
import { useReportStore } from '@/modules/reports/store/useReportStore';

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
                    } catch (error) {
                        console.error('[Auth] Logout backend error:', error);
                    } finally {
                        // 1. Limpiar estado de autenticación
                        set({ user: null, isAuthenticated: false, isLoading: false });

                        // 2. Resetear stores de datos de usuario (Zustand)
                        useTransactionStore.getState().reset();
                        useReportStore.getState().reset();

                        // 3. Purgar TODO el cache de React Query
                        //    Esto elimina queries en cache (transactions, reports, etc.)
                        //    para que el próximo usuario no vea datos del usuario anterior
                        queryClient.clear();

                        console.log('[Auth] Logout complete — all stores and query cache cleared');
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
