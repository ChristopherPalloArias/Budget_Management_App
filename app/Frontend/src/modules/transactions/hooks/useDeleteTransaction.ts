import { useMutation, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";
import { deleteTransaction } from "../services/transactionService";

/**
 * Hook para gestionar la eliminación de transacciones.
 * Invalida las queries de transacciones tras una eliminación exitosa.
 */
export function useDeleteTransaction() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: (id: string | number) => deleteTransaction(id),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ["transactions"] });
            queryClient.invalidateQueries({ queryKey: ["reports"] });
            toast.success("Transacción eliminada con éxito");
        },
        onError: (error: any) => {
            const status = error?.response?.status;

            if (status === 404) {
                toast.error("La transacción que intentas eliminar no existe o ya fue eliminada.");
            } else if (status === 403) {
                toast.error("No tienes permisos para eliminar esta transacción.");
            } else {
                toast.error("No se pudo eliminar la transacción. Por favor, intenta de nuevo más tarde.");
            }
        },
    });
}
