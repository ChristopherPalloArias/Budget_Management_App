import { useTransactionStore } from '../useTransactionStore';
import { act } from '@testing-library/react';

describe('useTransactionStore', () => {
  beforeEach(() => {
    act(() => {
      useTransactionStore.getState().reset();
    });
  });

  it('should initialize with current month', () => {
    const state = useTransactionStore.getState();
    const currentMonth = new Date().toISOString().slice(0, 7);
    expect(state.selectedPeriod).toBe(currentMonth);
  });

  it('should update selected period', () => {
    act(() => {
      useTransactionStore.getState().setSelectedPeriod('2023-01');
    });
    
    expect(useTransactionStore.getState().selectedPeriod).toBe('2023-01');
  });

  it('should reset to initial state', () => {
    act(() => {
      useTransactionStore.getState().setSelectedPeriod('2023-01');
      useTransactionStore.getState().reset();
    });
    
    const currentMonth = new Date().toISOString().slice(0, 7);
    expect(useTransactionStore.getState().selectedPeriod).toBe(currentMonth);
  });
});
