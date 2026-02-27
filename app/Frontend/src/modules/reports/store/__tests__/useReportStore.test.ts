import { useReportStore } from '../useReportStore';
import { act } from '@testing-library/react';

describe('useReportStore', () => {
  beforeEach(() => {
    act(() => {
      useReportStore.getState().reset();
    });
  });

  it('should initialize with default values', () => {
    const state = useReportStore.getState();
    expect(state.currentReport).toBeNull();
    expect(state.filters.startPeriod).toBeDefined();
    expect(state.filters.endPeriod).toBeDefined();
  });

  it('should update current report', () => {
    const mockReport = { id: 1, period: '2023-01' } as any;
    
    act(() => {
      useReportStore.getState().setCurrentReport(mockReport);
    });
    
    expect(useReportStore.getState().currentReport).toEqual(mockReport);
  });

  it('should update filters', () => {
    const mockFilters = { startPeriod: '2023-01', endPeriod: '2023-02' };
    
    act(() => {
      useReportStore.getState().setFilters(mockFilters);
    });
    
    expect(useReportStore.getState().filters).toEqual(mockFilters);
  });

  it('should clear report data', () => {
     act(() => {
       useReportStore.getState().setCurrentReport({ id: 1 } as any);
       useReportStore.getState().clearReportData();
     });
     
     expect(useReportStore.getState().currentReport).toBeNull();
  });
});
