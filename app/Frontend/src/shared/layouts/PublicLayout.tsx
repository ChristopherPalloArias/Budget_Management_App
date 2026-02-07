import { Outlet } from 'react-router-dom';

export const PublicLayout = () => {
    return (
        <div className="min-h-screen bg-gradient-to-br from-green-200 to-green-300">
            <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
                <Outlet />
            </main>
        </div>
    );
};
