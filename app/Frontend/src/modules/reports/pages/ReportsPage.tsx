import { useEffect } from "react"
import { useGetReportsSummary } from "../hooks/useGetReportsSummary"
import { useReportStore } from "../store/useReportStore"
import { ReportSummaryCards } from "../components/ReportSummaryCards"
import { ReportTable } from "../components/ReportTable"
import { ReportFilters } from "../components/ReportFilters"
import { ReportsPageSkeleton } from "../components/ReportsPageSkeleton"

export function ReportsPage() {
  const { filters, setFilters } = useReportStore()
  const { data: reportsData, isLoading, isFetching, refetch } = useGetReportsSummary({
    startPeriod: filters.startPeriod,
    endPeriod: filters.endPeriod,
  })

  useEffect(() => {
    refetch()
  }, [filters, refetch])

  const handleFiltersChange = (newFilters: any) => {
    setFilters(newFilters)
  }

  const handleRefresh = () => {
    refetch()
  }

  if (isLoading && !reportsData) {
    return <ReportsPageSkeleton />
  }

  return (
    <div className="flex-1 space-y-4 p-4 pt-6">
      {/* Header Section */}
      <div className="flex items-start justify-between gap-4">
        <div className="space-y-1">
          <h2 className="text-3xl font-bold tracking-tight">
            Reportes Financieros
          </h2>
          <p className="text-muted-foreground">
            Visualiza y analiza tus reportes financieros consolidados por período
          </p>
        </div>
      </div>

      {/* Filters Section */}
      <div className="rounded-lg border bg-card p-4">
        <h3 className="text-lg font-semibold mb-4">Filtros de Búsqueda</h3>
        <ReportFilters
          filters={filters}
          onFiltersChange={handleFiltersChange}
          onRefresh={handleRefresh}
          isLoading={isLoading}
          isFetching={isFetching}
        />
      </div>

      {/* Summary Cards */}
      {reportsData && (
        <ReportSummaryCards
          balance={reportsData.balance}
          totalIncome={reportsData.totalIncome}
          totalExpenses={reportsData.totalExpenses}
          isLoading={isLoading}
        />
      )}

      {/* Reports Table */}
      <div className="rounded-lg border bg-card p-4">
        <div className="mb-4">
          <h3 className="text-lg font-semibold">Historial de Reportes</h3>
          <p className="text-sm text-muted-foreground">
            Listado de todos tus reportes generados en el período seleccionado
          </p>
        </div>
        <ReportTable
          data={reportsData?.reports || []}
          isLoading={isLoading}
        />
      </div>
    </div>
  )
}