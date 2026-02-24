import { Button } from "@/components/ui/button";

export function TransactionPageError({ message }: { message?: string }) {
    return (
        <div className="flex flex-col items-center justify-center py-8 text-center">
            <p className="text-lg font-medium">Error al cargar transacciones</p>
            <p className="text-muted-foreground">
                {message || "Inténtalo de nuevo más tarde"}
            </p>
            <Button
                variant="outline"
                onClick={() => window.location.reload()}
                className="mt-4"
            >
                Reintentar
            </Button>
        </div>
    );
}
