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
Actúa como Senior Frontend Developer. Tu objetivo es implementar el flujo de Registro de Usuarios (Sign Up) dentro del módulo de autenticación (`src/modules/auth`) en `app/frontend`.

# CONTEXTO DEL SISTEMA
- Arquitectura: Modular por dominios.
- Autenticación: Firebase Auth.
- Estado: Zustand (`src/modules/auth/store/useUserStore.ts`).
- Validación: React Hook Form + Zod.
- UI: Componentes de shadcn/ui.

# TAREAS ESPECÍFICAS (Flujo de Registro)

1. AMPLIACIÓN DEL SERVICIO (API/Firebase):
   - Actualiza `src/modules/auth/services/authService.ts`.
   - Implementa la función `registerWithEmail`: debe crear el usuario en Firebase, actualizar el perfil (displayName) y sincronizar el `useUserStore`.
   - Implementa manejo de errores específicos de Firebase (ej: email-already-in-use).

2. COMPONENTES DE INTERFAZ (UI):
   - Crea `src/modules/auth/components/RegisterForm.tsx`.
   - Requisitos del formulario: Nombre completo, Email, Password y Confirmar Password.
   - Esquema Zod: Validar que las contraseñas coincidan y que tengan un mínimo de 8 caracteres.
   - Estilo: Mantener consistencia visual con el LoginForm.

3. PÁGINA DE REGISTRO:
   - Crea `src/modules/auth/pages/RegisterPage.tsx`.
   - Debe incluir un enlace para redirigir a los usuarios que ya tienen cuenta hacia la página de Login.

4. INTEGRACIÓN DE RUTAS:
   - Registrar `RegisterPage.tsx` en `src/core/router/AppRouter.tsx` dentro del grupo de rutas públicas.

# REQUISITOS DE CALIDAD
- Mantener la lógica de negocio separada de la UI (Separation of Concerns).
- Uso estricto de TypeScript para los retornos del servicio de Firebase.

# FORMATO DE SALIDA
1. Código actualizado de `authService.ts` (solo la función nueva).
2. Código de `RegisterForm.tsx` con su esquema Zod.
3. Código de `RegisterPage.tsx`.