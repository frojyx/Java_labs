import {
  BookOpenText,
  ChefHat,
  Grid2x2,
  Leaf,
  ShoppingBag,
  Users,
} from "lucide-react";

export const navigationItems = [
  { to: "/", label: "Главная", icon: Grid2x2 },
  { to: "/dishes", label: "Блюда", icon: ChefHat },
  { to: "/orders", label: "Заказы", icon: ShoppingBag },
  { to: "/clients", label: "Клиенты", icon: Users },
  { to: "/categories", label: "Категории", icon: BookOpenText },
  { to: "/ingredients", label: "Ингредиенты", icon: Leaf },
];
