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
import type { Category } from "@/types/entities";

interface CategoryFormDialogProps {
  open: boolean;
  category?: Category | null;
  isSubmitting?: boolean;
  onOpenChange: (open: boolean) => void;
  onSubmit: (payload: Omit<Category, "id">) => void;
}

export function CategoryFormDialog({
  open,
  category,
  isSubmitting,
  onOpenChange,
  onSubmit,
}: CategoryFormDialogProps) {
  const [name, setName] = useState("");

  useEffect(() => {
    setName(category?.name || "");
  }, [category, open]);

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle>{category ? "Редактировать категорию" : "Создать категорию"}</DialogTitle>
          <DialogDescription>
            Упорядочьте меню с помощью понятных и аккуратных названий категорий.
          </DialogDescription>
        </DialogHeader>
        <div className="space-y-2">
          <Label htmlFor="category-name">Название категории</Label>
          <Input
            id="category-name"
            value={name}
            onChange={(event) => setName(event.target.value)}
            placeholder="Основные блюда"
          />
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)}>
            Отмена
          </Button>
          <Button disabled={isSubmitting} onClick={() => onSubmit({ name: name.trim() })}>
            {isSubmitting ? "Сохранение..." : category ? "Сохранить изменения" : "Создать категорию"}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
