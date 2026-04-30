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
import type { Client } from "@/types/entities";

interface ClientFormDialogProps {
  open: boolean;
  client?: Client | null;
  isSubmitting?: boolean;
  onOpenChange: (open: boolean) => void;
  onSubmit: (payload: Omit<Client, "id">) => void;
}

export function ClientFormDialog({
  open,
  client,
  isSubmitting,
  onOpenChange,
  onSubmit,
}: ClientFormDialogProps) {
  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");

  useEffect(() => {
    setFirstName(client?.firstName || "");
    setLastName(client?.lastName || "");
  }, [client, open]);

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-lg">
        <DialogHeader>
          <DialogTitle>{client ? "Редактировать клиента" : "Создать клиента"}</DialogTitle>
          <DialogDescription>
            Ведите клиентскую базу в аккуратном и удобном виде.
          </DialogDescription>
        </DialogHeader>
        <div className="grid gap-5 sm:grid-cols-2">
          <div className="space-y-2">
            <Label htmlFor="client-first-name">Имя</Label>
            <Input
              id="client-first-name"
              value={firstName}
              onChange={(event) => setFirstName(event.target.value)}
              placeholder="Анна"
            />
          </div>
          <div className="space-y-2">
            <Label htmlFor="client-last-name">Фамилия</Label>
            <Input
              id="client-last-name"
              value={lastName}
              onChange={(event) => setLastName(event.target.value)}
              placeholder="Иванова"
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
              onSubmit({ firstName: firstName.trim(), lastName: lastName.trim() })
            }
          >
            {isSubmitting ? "Сохранение..." : client ? "Сохранить изменения" : "Создать клиента"}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
