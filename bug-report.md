# Bug Report – Error al cargar transacciones en estado vacío

---

## Resumen del Bug

| Campo        | Valor                                                                 |
|--------------|-----------------------------------------------------------------------|
| **ID**       | BUG-TXN-001                                                          |
| **Título**   | `(intermediate value).data.map is not a function` al cargar transacciones sin registros |
| **Estado**   | 🟡 Nuevo                                                              |
| **Severidad**| 🔴 Alta (Bloqueante — la página de transacciones es completamente inaccesible) |
| **Prioridad**| P1 — Alta                                                             |
| **Módulo**   | Módulo de Transacciones (frontend + backend)                          |
| **Reportado**| 2026-02-20                                                            |
| **Etiquetas**| `bug`, `frontend`, `backend`, `api-contract`, `data-mapping`, `empty-state`, `P1`, `transactions` |

---

## Descripción Funcional

### Contexto

La aplicación **Budget Management App** es un sistema de gestión de presupuestos con arquitectura de microservicios (backend en Java/Spring Boot) y frontend en React (TypeScript). El módulo de transacciones permite al usuario visualizar, crear y gestionar sus transacciones financieras.

### Descripción detallada del problema

Cuando un usuario **sin transacciones registradas** accede a la página de transacciones, la aplicación lanza un error de ejecución de JavaScript en lugar de mostrar un estado vacío (*empty state*). El error impide por completo la visualización y operación de la página de transacciones.

**Mensaje de error:**

```
(intermediate value).data.map is not a function
```

El mensaje se presenta al usuario mediante el componente `TransactionPageError` con el texto *"Error al cargar transacciones"*.

### Comportamiento esperado

- La página de transacciones se carga correctamente.
- Si el usuario no tiene transacciones, se muestra un **empty state** indicando que no hay transacciones disponibles.
- El usuario puede crear nuevas transacciones desde esta vista.

### Comportamiento actual

- Se muestra un componente de error con el mensaje: *"Error al cargar transacciones"*.
- El detalle técnico del error es: `(intermediate value).data.map is not a function`.
- La página es **completamente inaccesible** — no se puede ver ni crear transacciones.

---

## Pasos para Reproducir

### Precondiciones

- La aplicación debe estar ejecutándose (frontend + backend de transacciones).
- El usuario debe estar autenticado.
- El usuario **no debe tener transacciones registradas** en la base de datos.

### Pasos

1. Iniciar sesión con un usuario que no tiene transacciones registradas.
2. Navegar a la página de **Transacciones** (ruta del módulo transactions).
3. Esperar a que la página intente cargar las transacciones.
4. **Resultado:** Se muestra el componente de error `TransactionPageError` con el mensaje *"Error al cargar transacciones"* y el detalle `(intermediate value).data.map is not a function`.

### Datos y condiciones necesarias

- Usuario autenticado con `userId` válido.
- Cero (0) transacciones asociadas a ese usuario en la base de datos.
- Backend del microservicio de transacciones activo y respondiendo.

---

## Análisis Técnico

### Componentes / Módulos afectados

