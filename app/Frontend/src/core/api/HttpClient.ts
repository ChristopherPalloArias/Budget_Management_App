import axios, { type AxiosInstance, AxiosError, type InternalAxiosRequestConfig, type AxiosResponse } from 'axios';
import { API_TIMEOUT, HTTP_STATUS } from '@/core/constants/app.constants';

export type ServiceType = 'transactions' | 'reports';

interface HttpClientConfig {
  timeout?: number;
}

class HttpClient {
  private static instances: Map<ServiceType, AxiosInstance> = new Map();

  static getInstance(serviceType: ServiceType, config?: HttpClientConfig): AxiosInstance {
    if (this.instances.has(serviceType)) {
      return this.instances.get(serviceType)!;
    }

    const baseURL = this.getBaseURL(serviceType);
    const timeout = config?.timeout ?? API_TIMEOUT;
    
    const instance: AxiosInstance = axios.create({
      baseURL,
      timeout,
      headers: {
        'Content-Type': 'application/json',
      },
    });

    this.setupInterceptors(instance, serviceType);
    this.instances.set(serviceType, instance);
    return instance;
  }

  private static getBaseURL(serviceType: ServiceType): string {
    switch (serviceType) {
      case 'transactions':
        return import.meta.env.VITE_API_TRANSACTIONS_URL;
      case 'reports':
        return import.meta.env.VITE_API_REPORTS_URL;
      default:
        throw new Error(`Service type '${serviceType}' not supported`);
    }
  }

  private static setupInterceptors(instance: AxiosInstance, serviceType: ServiceType): void {
    instance.interceptors.request.use(
      (config: InternalAxiosRequestConfig) => {
        console.log(`[${serviceType.toUpperCase()}] ${config.method?.toUpperCase()} ${config.url}`);
        return config;
      },
      (error: AxiosError) => {
        console.error(`[${serviceType.toUpperCase()} Request Error]`, error);
        return Promise.reject(error);
      }
    );

    instance.interceptors.response.use(
      (response: AxiosResponse) => {
        console.log(`[${serviceType.toUpperCase()} Response] ${response.status} ${response.config.url}`);
        return response;
      },
      (error: AxiosError) => {
        if (!error.response) {
          console.error(`[${serviceType.toUpperCase()} Network Error]`, {
            message: error.message,
            code: error.code,
          });
          return Promise.reject({
            message: 'Error de conexión. Verifica tu internet.',
            code: 'NETWORK_ERROR',
          });
        }

        const status = error.response.status;
        const errorData = error.response.data;

        console.error(`[${serviceType.toUpperCase()} Error ${status}]`, {
          url: error.config?.url,
          method: error.config?.method,
          data: errorData,
        });

        switch (status) {
          case HTTP_STATUS.UNAUTHORIZED:
            console.warn(`[${serviceType.toUpperCase()}] Unauthorized - User needs to login`);
            break;
          case HTTP_STATUS.FORBIDDEN:
            console.warn(`[${serviceType.toUpperCase()}] Forbidden - Insufficient permissions`);
            break;
          case HTTP_STATUS.NOT_FOUND:
            console.warn(`[${serviceType.toUpperCase()}] Resource not found`);
            break;
          case HTTP_STATUS.SERVER_ERROR:
            console.error(`[${serviceType.toUpperCase()}] Internal Server Error`);
            break;
          case HTTP_STATUS.SERVICE_UNAVAILABLE:
            console.error(`[${serviceType.toUpperCase()}] Service Unavailable`);
            break;
        }

        return Promise.reject(error);
      }
    );
  }

  static clearInstances(): void {
    this.instances.clear();
  }
}

export default HttpClient;
