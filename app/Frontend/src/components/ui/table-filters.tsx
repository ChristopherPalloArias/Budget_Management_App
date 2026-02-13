import { Plus } from "lucide-react";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";

interface FilterOption {
  label: string;
  value: string;
}

interface TableFiltersProps {
  searchQuery: string;
  onSearchChange: (value: string) => void;
  selectedFilters: Map<string, Set<string>>;
  onFilterChange: (key: string, values: Set<string>) => void;
  filterOptions: Record<string, FilterOption[]>;
  onResetFilters: () => void;
  onCreate?: () => void;
  createLabel?: string;
  searchPlaceholder?: string;
}

export function TableFilters({
  searchQuery,
  onSearchChange,
  selectedFilters,
  onFilterChange,
  filterOptions,
  onResetFilters,
  onCreate,
  createLabel = "Crear",
  searchPlaceholder = "Buscar...",
}: TableFiltersProps) {
  const hasActiveFilters = Array.from(selectedFilters.values()).some(
    (set) => set.size > 0
  );

  return (
    <div className="flex items-center justify-between gap-2 py-4">
      <div className="flex flex-1 items-center gap-2">
        <Input
          placeholder={searchPlaceholder}
          value={searchQuery}
          onChange={(e) => onSearchChange(e.target.value)}
          className="h-8 w-[150px] lg:w-[250px]"
        />
        
        {Object.entries(filterOptions).map(([key, options]) => (
          <FilterDropdown
            key={key}
            title={key}
            options={options}
            selectedValues={selectedFilters.get(key) || new Set()}
            onSelectedValuesChange={(values) => onFilterChange(key, values)}
          />
        ))}
        
        {hasActiveFilters && (
          <Button
            variant="ghost"
            size="sm"
            onClick={onResetFilters}
            className="h-8 px-2 lg:px-3"
          >
            Limpiar
          </Button>
        )}
      </div>
      
      {onCreate && (
        <Button size="sm" className="h-8 gap-1" onClick={onCreate}>
          <Plus className="h-3.5 w-3.5" />
          <span className="sr-only sm:not-sr-only sm:whitespace-nowrap">
            {createLabel}
          </span>
        </Button>
      )}
    </div>
  );
}

interface FilterDropdownProps {
  title: string;
  options: FilterOption[];
  selectedValues: Set<string>;
  onSelectedValuesChange: (values: Set<string>) => void;
}

function FilterDropdown({
  title,
  options,
  selectedValues,
  onSelectedValuesChange,
}: FilterDropdownProps) {
  const handleToggle = (value: string) => {
    const newValues = new Set(selectedValues);
    if (newValues.has(value)) {
      newValues.delete(value);
    } else {
      newValues.add(value);
    }
    onSelectedValuesChange(newValues);
  };

  return (
    <div className="relative group">
      <Button variant="outline" size="sm" className="h-8">
        {title}
        {selectedValues.size > 0 && (
          <span className="ml-1 rounded-full bg-primary px-1.5 py-0.5 text-xs text-primary-foreground">
            {selectedValues.size}
          </span>
        )}
      </Button>
      <div className="absolute top-full left-0 z-50 mt-1 hidden group-hover:block min-w-[150px] rounded-md border bg-background p-1 shadow-md">
        {options.map((option) => (
          <label
            key={option.value}
            className="flex items-center gap-2 rounded px-2 py-1.5 cursor-pointer hover:bg-accent"
          >
            <input
              type="checkbox"
              checked={selectedValues.has(option.value)}
              onChange={() => handleToggle(option.value)}
              className="rounded border-gray-300"
            />
            <span className="text-sm">{option.label}</span>
          </label>
        ))}
      </div>
    </div>
  );
}
