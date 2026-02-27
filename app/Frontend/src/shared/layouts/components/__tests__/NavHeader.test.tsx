import { render, screen } from '@testing-library/react';
import { NavHeader } from '../NavHeader';
import { MemoryRouter } from 'react-router-dom';

// Mock UI components
jest.mock('../../../../components/ui/sidebar', () => ({
  SidebarTrigger: () => <button data-testid="sidebar-trigger">Trigger</button>,
}));

jest.mock('../../../../components/ui/separator', () => ({
  Separator: () => <div data-testid="separator">Separator</div>,
}));

jest.mock('../DynamicBreadcrumbs', () => ({
  DynamicBreadcrumbs: () => <div data-testid="dynamic-breadcrumbs">Breadcrumbs</div>,
}));

jest.mock('../CommandMenu', () => ({
  CommandMenu: () => <div data-testid="command-menu">CommandMenu</div>,
}));

jest.mock('../ThemeToggle', () => ({
  ThemeToggle: () => <div data-testid="theme-toggle">ThemeToggle</div>,
}));

jest.mock('../NavHeaderUser', () => ({
  NavHeaderUser: () => <div data-testid="nav-header-user">NavHeaderUser</div>,
}));

describe('NavHeader', () => {
  it('should render the header with all parts', () => {
    render(
      <MemoryRouter>
        <NavHeader />
      </MemoryRouter>
    );
    
    expect(screen.getByTestId('sidebar-trigger')).toBeDefined();
    expect(screen.getByTestId('dynamic-breadcrumbs')).toBeDefined();
    expect(screen.getByTestId('command-menu')).toBeDefined();
    expect(screen.getByTestId('theme-toggle')).toBeDefined();
    expect(screen.getByTestId('nav-header-user')).toBeDefined();
  });
});
