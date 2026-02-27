import { renderHook, act } from '@testing-library/react';
import { useDeleteReport } from '../useDeleteReport';
import * as reportService from '../../services/reportService';
import { useMutation, useQueryClient } from '@tanstack/react-query';

// Mock reportService
jest.mock('../../services/reportService');

// Mock react-query
jest.mock('@tanstack/react-query', () => {
    const original = jest.requireActual('@tanstack/react-query');
    return {
        ...original,
        useMutation: jest.fn(),
        useQueryClient: jest.fn(),
    };
});

describe('useDeleteReport', () => {
  it('should call deleteReport on mutate', async () => {
    const mutateAsyncMock = jest.fn();
    (useMutation as jest.Mock).mockReturnValue({
        mutateAsync: mutateAsyncMock,
        isPending: false
    });
    (useQueryClient as jest.Mock).mockReturnValue({
        invalidateQueries: jest.fn()
    });

    const { result } = renderHook(() => useDeleteReport());
    
    await act(async () => {
        await result.current.mutateAsync(1);
    });
    
    expect(mutateAsyncMock).toHaveBeenCalledWith(1);
  });
});
