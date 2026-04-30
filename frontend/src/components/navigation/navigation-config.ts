import {
  BookOpenText,
  ChefHat,
  Leaf,
  ShoppingBag,
  Users,
} from "lucide-react";

export const navigationItems = [
  { to: "/orders", label: "Заказы", icon: ShoppingBag },
  { to: "/dishes", label: "Блюда", icon: ChefHat },
  { to: "/clients", label: "Клиенты", icon: Users },
  { to: "/categories", label: "Категории", icon: BookOpenText },
  { to: "/ingredients", label: "Ингредиенты", icon: Leaf },
];
