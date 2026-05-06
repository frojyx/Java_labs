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
import { Label } from "@/components/ui/label";
import { Select } from "@/components/ui/select";
import { MultiValueInput } from "@/components/shared/multi-value-input";
import type { Client, Dish, Order } from "@/types/entities";

interface OrderFormDialogProps {
  open: boolean;
  order?: Order | null;
  clients: Client[];
  dishes: Dish[];
  isSubmitting?: boolean;
  onOpenChange: (open: boolean) => void;
  onSubmit: (payload: Omit<Order, "id">) => void;
}

export function OrderFormDialog({
  open,
  order,
  clients,
  dishes,
  isSubmitting,
  onOpenChange,
  onSubmit,
}: OrderFormDialogProps) {
  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");
  const [selectedClientId, setSelectedClientId] = useState("");
  const [dishNames, setDishNames] = useState<string[]>([]);

  useEffect(() => {
    setDishNames(order?.dishNames || []);

    if (order) {
      setFirstName(order.clientFirstName);
      setLastName(order.clientLastName);

      const matchedClient = clients.find(
        (client) =>
          client.firstName === order.clientFirstName && client.lastName === order.clientLastName,
      );
      setSelectedClientId(matchedClient ? String(matchedClient.id) : "");
      return;
    }

    if (clients.length > 0) {
      const defaultClient = clients[0];
      setSelectedClientId(String(defaultClient.id));
      setFirstName(defaultClient.firstName);
      setLastName(defaultClient.lastName);
    } else {
      setSelectedClientId("");
      setFirstName("");
      setLastName("");
    }
  }, [clients, order, open]);

  const isEditing = Boolean(order);

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{order ? "Редактировать заказ" : "Создать заказ"}</DialogTitle>
          {isEditing ? null : (
            <DialogDescription>
              Укажите клиента и блюда, входящие в заказ.
            </DialogDescription>
          )}
        </DialogHeader>
        <div className="grid gap-5 sm:grid-cols-2">
          {isEditing ? null : (
            <div className="space-y-2 sm:col-span-2">
              <Label htmlFor="order-client">Клиент</Label>
              <Select
                id="order-client"
                value={selectedClientId}
                placeholder="Выберите существующего клиента"
                onChange={(event) => {
                  const nextClientId = event.target.value;
                  setSelectedClientId(nextClientId);

                  const selectedClient = clients.find(
                    (client) => String(client.id) === nextClientId,
                  );
                  setFirstName(selectedClient?.firstName || "");
                  setLastName(selectedClient?.lastName || "");
                }}
              >
                {clients.map((client) => (
                  <option key={client.id} value={client.id}>
                    {client.firstName} {client.lastName}
                  </option>
                ))}
              </Select>
            </div>
          )}
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
            disabled={isSubmitting || (!isEditing && (!selectedClientId || dishNames.length === 0))}
            onClick={() =>
              onSubmit({
                clientFirstName: isEditing ? order?.clientFirstName || "" : firstName.trim(),
                clientLastName: isEditing ? order?.clientLastName || "" : lastName.trim(),
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
