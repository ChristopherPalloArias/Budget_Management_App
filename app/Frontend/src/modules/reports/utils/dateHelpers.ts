import {
  formatPeriod,
  getSubMonths,
  getCurrentDate,
} from '@/lib/date-utils';

export interface PeriodRange {
  startPeriod: string;
  endPeriod: string;
}

export const getLastYearRange = (): PeriodRange => {
  const now = getCurrentDate();
  const endMonth = now;
  const startMonth = getSubMonths(now, 11);

  return {
    startPeriod: formatPeriod(startMonth),
    endPeriod: formatPeriod(endMonth),
  };
};

