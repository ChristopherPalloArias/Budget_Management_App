--- 

# ROLE
Actúa como Senior Frontend Architect. Tu objetivo es realizar el SETUP TÉCNICO de las librerías en app/frontend para el proyecto de Finanzas Personales. 

# ESTRATEGIA: CONFIGURACIÓN POR MÓDULOS E INFRAESTRUCTURA
No implementes vistas ni lógica de negocio. Configura el "Core" de cada librería para que queden listas para ser consumidas por los módulos del aplicativo.

# TAREAS DE CONFIGURACIÓN PASO A PASO

1. ARQUITECTURA MODULAR (Estructura de Carpetas)
Genera la estructura de directorios bajo una filosofía de módulos:
- src/core/: Configuraciones transversales (Firebase, Providers, API Client).
- src/shared/: Componentes UI (shadcn), utilidades, tipos globales y layouts base.
- src/modules/: Carpetas independientes por dominio (ej. auth, transactions, budgets). Cada módulo debe tener su propia subestructura: /api, /hooks, /store y /components.

2. SETUP DE LIBRERÍAS (Andamiaje Técnico)

A. FIREBASE (Auth Engine):
   - Configura src/core/config/firebase.config.ts. 
   - Inicializa el SDK de Firebase (Auth) utilizando variables de entorno para las credenciales.

B. ZUSTAND (State Engine):
   - Configura el store base en src/core/store/useUIStore.ts (para estados globales de la interfaz como sidebars o modales).
   - Crea un store base para el usuario en src/modules/auth/store/useUserStore.ts que escuche los cambios de estado de Firebase.

C. TANSTACK QUERY (Cache Engine):
   - Configura el QueryClient en src/core/config/queryClient.ts con una política de staleTime y retry definida para una aplicación financiera.

D. AXIOS (Network Engine):
   - Configura la instancia base en src/core/api/httpClient.ts.
   - Implementa solo interceptores de manejo de errores globales (ej. logs de errores de red o timeouts) y baseURL desde .env.

E. ROUTING (Navigation Engine):
   - Configura el esqueleto de rutas en src/core/router/AppRouter.tsx.
   - Define un PublicLayout y un DashboardLayout básico (solo la estructura de slots/outlets).

3. BOILERPLATE DE INTEGRACIÓN (main.tsx)
- Configura main.tsx envolviendo la aplicación únicamente con los Providers necesarios (QueryClient, Router).

# REQUISITOS TÉCNICOS
- Todo el código debe estar en TypeScript estricto.
- El diseño debe permitir que el "Producer" (API) y el Frontend escalen de forma independiente.

# FORMATO DE SALIDA
1. Árbol de archivos detallado.
2. Bloques de código para los archivos de configuración mencionados (firebase, queryClient, httpClient, useUserStore, AppRouter).

---

# ROLE
Actúa como Senior Frontend Developer. Tu objetivo es implementar el flujo de Autenticación (Login) dentro de la arquitectura modular preestablecida en app/frontend.

# CONTEXTO DEL SISTEMA
- Arquitectura: Modular por dominios (src/modules/auth).
- Autenticación: Firebase Auth (ya configurado en src/core/config/firebase.config.ts).
- Estado: Zustand (src/modules/auth/store/useUserStore.ts).
- UI: Tailwind CSS + shadcn/ui.
- Formularios: React Hook Form + Zod.

# TAREAS ESPECÍFICAS (Módulo Auth)

1. LÓGICA DE SERVICIO (API/Firebase):
   - Crea src/modules/auth/services/authService.ts.
   - Implementa funciones para: Login con Email/Password y Login con Google usando el SDK de Firebase.
   - Estas funciones deben actualizar el useUserStore al completarse.

2. COMPONENTES DE INTERFAZ (UI):
   - Crea src/modules/auth/components/LoginForm.tsx usando react-hook-form y las validaciones de zod.
   - Utiliza componentes de shadcn/ui (Button, Input, Card) para el diseño.
   - Implementa estados de carga (loading) y manejo de errores visibles para el usuario.

3. PÁGINA DE LOGIN:
   - Crea src/modules/auth/pages/LoginPage.tsx que sirva como contenedor del formulario.
   - Asegúrate de que, tras un login exitoso, el usuario sea redirigido al Dashboard mediante react-router-dom.

4. INTEGRACIÓN DE RUTAS:
   - Registra la nueva página en src/core/router/AppRouter.tsx dentro del PublicLayout.

# REQUISITOS DE CALIDAD
- No escribas lógica fuera de las carpetas del módulo auth.
- Todo el código debe ser Type-Safe.

