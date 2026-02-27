import { render, screen, fireEvent, act } from '@testing-library/react';
import { CommandMenu } from '../CommandMenu';
import { MemoryRouter } from 'react-router-dom';

const mockNavigate = jest.fn();
jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useNavigate: () => mockNavigate,
}));

// Mock UI components - fixed paths
jest.mock('../../../../components/ui/command', () => ({
  Command: ({ children }: any) => <div>{children}</div>,
  CommandInput: ({ onValueChange }: any) => <input onChange={(e) => onValueChange(e.target.value)} />,
  CommandList: ({ children }: any) => <div>{children}</div>,
  CommandEmpty: ({ children }: any) => <div>{children}</div>,
  CommandGroup: ({ children }: any) => <div>{children}</div>,
  CommandItem: ({ children, onSelect }: any) => <div onClick={onSelect}>{children}</div>,
}));

jest.mock('../../../../components/ui/popover', () => ({
  Popover: ({ children, open }: any) => <div>{children}</div>,
  PopoverTrigger: ({ children }: any) => <div>{children}</div>,
  PopoverContent: ({ children }: any) => <div>{children}</div>,
}));

jest.mock('../../../../components/ui/button', () => ({
  Button: ({ children, onClick }: any) => <button onClick={onClick}>{children}</button>,
}));

describe('CommandMenu', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('should toggle open state on keyboard shortcut', () => {
    render(
      <MemoryRouter>
        <CommandMenu />
      </MemoryRouter>
    );

    act(() => {
      fireEvent.keyDown(document, { key: 'k', ctrlKey: true });
    });
    
    expect(screen.getByText('Buscar...')).toBeDefined();
  });

  it('should navigate on item selection', () => {
    render(
      <MemoryRouter>
        <CommandMenu />
      </MemoryRouter>
    );

    const item = screen.getByText('Transacciones');
    fireEvent.click(item);
    
    expect(mockNavigate).toHaveBeenCalledWith('/transactions');
  });
});
