import {
  AlertTriangle,
  ArrowRight,
  BookOpenText,
  ChefHat,
  Leaf,
  ShoppingBag,
  Sparkles,
  Users,
} from "lucide-react";
import { Link } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { LoadingState, ErrorState } from "@/components/shared/status-view";
import { useResource } from "@/hooks/use-resource";
import { formatCount, formatCurrency } from "@/lib/format";
import { getDashboardData } from "@/services/dashboard-service";
import { mockFeaturedDishes } from "@/features/home/mock-featured-dishes";

const quickLinks = [
  {
    title: "Блюда",
    description: "Управляйте меню, ценами и составом блюд.",
    to: "/dishes",
    icon: ChefHat,
  },
  {
    title: "Заказы",
    description: "Просматривайте и редактируйте активные заказы.",
    to: "/orders",
    icon: ShoppingBag,
  },
  {
    title: "Клиенты",
    description: "Храните и обновляйте клиентские данные.",
    to: "/clients",
    icon: Users,
  },
  {
    title: "Категории",
    description: "Поддерживайте понятную структуру меню.",
    to: "/categories",
    icon: BookOpenText,
  },
  {
    title: "Ингредиенты",
    description: "Ведите список ингредиентов для каждого блюда.",
    to: "/ingredients",
    icon: Leaf,
  },
];

export function HomePage() {
  const { data, isLoading, error, reload } = useResource(getDashboardData, []);
  const featuredDishes =
    data?.featuredDishes && data.featuredDishes.length > 0
      ? data.featuredDishes
      : mockFeaturedDishes;

  if (isLoading) {
    return <LoadingState label="Подготавливаем главную страницу..." />;
  }

  if (error || !data) {
    return <ErrorState message={error || "Главная страница временно недоступна."} onRetry={reload} />;
  }

  return (
    <div className="space-y-8">
      <section className="overflow-hidden rounded-[2.5rem] bg-hero-glow p-8 text-white shadow-ambient sm:p-10 lg:min-h-[420px] lg:p-12">
        <div className="grid items-start gap-8 xl:grid-cols-[minmax(0,1.2fr)_380px]">
          <div className="flex h-full flex-col justify-center space-y-6">
            <div className="space-y-4">
              <h1 className="max-w-3xl font-display text-5xl leading-none sm:text-6xl">
                Ресторан
              </h1>
              <p className="max-w-2xl text-base leading-7 text-white/74 sm:text-lg">
                Удобная панель для управления меню, клиентами и заказами.
                Красивый интерфейс с практичной ежедневной пользой.
              </p>
            </div>
            <div className="flex flex-wrap gap-3">
              <Button asChild size="lg">
                <Link to="/dishes">
                  Открыть блюда
                  <ArrowRight className="h-4 w-4" />
                </Link>
              </Button>
              <Button asChild size="lg" variant="outline" className="text-black hover:text-black">
                <Link to="/orders">Открыть заказы</Link>
              </Button>
            </div>
            {data.warnings.length > 0 ? (
              <div className="flex items-start gap-3 rounded-[1.5rem] border border-white/15 bg-black/15 p-4 text-sm text-white/78">
                <AlertTriangle className="mt-0.5 h-4 w-4 shrink-0 text-accent/95" />
                <div>
                  Часть показателей сейчас недоступна.
                  {" "}
                  {data.warnings.join(" ")}
                </div>
              </div>
            ) : null}
          </div>
          <Card className="border-white/10 bg-white/10 text-white shadow-none backdrop-blur">
            <CardContent className="space-y-6 p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-xs uppercase tracking-[0.28em] text-white/82">
                    Общая сводка
                  </p>
                  <p className="mt-2 font-display text-3xl">Состояние системы</p>
                </div>
                <div className="rounded-full bg-white/10 p-3 text-accent">
                  <Sparkles className="h-5 w-5" />
                </div>
              </div>
              <div className="grid gap-3 sm:grid-cols-2">
                <div className="rounded-[1.5rem] bg-black/15 p-4">
                  <div className="text-xs uppercase tracking-[0.24em] text-white/80">Клиенты</div>
                  <div className="mt-3 font-display text-3xl">
                    {formatCount(data.summary.clients)}
                  </div>
                </div>
                <div className="rounded-[1.5rem] bg-black/15 p-4">
                  <div className="text-xs uppercase tracking-[0.24em] text-white/80">Заказы</div>
                  <div className="mt-3 font-display text-3xl">
                    {formatCount(data.summary.orders)}
                  </div>
                </div>
                <div className="rounded-[1.5rem] bg-black/15 p-4">
                  <div className="text-xs uppercase tracking-[0.24em] text-white/80">Блюда</div>
                  <div className="mt-3 font-display text-3xl">
                    {formatCount(data.summary.dishes)}
                  </div>
                </div>
                <div className="rounded-[1.5rem] bg-black/15 p-4">
                  <div className="text-xs uppercase tracking-[0.24em] text-white/80">
                    Ингредиенты
                  </div>
                  <div className="mt-3 font-display text-3xl">
                    {formatCount(data.summary.ingredients)}
                  </div>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>
      </section>

      <section className="page-grid">
        <Card className="bg-white/80">
          <CardContent className="space-y-6 p-6 sm:p-8">
            <div className="space-y-2">
              <p className="text-xs uppercase tracking-[0.28em] text-primary/75">
                Быстрый переход
              </p>
              <h2 className="font-display text-4xl">Разделы управления</h2>
            </div>
            <div className="grid gap-4 md:grid-cols-2">
              {quickLinks.map((item) => {
                const Icon = item.icon;
                return (
                  <Link
                    key={item.to}
                    to={item.to}
                    className="group rounded-[1.75rem] border border-border/80 bg-background/80 p-5 transition hover:-translate-y-1 hover:border-primary/30 hover:shadow-float"
                  >
                    <div className="mb-4 flex h-12 w-12 items-center justify-center rounded-full bg-primary/10 text-primary">
                      <Icon className="h-5 w-5" />
                    </div>
                    <h3 className="font-display text-2xl">{item.title}</h3>
                    <p className="mt-2 text-sm leading-6 text-muted-foreground">
                      {item.description}
                    </p>
                  </Link>
                );
              })}
            </div>
          </CardContent>
        </Card>

        <Card className="overflow-hidden bg-[#1c1713] text-white">
          <CardContent className="space-y-6 p-6">
            <div className="space-y-2">
              <p className="text-xs uppercase tracking-[0.28em] text-accent/95">Превью</p>
              <h2 className="font-display text-3xl">Подборка блюд</h2>
              <p className="text-sm leading-6 text-white/65">
                Краткий обзор блюд, которые выделяются в меню прямо сейчас.
              </p>
            </div>
            <div className="space-y-4">
              {featuredDishes.map((dish) => (
                <div key={dish.id} className="rounded-[1.5rem] border border-white/10 bg-white/5 p-4">
                  <div className="flex items-start justify-between gap-4">
                    <div>
                      <p className="text-xs uppercase tracking-[0.24em] text-white/80">
                        {dish.category}
                      </p>
                      <h3 className="mt-2 font-display text-2xl">{dish.name}</h3>
                    </div>
                    <div className="text-sm text-accent">{formatCurrency(dish.price)}</div>
                  </div>
                  <p className="mt-3 text-sm leading-6 text-white/82">
                    {dish.ingredients.join(" / ")}
                  </p>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      </section>
    </div>
  );
}
