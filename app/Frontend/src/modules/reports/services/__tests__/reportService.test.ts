import HttpClient from '../../../../core/api/HttpClient';
import { getReportsSummary, getReportByPeriod, downloadReportPdf, deleteReport, recalculateReport } from '../reportService';

// Mock HttpClient
// HttpClient is already globally mocked in jest.setup.ts
// We just need to get the instance and mock its methods
const mockReportsHttpClient = HttpClient.getInstance('reports');

describe('reportService', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    
    // Mock global URL and document for downloadReportPdf
    if (typeof globalThis.URL.createObjectURL !== 'function') {
        globalThis.URL.createObjectURL = jest.fn();
    }
    if (typeof globalThis.URL.revokeObjectURL !== 'function') {
        globalThis.URL.revokeObjectURL = jest.fn();
    }
    
    document.body.innerHTML = '';
  });

  describe('getReportsSummary', () => {
    it('should call GET /v1/reports/summary with filters and return adapted data', async () => {
      const mockResponse = {
        data: {
          balance: 1000,
          totalIncome: 2000,
          totalExpense: 1000,
          savings: 500,
          reports: []
        }
      };
      
      (mockReportsHttpClient.get as jest.Mock).mockResolvedValue(mockResponse);
      
      const filters = { startPeriod: '2023-01', endPeriod: '2023-06' };
      const result = await getReportsSummary(filters);
      
      expect(mockReportsHttpClient.get).toHaveBeenCalledWith(
        expect.stringContaining('/v1/reports/summary?startPeriod=2023-01&endPeriod=2023-06')
      );
      expect(result.balance).toBe(1000);
      expect(result.totalExpenses).toBe(1000);
    });
  });

  describe('getReportByPeriod', () => {
    it('should call GET /v1/reports with period and return adapted data', async () => {
      const mockResponse = {
        data: {
          reportId: 1,
          userId: 'u1',
          period: '2023-01',
          balance: 1000,
          totalIncome: 1500,
          totalExpense: 500,
          savings: 200
        }
      };
      
      (mockReportsHttpClient.get as jest.Mock).mockResolvedValue(mockResponse);
      
      const result = await getReportByPeriod({ period: '2023-01' });
      
      expect(mockReportsHttpClient.get).toHaveBeenCalledWith('/v1/reports?period=2023-01');
      expect(result.id).toBe(1);
      expect(result.period).toBe('2023-01');
    });
  });

  describe('downloadReportPdf', () => {
    it('should call GET /v1/reports/pdf and trigger download', async () => {
        const mockBlob = new Blob(['pdf-content'], { type: 'application/pdf' });
        (mockReportsHttpClient.get as jest.Mock).mockResolvedValue({ data: mockBlob });
        
        // Mock click on link
        const clickMock = jest.fn();
        jest.spyOn(document, 'createElement').mockReturnValue({
            click: clickMock,
            remove: jest.fn(),
            href: '',
            download: '',
        } as any);

        await downloadReportPdf('2023-01');
        
        expect(mockReportsHttpClient.get).toHaveBeenCalledWith(
            '/v1/reports/pdf?period=2023-01',
            expect.objectContaining({ responseType: 'blob' })
        );
        expect(clickMock).toHaveBeenCalled();
    });
  });

  describe('deleteReport', () => {
    it('should call DELETE /v1/reports/:id', async () => {
      (mockReportsHttpClient.delete as jest.Mock).mockResolvedValue({});
      
      await deleteReport(123);
      
      expect(mockReportsHttpClient.delete).toHaveBeenCalledWith('/v1/reports/123');
    });
  });

  describe('recalculateReport', () => {
    it('should call POST /v1/reports/recalculate', async () => {
      const mockResponse = { data: { balance: 1000, period: '2023-01' } };
      (mockReportsHttpClient.post as jest.Mock).mockResolvedValue(mockResponse);
      
      const result = await recalculateReport('2023-01');
      
      expect(mockReportsHttpClient.post).toHaveBeenCalledWith(
        '/v1/reports/recalculate',
        { period: '2023-01' }
      );
      expect(result.balance).toBe(1000);
    });
  });
});
