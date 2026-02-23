import { useQuery } from "@tanstack/react-query"
import { getReportByPeriod } from "../services/reportService"
import type { ReportFilters } from "../types/report.types"
import { useUserStore } from "@/modules/auth"

export function useGetReportByPeriod(filters: Required<Pick<ReportFilters, 'period'>>) {
  const { user } = useUserStore()
  
  return useQuery({
    queryKey: ["reports", "byPeriod", filters.period],
    queryFn: () => {
      if (!user) throw new Error("User not authenticated")
      return getReportByPeriod(user.id, filters)
    },
    enabled: !!user && !!filters.period,
    staleTime: 1000 * 60 * 15,
    retry: 2,
  })
}