import { useQuery } from "@tanstack/react-query"
import { getReportsSummary } from "../services/reportService"
import type { ReportFilters } from "../types/report.types"
import { useUserStore } from "@/modules/auth"

export function useGetReportsSummary(filters: Omit<ReportFilters, 'period'> = {}) {
  const { user } = useUserStore()
  
  return useQuery({
    queryKey: ["reports", "summary", filters],
    queryFn: () => {
      if (!user) throw new Error("User not authenticated")
      return getReportsSummary(user.id, filters)
    },
    enabled: !!user,
    staleTime: 1000 * 60 * 10,
    retry: 2,
  })
}