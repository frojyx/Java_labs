import { ReactNode } from "react";
import { Button } from "@/components/ui/button";

interface PageHeaderProps {
  eyebrow?: string;
  title: string;
  description?: string;
  actionLabel?: string;
  actionIcon?: ReactNode;
  onAction?: () => void;
}

export function PageHeader({
  eyebrow,
  title,
  description,
  actionLabel,
  actionIcon,
  onAction,
}: PageHeaderProps) {
  return (
    <div className="flex flex-col gap-3 lg:flex-row lg:items-end lg:justify-between">
      <div className="space-y-2">
        {eyebrow ? (
          <div className="text-xs font-semibold uppercase tracking-[0.3em] text-primary/80">
            {eyebrow}
          </div>
        ) : null}
        <div className="space-y-2">
          <h1 className="font-display text-4xl tracking-tight text-foreground sm:text-5xl">
            {title}
          </h1>
          {description ? (
            <p className="max-w-2xl text-sm leading-6 text-muted-foreground sm:text-base">
              {description}
            </p>
          ) : null}
        </div>
      </div>
      {actionLabel && onAction ? (
        <Button size="lg" onClick={onAction}>
          {actionIcon}
          {actionLabel}
        </Button>
      ) : null}
    </div>
  );
}
