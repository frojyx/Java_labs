import { ReactNode } from "react";
import { Card, CardContent } from "@/components/ui/card";

interface StatCardProps {
  label: string;
  value: string;
  icon: ReactNode;
  tone?: "light" | "dark";
  hideLabel?: boolean;
}

export function StatCard({
  label,
  value,
  icon,
  tone = "light",
  hideLabel = false,
}: StatCardProps) {
  return (
    <Card
      className={
        tone === "dark"
          ? "h-full border-transparent bg-[#191510] text-white"
          : "h-full bg-white/80"
      }
    >
      <CardContent className="flex items-center justify-between p-6">
        <div className={hideLabel ? "space-y-0" : "space-y-2"}>
          {hideLabel ? null : (
            <p
              className={
                tone === "dark"
                  ? "text-xs uppercase tracking-[0.28em] text-white/82"
                  : "text-xs uppercase tracking-[0.28em] text-foreground/72"
              }
            >
              {label}
            </p>
          )}
          <p className="font-display text-4xl">{value}</p>
        </div>
        <div
          className={
            tone === "dark"
              ? "rounded-full bg-white/10 p-4 text-accent"
              : "rounded-full bg-primary/10 p-4 text-primary"
          }
        >
          {icon}
        </div>
      </CardContent>
    </Card>
  );
}
