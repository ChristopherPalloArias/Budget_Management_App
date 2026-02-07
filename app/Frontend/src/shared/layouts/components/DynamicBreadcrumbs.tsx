import { useLocation, Link } from "react-router-dom"
import {
  Breadcrumb,
  BreadcrumbItem,
  BreadcrumbLink,
  BreadcrumbList,
  BreadcrumbPage,
  BreadcrumbSeparator,
} from "../../../components/ui/breadcrumb"

const routeMap: Record<string, string> = {
  "/": "Inicio",
  "/transactions": "Transacciones",
  "/budgets": "Presupuestos",
  "/reports": "Reportes",
  "/settings": "Configuración",
}

function capitalizeFirstLetter(str: string): string {
  return str.charAt(0).toUpperCase() + str.slice(1)
}

function generateBreadcrumbs(pathname: string): Array<{ label: string; href: string }> {
  const segments = pathname.split("/").filter(Boolean)
  
  if (segments.length === 0) {
    return [{ label: "Inicio", href: "/" }]
  }

  const breadcrumbs: Array<{ label: string; href: string }> = []
  
  segments.forEach((segment, index) => {
    const href = "/" + segments.slice(0, index + 1).join("/")
    const formattedSegment = segment.replace(/-/g, " ")
    
    const label = routeMap[href] || capitalizeFirstLetter(formattedSegment)
    breadcrumbs.push({ label, href })
  })

  return breadcrumbs
}

export function DynamicBreadcrumbs() {
  const location = useLocation()
  const breadcrumbs = generateBreadcrumbs(location.pathname)

  return (
    <Breadcrumb>
      <BreadcrumbList>
        {breadcrumbs.map((item, index) => (
          <div key={item.href} className="flex items-center">
            {index > 0 && <BreadcrumbSeparator />}
            <BreadcrumbItem>
              {index === breadcrumbs.length - 1 ? (
                <BreadcrumbPage>{item.label}</BreadcrumbPage>
              ) : (
                <BreadcrumbLink asChild>
                  <Link to={item.href}>{item.label}</Link>
                </BreadcrumbLink>
              )}
            </BreadcrumbItem>
          </div>
        ))}
      </BreadcrumbList>
    </Breadcrumb>
  )
}