# FORMATO DE SALIDA
1. Código de authService.ts.
2. Código de LoginForm.tsx y su esquema de validación con Zod.
3. Código de LoginPage.tsx.
4. Actualización sugerida para AppRouter.tsx.

---

# ROLE
Actúa como Senior Frontend Developer. Tu objetivo es implementar el flujo de Registro de Usuarios (Sign Up) dentro del módulo de autenticación (src/modules/auth) en app/frontend.

# CONTEXTO DEL SISTEMA
- Arquitectura: Modular por dominios.
- Autenticación: Firebase Auth.
- Estado: Zustand (src/modules/auth/store/useUserStore.ts).
- Validación: React Hook Form + Zod.
- UI: Componentes de shadcn/ui.

# TAREAS ESPECÍFICAS (Flujo de Registro)

1. AMPLIACIÓN DEL SERVICIO (API/Firebase):
   - Actualiza src/modules/auth/services/authService.ts.
   - Implementa la función registerWithEmail: debe crear el usuario en Firebase, actualizar el perfil (displayName) y sincronizar el useUserStore.
   - Implementa manejo de errores específicos de Firebase (ej: email-already-in-use).

2. COMPONENTES DE INTERFAZ (UI):
   - Crea src/modules/auth/components/RegisterForm.tsx.
   - Requisitos del formulario: Nombre completo, Email, Password y Confirmar Password.
   - Esquema Zod: Validar que las contraseñas coincidan y que tengan un mínimo de 8 caracteres.
   - Estilo: Mantener consistencia visual con el LoginForm.

3. PÁGINA DE REGISTRO:
   - Crea src/modules/auth/pages/RegisterPage.tsx.
   - Debe incluir un enlace para redirigir a los usuarios que ya tienen cuenta hacia la página de Login.

4. INTEGRACIÓN DE RUTAS:
   - Registrar RegisterPage.tsx en src/core/router/AppRouter.tsx dentro del grupo de rutas públicas.

# REQUISITOS DE CALIDAD
- Mantener la lógica de negocio separada de la UI (Separation of Concerns).
- Uso estricto de TypeScript para los retornos del servicio de Firebase.

# FORMATO DE SALIDA
1. Código actualizado de authService.ts (solo la función nueva).
2. Código de RegisterForm.tsx con su esquema Zod.
3. Código de RegisterPage.tsx.

---

# ROLE
Actúa como Senior Frontend Engineer. Tu objetivo es implementar la lógica de protección de rutas (Auth Guard) utilizando el estado de Firebase y Zustand dentro de la arquitectura modular.

# CONTEXTO TÉCNICO
- Estado Global: useUserStore.ts (Zustand) que contiene el objeto user y un booleano isLoading.
- Router: React Router DOM v6+.
- Ubicación: El componente debe residir en src/modules/auth/components/ProtectedRoute.tsx.

# TAREAS ESPECÍFICAS

1. HOOK DE SESIÓN:
   - Crea un hook en src/shared/hooks/useAuthStatus.ts que escuche el estado de useUserStore.
   - El hook debe retornar { isAuthenticated, isChecking }.

2. COMPONENTE PROTECTED ROUTE:
   - Crea un componente ProtectedRoute que envuelva las rutas privadas.
   - LÓGICA: 
     - Si isChecking es true, muestra un componente de "Loading" (puedes usar un Skeleton de shadcn o un spinner simple).
     - Si el usuario NO está autenticado, redirige a /login usando el componente <Navigate /> de React Router.
     - Si está autenticado, renderiza los children o un <Outlet />.

3. INTEGRACIÓN EN EL ROUTER:
   - Actualiza src/core/router/AppRouter.tsx para envolver todas las rutas del Dashboard (ingresos, gastos, Reportes) con este nuevo ProtectedRoute.

# REQUISITOS DE CALIDAD
- No hardcodear la redirección; usa una constante de rutas si existe.
- Asegúrate de que no haya "flashes" de contenido protegido mientras Firebase inicializa (manejo correcto del estado loading).

# FORMATO DE SALIDA
1. Código de useAuthStatus.ts.
2. Código de ProtectedRoute.tsx.
3. Ejemplo de cómo aplicarlo en el AppRouter.tsx.

---

# ROLE
Actúa como Senior Frontend Engineer. Tu objetivo es evitar que usuarios ya autenticados accedan a las páginas de Login o Registro, redirigiéndolos automáticamente al Dashboard.

