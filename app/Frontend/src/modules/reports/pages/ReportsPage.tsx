import { useEffect } from "react"
import { useGetReportsSummary } from "../hooks/useGetReportsSummary"
import { useReportStore } from "../store/useReportStore"
import { ReportSummaryCards } from "../components/ReportSummaryCards"
import { ReportTable } from "../components/ReportTable"
import { ReportFilters } from "../components/ReportFilters"
import { Skeleton } from "../../../components/ui/skeleton"

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
    return (
      <div className="flex-1 space-y-4 p-4 pt-6">
        <div className="flex items-center justify-between space-y-2">
          <div>
            <Skeleton className="h-8 w-64" />
            <Skeleton className="h-4 w-96 mt-2" />
          </div>
        </div>
        
        <div className="grid gap-4 md:grid-cols-3">
          {Array.from({ length: 3 }).map((_, index) => (
            <div key={index} className="rounded-lg border p-6">
              <Skeleton className="h-4 w-24 mb-2" />
              <Skeleton className="h-8 w-32" />
            </div>
          ))}
        </div>

        <div className="rounded-lg border">
          <div className="p-6">
            <Skeleton className="h-10 w-full mb-4" />
            <div className="space-y-2">
              {Array.from({ length: 5 }).map((_, index) => (
                <div key={index} className="flex items-center space-x-4 p-2">
                  <Skeleton className="h-4 w-20" />
                  <Skeleton className="h-4 w-24" />
                  <Skeleton className="h-4 w-24" />
                  <Skeleton className="h-4 w-20" />
                  <Skeleton className="h-4 w-24" />
                  <Skeleton className="h-4 w-16" />
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    )
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