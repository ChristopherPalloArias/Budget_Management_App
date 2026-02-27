import { render, screen } from '@testing-library/react';
import { AppSidebar } from '../AppSidebar';
import { MemoryRouter } from 'react-router-dom';

// Mock UI components - fixed relative paths and ensured all named exports are present
jest.mock('../../../../components/ui/sidebar', () => ({
  Sidebar: ({ children }: any) => <aside>{children}</aside>,
  SidebarContent: ({ children }: any) => <div>{children}</div>,
  SidebarFooter: ({ children }: any) => <footer>{children}</footer>,
  SidebarGroup: ({ children }: any) => <div>{children}</div>,
  SidebarGroupContent: ({ children }: any) => <div>{children}</div>,
  SidebarGroupLabel: ({ children }: any) => <div>{children}</div>,
  SidebarHeader: ({ children }: any) => <header>{children}</header>,
  SidebarMenu: ({ children }: any) => <ul>{children}</ul>,
  SidebarMenuButton: ({ children }: any) => <li>{children}</li>,
  SidebarMenuItem: ({ children }: any) => <li>{children}</li>,
  useSidebar: () => ({ isMobile: false, open: true, setOpen: jest.fn(), openMobile: false, setOpenMobile: jest.fn(), toggleSidebar: jest.fn() }),
}));

jest.mock('../NavUser', () => ({
  NavUser: () => <div data-testid="nav-user">NavUser</div>
}));

describe('AppSidebar', () => {
  it('should render navigation items', () => {
    render(
      <MemoryRouter>
        <AppSidebar />
      </MemoryRouter>
    );
    
    expect(screen.getByText('Reportes')).toBeDefined();
    expect(screen.getByText('Transacciones')).toBeDefined();
    expect(screen.getByTestId('nav-user')).toBeDefined();
  });
});
