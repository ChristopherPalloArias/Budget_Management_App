# 📋 story-initial.md — Especificación de Historias de Usuario
### Budget Management App (AS-IS)
> **Tipo de Documento:** Reconstrucción de Backlog — Basado en funcionalidades actualmente implementadas y observables.
> **Fecha:** 2026-02-19
> **Metodología:** Agile / SCRUM + Diseño Orientado al Dominio (DDD) + Desarrollo Guiado por Comportamiento (BDD)

---

## 1. Descripción General del Proyecto

### 1.1 Resumen Funcional

La **Budget Management App** es una aplicación full-stack de seguimiento financiero, basada en eventos, que permite a los usuarios registrarse, autenticarse, registrar transacciones financieras personales (ingresos y gastos) y visualizar reportes financieros consolidados por período. Soporta dos métodos de autenticación (correo/contraseña y Google OAuth), aplica control de acceso a nivel de ruta y genera resúmenes financieros de forma asíncrona mediante la propagación de eventos a través de un broker de mensajes.

### 1.2 Actores e Roles Identificados

| Actor | Descripción |
|---|---|
| **Usuario Registrado** | Persona que ha creado una cuenta y puede gestionar sus transacciones y reportes. |
| **Invitado (No Autenticado)** | Persona que solo puede acceder a las páginas de inicio de sesión y registro. |
| **Servicio de Transacciones** | Microservicio backend (Spring Boot) que gestiona el CRUD de transacciones y publica eventos. |
| **Servicio de Reportes** | Microservicio backend (Spring Boot) que escucha eventos y agrega resúmenes financieros por usuario por período. |
| **Broker RabbitMQ** | Broker de mensajes que desacopla el Servicio de Transacciones del Servicio de Reportes mediante eventos asíncronos. |
| **Firebase Authentication** | Proveedor de identidad externo que gestiona usuarios, inicio de sesión con correo/contraseña y Google OAuth. |

### 1.3 Desglose de Dominios / Módulos

| Dominio | Módulos / Componentes |
|---|---|
| **Autenticación** | Login (correo + Google), Registro, Guardias de Ruta (ProtectedRoute, PublicRoute), Estado de Auth (Zustand store) |
| **Transacciones** | Crear Transacción, Listar Transacciones, Filtrar y Buscar Transacciones, Formulario, Tabla de Datos |
| **Reportes** | Dashboard de Reportes, Filtros de Período, Tarjetas de Resumen (Ingreso / Gasto / Balance), Tabla de Historial |
| **Integración Basada en Eventos** | TransactionCreatedEvent → RabbitMQ → ReportConsumer → Agregación de Reporte |
| **Infraestructura** | Orquestación con Docker Compose, MySQL (transactions_db, reports_db), CI/CD con GitHub Actions |

---

## 2. Historias de Usuario

---

### 📦 Dominio: Autenticación

---

#### US-001 — Registro de Usuario con Correo y Contraseña

**Descripción:**

> Como **Invitado**,
> quiero **crear una cuenta usando mi nombre completo, correo electrónico, contraseña y confirmación de contraseña**,
> para **poder acceder a la Budget Management App con mi propio espacio personal.**

**Validación INVEST:**

| Principio | ✅ |
|---|---|
| Independiente | El flujo de registro es autónomo y no depende de otras historias. |
| Negociable | Los requisitos de campos están definidos por el esquema Zod y pueden ajustarse. |
| Valioso | Permite a nuevos usuarios acceder al sistema. |
| Estimable | Formulario pequeño y llamada de registro a Firebase claramente delimitada. |
| Pequeño | Una sola página de formulario; acotado al flujo de registro. |
| Comprobable | Cubierto por `RegisterForm.test.tsx` y `RegisterForm.integration.test.tsx`. |

**Criterios de Aceptación (Gherkin):**

```gherkin
Funcionalidad: Registro de Usuario

  Escenario: Registro exitoso con credenciales válidas
    Dado que el usuario está en la página de registro (/register)
    Y el usuario no está autenticado
    Cuando el usuario completa nombre, correo, contraseña y confirmación válidos
    Y hace clic en "Crear Cuenta"
    Entonces la cuenta se crea a través de Firebase Authentication
    Y el usuario es redirigido al dashboard (/dashboard)

  Escenario: Registro falla cuando las contraseñas no coinciden
    Dado que el usuario está en la página de registro
    Cuando el usuario escribe valores distintos en contraseña y confirmar contraseña
    Y hace clic en "Crear Cuenta"
    Entonces se muestra un error de validación bajo el campo de confirmación
    Y la cuenta no es creada

  Escenario: Registro falla cuando falta un campo obligatorio
    Dado que el usuario está en la página de registro
    Cuando el usuario deja uno o más campos obligatorios vacíos
    Y hace clic en "Crear Cuenta"
    Entonces se muestran mensajes de error en línea para cada campo vacío
    Y el formulario no es enviado

  Escenario: Registro falla cuando el correo ya está registrado
    Dado que el usuario ingresa un correo ya registrado en el sistema
    Cuando hace clic en "Crear Cuenta"
    Entonces se muestra un mensaje de error en la parte superior del formulario
    Y el usuario permanece en la página de registro
```

**Actor(es):** Invitado  
**Componentes Relacionados:** `RegisterForm.tsx`, `RegisterPage.tsx`, `authService.ts`, `authRepository` (Firebase), `registerSchema.ts`  
**Dependencias:** Proyecto de Firebase Authentication configurado y accesible.

