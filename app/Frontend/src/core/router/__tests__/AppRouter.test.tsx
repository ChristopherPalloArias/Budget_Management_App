import { render, screen } from '@testing-library/react';
import { AppRouter } from '../AppRouter';
import { useUserStore } from '@/modules/auth';

// Mock useUserStore
jest.mock('@/modules/auth', () => ({
  useUserStore: jest.fn(),
  ProtectedRoute: ({ children }: any) => <div>ProtectedRoute {children}</div>,
  PublicRoute: ({ children }: any) => <div>PublicRoute {children}</div>,
}));

// Mock layout components - fixed paths for src/core/router/__tests__
jest.mock('../../../shared/layouts/PublicLayout', () => ({
  PublicLayout: ({ children }: any) => <div>PublicLayout {children}</div>,
}));
jest.mock('../../../shared/layouts/DashboardLayout', () => ({
  DashboardLayout: ({ children }: any) => <div>DashboardLayout {children}</div>,
}));

describe('AppRouter', () => {
  it('should render PublicRoute when not authenticated', () => {
    (useUserStore as unknown as jest.Mock).mockReturnValue({ user: null });
    
    render(<AppRouter />);
    
    expect(screen.getByText(/PublicRoute/)).toBeDefined();
  });
});
