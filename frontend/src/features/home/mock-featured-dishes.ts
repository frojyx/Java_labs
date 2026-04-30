import type { Dish } from "@/types/entities";

export const mockFeaturedDishes: Dish[] = [
  {
    id: 1,
    name: "Лосось с цитрусами",
    category: "Фирменное",
    ingredients: ["Лосось", "Юдзу", "Фенхель", "Травы"],
    price: 28,
    weight: 320,
  },
  {
    id: 2,
    name: "Ризотто с трюфелем и грибами",
    category: "Выбор шефа",
    ingredients: ["Арборио", "Трюфель", "Пармезан", "Белые грибы"],
    price: 24,
    weight: 360,
  },
  {
    id: 3,
    name: "Томлёная баранина",
    category: "Резерв",
    ingredients: ["Баранина", "Розмарин", "Финиковая глазурь", "Морковь"],
    price: 34,
    weight: 410,
  },
];
