import { render, screen } from '@testing-library/react';
import { DynamicBreadcrumbs } from '../DynamicBreadcrumbs';
import { useLocation, MemoryRouter } from 'react-router-dom';

// Mock react-router-dom's useLocation
jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useLocation: jest.fn(),
}));

// Mock Breadcrumb UI components - fixed relative paths for src/shared/layouts/components/__tests__
jest.mock('../../../../components/ui/breadcrumb', () => ({
  Breadcrumb: ({ children }: any) => <nav>{children}</nav>,
  BreadcrumbList: ({ children }: any) => <ul>{children}</ul>,
  BreadcrumbItem: ({ children }: any) => <li>{children}</li>,
  BreadcrumbLink: ({ children }: any) => <span>{children}</span>,
  BreadcrumbPage: ({ children }: any) => <span>{children}</span>,
  BreadcrumbSeparator: () => <span>/</span>,
}));

describe('DynamicBreadcrumbs', () => {
  it('should render breadcrumbs for /dashboard', () => {
    (useLocation as jest.Mock).mockReturnValue({ pathname: '/dashboard' });
    
    render(
      <MemoryRouter>
        <DynamicBreadcrumbs />
      </MemoryRouter>
    );
    
    expect(screen.getByText('Reportes')).toBeDefined();
  });

  it('should render breadcrumbs for /transactions', () => {
    (useLocation as jest.Mock).mockReturnValue({ pathname: '/transactions' });
    
    render(
      <MemoryRouter>
        <DynamicBreadcrumbs />
      </MemoryRouter>
    );
    
    expect(screen.getByText('Transacciones')).toBeDefined();
  });

  it('should render default breadcrumb for unknown path', () => {
    (useLocation as jest.Mock).mockReturnValue({ pathname: '/unknown' });
    
    render(
      <MemoryRouter>
        <DynamicBreadcrumbs />
      </MemoryRouter>
    );
    
    expect(screen.getByText('Unknown')).toBeDefined();
  });
});