# CONTEXTO TÉCNICO
- Estado Global: useUserStore.ts (Zustand).
- Router: React Router DOM.
- Ubicación: Crea src/modules/auth/components/PublicRoute.tsx.

# TAREAS ESPECÍFICAS

1. COMPONENTE PUBLIC ROUTE (GUEST GUARD):
   - Crea un componente llamado PublicRoute.
   - LÓGICA:
     - Consume el estado user e isLoading de useUserStore.
     - Si isLoading es true, muestra el componente de "Loading" (para evitar saltos visuales).
     - Si el usuario YA está autenticado (user !== null), redirige inmediatamente a /dashboard usando <Navigate to="/dashboard" replace />.
     - Si el usuario NO está autenticado, renderiza los children o el <Outlet />.

2. REFACTORIZACIÓN DEL ROUTER:
   - Actualiza src/core/router/AppRouter.tsx.
   - Envuelve las rutas públicas (Login, Landing, Registro) con este nuevo PublicRoute.

# REQUISITOS DE CALIDAD
- Usa el flag replace en el componente Maps para que la página de login no quede en el historial de navegación.
- Mantén la coherencia con el componente ProtectedRoute creado anteriormente.
- Sigue la metodología AI-First: genera el código optimizado para que el QA Engineer valide que no se puede "volver atrás" al login tras loguearse.

# FORMATO DE SALIDA
1. Código de PublicRoute.tsx.
2. Ejemplo de implementación en AppRouter.tsx mostrando la diferencia entre rutas protegidas y rutas solo para invitados.

---

# Dashboard

# ROLE
Actúa como Senior Frontend Architect. Tu objetivo es construir el LAYOUT del Dashboard para la aplicación de Finanzas Personales, integrando el componente oficial de SHADCN SIDEBAR.

# CONTEXTO TÉCNICO
- Arquitectura: Modular. El layout vive en src/shared/layouts.
- Componentes Base: Ya se ha ejecutado pnpm dlx shadcn@latest add sidebar. Debes usar los componentes de src/components/ui/sidebar.tsx.
- Estado: El Sidebar debe integrarse con SidebarProvider.
- Iconos: Lucide React.

# TAREAS ESPECÍFICAS

1. COMPONENTE APP-SIDEBAR:
   - Crea src/shared/layouts/components/AppSidebar.tsx utilizando la estructura oficial de shadcn (Sidebar, SidebarContent, SidebarGroup, SidebarMenu, etc.).
   - Navegación Principal: "Inicio", "Transacciones", "Reportes", "Configuración".
   - Footer del Sidebar: Incluye un componente NavUser.tsx que muestre los datos del usuario desde useUserStore.

2. COMPONENTE HEADER:
   - Crea src/shared/layouts/components/DashboardHeader.tsx.
   - Debe incluir el SidebarTrigger (para colapsar/expandir), un Breadcrumb dinámico y un buscador (Input).

3. LAYOUT MAESTRO (DashboardLayout.tsx):
   - Actualiza src/shared/layouts/DashboardLayout.tsx.
   - Envuelve toda la estructura en el SidebarProvider.
   - Implementa el AppSidebar y el DashboardHeader.
   - El área principal debe tener un fondo bg-muted/50 y utilizar el componente <Outlet /> de react-router-dom para renderizar el contenido de los módulos.

# REQUISITOS DE CALIDAD
- No crees lógica de colapsado manual; usa las propiedades nativas de shadcn sidebar.
- Asegura que el layout sea totalmente responsivo (el sidebar debe convertirse automáticamente en un Drawer móvil).

# FORMATO DE SALIDA
1. Código de AppSidebar.tsx y NavUser.tsx.
2. Código de DashboardHeader.tsx.
3. Código final de DashboardLayout.tsx integrando el SidebarProvider.

---

# ROLE

Actúa como Senior Frontend Engineer. Tu objetivo es construir el componente NavHeader.tsx en src/shared/layouts/components/, integrando navegación inteligente y controles de sistema.

# CONTEXTO TÉCNICO
- Arquitectura: Modular.
- Dependencias: shadcn/ui (Breadcrumb, Command, Popover, Button, Input), Lucide React (iconos), next-themes (o el sistema de temas que uses).
- Estado: Integrar con useUserStore para los datos del usuario.

# TAREAS ESPECÍFICAS

1. BREADCRUMBS DINÁMICOS:
   - Implementa lógica en el NavHeader para que los breadcrumbs se generen automáticamente basados en la URL actual (location.pathname de react-router-dom).
   - Debe mapear rutas (ej: /dashboard/transactions -> Dashboard > Transacciones).

