import {
  format,
  parseISO,
  startOfMonth,
  endOfMonth,
  subMonths,
  addMonths,
  isValid,
  isBefore,
  isAfter,
  differenceInDays,
  differenceInMonths,
  parse,
  isSameDay,
  isSameMonth,
  startOfYear,
  endOfYear,
} from 'date-fns';
import { es } from 'date-fns/locale';

export { es as esLocale };

export type DateInput = Date | string | number;

const defaultLocale = es;

export const formatDate = (
  date: DateInput,
  formatStr: string = 'dd/MM/yyyy'
): string => {
  const dateObj = typeof date === 'string' ? parseISO(date) : new Date(date);
  return format(dateObj, formatStr, { locale: defaultLocale });
};

export const formatDateLong = (date: DateInput): string => {
  return formatDate(date, 'dd "de" MMMM "de" yyyy');
};

export const formatDateShort = (date: DateInput): string => {
  return formatDate(date, 'dd MMM yyyy');
};

export const formatDateISO = (date: DateInput): string => {
  return formatDate(date, 'yyyy-MM-dd');
};

export const formatMonthYear = (date: DateInput): string => {
  return formatDate(date, 'MMMM yyyy');
};

export const formatMonthYearShort = (date: DateInput): string => {
  return formatDate(date, 'MMM yyyy');
};

export const formatPeriod = (date: DateInput): string => {
  return formatDate(date, 'yyyy-MM');
};

export const parseDate = (dateString: string, formatStr: string = 'yyyy-MM-dd'): Date | null => {
  try {
    const parsed = parse(dateString, formatStr, new Date());
    return isValid(parsed) ? parsed : null;
  } catch {
    return null;
  }
};

export const parseISODate = (dateString: string): Date | null => {
  try {
    const parsed = parseISO(dateString);
    return isValid(parsed) ? parsed : null;
  } catch {
    return null;
  }
};

export const toDate = (date: DateInput): Date => {
  if (date instanceof Date) return date;
  if (typeof date === 'string') return parseISO(date);
  return new Date(date);
};

export const getStartOfMonth = (date: DateInput): Date => {
  return startOfMonth(toDate(date));
};

export const getEndOfMonth = (date: DateInput): Date => {
  return endOfMonth(toDate(date));
};

export const getSubMonths = (date: DateInput, amount: number): Date => {
  return subMonths(toDate(date), amount);
};

export const getAddMonths = (date: DateInput, amount: number): Date => {
  return addMonths(toDate(date), amount);
};

export const getStartOfYear = (date: DateInput): Date => {
  return startOfYear(toDate(date));
};

export const getEndOfYear = (date: DateInput): Date => {
  return endOfYear(toDate(date));
};

export const isValidDate = (date: DateInput): boolean => {
  const dateObj = toDate(date);
  return isValid(dateObj);
};

export const isDateBefore = (date: DateInput, dateToCompare: DateInput): boolean => {
  return isBefore(toDate(date), toDate(dateToCompare));
};

export const isDateAfter = (date: DateInput, dateToCompare: DateInput): boolean => {
  return isAfter(toDate(date), toDate(dateToCompare));
};

export const isSameDayAs = (date: DateInput, dateToCompare: DateInput): boolean => {
  return isSameDay(toDate(date), toDate(dateToCompare));
};

export const isSameMonthAs = (date: DateInput, dateToCompare: DateInput): boolean => {
  return isSameMonth(toDate(date), toDate(dateToCompare));
};

export const getDaysDifference = (date: DateInput, dateToCompare: DateInput): number => {
  return differenceInDays(toDate(date), toDate(dateToCompare));
};

export const getMonthsDifference = (date: DateInput, dateToCompare: DateInput): number => {
  return differenceInMonths(toDate(date), toDate(dateToCompare));
};

export const getCurrentDate = (): Date => {
  return new Date();
};

export const getCurrentYear = (): number => {
  return new Date().getFullYear();
};

export const getCurrentMonth = (): number => {
  return new Date().getMonth();
};

export const createDate = (year: number, month: number, day: number = 1): Date => {
  return new Date(year, month, day);
};

export const periodToDate = (period: string): Date | null => {
  if (!period) return null;
  
  const [year, month] = period.split('-').map(Number);
  if (isNaN(year) || isNaN(month)) return null;
  
  return createDate(year, month - 1, 1);
};

export const dateToPeriod = (date: DateInput): string => {
  return formatPeriod(date);
};
