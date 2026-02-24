import { Skeleton } from "@/components/ui/skeleton";

export function ReportsPageSkeleton() {
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
    );
}
