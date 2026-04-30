import { useEffect, useState } from "react";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Select } from "@/components/ui/select";
import { MultiValueInput } from "@/components/shared/multi-value-input";
import type { Category, Dish, Ingredient } from "@/types/entities";

interface DishFormDialogProps {
  open: boolean;
  dish?: Dish | null;
  categories: Category[];
  ingredients: Ingredient[];
  isSubmitting?: boolean;
  onOpenChange: (open: boolean) => void;
  onSubmit: (payload: Omit<Dish, "id">) => void;
}

const initialForm = {
  name: "",
  category: "",
  ingredients: [] as string[],
  price: "",
  weight: "",
};

export function DishFormDialog({
  open,
  dish,
  categories,
  ingredients,
  isSubmitting,
  onOpenChange,
  onSubmit,
}: DishFormDialogProps) {
  const [form, setForm] = useState(initialForm);

  useEffect(() => {
    if (dish) {
      setForm({
        name: dish.name,
        category: dish.category,
        ingredients: dish.ingredients,
        price: String(dish.price),
        weight: String(dish.weight),
      });
      return;
    }

    setForm(initialForm);
  }, [dish, open]);

  function handleSubmit() {
    onSubmit({
      name: form.name.trim(),
      category: form.category.trim(),
      ingredients: form.ingredients,
      price: Number(form.price),
      weight: Number(form.weight),
    });
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{dish ? "Редактировать блюдо" : "Создать блюдо"}</DialogTitle>
          <DialogDescription>
            Заполните категорию, цену, вес и ингредиенты блюда.
          </DialogDescription>
        </DialogHeader>
        <div className="grid gap-5 md:grid-cols-2">
          <div className="space-y-2 md:col-span-2">
            <Label htmlFor="dish-name">Название блюда</Label>
            <Input
              id="dish-name"
              value={form.name}
              onChange={(event) => setForm({ ...form, name: event.target.value })}
              placeholder="Паста Карбонара"
            />
          </div>
          <div className="space-y-2">
            <Label htmlFor="dish-category">Категория</Label>
            <Select
              id="dish-category"
              value={form.category}
              onChange={(event) => setForm({ ...form, category: event.target.value })}
              placeholder="Выберите категорию"
            >
              {categories.map((category) => (
                <option key={category.id} value={category.name}>
                  {category.name}
                </option>
              ))}
            </Select>
          </div>
          <div className="space-y-2">
            <Label htmlFor="dish-price">Цена</Label>
            <Input
              id="dish-price"
              type="number"
              min="0"
              step="0.01"
              value={form.price}
              onChange={(event) => setForm({ ...form, price: event.target.value })}
              placeholder="18.50"
            />
          </div>
          <div className="space-y-2 md:col-span-2">
            <Label htmlFor="dish-weight">Вес в граммах</Label>
            <Input
              id="dish-weight"
              type="number"
              min="1"
              value={form.weight}
              onChange={(event) => setForm({ ...form, weight: event.target.value })}
              placeholder="340"
            />
          </div>
          <div className="md:col-span-2">
            <MultiValueInput
              label="Ингредиенты"
              values={form.ingredients}
              suggestions={ingredients.map((item) => item.name)}
              placeholder="Введите ингредиент и нажмите Enter"
              onChange={(values) => setForm({ ...form, ingredients: values })}
            />
          </div>
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)}>
            Отмена
          </Button>
          <Button onClick={handleSubmit} disabled={isSubmitting}>
            {isSubmitting ? "Сохранение..." : dish ? "Сохранить изменения" : "Создать блюдо"}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
