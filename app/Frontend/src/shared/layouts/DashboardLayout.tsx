import { Outlet } from "react-router-dom"
import { AppSidebar } from "./components/AppSidebar"
import { NavHeader } from "./components/NavHeader"
import { SidebarProvider, SidebarInset } from "../../components/ui/sidebar"

export const DashboardLayout = () => {
  return (
    <SidebarProvider>
      <AppSidebar />
      <SidebarInset>
        <NavHeader />
        <div className="flex flex-1 flex-col gap-4 p-4 bg-muted/50">
          <div className="min-h-[100vh] flex-1 rounded-xl bg-background p-6 md:min-h-min">
            <Outlet />
          </div>
        </div>
      </SidebarInset>
    </SidebarProvider>
  )
}