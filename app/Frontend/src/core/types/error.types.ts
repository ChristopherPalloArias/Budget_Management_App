export interface AppError {
  message: string;
  code: string;
  status?: number;
  details?: Record<string, unknown>;
}

export interface ValidationError {
  field: string;
  message: string;
}

export interface AuthError extends AppError {
  code: 'INVALID_CREDENTIALS' | 'NETWORK_ERROR' | 'USER_NOT_FOUND' | 'TOO_MANY_ATTEMPTS';
}

export interface ApiError extends AppError {
  status: number;
}

export type ErrorType = 'AUTH' | 'API' | 'VALIDATION' | 'UNKNOWN';

export interface ParsedError {
  type: ErrorType;
  message: string;
  code: string;
  status?: number;
}

export const ERROR_CODES = {
  NETWORK_ERROR: 'NETWORK_ERROR',
  INVALID_CREDENTIALS: 'INVALID_CREDENTIALS',
  USER_NOT_FOUND: 'USER_NOT_FOUND',
  UNAUTHORIZED: 'UNAUTHORIZED',
  FORBIDDEN: 'FORBIDDEN',
  NOT_FOUND: 'NOT_FOUND',
  VALIDATION_ERROR: 'VALIDATION_ERROR',
  SERVER_ERROR: 'SERVER_ERROR',
  TOO_MANY_ATTEMPTS: 'TOO_MANY_ATTEMPTS',
  UNKNOWN_ERROR: 'UNKNOWN_ERROR',
} as const;
