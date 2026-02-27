import { render, screen, fireEvent } from '@testing-library/react';
import { NavHeaderUser } from '../NavHeaderUser';
import { useUserStore } from '../../../../modules/auth/store/useUserStore';

// Mock useUserStore - fixed relative paths for src/shared/layouts/components/__tests__
jest.mock('../../../../modules/auth/store/useUserStore');

// Mock UI components
jest.mock('../../../../components/ui/dropdown-menu', () => ({
  DropdownMenu: ({ children }: any) => <div>{children}</div>,
  DropdownMenuTrigger: ({ children }: any) => <div>{children}</div>,
  DropdownMenuContent: ({ children }: any) => <div>{children}</div>,
  DropdownMenuItem: ({ children, onClick }: any) => <div onClick={onClick}>{children}</div>,
  DropdownMenuLabel: ({ children }: any) => <div>{children}</div>,
  DropdownMenuSeparator: () => <div>---</div>,
}));

jest.mock('../../../../components/ui/avatar', () => ({
  Avatar: ({ children }: any) => <div>{children}</div>,
  AvatarImage: ({ src, alt }: any) => <img src={src} alt={alt} />,
  AvatarFallback: ({ children }: any) => <div>{children}</div>,
}));

describe('NavHeaderUser', () => {
  const mockLogout = jest.fn();
  
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('should render user info when authenticated', () => {
    (useUserStore as unknown as jest.Mock).mockReturnValue({
      user: { displayName: 'Test User', email: 'test@example.com', photoURL: '' },
      logout: mockLogout,
    });

    render(<NavHeaderUser />);
    
    expect(screen.getByText('Test User')).toBeDefined();
    expect(screen.getByText('test@example.com')).toBeDefined();
  });

  it('should call logout on menu item click', () => {
    (useUserStore as unknown as jest.Mock).mockReturnValue({
      user: { displayName: 'Test User', email: 'test@example.com', photoURL: '' },
      logout: mockLogout,
    });

    render(<NavHeaderUser />);
    
    fireEvent.click(screen.getByText('Cerrar sesión'));
    expect(mockLogout).toHaveBeenCalled();
  });

  it('should return null when no user', () => {
    (useUserStore as unknown as jest.Mock).mockReturnValue({ user: null });
    
    const { container } = render(<NavHeaderUser />);
    expect(container.firstChild).toBeNull();
  });
});
