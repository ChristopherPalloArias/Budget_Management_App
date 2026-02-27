import { renderHook } from '@testing-library/react';
import { useAuthInitialization } from '../useAuthInitialization';
import { useUserStore } from '../../store/useUserStore';

// Mock useUserStore
jest.mock('../../store/useUserStore', () => ({
  useUserStore: jest.fn()
}));

describe('useAuthInitialization', () => {
  it('should call initAuthListener on mount', () => {
    const unsubscribeMock = jest.fn();
    const initAuthListenerMock = jest.fn().mockReturnValue(unsubscribeMock);
    
    (useUserStore as unknown as jest.Mock).mockImplementation((selector) => selector({
      initAuthListener: initAuthListenerMock
    }));

    const { unmount } = renderHook(() => useAuthInitialization());
    
    expect(initAuthListenerMock).toHaveBeenCalled();
    
    unmount();
    expect(unsubscribeMock).toHaveBeenCalled();
  });
});
