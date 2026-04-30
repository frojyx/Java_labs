import { NavLink } from "react-router-dom";
import { navigationItems } from "@/components/navigation/navigation-config";
import { cn } from "@/lib/utils";

export function AppSidebar() {
  return (
    <aside className="hidden w-80 flex-col justify-between rounded-[2rem] border border-white/40 bg-[#1a1612] p-6 text-white shadow-ambient lg:flex">
      <div className="space-y-8">
        <div className="space-y-3">
          <div className="inline-flex rounded-full border border-white/10 bg-white/5 px-4 py-1 text-xs uppercase tracking-[0.28em] text-accent">
            Ресторан
          </div>
          <div>
            <h1 className="font-display text-4xl">Панель ресторана</h1>
            <p className="mt-2 text-sm leading-6 text-white">
              Управление меню, клиентами и заказами в одном месте.
            </p>
          </div>
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
                      ? "bg-white text-[#1a1612] shadow-float"
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
      <div className="rounded-[1.5rem] border border-white/10 bg-white/5 p-5">
        <p className="text-xs uppercase tracking-[0.24em] text-white/45">Атмосфера</p>
        <p className="mt-3 font-display text-2xl">Аккуратный сервис и понятные процессы.</p>
      </div>
    </aside>
  );
}
