import { Outlet, useLocation } from "react-router-dom";
import { AppSidebar } from "@/components/navigation/app-sidebar";
import { MobileNavigation } from "@/components/navigation/mobile-navigation";
import { Toaster } from "sonner";

export function AppLayout() {
  const location = useLocation();
  const isHomePage = location.pathname === "/";

  return (
    <>
      <div className="mx-auto flex min-h-screen w-full max-w-[1600px] gap-6 px-4 py-4 sm:px-6 lg:px-8">
        <AppSidebar />
        <main className="flex flex-1 flex-col gap-6">
          <MobileNavigation />
          <div
            className={
              isHomePage
                ? "space-y-8"
                : "space-y-8 rounded-[2rem] border border-white/50 bg-white/70 p-4 shadow-ambient backdrop-blur-xl sm:p-6 lg:p-8"
            }
          >
            <Outlet />
          </div>
        </main>
      </div>
      <Toaster richColors position="top-right" />
    </>
  );
}
