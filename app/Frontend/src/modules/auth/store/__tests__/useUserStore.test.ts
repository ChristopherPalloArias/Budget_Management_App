import { useUserStore } from '../useUserStore';
import { act } from '@testing-library/react';

describe('useUserStore', () => {
  beforeEach(() => {
    act(() => {
      // Manual reset to initial state
      useUserStore.setState({
        user: null,
        isAuthenticated: false,
        isLoading: true,
      });
    });
  });

  it('should initialize with default values', () => {
    const state = useUserStore.getState();
    expect(state.user).toBeNull();
    expect(state.isAuthenticated).toBe(false);
    expect(state.isLoading).toBe(true);
  });

  it('should set user', () => {
    const mockUser = { id: '1', displayName: 'Test User', email: 'test@example.com', photoURL: '' };
    
    act(() => {
      useUserStore.getState().setUser(mockUser);
    });
    
    const state = useUserStore.getState();
    expect(state.user).toEqual(mockUser);
    expect(state.isAuthenticated).toBe(true);
    expect(state.isLoading).toBe(false);
  });

  it('should logout', async () => {
    act(() => {
      useUserStore.getState().setUser({ id: '1' } as any);
    });
    
    await act(async () => {
      await useUserStore.getState().logout();
    });
    
    const state = useUserStore.getState();
    expect(state.user).toBeNull();
    expect(state.isAuthenticated).toBe(false);
    expect(state.isLoading).toBe(false);
  });
});
