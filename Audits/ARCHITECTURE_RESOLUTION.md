# Reporte de Resolución de Arquitectura y Calidad – Sistema de Gestión de Presupuesto

**Fecha:** 10 de Febrero de 2026
**Estado:** Aprobado para Implementación
**Alcance:** Backend (Microservicios), Frontend (React), Infraestructura

---

## Fase 1: Detección y Clasificación

Esta sección formaliza los hallazgos de la auditoría inicial, identificando antipatrones arquitectónicos específicos y "code smells" designados para resolución.

### Hallazgos en Backend

| ID       | Hallazgo                               | Clasificación                              | Severidad  | Componente Afectado                         |
| -------- | -------------------------------------- | ------------------------------------------ | ---------- | ------------------------------------------- |
| **B-01** | **Entidades JPA en API REST**          | **Onion Wrapping / Abstracción con Fugas** | 🔴 Crítico | `TransactionController`, `ReportController` |
| **B-02** | **Controlador Orquestando Mensajería** | **Smart Controller / Objeto Dios (Mini)**  | 🔴 Crítico | `TransactionController`                     |
| **B-03** | **Configuración RabbitMQ Hardcoded**   | **Configuración Hardcoded**                | 🟡 Alto    | Infraestructura                             |
| **B-04** | **Configuración CORS Hardcoded**       | **Configuración Hardcoded**                | 🟡 Medio   | Aplicaciones `main`                         |
| **B-05** | **Endpoints de Lista sin Paginación**  | **Conjunto de Resultados Ilimitado**       | 🟡 Medio   | `TransactionController`                     |

### Hallazgos en Frontend

| ID       | Hallazgo                                           | Clasificación                          | Severidad | Componente Afectado           |
| -------- | -------------------------------------------------- | -------------------------------------- | --------- | ----------------------------- |
| **F-01** | **Bloqueo de Proveedor (Vendor Lock-in) Firebase** | **Vendor Lock-in**                     | 🟠 Alto   | `useUserStore`, `authService` |
| **F-02** | **Efectos Secundarios en Carga de Módulo**         | **Efecto Secundario de Estado Global** | 🟠 Alto   | `useUserStore`                |
| **F-03** | **Reinventar Utilidades de Fecha**                 | **Not Invented Here (NIH)**            | 🟡 Medio  | `date-utils.ts`               |
| **F-04** | **Componentes/Estilos en Línea**                   | **Código Espagueti**                   | 🟡 Medio  | `AppRouter`, `DataTable`      |

---

## Fase 2: Propuesta de Solución y Justificación

### [B-01] Entidades JPA en API REST

**Resumen del Problema:**
Exponer clases `@Entity` de JPA directamente en las respuestas REST acopla fuertemente el contrato de la API al esquema de la base de datos.

**🔍 Evidencia en Código:**
En `TransactionController.java`:

```java
// El controlador retorna directamente la entidad de base de datos 'Transaction'
@PostMapping
public ResponseEntity<Transaction> create(@Valid @RequestBody Transaction transaction) {
    // ...
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
}
```

**⚠️ Violaciones SOLID Resueltas:**

- **SRP (Principio de Responsabilidad Única):** La Entidad `@Entity` tiene dos responsabilidades: mapear la tabla de BD y definir el contrato JSON de la API. Al usar DTO, separamos estas preocupaciones.
- **OCP (Principio Abierto/Cerrado):** Si necesitas cambiar el formato JSON (ej. renombrar un campo para el frontend), tienes que modificar la Entidad, rompiendo potencialmente la persistencia. Con DTOs, puedes extender la API sin tocar el núcleo de datos.

**Solución Recomendada:**
Implementar el patrón **DTO (Data Transfer Object)** con una capa de mapeo.

- ✅ **Preferido:** Crear DTOs basados en `record` (`TransactionResponse`, `CreateTransactionRequest`) y usar **MapStruct** para un mapeo eficiente y seguro.

**💡 ¿Por qué es bueno implementar este patrón? (DTO & Adapter)**

1.  **Desacoplamiento:** Te permite cambiar la estructura de tu base de datos sin romper la aplicación móvil o web.
2.  **Seguridad:** Evitas exponer datos sensibles (como contraseñas o auditoría interna) que podrían estar en la entidad pero no deberían ir al cliente.
3.  **Rendimiento:** Previenes el famoso error `LazyInitializationException` y ciclos infinitos de serialización JSON.

---

### [B-02] Controladores Orquestando Infraestructura (Smart Controller)

**Resumen del Problema:**
El `TransactionController` viola el Principio de Responsabilidad Única (SRP) al gestionar directamente la mensajería RabbitMQ.

**🔍 Evidencia en Código:**
En `TransactionController.java`:

```java
// El controlador inyecta lógica de infraestructura directamente
private final TransactionMessageProducer transactionMessageProducer;

@PostMapping
public ResponseEntity<Transaction> create(...) {
    Transaction created = transactionService.create(transaction);
    // El controlador decide cuándo y cómo enviar mensajes a RabbitMQ
    transactionMessageProducer.sendCreated(created);
    // ...
}
```

**⚠️ Violaciones SOLID Resueltas:**

- **SRP (Principio de Responsabilidad Única):** El controlador está haciendo demasiadas cosas: validar HTTP, llamar al servicio de negocio, Y coordinar la mensajería de infraestructura.
- **DIP (Principio de Inversión de Dependencias):** El módulo de alto nivel (Controlador) depende directamente de un detalle de implementación de bajo nivel (`TransactionMessageProducer` de RabbitMQ). Debería depender de una abstracción (Evento).

