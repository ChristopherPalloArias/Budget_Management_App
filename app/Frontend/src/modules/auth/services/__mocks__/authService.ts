import type { IAuthUser } from "@/core/auth/interfaces/IAuthRepository";

// Mock user data
export const mockAuthUser: IAuthUser = {
  id: "test-user-id",
  email: "test@example.com",
  displayName: "Test User",
  photoURL: null,
};

// Mock functions
export const loginWithEmail = jest.fn(
  async (email: string, password: string): Promise<IAuthUser> => {
    if (email === "error@test.com") {
      throw new Error("Invalid credentials");
    }
    return mockAuthUser;
  },
);

export const loginWithGoogle = jest.fn(async (): Promise<IAuthUser> => {
  return mockAuthUser;
});

export const registerWithEmail = jest.fn(
  async (
    displayName: string,
    email: string,
    password: string,
  ): Promise<IAuthUser> => {
    if (email === "existing@test.com") {
      throw new Error("Email already in use");
    }
    return { ...mockAuthUser, displayName, email };
  },
);

export const logout = jest.fn(async (): Promise<void> => {
  return Promise.resolve();
});
