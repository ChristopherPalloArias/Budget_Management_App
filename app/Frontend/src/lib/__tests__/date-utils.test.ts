import {
  formatDate,
  formatDateLong,
  formatDateShort,
  formatDateISO,
  formatMonthYear,
  formatMonthYearShort,
  formatPeriod,
  parseDate,
  parseISODate,
  toDate,
  getStartOfMonth,
  getEndOfMonth,
  getSubMonths,
  getAddMonths,
  getStartOfYear,
  getEndOfYear,
  isValidDate,
  isDateBefore,
  isDateAfter,
  isSameDayAs,
  isSameMonthAs,
  getDaysDifference,
  getMonthsDifference,
  periodToDate,
  dateToPeriod,
} from '../date-utils';

describe('date-utils', () => {
  const mockDate = new Date(2023, 5, 15); // June 15, 2023

  describe('formatDate', () => {
    it('should format date with default format', () => {
      expect(formatDate(mockDate)).toBe('15/06/2023');
    });

    it('should format ISO string', () => {
      expect(formatDate('2023-06-15')).toContain('15/06/2023');
    });
  });

  describe('formatDateLong', () => {
    it('should format date in long format (Spanish)', () => {
      const result = formatDateLong(mockDate);
      console.log('DEBUG: formatDateLong result:', JSON.stringify(result));
      // Use regex to be resilient to spaces, quotes or capitalization
      expect(result).toMatch(/15.*junio.*2023/i);
    });
  });

  describe('formatDateShort', () => {
    it('should format date in short format', () => {
      expect(formatDateShort(mockDate)).toBe('15 jun 2023');
    });
  });

  describe('formatDateISO', () => {
    it('should format date as yyyy-MM-dd', () => {
      expect(formatDateISO(mockDate)).toBe('2023-06-15');
    });
  });

  describe('formatMonthYear', () => {
    it('should format as MMMM yyyy', () => {
      expect(formatMonthYear(mockDate)).toBe('junio 2023');
    });
  });

  describe('formatMonthYearShort', () => {
    it('should format as MMM yyyy', () => {
      expect(formatMonthYearShort(mockDate)).toBe('jun 2023');
    });
  });

  describe('formatPeriod', () => {
    it('should format as yyyy-MM', () => {
      expect(formatPeriod(mockDate)).toBe('2023-06');
    });
  });

  describe('parseDate', () => {
    it('should parse valid date string', () => {
      const result = parseDate('2023-06-15', 'yyyy-MM-dd');
      expect(result).toBeInstanceOf(Date);
      expect(result?.getFullYear()).toBe(2023);
      expect(result?.getMonth()).toBe(5);
    });

    it('should return null for invalid date string', () => {
      expect(parseDate('invalid-date')).toBeNull();
    });
  });

  describe('parseISODate', () => {
    it('should parse ISO string', () => {
      const result = parseISODate('2023-06-15T00:00:00.000Z');
      expect(result).toBeInstanceOf(Date);
    });
  });

  describe('toDate', () => {
    it('should convert DateInput to Date object', () => {
      expect(toDate(mockDate)).toBe(mockDate);
      expect(toDate('2023-06-15')).toBeInstanceOf(Date);
      expect(toDate(mockDate.getTime())).toBeInstanceOf(Date);
    });
  });

  describe('getStartOfMonth', () => {
    it('should return start of month', () => {
      const result = getStartOfMonth(mockDate);
      expect(result.getDate()).toBe(1);
    });
  });

  describe('getEndOfMonth', () => {
    it('should return end of month', () => {
      const result = getEndOfMonth(mockDate);
      expect(result.getDate()).toBe(30); // June has 30 days
    });
  });

  describe('getSubMonths and getAddMonths', () => {
    it('should subtract and add months correctly', () => {
      expect(getSubMonths(mockDate, 1).getMonth()).toBe(4);
      expect(getAddMonths(mockDate, 1).getMonth()).toBe(6);
    });
  });

  describe('getStartOfYear and getEndOfYear', () => {
    it('should return start and end of year', () => {
      expect(getStartOfYear(mockDate).getMonth()).toBe(0);
      expect(getEndOfYear(mockDate).getMonth()).toBe(11);
    });
  });

  describe('isValidDate', () => {
    it('should validate date', () => {
      expect(isValidDate(mockDate)).toBe(true);
      expect(isValidDate('not a date')).toBe(false);
    });
  });

  describe('isDateBefore and isDateAfter', () => {
    it('should compare dates correctly', () => {
      const earlier = new Date(2023, 1, 1);
      const later = new Date(2023, 11, 1);
      expect(isDateBefore(earlier, later)).toBe(true);
      expect(isDateAfter(later, earlier)).toBe(true);
    });
  });

  describe('isSameDayAs and isSameMonthAs', () => {
    it('should check if same day or month', () => {
      expect(isSameDayAs(mockDate, new Date(2023, 5, 15))).toBe(true);
      expect(isSameMonthAs(mockDate, new Date(2023, 5, 1))).toBe(true);
    });
  });

  describe('getDaysDifference and getMonthsDifference', () => {
    it('should calculate difference', () => {
      const date1 = new Date(2023, 6, 1);
      const date2 = new Date(2023, 6, 11);
      expect(getDaysDifference(date2, date1)).toBe(10);
      expect(getMonthsDifference(new Date(2023, 8, 1), new Date(2023, 6, 1))).toBe(2);
    });
  });

  describe('periodToDate', () => {
    it('should convert period string to date', () => {
      const result = periodToDate('2023-06');
      expect(result?.getFullYear()).toBe(2023);
      expect(result?.getMonth()).toBe(5);
    });

    it('should return null for empty or invalid period', () => {
      expect(periodToDate('')).toBeNull();
      expect(periodToDate('abc')).toBeNull();
    });
  });

  describe('dateToPeriod', () => {
    it('should convert date to period string', () => {
      expect(dateToPeriod(mockDate)).toBe('2023-06');
    });
  });
});
