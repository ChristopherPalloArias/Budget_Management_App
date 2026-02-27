import { render, screen } from '@testing-library/react';
import { DashboardLayout } from '../DashboardLayout';
import { MemoryRouter } from 'react-router-dom';

// Mock UI components - fixed relative paths for src/shared/layouts/__tests__
jest.mock('../../../components/ui/sidebar', () => ({
  SidebarProvider: ({ children }: any) => <div data-testid="sidebar-provider">{children}</div>,
  SidebarInset: ({ children }: any) => <div data-testid="sidebar-inset">{children}</div>,
}));

jest.mock('../components/AppSidebar', () => ({
  AppSidebar: () => <div data-testid="app-sidebar">AppSidebar</div>
}));

jest.mock('../components/NavHeader', () => ({
  NavHeader: () => <div data-testid="nav-header">NavHeader</div>
}));

describe('DashboardLayout', () => {
  it('should render the layout with sidebar and header', () => {
    render(
      <MemoryRouter>
        <DashboardLayout />
      </MemoryRouter>
    );
    
    expect(screen.getByTestId('sidebar-provider')).toBeDefined();
    expect(screen.getByTestId('app-sidebar')).toBeDefined();
    expect(screen.getByTestId('sidebar-inset')).toBeDefined();
    expect(screen.getByTestId('nav-header')).toBeDefined();
  });
});
