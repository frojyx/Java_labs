import { api } from "@/lib/api";
import type { Order } from "@/types/entities";

export type OrderPayload = Omit<Order, "id">;

export const ordersService = {
  async getAll() {
    const response = await api.get<Order[]>("/orders");
    return response.data;
  },
  async search(query: string) {
    const response = await api.get<Order[]>("/orders");
    const normalizedQuery = query.trim().toLowerCase();
    if (!normalizedQuery) {
      return response.data;
    }

    return response.data.filter((order) =>
      `${order.clientFirstName} ${order.clientLastName} ${order.dishNames.join(" ")}`
        .toLowerCase()
        .includes(normalizedQuery),
    );
  },
  async create(payload: OrderPayload) {
    const response = await api.post<Order>("/orders", payload);
    return response.data;
  },
  async update(id: number, payload: OrderPayload) {
    const response = await api.put<Order>(`/orders/${id}`, payload);
    return response.data;
  },
  async delete(id: number) {
    await api.delete(`/orders/${id}`);
  },
};
