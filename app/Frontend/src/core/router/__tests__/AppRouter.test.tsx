import { render, screen } from '@testing-library/react';
import { AppRouter } from '../AppRouter';
import { useUserStore } from '@/modules/auth/store/useUserStore';

// Mock useUserStore directly
jest.mock('@/modules/auth/store/useUserStore', () => ({
  useUserStore: jest.fn(),
}));

// Mock components used in AppRouter
jest.mock('@/modules/auth', () => ({
  ProtectedRoute: ({ children }: any) => <div data-testid="protected-route">ProtectedRoute {children}</div>,
  PublicRoute: ({ children }: any) => <div data-testid="public-route">PublicRoute {children}</div>,
}));

jest.mock('@/shared/layouts/PublicLayout', () => ({
  PublicLayout: () => <div data-testid="public-layout">PublicLayout</div>,
}));

jest.mock('@/shared/layouts/DashboardLayout', () => ({
  DashboardLayout: () => <div data-testid="dashboard-layout">DashboardLayout</div>,
}));

describe('AppRouter', () => {
  it('should render PublicRoute when not authenticated', () => {
    (useUserStore as unknown as jest.Mock).mockReturnValue({ user: null, isLoading: false });
    
    render(<AppRouter />);
    
    expect(screen.getByTestId('public-route')).toBeDefined();
  });
});
