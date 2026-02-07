import { Plus } from "lucide-react"
import { Input } from "../../../components/ui/input"
import { Button } from "../../../components/ui/button"
import { DataTableFacetedFilter } from "./DataTableFacetedFilter"

interface DataTableToolbarProps {
  searchQuery: string
  onSearchChange: (value: string) => void
  selectedType: Set<string>
  selectedCategory: Set<string>
  onTypeFilterChange: (values: Set<string>) => void
  onCategoryFilterChange: (values: Set<string>) => void
  categories: string[]
  onResetFilters: () => void
}

const typeOptions = [
  { label: "Ingreso", value: "INCOME" },
  { label: "Egreso", value: "EXPENSE" },
]

export function DataTableToolbar({
  searchQuery,
  onSearchChange,
  selectedType,
  selectedCategory,
  onTypeFilterChange,
  onCategoryFilterChange,
  categories,
  onResetFilters,
}: DataTableToolbarProps) {
  const categoryOptions = categories.map((category) => ({
    label: category,
    value: category,
  }))

  const hasActiveFilters = selectedType.size > 0 || selectedCategory.size > 0

  return (
    <div className="flex items-center justify-between gap-2 py-4">
      <div className="flex flex-1 items-center gap-2">
        <Input
          placeholder="Filtrar por concepto..."
          value={searchQuery}
          onChange={(e) => onSearchChange(e.target.value)}
          className="h-8 w-[150px] lg:w-[250px]"
        />
        <DataTableFacetedFilter
          title="Tipo"
          options={typeOptions}
          selectedValues={selectedType}
          onSelectedValuesChange={onTypeFilterChange}
        />
        <DataTableFacetedFilter
          title="Categoría"
          options={categoryOptions}
          selectedValues={selectedCategory}
          onSelectedValuesChange={onCategoryFilterChange}
        />
        {hasActiveFilters && (
          <Button variant="ghost" size="sm" onClick={onResetFilters} className="h-8 px-2 lg:px-3">
            Limpiar
          </Button>
        )}
      </div>
      <Button size="sm" className="h-8 gap-1">
        <Plus className="h-3.5 w-3.5" />
        <span className="sr-only sm:not-sr-only sm:whitespace-nowrap">
          Nueva Transacción
        </span>
      </Button>
    </div>
  )
}