import { ApiAuthRepository } from '../ApiAuthRepository';
import HttpClient from '@/core/api/HttpClient';

// Mock HttpClient
jest.mock('@/core/api/HttpClient', () => ({
    __esModule: true,
    default: {
        getInstance: jest.fn(),
    },
}));

// Mock localStorage
const mockLocalStorage = (() => {
    let store: Record<string, string> = {};
    return {
        getItem: jest.fn((key: string) => store[key] ?? null),
        setItem: jest.fn((key: string, value: string) => {
            store[key] = value;
        }),
        removeItem: jest.fn((key: string) => {
            delete store[key];
        }),
        clear: jest.fn(() => {
            store = {};
        }),
    };
})();

Object.defineProperty(window, 'localStorage', { value: mockLocalStorage });

describe('ApiAuthRepository', () => {
    let repository: ApiAuthRepository;
    let mockAxiosInstance: {
        post: jest.Mock;
        get: jest.Mock;
    };

    const MOCK_AUTH_RESPONSE = {
        userId: '550e8400-e29b-41d4-a716-446655440000',
        email: 'test@example.com',
        displayName: 'Test User',
        token: 'eyJhbGciOiJIUzI1NiJ9.mock.token',
    };

    const MOCK_USER_RESPONSE = {
        userId: '550e8400-e29b-41d4-a716-446655440000',
        email: 'test@example.com',
        displayName: 'Test User',
        photoURL: null,
    };

    beforeEach(() => {
        jest.clearAllMocks();
        mockLocalStorage.clear();

        mockAxiosInstance = {
            post: jest.fn(),
            get: jest.fn(),
        };

        (HttpClient.getInstance as jest.Mock).mockReturnValue(mockAxiosInstance);
        repository = new ApiAuthRepository();
    });

    describe('signIn', () => {
        it('should call POST /v1/auth/login and return mapped user', async () => {
            // Arrange
            mockAxiosInstance.post.mockResolvedValue({ data: MOCK_AUTH_RESPONSE });

            // Act
            const user = await repository.signIn({
                email: 'test@example.com',
                password: 'SecurePass123',
            });

            // Assert
            expect(mockAxiosInstance.post).toHaveBeenCalledWith('/v1/auth/login', {
                email: 'test@example.com',
                password: 'SecurePass123',
            });
            expect(user).toEqual({
                id: MOCK_AUTH_RESPONSE.userId,
                email: MOCK_AUTH_RESPONSE.email,
                displayName: MOCK_AUTH_RESPONSE.displayName,
                photoURL: null,
            });
        });

        it('should save the JWT token in localStorage', async () => {
            // Arrange
            mockAxiosInstance.post.mockResolvedValue({ data: MOCK_AUTH_RESPONSE });

            // Act
            await repository.signIn({
                email: 'test@example.com',
                password: 'SecurePass123',
            });

            // Assert
            expect(mockLocalStorage.setItem).toHaveBeenCalledWith(
                'auth_token',
                MOCK_AUTH_RESPONSE.token
            );
        });

        it('should throw Error with message from API error response', async () => {
            // Arrange
            mockAxiosInstance.post.mockRejectedValue({
                response: {
                    status: 401,
                    data: { message: 'Credenciales incorrectas' },
                },
            });

            // Act & Assert
            await expect(
                repository.signIn({ email: 'test@example.com', password: 'wrong' })
            ).rejects.toThrow('Credenciales incorrectas');
        });
    });

    describe('register', () => {
        it('should call POST /v1/auth/register and return mapped user', async () => {
            // Arrange
            mockAxiosInstance.post.mockResolvedValue({ data: MOCK_AUTH_RESPONSE });

            // Act
            const user = await repository.register({
                displayName: 'Test User',
                email: 'test@example.com',
                password: 'SecurePass123',
            });

            // Assert
            expect(mockAxiosInstance.post).toHaveBeenCalledWith('/v1/auth/register', {
                displayName: 'Test User',
                email: 'test@example.com',
                password: 'SecurePass123',
            });
            expect(user).toEqual({
                id: MOCK_AUTH_RESPONSE.userId,
                email: MOCK_AUTH_RESPONSE.email,
                displayName: MOCK_AUTH_RESPONSE.displayName,
                photoURL: null,
            });
        });

        it('should save the JWT token in localStorage', async () => {
            // Arrange
            mockAxiosInstance.post.mockResolvedValue({ data: MOCK_AUTH_RESPONSE });

            // Act
            await repository.register({
                displayName: 'Test User',
                email: 'test@example.com',
                password: 'SecurePass123',
            });

            // Assert
            expect(mockLocalStorage.setItem).toHaveBeenCalledWith(
                'auth_token',
                MOCK_AUTH_RESPONSE.token
            );
        });

        it('should throw Error when email already exists (409)', async () => {
            // Arrange
            mockAxiosInstance.post.mockRejectedValue({
                response: {
                    status: 409,
                    data: { message: "El correo electrónico 'test@example.com' ya está registrado" },
                },
            });

            // Act & Assert
            await expect(
                repository.register({
                    displayName: 'Test User',
                    email: 'test@example.com',
                    password: 'SecurePass123',
                })
            ).rejects.toThrow(/ya está registrado/);
        });
    });

    describe('signOut', () => {
        it('should remove the token from localStorage', async () => {
            // Arrange
            mockLocalStorage.setItem('auth_token', 'some-token');
            mockLocalStorage.getItem.mockReturnValue('some-token');
            mockAxiosInstance.post.mockResolvedValue({});

            // Act
            await repository.signOut();

            // Assert
            expect(mockLocalStorage.removeItem).toHaveBeenCalledWith('auth_token');
        });

        it('should call POST /v1/auth/logout with Bearer token', async () => {
            // Arrange
            mockLocalStorage.getItem.mockReturnValue('existing-token');
            mockAxiosInstance.post.mockResolvedValue({});

            // Act
            await repository.signOut();

            // Assert
            expect(mockAxiosInstance.post).toHaveBeenCalledWith(
                '/v1/auth/logout',
                null,
                { headers: { Authorization: 'Bearer existing-token' } }
            );
        });

        it('should still remove token even if backend logout fails', async () => {
            // Arrange
            mockLocalStorage.getItem.mockReturnValue('existing-token');
            mockAxiosInstance.post.mockRejectedValue(new Error('Network error'));

            // Act
            await repository.signOut();

            // Assert — token should be removed regardless
            expect(mockLocalStorage.removeItem).toHaveBeenCalledWith('auth_token');
        });
    });

    describe('signInWithProvider', () => {
        it('should throw informative error for Google provider', async () => {
            // Act & Assert
            await expect(repository.signInWithProvider('GOOGLE')).rejects.toThrow(
                'Inicio de sesión con Google no disponible temporalmente. Use email y contraseña.'
            );
        });

        it('should throw for any provider', async () => {
            await expect(repository.signInWithProvider('EMAIL')).rejects.toThrow(
                'Inicio de sesión con Google no disponible temporalmente'
            );
        });
    });

    describe('onAuthStateChanged', () => {
        it('should call callback with null when no token exists', () => {
            // Arrange
            mockLocalStorage.getItem.mockReturnValue(null as any);
            const callback = jest.fn();

            // Act
            repository.onAuthStateChanged(callback);

            // Assert — callback called synchronously with null
            expect(callback).toHaveBeenCalledWith(null);
        });

        it('should call GET /v1/auth/me and callback with user when token exists', async () => {
            // Arrange
            mockLocalStorage.getItem.mockReturnValue('valid-token');
            mockAxiosInstance.get.mockResolvedValue({ data: MOCK_USER_RESPONSE });
            const callback = jest.fn();

            // Act
            repository.onAuthStateChanged(callback);

            // Wait for the async GET /me to resolve
            await new Promise((resolve) => setTimeout(resolve, 10));

            // Assert
            expect(mockAxiosInstance.get).toHaveBeenCalledWith('/v1/auth/me', {
                headers: { Authorization: 'Bearer valid-token' },
            });
            expect(callback).toHaveBeenCalledWith({
                id: MOCK_USER_RESPONSE.userId,
                email: MOCK_USER_RESPONSE.email,
                displayName: MOCK_USER_RESPONSE.displayName,
                photoURL: null,
            });
        });

        it('should call callback with null and remove token when GET /me fails', async () => {
            // Arrange
            mockLocalStorage.getItem.mockReturnValue('expired-token');
            mockAxiosInstance.get.mockRejectedValue({ response: { status: 401 } });
            const callback = jest.fn();

            // Act
            repository.onAuthStateChanged(callback);

            // Wait for the async GET /me rejection to propagate
            await new Promise((resolve) => setTimeout(resolve, 10));

            // Assert
            expect(callback).toHaveBeenCalledWith(null);
            expect(mockLocalStorage.removeItem).toHaveBeenCalledWith('auth_token');
        });

        it('should return a noop unsubscribe function', () => {
            // Arrange
            mockLocalStorage.getItem.mockReturnValue(null as any);
            const callback = jest.fn();

            // Act
            const unsubscribe = repository.onAuthStateChanged(callback);

            // Assert — should be a function that doesn't throw
            expect(typeof unsubscribe).toBe('function');
            expect(() => unsubscribe()).not.toThrow();
        });
    });

    describe('getToken (static)', () => {
        it('should return token from localStorage', () => {
            // Arrange
            mockLocalStorage.getItem.mockReturnValue('test-token');

            // Act & Assert
            expect(ApiAuthRepository.getToken()).toBe('test-token');
        });

        it('should return null when no token exists', () => {
            // Arrange
            mockLocalStorage.getItem.mockReturnValue(null as any);

            // Act & Assert
            expect(ApiAuthRepository.getToken()).toBeNull();
        });
    });
});
