import { TableCell, TableRow } from "@/components/ui/table";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Pencil, Trash2 } from "lucide-react";
import { getCategoryColor, getCategoryLabel } from "@/core/constants/categories.constants";
import { formatCurrency } from "@/shared/utils/currencyUtils";
import { formatDate } from "@/lib/date-utils";
import type { TransactionModel } from "../types/transaction.types";
import { Pencil } from "lucide-react";

interface TransactionTableRowProps {
  transaction: TransactionModel;
  onEdit: (transaction: TransactionModel) => void;
  onDelete: (transaction: TransactionModel) => void;
  isDeleting?: boolean;
}

export function TransactionTableRow({
  transaction,
  onEdit,
  onDelete,
  isDeleting = false,
}: TransactionTableRowProps) {
  const isIncome = transaction.type === "INCOME";

  return (
    <TableRow key={transaction.id}>
      <TableCell>{formatDate(transaction.date, 'dd/MM/yyyy')}</TableCell>
      <TableCell className="font-medium max-w-[200px] truncate">
        {transaction.description}
      </TableCell>
      <TableCell>
        <Badge className={getCategoryColor(transaction.category)}>
          {getCategoryLabel(transaction.category)}
        </Badge>
      </TableCell>
      <TableCell
        className={`font-semibold ${isIncome ? "text-green-600" : "text-red-600"}`}
      >
        {isIncome ? "+" : "-"} {formatCurrency(transaction.amount)}
      </TableCell>
      <TableCell>
        <Badge variant={isIncome ? "default" : "destructive"}>
          {isIncome ? "Ingreso" : "Egreso"}
        </Badge>
      </TableCell>
      {onEdit && (
        <TableCell className="text-right">
          <Button
            type="button"
            variant="ghost"
            size="icon-sm"
            onClick={onEdit}
            aria-label="Editar transacción"
          >
            <Pencil className="h-4 w-4" />
          </Button>
        </TableCell>
      )}
      <TableCell className="text-right">
        <div className="flex items-center justify-end gap-1">
          <Button
            variant="ghost"
            size="icon"
            aria-label="Editar transacción"
            onClick={() => onEdit(transaction)}
            className="h-8 w-8 text-muted-foreground hover:text-primary"
          >
            <Pencil className="h-4 w-4" />
          </Button>
          <Button
            variant="ghost"
            size="icon"
            aria-label="Eliminar transacción"
            onClick={() => onDelete(transaction)}
            disabled={isDeleting}
            className="h-8 w-8 text-muted-foreground hover:text-destructive"
          >
            <Trash2 className="h-4 w-4" />
          </Button>
        </div>
      </TableCell>
    </TableRow>
  );
}
