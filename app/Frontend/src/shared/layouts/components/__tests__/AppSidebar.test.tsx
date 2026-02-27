import { render, screen } from '@testing-library/react';
import { AppSidebar } from '../AppSidebar';
import { MemoryRouter } from 'react-router-dom';

// Mock UI components - fixed relative paths for src/shared/layouts/components/__tests__
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
}));

jest.mock('../NavUser', () => ({
  NavUser: () => <div>NavUser</div>
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
    expect(screen.getByText('NavUser')).toBeDefined();
  });
});
