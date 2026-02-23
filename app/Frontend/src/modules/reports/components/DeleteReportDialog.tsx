import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog";
import { AlertCircle } from "lucide-react";

interface DeleteReportDialogProps {
  isOpen: boolean;
  onOpenChange: (open: boolean) => void;
  onConfirm: () => void;
  isPending: boolean;
  period: string;
  isProtected?: boolean;
}

/**
 * Diálogo de confirmación para eliminar reportes financieros.
 * Muestra el período del reporte a eliminar y requiere confirmación explícita.
 */
export function DeleteReportDialog({
  isOpen,
  onOpenChange,
  onConfirm,
  isPending,
  period,
  isProtected = false,
}: DeleteReportDialogProps) {
  return (
    <AlertDialog open={isOpen} onOpenChange={onOpenChange}>
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>¿Eliminar reporte?</AlertDialogTitle>
          <AlertDialogDescription>
            {isProtected ? (
              <div className="flex flex-col gap-3">
                <div className="flex items-center gap-2 text-destructive font-medium">
                  <AlertCircle className="h-4 w-4" />
                  <span>Acción restringida</span>
                </div>
                <p>
                  No se puede eliminar el reporte para el periodo <strong>{period}</strong> (periodo actual) mientras existan transacciones activas. Cierra el periodo o elimina las transacciones primero.
                </p>
              </div>
            ) : (
              <span>
                ¿Estás seguro de que deseas eliminar el reporte para el periodo <strong>{period}</strong>? Esta acción no se puede deshacer.
              </span>
            )}
          </AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel disabled={isPending}>Cancelar</AlertDialogCancel>
          {!isProtected && (
            <AlertDialogAction
              onClick={(e: React.MouseEvent<HTMLButtonElement>) => {
                e.preventDefault();
                onConfirm();
              }}
              disabled={isPending}
              className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
            >
              {isPending ? "Eliminando..." : "Confirmar"}
            </AlertDialogAction>
          )}
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  );
}