2. COMMAND MENU (Buscador Ctrl+K):
   - Configura el componente Command de shadcn para crear una paleta de comandos.
   - Atajo de teclado: Debe abrirse al presionar Ctrl + K (o Cmd + K).
   - Funcionalidad: Permitir buscar y navegar rápidamente a: "Inicio", "Transacciones", "Reportes", "Configuración".

3. THEME TOGGLE:
   - Añade un botón que permita cambiar entre temas: "Light", "Dark" y "System".
   - Asegura que el icono cambie según el tema activo (Sun/Moon).

4. INTEGRACIÓN NAVUSER:
   - Coloca el componente NavUser (que contiene el avatar, nombre y opciones de configuración/logout) en el extremo derecho del header.

5. ESTRUCTURA DEL HEADER:
   - Izquierda: SidebarTrigger + Separador Vertical + Breadcrumbs Dinámicos.
   - Centro: Botón de búsqueda visual que indique "Press Ctrl+K".
   - Derecha: ThemeToggle + NavUser.

# REQUISITOS DE CALIDAD
- Accesibilidad: El Command Menu debe ser totalmente navegable con teclado.
- Limpieza: Separa la lógica del Command Menu en un subcomponente o hook si el archivo se vuelve muy extenso.
- Estilo: El header debe tener un backdrop-blur y bordes sutiles para un acabado moderno.

# FORMATO DE SALIDA
1. Código de NavHeader.tsx.
2. Código de un subcomponente CommandMenu.tsx (si aplica).
3. Código del ThemeToggle.tsx.

---

# ROLE

Actúa como Senior Frontend Engineer experto en Clean Architecture. Tu objetivo es implementar la infraestructura del módulo de Transacciones en src/modules/transactions basándote en un JSON de respuesta del backend, pero aplicando un Patrón Adaptador (Mapper).

# CONTEXTO TÉCNICO
- Dominio: Gestión de Ingresos y Gastos.
- Backend Response (JSON):
  { "reportId": 3, "userId": "user", "period": "2026-02", "totalIncome": 1000.0, "totalExpense": 0.0, "balance": 1000.0, "createdAt": "...", "updatedAt": "..." }

# TAREAS ESPECÍFICAS

1. DEFINICIÓN DE MODELOS (Interfaces):
   - Crea src/modules/transactions/types/transaction.types.ts.
   - Define la interfaz TransactionResponse (exactamente como viene del JSON).
   - Define la interfaz TransactionModel como queremos que se use en el frontend, ej: camelCase o nombres más semánticos en inglés.

2. PATRÓN ADAPTADOR (Mapper):
   - Crea src/modules/transactions/adapters/transaction.adapter.ts.
   - Implementa una función transactionAdapter que transforme el objeto del backend (TransactionResponse) al modelo del frontend (TransactionModel).
   - Asegúrate de manejar valores por defecto (null-safety) y formatear las fechas si es necesario.

3. SERVICIO DE API (Axios + TanStack Query):
   - Crea src/modules/transactions/services/transactionService.ts usando la instancia de httpClient ya configurada.
   - Implementa la petición GET. La función debe aplicar el adaptador inmediatamente después de recibir la respuesta: .then(res => transactionAdapter(res.data)).

4. HOOK DE DATOS:
   - Crea src/modules/transactions/hooks/useGetTransactions.ts usando useQuery de TanStack Query para consumir el servicio.

5. ESTADO GLOBAL (Zustand):
   - Crea src/modules/transactions/store/useTransactionStore.ts por si necesitamos almacenar el último reporte consultado de forma global.

# REQUISITOS DE CALIDAD
- No implementes UI todavía, solo la lógica de datos y adaptadores.
- Sigue el principio de "Single Source of Truth": el componente solo debe conocer el TransactionModel, nunca el JSON crudo del backend.

# FORMATO DE SALIDA
1. Código de los Types (Interfaces).
2. Código del Adaptador (Mapper).
3. Código del Servicio y el Hook.

--- 

# ROLE
Actúa como Senior Frontend Developer experto en Diseño de Sistemas. Tu objetivo es implementar la interfaz completa del CRUD de transacciones en el módulo src/modules/transactions usando shadcn/ui.

# CONTEXTO TÉCNICO
- Arquitectura: Modular.
- UI: shadcn/ui (Data Table, Dialog, Form, Toast).
- Estado: Zustand para el estado local y TanStack Query para la sincronización con el servidor.
- Backend: Los datos vienen del backend-api (Producer).

# TAREAS ESPECÍFICAS (UI CRUD)

