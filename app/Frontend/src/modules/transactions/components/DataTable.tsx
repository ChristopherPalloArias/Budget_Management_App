import { useState, useMemo } from "react"
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "../../../components/ui/table"
import { Button } from "../../../components/ui/button"
import { Badge } from "../../../components/ui/badge"
import { DataTableToolbar } from "./DataTableToolbar"
import type { TransactionModel } from "../types/transaction.types"

const formatCurrency = (amount: number) => {
  return new Intl.NumberFormat("es-CO", {
    style: "currency",
    currency: "COP"
  }).format(amount)
}

const getCategoryColor = (category: string) => {
  const colors: Record<string, string> = {
    "Alimentación": "bg-blue-100 text-blue-800",
    "Transporte": "bg-green-100 text-green-800",
    "Vivienda": "bg-purple-100 text-purple-800",
    "Salud": "bg-red-100 text-red-800",
    "Educación": "bg-yellow-100 text-yellow-800",
    "Entretenimiento": "bg-pink-100 text-pink-800",
    "Salario": "bg-emerald-100 text-emerald-800",
    "Negocio": "bg-orange-100 text-orange-800",
    "Inversiones": "bg-indigo-100 text-indigo-800",
    "Otros": "bg-gray-100 text-gray-800",
  }
  return colors[category] || colors["Otros"]
}

interface DataTableProps {
  data: TransactionModel[]
  onCreateTransaction: () => void
}