---

#### US-002 — Inicio de Sesión con Correo y Contraseña

**Descripción:**

> Como **Usuario Registrado**,
> quiero **iniciar sesión usando mi correo electrónico y contraseña**,
> para **acceder de forma segura a mis datos financieros personales.**

**Validación INVEST:**

| Principio | ✅ |
|---|---|
| Independiente | Flujo de login autónomo, independiente del registro. |
| Negociable | Reglas de validación y mensajes de error pueden ajustarse. |
| Valioso | Punto de entrada principal a la aplicación protegida. |
| Estimable | Bien delimitado: un formulario, una llamada al servicio. |
| Pequeño | Acotado a la página de login y actualización del estado de auth. |
| Comprobable | Cubierto por `LoginForm.test.tsx` y `LoginForm.integration.test.tsx`. |

**Criterios de Aceptación (Gherkin):**

```gherkin
Funcionalidad: Inicio de Sesión con Correo y Contraseña

  Escenario: Inicio de sesión exitoso con credenciales válidas
    Dado que el usuario está en la página de inicio de sesión (/login)
    Cuando el usuario ingresa su correo registrado y la contraseña correcta
    Y hace clic en "Iniciar Sesión"
    Entonces el sistema autentica al usuario a través de Firebase
    Y el usuario es redirigido al dashboard

  Escenario: Inicio de sesión falla con contraseña incorrecta
    Dado que el usuario está en la página de inicio de sesión
    Cuando ingresa un correo válido con una contraseña incorrecta
    Y hace clic en "Iniciar Sesión"
    Entonces se muestra un mensaje de error en la parte superior del formulario
    Y el usuario permanece en la página de login

  Escenario: El formulario se deshabilita mientras la solicitud está en curso
    Dado que el usuario envía el formulario de login
    Entonces el botón "Iniciar Sesión" se deshabilita
    Y su texto cambia a "Iniciando sesión..."
    Y todos los campos del formulario quedan deshabilitados mientras carga
```

**Actor(es):** Usuario Registrado  
**Componentes Relacionados:** `LoginForm.tsx`, `LoginPage.tsx`, `useLoginForm.ts`, `authService.ts`, `loginSchema.ts`  
**Dependencias:** Firebase Authentication configurado.

---

#### US-003 — Inicio de Sesión con Google OAuth

**Descripción:**

> Como **Invitado**,
> quiero **iniciar sesión usando mi cuenta de Google**,
> para **acceder al sistema sin crear un nombre de usuario y contraseña nuevos.**

**Validación INVEST:**

| Principio | ✅ |
|---|---|
| Independiente | El login con Google es una vía alternativa que no afecta al login por correo. |
| Negociable | El proveedor de Google puede ser reemplazado o ampliado con otros OAuth. |
| Valioso | Reduce la fricción para usuarios con cuenta Google. |
| Estimable | Una sola llamada a `authRepository.signInWithProvider('GOOGLE')`. |
| Pequeño | Acotado a un botón en la página de login. |
| Comprobable | Comprobable simulando el popup de Google mediante mocks de Firebase. |

**Criterios de Aceptación (Gherkin):**

```gherkin
Funcionalidad: Inicio de Sesión con Google

  Escenario: Inicio de sesión con Google exitoso
    Dado que el usuario está en la página de inicio de sesión
    Cuando el usuario hace clic en el botón "Google"
    Entonces se dispara el popup de autenticación de Google
    Y tras autenticarse correctamente, el usuario es redirigido al dashboard

  Escenario: El botón Google se deshabilita mientras la solicitud está en curso
    Dado que el usuario hace clic en el botón "Google"
    Entonces el texto del botón cambia a "Conectando..."
    Y tanto el formulario de correo como el botón Google quedan deshabilitados
    Y al completarse, el usuario es redirigido al dashboard
```

**Actor(es):** Invitado  
**Componentes Relacionados:** `LoginForm.tsx`, `useLoginForm.ts`, `authService.loginWithGoogle`, proveedor `GOOGLE` de Firebase  
**Dependencias:** Proyecto de Firebase Authentication con proveedor Google habilitado.

---

#### US-004 — Cierre de Sesión

**Descripción:**

> Como **Usuario Registrado**,
> quiero **cerrar sesión en la aplicación**,
> para **que mi sesión quede cerrada y mi cuenta esté protegida en dispositivos compartidos.**

**Validación INVEST:**

| Principio | ✅ |
|---|---|
| Independiente | La acción de logout es autónoma y no depende de otros flujos. |
| Negociable | El destino de redirección post-logout es configurable. |
| Valioso | Esencial para la seguridad de la cuenta. |
| Estimable | Una sola llamada a `authRepository.signOut()`. |
| Pequeño | Una sola acción, acotada al cierre de sesión. |
| Comprobable | Verificable comprobando el estado de auth tras el logout. |

**Criterios de Aceptación (Gherkin):**

```gherkin
Funcionalidad: Cierre de Sesión

  Escenario: El usuario cierra sesión correctamente
    Dado que el usuario está autenticado y visualiza cualquier página protegida
    Cuando el usuario activa la acción de cerrar sesión
    Entonces el sistema llama a Firebase signOut
    Y el estado de auth del usuario se limpia del store de la aplicación
    Y el usuario es redirigido a la página de login

  Escenario: El cierre de sesión falla de forma controlada
    Dado que la solicitud de Firebase signOut encuentra un error
    Cuando el usuario intenta cerrar sesión
    Entonces se muestra un mensaje de error descriptivo al usuario
    Y la sesión permanece activa
```

