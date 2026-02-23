import type { ParsedError, ErrorType, AppError } from '@/core/types/error.types';
import { ERROR_CODES } from '@/core/types/error.types';

const getErrorType = (error: unknown): ErrorType => {
  if (error && typeof error === 'object' && 'code' in error) {
    const errorCode = (error as AppError).code;
    if (errorCode === ERROR_CODES.INVALID_CREDENTIALS || 
        errorCode === ERROR_CODES.UNAUTHORIZED ||
        errorCode === ERROR_CODES.USER_NOT_FOUND) {
      return 'AUTH';
    }
  }
  
  if (error && typeof error === 'object' && 'status' in error) {
    return 'API';
  }
  
  return 'UNKNOWN';
};

const getErrorMessage = (error: unknown): string => {
  if (!error) return 'Ha ocurrido un error desconocido';
  
  if (typeof error === 'string') return error;
  
  if (typeof error === 'object') {
    const err = error as Record<string, unknown>;
    
    if ('message' in err && typeof err.message === 'string') {
      return err.message;
    }
    
    if ('data' in err && typeof err.data === 'object' && err.data !== null) {
      const data = err.data as Record<string, unknown>;
      if ('message' in data && typeof data.message === 'string') {
        return data.message;
      }
    }
  }
  
  return 'Ha ocurrido un error desconocido';
};

const getErrorCode = (error: unknown): string => {
  if (error && typeof error === 'object' && 'code' in error) {
    return (error as AppError).code;
  }
  
  if (error && typeof error === 'object' && 'response' in error) {
    const axiosError = error as { response?: { status: number } };
    if (axiosError.response) {
      switch (axiosError.response.status) {
        case 401: return ERROR_CODES.INVALID_CREDENTIALS;
        case 403: return ERROR_CODES.FORBIDDEN;
        case 404: return ERROR_CODES.NOT_FOUND;
        case 500: return ERROR_CODES.SERVER_ERROR;
      }
    }
  }
  
  return ERROR_CODES.UNKNOWN_ERROR;
};

const getErrorStatus = (error: unknown): number | undefined => {
  if (error && typeof error === 'object' && 'status' in error) {
    return (error as AppError).status;
  }
  
  if (error && typeof error === 'object' && 'response' in error) {
    const axiosError = error as { response?: { status: number } };
    return axiosError.response?.status;
  }
  
  return undefined;
};

export const parseError = (error: unknown): ParsedError => {
  return {
    type: getErrorType(error),
    message: getErrorMessage(error),
    code: getErrorCode(error),
    status: getErrorStatus(error),
  };
};

export const formatAuthError = (error: unknown): string => {
  const parsed = parseError(error);
  
  switch (parsed.code) {
    case ERROR_CODES.INVALID_CREDENTIALS:
      return 'Credenciales inválidas. Por favor verifica tu correo y contraseña.';
    case ERROR_CODES.USER_NOT_FOUND:
      return 'No existe una cuenta con este correo electrónico.';
    case ERROR_CODES.NETWORK_ERROR:
      return 'Error de conexión. Por favor verifica tu conexión a internet.';
    case ERROR_CODES.TOO_MANY_ATTEMPTS:
      return 'Demasiados intentos. Por favor espera unos minutos.';
    default:
      return parsed.message || 'Error al iniciar sesión. Intenta de nuevo.';
  }
};

export const formatApiError = (error: unknown): string => {
  const parsed = parseError(error);
  
  switch (parsed.status) {
    case 401:
      return 'Sesión expirada. Por favor inicia sesión nuevamente.';
    case 403:
      return 'No tienes permisos para realizar esta acción.';
    case 404:
      return 'Recurso no encontrado.';
    case 500:
      return 'Error del servidor. Intenta más tarde.';
    case 503:
      return 'Servicio no disponible. Intenta más tarde.';
    default:
      return parsed.message || 'Ha ocurrido un error.';
  }
};

export const isNetworkError = (error: unknown): boolean => {
  const parsed = parseError(error);
  return parsed.type === 'UNKNOWN' && !parsed.status;
};

export const isAuthError = (error: unknown): boolean => {
  return parseError(error).type === 'AUTH';
};
