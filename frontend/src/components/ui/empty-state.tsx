import { ChefHat } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";

interface EmptyStateProps {
  title: string;
  description: string;
  actionLabel?: string;
  onAction?: () => void;
}

export function EmptyState({
  title,
  description,
  actionLabel,
  onAction,
}: EmptyStateProps) {
  return (
    <Card className="border-dashed bg-white/70">
      <CardContent className="flex flex-col items-center gap-4 px-6 py-12 text-center">
        <div className="rounded-full bg-secondary p-4 text-primary">
          <ChefHat className="h-7 w-7" />
        </div>
        <div className="space-y-2">
          <h3 className="font-display text-2xl">{title}</h3>
          <p className="mx-auto max-w-md text-sm text-muted-foreground">{description}</p>
        </div>
        {actionLabel && onAction ? <Button onClick={onAction}>{actionLabel}</Button> : null}
      </CardContent>
    </Card>
  );
}
