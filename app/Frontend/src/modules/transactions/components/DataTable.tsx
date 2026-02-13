import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { TablePagination } from "@/components/ui/table-pagination";
import { DataTableToolbar } from "./DataTableToolbar";
import { TransactionTableRow } from "./TransactionTableRow";
import { useDataTableLogic } from "@/shared/hooks/useDataTableLogic";
import { DEFAULT_PAGE_SIZE } from "@/core/constants/app.constants";
import type { TransactionModel } from "../types/transaction.types";

interface DataTableProps {
  data: TransactionModel[];
  onCreateTransaction: () => void;
}

const TRANSACTION_SEARCH_FIELDS: (keyof TransactionModel)[] = ["description"];

export function DataTable({ data, onCreateTransaction }: DataTableProps) {
  const {
    state,
    paginatedData,
    totalPages,
    totalFiltered,
    categories,
    setSearchQuery,
    setTypeFilter,
    setCategoryFilter,
    resetFilters,
    nextPage,
    prevPage,
  } = useDataTableLogic({
    data,
    pageSize: DEFAULT_PAGE_SIZE,
    searchFields: TRANSACTION_SEARCH_FIELDS,
  });

  const hasData = paginatedData.length > 0;
  const hasFilters = state.searchQuery || state.selectedType.size > 0 || state.selectedCategory.size > 0;

  return (
    <div className="space-y-4">
      <TableHeaderSection />
      
      <DataTableToolbar
        searchQuery={state.searchQuery}
        onSearchChange={setSearchQuery}
        selectedType={state.selectedType}
        selectedCategory={state.selectedCategory}
        onTypeFilterChange={setTypeFilter}
        onCategoryFilterChange={setCategoryFilter}
        categories={categories}
        onResetFilters={resetFilters}
        onCreateTransaction={onCreateTransaction}
      />

      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>Fecha</TableHead>
            <TableHead>Concepto</TableHead>
            <TableHead>Categoría</TableHead>
            <TableHead>Monto</TableHead>
            <TableHead>Tipo</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {hasData ? (
            paginatedData.map((transaction) => (
              <TransactionTableRow
                key={transaction.id}
                transaction={transaction}
              />
            ))
          ) : (
            <TableRow>
              <TableCell colSpan={5} className="h-24 text-center">
                {hasFilters ? (
                  <>
                    No se encontraron transacciones con los filtros aplicados.
                    <span className="text-muted-foreground">
                      {" "}Intenta ajustando los filtros o crea una nueva transacción.
                    </span>
                  </>
                ) : (
                  <>
                    No hay transacciones registradas.
                    <span className="text-muted-foreground">
                      {" "}Crea tu primera transacción usando el botón de arriba.
                    </span>
                  </>
                )}
              </TableCell>
            </TableRow>
          )}
        </TableBody>
      </Table>

      <TablePagination
        pageIndex={state.pageIndex}
        totalPages={totalPages}
        totalFiltered={totalFiltered}
        onPageChange={() => {}}
        onPrevPage={prevPage}
        onNextPage={nextPage}
      />
    </div>
  );
}

function TableHeaderSection() {
  return (
    <div className="flex items-start justify-between gap-4">
      <div className="space-y-1">
        <h2 className="text-2xl font-semibold tracking-tight">
          Listado de Transacciones
        </h2>
        <p className="text-sm text-muted-foreground">
          Gestiona y visualiza tus movimientos financieros
        </p>
      </div>
    </div>
  );
}
