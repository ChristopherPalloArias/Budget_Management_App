import { create } from 'zustand';
import { devtools } from 'zustand/middleware';
import type { ReportModel, ReportFilters } from '../types/report.types';
import { getLastYearRange } from '../utils/dateHelpers';

interface ReportState {
    currentReport: ReportModel | null;
    filters: ReportFilters;

    setCurrentReport: (report: ReportModel | null) => void;
    setFilters: (filters: ReportFilters) => void;
    clearReportData: () => void;
    reset: () => void;
}

// Obtener rango por defecto del último año
const defaultFilters = getLastYearRange();

const getInitialState = () => ({
    currentReport: null as ReportModel | null,
    filters: {
        startPeriod: defaultFilters.startPeriod,
        endPeriod: defaultFilters.endPeriod,
    } as ReportFilters,
});

export const useReportStore = create<ReportState>()(
    devtools(
        (set) => ({
            ...getInitialState(),

            setCurrentReport: (report) =>
                set({ currentReport: report }, false, 'setCurrentReport'),

            setFilters: (filters) =>
                set({ filters: { ...filters } }, false, 'setFilters'),

            clearReportData: () =>
                set(
                    getInitialState(),
                    false,
                    'clearReportData'
                ),

            /**
             * Resetea el store completo a su estado inicial.
             * Debe invocarse durante el logout para evitar phantom data.
             */
            reset: () =>
                set(getInitialState(), false, 'reset'),
        }),
        { name: 'Report Store' }
    )
);