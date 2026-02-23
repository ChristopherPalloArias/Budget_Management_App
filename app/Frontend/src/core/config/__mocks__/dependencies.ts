// Mock authRepository
export const authRepository = {
  signIn: jest.fn(),
  signInWithProvider: jest.fn(),
  register: jest.fn(),
  signOut: jest.fn(),
  onAuthStateChanged: jest.fn((callback) => {
    callback(null);
    return jest.fn();
  }),
};