**Actor(es):** Usuario Registrado  
**Componentes Relacionados:** `authService.logout`, `authRepository.signOut`  
**Dependencias:** Sesión activa de Firebase autenticada.

---

#### US-005 — Control de Acceso a Rutas para Usuarios No Autenticados

**Descripción:**

> Como **Invitado**,
> quiero **ser redirigido automáticamente a la página de login cuando intento acceder a un área protegida**,
> para **que los usuarios no autorizados no puedan acceder a datos financieros.**

**Validación INVEST:**

| Principio | ✅ |
|---|---|
| Independiente | La lógica del guardia de ruta está desacoplada del contenido de las páginas. |
| Negociable | El destino de redirección es configurable. |
| Valioso | Medida de seguridad fundamental para la aplicación. |
| Estimable | Acotado a la lógica del componente `ProtectedRoute`. |
| Pequeño | Un componente con una redirección condicional. |
| Comprobable | Cubierto por `ProtectedRoute.test.tsx`. |

**Criterios de Aceptación (Gherkin):**

```gherkin
Funcionalidad: Control de Acceso a Rutas

  Escenario: Usuario no autenticado accede a una ruta protegida
    Dado que el usuario no está autenticado
    Cuando navega directamente a una URL protegida (ej: /dashboard, /transactions, /reports)
    Entonces el sistema lo redirige a /login

  Escenario: Usuario autenticado puede acceder a rutas protegidas
    Dado que el usuario está autenticado
    Cuando navega a /transactions o /reports
    Entonces la página solicitada se muestra con normalidad

  Escenario: Verificación del estado de auth en progreso
    Dado que el usuario carga la aplicación
    Y el estado de auth aún no ha sido verificado por Firebase
    Entonces se muestra un spinner de carga
    Y no ocurre ninguna redirección hasta que el estado de auth es confirmado
```

**Actor(es):** Invitado, Usuario Registrado  
**Componentes Relacionados:** `ProtectedRoute.tsx`, `PublicRoute.tsx`, `useAuthStatus.ts`  
**Dependencias:** Observable del estado de auth de Firebase.

---

### 📦 Dominio: Transacciones

---

#### US-006 — Crear una Nueva Transacción

**Descripción:**

> Como **Usuario Registrado**,
> quiero **crear una nueva transacción financiera especificando su tipo (ingreso o gasto), descripción, monto, categoría y fecha**,
> para **que mi actividad financiera quede registrada y persista para futuros reportes.**

**Validación INVEST:**

| Principio | ✅ |
|---|---|
| Independiente | El formulario de creación es un diálogo autónomo, independiente de la vista de lista. |
| Negociable | Los campos (descripción, monto, categoría, fecha, tipo) están definidos en el esquema Zod y pueden evolucionar. |
| Valioso | Función principal del sistema; sin ella, no existen datos para reportar. |
| Estimable | Formulario + llamada POST a la API + publicación de evento; alcance bien delimitado. |
| Pequeño | Un solo diálogo de formulario; el backend gestiona un único guardado en repositorio. |
| Comprobable | Cubierto por `TransactionForm.test.tsx`; backend probado en `TransactionServiceImplTest.java`. |

**Criterios de Aceptación (Gherkin):**

```gherkin
Funcionalidad: Crear Transacción

  Escenario: Creación exitosa de un ingreso
    Dado que el usuario está en la página de Transacciones
    Y hace clic en "Nueva Transacción"
    Cuando selecciona tipo "Ingreso", ingresa una descripción, un monto positivo, una categoría y una fecha
    Y hace clic en "Crear Transacción"
    Entonces la transacción se guarda mediante POST /api/v1/transactions
    Y se publica un TransactionCreatedEvent en RabbitMQ
    Y la nueva transacción aparece en la lista de transacciones

  Escenario: Creación exitosa de un gasto
    Dado que el usuario está en la página de Transacciones
    Y hace clic en "Nueva Transacción"
    Cuando selecciona tipo "Egreso" y completa todos los campos requeridos
    Y hace clic en "Crear Transacción"
    Entonces el gasto se guarda y aparece en la lista

  Escenario: Creación falla cuando faltan campos obligatorios
    Dado que el diálogo de creación está abierto
    Cuando el usuario deja descripción, monto o categoría vacíos
    Y hace clic en "Crear Transacción"
    Entonces se muestran errores de validación en línea para cada campo faltante
    Y el formulario no se envía a la API

  Escenario: El monto debe ser mayor a cero
    Dado que el diálogo de creación está abierto
    Cuando el usuario ingresa 0 o un número negativo como monto
    Entonces se muestra el error "El monto debe ser mayor a 0"
    Y el formulario no se envía

  Escenario: Las categorías cambian según el tipo seleccionado
    Dado que el diálogo de creación está abierto
    Cuando el usuario selecciona tipo "Ingreso"
    Entonces el desplegable de categoría muestra: Salario, Negocio, Inversiones, Otros
    Cuando el usuario cambia el tipo a "Egreso"
    Entonces el desplegable muestra: Alimentación, Transporte, Vivienda, Salud, Educación, Entretenimiento, Otros
```