1. DATA TABLE (Visualización):
   - Crea src/modules/transactions/components/TransactionTable.tsx utilizando el componente DataTable de shadcn.
   - Columnas: Fecha, Concepto, Categoría (Badge), Monto (con formato de moneda y color: rojo para egreso, verde para ingreso) y Acciones.

2. FORMULARIO DE EDICIÓN (Modal):
   - Crea src/modules/transactions/components/TransactionForm.tsx.
   - Debe ser un formulario reutilizable tanto para "Crear" como para "Editar".
   - Campos: Monto, Tipo (Ingreso/Egreso), Categoría (Select), Fecha (DatePicker) y Descripción.

3. ACCIONES CRUD (Modales y Alertas):
   - Implementa un DropdownMenu en cada fila para "Editar" y "Eliminar".
   - El botón "Eliminar" debe disparar un AlertDialog de confirmación.
   - El botón "Editar" debe abrir un Dialog (Modal) con el formulario precargado.

4. NOTIFICACIONES:
   - Usa el hook useToast de shadcn para confirmar cuando una transacción ha sido modificada o eliminada exitosamente.

# REQUISITOS DE CALIDAD
- No escribas lógica de API aquí; usa placeholders para las mutaciones de TanStack Query (ej. useMutation).
- Asegúrate de que el diseño sea Responsive (apilar columnas en móviles si es necesario).
- Accesibilidad: Los modales deben poder cerrarse con la tecla ESC y tener el foco correcto.

# FORMATO DE SALIDA
1. Código de columns.tsx (Definición de columnas de la tabla).
2. Código de TransactionTable.tsx.
3. Código de TransactionForm.tsx (con Zod).

---

# ROLE
Actúa como Senior Frontend Developer. Tu objetivo es actualizar la implementación de la tabla de transacciones en src/modules/transactions para incluir capacidades de paginación y búsqueda, eliminando cualquier funcionalidad de edición o borrado.

# CONTEXTO TÉCNICO
- UI: shadcn/ui (DataTable, Input, Button, Pagination).
- Estado: TanStack Table para la lógica de la tabla.
- API: transactionService.ts (Solo se usarán métodos GET y POST).
- Requisito: Retirar los métodos 'Update' y 'Delete' tanto del servicio como de la interfaz (columnas).

# TAREAS ESPECÍFICAS

1. LIMPIEZA DE CÓDIGO (Refactor):
   - Elimina los métodos updateTransaction y deleteTransaction de src/modules/transactions/services/transactionService.ts.
   - En src/modules/transactions/components/columns.tsx, elimina la columna de "Acciones" (DropdownMenu) que permitía editar o borrar.

2. IMPLEMENTACIÓN DE BÚSQUEDA (Filtering):
   - Añade un Input de búsqueda sobre la tabla para filtrar transacciones por "Concepto" o "Descripción".
   - Implementa el filtrado del lado del cliente utilizando la función onColumnFiltersChange de TanStack Table.

3. IMPLEMENTACIÓN DE PAGINACIÓN:
   - Añade los controles de paginación de shadcn debajo de la tabla (botones de Anterior, Siguiente y contador de páginas).
   - Configura el estado inicial para mostrar 10 filas por página.

4. UI REFINADA (TransactionPage.tsx):
   - El encabezado solo debe mostrar el buscador y el botón "Nueva Transacción".
   - Asegura que la tabla maneje correctamente el estado de "Sin resultados" cuando el filtro no coincida con ninguna transacción.

# REQUISITOS DE CALIDAD
- No dejes código muerto (Dead Code); si una función no se usa, elimínala.
- El diseño debe mantenerse limpio y profesional siguiendo la estética de shadcn.
- Todo el código debe ser Type-Safe.

# FORMATO DE SALIDA
1. Código actualizado de transactionService.ts (Limpio).
2. Código actualizado de columns.tsx (Sin acciones).
3. Código de TransactionPage.tsx con el buscador e integración de paginación.

---

# ROLE
Actúa como Senior Frontend Developer. Tu tarea es sincronizar el frontend con el nuevo contrato de datos enviado por el Backend (Spring Boot Producer).

# CONTEXTO DEL CAMBIO
El objeto de transacción ha sido modificado en el backend. Ahora el JSON tiene la siguiente estructura:
{
   "transactionId": number,
   "userId": string,
   "type": "INCOME" | "EXPENSE",
   "amount": number,
   "category": string,
   "date": string (ISO 8601),
   "description": string
}

# TAREAS ESPECÍFICAS

