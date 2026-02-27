import { renderHook, act } from '@testing-library/react';
import { useTransactionOperations } from '../useTransactionOperations';
import { transactionBusinessLogic } from '../../services/transactionBusinessLogic';
import { useQueryClient } from '@tanstack/react-query';

// Mock transactionBusinessLogic
jest.mock('../../services/transactionBusinessLogic', () => ({
  transactionBusinessLogic: {
    createTransaction: jest.fn(),
    updateTransaction: jest.fn()
  }
}));

// Mock react-query
jest.mock('@tanstack/react-query', () => ({
    useQueryClient: jest.fn(),
}));

// Mock sonner
jest.mock('sonner', () => ({
  toast: {
    success: jest.fn(),
    error: jest.fn()
  }
}));

describe('useTransactionOperations', () => {
  beforeEach(() => {
    (useQueryClient as jest.Mock).mockReturnValue({
        invalidateQueries: jest.fn()
    });
    jest.clearAllMocks();
  });

  it('should call createTransaction correctly', async () => {
    (transactionBusinessLogic.createTransaction as jest.Mock).mockResolvedValue({ id: '1' });

    const { result } = renderHook(() => useTransactionOperations());
    
    const formData = { description: 'test' } as any;
    let operationResult;
    await act(async () => {
        operationResult = await result.current.createTransaction(formData, '1');
    });
    
    expect(transactionBusinessLogic.createTransaction).toHaveBeenCalledWith(formData, '1');
    expect(operationResult).toEqual({ success: true });
  });

  it('should call updateTransaction correctly', async () => {
    (transactionBusinessLogic.updateTransaction as jest.Mock).mockResolvedValue({ id: '1' });

    const { result } = renderHook(() => useTransactionOperations());
    
    const formData = { description: 'test-updated' } as any;
    let operationResult;
    await act(async () => {
        operationResult = await result.current.editTransaction('1', formData);
    });
    
    expect(transactionBusinessLogic.updateTransaction).toHaveBeenCalledWith('1', formData);
    expect(operationResult).toEqual({ success: true });
  });
});