export function DataTable({ data, onCreateTransaction }: DataTableProps) {
  const [pageIndex, setPageIndex] = useState(0)
  const [searchQuery, setSearchQuery] = useState("")
  const [selectedType, setSelectedType] = useState<Set<string>>(new Set())
  const [selectedCategory, setSelectedCategory] = useState<Set<string>>(new Set())
  const pageSize = 10

  // Get unique categories from data
  const categories = useMemo(() => {
    if (!Array.isArray(data)) return []
    const uniqueCategories = Array.from(new Set(data.map(item => item.category)))
    return uniqueCategories.sort()
  }, [data])

  // Filter data based on all criteria
  const filteredData = useMemo(() => {
    if (!Array.isArray(data)) return []
    return data.filter(transaction => {
      const matchesSearch = transaction.description.toLowerCase().includes(searchQuery.toLowerCase())
      const matchesType = selectedType.size === 0 || selectedType.has(transaction.type)
      const matchesCategory = selectedCategory.size === 0 || selectedCategory.has(transaction.category)
      return matchesSearch && matchesType && matchesCategory
    })
  }, [data, searchQuery, selectedType, selectedCategory])

  // Calculate pagination
  const startIndex = pageIndex * pageSize
  const endIndex = startIndex + pageSize
  const paginatedData = filteredData.slice(startIndex, endIndex)
  const totalPages = Math.ceil(filteredData.length / pageSize)

  const handlePageChange = (newPageIndex: number) => {
    if (newPageIndex >= 0 && newPageIndex < totalPages) {
      setPageIndex(newPageIndex)
    }
  }

  const handleResetFilters = () => {
    setSearchQuery("")
    setSelectedType(new Set())
    setSelectedCategory(new Set())
    setPageIndex(0)
  }

  // Reset page when filters change
  const handleSearchChange = (value: string) => {
    setSearchQuery(value)
    setPageIndex(0)
  }

  const handleTypeFilterChange = (values: Set<string>) => {
    setSelectedType(values)
    setPageIndex(0)
  }

  const handleCategoryFilterChange = (values: Set<string>) => {
    setSelectedCategory(values)
    setPageIndex(0)
  }

  return (
    <div className="space-y-4">
      {/* Header Section */}
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

      {/* Toolbar */}
      <DataTableToolbar
        searchQuery={searchQuery}
        onSearchChange={handleSearchChange}
        selectedType={selectedType}
        selectedCategory={selectedCategory}
        onTypeFilterChange={handleTypeFilterChange}
        onCategoryFilterChange={handleCategoryFilterChange}
        categories={categories}
        onResetFilters={handleResetFilters}
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
          {paginatedData.length > 0 ? (
            paginatedData.map((transaction) => (
              <TableRow key={transaction.id}>
                <TableCell>
                  {transaction.date.toLocaleDateString("es-CO", {
                    day: "2-digit",
                    month: "2-digit",
                    year: "numeric"
                  })}
                </TableCell>
                <TableCell className="font-medium max-w-[200px] truncate">
                  {transaction.description}
                </TableCell>
                <TableCell>
                  <Badge className={getCategoryColor(transaction.category)}>
                    {transaction.category}
                  </Badge>
                </TableCell>
                <TableCell className={`font-semibold ${
                  transaction.type === "INCOME" ? "text-green-600" : "text-red-600"
                }`}>
                  {transaction.type === "INCOME" ? "+" : "-"} {formatCurrency(transaction.amount)}
                </TableCell>
                <TableCell>
                  <Badge variant={transaction.type === "INCOME" ? "default" : "destructive"}>
                    {transaction.type === "INCOME" ? "Ingreso" : "Egreso"}
                  </Badge>
                </TableCell>
              </TableRow>
            ))
          ) : (
            <TableRow>
              <TableCell
                colSpan={5}
                className="h-24 text-center"
              >
{filteredData.length === 0 ? (
                   <>
                     No se encontraron transacciones con los filtros aplicados.{" "}
                     <span className="text-muted-foreground">
                       Intenta ajustando los filtros o crea una nueva transacción.
                     </span>
                   </>
                 ) : (
                  <>
                    No hay transacciones registradas.{" "}
                    <span className="text-muted-foreground">
                      Crea tu primera transacción usando el botón de arriba.
                    </span>
                  </>
                )}
              </TableCell>
            </TableRow>
          )}
        </TableBody>
      </Table>

      <div className="flex items-center justify-between space-x-2 py-4">
        <div className="text-sm text-muted-foreground">
          {filteredData.length} transacción(es) encontrada(s).
          {searchQuery || selectedType.size > 0 || selectedCategory.size > 0}
        </div>

        {totalPages > 1 && (
          <div className="flex items-center space-x-2">
            <Button
              variant="outline"
              size="sm"
              onClick={() => handlePageChange(pageIndex - 1)}
              disabled={pageIndex === 0}
            >
              <svg width="15" height="15" viewBox="0 0 15 15" fill="none" xmlns="http://www.w3.org/2000/svg" className="h-4 w-4">
                <path d="M8.84182 3.13514C9.04327 3.32401 9.05348 3.64042 8.86462 3.84188L5.43521 7.49991L8.86462 11.1579C9.05348 11.3594 9.04327 11.6758 8.84182 11.8647C8.64036 12.0535 8.32394 12.0433 8.13508 11.8419L4.38508 7.84188C4.20477 7.64955 4.20477 7.35027 4.38508 7.15794L8.13508 3.15794C8.32394 2.95648 8.64036 2.94628 8.84182 3.13514Z" fill="currentColor" fill-rule="evenodd" clip-rule="evenodd">
                </path>
              </svg>
            </Button>
            <div className="flex items-center gap-1">
              <span className="text-sm font-medium">
                Página {pageIndex + 1} de {totalPages}
              </span>
            </div>
            <Button
              variant="outline"
              size="sm"
              onClick={() => handlePageChange(pageIndex + 1)}
              disabled={pageIndex === totalPages - 1}
            >
              <svg width="15" height="15" viewBox="0 0 15 15" fill="none" xmlns="http://www.w3.org/2000/svg" className="h-4 w-4">
                <path d="M6.1584 3.13508C6.35985 2.94621 6.67627 2.95642 6.86514 3.15788L10.6151 7.15788C10.7954 7.3502 10.7954 7.64949 10.6151 7.84182L6.86514 11.8418C6.67627 12.0433 6.35985 12.0535 6.1584 11.8646C5.95694 11.6757 5.94673 11.3593 6.1356 11.1579L9.565 7.49985L6.1356 3.84182C5.94673 3.64036 5.95694 3.32394 6.1584 3.13508Z" fill="currentColor" fill-rule="evenodd" clip-rule="evenodd">
                </path>
              </svg>
            </Button>
          </div>
        )}
      </div>
    </div>
  )
}