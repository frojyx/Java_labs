import { useDeferredValue, useEffect, useState } from "react";
import { Pencil, Plus, Trash2 } from "lucide-react";
import { toast } from "sonner";
import { DeleteConfirmationDialog } from "@/components/shared/delete-confirmation-dialog";
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
import { dishesService } from "@/services/dishes-service";
import { ordersService } from "@/services/orders-service";
import type { Dish, Order } from "@/types/entities";

export function OrdersPage() {
  const [orders, setOrders] = useState<Order[]>([]);
  const [dishes, setDishes] = useState<Dish[]>([]);
  const [query, setQuery] = useState("");
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
      const [ordersData, dishesData] = await Promise.all([
        ordersService.search(deferredQuery),
        dishesService.getAll(),
      ]);
      setOrders(ordersData);
      setDishes(dishesData);
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

  return (
    <div className="space-y-8">
      <PageHeader
        eyebrow="Заказы"
        title="Заказы"
        description="Просматривайте, создавайте и редактируйте заказы клиентов в одном разделе."
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
                {orders.map((order) => (
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
      )}

      <OrderFormDialog
        open={dialogOpen}
        order={selectedOrder}
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