**Actor(es):** Usuario Registrado, Servicio de Transacciones (backend), Broker RabbitMQ  
**Componentes Relacionados:** `TransactionForm.tsx`, `TransactionPage.tsx`, `useTransactionPage.ts`, `TransactionController.java`, `TransactionServiceImpl.java`, `TransactionEventListener.java`, `TransactionMessageProducer.java`  
**Dependencias:** Usuario autenticado; Servicio de Transacciones y RabbitMQ en ejecución.

---

#### US-007 — Ver el Listado de Transacciones

**Descripción:**

> Como **Usuario Registrado**,
> quiero **ver una lista paginada de todas mis transacciones registradas**,
> para **revisar mi historial financiero completo.**

**Validación INVEST:**

| Principio | ✅ |
|---|---|
| Independiente | La vista de lista opera independientemente de crear, filtrar o reportar. |
| Negociable | El tamaño de página, columnas y orden de classification son configurables. |
| Valioso | Proporciona visibilidad sobre la actividad financiera registrada. |
| Estimable | Endpoint GET + renderizado de tabla; bien delimitado. |
| Pequeño | Listado de solo lectura; acotado al componente DataTable. |
| Comprobable | Cubierto por `DataTable.test.tsx`. |

**Criterios de Aceptación (Gherkin):**

```gherkin
Funcionalidad: Ver Listado de Transacciones

  Escenario: El usuario ve una lista paginada al cargar la página
    Dado que el usuario está autenticado y navega a /transactions
    Cuando la página carga
    Entonces se realiza una solicitud GET a /api/v1/transactions con paginación por defecto (10 por página, ordenadas por fecha descendente)
    Y la lista de transacciones se muestra en el DataTable

  Escenario: Se muestra un esqueleto de carga mientras se obtienen los datos
    Dado que el usuario navega a la página de Transacciones
    Cuando los datos aún están siendo obtenidos
    Entonces se muestran filas de esqueleto como marcadores de posición

  Escenario: Se muestra el estado de error cuando la API no está disponible
    Dado que el backend no está disponible
    Cuando el usuario navega a la página de Transacciones
    Entonces se muestra el mensaje "Error al cargar transacciones"
    Y se muestra un botón "Reintentar" para recargar la página
```

**Actor(es):** Usuario Registrado, Servicio de Transacciones  
**Componentes Relacionados:** `TransactionPage.tsx`, `DataTable.tsx`, `TransactionTableRow.tsx`, `TransactionController.java` (GET /api/v1/transactions)  
**Dependencias:** Usuario autenticado; Servicio de Transacciones en ejecución.

---

#### US-008 — Filtrar Transacciones por Tipo

**Descripción:**

> Como **Usuario Registrado**,
> quiero **filtrar la lista de transacciones por tipo (Ingreso o Gasto)**,
> para **centrar mi análisis en una categoría específica de movimientos.**

**Validación INVEST:**

| Principio | ✅ |
|---|---|
| Independiente | El filtro por tipo es independiente de los filtros de categoría y búsqueda. |
| Negociable | Las opciones de tipo (INCOME, EXPENSE) reflejan el enum TransactionType. |
| Valioso | Permite aislar flujos de ingresos o gastos de un vistazo. |
| Estimable | Filtro facetado del lado del cliente; acotado al componente DataTableFacetedFilter. |
| Pequeño | Un solo control de filtro en la barra de herramientas. |
| Comprobable | Cubierto por `DataTableFacetedFilter.test.tsx`. |

**Criterios de Aceptación (Gherkin):**

```gherkin
Funcionalidad: Filtrar Transacciones por Tipo

  Escenario: El usuario filtra por tipo "Ingreso"
    Dado que el usuario está en la página de Transacciones con transacciones de ambos tipos visibles
    Cuando abre el filtro "Tipo" y selecciona "Ingreso"
    Entonces solo se muestran transacciones de tipo INCOME en la tabla

  Escenario: El usuario filtra por tipo "Egreso"
    Dado que el usuario está en la página de Transacciones
    Cuando abre el filtro "Tipo" y selecciona "Egreso"
    Entonces solo se muestran transacciones de tipo EXPENSE en la tabla

  Escenario: El usuario limpia el filtro de tipo activo
    Dado que hay un filtro "Tipo" activo
    Cuando el usuario hace clic en "Limpiar"
    Entonces se vuelven a mostrar todas las transacciones sin importar el tipo
```

**Actor(es):** Usuario Registrado  
**Componentes Relacionados:** `DataTableToolbar.tsx`, `DataTableFacetedFilter.tsx`  
**Dependencias:** Transacciones cargadas en el DataTable.

---

#### US-009 — Filtrar Transacciones por Categoría

**Descripción:**

> Como **Usuario Registrado**,
> quiero **filtrar la lista de transacciones por categoría**,
> para **analizar áreas específicas de gasto o ingreso (ej: Alimentación, Salario).**

**Validación INVEST:**

| Principio | ✅ |
|---|---|
| Independiente | El filtro de categoría es independiente del de tipo y búsqueda. |
| Negociable | Las categorías disponibles provienen de la constante `TRANSACTION_CATEGORIES` y pueden ampliarse. |
| Valioso | Permite un análisis granular de patrones de gasto. |
| Estimable | Filtro facetado del lado del cliente; acotado a DataTableFacetedFilter. |
| Pequeño | Un control de filtro, solo del lado del cliente. |
| Comprobable | Cubierto por `DataTableFacetedFilter.test.tsx`. |

