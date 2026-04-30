import { Menu } from "lucide-react";
import { NavLink } from "react-router-dom";
import { navigationItems } from "@/components/navigation/navigation-config";
import { Button } from "@/components/ui/button";
import { Sheet, SheetContent, SheetTrigger } from "@/components/ui/sheet";
import { cn } from "@/lib/utils";

export function MobileNavigation() {
  return (
    <div className="flex items-center justify-between rounded-[1.5rem] border border-white/50 bg-white/80 px-4 py-3 shadow-float lg:hidden">
      <div>
        <div className="text-xs uppercase tracking-[0.28em] text-primary/70">Ресторан</div>
        <div className="font-display text-2xl">Панель</div>
      </div>
      <Sheet>
        <SheetTrigger asChild>
          <Button size="icon" variant="outline">
            <Menu className="h-4 w-4" />
          </Button>
        </SheetTrigger>
        <SheetContent className="bg-[#1a1612] text-white">
          <div className="space-y-6 pt-10">
            <div>
              <div className="text-xs uppercase tracking-[0.28em] text-accent">Навигация</div>
              <h2 className="mt-2 font-display text-3xl">Ресторан</h2>
            </div>
            <nav className="space-y-2">
              {navigationItems.map((item) => {
                const Icon = item.icon;
                return (
                  <NavLink
                    key={item.to}
                    to={item.to}
                    className={({ isActive }) =>
                      cn(
                        "flex items-center gap-3 rounded-2xl px-4 py-3 text-sm transition",
                        isActive
                          ? "bg-white text-[#1a1612]"
                          : "text-white/72 hover:bg-white/8 hover:text-white",
                      )
                    }
                  >
                    <Icon className="h-4 w-4" />
                    <span>{item.label}</span>
                  </NavLink>
                );
              })}
            </nav>
          </div>
        </SheetContent>
      </Sheet>
    </div>
  );
}
