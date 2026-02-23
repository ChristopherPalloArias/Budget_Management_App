import { useState } from "react"
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "../../../components/ui/table"
import { Button } from "../../../components/ui/button"
import { Trash2, RefreshCw } from "lucide-react"

import type { ReportModel } from "../types/report.types"
import { Skeleton } from "../../../components/ui/skeleton"
import { useDownloadReportPdf } from "../hooks/useDownloadReportPdf"
import { useDeleteReport } from "../hooks/useDeleteReport"
import { DeleteReportDialog } from "./DeleteReportDialog"
import { RecalculateReportDialog } from "./RecalculateReportDialog"

interface ReportTableProps {
  data: ReportModel[]
  isLoading?: boolean
}

export function ReportTable({ data, isLoading }: ReportTableProps) {
  const [pageIndex, setPageIndex] = useState(0)
  const [reportToDelete, setReportToDelete] = useState<ReportModel | null>(null)
  const [isRecalculateDialogOpen, setIsRecalculateDialogOpen] = useState(false)
  const [selectedPeriod, setSelectedPeriod] = useState("")
  const pageSize = 10
  const { download, downloadingPeriod } = useDownloadReportPdf()
  const { mutate: deleteReport, isPending } = useDeleteReport()

  // Calculate pagination
  const startIndex = pageIndex * pageSize
  const endIndex = startIndex + pageSize
  const paginatedData = data.slice(startIndex, endIndex)
  const totalPages = Math.ceil(data.length / pageSize)

  const handlePageChange = (newPageIndex: number) => {
    if (newPageIndex >= 0 && newPageIndex < totalPages) {
      setPageIndex(newPageIndex)
    }
  }

  const handleConfirmDelete = () => {
    if (reportToDelete) {
      deleteReport(reportToDelete.id, {
        onSuccess: () => setReportToDelete(null),
      })
    }
  }

  const handleRecalculateClick = (period: string) => {
    setSelectedPeriod(period)
    setIsRecalculateDialogOpen(true)
  }

  if (isLoading) {
    return (
      <div className="space-y-4">
        <div className="rounded-md border">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Periodo</TableHead>
                <TableHead>Ingresos Totales</TableHead>
                <TableHead>Gastos Totales</TableHead>
                <TableHead>Balance Neto</TableHead>
                <TableHead>Fecha de Generación</TableHead>
                <TableHead>Ahorros</TableHead>
                <TableHead className="text-center">PDF</TableHead>
                <TableHead className="text-center">Recalcular</TableHead>
                <TableHead className="text-right">Acciones</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {Array.from({ length: 5 }).map((_, index) => (
                <TableRow key={index}>
                  <TableCell><Skeleton className="h-4 w-24" /></TableCell>
                  <TableCell><Skeleton className="h-4 w-32" /></TableCell>
                  <TableCell><Skeleton className="h-4 w-32" /></TableCell>
                  <TableCell><Skeleton className="h-4 w-28" /></TableCell>
                  <TableCell><Skeleton className="h-4 w-24" /></TableCell>
                  <TableCell><Skeleton className="h-4 w-20" /></TableCell>
                  <TableCell><Skeleton className="h-8 w-20" /></TableCell>
                  <TableCell><Skeleton className="h-8 w-24" /></TableCell>
                  <TableCell><Skeleton className="h-8 w-8" /></TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </div>
      </div>
    )
  }

  return (
    <div className="space-y-4">
      <div className="rounded-md border">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Periodo</TableHead>
              <TableHead>Ingresos Totales</TableHead>
              <TableHead>Gastos Totales</TableHead>
              <TableHead>Balance Neto</TableHead>
              <TableHead>Fecha de Generación</TableHead>
              <TableHead>Ahorros</TableHead>
              <TableHead className="text-center">PDF</TableHead>
              <TableHead className="text-center">Recalcular</TableHead>
              <TableHead className="text-right">Acciones</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {paginatedData.length > 0 ? (
              paginatedData.map((report) => (
                <TableRow key={report.id}>
                  <TableCell>
                    {report.period ? new Date(report.period + '-01').toLocaleDateString("es-CO", {
                      month: "long",
                      year: "numeric"
                    }) : ''}
                  </TableCell>
                  <TableCell className="text-green-600 font-semibold">
                    {new Intl.NumberFormat("es-CO", {
                      style: "currency",
                      currency: "COP"
                    }).format(report.totalIncome)}
                  </TableCell>
                  <TableCell className="text-red-600 font-semibold">
                    {new Intl.NumberFormat("es-CO", {
                      style: "currency",
                      currency: "COP"
                    }).format(report.totalExpenses)}
                  </TableCell>
                  <TableCell className={`font-semibold ${report.balance >= 0 ? "text-green-600" : "text-red-600"
                    }`}>
                    {new Intl.NumberFormat("es-CO", {
                      style: "currency",
                      currency: "COP"
                    }).format(report.balance)}
                  </TableCell>
                  <TableCell className="text-muted-foreground">
                    {report.createdAt.toLocaleDateString("es-CO", {
                      day: "2-digit",
                      month: "2-digit",
                      year: "numeric"
                    })}
                  </TableCell>
                  <TableCell className={`font-medium ${report.savings > 0 ? "text-blue-600" : "text-gray-600"
                    }`}>
                    {new Intl.NumberFormat("es-CO", {
                      style: "currency",
                      currency: "COP"
                    }).format(report.savings)}
                  </TableCell>
                  <TableCell className="text-center">
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => download(report.period)}
                      disabled={downloadingPeriod === report.period}
                      className="gap-1.5"
                    >
                      {downloadingPeriod === report.period ? (
                        <>
                          <svg className="h-4 w-4 animate-spin" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                            <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                            <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                          </svg>
                          Descargando…
                        </>
                      ) : (
                        <>
                          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="h-4 w-4">
                            <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
                            <polyline points="7 10 12 15 17 10"></polyline>
                            <line x1="12" y1="15" x2="12" y2="3"></line>
                          </svg>
                          PDF
                        </>
                      )}
                    </Button>
                  </TableCell>
                  <TableCell className="text-center">
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => handleRecalculateClick(report.period)}
                      className="gap-2"
                    >
                      <RefreshCw className="h-4 w-4" />
                      Recalcular
                    </Button>
                  </TableCell>
                  <TableCell className="text-right">
                    <Button
                      variant="ghost"
                      size="icon"
                      aria-label="Delete"
                      onClick={() => setReportToDelete(report)}
                      disabled={isPending}
                      className="text-muted-foreground hover:text-destructive"
                    >
                      <Trash2 className="h-4 w-4" />
                    </Button>
                  </TableCell>
                </TableRow>
              ))
            ) : (
              <TableRow>
                <TableCell colSpan={9} className="h-24 text-center">
                  No se encontraron reportes en el período seleccionado.
                  <span className="text-muted-foreground">
                    {" "}Intenta ajustando los filtros de fecha.
                  </span>
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </div>

      <div className="flex items-center justify-between space-x-2 py-4">
        <div className="text-sm text-muted-foreground">
          {data.length} reporte(s) encontrado(s).
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

      <DeleteReportDialog
        isOpen={!!reportToDelete}
        onOpenChange={(open) => !open && setReportToDelete(null)}
        onConfirm={handleConfirmDelete}
        isPending={isPending}
        period={reportToDelete?.period || ""}
      />
      <RecalculateReportDialog
        open={isRecalculateDialogOpen}
        onOpenChange={setIsRecalculateDialogOpen}
        initialPeriod={selectedPeriod}
      />
    </div>
  )
}