**Criterios de Aceptación (Gherkin):**

```gherkin
Funcionalidad: Filtrar Transacciones por Categoría

  Escenario: El usuario filtra por una categoría de gasto específica
    Dado que el usuario está en la página de Transacciones
    Cuando abre el filtro "Categoría" y selecciona "Alimentación"
    Entonces solo se muestran transacciones categorizadas como Alimentación

  Escenario: El usuario filtra por una categoría de ingreso específica
    Dado que el usuario está en la página de Transacciones
    Cuando selecciona la categoría "Salario"
    Entonces solo se muestran transacciones de ingreso tipo Salario

  Escenario: El usuario limpia el filtro de categoría
    Dado que hay un filtro de categoría activo
    Cuando el usuario hace clic en "Limpiar"
    Entonces se vuelven a mostrar todas las transacciones
```

**Actor(es):** Usuario Registrado  
**Componentes Relacionados:** `DataTableToolbar.tsx`, `DataTableFacetedFilter.tsx`, `TRANSACTION_CATEGORIES`  
**Dependencias:** Transacciones cargadas en el DataTable.

---

#### US-010 — Buscar Transacciones por Descripción

**Descripción:**

> Como **Usuario Registrado**,
> quiero **buscar transacciones escribiendo parte de la descripción (concepto)**,
> para **encontrar rápidamente una transacción específica sin desplazarme por toda la lista.**

**Validación INVEST:**

| Principio | ✅ |
|---|---|
| Independiente | La búsqueda no depende de los filtros por tipo o categoría. |
| Negociable | El criterio de búsqueda (campo descripción) es configurable. |
| Valioso | Agiliza la navegación para usuarios con muchas transacciones. |
| Estimable | Filtro de texto del lado del cliente en la barra de herramientas del DataTable. |
| Pequeño | Un campo de entrada con filtrado en tiempo real. |
| Comprobable | Cubierto por `DataTableToolbar.test.tsx`. |

**Criterios de Aceptación (Gherkin):**

```gherkin
Funcionalidad: Buscar Transacciones por Descripción

  Escenario: El usuario busca una transacción por descripción parcial
    Dado que el usuario está en la página de Transacciones con múltiples transacciones cargadas
    Cuando escribe "Super" en el campo "Filtrar por concepto..."
    Entonces solo se muestran en la tabla las transacciones cuya descripción contiene "Super"

  Escenario: El usuario borra la búsqueda
    Dado que el usuario tiene una búsqueda de texto activa
    Cuando borra el campo de búsqueda
    Entonces se vuelven a mostrar todas las transacciones

  Escenario: La búsqueda no arroja resultados
    Dado que el usuario escribe una cadena que no coincide con ninguna descripción
    Entonces la tabla muestra un estado vacío
```

**Actor(es):** Usuario Registrado  
**Componentes Relacionados:** `DataTableToolbar.tsx`, `DataTable.tsx`  
**Dependencias:** Transacciones cargadas en el DataTable.

---

### 📦 Dominio: Reportes

---

#### US-011 — Ver Reporte Financiero de un Período Específico

**Descripción:**

> Como **Usuario Registrado**,
> quiero **ver un reporte de resumen financiero para un mes determinado (período)**,
> para **comprender mi total de ingresos, gastos y balance de ese período.**

**Validación INVEST:**

| Principio | ✅ |
|---|---|
| Independiente | La consulta de reporte por período es independiente del flujo de creación de transacciones. |
| Negociable | El formato de período (yyyy-MM) y las reglas de validación pueden ajustarse. |
| Valioso | Función principal de reportes; proporciona visibilidad financiera. |
| Estimable | Una llamada GET a un endpoint; acotado a una sola entidad de reporte. |
| Pequeño | Consulta de un solo recurso por userId y período. |
| Comprobable | Cubierto por `ReportServiceImplTest.java`. |

**Criterios de Aceptación (Gherkin):**

```gherkin
Funcionalidad: Ver Reporte de Período

  Escenario: El usuario obtiene un reporte de un período existente
    Dado que el usuario está autenticado
    Cuando se realiza una solicitud GET a /api/v1/reports/{userId}?period=yyyy-MM
    Entonces el sistema devuelve un reporte con totalIncome, totalExpense y balance para ese período

  Escenario: No existe reporte para el período solicitado
    Dado que no existen transacciones en el período solicitado
    Cuando se realiza una solicitud GET a /api/v1/reports/{userId}?period=yyyy-MM
    Entonces la API devuelve HTTP 404 Not Found
```

**Actor(es):** Usuario Registrado, Servicio de Reportes  
**Componentes Relacionados:** `ReportController.java` (GET /api/v1/reports/{userId}), `ReportServiceImpl.java`, `Report.java`  
**Dependencias:** Al menos una transacción debe haberse creado en el período solicitado (el reporte se genera de forma asíncrona vía RabbitMQ).

---

#### US-012 — Ver Resumen Financiero en un Rango de Fechas

**Descripción:**

> Como **Usuario Registrado**,
> quiero **ver un resumen consolidado de mis ingresos, gastos y balance neto a través de un rango de períodos**,
> para **evaluar mi salud financiera general a lo largo del tiempo.**

