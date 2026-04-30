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
import type { Ingredient } from "@/types/entities";

interface IngredientFormDialogProps {
  open: boolean;
  ingredient?: Ingredient | null;
  isSubmitting?: boolean;
  onOpenChange: (open: boolean) => void;
  onSubmit: (payload: Omit<Ingredient, "id">) => void;
}

export function IngredientFormDialog({
  open,
  ingredient,
  isSubmitting,
  onOpenChange,
  onSubmit,
}: IngredientFormDialogProps) {
  const [name, setName] = useState("");

  useEffect(() => {
    setName(ingredient?.name || "");
  }, [ingredient, open]);

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle>{ingredient ? "Редактировать ингредиент" : "Создать ингредиент"}</DialogTitle>
          <DialogDescription>
            Поддерживайте аккуратный каталог ингредиентов для блюд и поиска.
          </DialogDescription>
        </DialogHeader>
        <div className="space-y-2">
          <Label htmlFor="ingredient-name">Название ингредиента</Label>
          <Input
            id="ingredient-name"
            value={name}
            onChange={(event) => setName(event.target.value)}
            placeholder="Пармезан"
          />
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)}>
            Отмена
          </Button>
          <Button disabled={isSubmitting} onClick={() => onSubmit({ name: name.trim() })}>
            {isSubmitting ? "Сохранение..." : ingredient ? "Сохранить изменения" : "Создать ингредиент"}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
