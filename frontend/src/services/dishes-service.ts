import { api } from "@/lib/api";
import type { PageResponse } from "@/types/api";
import type { Dish } from "@/types/entities";

export interface DishSearchParams {
  query?: string;
  categoryName?: string;
  ingredientName?: string;
  minPrice?: number;
  maxPrice?: number;
  page?: number;
  size?: number;
}

export type DishPayload = Omit<Dish, "id">;

export const dishesService = {
  async getAll() {
    const response = await api.get<Dish[]>("/dishAll");
    return response.data;
  },
  async search(params: DishSearchParams) {
    const response = await api.get<PageResponse<Dish>>("/dish/search", {
      params: {
        namePart: params.query || undefined,
        categoryName: params.categoryName || undefined,
        ingredientName: params.ingredientName || undefined,
        minPrice: params.minPrice,
        maxPrice: params.maxPrice,
        useNativeQuery: true,
        page: params.page ?? 0,
        size: params.size ?? 24,
      },
    });
    return response.data;
  },
  async create(payload: DishPayload) {
    const response = await api.post<Dish>("/dish", payload);
    return response.data;
  },
  async update(id: number, payload: DishPayload) {
    const response = await api.put<Dish>(`/dish/${id}`, payload);
    return response.data;
  },
  async delete(id: number) {
    await api.delete(`/dish/${id}`);
  },
};
