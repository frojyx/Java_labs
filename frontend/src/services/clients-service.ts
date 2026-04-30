import { api } from "@/lib/api";
import type { Client } from "@/types/entities";

export type ClientPayload = Omit<Client, "id">;

export const clientsService = {
  async getAll() {
    const response = await api.get<Client[]>("/clients");
    return response.data;
  },
  async search(query: string) {
    const response = await api.get<Client[]>("/clients");
    const normalizedQuery = query.trim().toLowerCase();
    if (!normalizedQuery) {
      return response.data;
    }

    return response.data.filter((client) =>
      `${client.firstName} ${client.lastName}`.toLowerCase().includes(normalizedQuery),
    );
  },
  async create(payload: ClientPayload) {
    const response = await api.post<Client>("/clients", payload);
    return response.data;
  },
  async update(id: number, payload: ClientPayload) {
    const response = await api.put<Client>(`/clients/${id}`, payload);
    return response.data;
  },
  async delete(id: number) {
    await api.delete(`/clients/${id}`);
  },
};
