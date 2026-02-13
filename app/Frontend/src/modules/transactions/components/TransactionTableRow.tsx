import { TableCell, TableRow } from "@/components/ui/table";
import { Badge } from "@/components/ui/badge";
import { getCategoryColor, getCategoryLabel } from "@/core/constants/categories.constants";
import { formatCurrency } from "@/shared/utils/currencyUtils";
import type { TransactionModel } from "../types/transaction.types";

interface TransactionTableRowProps {
  transaction: TransactionModel;
}

const formatDate = (date: Date): string => {
  return date.toLocaleDateString("es-CO", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
  });
};

export function TransactionTableRow({ transaction }: TransactionTableRowProps) {
  const isIncome = transaction.type === "INCOME";

  return (
    <TableRow key={transaction.id}>
      <TableCell>{formatDate(transaction.date)}</TableCell>
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
    </TableRow>
  );
}