| Capa      | Archivo                                           | Rol                                    |
|-----------|---------------------------------------------------|----------------------------------------|
| Frontend  | [`transactionService.ts`](file:///Users/jeanvillacis/Documents/Project-G2/Budget_Management_App/app/Frontend/src/modules/transactions/services/transactionService.ts) | Servicio que invoca la API y mapea la respuesta |
| Frontend  | [`useTransactions.ts`](file:///Users/jeanvillacis/Documents/Project-G2/Budget_Management_App/app/Frontend/src/modules/transactions/hooks/useTransactions.ts) | Hook que consume el servicio via React Query |
| Frontend  | [`transaction.adapter.ts`](file:///Users/jeanvillacis/Documents/Project-G2/Budget_Management_App/app/Frontend/src/modules/transactions/adapters/transaction.adapter.ts) | Adaptador de la respuesta de API al modelo del frontend |
| Frontend  | [`TransactionPage.tsx`](file:///Users/jeanvillacis/Documents/Project-G2/Budget_Management_App/app/Frontend/src/modules/transactions/pages/TransactionPage.tsx) | Página que renderiza las transacciones o el error |
| Frontend  | [`HttpClient.ts`](file:///Users/jeanvillacis/Documents/Project-G2/Budget_Management_App/app/Frontend/src/core/api/HttpClient.ts) | Cliente HTTP basado en Axios |
| Backend   | [`TransactionController.java`](file:///Users/jeanvillacis/Documents/Project-G2/Budget_Management_App/app/backend-microservice/transaction/src/main/java/com/microservice/transaction/controller/TransactionController.java) | Controlador REST del endpoint GET |
| Backend   | [`TransactionServiceImpl.java`](file:///Users/jeanvillacis/Documents/Project-G2/Budget_Management_App/app/backend-microservice/transaction/src/main/java/com/microservice/transaction/service/impl/TransactionServiceImpl.java) | Implementación del servicio que consulta la BD |
| Backend   | [`PaginatedResponse.java`](file:///Users/jeanvillacis/Documents/Project-G2/Budget_Management_App/app/backend-microservice/transaction/src/main/java/com/microservice/transaction/dto/PaginatedResponse.java) | DTO de respuesta paginada |

### Flujo de ejecución afectado

```
TransactionPage.tsx
  → useTransactionPage.ts
    → useTransactions.ts (React Query)
      → getTransactionsByUser() [transactionService.ts, línea 12-22]
        → HttpClient.get<TransactionItemResponse[]>(endpoint) [Axios]
          → GET /v1/transactions?userId={userId}
            → TransactionController.getAll() [Backend]
              → TransactionServiceImpl.getAll(pageable)
                → Retorna PaginatedResponse<TransactionResponse>
```

### Código involucrado — Punto exacto del error

El error ocurre en [`transactionService.ts`, línea 21](file:///Users/jeanvillacis/Documents/Project-G2/Budget_Management_App/app/Frontend/src/modules/transactions/services/transactionService.ts#L21):

```typescript
const response = await transactionsHttpClient.get<TransactionItemResponse[]>(endpoint);
return response.data.map(transactionAdapter);  // ← ERROR AQUÍ
```

El frontend asume que `response.data` es un `TransactionItemResponse[]` (array), pero la realidad es diferente.

### Validación del contrato de la API

#### Lo que el Frontend espera

El frontend invoca `GET /v1/transactions?userId={userId}` y espera recibir un **array directo** de objetos `TransactionItemResponse`:

```json
[
  { "transactionId": 1, "userId": "abc", "amount": 100, ... },
  { "transactionId": 2, "userId": "abc", "amount": 200, ... }
]
```

#### Lo que el Backend realmente retorna

El controlador [`TransactionController.java`, línea 38-43](file:///Users/jeanvillacis/Documents/Project-G2/Budget_Management_App/app/backend-microservice/transaction/src/main/java/com/microservice/transaction/controller/TransactionController.java#L38-L43) define el endpoint `GET /api/v1/transactions` con tipo de retorno `ResponseEntity<PaginatedResponse<TransactionResponse>>`:

```java
@GetMapping
public ResponseEntity<PaginatedResponse<TransactionResponse>> getAll(
    @PageableDefault(size = 10, page = 0, sort = "date", direction = Sort.Direction.DESC) Pageable pageable) {
    PaginatedResponse<TransactionResponse> transactions = transactionService.getAll(pageable);
    return ResponseEntity.ok(transactions);
}
```

La estructura real del JSON de respuesta es un **objeto paginado**, no un array:

```json
{
  "content": [ ... ],    // ← El array de transacciones está aquí
  "page": 0,
  "size": 10,
  "totalElements": 0,
  "totalPages": 0,
  "last": true
}
```

Definición del DTO [`PaginatedResponse.java`](file:///Users/jeanvillacis/Documents/Project-G2/Budget_Management_App/app/backend-microservice/transaction/src/main/java/com/microservice/transaction/dto/PaginatedResponse.java):

```java
public record PaginatedResponse<T>(
    List<T> content,
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean last) {
}
```

#### Discrepancia adicional: Parámetro `userId` ignorado

El frontend envía `?userId={userId}` como query parameter, pero el backend **no acepta ni utiliza** este parámetro. El método `getAll()` del controlador solo recibe `Pageable` y retorna **todas** las transacciones del sistema sin filtrar por usuario.

### Análisis del error (`.map()` sobre dato no iterable)

La cadena de ejecución que produce el error es la siguiente:

1. Axios realiza el `GET` y recibe un HTTP 200 con el body JSON del `PaginatedResponse`.
2. Axios envuelve la respuesta: `response.data` = `{ content: [], page: 0, size: 10, ... }` (un **objeto**, no un array).
3. El código ejecuta `response.data.map(transactionAdapter)`.
4. Como `response.data` es un **objeto plano** (no un array), **no tiene el método `.map()`**.
5. Se lanza: **`TypeError: (intermediate value).data.map is not a function`**.
6. React Query captura el error y lo propaga al componente `TransactionPage.tsx`, que lo renderiza mediante `TransactionPageError`.

### Causa raíz (Root Cause Analysis)

> **El bug tiene origen compartido (frontend + backend) debido a una ruptura del contrato API.**

| # | Causa                                                                                      | Capa     |
|---|--------------------------------------------------------------------------------------------|----------|
| 1 | **Contrato API desalineado:** El frontend espera un `TransactionItemResponse[]` pero el backend retorna un `PaginatedResponse<TransactionResponse>` (objeto con campo `content`). | Compartido |
| 2 | **Ausencia de validación defensiva:** El servicio frontend (`transactionService.ts`, L21) ejecuta `.map()` directamente sobre `response.data` sin verificar que sea un array. | Frontend |
| 3 | **Tipo genérico engañoso:** El `HttpClient.get<TransactionItemResponse[]>()` define el tipo genérico como array, pero esto es solo una anotación de TypeScript — no valida la respuesta en tiempo de ejecución. | Frontend |
| 4 | **Parámetro `userId` ignorado:** El endpoint backend ignora el query param `userId`, lo cual indica que la integración frontend-backend no fue coordinada. | Backend  |

---

## Impacto

### Impacto en el usuario

- 🔴 **Bloqueante:** Todo usuario nuevo o sin transacciones **no puede acceder** a la página de transacciones.
- Impide la creación de la primera transacción, ya que el formulario de creación se accede desde esta misma página.
- Degrada severamente la primera experiencia del usuario (*first-time user experience*).
- No hay workaround disponible para el usuario final.

### Impacto en el sistema

- El módulo de transacciones queda **completamente inoperativo** para usuarios sin datos previos.
- React Query reintenta la consulta 2 veces (`retry: 2` en [`useTransactions.ts`, línea 26](file:///Users/jeanvillacis/Documents/Project-G2/Budget_Management_App/app/Frontend/src/modules/transactions/hooks/useTransactions.ts#L26)), generando 3 llamadas fallidas al backend por cada acceso.
- Posible pérdida de confianza del usuario y abandono de la aplicación.

### Riesgos asociados

- **Seguridad:** El endpoint `getAll()` retorna transacciones de **todos** los usuarios sin filtrar, lo cual es una vulnerabilidad de exposición de datos si se corrige solo el mapeo sin agregar filtrado por usuario.
- **Escalabilidad:** Si se resuelve solo el mapeo y se ignoran los campos de paginación, la página intentará renderizar todas las transacciones sin paginación del lado del cliente.
- **Regresión potencial:** Cualquier fix debe considerar que otros consumidores del endpoint podrían depender de la estructura actual del `PaginatedResponse`.

---

## Propuesta de Solución

### Opción A — Fix en Frontend (adaptarse al contrato actual del backend)

1. **Modificar `transactionService.ts`** para extraer el array desde `response.data.content` en lugar de `response.data`.
2. **Actualizar los tipos TypeScript** para reflejar la estructura real de `PaginatedResponse` (con campos `content`, `page`, `size`, `totalElements`, `totalPages`, `last`).
3. **Agregar validación defensiva** antes de llamar `.map()`:
   - Verificar que el dato sea un array antes de aplicar `.map()`.
   - Si es vacío o `null`, retornar un array vacío `[]`.

### Opción B — Fix compartido (Frontend + Backend)

1. **Backend:** Agregar un endpoint alternativo que retorne un `List<TransactionResponse>` plano (sin paginación) o bien filtrado por `userId`.
2. **Frontend:** Adaptar el servicio al nuevo endpoint.
3. **Ambos:** Documentar el contrato API formalmente (OpenAPI/Swagger).

### Opción C — Fix integral (Recomendada)

1. **Backend:**
   - Agregar soporte para el query parameter `userId` en el endpoint `GET /api/v1/transactions` para filtrar transacciones por usuario.
   - Mantener la respuesta paginada (`PaginatedResponse`).
2. **Frontend:**
   - Actualizar `transactionService.ts` para leer `response.data.content` (el array dentro del objeto paginado).
   - Crear un tipo `PaginatedTransactionResponse` que refleje la estructura real del backend.
   - Implementar validación defensiva sobre `response.data.content` antes de `.map()`.
   - Implementar soporte de paginación en la UI (aprovechar `totalPages`, `totalElements`, etc.).
   - Agregar un componente de **empty state** cuando `content` sea un array vacío.

### Consideraciones de validación de datos

- Toda respuesta de API debe ser validada en tiempo de ejecución (no confiar solo en tipos TypeScript).
- Aplicar el patrón de *defensive coding*: `Array.isArray(data) ? data.map(...) : []`.
- Considerar el uso de una librería de validación de esquemas (ej. `zod`) para validar las respuestas de API.

---

## Criterios de Aceptación del Bug

Para considerar este bug como resuelto, **todos** los siguientes criterios deben cumplirse:

### Escenario 1 — Usuario sin transacciones

- [ ] Al acceder a la página de transacciones, la página se carga sin errores.
- [ ] Se muestra un **empty state** con un mensaje claro (ej. *"No tienes transacciones registradas"*).
- [ ] El botón/acción para crear una nueva transacción está visible y funcional.
- [ ] No se muestra ningún error en consola de JavaScript.

### Escenario 2 — Usuario con transacciones

- [ ] Las transacciones existentes se muestran correctamente en la tabla.
- [ ] Los datos se mapean correctamente desde la respuesta de la API al modelo del frontend.
- [ ] La funcionalidad de creación de transacciones sigue operativa.

### Escenario 3 — Manejo de errores

- [ ] Si el backend no está disponible (error de red), se muestra un mensaje de error apropiado, **no** un crash de JavaScript.
- [ ] Si la respuesta de la API tiene formato inesperado, la aplicación maneja el caso gracefully sin lanzar excepciones no controladas.

### Escenario 4 — Contrato de API

- [ ] El tipo TypeScript en el frontend refleja la estructura real de la respuesta del backend.
- [ ] `response.data.content` (o equivalente) se utiliza correctamente para extraer el array de transacciones.
- [ ] No existen anotaciones de tipo genérico engañosas (`get<T>`) que no correspondan con la respuesta real.

---

## Ciclo de Vida del Bug

| Fase          | Estado           | Fecha       | Descripción                                                                 |
|---------------|------------------|-------------|-----------------------------------------------------------------------------|
| **Detección**     | ✅ Completada    | 2026-02-19  | Bug detectado por el equipo QA durante pruebas con usuario sin datos.       |
| **Reproducción**  | ✅ Completada    | 2026-02-20  | Reproducido de forma consistente: 100% reproducible con usuario sin transacciones. |
| **Análisis**      | ✅ Completado    | 2026-02-19  | Root cause identificado: desalineación del contrato API entre frontend y backend. Detalle en sección *Análisis Técnico*. |
| **Corrección**    | ✅ Completado     | —           | Asignación pendiente al equipo de desarrollo. Se recomienda la **Opción C** (fix integral). |
| **Validación**    | ✅ Completado     | —           | QA validará los 4 escenarios de los Criterios de Aceptación post-fix.       |
| **Cierre**        | ✅ Completado     | —           | Se cerrará tras validación exitosa por QA y revisión de código (code review). |

---

## Trazabilidad

| Relación                   | Referencia                                                                |
|----------------------------|---------------------------------------------------------------------------|
| Archivo con error principal | [`transactionService.ts:21`](file:///Users/jeanvillacis/Documents/Project-G2/Budget_Management_App/app/Frontend/src/modules/transactions/services/transactionService.ts#L21) |
| Endpoint backend afectado  | `GET /api/v1/transactions` — [`TransactionController.java:38-43`](file:///Users/jeanvillacis/Documents/Project-G2/Budget_Management_App/app/backend-microservice/transaction/src/main/java/com/microservice/transaction/controller/TransactionController.java#L38-L43) |
| DTO de respuesta backend   | [`PaginatedResponse.java`](file:///Users/jeanvillacis/Documents/Project-G2/Budget_Management_App/app/backend-microservice/transaction/src/main/java/com/microservice/transaction/dto/PaginatedResponse.java) |
| Componente de error UI     | [`TransactionPage.tsx:90-106`](file:///Users/jeanvillacis/Documents/Project-G2/Budget_Management_App/app/Frontend/src/modules/transactions/pages/TransactionPage.tsx#L90-L106) |
| Deuda técnica relacionada  | DT-DOC-05 (Falta de operaciones CRUD completas en backend)               |

---

*Documento generado el 2026-02-20. Apto para importación directa en Jira, GitHub Issues o herramientas de bug tracking.*
