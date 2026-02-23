import { useState, useCallback, useEffect } from "react"
import { Search, RotateCcw } from "lucide-react"
import { Button } from "../../../components/ui/button"
import { DatePickerWithRange } from "../../../components/ui/date-picker"
import type { ReportFilters } from "../types/report.types"
import { getLastYearRange } from "../utils/dateHelpers"
import { periodToDate } from "@/lib/date-utils"


interface DateRange {
  from?: Date
  to?: Date
}

interface ReportFiltersProps {
  filters: ReportFilters
  onFiltersChange: (filters: ReportFilters) => void
  onRefresh: () => void
  isLoading?: boolean
  isFetching?: boolean
}

const formatDateToYYYYMM = (date?: Date): string | undefined => {
  if (!date) return undefined
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  return `${year}-${month}`
}

export function ReportFilters({ 
  filters, 
  onFiltersChange, 
  onRefresh, 
  isLoading = false,
  isFetching = false 
}: ReportFiltersProps) {
  // Buffer state for local date range selection
  const [localDateRange, setLocalDateRange] = useState<any>(() => {
    // Initialize with URL filters or current month
    if (filters.startPeriod || filters.endPeriod) {
      const fromDate = periodToDate(filters.startPeriod || '')
      const toDate = periodToDate(filters.endPeriod || '')
      return {
        from: fromDate || undefined,
        to: toDate || undefined
      }
    }
    // Si no hay filtros, usar el último año por defecto
    const lastYearRange = getLastYearRange()
    return {
      from: periodToDate(lastYearRange.startPeriod) || undefined,
      to: periodToDate(lastYearRange.endPeriod) || undefined
    }
  })

  // Update local buffer when URL filters change externally
  useEffect(() => {
    if (filters.startPeriod || filters.endPeriod) {
      const fromDate = periodToDate(filters.startPeriod || '')
      const toDate = periodToDate(filters.endPeriod || '')
      setLocalDateRange({
        from: fromDate || undefined,
        to: toDate || undefined
      })
    }
  }, [filters.startPeriod, filters.endPeriod])

  const handleDateRangeChange = useCallback((range: any) => {
    setLocalDateRange(range || { from: undefined, to: undefined })
  }, [])

  const validateDateRange = useCallback((range: DateRange): boolean => {
    if (!range.from || !range.to) return false
    return range.from <= range.to
  }, [])

  const handleUpdateReports = useCallback(() => {
    if (!localDateRange || !validateDateRange(localDateRange)) {
      return
    }

    const newFilters: ReportFilters = {
      startPeriod: formatDateToYYYYMM(localDateRange.from),
      endPeriod: formatDateToYYYYMM(localDateRange.to)
    }

    onFiltersChange(newFilters)
    onRefresh()
  }, [localDateRange, validateDateRange, onFiltersChange, onRefresh])

  const handleClearFilters = useCallback(() => {
    const lastYearRange = getLastYearRange()
    setLocalDateRange({
      from: periodToDate(lastYearRange.startPeriod) || undefined,
      to: periodToDate(lastYearRange.endPeriod) || undefined
    })
    onFiltersChange({})
    setTimeout(onRefresh, 100)
  }, [onFiltersChange, onRefresh])

  return (
    <div className="flex flex-col gap-6 p-4 bg-card rounded-lg border">
      <div className="flex flex-col lg:flex-row lg:items-center lg:justify-between gap-6">
        <div className="flex-1 max-w-md">
          <DatePickerWithRange
            value={localDateRange}
            onChange={handleDateRangeChange}
            className="w-full"
          />
        </div>
        
        <div className="flex flex-col sm:flex-row gap-3 lg:gap-2">
          <Button 
            variant="outline" 
            onClick={handleClearFilters}
            disabled={isLoading || isFetching}
            className="w-full sm:w-auto"
          >
            <RotateCcw className="h-4 w-4 mr-2" />
            Limpiar Filtros
          </Button>
          
          <Button 
            onClick={handleUpdateReports}
            disabled={isLoading || isFetching}
            className="w-full sm:w-auto"
          >
            <Search className="h-4 w-4 mr-2" />
            {isFetching ? "Actualizando..." : "Consultar Reportes"}
          </Button>
        </div>
      </div>
    </div>
  )
}