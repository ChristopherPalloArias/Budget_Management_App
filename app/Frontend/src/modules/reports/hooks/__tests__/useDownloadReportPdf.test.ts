import { renderHook, act } from '@testing-library/react';
import { useDownloadReportPdf } from '../useDownloadReportPdf';
import * as reportService from '../../services/reportService';
import { useUserStore } from '@/modules/auth';

// Mock services
jest.mock('../../services/reportService');
jest.mock('@/modules/auth', () => ({
  useUserStore: jest.fn()
}));

describe('useDownloadReportPdf', () => {
  it('should call downloadReportPdf on download', async () => {
    (useUserStore as unknown as jest.Mock).mockReturnValue({
      user: { id: '1' }
    });

    const { result } = renderHook(() => useDownloadReportPdf());
    
    await act(async () => {
        await result.current.download('2023-01');
    });
    
    expect(reportService.downloadReportPdf).toHaveBeenCalledWith('2023-01');
  });
});
