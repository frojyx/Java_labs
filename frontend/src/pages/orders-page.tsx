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
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { OrderFormDialog } from "@/features/orders/order-form-dialog";
import { getApiErrorMessage } from "@/lib/api";
import { clientsService } from "@/services/clients-service";
import { dishesService } from "@/services/dishes-service";
import { ordersService } from "@/services/orders-service";
import type { Client, Dish, Order } from "@/types/entities";

export function OrdersPage() {
  const pageSize = 9;
  const [clients, setClients] = useState<Client[]>([]);
  const [orders, setOrders] = useState<Order[]>([]);
  const [dishes, setDishes] = useState<Dish[]>([]);
  const [query, setQuery] = useState("");
  const [page, setPage] = useState(0);
  const [isLoading, setIsLoading] = useState(true);
  const [hasLoadedOnce, setHasLoadedOnce] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [selectedOrder, setSelectedOrder] = useState<Order | null>(null);
  const [orderToDelete, setOrderToDelete] = useState<Order | null>(null);
  const deferredQuery = useDeferredValue(query);

  async function loadOrders() {
    if (!hasLoadedOnce) {
      setIsLoading(true);
    }
    setError(null);
    try {
      const [ordersData, dishesData, clientsData] = await Promise.all([
        ordersService.search(deferredQuery),
        dishesService.getAll(),
        clientsService.getAll(),
      ]);
      setOrders(ordersData);
      setDishes(dishesData);
      setClients(clientsData);
    } catch (err) {
      setError(getApiErrorMessage(err));
    } finally {
      setIsLoading(false);
      setHasLoadedOnce(true);
    }
  }

  useEffect(() => {
    void loadOrders();
  }, [deferredQuery]);

  useEffect(() => {
    setPage(0);
  }, [deferredQuery]);

  useEffect(() => {
    const nextTotalPages = Math.ceil(orders.length / pageSize);
    if (nextTotalPages > 0 && page > nextTotalPages - 1) {
      setPage(nextTotalPages - 1);
    }
  }, [orders.length, page]);

  async function handleSubmit(payload: Omit<Order, "id">) {
    setIsSubmitting(true);
    try {
      if (selectedOrder) {
        await ordersService.update(selectedOrder.id, payload);
        toast.success("Заказ успешно обновлён.");
      } else {
        await ordersService.create(payload);
        toast.success("Заказ успешно создан.");
      }
      setDialogOpen(false);
      setSelectedOrder(null);
      void loadOrders();
    } catch (err) {
      toast.error(getApiErrorMessage(err));
    } finally {
      setIsSubmitting(false);
    }
  }

  async function handleDelete() {
    if (!orderToDelete) {
      return;
    }

    try {
      await ordersService.delete(orderToDelete.id);
      toast.success("Заказ успешно удалён.");
      setOrderToDelete(null);
      void loadOrders();
    } catch (err) {
      toast.error(getApiErrorMessage(err));
    }
  }

  if (isLoading) {
    return <LoadingState label="Загрузка заказов..." />;
  }

  if (error) {
    return <ErrorState message={error} onRetry={loadOrders} />;
  }

  const totalPages = Math.ceil(orders.length / pageSize);
  const paginatedOrders = orders.slice(page * pageSize, page * pageSize + pageSize);

  return (
    <div className="space-y-6">
      <PageHeader
        eyebrow="Заказы"
        title="Заказы"
        actionLabel="Создать заказ"
        actionIcon={<Plus className="h-4 w-4" />}
        onAction={() => {
          setSelectedOrder(null);
          setDialogOpen(true);
        }}
      />

      <div className="rounded-[2rem] border border-border/70 bg-white/75 p-5 shadow-float">
        <SearchInput
          value={query}
          onChange={setQuery}
          placeholder="Поиск по клиенту или блюдам..."
        />
      </div>

      {orders.length === 0 ? (
        <EmptyState
          title="Заказы не найдены"
          description="Здесь будут отображаться созданные заказы."
          actionLabel="Создать заказ"
          onAction={() => {
            setSelectedOrder(null);
            setDialogOpen(true);
          }}
        />
      ) : (
        <div className="space-y-5">
          <Card className="bg-white/85">
            <CardContent className="p-0">
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Заказ</TableHead>
                    <TableHead>Клиент</TableHead>
                    <TableHead>Блюда</TableHead>
                    <TableHead className="text-right">Действия</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {paginatedOrders.map((order) => (
                    <TableRow key={order.id}>
                      <TableCell>
                        <div className="font-medium">#{order.id}</div>
                      </TableCell>
                      <TableCell>
                        <div className="font-medium">
                          {order.clientFirstName} {order.clientLastName}
                        </div>
                      </TableCell>
                      <TableCell>
                        <div className="flex flex-wrap gap-2">
                          {order.dishNames.map((dishName) => (
                            <Badge key={dishName} variant="outline">
                              {dishName}
                            </Badge>
                          ))}
                        </div>
                      </TableCell>
                      <TableCell className="text-right">
                        <div className="flex justify-end gap-2">
                          <Button
                            variant="ghost"
                            size="icon"
                            onClick={() => {
                              setSelectedOrder(order);
                              setDialogOpen(true);
                            }}
                          >
                            <Pencil className="h-4 w-4" />
                          </Button>
                          <Button
                            variant="ghost"
                            size="icon"
                            onClick={() => setOrderToDelete(order)}
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

          <PaginationControls
            page={page}
            totalPages={totalPages}
            totalItems={orders.length}
            itemLabel="заказов"
            onPageChange={setPage}
          />
        </div>
      )}

      <OrderFormDialog
        open={dialogOpen}
        order={selectedOrder}
        clients={clients}
        dishes={dishes}
        isSubmitting={isSubmitting}
        onOpenChange={(open) => {
          setDialogOpen(open);
          if (!open) {
            setSelectedOrder(null);
          }
        }}
        onSubmit={handleSubmit}
      />

      <DeleteConfirmationDialog
        open={Boolean(orderToDelete)}
        title="Удалить заказ"
        description={`Удалить заказ #${orderToDelete?.id || ""}?`}
        onOpenChange={(open) => {
          if (!open) {
            setOrderToDelete(null);
          }
        }}
        onConfirm={handleDelete}
      />
    </div>
  );
}
