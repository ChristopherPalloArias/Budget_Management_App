import React from 'react';
import { render, screen } from '@testing-library/react';
import { AppRouter } from '../AppRouter';
import { useUserStore } from '../../../modules/auth/store/useUserStore';

// Mock useUserStore directly
jest.mock('../../../modules/auth/store/useUserStore', () => ({
  __esModule: true,
  useUserStore: jest.fn(),
}));

// Mock components used in AppRouter
jest.mock('../../../modules/auth', () => ({
  __esModule: true,
  ProtectedRoute: ({ children }: any) => <div data-testid="protected-route">{children}</div>,
  PublicRoute: ({ children }: any) => <div data-testid="public-route">{children}</div>,
}));

jest.mock('../../PublicLayout', () => ({
  __esModule: true,
  PublicLayout: ({ children }: any) => <div data-testid="public-layout">{children}</div>,
}));

jest.mock('../../DashboardLayout', () => ({
  __esModule: true,
  DashboardLayout: ({ children }: any) => <div data-testid="dashboard-layout">{children}</div>,
}));

jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  BrowserRouter: ({ children }: any) => <div data-testid="browser-router">{children}</div>,
}));

describe('AppRouter', () => {
  it('should render PublicRoute when not authenticated', () => {
    (useUserStore as unknown as jest.Mock).mockReturnValue({ user: null, isLoading: false });
    
    render(<AppRouter />);
    
    expect(screen.getByTestId('public-route')).toBeDefined();
  });
});
