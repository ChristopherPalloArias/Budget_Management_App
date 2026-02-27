import { parseError, formatAuthError, formatApiError, isNetworkError, isAuthError } from '../errorHandler';
import { ERROR_CODES } from '@/core/types/error.types';

describe('errorHandler', () => {
  describe('parseError', () => {
    it('should parse error with code as AUTH type', () => {
      const error = { code: ERROR_CODES.UNAUTHORIZED, message: 'Unauthorized' };
      const parsed = parseError(error);
      expect(parsed.type).toBe('AUTH');
      expect(parsed.code).toBe(ERROR_CODES.UNAUTHORIZED);
      expect(parsed.message).toBe('Unauthorized');
    });

    it('should parse axios-like error as API type', () => {
      const error = { response: { status: 404, data: { message: 'Not Found' } } };
      const parsed = parseError(error);
      expect(parsed.type).toBe('API');
      expect(parsed.status).toBe(404);
      expect(parsed.code).toBe(ERROR_CODES.NOT_FOUND);
      expect(parsed.message).toBe('Not Found');
    });

    it('should handle unknown error objects', () => {
      const error = { some: 'random error' };
      const parsed = parseError(error);
      expect(parsed.type).toBe('UNKNOWN');
      expect(parsed.message).toBe('Ha ocurrido un error desconocido');
    });

    it('should handle string errors', () => {
      const parsed = parseError('Custom Error Message');
      expect(parsed.message).toBe('Custom Error Message');
    });
  });

  describe('formatAuthError', () => {
    it('should return specific message for INVALID_CREDENTIALS', () => {
      const error = { code: ERROR_CODES.INVALID_CREDENTIALS };
      expect(formatAuthError(error)).toContain('Credenciales inválidas');
    });

    it('should return default message for unknown auth error', () => {
      const error = { code: 'SOME_OTHER_CODE', message: 'Original Message' };
      expect(formatAuthError(error)).toBe('Original Message');
    });
  });

  describe('formatApiError', () => {
    it('should return specific message for 401', () => {
      const error = { response: { status: 401 } };
      expect(formatApiError(error)).toContain('Sesión expirada');
    });

    it('should return specific message for 500', () => {
      const error = { response: { status: 500 } };
      expect(formatApiError(error)).toContain('Error del servidor');
    });
  });

  describe('isNetworkError', () => {
    it('should return true if no status and unknown type', () => {
      expect(isNetworkError({})).toBe(true);
    });

    it('should return false if there is a status', () => {
      expect(isNetworkError({ response: { status: 500 } })).toBe(false);
    });
  });

  describe('isAuthError', () => {
    it('should return true for auth codes', () => {
      expect(isAuthError({ code: ERROR_CODES.UNAUTHORIZED })).toBe(true);
    });
  });
});
