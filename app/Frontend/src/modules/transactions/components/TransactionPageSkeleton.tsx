import { Skeleton } from "@/components/ui/skeleton";

export function TransactionPageSkeleton() {
    return (
        <div className="space-y-4">
            <div className="flex items-center justify-between">
                <div>
                    <Skeleton className="h-8 w-48" />
                    <Skeleton className="mt-2 h-4 w-64" />
                </div>
                <Skeleton className="h-10 w-40" />
            </div>
            <div className="space-y-2">
                {[...Array(5)].map((_, i) => (
                    <div key={i} className="flex items-center space-x-4">
                        <Skeleton className="h-12 w-24" />
                        <Skeleton className="h-12 flex-1" />
                        <Skeleton className="h-12 w-24" />
                        <Skeleton className="h-12 w-32" />
                        <Skeleton className="h-12 w-20" />
                        <Skeleton className="h-12 w-20" />
                    </div>
                ))}
            </div>
        </div>
    );
}
