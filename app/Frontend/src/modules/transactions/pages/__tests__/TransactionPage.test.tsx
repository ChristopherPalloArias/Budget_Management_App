import { render, screen } from '@testing-library/react';
import { TransactionPage } from '../TransactionPage';
import { useUserStore } from '@/modules/auth';
import { useTransactions } from '../../hooks/useTransactions';

// Mock hooks
jest.mock('@/modules/auth', () => ({
  useUserStore: jest.fn(),
}));
jest.mock('../../hooks/useTransactions');

// Mock child components
jest.mock('../../components/DataTable', () => ({
  DataTable: () => <div>DataTable</div>
}));
jest.mock('../../components/TransactionModalsProvider', () => ({
  TransactionModalsProvider: ({ children }: any) => <div>{children}</div>
}));
jest.mock('../../components/TransactionPageSkeleton', () => ({
  TransactionPageSkeleton: () => <div>TransactionPageSkeleton</div>
}));
jest.mock('../../components/TransactionPageError', () => ({
  TransactionPageError: ({ message }: any) => <div>Error: {message}</div>
}));

describe('TransactionPage', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('should render null when no user', () => {
    (useUserStore as unknown as jest.Mock).mockReturnValue({ user: null });
    (useTransactions as jest.Mock).mockReturnValue({
      transactions: [],
      isLoading: false,
      error: null,
    });

    const { container } = render(<TransactionPage />);
    expect(container.firstChild).toBeNull();
  });

  it('should render skeleton when loading', () => {
    (useUserStore as unknown as jest.Mock).mockReturnValue({ user: { id: '1' } });
    (useTransactions as jest.Mock).mockReturnValue({
      transactions: [],
      isLoading: true,
      error: null,
    });

    render(<TransactionPage />);
    expect(screen.getByText('TransactionPageSkeleton')).toBeDefined();
  });

  it('should render error when fetch fails', () => {
    (useUserStore as unknown as jest.Mock).mockReturnValue({ user: { id: '1' } });
    (useTransactions as jest.Mock).mockReturnValue({
      transactions: [],
      isLoading: false,
      error: { message: 'Failed to fetch' },
    });

    render(<TransactionPage />);
    expect(screen.getByText('Error: Failed to fetch')).toBeDefined();
  });

  it('should render DataTable when data is loaded', () => {
    (useUserStore as unknown as jest.Mock).mockReturnValue({ user: { id: '1' } });
    (useTransactions as jest.Mock).mockReturnValue({
      transactions: [{ id: '1', amount: 100 }],
      isLoading: false,
      error: null,
    });

    render(<TransactionPage />);
    expect(screen.getByText('DataTable')).toBeDefined();
  });
});
