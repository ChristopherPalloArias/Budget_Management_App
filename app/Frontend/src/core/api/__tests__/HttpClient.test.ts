import axios from 'axios';
import HttpClient from '../HttpClient';
import { API_TIMEOUT } from '@/core/constants/app.constants';

// We need to unmock HttpClient because it's globally mocked in jest.setup.ts
jest.unmock('../HttpClient');

// Mock axios
jest.mock('axios');
const mockedAxios = axios as jest.Mocked<typeof axios>;

describe('HttpClient', () => {
  const mockAxiosInstance = {
    interceptors: {
      request: { use: jest.fn() },
      response: { use: jest.fn() },
    },
    get: jest.fn(),
    post: jest.fn(),
  };

  beforeEach(() => {
    jest.clearAllMocks();
    HttpClient.clearInstances();
    mockedAxios.create.mockReturnValue(mockAxiosInstance as any);
  });

  describe('getInstance', () => {
    it('should create and return an axios instance for a service', () => {
      const instance = HttpClient.getInstance('auth');
      
      expect(mockedAxios.create).toHaveBeenCalledWith(expect.objectContaining({
        timeout: API_TIMEOUT,
      }));
      expect(instance).toBe(mockAxiosInstance);
    });

    it('should return the same instance if called again for the same service', () => {
      const instance1 = HttpClient.getInstance('transactions');
      const instance2 = HttpClient.getInstance('transactions');
      
      expect(mockedAxios.create).toHaveBeenCalledTimes(1);
      expect(instance1).toBe(instance2);
    });

    it('should use custom timeout if provided', () => {
      HttpClient.getInstance('reports', { timeout: 5000 });
      
      expect(mockedAxios.create).toHaveBeenCalledWith(expect.objectContaining({
        timeout: 5000,
      }));
    });
  });

  describe('Interceptors', () => {
    it('should set up request and response interceptors', () => {
      HttpClient.getInstance('auth');
      
      expect(mockAxiosInstance.interceptors.request.use).toHaveBeenCalled();
      expect(mockAxiosInstance.interceptors.response.use).toHaveBeenCalled();
    });

    it('should attach token from tokenProvider if available', () => {
      const mockTokenProvider = {
        getToken: jest.fn().mockReturnValue('mock-token'),
      };
      HttpClient.setTokenProvider(mockTokenProvider as any);
      
      HttpClient.getInstance('auth');
      
      // Get the request interceptor function
      const requestInterceptor = mockAxiosInstance.interceptors.request.use.mock.calls[0][0];
      const mockConfig = { headers: {} };
      
      const resultConfig = requestInterceptor(mockConfig);
      
      expect(mockTokenProvider.getToken).toHaveBeenCalled();
      expect(resultConfig.headers.Authorization).toBe('Bearer mock-token');
    });
  });
});
