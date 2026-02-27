import { useUserStore } from '../useUserStore';
import { act } from '@testing-library/react';

describe('useUserStore', () => {
  beforeEach(() => {
    act(() => {
      useUserStore.getState().setUser(null);
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
    
    expect(useUserStore.getState().user).toEqual(mockUser);
    expect(useUserStore.getState().isAuthenticated).toBe(true);
    expect(useUserStore.getState().isLoading).toBe(false);
  });

  it('should logout', async () => {
    act(() => {
      useUserStore.getState().setUser({ id: '1' } as any);
    });
    
    await act(async () => {
      await useUserStore.getState().logout();
    });
    
    expect(useUserStore.getState().user).toBeNull();
    expect(useUserStore.getState().isAuthenticated).toBe(false);
    expect(useUserStore.getState().isLoading).toBe(false);
  });
});