**Validación INVEST:**

| Principio | ✅ |
|---|---|
| Independiente | La consulta de resumen por rango es independiente de la consulta de período único. |
| Negociable | La granularidad del rango de fechas (períodos mensuales) está fijada por la implementación actual. |
| Valioso | Permite planificación financiera multi-período y detección de tendencias. |
| Estimable | Un endpoint GET con dos parámetros de período; lógica de agregación acotada. |
| Pequeño | Consulta de base de datos con agregación en memoria. |
| Comprobable | Cubierto por `ReportServiceImplTest.java`. |

**Criterios de Aceptación (Gherkin):**

```gherkin
Funcionalidad: Resumen Financiero por Rango de Fechas

  Escenario: El usuario ve un resumen para un rango de fechas válido
    Dado que el usuario está autenticado y tiene reportes en múltiples períodos
    Cuando se realiza GET a /api/v1/reports/{userId}/summary?startPeriod=yyyy-MM&endPeriod=yyyy-MM
    Entonces el sistema devuelve el totalIncome, totalExpense, balance neto e historial de reportes del rango

  Escenario: El resumen muestra los totales correctos
    Dado que existen reportes para 2025-01 (ingreso: 1000, gasto: 300) y 2025-02 (ingreso: 1500, gasto: 700)
    Cuando el usuario solicita el resumen de 2025-01 a 2025-02
    Entonces totalIncome = 2500, totalExpense = 1000, balance = 1500

  Escenario: El resumen con formato de período inválido devuelve error
    Dado que el usuario proporciona un período que no tiene el formato yyyy-MM
    Cuando se realiza la solicitud al endpoint de resumen
    Entonces la API devuelve HTTP 400 Bad Request con un error de validación
```

**Actor(es):** Usuario Registrado, Servicio de Reportes  
**Componentes Relacionados:** `ReportController.java` (GET /api/v1/reports/{userId}/summary), `ReportServiceImpl.getReportsByPeriodRange`, `ReportSummary.java`, `ValidPeriod.java`  
**Dependencias:** Deben existir reportes en el rango de períodos solicitado.

---

#### US-013 — Ver Historial de Reportes Paginado

**Descripción:**

> Como **Usuario Registrado**,
> quiero **navegar por un listado paginado de todos mis reportes financieros mensuales**,
> para **revisar datos históricos y navegar a través de períodos pasados.**

**Validación INVEST:**

| Principio | ✅ |
|---|---|
| Independiente | El listado de historial es independiente del resumen y del reporte por período único. |
| Negociable | El tamaño de página y campo de orderamiento (period DESC) son configurables. |
| Valioso | Permite revisión histórica más allá del rango seleccionado. |
| Estimable | Un endpoint GET con paginación; acotado a recuperación de lista. |
| Pequeño | Listado paginado de solo lectura; acotado a ReportTable. |
| Comprobable | Cubierto por `ReportTable.test.tsx` y `ReportServiceImplTest.java`. |

**Criterios de Aceptación (Gherkin):**

```gherkin
Funcionalidad: Historial de Reportes Paginado

  Escenario: El usuario ve el listado paginado de reportes
    Dado que el usuario está en la página de Reportes
    Cuando se realiza GET a /api/v1/reports/{userId}/all con paginación por defecto (10 por página, ordenados por período DESC)
    Entonces el sistema devuelve una lista paginada de reportes del usuario

  Escenario: Los reportes se muestran en el componente ReportTable
    Dado que los datos de reportes son devueltos por la API
    Cuando la ReportsPage renderiza
    Entonces el ReportTable muestra el historial con columnas de período, ingreso, gasto y balance
```

**Actor(es):** Usuario Registrado, Servicio de Reportes  
**Componentes Relacionados:** `ReportController.java` (GET /api/v1/reports/{userId}/all), `ReportTable.tsx`, `ReportTableColumns.tsx`, `ReportsPage.tsx`  
**Dependencias:** El usuario debe tener al menos un reporte existente.

---

#### US-014 — Filtrar Reportes por Rango de Período en la Interfaz

**Descripción:**

> Como **Usuario Registrado**,
> quiero **aplicar filtros de período de inicio y período de fin desde la página de Reportes**,
> para **acotar la vista de reportes a un rango de tiempo específico sin cambiar la URL manualmente.**

**Validación INVEST:**

| Principio | ✅ |
|---|---|
| Independiente | Los controles de filtro son gestionados por el store Zustand de reportes, independientes de la tabla. |
| Negociable | El formato de la UI de filtros puede rediseñarse sin afectar el contrato del backend. |
| Valioso | Permite la exploración de reportes financieros por parte del usuario. |
| Estimable | Gestión de estado de filtro en Zustand + disparador de refetch; alcance pequeño. |
| Pequeño | Acotado al componente ReportFilters y useReportStore. |
| Comprobable | Cubierto por `ReportFilters.test.tsx`. |

**Criterios de Aceptación (Gherkin):**

