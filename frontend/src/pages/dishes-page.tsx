import { startTransition, useEffect, useState } from "react";
import { Plus } from "lucide-react";
import { toast } from "sonner";
import { DeleteConfirmationDialog } from "@/components/shared/delete-confirmation-dialog";
import { PageHeader } from "@/components/shared/page-header";
import { SearchInput } from "@/components/shared/search-input";
import { ErrorState, LoadingState } from "@/components/shared/status-view";
import { EmptyState } from "@/components/ui/empty-state";
import { Input } from "@/components/ui/input";
import { dishesService } from "@/services/dishes-service";
import { categoriesService } from "@/services/categories-service";
import { ingredientsService } from "@/services/ingredients-service";
import { DishCard } from "@/features/dishes/dish-card";
import { DishFormDialog } from "@/features/dishes/dish-form-dialog";
import type { Category, Dish, Ingredient } from "@/types/entities";
import { getApiErrorMessage } from "@/lib/api";

export function DishesPage() {
  const [dishes, setDishes] = useState<Dish[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [ingredients, setIngredients] = useState<Ingredient[]>([]);
  const [query, setQuery] = useState("");
  const [priceRange, setPriceRange] = useState({ min: "", max: "" });
  const [isLoading, setIsLoading] = useState(true);
  const [hasLoadedOnce, setHasLoadedOnce] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [selectedDish, setSelectedDish] = useState<Dish | null>(null);
  const [dishToDelete, setDishToDelete] = useState<Dish | null>(null);

  async function loadPage() {
    if (!hasLoadedOnce) {
      setIsLoading(true);
    }
    setError(null);
    try {
      const [dishResponse, categoryResponse, ingredientResponse] = await Promise.all([
        dishesService.search({
          query: query || undefined,
          minPrice: priceRange.min ? Number(priceRange.min) : undefined,
          maxPrice: priceRange.max ? Number(priceRange.max) : undefined,
        }),
        categoriesService.getAll(),
        ingredientsService.getAll(),
      ]);
      setDishes(dishResponse.content);
      setCategories(categoryResponse);
      setIngredients(ingredientResponse);
    } catch (err) {
      setError(getApiErrorMessage(err));
    } finally {
      setIsLoading(false);
      setHasLoadedOnce(true);
    }
  }

  useEffect(() => {
    const timeoutId = window.setTimeout(() => {
      startTransition(() => {
        void loadPage();
      });
    }, 250);

    return () => window.clearTimeout(timeoutId);
  }, [query, priceRange.min, priceRange.max]);

  async function handleSubmit(payload: Omit<Dish, "id">) {
    setIsSubmitting(true);
    try {
      if (selectedDish) {
        await dishesService.update(selectedDish.id, payload);
        toast.success("Блюдо успешно обновлено.");
      } else {
        await dishesService.create(payload);
        toast.success("Блюдо успешно создано.");
      }
      setDialogOpen(false);
      setSelectedDish(null);
      void loadPage();
    } catch (err) {
      toast.error(getApiErrorMessage(err));
    } finally {
      setIsSubmitting(false);
    }
  }

  async function handleDelete() {
    if (!dishToDelete) {
      return;
    }

    try {
      await dishesService.delete(dishToDelete.id);
      toast.success("Блюдо успешно удалено.");
      setDishToDelete(null);
      void loadPage();
    } catch (err) {
      toast.error(getApiErrorMessage(err));
    }
  }

  if (isLoading) {
    return <LoadingState label="Подготавливаем список блюд..." />;
  }

  if (error) {
    return <ErrorState message={error} onRetry={loadPage} />;
  }

  return (
    <div className="space-y-8">
      <PageHeader
        eyebrow="Меню"
        title="Блюда"
        description="Управляйте блюдами, редактируйте состав и быстро поддерживайте меню в актуальном состоянии."
        actionLabel="Создать блюдо"
        actionIcon={<Plus className="h-4 w-4" />}
        onAction={() => {
          setSelectedDish(null);
          setDialogOpen(true);
        }}
      />

      <div className="grid gap-4 rounded-[2rem] border border-border/70 bg-white/75 p-5 shadow-float lg:grid-cols-[minmax(0,1fr)_160px_160px]">
        <SearchInput
          value={query}
          onChange={setQuery}
          placeholder="Поиск по названию, категории или ингредиентам..."
        />
        <Input
          type="number"
          min="0"
          placeholder="Мин. цена"
          value={priceRange.min}
          onChange={(event) => setPriceRange({ ...priceRange, min: event.target.value })}
        />
        <Input
          type="number"
          min="0"
          placeholder="Макс. цена"
          value={priceRange.max}
          onChange={(event) => setPriceRange({ ...priceRange, max: event.target.value })}
        />
      </div>

      {dishes.length === 0 ? (
        <EmptyState
          title="Подходящие блюда не найдены"
          description="Попробуйте изменить запрос или добавьте новое блюдо в меню."
          actionLabel="Создать блюдо"
          onAction={() => {
            setSelectedDish(null);
            setDialogOpen(true);
          }}
        />
      ) : (
        <div className="grid gap-5 md:grid-cols-2 2xl:grid-cols-3">
          {dishes.map((dish) => (
            <DishCard
              key={dish.id}
              dish={dish}
              onEdit={(item) => {
                setSelectedDish(item);
                setDialogOpen(true);
              }}
              onDelete={setDishToDelete}
            />
          ))}
        </div>
      )}

      <DishFormDialog
        open={dialogOpen}
        dish={selectedDish}
        categories={categories}
        ingredients={ingredients}
        isSubmitting={isSubmitting}
        onOpenChange={(open) => {
          setDialogOpen(open);
          if (!open) {
            setSelectedDish(null);
          }
        }}
        onSubmit={handleSubmit}
      />

      <DeleteConfirmationDialog
        open={Boolean(dishToDelete)}
        title="Удалить блюдо"
        description={`Удалить ${dishToDelete?.name || "это блюдо"} из меню?`}
        onOpenChange={(open) => {
          if (!open) {
            setDishToDelete(null);
          }
        }}
        onConfirm={handleDelete}
      />
    </div>
  );
}
