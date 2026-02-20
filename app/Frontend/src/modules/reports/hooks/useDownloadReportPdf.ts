import { useState } from 'react';
import { downloadReportPdf } from '../services/reportService';
import { useUserStore } from '@/modules/auth';

/**
 * Hook para gestionar la descarga de reportes en formato PDF.
 * Maneja el estado de carga y errores por período individual.
 *
 * US-021 — Descargar Reporte de un Período como PDF
 */
export function useDownloadReportPdf() {
    const { user } = useUserStore();
    const [downloadingPeriod, setDownloadingPeriod] = useState<string | null>(null);
    const [error, setError] = useState<string | null>(null);

    const download = async (period: string) => {
        if (!user) {
            setError('Usuario no autenticado');
            return;
        }

        try {
            setDownloadingPeriod(period);
            setError(null);
            await downloadReportPdf(user.id, period);
        } catch (err) {
            const message = err instanceof Error
                ? err.message
                : 'Error al descargar el reporte PDF';
            setError(message);
            console.error('[PDF Download Error]', err);
        } finally {
            setDownloadingPeriod(null);
        }
    };

    return {
        download,
        downloadingPeriod,
        isDownloading: downloadingPeriod !== null,
        error,
    };
}
