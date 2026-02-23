// Mock Zustand store for testing
const mockStore = {
  user: null,
  isAuthenticated: false,
  isLoading: false,
  setUser: jest.fn(),
  clearUser: jest.fn(),
  setLoading: jest.fn(),
  initAuthListener: jest.fn(() => jest.fn()),
};

export const useUserStore = jest.fn((selector) => {
  if (typeof selector === "function") {
    return selector(mockStore);
  }
  return mockStore;
});
