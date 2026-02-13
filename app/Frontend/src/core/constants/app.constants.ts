export const DEFAULT_PAGE_SIZE = 10;
export const DEFAULT_PAGE_INDEX = 0;

export const API_TIMEOUT = 15000;

export const CURRENCY_CONFIG = {
  DEFAULT: {
    code: 'COP',
    locale: 'es-CO',
    currencyDisplay: 'symbol',
  },
  USD: {
    code: 'USD',
    locale: 'en-US',
    currencyDisplay: 'symbol',
  },
} as const;

export type CurrencyCode = keyof typeof CURRENCY_CONFIG;

export interface CurrencyConfig {
  code: string;
  locale: string;
  currencyDisplay: 'symbol' | 'name' | 'code';
}

export const getCurrencyConfig = (code: CurrencyCode = 'DEFAULT'): CurrencyConfig => {
  return CURRENCY_CONFIG[code];
};

export const PAGINATION = {
  MIN_PAGE_SIZE: 5,
  MAX_PAGE_SIZE: 100,
  DEFAULT_SIZES: [5, 10, 25, 50],
} as const;

export const API_ENDPOINTS = {
  TRANSACTIONS: '/v1/transactions',
  REPORTS: '/v1/reports',
  USERS: '/v1/users',
} as const;

export const HTTP_STATUS = {
  OK: 200,
  CREATED: 201,
  NO_CONTENT: 204,
  BAD_REQUEST: 400,
  UNAUTHORIZED: 401,
  FORBIDDEN: 403,
  NOT_FOUND: 404,
  SERVER_ERROR: 500,
  SERVICE_UNAVAILABLE: 503,
} as const;