1. ACTUALIZACIÓN DE TIPOS (TypeScript):
   - Modifica el archivo de tipos/interfaces (ej. src/shared/types/index.ts o el específico del módulo) para reflejar exactamente estos nombres de campos.

2. REFACTORIZACIÓN DEL ADAPTER:
   - Actualiza el adapter en src/modules/transactions/services/transactionService.ts para que mapee correctamente los datos de la API hacia la aplicación.
   - Asegúrate de que no queden referencias a nombres de campos antiguos (como 'id' o 'user_id').

3. AJUSTE DE COLUMNAS (Data Table):
   - Actualiza src/modules/transactions/components/DataTable.tsx para que el accessorKey de cada columna coincida con los nuevos nombres del JSON (ej. de id a transactionId).

4. FORMULARIO Y SCHEMAS:
   - Verifica que el transactionSchema.ts (Zod) y el formulario coincidan con estos campos para que el POST sea exitoso.

# REQUISITOS DE CALIDAD
- Realiza una búsqueda global de los términos antiguos para asegurar que no queden errores de "undefined" en la UI.
- Mantén el tipado estricto.

# FORMATO DE SALIDA
1. Código actualizado de la Interface de TypeScript.
2. Código actualizado de transactionService.tsx.
3. Código actualizado del servicio/adapter.

---

# ROLE
Actúa como Senior Frontend Engineer experto en UI/UX. Tu objetivo es refactorizar el componente DataTable.tsx en src/modules/transactions/components/ para transformarlo en una interfaz de gestión completa.

# CONTEXTO TÉCNICO
- UI: shadcn/ui (DataTable, Popover, Command, Badge, Separator).
- Lógica: TanStack Table (useReactTable).
- Iconos: Lucide React (Filter, ListFilter, Plus).

# TAREAS ESPECÍFICAS

1. ENCABEZADO DE TABLA (Contexto):
   - Añade una sección de encabezado dentro del contenedor de la tabla.
   - Debe incluir un Título (ej: "Listado de Transacciones") y una breve descripción (ej: "Gestiona y visualiza tus movimientos financieros").

2. BARRA DE HERRAMIENTAS (Toolbar):
   - Implementa un área de herramientas (DataTableToolbar) sobre la tabla.
   - Debe integrar el input de búsqueda ya existente (por descripción).
   - Añade un Botón de "Filtros" que despliegue un Faceted Filter (filtro facetado) de shadcn.

3. FILTROS AVANZADOS (Categoría y Tipo):
   - Implementa filtros específicos para:
     - Tipo: INCOME (Ingreso) / EXPENSE (Egreso).
     - Categoría: (Basado en los valores únicos de tus datos, ej: Ocio, Comida, Salario).
   - El filtro debe mostrar un Badge con el número de elementos seleccionados.

4. BOTÓN DE LIMPIEZA:
   - Si hay filtros activos, debe aparecer un botón "Reset" para limpiar todos los filtros de una sola vez.

# REQUISITOS DE CALIDAD
- No rompas la paginación ya existente.
- Asegura que los filtros sean reactivos y actualicen la tabla instantáneamente.
- El diseño debe ser limpio, alineado a la izquierda para el contexto y a la derecha para las acciones.

# FORMATO DE SALIDA
1. Código actualizado de DataTable.tsx.
2. Código de subcomponentes de soporte si es necesario (ej: DataTableFacetedFilter.tsx).

---

# ROLE
Actúa como Senior Frontend Engineer experto en Clean Architecture. Tu objetivo es implementar la infraestructura del módulo de Reportes en src/modules/reports aplicando el Patrón Adaptador para transformar la respuesta compleja del backend.

# CONTEXTO TÉCNICO
- Dominio: Reportes Financieros Consolidados.
- Endpoints Disponibles (Solo GET):
  1. api/v1/reports/{userId}/summary?startPeriod=YYYY-MM&endPeriod=YYYY-MM: Resumen de reportes generados.
  2. api/v1/reports/{userId}?period=YYYY-MM: Reporte específico por periodo.
- Estructura JSON esperada: Objeto con totales (balance, totalIncome, etc.) y un array reports[].

# TAREAS ESPECÍFICAS

1. DEFINICIÓN DE MODELOS (Interfaces):
   - Crea src/modules/reports/types/report.types.ts.
   - Define ReportResponse (Exactamente como viene el JSON del backend con snake_case o el formato actual).
   - Define ReportModel (Interface limpia para el frontend, con nombres semánticos y tipos claros).
   - Define ReportFilters para manejar los Query Params de period, startPeriod y endPeriod.

