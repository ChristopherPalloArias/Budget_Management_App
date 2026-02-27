import { renderHook } from '@testing-library/react';
import { useGetReportsSummary } from '../useGetReportsSummary';
import * as reportService from '../../services/reportService';
import { useQuery } from '@tanstack/react-query';
import { useUserStore } from '@/modules/auth';

// Mock reportService
jest.mock('../../services/reportService');

// Mock react-query
jest.mock('@tanstack/react-query', () => ({
    useQuery: jest.fn(),
}));

// Mock useUserStore
jest.mock('@/modules/auth', () => ({
  useUserStore: jest.fn()
}));

describe('useGetReportsSummary', () => {
  it('should call getReportsSummary', () => {
    (useUserStore as unknown as jest.Mock).mockReturnValue({ user: { id: '1' } });
    (useQuery as jest.Mock).mockReturnValue({
        data: null,
        isLoading: false
    });

    renderHook(() => useGetReportsSummary());
    
    expect(useQuery).toHaveBeenCalledWith(
        expect.objectContaining({
            queryKey: expect.arrayContaining(['reports', 'summary']),
        })
    );
  });
});
