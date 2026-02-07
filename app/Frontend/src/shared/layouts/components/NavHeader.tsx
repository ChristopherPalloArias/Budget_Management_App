import { SidebarTrigger } from "../../../components/ui/sidebar"
import { Separator } from "../../../components/ui/separator"
import { DynamicBreadcrumbs } from "./DynamicBreadcrumbs"
import { CommandMenu } from "./CommandMenu"
import { ThemeToggle } from "./ThemeToggle"
import { NavHeaderUser } from "./NavHeaderUser"

export function NavHeader() {
  return (
    <header className="flex h-16 shrink-0 items-center gap-2 border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
      <div className="flex items-center gap-2 px-4">
        <SidebarTrigger className="-ml-1" />
        <Separator orientation="vertical" className="mr-2 h-4" />
        <DynamicBreadcrumbs />
      </div>
      
      <div className="flex flex-1 items-center gap-2 px-4">
        <div className="ml-auto flex items-center space-x-2">
          <CommandMenu />
          <ThemeToggle />
          <NavHeaderUser />
        </div>
      </div>
    </header>
  )
}