import { render, screen } from '@testing-library/react';
import { ReportsPage } from '../ReportsPage';
import { useReportStore } from '../../store/useReportStore';
import { useGetReportsSummary } from '../../hooks/useGetReportsSummary';

// Mock hooks
jest.mock('../../store/useReportStore');
jest.mock('../../hooks/useGetReportsSummary');

// Mock child components to simplify
jest.mock('../../components/ReportFilters', () => ({
  ReportFilters: () => <div>ReportFilters</div>
}));
jest.mock('../../components/ReportSummaryCards', () => ({
  ReportSummaryCards: () => <div>ReportSummaryCards</div>
}));
jest.mock('../../components/ReportTable', () => ({
  ReportTable: () => <div>ReportTable</div>
}));
jest.mock('../../components/ReportsPageSkeleton', () => ({
  ReportsPageSkeleton: () => <div>ReportsPageSkeleton</div>
}));

describe('ReportsPage', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('should render skeleton when loading and no data', () => {
    (useReportStore as unknown as jest.Mock).mockReturnValue({
      filters: { startPeriod: '', endPeriod: '' },
      setFilters: jest.fn(),
    });
    (useGetReportsSummary as jest.Mock).mockReturnValue({
      data: null,
      isLoading: true,
      refetch: jest.fn(),
    });

    render(<ReportsPage />);
    expect(screen.getByText('ReportsPageSkeleton')).toBeDefined();
  });

  it('should render content when data is loaded', () => {
    (useReportStore as unknown as jest.Mock).mockReturnValue({
      filters: { startPeriod: '2023-01', endPeriod: '2023-02' },
      setFilters: jest.fn(),
    });
    (useGetReportsSummary as jest.Mock).mockReturnValue({
      data: { 
        balance: 100, 
        totalIncome: 200, 
        totalExpenses: 100,
        reports: [] 
      },
      isLoading: false,
      refetch: jest.fn(),
    });

    render(<ReportsPage />);
    expect(screen.getByText('Reportes Financieros')).toBeDefined();
    expect(screen.getByText('ReportSummaryCards')).toBeDefined();
    expect(screen.getByText('ReportTable')).toBeDefined();
  });
});
