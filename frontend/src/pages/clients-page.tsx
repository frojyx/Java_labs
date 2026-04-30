import { useDeferredValue, useEffect, useState } from "react";
import { Pencil, Plus, Trash2 } from "lucide-react";
import { toast } from "sonner";
import { DeleteConfirmationDialog } from "@/components/shared/delete-confirmation-dialog";
import { PageHeader } from "@/components/shared/page-header";
import { SearchInput } from "@/components/shared/search-input";
import { ErrorState, LoadingState } from "@/components/shared/status-view";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { EmptyState } from "@/components/ui/empty-state";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { ClientFormDialog } from "@/features/clients/client-form-dialog";
import { getApiErrorMessage } from "@/lib/api";
import { clientsService } from "@/services/clients-service";
import type { Client } from "@/types/entities";

export function ClientsPage() {
  const [clients, setClients] = useState<Client[]>([]);
  const [query, setQuery] = useState("");
  const [isLoading, setIsLoading] = useState(true);
  const [hasLoadedOnce, setHasLoadedOnce] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [selectedClient, setSelectedClient] = useState<Client | null>(null);
  const [clientToDelete, setClientToDelete] = useState<Client | null>(null);
  const deferredQuery = useDeferredValue(query);

  async function loadClients() {
    if (!hasLoadedOnce) {
      setIsLoading(true);
    }
    setError(null);
    try {
      const data = await clientsService.search(deferredQuery);
      setClients(data);
    } catch (err) {
      setError(getApiErrorMessage(err));
    } finally {
      setIsLoading(false);
      setHasLoadedOnce(true);
    }
  }

  useEffect(() => {
    void loadClients();
  }, [deferredQuery]);

  async function handleSubmit(payload: Omit<Client, "id">) {
    setIsSubmitting(true);
    try {
      if (selectedClient) {
        await clientsService.update(selectedClient.id, payload);
        toast.success("Клиент успешно обновлён.");
      } else {
        await clientsService.create(payload);
        toast.success("Клиент успешно создан.");
      }
      setDialogOpen(false);
      setSelectedClient(null);
      void loadClients();
    } catch (err) {
      toast.error(getApiErrorMessage(err));
    } finally {
      setIsSubmitting(false);
    }
  }

  async function handleDelete() {
    if (!clientToDelete) {
      return;
    }

    try {
      await clientsService.delete(clientToDelete.id);
      toast.success("Клиент успешно удалён.");
      setClientToDelete(null);
      void loadClients();
    } catch (err) {
      toast.error(getApiErrorMessage(err));
    }
  }

  if (isLoading) {
    return <LoadingState label="Загрузка клиентов..." />;
  }

  if (error) {
    return <ErrorState message={error} onRetry={loadClients} />;
  }

  return (
    <div className="space-y-8">
      <PageHeader
        eyebrow="Клиентская база"
        title="Клиенты"
        description="Управляйте списком клиентов, быстро ищите записи и обновляйте данные."
        actionLabel="Создать клиента"
        actionIcon={<Plus className="h-4 w-4" />}
        onAction={() => {
          setSelectedClient(null);
          setDialogOpen(true);
        }}
      />

      <div className="rounded-[2rem] border border-border/70 bg-white/75 p-5 shadow-float">
        <SearchInput
          value={query}
          onChange={setQuery}
          placeholder="Поиск по имени или фамилии..."
        />
      </div>

      {clients.length === 0 ? (
        <EmptyState
          title="Клиенты не найдены"
          description="Добавьте клиента, чтобы начать формировать клиентскую базу."
          actionLabel="Создать клиента"
          onAction={() => {
            setSelectedClient(null);
            setDialogOpen(true);
          }}
        />
      ) : (
        <Card className="bg-white/85">
          <CardContent className="p-0">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>ID</TableHead>
                  <TableHead>Имя</TableHead>
                  <TableHead>Фамилия</TableHead>
                  <TableHead className="text-right">Действия</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {clients.map((client) => (
                  <TableRow key={client.id}>
                    <TableCell>#{client.id}</TableCell>
                    <TableCell className="font-medium">{client.firstName}</TableCell>
                    <TableCell>{client.lastName}</TableCell>
                    <TableCell className="text-right">
                      <div className="flex justify-end gap-2">
                        <Button
                          variant="ghost"
                          size="icon"
                          onClick={() => {
                            setSelectedClient(client);
                            setDialogOpen(true);
                          }}
                        >
                          <Pencil className="h-4 w-4" />
                        </Button>
                        <Button
                          variant="ghost"
                          size="icon"
                          onClick={() => setClientToDelete(client)}
                        >
                          <Trash2 className="h-4 w-4" />
                        </Button>
                      </div>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </CardContent>
        </Card>
      )}

      <ClientFormDialog
        open={dialogOpen}
        client={selectedClient}
        isSubmitting={isSubmitting}
        onOpenChange={(open) => {
          setDialogOpen(open);
          if (!open) {
            setSelectedClient(null);
          }
        }}
        onSubmit={handleSubmit}
      />

      <DeleteConfirmationDialog
        open={Boolean(clientToDelete)}
        title="Удалить клиента"
        description={`Удалить ${clientToDelete?.firstName || ""} ${clientToDelete?.lastName || ""}?`}
        onOpenChange={(open) => {
          if (!open) {
            setClientToDelete(null);
          }
        }}
        onConfirm={handleDelete}
      />
    </div>
  );
}
