import { render, screen, fireEvent } from '@testing-library/react';
import { ThemeToggle } from '../ThemeToggle';

// Mock matchMedia
Object.defineProperty(window, 'matchMedia', {
  writable: true,
  value: jest.fn().mockImplementation(query => ({
    matches: false,
    media: query,
    onchange: null,
    addListener: jest.fn(), // Deprecated
    removeListener: jest.fn(), // Deprecated
    addEventListener: jest.fn(),
    removeEventListener: jest.fn(),
    dispatchEvent: jest.fn(),
  })),
});

describe('ThemeToggle', () => {
  beforeEach(() => {
    localStorage.clear();
    document.documentElement.classList.remove('light', 'dark');
  });

  it('should initialize with system theme by default', () => {
    render(<ThemeToggle />);
    expect(document.documentElement.classList.contains('light')).toBe(true);
  });

  it('should apply saved theme from localStorage', () => {
    localStorage.setItem('theme', 'dark');
    render(<ThemeToggle />);
    expect(document.documentElement.classList.contains('dark')).toBe(true);
  });

  it('should change theme to dark when selected', () => {
    render(<ThemeToggle />);
    
    // Open dropdown (Trigger is a button)
    const trigger = screen.getByRole('button', { name: /toggle theme/i });
    fireEvent.click(trigger);
    
    // Select "Oscuro" (Dark)
    const darkOption = screen.getByText('Oscuro');
    fireEvent.click(darkOption);
    
    expect(document.documentElement.classList.contains('dark')).toBe(true);
    expect(localStorage.getItem('theme')).toBe('dark');
  });

  it('should change theme to light when selected', () => {
    localStorage.setItem('theme', 'dark');
    render(<ThemeToggle />);
    
    const trigger = screen.getByRole('button', { name: /toggle theme/i });
    fireEvent.click(trigger);
    
    const lightOption = screen.getByText('Claro');
    fireEvent.click(lightOption);
    
    expect(document.documentElement.classList.contains('light')).toBe(true);
    expect(localStorage.getItem('theme')).toBe('light');
  });
});
