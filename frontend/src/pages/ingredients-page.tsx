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
import { IngredientFormDialog } from "@/features/ingredients/ingredient-form-dialog";
import { getApiErrorMessage } from "@/lib/api";
import { ingredientsService } from "@/services/ingredients-service";
import type { Ingredient } from "@/types/entities";

export function IngredientsPage() {
  const pageSize = 9;
  const [ingredients, setIngredients] = useState<Ingredient[]>([]);
  const [query, setQuery] = useState("");
  const [page, setPage] = useState(0);
  const [isLoading, setIsLoading] = useState(true);
  const [hasLoadedOnce, setHasLoadedOnce] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [selectedIngredient, setSelectedIngredient] = useState<Ingredient | null>(null);
  const [ingredientToDelete, setIngredientToDelete] = useState<Ingredient | null>(null);
  const deferredQuery = useDeferredValue(query);

  async function loadIngredients() {
    if (!hasLoadedOnce) {
      setIsLoading(true);
    }
    setError(null);
    try {
      const data = await ingredientsService.search(deferredQuery);
      setIngredients(data);
    } catch (err) {
      setError(getApiErrorMessage(err));
    } finally {
      setIsLoading(false);
      setHasLoadedOnce(true);
    }
  }

  useEffect(() => {
    void loadIngredients();
  }, [deferredQuery]);

  useEffect(() => {
    setPage(0);
  }, [deferredQuery]);

  useEffect(() => {
    const nextTotalPages = Math.ceil(ingredients.length / pageSize);
    if (nextTotalPages > 0 && page > nextTotalPages - 1) {
      setPage(nextTotalPages - 1);
    }
  }, [ingredients.length, page]);

  async function handleSubmit(payload: Omit<Ingredient, "id">) {
    setIsSubmitting(true);
    try {
      if (selectedIngredient) {
        await ingredientsService.update(selectedIngredient.id, payload);
        toast.success("Ингредиент успешно обновлён.");
      } else {
        await ingredientsService.create(payload);
        toast.success("Ингредиент успешно создан.");
      }
      setDialogOpen(false);
      setSelectedIngredient(null);
      void loadIngredients();
    } catch (err) {
      toast.error(getApiErrorMessage(err));
    } finally {
      setIsSubmitting(false);
    }
  }

  async function handleDelete() {
    if (!ingredientToDelete) {
      return;
    }

    try {
      await ingredientsService.delete(ingredientToDelete.id);
      toast.success("Ингредиент успешно удалён.");
      setIngredientToDelete(null);
      void loadIngredients();
    } catch (err) {
      toast.error(getApiErrorMessage(err));
    }
  }

  if (isLoading) {
    return <LoadingState label="Загрузка ингредиентов..." />;
  }

  if (error) {
    return <ErrorState message={error} onRetry={loadIngredients} />;
  }

  const totalPages = Math.ceil(ingredients.length / pageSize);
  const paginatedIngredients = ingredients.slice(page * pageSize, page * pageSize + pageSize);

  return (
    <div className="space-y-6">
      <PageHeader
        eyebrow="Каталог ингредиентов"
        title="Ингредиенты"
        actionLabel="Создать ингредиент"
        actionIcon={<Plus className="h-4 w-4" />}
        onAction={() => {
          setSelectedIngredient(null);
          setDialogOpen(true);
        }}
      />

      <div className="rounded-[2rem] border border-border/70 bg-white/75 p-5 shadow-float">
        <SearchInput
          value={query}
          onChange={setQuery}
          placeholder="Поиск ингредиентов..."
        />
      </div>

      {ingredients.length === 0 ? (
        <EmptyState
          title="Ингредиенты не найдены"
          description="Добавьте ингредиенты, чтобы использовать их в блюдах и поиске."
          actionLabel="Создать ингредиент"
          onAction={() => {
            setSelectedIngredient(null);
            setDialogOpen(true);
          }}
        />
      ) : (
        <div className="space-y-5">
          <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
            {paginatedIngredients.map((ingredient) => (
              <Card key={ingredient.id} className="bg-white/85">
                <CardContent className="flex items-center justify-between gap-4 p-5">
                  <div className="space-y-3">
                    <Badge variant="secondary">#{ingredient.id}</Badge>
                    <h3 className="font-display text-2xl">{ingredient.name}</h3>
                  </div>
                  <div className="flex gap-2">
                    <Button
                      variant="ghost"
                      size="icon"
                      onClick={() => {
                        setSelectedIngredient(ingredient);
                        setDialogOpen(true);
                      }}
                    >
                      <Pencil className="h-4 w-4" />
                    </Button>
                    <Button
                      variant="ghost"
                      size="icon"
                      onClick={() => setIngredientToDelete(ingredient)}
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
            totalItems={ingredients.length}
            itemLabel="ингредиентов"
            onPageChange={setPage}
          />
        </div>
      )}

      <IngredientFormDialog
        open={dialogOpen}
        ingredient={selectedIngredient}
        isSubmitting={isSubmitting}
        onOpenChange={(open) => {
          setDialogOpen(open);
          if (!open) {
            setSelectedIngredient(null);
          }
        }}
        onSubmit={handleSubmit}
      />

      <DeleteConfirmationDialog
        open={Boolean(ingredientToDelete)}
        title="Удалить ингредиент"
        description={`Удалить ${ingredientToDelete?.name || "этот ингредиент"}?`}
        onOpenChange={(open) => {
          if (!open) {
            setIngredientToDelete(null);
          }
        }}
        onConfirm={handleDelete}
      />
    </div>
  );
}
