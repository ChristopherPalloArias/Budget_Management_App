import { render, screen, fireEvent } from '@testing-library/react';
import { NavUser } from '../NavUser';
import { useUserStore } from '../../../../modules/auth/store/useUserStore';
import { useSidebar } from '../../../../components/ui/sidebar';

// Mock hooks - fixed relative paths for src/shared/layouts/components/__tests__
jest.mock('../../../../modules/auth/store/useUserStore');
// Mock sidebar components more comprehensively
jest.mock('../../../../components/ui/sidebar', () => ({
  SidebarMenu: ({ children }: any) => <div data-testid="sidebar-menu">{children}</div>,
  SidebarMenuItem: ({ children }: any) => <div data-testid="sidebar-menu-item">{children}</div>,
  SidebarMenuButton: ({ children, onClick }: any) => (
    <button onClick={onClick} data-testid="sidebar-menu-button">{children}</button>
  ),
  useSidebar: jest.fn().mockReturnValue({ isMobile: false }),
}));

// Mock UI components
jest.mock('../../../../components/ui/dropdown-menu', () => ({
  DropdownMenu: ({ children }: any) => <div>{children}</div>,
  DropdownMenuTrigger: ({ children }: any) => <div>{children}</div>,
  DropdownMenuContent: ({ children }: any) => <div>{children}</div>,
  DropdownMenuItem: ({ children, onClick }: any) => <div onClick={onClick}>{children}</div>,
  DropdownMenuLabel: ({ children }: any) => <div>{children}</div>,
  DropdownMenuSeparator: () => <div>---</div>,
}));

jest.mock('@/components/ui/avatar', () => ({
  Avatar: ({ children }: any) => <div>{children}</div>,
  AvatarImage: ({ src, alt }: any) => <img src={src} alt={alt} />,
  AvatarFallback: ({ children }: any) => <div>{children}</div>,
}));

describe('NavUser', () => {
  const mockLogout = jest.fn();
  
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('should render user info when authenticated', () => {
    (useUserStore as unknown as jest.Mock).mockReturnValue({
      user: { displayName: 'Test User', email: 'test@example.com', photoURL: '' },
      logout: mockLogout,
    });
    (useSidebar as jest.Mock).mockReturnValue({ isMobile: false });

    render(<NavUser />);
    
    expect(screen.getByText('Test User')).toBeDefined();
    expect(screen.getByText('test@example.com')).toBeDefined();
  });

  it('should call logout on menu item click', () => {
    (useUserStore as unknown as jest.Mock).mockReturnValue({
      user: { displayName: 'Test User', email: 'test@example.com', photoURL: '' },
      logout: mockLogout,
    });
    (useSidebar as jest.Mock).mockReturnValue({ isMobile: false });

    render(<NavUser />);
    
    fireEvent.click(screen.getByText('Cerrar sesión'));
    expect(mockLogout).toHaveBeenCalled();
  });

  it('should return null when no user', () => {
    (useUserStore as unknown as jest.Mock).mockReturnValue({ user: null });
    
    const { container } = render(<NavUser />);
    expect(container.firstChild).toBeNull();
  });
});