```gherkin
Funcionalidad: Filtros de Período en Reportes

  Escenario: El usuario establece un rango de período y ve los resultados filtrados
    Dado que el usuario está en la página de Reportes
    Cuando selecciona un período de inicio (ej: 2025-01) y un período de fin (ej: 2025-06) mediante ReportFilters
    Entonces el resumen y la tabla de reportes se actualizan con los datos filtrados
    Y las tarjetas de resumen muestran el balance, ingresos y gastos consolidados para ese rango

  Escenario: El usuario hace clic en el botón de actualizar para recargar los datos
    Dado que el usuario está en la página de Reportes
    Cuando hace clic en el botón de refrescar/actualizar del componente ReportFilters
    Entonces se realiza una nueva solicitud a la API con los valores de filtro actuales
    Y los datos son actualizados

  Escenario: Se muestra estado de carga mientras se obtienen los datos del reporte
    Dado que el usuario cambia el filtro de período
    Cuando la solicitud a la API está en curso
    Entonces se muestran esqueletos de carga para las tarjetas de resumen y la tabla de reportes
```

**Actor(es):** Usuario Registrado  
**Componentes Relacionados:** `ReportFilters.tsx`, `ReportsPage.tsx`, `useReportStore.ts`, `useGetReportsSummary.ts`  
**Dependencias:** La API del Servicio de Reportes debe estar accesible.

---

#### US-015 — Ver Tarjetas de Resumen Financiero (Ingresos, Gastos, Balance)

**Descripción:**

> Como **Usuario Registrado**,
> quiero **ver tarjetas de resumen consolidado con mis ingresos totales, gastos totales y balance neto para el rango de período seleccionado**,
> para **comprender mi situación financiera de un vistazo.**

**Validación INVEST:**

| Principio | ✅ |
|---|---|
| Independiente | Las tarjetas de resumen se alimentan de la respuesta de la API de resumen; independientes de la tabla. |
| Negociable | El diseño de las tarjetas y las métricas mostradas son configurables. |
| Valioso | Presenta los KPIs financieros más importantes en un formato muy visible. |
| Estimable | Componente presentacional pequeño que consume datos ya obtenidos. |
| Pequeño | Acotado al componente ReportSummaryCards. |
| Comprobable | Cubierto por `ReportSummaryCards.test.tsx`. |

**Criterios de Aceptación (Gherkin):**

```gherkin
Funcionalidad: Tarjetas de Resumen Financiero

  Escenario: Las tarjetas muestran los valores financieros correctos
    Dado que el usuario está en la página de Reportes con un rango de período seleccionado
    Y la API devuelve totalIncome, totalExpenses y balance
    Cuando el componente ReportSummaryCards renderiza
    Entonces se muestran tres tarjetas: "Total Ingresos", "Total Egresos" y "Balance"
    Y cada tarjeta muestra el valor monetario correcto para el rango de período seleccionado

  Escenario: Las tarjetas no se muestran cuando no hay datos disponibles
    Dado que la API no devuelve datos de reportes (ej: sin transacciones en el período)
    Cuando la página renderiza
    Entonces la sección de tarjetas de resumen no se renderiza
```

**Actor(es):** Usuario Registrado  
**Componentes Relacionados:** `ReportSummaryCards.tsx`, `ReportsPage.tsx`, `useGetReportsSummary.ts`  
**Dependencias:** Los datos de resumen de reporte deben ser devueltos por la API.

---

### 📦 Dominio: Integración Basada en Eventos

---

#### US-016 — Actualización Automática del Reporte al Crear una Transacción

**Descripción:**

> Como **Usuario Registrado**,
> quiero que **mis reportes financieros se actualicen automáticamente cuando registro una nueva transacción**,
> para **que mis datos de reporte reflejen siempre mi actividad financiera más reciente sin intervención manual.**

**Validación INVEST:**

| Principio | ✅ |
|---|---|
| Independiente | La agregación de reportes está desacoplada del Servicio de Transacciones a través de RabbitMQ. |
| Negociable | Los nombres de las colas y la configuración del broker están externalizados en propiedades. |
| Valioso | Garantiza que los reportes estén siempre actualizados sin esfuerzo del usuario. |
| Estimable | Un listener de evento, un mensaje RabbitMQ, un upsert de reporte; alcance acotado. |
| Pequeño | Cadena de eventos asíncrona: Publicar → Consumir → Actualizar. |
| Comprobable | Cubierto por `ReportServiceImplTest.java` y escenarios de prueba de integración. |

**Criterios de Aceptación (Gherkin):**

```gherkin
Funcionalidad: Actualización Automática de Reporte al Crear Transacción

  Escenario: Una nueva transacción de INGRESO dispara una actualización del reporte
    Dado que el usuario crea una transacción de INGRESO para el período 2025-03 con monto 500
    Cuando la transacción es guardada y se genera un TransactionCreatedEvent
    Entonces el Servicio de Transacciones publica el evento de forma asíncrona en la cola "transaction-created" de RabbitMQ
    Y el Servicio de Reportes consume el mensaje
    Y el reporte del período 2025-03 es creado o actualizado con totalIncome += 500 y el balance recalculado

  Escenario: Una nueva transacción de GASTO dispara una actualización del reporte
    Dado que el usuario crea una transacción de GASTO para el período 2025-03 con monto 200
    Cuando el evento es publicado y consumido
    Entonces el reporte del período 2025-03 es actualizado con totalExpense += 200 y el balance recalculado

  Escenario: Se crea automáticamente un reporte si no existe para el período
    Dado que no existe ningún reporte para el período 2025-07
    Cuando se crea una transacción para ese período
    Entonces el Servicio de Reportes crea un nuevo registro de reporte con período 2025-07, userId, e inicializa ingreso/gasto/balance
```

