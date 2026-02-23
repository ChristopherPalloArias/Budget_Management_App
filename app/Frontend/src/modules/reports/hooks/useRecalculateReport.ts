import { useMutation, useQueryClient } from "@tanstack/react-query";
import { recalculateReport } from "../services/reportService";
import { useUserStore } from "@/modules/auth";
import { toast } from "sonner";

export function useRecalculateReport() {
  const { user } = useUserStore();
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (period: string) => {
      if (!user) throw new Error("User not authenticated");
      return recalculateReport(user.id, period);
    },
    onSuccess: (data) => {
      // Invalidar la cache de reportes para refrescar los datos
      queryClient.invalidateQueries({ queryKey: ["reports"] });
      
      toast.success("Reporte recalculado exitosamente", {
        description: `Balance actualizado: ${new Intl.NumberFormat("es-CO", {
          style: "currency",
          currency: "COP"
        }).format(data.balance)}`,
      });
    },
    onError: (error: Error) => {
      toast.error("Error al recalcular el reporte", {
        description: error.message || "Por favor, intenta de nuevo más tarde.",
      });
    },
  });
}
