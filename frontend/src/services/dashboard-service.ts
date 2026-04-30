import { categoriesService } from "@/services/categories-service";
import { clientsService } from "@/services/clients-service";
import { dishesService } from "@/services/dishes-service";
import { ingredientsService } from "@/services/ingredients-service";
import { ordersService } from "@/services/orders-service";
import { getApiErrorMessage } from "@/lib/api";
import type { DashboardSummary, Dish } from "@/types/entities";

export async function getDashboardData() {
  const results = await Promise.allSettled([
    clientsService.getAll(),
    ordersService.getAll(),
    dishesService.getAll(),
    categoriesService.getAll(),
    ingredientsService.getAll(),
  ]);

  const clients = getCollectionResult(results[0], "РєР»РёРµРЅС‚РѕРІ");
  const orders = getCollectionResult(results[1], "Р·Р°РєР°Р·РѕРІ");
  const dishes = getCollectionResult(results[2], "Р±Р»СЋРґ");
  const categories = getCollectionResult(results[3], "РєР°С‚РµРіРѕСЂРёР№");
  const ingredients = getCollectionResult(results[4], "РёРЅРіСЂРµРґРёРµРЅС‚РѕРІ");

  const failures = [clients, orders, dishes, categories, ingredients].filter(
    (result) => result.error,
  );

  if (failures.length === results.length) {
    throw new Error(failures[0]?.error || "Данные главной страницы недоступны.");
  }

  const summary: DashboardSummary = {
    clients: clients.data.length,
    orders: orders.data.length,
    dishes: dishes.data.length,
    categories: categories.data.length,
    ingredients: ingredients.data.length,
  };

  return {
    summary,
    featuredDishes: selectFeaturedDishes(dishes.data),
    warnings: failures.map((failure) => failure.error!),
  };
}

function selectFeaturedDishes(dishes: Dish[]) {
  return [...dishes]
    .sort((left, right) => right.price - left.price || right.weight - left.weight)
    .slice(0, 3);
}

function getCollectionResult<T>(
  result: PromiseSettledResult<T[]>,
  label: string,
) {
  if (result.status === "fulfilled") {
    return {
      data: result.value,
      error: null,
    };
  }

  return {
    data: [] as T[],
    error: `РќРµ СѓРґР°Р»РѕСЃСЊ Р·Р°РіСЂСѓР·РёС‚СЊ ${label}: ${getApiErrorMessage(result.reason)}`,
  };
}
