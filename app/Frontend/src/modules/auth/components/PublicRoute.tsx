import { Navigate, Outlet } from 'react-router-dom';
import { useUserStore } from '../store/useUserStore.ts';

interface PublicRouteProps {
    children?: React.ReactNode;
}

export const PublicRoute = ({ children }: PublicRouteProps) => {
    const user = useUserStore((state) => state.user);
    const isLoading = useUserStore((state) => state.isLoading);

    if (isLoading) {
        return (
            <div className="min-h-screen flex items-center justify-center">
                <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-600"></div>
            </div>
        );
    }

    if (user) {
        return <Navigate to="/dashboard" replace />;
    }

    return <>{children || <Outlet />}</>;
};