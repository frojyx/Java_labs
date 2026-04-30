import { AlertTriangle, LoaderCircle } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";

export function LoadingState({ label = "Загрузка..." }: { label?: string }) {
  return (
    <Card className="bg-white/70">
      <CardContent className="flex items-center justify-center gap-3 p-10 text-muted-foreground">
        <LoaderCircle className="h-5 w-5 animate-spin" />
        <span>{label}</span>
      </CardContent>
    </Card>
  );
}

export function ErrorState({
  message,
  onRetry,
}: {
  message: string;
  onRetry?: () => void;
}) {
  return (
    <Card className="border-destructive/20 bg-white/80">
      <CardContent className="flex flex-col items-center gap-4 p-10 text-center">
        <div className="rounded-full bg-destructive/10 p-4 text-destructive">
          <AlertTriangle className="h-6 w-6" />
        </div>
        <div className="space-y-2">
          <h3 className="font-display text-2xl">Не удалось загрузить раздел</h3>
          <p className="max-w-md text-sm text-muted-foreground">{message}</p>
        </div>
        {onRetry ? <Button onClick={onRetry}>Повторить</Button> : null}
      </CardContent>
    </Card>
  );
}
