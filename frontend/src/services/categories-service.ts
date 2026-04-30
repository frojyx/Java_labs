import { api } from "@/lib/api";
import type { Category } from "@/types/entities";

export type CategoryPayload = Omit<Category, "id">;

export const categoriesService = {
  async getAll() {
    const response = await api.get<Category[]>("/categories");
    return response.data;
  },
  async search(query: string) {
    const response = await api.get<Category[]>("/categories");
    const normalizedQuery = query.trim().toLowerCase();
    if (!normalizedQuery) {
      return response.data;
    }

    return response.data.filter((category) =>
      category.name.toLowerCase().includes(normalizedQuery),
    );
  },
  async create(payload: CategoryPayload) {
    const response = await api.post<Category>("/categories", payload);
    return response.data;
  },
  async update(id: number, payload: CategoryPayload) {
    const response = await api.put<Category>(`/categories/${id}`, payload);
    return response.data;
  },
  async delete(id: number) {
    await api.delete(`/categories/${id}`);
  },
};
