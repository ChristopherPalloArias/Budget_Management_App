import { renderHook, act } from '@testing-library/react';
import { useDeleteTransaction } from '../useDeleteTransaction';
import * as transactionService from '../../services/transactionService';
import { useMutation, useQueryClient } from '@tanstack/react-query';

// Mock transactionService
jest.mock('../../services/transactionService');

// Mock react-query
jest.mock('@tanstack/react-query', () => {
    const original = jest.requireActual('@tanstack/react-query');
    return {
        ...original,
        useMutation: jest.fn(),
        useQueryClient: jest.fn(),
    };
});

describe('useDeleteTransaction', () => {
  it('should call deleteTransaction on mutate', async () => {
    const mutateAsyncMock = jest.fn();
    (useMutation as jest.Mock).mockReturnValue({
        mutateAsync: mutateAsyncMock,
        isPending: false
    });
    (useQueryClient as jest.Mock).mockReturnValue({
        invalidateQueries: jest.fn()
    });

    const { result } = renderHook(() => useDeleteTransaction());
    
    await act(async () => {
        await result.current.mutateAsync(1);
    });
    
    expect(mutateAsyncMock).toHaveBeenCalledWith(1);
  });
});