2. PATRÓN ADAPTADOR (Mapper):
   - Crea src/modules/reports/adapters/report.adapter.ts.
   - Implementa reportAdapter: transforma el objeto raíz del backend al modelo del frontend.
   - Implementa reportListAdapter: para transformar específicamente el array de reportes individuales.
   - Asegura el manejo de fechas (ISO a formato local) y valores monetarios (formato decimal).

3. SERVICIO DE API (Axios):
   - Crea src/modules/reports/services/reportService.ts usando la instancia de httpClient.
   - Implementa getReportsSummary: recibe userId y el rango de periodos.
   - Implementa getReportByPeriod: recibe userId y un periodo específico.
   - Importante: Ambas funciones deben aplicar su respectivo adaptador antes de devolver el dato: .then(res => reportAdapter(res)).

4. HOOKS DE DATOS (TanStack Query):
   - Crea src/modules/reports/hooks/useGetReportsSummary.ts.
   - Crea src/modules/reports/hooks/useGetReportByPeriod.ts.
   - Configura las queryKeys de forma dinámica para que reaccionen a los cambios en los filtros de periodo.

5. ESTADO GLOBAL (Zustand):
   - Crea src/modules/reports/store/useReportStore.ts.
   - Debe almacenar el currentReport (el último consultado) y los filtros seleccionados por el usuario para persistir la vista entre navegaciones.

# REQUISITOS DE CALIDAD
- No incluyas métodos POST, PUT o DELETE.
- El código debe ser 100% Type-Safe.
- Aplica el principio de Responsabilidad Única: los hooks solo consultan, los adapters solo transforman.

# FORMATO DE SALIDA
- Código completo de los archivos en sus respectivas rutas dentro de src/modules/reports/.

---

# ROLE
Actúa como Senior Frontend Developer experto en UI/UX. Tu objetivo es implementar la interfaz de visualización y consulta del módulo de Reportes en src/modules/reports usando shadcn/ui.

# CONTEXTO TÉCNICO
- Arquitectura: Modular.
- UI: shadcn/ui (Data Table, Card, Badge, Button, Popover, Calendar).
- Estado: Zustand (para persistir filtros) y TanStack Query (para fetching de datos).
- Backend: Los datos son de solo lectura (GET) provenientes del backend-api.

# TAREAS ESPECÍFICAS (UI Reportes)

1. DATA TABLE (Historial de Reportes):
   - Crea src/modules/reports/components/ReportTable.tsx usando el componente DataTable de shadcn.
   - Columnas: Periodo (Mes/Año), Ingresos Totales, Gastos Totales, Balance Neto y Fecha de Generación.
   - Formato: Los montos deben usar formato de moneda. El Balance Neto debe resaltar en verde si es positivo y rojo si es negativo.

2. DASHBOARD CARDS (Resumen Global):
   - Crea src/modules/reports/components/ReportSummaryCards.tsx.
   - Debe mostrar 3 tarjetas superiores con los datos agregados que vienen en la raíz del JSON:
     1. Balance Total.
     2. Total Ingresos del Periodo.
     3. Total Gastos del Periodo.

3. BARRA DE FILTROS (Query Params):
   - Crea src/modules/reports/components/ReportFilters.tsx.
   - Implementa selectores de fecha (DatePicker o Select de Mes/Año) para definir startPeriod y endPeriod.
   - Al cambiar los filtros, debe actualizar la URL mediante Query Params y disparar el refetch de TanStack Query.

4. PÁGINA PRINCIPAL (Layout):
   - Crea src/modules/reports/pages/ReportsPage.tsx.
   - Organiza la vista: Título y Descripción -> Filtros -> Summary Cards -> Data Table.
   - Implementa estados de Loading (Skeletons) y Empty State por si no hay reportes en el rango seleccionado.

# REQUISITOS DE CALIDAD
- Solo Lectura: No implementes formularios de creación, edición o botones de eliminar.
- Consistencia: Usa los Adapters previamente creados para asegurar que la tabla reciba los nombres de campos correctos (camelCase).
- Responsive: Las cards de resumen deben apilarse en una sola columna en dispositivos móviles.

# FORMATO DE SALIDA
1. Código de ReportTableColumns.tsx.
2. Código de ReportSummaryCards.tsx.
3. Código de ReportsPage.tsx integrando el hook useGetReportsSummary.

---

# ROLE
Actúa como Senior Frontend Developer experto en UX. Tu objetivo es mejorar el componente ReportFilters.tsx para optimizar la experiencia de búsqueda de reportes.

