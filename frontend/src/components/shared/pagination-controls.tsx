import { Button } from "@/components/ui/button";

interface PaginationControlsProps {
  page: number;
  totalPages: number;
  totalItems: number;
  itemLabel: string;
  onPageChange: (page: number) => void;
}

export function PaginationControls({
  page,
  totalPages,
  totalItems,
  itemLabel,
  onPageChange,
}: PaginationControlsProps) {
  if (totalPages <= 1) {
    return null;
  }

  return (
    <div className="flex flex-col gap-3 rounded-[1.5rem] border border-border/70 bg-white/75 p-4 shadow-float sm:flex-row sm:items-center sm:justify-between">
      <div className="text-sm text-muted-foreground">
        Страница {page + 1} из {totalPages}. Всего {itemLabel}: {totalItems}
      </div>
      <div className="flex gap-3">
        <Button
          variant="outline"
          disabled={page === 0}
          onClick={() => onPageChange(Math.max(0, page - 1))}
        >
          Назад
        </Button>
        <Button
          variant="outline"
          disabled={page >= totalPages - 1}
          onClick={() => onPageChange(Math.min(totalPages - 1, page + 1))}
        >
          Вперёд
        </Button>
      </div>
    </div>
  );
}
