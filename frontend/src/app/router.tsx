import { Navigate, createBrowserRouter } from "react-router-dom";
import { AppLayout } from "@/layouts/app-layout";
import { ClientsPage } from "@/pages/clients-page";
import { OrdersPage } from "@/pages/orders-page";
import { DishesPage } from "@/pages/dishes-page";
import { CategoriesPage } from "@/pages/categories-page";
import { IngredientsPage } from "@/pages/ingredients-page";
import { NotFoundPage } from "@/pages/not-found-page";

export const router = createBrowserRouter([
  {
    path: "/",
    element: <AppLayout />,
    errorElement: <NotFoundPage />,
    children: [
      { index: true, element: <Navigate to="/orders" replace /> },
      { path: "clients", element: <ClientsPage /> },
      { path: "orders", element: <OrdersPage /> },
      { path: "dishes", element: <DishesPage /> },
      { path: "categories", element: <CategoriesPage /> },
      { path: "ingredients", element: <IngredientsPage /> },
    ],
  },
  {
    path: "*",
    element: <NotFoundPage />,
  },
]);
