import { api } from "@/lib/api";
import type { Ingredient } from "@/types/entities";

export type IngredientPayload = Omit<Ingredient, "id">;

export const ingredientsService = {
  async getAll() {
    const response = await api.get<Ingredient[]>("/ingredients");
    return response.data;
  },
  async search(query: string) {
    const response = await api.get<Ingredient[]>("/ingredients");
    const normalizedQuery = query.trim().toLowerCase();
    if (!normalizedQuery) {
      return response.data;
    }

    return response.data.filter((ingredient) =>
      ingredient.name.toLowerCase().includes(normalizedQuery),
    );
  },
  async create(payload: IngredientPayload) {
    const response = await api.post<Ingredient>("/ingredients", payload);
    return response.data;
  },
  async update(id: number, payload: IngredientPayload) {
    const response = await api.put<Ingredient>(`/ingredients/${id}`, payload);
    return response.data;
  },
  async delete(id: number) {
    await api.delete(`/ingredients/${id}`);
  },
};
