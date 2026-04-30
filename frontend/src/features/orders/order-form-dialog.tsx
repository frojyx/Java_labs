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
import { MultiValueInput } from "@/components/shared/multi-value-input";
import type { Dish, Order } from "@/types/entities";

interface OrderFormDialogProps {
  open: boolean;
  order?: Order | null;
  dishes: Dish[];
  isSubmitting?: boolean;
  onOpenChange: (open: boolean) => void;
  onSubmit: (payload: Omit<Order, "id">) => void;
}

export function OrderFormDialog({
  open,
  order,
  dishes,
  isSubmitting,
  onOpenChange,
  onSubmit,
}: OrderFormDialogProps) {
  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");
  const [dishNames, setDishNames] = useState<string[]>([]);

  useEffect(() => {
    setFirstName(order?.clientFirstName || "");
    setLastName(order?.clientLastName || "");
    setDishNames(order?.dishNames || []);
  }, [order, open]);

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{order ? "Редактировать заказ" : "Создать заказ"}</DialogTitle>
          <DialogDescription>
            Укажите клиента и блюда, входящие в заказ.
          </DialogDescription>
        </DialogHeader>
        <div className="grid gap-5 sm:grid-cols-2">
          <div className="space-y-2">
            <Label htmlFor="order-first-name">Имя клиента</Label>
            <Input
              id="order-first-name"
              value={firstName}
              onChange={(event) => setFirstName(event.target.value)}
              placeholder="Анна"
            />
          </div>
          <div className="space-y-2">
            <Label htmlFor="order-last-name">Фамилия клиента</Label>
            <Input
              id="order-last-name"
              value={lastName}
              onChange={(event) => setLastName(event.target.value)}
              placeholder="Петрова"
            />
          </div>
          <div className="sm:col-span-2">
            <MultiValueInput
              label="Блюда"
              values={dishNames}
              suggestions={dishes.map((dish) => dish.name)}
              placeholder="Введите блюдо и нажмите Enter"
              onChange={setDishNames}
            />
          </div>
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)}>
            Отмена
          </Button>
          <Button
            disabled={isSubmitting}
            onClick={() =>
              onSubmit({
                clientFirstName: firstName.trim(),
                clientLastName: lastName.trim(),
                dishNames,
              })
            }
          >
            {isSubmitting ? "Сохранение..." : order ? "Сохранить изменения" : "Создать заказ"}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
