import { renderHook } from '@testing-library/react';
import { useAuthStatus } from '../useAuthStatus';
import { useUserStore } from '../../store/useUserStore';

// Mock useUserStore
jest.mock('../../store/useUserStore', () => ({
  useUserStore: jest.fn()
}));

describe('useAuthStatus', () => {
  it('should return authentication status', () => {
    (useUserStore as unknown as jest.Mock).mockImplementation((selector) => selector({
      isAuthenticated: true,
      isLoading: false
    }));

    const { result } = renderHook(() => useAuthStatus());
    
    expect(result.current.isAuthenticated).toBe(true);
    expect(result.current.isChecking).toBe(false);
  });

  it('should return loading status', () => {
    (useUserStore as unknown as jest.Mock).mockImplementation((selector) => selector({
      isAuthenticated: false,
      isLoading: true
    }));

    const { result } = renderHook(() => useAuthStatus());
    
    expect(result.current.isAuthenticated).toBe(false);
    expect(result.current.isChecking).toBe(true);
  });
});
