import axios, { type AxiosInstance, AxiosError, type InternalAxiosRequestConfig, type AxiosResponse } from 'axios';

/**
 * Base HTTP Client Configuration
 * Configured with interceptors for global error handling
 */
const httpClient: AxiosInstance = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:3001',
    timeout: 15000,
    headers: {
        'Content-Type': 'application/json',
    },
});

/**
 * Request Interceptor
 */
httpClient.interceptors.request.use(
    (config: InternalAxiosRequestConfig) => {
        console.log(`[HTTP] ${config.method?.toUpperCase()} ${config.url}`);
        return config;
    },
    (error: AxiosError) => {
        console.error('[HTTP Request Error]', error);
        return Promise.reject(error);
    }
);

/**
 * Response Interceptor
 * Handle global errors (network, timeout, server errors)
 */
httpClient.interceptors.response.use(
    (response: AxiosResponse) => {
        console.log(`[HTTP Response] ${response.status} ${response.config.url}`);
        return response;
    },
    (error: AxiosError) => {
        if (!error.response) {
            console.error('[HTTP Network Error]', {
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

        console.error(`[HTTP Error ${status}]`, {
            url: error.config?.url,
            method: error.config?.method,
            data: errorData,
        });

        switch (status) {
            case 401:
                console.warn('[HTTP 401] Unauthorized - User needs to login');
                break;
            case 403:
                console.warn('[HTTP 403] Forbidden - Insufficient permissions');
                break;
            case 404:
                console.warn('[HTTP 404] Resource not found');
                break;
            case 500:
                console.error('[HTTP 500] Internal Server Error');
                break;
            case 503:
                console.error('[HTTP 503] Service Unavailable');
                break;
        }

        return Promise.reject(error);
    }
);

export default httpClient;
