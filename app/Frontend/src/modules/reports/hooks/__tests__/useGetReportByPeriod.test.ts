import { renderHook } from '@testing-library/react';
import { useGetReportByPeriod } from '../useGetReportByPeriod';
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

describe('useGetReportByPeriod', () => {
  it('should call getReportByPeriod with period object', () => {
    (useUserStore as unknown as jest.Mock).mockReturnValue({ user: { id: '1' } });
    (useQuery as jest.Mock).mockReturnValue({
        data: null,
        isLoading: false
    });

    renderHook(() => useGetReportByPeriod({ period: '2023-01' }));
    
    expect(useQuery).toHaveBeenCalledWith(
        expect.objectContaining({
            queryKey: expect.arrayContaining(['reports', 'byPeriod', '2023-01']),
        })
    );
  });
});
