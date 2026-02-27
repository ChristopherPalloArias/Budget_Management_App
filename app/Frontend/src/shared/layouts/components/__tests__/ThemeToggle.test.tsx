import { render, screen, fireEvent, act } from '@testing-library/react';
import { ThemeToggle } from '../ThemeToggle';

// Mock UI components - simplified to avoid portal issues
jest.mock('../../../components/ui/dropdown-menu', () => ({
  DropdownMenu: ({ children }: any) => <div>{children}</div>,
  DropdownMenuTrigger: ({ children }: any) => <div>{children}</div>,
  DropdownMenuContent: ({ children }: any) => <div>{children}</div>,
  DropdownMenuItem: ({ children, onClick }: any) => <button onClick={onClick}>{children}</button>,
}));

jest.mock('../../../components/ui/button', () => ({
  Button: ({ children, onClick }: any) => <button onClick={onClick}>{children}</button>,
}));

describe('ThemeToggle', () => {
  beforeEach(() => {
    localStorage.clear();
    document.documentElement.classList.remove('light', 'dark');
  });

  it('should render theme toggle button', () => {
    render(<ThemeToggle />);
    expect(screen.getByText('Toggle theme')).toBeDefined();
  });

  it('should change theme to dark when selected', async () => {
    render(<ThemeToggle />);
    
    const darkOption = screen.getByText('Oscuro');
    fireEvent.click(darkOption);
    
    expect(document.documentElement.classList.contains('dark')).toBe(true);
    expect(localStorage.getItem('theme')).toBe('dark');
  });

  it('should change theme to light when selected', async () => {
    render(<ThemeToggle />);
    
    const lightOption = screen.getByText('Claro');
    fireEvent.click(lightOption);
    
    expect(document.documentElement.classList.contains('light')).toBe(true);
    expect(localStorage.getItem('theme')).toBe('light');
  });
});
