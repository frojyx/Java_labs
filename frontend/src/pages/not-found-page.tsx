import { Link } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";

export function NotFoundPage() {
  return (
    <div className="flex min-h-screen items-center justify-center px-4">
      <Card className="max-w-xl bg-white/85">
        <CardContent className="space-y-6 p-10 text-center">
          <div className="text-xs uppercase tracking-[0.28em] text-primary/70">404</div>
          <h1 className="font-display text-5xl">Страница не найдена</h1>
          <p className="text-sm leading-6 text-muted-foreground">
            Запрошенная страница отсутствует в панели управления рестораном.
          </p>
          <Button asChild size="lg">
            <Link to="/">Вернуться на главную</Link>
          </Button>
        </CardContent>
      </Card>
    </div>
  );
}