**Solución Recomendada:**
Refactorizar usando los patrones **Capa de Servicio** y **Eventos de Dominio**.

- ✅ **Preferido:** Mover toda la lógica de negocio a `TransactionService`. Publicar **Eventos de Dominio** limpios (ej. `TransactionCreatedEvent`) que sean manejados por un listener de infraestructura.

**💡 ¿Por qué es bueno implementar este patrón? (Observer / Events)**

1.  **Testabilidad:** Puedes probar el Controlador unitariamente sin tener que burlar (mock) RabbitMQ o colas de mensajería complejas.
2.  **Mantenibilidad:** Cumples SRP. Cada clase tiene una única razón para cambiar.
3.  **Extensibilidad:** Si mañana quieres enviar un email además de un mensaje RabbitMQ, solo agregas otro listener, sin tocar el controlador.

---

### [F-01] Bloqueo de Proveedor (Vendor Lock-in) Firebase

**Resumen del Problema:**
La lógica de dominio (User Store) importa directamente el SDK de `firebase/auth`.

**🔍 Evidencia en Código:**
En `useUserStore.ts`:

```typescript
// Importación directa del SDK de un tercero en la capa de estado/negocio
import { type User as FirebaseUser, onAuthStateChanged } from "firebase/auth";
import { auth } from "../../../core/config/firebase.config.js";

interface UserState {
  user: FirebaseUser | null; // El tipo de dato depende de Firebase
  // ...
}
```

**⚠️ Violaciones SOLID Resueltas:**

- **DIP (Principio de Inversión de Dependencias):** La lógica de negocio de alto nivel (Store de Usuario) depende de un detalle de bajo nivel (SDK de Firebase). Deberían depender de abstracciones.
- **ISP (Principio de Segregación de Interfaces):** `FirebaseUser` trae docenas de propiedades que probablemente tu app no usa. Estás forzado a depender de una interfaz "gorda". Una interfaz propia `User` sería más limpia.

**Solución Recomendada:**
Implementar el patrón **Repositorio/Adapter**.

- ✅ **Preferido:** Definir una interfaz `AuthRepository` (TypeScript puro). Crear un `FirebaseAuthAdapter` que implemente esta interfaz.

**💡 ¿Por qué es bueno implementar este patrón? (Adapter & Dependency Inversion)**

1.  **Independencia:** Si Firebase sube precios o cierra, puedes cambiar a Auth0 o AWS Cognito solo cambiando el Adaptador, sin reescribir toda tu aplicación.
2.  **Testing:** Puedes inyectar un "MockAuthAdapter" para pruebas automáticas sin necesitar conexión real a internet ni credenciales de Firebase.
3.  **Dominio Puro:** Tu lógica de negocio habla de "Usuario" y "Email", no de "FirebaseUser" o "GoogleAuthProvider".

---

### [F-03] Reinventar Utilidades de Fecha

**Resumen del Problema:**
Se están escribiendo funciones manuales para formatear fechas en lugar de usar librerías estándar probadas.

**🔍 Evidencia en Código:**
En `date-utils.ts`:

```typescript
// Mantenimiento manual de arrays de nombres meses
const longMonths = [
    "January", "February", "March", ...
]
// Lógica propensa a errores para reemplazar strings
result = result.replace(/LLLL/g, longMonths[date.getMonth()])
```

**⚠️ Violaciones SOLID Resueltas:**

- **SRP (Principio de Responsabilidad Única):** Tu equipo no debería tener la responsabilidad de mantener lógica compleja de calendarios y zonas horarias. Esa responsabilidad pertenece a una librería especializada.

**Solución Recomendada:**
Usar librerías estándar como **date-fns**.

**💡 ¿Por qué es bueno usar librerías estándar?**

1.  **Fiabilidad:** `date-fns` ha sido probada por millones de desarrolladores; tu función manual probablemente tenga bugs en años bisiestos o zonas horarias.
2.  **Internacionalización (i18n):** Las librerías ya soportan español, inglés, chino, etc., "gratis". Tu código manual solo soporta lo que escribiste.

---

## Fase 3: Guía de Refactorización (Incremental)

### Paso 1: Asegurar el Contrato de API (Backend)

**Acción:** Crear DTOs para `Transaction` y `Report`.

1.  Definir `TransactionResponseDTO`.
2.  Agregar mapper con MapStruct.
3.  Actualizar `TransactionController` para retornar `ResponseEntity<TransactionResponseDTO>` en lugar de `Entity`.

### Paso 2: Extraer Lógica de Mensajería (Backend)

**Acción:** Desacoplar Controlador de RabbitMQ.

1.  Crear `TransactionService.createTransaction()`.
2.  Mover la llamada RabbitMQ del Controlador a un evento `@PostPersist` o llamada explícita en el Servicio.

### Paso 3: Abstraer Autenticación (Frontend)

**Acción:** Definición de interfaz.

1.  Crear interfaz `src/core/auth/AuthRepository.ts`.
2.  Renombrar el actual `authService` a `FirebaseAuthAdapter`.
3.  Inyectar el adaptador en `useUserStore`.

---

## Fase 4: Impacto en Calidad y Testing

### Mejoras en Testabilidad

- **Tests Unitarios:** Al remover `TransactionMessageProducer` del Controlador, podemos probar el Controlador usando Mocks simples del Servicio.
- **Tests de Integración:** Los DTOs permiten probar contratos de API independientemente del esquema de BD.

### Riesgos de Regresión

- **Serialización:** Asegurar que los DTOs se serialicen exactamente igual que las Entidades es crítico para no romper clientes móviles/web existentes.

---
