import { toSafeNumber, formatCurrency, formatCurrencySimple, formatNumber, isValidNumber } from '../currency';

describe('currency internal utility', () => {
  describe('toSafeNumber', () => {
    it('should convert null/undefined/empty to 0', () => {
      expect(toSafeNumber(null)).toBe(0);
      expect(toSafeNumber(undefined)).toBe(0);
      expect(toSafeNumber('')).toBe(0);
    });

    it('should return number for valid numeric values', () => {
      expect(toSafeNumber(100)).toBe(100);
      expect(toSafeNumber('100.5')).toBe(100.5);
    });

    it('should return 0 for NaN/Infinity', () => {
      expect(toSafeNumber(NaN)).toBe(0);
      expect(toSafeNumber(Infinity)).toBe(0);
      expect(toSafeNumber('abc')).toBe(0);
    });
  });

  describe('formatCurrency', () => {
    it('should format as COP by default', () => {
      const result = formatCurrency(1000);
      expect(result).toMatch(/\$|COP/);
      expect(result).toMatch(/1/);
      expect(result).toMatch(/000/);
    });

    it('should handle custom locale and currency', () => {
      const result = formatCurrency(1000, 'en-US', 'USD');
      expect(result).toMatch(/\$1,000/);
    });
  });

  describe('formatCurrencySimple', () => {
    it('should format with symbol and thousands separator', () => {
      const result = formatCurrencySimple(1234);
      expect(result).toMatch(/\$ 1.234/);
    });
  });

  describe('formatNumber', () => {
    it('should format with thousands separator', () => {
      expect(formatNumber(1234)).toMatch(/1.234/);
    });
  });

  describe('isValidNumber', () => {
    it('should return true for valid numbers', () => {
      expect(isValidNumber(100)).toBe(true);
      expect(isValidNumber('100')).toBe(true);
    });

    it('should return false for invalid numbers', () => {
      expect(isValidNumber('abc')).toBe(false);
      expect(isValidNumber(null)).toBe(true); // toSafeNumber(null) is 0, which is valid
    });
  });
});