**Actor(es):** Servicio de Transacciones (Productor), Broker RabbitMQ, Servicio de Reportes (Consumidor)  
**Componentes Relacionados:** `TransactionServiceImpl.java`, `TransactionCreatedEvent.java`, `TransactionEventListener.java`, `TransactionMessageProducer.java`, `ReportConsumer.java`, `ReportServiceImpl.updateReport`, `ReportRepository`  
**Dependencias:** Broker RabbitMQ en ejecución; Servicio de Reportes suscrito a la cola `transaction-created`.

---

## 3. Matriz de Trazabilidad

| ID Historia | Título | Dominio | Componentes Frontend | Componentes Backend | Base de Datos | Integración |
|---|---|---|---|---|---|---|
| US-001 | Registro de Usuario | Autenticación | `RegisterForm`, `RegisterPage` | Firebase Auth | — | Firebase |
| US-002 | Login con Correo/Contraseña | Autenticación | `LoginForm`, `LoginPage`, `useLoginForm` | Firebase Auth | — | Firebase |
| US-003 | Login con Google | Autenticación | `LoginForm`, `useLoginForm` | Firebase Auth (Proveedor Google) | — | Firebase/Google OAuth |
| US-004 | Cierre de Sesión | Autenticación | `authService.logout` | Firebase Auth | — | Firebase |
| US-005 | Control de Acceso a Rutas | Autenticación | `ProtectedRoute`, `PublicRoute`, `useAuthStatus` | — | — | — |
| US-006 | Crear Transacción | Transacciones | `TransactionForm`, `TransactionPage`, `useTransactionPage` | `TransactionController`, `TransactionServiceImpl` | `transactions_db` | RabbitMQ (publicar evento) |
| US-007 | Ver Listado de Transacciones | Transacciones | `DataTable`, `TransactionTableRow`, `TransactionPage` | `TransactionController` (GET all) | `transactions_db` | — |
| US-008 | Filtrar por Tipo | Transacciones | `DataTableToolbar`, `DataTableFacetedFilter` | — | — | — |
| US-009 | Filtrar por Categoría | Transacciones | `DataTableToolbar`, `DataTableFacetedFilter` | — | — | — |
| US-010 | Buscar por Descripción | Transacciones | `DataTableToolbar`, `DataTable` | — | — | — |
| US-011 | Ver Reporte por Período | Reportes | `ReportsPage` | `ReportController` (GET /{userId}) | `reports_db` | — |
| US-012 | Ver Resumen por Rango | Reportes | `ReportsPage`, `ReportSummaryCards` | `ReportController` (GET /summary) | `reports_db` | — |
| US-013 | Ver Historial de Reportes | Reportes | `ReportTable`, `ReportsPage` | `ReportController` (GET /all) | `reports_db` | — |
| US-014 | Filtrar Reportes por Período | Reportes | `ReportFilters`, `useReportStore` | `ReportController` (GET /summary) | `reports_db` | — |
| US-015 | Tarjetas de Resumen | Reportes | `ReportSummaryCards` | `ReportController` (GET /summary) | `reports_db` | — |
| US-016 | Actualización Automática de Reporte | Basado en Eventos | — | `TransactionEventListener`, `ReportConsumer`, `ReportServiceImpl` | `reports_db` | RabbitMQ |

---

## 4. Mapa de Módulos y Componentes

```
Budget Management App
│
├── 🔐 Dominio: Autenticación
│   ├── Firebase Authentication (Proveedor de Identidad)
│   ├── LoginPage → LoginForm (correo + Google)
│   ├── RegisterPage → RegisterForm
│   ├── ProtectedRoute (protege /transactions, /reports, /dashboard)
│   └── PublicRoute (evita acceso a /login, /register si ya autenticado)
│
├── 💸 Dominio: Transacciones
│   ├── TransactionPage
│   │   ├── DataTable (listado + paginación)
│   │   ├── DataTableToolbar (búsqueda + filtro por tipo + filtro por categoría)
│   │   └── TransactionForm (diálogo de creación)
│   └── Servicio de Transacciones (Spring Boot — transactions_db)
│       ├── POST /api/v1/transactions (crear + publicar evento)
│       └── GET  /api/v1/transactions (listar todos, paginado)
│
├── 📊 Dominio: Reportes
│   ├── ReportsPage
│   │   ├── ReportFilters (selector de rango de período)
│   │   ├── ReportSummaryCards (KPIs: ingreso / gasto / balance)
│   │   └── ReportTable (historial paginado)
│   └── Servicio de Reportes (Spring Boot — reports_db)
│       ├── GET /api/v1/reports/{userId}         (período único)
│       ├── GET /api/v1/reports/{userId}/all     (historial paginado)
│       └── GET /api/v1/reports/{userId}/summary (agregación por rango de fechas)
│
└── 🔄 Integración Basada en Eventos
    ├── RabbitMQ (Broker de Mensajes)
    │   ├── Cola: transaction-created → ReportConsumer.consumeCreated()
    │   └── Cola: transaction-updated → ReportConsumer.consumeUpdated()
    └── Agregación de Reporte: updateReport() → getOrCreateReport() → upsert
```

---

*Este documento fue generado mediante ingeniería inversa del código fuente al 2026-02-19. Todas las historias de usuario representan funcionalidades actualmente implementadas y observables.*
