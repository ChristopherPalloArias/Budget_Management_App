import { renderHook } from '@testing-library/react';
import { useTransactions } from '../useTransactions';
import * as transactionService from '../../services/transactionService';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useUserStore } from '@/modules/auth';

// Mock transactionService
jest.mock('../../services/transactionService');

// Mock react-query
jest.mock('@tanstack/react-query', () => ({
    useQuery: jest.fn(),
    useMutation: jest.fn(),
    useQueryClient: jest.fn(),
}));

// Mock useUserStore
jest.mock('@/modules/auth', () => ({
  useUserStore: jest.fn()
}));

// Mock sonner
jest.mock('sonner', () => ({
  toast: {
    success: jest.fn(),
    error: jest.fn()
  }
}));

describe('useTransactions', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('should return default values when no user', () => {
    (useUserStore as unknown as jest.Mock).mockReturnValue({ user: null });
    
    const { result } = renderHook(() => useTransactions('2023-01'));
    
    expect(result.current.transactions).toEqual([]);
    expect(result.current.isLoading).toBe(false);
  });

  it('should call getTransactionsByUser when user exists', () => {
    (useUserStore as unknown as jest.Mock).mockReturnValue({ user: { id: '1' } });
    (useQuery as jest.Mock).mockReturnValue({
        data: [],
        isLoading: false,
        error: null
    });
    (useMutation as jest.Mock).mockReturnValue({
        mutate: jest.fn(),
        isPending: false
    });
    (useQueryClient as jest.Mock).mockReturnValue({});

    renderHook(() => useTransactions('2023-01'));
    
    expect(useQuery).toHaveBeenCalledWith(
        expect.objectContaining({
            queryKey: expect.arrayContaining(['transactions', '2023-01']),
        })
    );
  });
});
