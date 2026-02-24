import { Outlet } from 'react-router-dom';
import { ThemeToggle } from './components/ThemeToggle';

export const PublicLayout = () => {
    return (
        <div className="min-h-screen bg-white dark:bg-[#0B0F19] text-slate-900 dark:text-white transition-colors duration-300 relative">
            <header className="absolute top-6 right-6 z-50">
                <ThemeToggle />
            </header>
            <main className="w-full h-full">
                <Outlet />
            </main>
        </div>
    );
};
