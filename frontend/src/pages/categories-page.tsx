import { useDeferredValue, useEffect, useState } from "react";
import { Pencil, Plus, Trash2 } from "lucide-react";
import { toast } from "sonner";
import { DeleteConfirmationDialog } from "@/components/shared/delete-confirmation-dialog";
import { PaginationControls } from "@/components/shared/pagination-controls";
import { PageHeader } from "@/components/shared/page-header";
import { SearchInput } from "@/components/shared/search-input";
import { ErrorState, LoadingState } from "@/components/shared/status-view";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { EmptyState } from "@/components/ui/empty-state";
import { CategoryFormDialog } from "@/features/categories/category-form-dialog";
import { getApiErrorMessage } from "@/lib/api";
import { categoriesService } from "@/services/categories-service";
import type { Category } from "@/types/entities";

export function CategoriesPage() {
  const pageSize = 9;
  const [categories, setCategories] = useState<Category[]>([]);
  const [query, setQuery] = useState("");
  const [page, setPage] = useState(0);
  const [isLoading, setIsLoading] = useState(true);
  const [hasLoadedOnce, setHasLoadedOnce] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [selectedCategory, setSelectedCategory] = useState<Category | null>(null);
  const [categoryToDelete, setCategoryToDelete] = useState<Category | null>(null);
  const deferredQuery = useDeferredValue(query);

  async function loadCategories() {
    if (!hasLoadedOnce) {
      setIsLoading(true);
    }
    setError(null);
    try {
      const data = await categoriesService.search(deferredQuery);
      setCategories(data);
    } catch (err) {
      setError(getApiErrorMessage(err));
    } finally {
      setIsLoading(false);
      setHasLoadedOnce(true);
    }
  }

  useEffect(() => {
    void loadCategories();
  }, [deferredQuery]);

  useEffect(() => {
    setPage(0);
  }, [deferredQuery]);

  useEffect(() => {
    const nextTotalPages = Math.ceil(categories.length / pageSize);
    if (nextTotalPages > 0 && page > nextTotalPages - 1) {
      setPage(nextTotalPages - 1);
    }
  }, [categories.length, page]);

  async function handleSubmit(payload: Omit<Category, "id">) {
    setIsSubmitting(true);
    try {
      if (selectedCategory) {
        await categoriesService.update(selectedCategory.id, payload);
        toast.success("Категория успешно обновлена.");
      } else {
        await categoriesService.create(payload);
        toast.success("Категория успешно создана.");
      }
      setDialogOpen(false);
      setSelectedCategory(null);
      void loadCategories();
    } catch (err) {
      toast.error(getApiErrorMessage(err));
    } finally {
      setIsSubmitting(false);
    }
  }

  async function handleDelete() {
    if (!categoryToDelete) {
      return;
    }

    try {
      await categoriesService.delete(categoryToDelete.id);
      toast.success("Категория успешно удалена.");
      setCategoryToDelete(null);
      void loadCategories();
    } catch (err) {
      toast.error(getApiErrorMessage(err));
    }
  }

  if (isLoading) {
    return <LoadingState label="Загрузка категорий..." />;
  }

  if (error) {
    return <ErrorState message={error} onRetry={loadCategories} />;
  }

  const totalPages = Math.ceil(categories.length / pageSize);
  const paginatedCategories = categories.slice(page * pageSize, page * pageSize + pageSize);

  return (
    <div className="space-y-6">
      <PageHeader
        eyebrow="Структура меню"
        title="Категории"
        actionLabel="Создать категорию"
        actionIcon={<Plus className="h-4 w-4" />}
        onAction={() => {
          setSelectedCategory(null);
          setDialogOpen(true);
        }}
      />

      <div className="rounded-[2rem] border border-border/70 bg-white/75 p-5 shadow-float">
        <SearchInput
          value={query}
          onChange={setQuery}
          placeholder="Поиск категорий..."
        />
      </div>

      {categories.length === 0 ? (
        <EmptyState
          title="Категории не найдены"
          description="Создайте категорию, чтобы сделать структуру меню понятнее."
          actionLabel="Создать категорию"
          onAction={() => {
            setSelectedCategory(null);
            setDialogOpen(true);
          }}
        />
      ) : (
        <div className="space-y-5">
          <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
            {paginatedCategories.map((category) => (
              <Card key={category.id} className="bg-white/85">
                <CardContent className="flex items-center justify-between gap-4 p-5">
                  <div className="space-y-3">
                    <Badge variant="secondary">#{category.id}</Badge>
                    <h3 className="font-display text-2xl">{category.name}</h3>
                  </div>
                  <div className="flex gap-2">
                    <Button
                      variant="ghost"
                      size="icon"
                      onClick={() => {
                        setSelectedCategory(category);
                        setDialogOpen(true);
                      }}
                    >
                      <Pencil className="h-4 w-4" />
                    </Button>
                    <Button
                      variant="ghost"
                      size="icon"
                      onClick={() => setCategoryToDelete(category)}
                    >
                      <Trash2 className="h-4 w-4" />
                    </Button>
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>

          <PaginationControls
            page={page}
            totalPages={totalPages}
            totalItems={categories.length}
            itemLabel="категорий"
            onPageChange={setPage}
          />
        </div>
      )}

      <CategoryFormDialog
        open={dialogOpen}
        category={selectedCategory}
        isSubmitting={isSubmitting}
        onOpenChange={(open) => {
          setDialogOpen(open);
          if (!open) {
            setSelectedCategory(null);
          }
        }}
        onSubmit={handleSubmit}
      />

      <DeleteConfirmationDialog
        open={Boolean(categoryToDelete)}
        title="Удалить категорию"
        description={`Удалить ${categoryToDelete?.name || "эту категорию"}?`}
        onOpenChange={(open) => {
          if (!open) {
            setCategoryToDelete(null);
          }
        }}
        onConfirm={handleDelete}
      />
    </div>
  );
}
