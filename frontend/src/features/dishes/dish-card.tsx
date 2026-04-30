import { Pencil, Trash2 } from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { formatCurrency } from "@/lib/format";
import type { Dish } from "@/types/entities";

interface DishCardProps {
  dish: Dish;
  onEdit: (dish: Dish) => void;
  onDelete: (dish: Dish) => void;
}

export function DishCard({ dish, onEdit, onDelete }: DishCardProps) {
  return (
    <Card className="group flex h-full min-h-[320px] flex-col overflow-hidden border-[#d9ccbf]/80 bg-gradient-to-br from-[#1f1915] via-[#2b221c] to-[#17120f] text-white transition duration-300 hover:-translate-y-1">
      <CardContent className="flex flex-1 flex-col gap-3 p-5">
        <div className="inline-flex rounded-full border border-white/15 bg-black/20 px-3 py-1 text-xs uppercase tracking-[0.28em] text-accent">
          {dish.category}
        </div>
        <div className="space-y-2">
          <h3 className="min-h-[72px] font-display text-[2rem] leading-tight">{dish.name}</h3>
          <p className="min-h-[48px] text-sm leading-6 text-white/70">{dish.ingredients.join(" / ")}</p>
        </div>
        <div className="mt-auto flex flex-wrap items-center justify-between gap-3">
          <div className="flex flex-wrap items-center gap-2">
            <Badge variant="outline" className="border-white/15 bg-white/8 text-white">
              {formatCurrency(dish.price)}
            </Badge>
            <Badge variant="outline" className="border-white/15 bg-white/8 text-white">
              {dish.weight} г
            </Badge>
          </div>
          <div className="flex items-center gap-2">
            <Button variant="secondary" size="icon" onClick={() => onEdit(dish)}>
              <Pencil className="h-4 w-4" />
            </Button>
            <Button variant="outline" size="icon" onClick={() => onDelete(dish)}>
              <Trash2 className="h-4 w-4" />
            </Button>
          </div>
        </div>
      </CardContent>
    </Card>
  );
}
