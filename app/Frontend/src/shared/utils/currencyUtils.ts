import { CURRENCY_CONFIG, type CurrencyCode } from "@/core/constants/app.constants";

export const formatCurrency = (
  amount: number,
  currencyCode: CurrencyCode = 'DEFAULT'
): string => {
  const config = CURRENCY_CONFIG[currencyCode];
  
  return new Intl.NumberFormat(config.locale, {
    style: 'currency',
    currency: config.code,
    currencyDisplay: config.currencyDisplay,
  }).format(amount);
};

export const formatCurrencyCompact = (
  amount: number,
  currencyCode: CurrencyCode = 'DEFAULT'
): string => {
  const config = CURRENCY_CONFIG[currencyCode];
  
  if (amount >= 1_000_000) {
    return new Intl.NumberFormat(config.locale, {
      style: 'currency',
      currency: config.code,
      notation: 'compact',
      maximumFractionDigits: 1,
    }).format(amount);
  }
  
  return formatCurrency(amount, currencyCode);
};

export const parseCurrency = (value: string): number => {
  const cleanedValue = value.replace(/[^0-9.-]/g, '');
  return parseFloat(cleanedValue) || 0;
};
