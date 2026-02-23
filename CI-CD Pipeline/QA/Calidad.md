# Informe de Calidad: Bug en CommandMenu — Falta de importación de `useNavigate`

Este documento describe un bug detectado en el módulo Frontend del repositorio (componente de navegación rápida “Command Menu”). El problema se origina por el uso del hook `useNavigate` sin su correspondiente importación desde `react-router-dom`, lo que provoca errores de compilación/ejecución y la imposibilidad de navegar al seleccionar opciones en el menú.

## 1. Introducción

- Componente afectado: `app/Frontend/src/shared/layouts/components/CommandMenu.tsx`
- Síntoma principal: Al abrir el command menu y seleccionar una entrada de navegación (“Transacciones”), la acción falla con error, y los atajos de teclado (⌘K / Ctrl+K / `/`) resultan en un componente que se abre pero no puede completar la navegación.
- Causa inmediata: Falta la importación del hook `useNavigate`.

### Evidencia en código (estado actual con defecto)

```tsx name=app/Frontend/src/shared/layouts/components/CommandMenu.tsx url=https://github.com/majoymajo/Budget_Management_App/blob/4837193d5bced094126b3727ecd6ab8aa1831bb2/app/Frontend/src/shared/layouts/components/CommandMenu.tsx#L2-L22
import { Search, TrendingUp } from "lucide-react"
import {
  Command,
  CommandEmpty,
  CommandGroup,
  CommandInput,
  CommandItem,
  CommandList,
} from "../../../components/ui/command"
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "../../../components/ui/popover"
import { Button } from "../../../components/ui/button"
import { useState, useEffect } from "react"
// Falta: import { useNavigate } from "react-router-dom"
```

```tsx name=app/Frontend/src/shared/layouts/components/CommandMenu.tsx url=https://github.com/majoymajo/Budget_Management_App/blob/4837193d5bced094126b3727ecd6ab8aa1831bb2/app/Frontend/src/shared/layouts/components/CommandMenu.tsx#L26-L42
export function CommandMenu() {
  const [open, setOpen] = useState(false)
  const [value, setValue] = useState("")
  const navigate = useNavigate() // Uso del hook sin importación

  useEffect(() => {
    const down = (e: KeyboardEvent) => {
      if ((e.key === "k" && (e.metaKey || e.ctrlKey)) || e.key === "/") {
        // ...
      }
    }
    document.addEventListener("keydown", down)
    return () => document.removeEventListener("keydown", down)
  }, [])
```

## 2. Clasificación: Error, Defecto, Fallo

### Error (Acción humana incorrecta)
- Omisión del import requerido: Durante la implementación del `CommandMenu` se usó el hook `useNavigate` sin añadir `import { useNavigate } from "react-router-dom"`.
- Posibles causas humanas:
  - Dependencia de auto-import de IDE que no se activó en este archivo.
  - Sobrecarga de contexto: múltiples imports de componentes UI llevaron a pasar por alto el import del hook de navegación.
  - Falta de checklist de revisiones (lint/TS/CI) que obligue a detectar símbolos no importados.

### Defecto (Imperfección visible en el código)
- El archivo `CommandMenu.tsx` referencia `useNavigate` sin declararlo ni importarlo.
- Defecto estático verificable:
  - TypeScript: “Cannot find name 'useNavigate'” o “useNavigate is not defined”.
  - En tiempo de ejecución (si compilara sin tipos): `ReferenceError: useNavigate is not defined`.

#### Ejemplo del defecto (fragmento con el símbolo no resuelto)
```tsx name=CommandMenu.defecto.tsx
// Dentro del componente:
const navigate = useNavigate() // Símbolo no importado -> error de tipos/ejecución
```

### Fallo (Comportamiento erróneo en ejecución)
- El command menu se abre, pero al seleccionar un item:
  - Se lanza un error en consola y la navegación no ocurre.
  - El flujo de teclado de atajo (⌘K/Ctrl+K) funciona para abrir el menú, pero “seleccionar y navegar” falla.

#### Pasos para reproducir
1. Abrir la aplicación y autenticar al usuario si aplica.
2. Presionar ⌘K/Ctrl+K o `/` para abrir el Command Menu.
3. Escribir “Transacciones” y seleccionar la opción.
4. Observado:
   - Error en consola (TypeScript o ReferenceError).
   - No hay cambio de ruta hacia `/transactions`.

#### Mensajes típicos
- TypeScript (build): `TS2304: Cannot find name 'useNavigate'`
- Runtime: `ReferenceError: useNavigate is not defined`

## 3. Recomendación de corrección

### Solución inmediata
- Añadir la importación faltante:
```tsx name=CommandMenu.fix.tsx
import { useNavigate } from "react-router-dom"
```

- Verificar que el componente monta correctamente y que `navigate(url)` realiza el cambio de ruta esperado al seleccionar un `CommandItem`.

### Ejemplo del archivo corregido (extracto)
```tsx name=app/Frontend/src/shared/layouts/components/CommandMenu.fixed.tsx
import { Search, TrendingUp } from "lucide-react"
import {
  Command,
  CommandEmpty,
  CommandGroup,
  CommandInput,
  CommandItem,
  CommandList,
} from "../../../components/ui/command"
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "../../../components/ui/popover"
import { Button } from "../../../components/ui/button"
import { useState, useEffect } from "react"
import { useNavigate } from "react-router-dom" // Import agregado

export function CommandMenu() {
  const [open, setOpen] = useState(false)
  const [value, setValue] = useState("")
  const navigate = useNavigate()

  // resto del componente sin cambios...
}
```

### Medidas preventivas
- Activar y exigir reglas de ESLint/TypeScript:
  - `no-undef` y `@typescript-eslint/no-unused-vars` para detectar símbolos no reconocidos/importados.
- Añadir tests de integración de navegación:
  - Simular atajos de teclado y selección en el command menu.
  - Verificar que `navigate` es llamado con la ruta correcta.
- Incorporar CI con build estricto:
  - Fallar el pipeline ante errores de tipo (TS) o lints críticos.
- Revisar otros componentes con navegación para detectar importaciones faltantes similares.

## 4. Impacto y severidad

- Impacto: Bloquea la funcionalidad de navegación rápida y deteriora la UX.
- Severidad: Media-Alta en Frontend (afecta accesos clave y atajos de teclado).

---
## 5. Conclusión

El bug proviene de una omisión de importación en `CommandMenu.tsx`. La corrección es trivial (añadir el import de `useNavigate`), pero su efecto en la experiencia de usuario es significativo. Se recomienda abordar la solución inmediata y reforzar la disciplina de calidad con reglas de linting, pruebas y CI para prevenir recurrencias.
