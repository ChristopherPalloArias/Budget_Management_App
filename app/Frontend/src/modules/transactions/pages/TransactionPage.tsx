import { DataTable } from "../components/DataTable";
import { useTransactions } from "../hooks/useTransactions";
import { useUserStore } from "@/modules/auth";
import { TransactionModalsProvider } from "../components/TransactionModalsProvider";
import { TransactionPageSkeleton } from "../components/TransactionPageSkeleton";
import { TransactionPageError } from "../components/TransactionPageError";

export function TransactionPage() {
  const { user } = useUserStore();
  const { transactions, isLoading, error: fetchError } = useTransactions();

  if (!user?.id) return null;

  if (isLoading) {
    return <TransactionPageSkeleton />;
  }

  if (fetchError) {
    return <TransactionPageError message={fetchError?.message} />;
  }

  return (
    <TransactionModalsProvider>
      <div className="space-y-6">
        <DataTable data={transactions} />
      </div>
    </TransactionModalsProvider>
  );
}
