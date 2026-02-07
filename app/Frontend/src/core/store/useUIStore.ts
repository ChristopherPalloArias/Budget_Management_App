import { create } from 'zustand';
import { devtools } from 'zustand/middleware';

/**
 * UI State Interface
 * Global UI states like sidebar, modals, theme, etc.
 */
interface UIState {
    // Sidebar state
    isSidebarOpen: boolean;
    toggleSidebar: () => void;
    setSidebarOpen: (isOpen: boolean) => void;

    // Modal state
    isModalOpen: boolean;
    modalContent: React.ReactNode | null;
    openModal: (content: React.ReactNode) => void;
    closeModal: () => void;

    // Theme
    theme: 'light' | 'dark' | 'system';
    setTheme: (theme: 'light' | 'dark' | 'system') => void;

    // Loading states
    isLoading: boolean;
    setIsLoading: (loading: boolean) => void;
}

/**
 * UI Store
 * Manages global UI state across the application
 */
export const useUIStore = create<UIState>()(
    devtools(
        (set) => ({
            // Sidebar
            isSidebarOpen: true,
            toggleSidebar: () => set((state) => ({ isSidebarOpen: !state.isSidebarOpen })),
            setSidebarOpen: (isOpen) => set({ isSidebarOpen: isOpen }),

            // Modal
            isModalOpen: false,
            modalContent: null,
            openModal: (content) => set({ isModalOpen: true, modalContent: content }),
            closeModal: () => set({ isModalOpen: false, modalContent: null }),

            // Theme
            theme: 'system',
            setTheme: (theme) => set({ theme }),

            // Loading
            isLoading: false,
            setIsLoading: (loading) => set({ isLoading: loading }),
        }),
        { name: 'UI Store' }
    )
);
