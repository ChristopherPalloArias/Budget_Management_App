import { useState } from "react";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "../../../components/ui/dialog";
import { Button } from "../../../components/ui/button";
import { Label } from "../../../components/ui/label";
import { Input } from "../../../components/ui/input";
import { useRecalculateReport } from "../hooks/useRecalculateReport";
import { Loader2 } from "lucide-react";

interface RecalculateReportDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  initialPeriod?: string;
}

export function RecalculateReportDialog({
  open,
  onOpenChange,
  initialPeriod = "",
}: RecalculateReportDialogProps) {
  const [period, setPeriod] = useState(initialPeriod);
  const { mutate: recalculate, isPending } = useRecalculateReport();

  const handleRecalculate = () => {
    if (!period) return;
    
    recalculate(period, {
      onSuccess: () => {
        onOpenChange(false);
        setPeriod("");
      },
    });
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[425px]">
        <DialogHeader>
          <DialogTitle>Recalcular Reporte Financiero</DialogTitle>
          <DialogDescription>
            Selecciona el período del reporte que deseas recalcular. El sistema actualizará los totales basándose en las transacciones actuales.
          </DialogDescription>
        </DialogHeader>
        
        <div className="grid gap-4 py-4">
          <div className="grid gap-2">
            <Label htmlFor="period">Período (YYYY-MM)</Label>
            <Input
              id="period"
              type="month"
              value={period}
              onChange={(e) => setPeriod(e.target.value)}
              placeholder="2025-11"
              disabled={isPending}
            />
            <p className="text-sm text-muted-foreground">
              Ejemplo: 2025-11 para Noviembre 2025
            </p>
          </div>
        </div>

        <DialogFooter>
          <Button
            variant="outline"
            onClick={() => onOpenChange(false)}
            disabled={isPending}
          >
            Cancelar
          </Button>
          <Button
            onClick={handleRecalculate}
            disabled={!period || isPending}
          >
            {isPending && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
            {isPending ? "Recalculando..." : "Recalcular"}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
