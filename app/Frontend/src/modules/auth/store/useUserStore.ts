import { create } from 'zustand';
import { devtools, persist } from 'zustand/middleware';
import { type User as FirebaseUser, onAuthStateChanged } from 'firebase/auth';
import { auth } from '../../../core/config/firebase.config.js';

interface UserState {
    user: FirebaseUser | null;
    isAuthenticated: boolean;
    isLoading: boolean;

    setUser: (user: FirebaseUser | null) => void;
    logout: () => void;

    initAuthListener: () => void;
}

/**
 * User Store
 * Manages authentication state and listens to Firebase Auth changes
 */
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
                        await auth.signOut();
                        set({ user: null, isAuthenticated: false });
                    } catch (error) {
                        console.error('[Auth] Logout error:', error);
                    }
                },

                initAuthListener: () => {
                    onAuthStateChanged(auth, (user) => {
                        get().setUser(user);
                    });
                },
            }),
            {
                name: 'user-storage',
                partialize: (state) => ({
                    user: state.user ? {
                        uid: state.user.uid,
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

useUserStore.getState().initAuthListener();
