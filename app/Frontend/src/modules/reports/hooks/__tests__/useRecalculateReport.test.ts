import { renderHook, act } from '@testing-library/react';
import { useRecalculateReport } from '../useRecalculateReport';
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

describe('useRecalculateReport', () => {
  it('should call recalculateReport on mutate', async () => {
    const mutateAsyncMock = jest.fn();
    (useMutation as jest.Mock).mockReturnValue({
        mutateAsync: mutateAsyncMock,
        isPending: false
    });
    (useQueryClient as jest.Mock).mockReturnValue({
        invalidateQueries: jest.fn()
    });

    const { result } = renderHook(() => useRecalculateReport());
    
    await act(async () => {
        await result.current.mutateAsync('2023-01');
    });
    
    expect(mutateAsyncMock).toHaveBeenCalledWith('2023-01');
  });
});
