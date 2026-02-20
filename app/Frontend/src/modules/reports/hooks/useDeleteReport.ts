import { useMutation, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";
import { deleteReport } from "../services/reportService";

export function useDeleteReport() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: string | number) => deleteReport(id),
    onSuccess: () => {
      // Invalida la query de reportes para refrescar la lista
      queryClient.invalidateQueries({ queryKey: ["reports"] });
      toast.success("Reporte eliminado con éxito");
    },
    onError: (error: any) => {
      const status = error?.response?.status;
      
      if (status === 404) {
        toast.error("El reporte que intentas eliminar no existe o ya fue eliminado.");
      } else if (status === 422) {
        toast.error("No se puede eliminar el reporte para el periodo actual mientras existan transacciones activas.");
      } else {
        toast.error("No se pudo eliminar el reporte. Por favor, intenta de nuevo más tarde.");
      }
    },
  });
}