# CONTEXTO TÉCNICO
- UI: shadcn/ui (DatePickerWithRange, Button, Label).
- Lógica: React useState para el buffer local y useSearchParams (react-router-dom) para la persistencia en URL.
- Comportamiento: La búsqueda debe ser "bajo demanda" (On-Demand).

# TAREAS ESPECÍFICAS

1. SELECTOR DE RANGO DE FECHAS (Date Picker):
   - Implementa el componente DatePickerWithRange de shadcn.
   - Debe permitir seleccionar un rango (Inicio y Fin).
   - Por defecto, debe mostrar el mes actual si no hay filtros en la URL.

2. LÓGICA DE BUFFER (Estado Local):
   - El componente debe mantener un estado local interno para el rango de fechas seleccionado.
   - **IMPORTANTE**: No debe actualizar los Query Params ni disparar el refetch de TanStack Query mientras el usuario cambia las fechas en el calendario.

3. BOTÓN DE ACCIÓN ("Consultar Reportes"):
   - Añade un botón con el texto "Actualizar Reportes" e icono de Search o Refresh.
   - Al hacer clic en este botón, el componente debe:
     1. Validar que el rango de fechas sea correcto.
     2. Formatear las fechas a YYYY-MM (o el formato que requiera el backend).
     3. Actualizar los Query Params de la URL (startPeriod y endPeriod).

4. FEEDBACK VISUAL:
   - Añade un botón de "Limpiar Filtros" que resetee el calendario al mes actual y limpie la URL.
   - El botón de "Actualizar" debe mostrar un estado de carga (spinner) si la query está en isFetching.

# REQUISITOS DE CALIDAD
- No uses useEffect para disparar la búsqueda; usa el evento onClick del botón.
- Asegura que el DatePicker sea responsivo y no se corte en pantallas pequeñas.
- Sigue la estética de shadcn: bordes sutiles, espaciado gap-4 y tipografía clara.

# FORMATO DE SALIDA
1. Código actualizado de ReportFilters.tsx.

---

# ROLE
Actúa como Technical Writer experto. Tu objetivo es crear un archivo README.md profesional para un proyecto Frontend de Gestión Financiera.

# CONTEXTO TÉCNICO
- Stack: React + Vite + TypeScript + pnpm.
- UI: shadcn/ui y Tailwind CSS.
- Infraestructura: Docker (Nginx) y Variables de Entorno (.env).

# SECCIONES REQUERIDAS
1. Prerrequisitos: Listar versiones necesarias de Node.js (v20+), pnpm y Docker.
2. Instalación Local: 
   - Clonar repositorio.
   - Instalación de dependencias con pnpm.
   - Configuración del archivo .env (explicar cada variable: API_URL, Firebase Keys).
   - Comando para correr en desarrollo.
3. Despliegue con Docker:
   - Explicar el build de la imagen.
   - Uso de Docker Compose para levantar el servicio en el puerto 3000.
4. Scripts Disponibles: Explicar comandos de lint, build y preview.

# REQUISITOS DE CALIDAD
- Usa badges para el stack tecnológico.
- Incluye bloques de código claros para los comandos de terminal.
- El tono debe ser instructivo y directo.

---

# ROLE
Actúa como Arquitecto de Software Senior. Tu objetivo es redactar una sección detallada (o un archivo ARCHITECTURE.md) que explique la ingeniería detrás del proyecto.

# TAREAS ESPECÍFICAS
1. Estructura de Carpetas: Explicar la organización modular:
   - src/core: Configuraciones globales (Axios, Rutas, Temas).
   - src/shared: Componentes UI reutilizables (shadcn), utilidades y hooks globales.
   - src/modules: División por dominios (Transactions, Reports, Auth).
2. Patrones de Diseño:
   - Patrón Adapter (Mapper): Explicar por qué desacoplamos el JSON del Backend de los modelos de la UI.
   - Custom Hooks: Uso de TanStack Query para la lógica de fetching.
   - State Management: Uso de Zustand para estados globales ligeros.
3. Flujo de Datos: Describir el camino desde que se hace un fetch en el Service, pasa por el Adapter, se almacena en el caché de Query y llega al componente de la tabla.
4. Decisiones Técnicas: Explicar por qué se eligió Nginx para servir la SPA y el manejo de rutas.

# REQUISITOS DE CALIDAD
- Incluye una representación visual de la estructura de directorios usando un bloque de texto tipo tree.
- Explica los beneficios de esta arquitectura: Escalabilidad, mantenibilidad y desacoplamiento.