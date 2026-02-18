import { Button } from "@/components/ui/button";

interface TablePaginationProps {
  pageIndex: number;
  totalPages: number;
  totalFiltered: number;
  onPageChange: (page: number) => void;
  onPrevPage: () => void;
  onNextPage: () => void;
}

const ChevronLeftIcon = () => (
  <svg width="15" height="15" viewBox="0 0 15 15" fill="none" xmlns="http://www.w3.org/2000/svg" className="h-4 w-4">
    <path d="M8.84182 3.13514C9.04327 3.32401 9.05348 3.64042 8.86462 3.84188L5.43521 7.49991L8.86462 11.1579C9.05348 11.3594 9.04327 11.6758 8.84182 11.8647C8.64036 12.0535 8.32394 12.0433 8.13508 11.8419L4.38508 7.84188C4.20477 7.64955 4.20477 7.35027 4.38508 7.15794L8.13508 3.15794C8.32394 2.95648 8.64036 2.94628 8.84182 3.13514Z" fill="currentColor" fillRule="evenodd" clipRule="evenodd" />
  </svg>
);

const ChevronRightIcon = () => (
  <svg width="15" height="15" viewBox="0 0 15 15" fill="none" xmlns="http://www.w3.org/2000/svg" className="h-4 w-4">
    <path d="M6.1584 3.13508C6.35985 2.94621 6.67627 2.95642 6.86514 3.15788L10.6151 7.15788C10.7954 7.3502 10.7954 7.64949 10.6151 7.84182L6.86514 11.8418C6.67627 12.0433 6.35985 12.0535 6.1584 11.8646C5.95694 11.6757 5.94673 11.3593 6.1356 11.1579L9.565 7.49985L6.1356 3.84182C5.94673 3.64036 5.95694 3.32394 6.1584 3.13508Z" fill="currentColor" fillRule="evenodd" clipRule="evenodd" />
  </svg>
);

export function TablePagination({
  pageIndex,
  totalPages,
  totalFiltered,
  onPageChange,
  onPrevPage,
  onNextPage,
}: TablePaginationProps) {
  if (totalPages <= 1) {
    return (
      <div className="flex items-center justify-between space-x-2 py-4">
        <div className="text-sm text-muted-foreground">
          {totalFiltered} elemento(s) encontrado(s).
        </div>
      </div>
    );
  }

  return (
    <div className="flex items-center justify-between space-x-2 py-4">
      <div className="text-sm text-muted-foreground">
        {totalFiltered} elemento(s) encontrado(s).
      </div>

      <div className="flex items-center space-x-2">
        <Button
          variant="outline"
          size="sm"
          onClick={onPrevPage}
          disabled={pageIndex === 0}
        >
          <ChevronLeftIcon />
        </Button>
        
        <div className="flex items-center gap-1">
          <span className="text-sm font-medium">
            Página {pageIndex + 1} de {totalPages}
          </span>
        </div>
        
        <Button
          variant="outline"
          size="sm"
          onClick={onNextPage}
          disabled={pageIndex === totalPages - 1}
        >
          <ChevronRightIcon />
        </Button>
      </div>
    </div>
  );
}
