# 📋 Informe de Traspaso de Ingeniería (Handover Report)

> **Proyecto:** Budget Management App
> **Fecha del análisis:** 2026-02-19
> **Fase:** Phase 1 — Engineering Handover
> **Analista:** Senior Software Architect & Quality Analyst
> **Base del análisis:** Inspección exhaustiva del código fuente existente (sin modificaciones)

---

## 1. Resumen Ejecutivo

**Budget Management App** es una aplicación full-stack de gestión financiera personal que permite a los usuarios registrar transacciones (ingresos y gastos), categorizarlas, y generar reportes financieros agregados por período mensual.

### Propósito de Negocio

El sistema resuelve la necesidad de **seguimiento financiero personal** a través de tres capacidades clave:

1. **Registro de transacciones**: CRUD de ingresos y gastos con categorización, fechas y descripciones.
2. **Reportes automáticos**: Generación asíncrona de resúmenes financieros mensuales (ingresos totales, gastos totales, balance) vía procesamiento de eventos.
3. **Autenticación segura**: Login/registro vía email/password y Google OAuth a través de Firebase Authentication.

### Origen del Proyecto

El proyecto fue construido en un **micro-sprint intensivo de 2.5 días** por un equipo de 3 desarrolladores (Jacob, Agus, Majo) trabajando en conjunto con asistentes de IA (GitHub Copilot, Open Code). Esto explica tanto las decisiones de velocidad sobre calidad como las deudas técnicas heredadas.

---

## 2. Stack Tecnológico — Desglose Detallado

### 2.1 Backend — Microservicios (Java / Spring Boot)

| Componente        | Tecnología                          | Versión         | Ubicación                                      |
| :---------------- | :---------------------------------- | :-------------- | :--------------------------------------------- |
| Lenguaje          | Java (Eclipse Temurin)              | **17**          | `pom.xml` → `<java.version>17</java.version>` |
| Framework         | Spring Boot                         | **4.0.2**       | `pom.xml` → `spring-boot-starter-parent`       |
| API Web           | Spring WebMVC                       | (incluida)      | `spring-boot-starter-webmvc`                   |
| Persistencia      | Spring Data JPA + Hibernate         | (incluida)      | `spring-boot-starter-data-jpa`                 |
| Base de Datos     | MySQL                               | **8.0**         | `docker-compose.yaml`, `mysql-connector-j`     |
| BD para Tests     | H2 (in-memory)                      | (incluida)      | `pom.xml` → `scope: test`                     |
| Mensajería        | RabbitMQ (AMQP)                     | **4.0**         | `spring-boot-starter-amqp`, `docker-compose`   |
| Validación        | Bean Validation (jakarta)           | (incluida)      | `spring-boot-starter-validation`               |
| Utilidades        | Lombok                              | (incluida)      | `@Getter`, `@Builder`, `@RequiredArgsConstructor` |
| Serialización MQ  | Jackson JSON                        | (incluida)      | `JacksonJsonMessageConverter`                  |
| Testing           | JUnit 5 + Mockito                   | (incluida)      | `spring-boot-starter-*-test`                   |
| Build             | Maven (wrapper incluido)            | —               | `mvnw` / `mvnw.cmd`                           |
| Contenedores      | Docker                              | —               | `Dockerfile` por servicio                      |

### 2.2 Frontend — Modular Monolith (React / TypeScript)

| Componente        | Tecnología                          | Versión         |
| :---------------- | :---------------------------------- | :-------------- |
| Framework UI      | React                               | **19.2.0**      |
| Lenguaje          | TypeScript                          | **~5.9.3**      |
| Bundler           | Vite                                | **7.2.4**       |
| Estilos           | Tailwind CSS                        | **4.1.18**      |
| Componentes UI    | Shadcn/UI (Radix UI)                | **1.4.3**       |
| Estado Global     | Zustand                             | **5.0.11**      |
| Estado Servidor   | TanStack Query (React Query)        | **5.90.20**     |
| Tablas            | TanStack Table                      | **8.20.5**      |
| Routing           | React Router DOM                    | **7.13.0**      |
| Formularios       | React Hook Form + Zod               | **7.71.1** / **4.3.6** |
| Animaciones       | Framer Motion                       | **12.33.0**     |
| Autenticación     | Firebase                            | **12.9.0**      |
| HTTP Client       | Axios                               | **1.13.4**      |
| Icons             | Lucide React + React Icons          | **0.563.0** / **5.5.0** |
| Testing           | Jest + React Testing Library        | **30.2.0** / **16.0.0** |
| Test Runner       | ts-jest                             | **29.4.6**      |

### 2.3 Infraestructura

| Componente        | Tecnología                          | Detalle                                    |
| :---------------- | :---------------------------------- | :----------------------------------------- |
| Orquestación      | Docker Compose                      | `docker-compose.yaml` en `production/`     |
| CI/CD             | GitHub Actions                      | Pipeline con linting, tests, build         |
| Quality Gate      | SonarCloud                          | Análisis post-tests, bloquea build si falla|
| Containers        | 5 servicios                         | 2× MySQL, 1× RabbitMQ, 1× Transaction, 1× Report |

---

## 3. Visión General de la Arquitectura

### 3.1 Arquitectura de Alto Nivel

El sistema implementa una **Arquitectura de Microservicios Event-Driven** con dos servicios backend independientes, comunicados asíncronamente a través de RabbitMQ:

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              FRONTEND                                      │
│                     React 19 + TypeScript 5.9                              │
│                     (Modular Monolith en Vite)                             │
│                                                                             │
│   ┌──────────────┐  ┌──────────────┐  ┌──────────┐  ┌──────────────────┐  │
│   │  Auth Module  │  │ Transactions │  │  Reports │  │   Home Module    │  │
│   │  (Firebase)   │  │   Module     │  │  Module  │  │                  │  │
│   └──────┬───────┘  └──────┬───────┘  └─────┬────┘  └──────────────────┘  │
│          │                 │                │                               │
│          │     Axios HttpClient (Service-specific instances)               │
└──────────┼─────────────────┼────────────────┼──────────────────────────────┘
           │                 │                │
     Firebase Auth     Port 8081         Port 8082
           │                 │                │
           ▼                 ▼                ▼
┌──────────────┐  ┌──────────────────┐  ┌──────────────────┐
│   Firebase   │  │   TRANSACTION    │  │     REPORT       │
│   Auth       │  │    SERVICE       │  │    SERVICE       │
│  (External)  │  │  (Spring Boot)   │  │  (Spring Boot)   │
└──────────────┘  │                  │  │                  │
                  │  Controller      │  │  Controller      │
                  │       ↓          │  │       ↓          │
                  │  Service         │  │  Service         │
                  │       ↓          │  │       ↑          │
                  │  Repository ──┐  │  │  Repository      │
                  │               │  │  │       ↑          │
                  │  EventPublish │  │  │  Consumer  ◄─────┤
                  │       ↓       │  │  │                  │
                  └───────┼───────┘  │  └──────────────────┘
                          │          │          ▲
                          ▼          │          │
                  ┌──────────────┐   │  ┌──────────────┐
                  │ mysql-       │   │  │ mysql-       │
                  │ transactions │   │  │ reports      │
                  │ (Port 3307)  │   │  │ (Port 3308)  │
                  └──────────────┘   │  └──────────────┘
                                     │
                          ┌──────────▼──────────┐
                          │      RabbitMQ       │
                          │   (Port 5672/15672) │
                          │  Exchange: topic    │
                          │  "transaction-      │
                          │   exchange"         │
                          └─────────────────────┘
```

### 3.2 Flujo Event-Driven Detallado (RabbitMQ)

Esta es la secuencia **exacta** del flujo de eventos del código fuente actual:

```
PASO 1: El usuario crea una transacción vía REST API
────────────────────────────────────────────────────
POST /api/v1/transactions
    │
    ▼
TransactionController.create(@Valid TransactionRequest dto)
    │
    ▼
TransactionServiceImpl.create(dto)
    │
    ├── 1. TransactionMapper.toRequest(dto) → Transaction entity
    ├── 2. transactionRepository.save(entity) → saved entity
    ├── 3. eventPublisher.publishEvent(new TransactionCreatedEvent(this, saved))
    │      ↑ Evento INTERNO de Spring (ApplicationEvent)
    └── 4. return TransactionMapper.toResponse(saved)

PASO 2: El evento Spring interno se convierte en mensaje RabbitMQ
─────────────────────────────────────────────────────────────────
TransactionEventListener.handleTransactionCreatedEvent(event)
    │   ↑ @Async @EventListener — Se ejecuta en hilo separado
    │
    ▼
TransactionMessageProducer.sendCreated(event.getTransaction())
    │
    ├── 1. Convierte Transaction → TransactionMessage (DTO serializable)
    └── 2. rabbitTemplate.convertAndSend(
              exchange: "transaction-exchange",
              routingKey: "transaction.created",
              message: TransactionMessage
           )
              │ Jackson serializa a JSON

PASO 3: RabbitMQ enruta el mensaje
───────────────────────────────────
TopicExchange("transaction-exchange")
    │
    ├── Binding: routingKey "transaction.created" → Queue "transaction-created"
    └── Binding: routingKey "transaction.updated" → Queue "transaction-updated"

PASO 4: El Report Service consume y agrega
──────────────────────────────────────────
ReportConsumer.consumeCreated(TransactionMessage message)
    │   ↑ @RabbitListener(queues = "transaction-created")
    │
    ▼
ReportServiceImpl.updateReport(transactionMessage)
    │
    ├── 1. Extrae period = fecha → "yyyy-MM" (ej: "2026-02")
    ├── 2. Busca Report existente: findByUserIdAndPeriod(userId, period)
    │      Si no existe → Crea uno nuevo con totales en $0.00
    │
    ├── 3. Según TransactionType:
    │      ├── INCOME  → report.totalIncome  += amount
    │      └── EXPENSE → report.totalExpense += amount
    │
    ├── 4. Recalcula: report.balance = totalIncome - totalExpense
    └── 5. reportRepository.save(report)
```

### 3.3 Endpoints REST del Sistema

#### Transaction Service (Port 8081)

| Método | Endpoint                    | Descripción                          | Request/Response                          |
| :----- | :-------------------------- | :----------------------------------- | :---------------------------------------- |
| `POST` | `/api/v1/transactions`      | Crear transacción                    | `TransactionRequest` → `TransactionResponse` |
| `GET`  | `/api/v1/transactions/{id}` | Obtener transacción por ID           | — → `TransactionResponse`                |
| `GET`  | `/api/v1/transactions`      | Listar transacciones (paginado)      | `?page=0&size=10` → `PaginatedResponse<TransactionResponse>` |

#### Report Service (Port 8082)

| Método | Endpoint                           | Descripción                          | Request/Response                          |
| :----- | :--------------------------------- | :----------------------------------- | :---------------------------------------- |
| `GET`  | `/api/v1/reports/{userId}`         | Reporte por usuario y período        | `?period=2026-02` → `ReportResponse`     |
| `GET`  | `/api/v1/reports/{userId}/all`     | Todos los reportes (paginado)        | `?page=0&size=10` → `PaginatedResponse<ReportResponse>` |
| `GET`  | `/api/v1/reports/{userId}/summary` | Resumen por rango de períodos        | `?startPeriod=2026-01&endPeriod=2026-06` → `ReportSummary` |

### 3.4 Arquitectura Interna Actual: Layered (NO Hexagonal)

Ambos microservicios siguen una **arquitectura en capas tradicional** (Layered Architecture):

```
┌─────────────────────────────────────────────┐
│  TRANSACTION SERVICE — Estructura Actual     │
│                                              │
│  com.microservice.transaction/               │
│  ├── controller/                             │  ← Capa de presentación
│  │   └── TransactionController.java          │
│  ├── service/                                │  ← Capa de lógica de negocio
│  │   ├── TransactionService.java (interface) │
│  │   └── impl/TransactionServiceImpl.java    │
│  ├── repository/                             │  ← Capa de acceso a datos
│  │   └── TransactionRepository.java          │
│  ├── model/                                  │  ← Entidad JPA (acoplada al framework)
│  │   ├── Transaction.java (@Entity)          │
│  │   └── TransactionType.java                │
│  ├── dto/                                    │  ← DTOs API (Records)
│  │   ├── TransactionRequest.java             │
│  │   ├── TransactionResponse.java            │
│  │   ├── TransactionMapper.java              │
│  │   └── PaginatedResponse.java              │
│  ├── event/                                  │  ← Eventos Spring (acoplados)
│  │   └── TransactionCreatedEvent.java        │
│  ├── exception/                              │  ← Manejo de errores
│  │   ├── EntityNotFoundException.java        │
│  │   ├── CustomErrorResponse.java            │
│  │   └── GlobalExceptionHandler.java         │
│  ├── infrastructure/                         │  ← Mensajería (parcialmente separada)
│  │   ├── RabbitMQConfiguration.java          │
│  │   ├── TransactionMessageProducer.java     │
│  │   ├── dto/TransactionMessage.java         │
│  │   └── listener/TransactionEventListener   │
│  └── config/                                 │
│      └── CorsConfig.java                     │
└─────────────────────────────────────────────┘
```

**Hallazgo clave:** La capa de `infrastructure/` ya existe como directorio y contiene el messaging (RabbitMQ), lo que muestra una **intención incompleta** de separar las preocupaciones, pero la entidad `Transaction.java` en `model/` sigue siendo una `@Entity` JPA — acoplada directamente al framework ORM.

---

## 4. Entidades de Negocio Clave

### 4.1 Transaction (Servicio de Transacciones)

| Campo           | Tipo Java           | DB Column         | Restricciones                              |
| :-------------- | :------------------ | :---------------- | :----------------------------------------- |
| `transactionId` | `Long`              | `transaction_id`  | PK, auto-generated (IDENTITY)              |
| `userId`        | `String`            | `user_id`         | NOT NULL — ID del usuario de Firebase      |
| `type`          | `TransactionType`   | `type`            | NOT NULL — Enum: `INCOME`, `EXPENSE`       |
| `amount`        | `BigDecimal(19,2)`  | `amount`          | NOT NULL, `@Positive`                      |
| `category`      | `String`            | `category`        | Texto libre (max 100 chars)                |
| `date`          | `LocalDate`         | `date`            | NOT NULL — Fecha de la transacción         |
| `description`   | `String`            | `description`     | Opcional (max 500 chars)                   |
| `createdAt`     | `OffsetDateTime`    | `created_at`      | NOT NULL, auto-set via `@PrePersist`       |

**Rol funcional:** Representa una operación financiera individual (ingreso o gasto) asociada a un usuario.

### 4.2 Report (Servicio de Reportes)

| Campo           | Tipo Java           | DB Column         | Restricciones                              |
| :-------------- | :------------------ | :---------------- | :----------------------------------------- |
| `reportId`      | `Long`              | `report_id`       | PK, auto-generated (IDENTITY)              |
| `userId`        | `String`            | `user_id`         | NOT NULL — ID del usuario de Firebase      |
| `period`        | `String`            | `period`          | NOT NULL, length=7, formato `"yyyy-MM"`    |
| `totalIncome`   | `BigDecimal(19,2)`  | `total_income`    | NOT NULL — Suma de ingresos del período    |
| `totalExpense`  | `BigDecimal(19,2)`  | `total_expense`   | NOT NULL — Suma de gastos del período      |
| `balance`       | `BigDecimal(19,2)`  | `balance`         | NOT NULL — `totalIncome - totalExpense`    |
| `createdAt`     | `OffsetDateTime`    | `created_at`      | NOT NULL, auto-set via `@PrePersist`       |
| `updatedAt`     | `OffsetDateTime`    | `updated_at`      | NOT NULL, auto-set via `@PrePersist/@PreUpdate` |

**Rol funcional:** Agrega los totales financieros de un usuario por período mensual. Se actualiza asíncronamente cada vez que llega un evento `TransactionCreated` desde RabbitMQ.

### 4.3 TransactionMessage (DTO de Mensajería Inter-servicio)

| Campo           | Tipo                | Rol                                                        |
| :-------------- | :------------------ | :--------------------------------------------------------- |
| `transactionId` | `Long`              | Referencia a la transacción original                       |
| `userId`        | `String`            | Usuario propietario                                        |
| `type`          | `TransactionType`   | `INCOME` o `EXPENSE` — determina la acumulación            |
| `amount`        | `BigDecimal`        | Monto a sumar al total correspondiente                     |
| `date`          | `LocalDate`         | Se usa para derivar el `period` del reporte (`yyyy-MM`)    |
| `category`      | `String`            | Categoría de la transacción                                |
| `description`   | `String`            | Descripción textual                                        |

> **Nota:** Este DTO está duplicado entre los dos microservicios — en `transaction` como clase con Lombok (`@Data @Builder`), y en `report` como Java Record con validaciones Bean. Esta inconsistencia es una deuda técnica menor.

### 4.4 IAuthUser (Frontend — Modelo de Usuario)

| Campo         | Tipo              | Fuente                |
| :------------ | :---------------- | :-------------------- |
| `id`          | `string`          | Firebase `uid`        |
| `email`       | `string \| null`  | Firebase `email`      |
| `displayName` | `string \| null`  | Firebase `displayName`|
| `photoURL`    | `string \| null`  | Firebase `photoURL`   |

**Rol funcional:** Representa al usuario autenticado. Su `id` se envía como `userId` en todas las operaciones de transacciones y reportes.

---

## 5. Evaluación Inicial y Deuda Técnica

### 5.1 ✅ Fortalezas Heredadas

El equipo anterior dejó fundaciones sólidas que **debemos preservar y extender**:

| # | Fortaleza | Evidencia en código | Impacto |
|---|---|---|---|
| **F-01** | **DTOs correctos (Records)** | `TransactionRequest`, `TransactionResponse`, `ReportResponse` son Java Records inmutables. Los controllers **nunca** exponen entidades JPA al API. | Previene fuga de datos y mass-assignment. |
| **F-02** | **Controllers delegados (no smart)** | `TransactionController` delega 100% al `TransactionService`. Cero lógica de negocio en controllers. | Separación de responsabilidades en capa web. |
| **F-03** | **Desacoplamiento del messaging** | El evento Spring interno (`TransactionCreatedEvent`) es interceptado async por `TransactionEventListener`, que delega al `TransactionMessageProducer`. El ServiceImpl no conoce RabbitMQ directamente. | El servicio no depende del broker. |
| **F-04** | **Configuración externalizada** | `RabbitMQConfiguration` usa `@Value` para inyectar nombres de exchanges y colas. CORS usa `@Value` para orígenes permitidos. Docker Compose usa variables de entorno. | Sin hardcoding de infraestructura. |
| **F-05** | **Validación en Request DTOs** | `TransactionRequest` usa `@NotBlank`, `@NotNull`, `@Positive`, `@Size`. Report usa validador custom `@ValidPeriod` con regex. | Validación declarativa en la frontera de entrada. |
| **F-06** | **Frontend Auth desacoplado** | `IAuthRepository` (interface) → `FirebaseAuthRepository` (implementación). `useUserStore` usa `authRepository` inyectado vía `dependencies.ts`. | Firebase es reemplazable sin tocar la lógica de negocio. |
| **F-07** | **Frontend modular** | 4 módulos independientes (`auth`, `transactions`, `reports`, `home`), cada uno con `components/`, `hooks/`, `services/`, `store/`, `types/`, `adapters/`. | Escalabilidad y mantenibilidad del frontend. |
| **F-08** | **Adapter Pattern en Frontend** | `transactionAdapter` transforma `TransactionItemResponse` (API) → `TransactionModel` (dominio UI). `reportAdapter` hace lo mismo para reportes. | Desacopla el formato de la API del modelo interno del frontend. |
| **F-09** | **Paginated Response genérico** | `PaginatedResponse<T>` como Record genérico reutilizado en ambos microservicios. | Consistencia en la paginación. |
| **F-10** | **Custom Validation** | `@ValidPeriod` + `PeriodValidator` implementa un validador custom reusable para formato `YYYY-MM`. | Validación semántica del dominio. |

### 5.2 🔴 Deuda Técnica Crítica

#### DT-ARCH-01: Ausencia de Arquitectura Hexagonal (Severidad: ALTA)

**Problema:** Ambos microservicios usan arquitectura en capas tradicional donde la entidad de dominio (`Transaction.java`) **es** la entidad JPA:

```java
// ESTADO ACTUAL — model/Transaction.java
@Entity                           // ← Dependencia JPA en el "dominio"
@Table(name = "transactions")     // ← Acoplamiento a esquema de BD
public class Transaction {
    @Id @GeneratedValue            // ← Anotaciones de infraestructura
    private Long transactionId;
    @Column(name = "user_id")      // ← Mapping directo BD
    private String userId;
    // ... sin lógica de negocio, solo getters/setters (Anemic Model)
}
```

**Violaciones específicas:**
- `domain/model/` **no existe** — La entidad "de dominio" vive en `model/` con anotaciones `@Entity`, `@Table`, `@Column` de JPA.
- `domain/port/` **no existe** — No hay puertos de entrada ni salida. `TransactionService` (interfaz) importa DTOs Spring (`Pageable`), acoplando el contrato al framework.
- `TransactionCreatedEvent` extiende `ApplicationEvent` de Spring — el evento del dominio depende del framework.
- `TransactionServiceImpl` usa `@Service` directamente — No existe `BeanConfiguration` para inyección desacoplada.
- El modelo es **anémico**: `Transaction.java` solo tiene campos + getters/setters vía Lombok, sin métodos de llogica de negocio (`validate()`, etc.).

**Plan de migración recomendado:** Extraer un `domain/` puro con modelo rico, ports (interfaces), y eventos POJO → Mover las implementaciones actuales a `infrastructure/adapter/`.

---

#### DT-QA-01: Cobertura de Tests Críticamente Baja (Severidad: **P0 — CRÍTICA**)

**Estado actual del testing backend:**

| Servicio      | Clase Testeada            | # Tests | Cobertura del Servicio        |
| :------------ | :------------------------ | :------ | :---------------------------- |
| `transaction` | `TransactionServiceImplTest` | **1** | Solo happy path de `create()`. No hay tests para `getById()`, `getAll()`, ni edge cases. |
| `report`      | `ReportServiceImplTest`   | **1**   | Solo happy path de `updateReport()`. No hay tests para `getReport()`, `getReportsByUserId()`, `getReportsByPeriodRange()`. |

**Lo que NO está testeado en backend:**

- ❌ Controllers (0 tests `@WebMvcTest`)
- ❌ Repositorios (0 tests `@DataJpaTest`)
- ❌ `TransactionEventListener` (manejo async)
- ❌ `TransactionMessageProducer` (serialización/envío RabbitMQ)
- ❌ `ReportConsumer` (deserialización/consumo)
- ❌ Edge cases: transacción no encontrada, validación fallida, montos negativos
- ❌ `TransactionMapper` y `ReportMapper` (mappings)
- ❌ `PeriodValidator` (validación custom)
- ❌ `GlobalExceptionHandler` (manejo de errores HTTP)

**Estado actual del testing frontend:**

| Módulo         | Archivos de Test          | Observación                        |
| :------------- | :------------------------ | :--------------------------------- |
| `auth`         | 7 archivos de test        | Mejor cobertura — incluye unit e integration tests |
| `transactions` | 5 archivos de test        | Tests de componentes UI            |
| `reports`      | 4 archivos de test        | Tests de componentes UI            |
| `services/`    | **0 archivos**            | ❌ Sin tests para servicios HTTP   |
| `hooks/`       | **0 archivos**            | ❌ Sin tests para custom hooks     |
| `stores/`      | **0 archivos**            | ❌ Sin tests para Zustand stores   |
| `adapters/`    | **0 archivos**            | ❌ Sin tests para adapters/mappers |

**Riesgo:** La pipeline de CI/CD pasa en verde pero **valida muy poca funcionalidad real**. Cualquier refactoring puede introducir regresiones silenciosas.

---

#### DT-FE-01: God Component — DataTable.tsx (Severidad: MEDIA)

**Evidencia:** `app/Frontend/src/modules/transactions/components/DataTable.tsx` mezcla:
- Lógica de UI (renderizado de tabla).
- Lógica de negocio (filtrado por `description`, `type`, `category`).
- Lógica de estado (paginación manual `pageIndex`, slices).
- Formateo (moneda, fechas, colores de categoría).

**Impacto:** Difícil de leer, mantener, testear, y reutilizar.

---

#### DT-FE-02: Hardcoded Values (Severidad: MEDIA)

- `pageSize = 10` definido dentro del componente.
- Mapa de colores `getCategoryColor()` hardcodeado.
- Textos en español hardcodeados en la UI (sin i18n).

---

#### DT-FE-03: `console.log` en Producción (Severidad: BAJA)

**Evidencia:** `HttpClient.ts` líneas 48, 52, 59, 64, 77, 85, 86, 94 — El HTTP interceptor usa `console.log` y `console.error` extensivamente para logging. Debería usar un logging condicional o ser removido en producción.

---

#### DT-BE-02: TransactionMessage Duplicado e Inconsistente (Severidad: BAJA)

**Evidencia:**
- En `transaction/infrastructure/dto/TransactionMessage.java` — Clase con Lombok (`@Data @Builder`), tiene campo `createdAt`.
- En `report/infrastructure/dto/TransactionMessage.java` — **Java Record** con Bean Validation (`@NotNull`, `@NotBlank`), **no** tiene campo `createdAt`.

**Riesgo:** Si se agrega un campo en un servicio y no en el otro, la deserialización puede fallar silenciosamente.

---

#### DT-BE-03: Manejo de Eventos Frágil (Severidad: MEDIA)

- `TransactionCreatedEvent` extiende `ApplicationEvent` (Spring). Si el evento falla en `@Async`, no hay mecanismo de retry ni Dead Letter Queue (DLQ).
- `ReportConsumer` no implementa manejo de errores — Si `updateReport()` lanza una excepción, el mensaje se pierde (no hay DLQ configurado).
- No hay idempotencia — Si el mismo mensaje se procesa dos veces, el reporte se acumula incorrectamente.

---

#### DT-ARCH-02: Acoplamiento del Service Interface a Spring (Severidad: MEDIA)

```java
// ReportService.java — El contrato del servicio importa Pageable de Spring:
import org.springframework.data.domain.Pageable;
public interface ReportService {
    PaginatedResponse<ReportResponse> getReportsByUserId(String userId, Pageable pageable);
}
```

Esto impide que la interfaz del servicio sea un Port puro del dominio.

---

### 5.3 📊 Resumen de Evaluación

```
CATEGORÍA              ESTADO          ACCIÓN REQUERIDA
──────────────────────────────────────────────────────────
Separación API/Entidad  ✅ Resuelto     Mantener (DTOs Records)
Controllers delegados   ✅ Resuelto     Mantener
Desacoplamiento MQ      🟡 Parcial     Completar con Ports
Configuración externa   ✅ Resuelto     Mantener
Validación de entrada   ✅ Resuelto     Mantener y extender
Auth frontend           ✅ Resuelto     Mantener patrón Repository
──────────────────────────────────────────────────────────
Arquitectura Hexagonal  🔴 Ausente      MIGRAR (Phase 2)
Test Coverage Backend   🔴 Crítico      RESOLVER INMEDIATAMENTE (P0)
Test Coverage Frontend  🟡 Parcial      Extender a services/hooks/stores
God Component           🔴 Presente     Refactorizar DataTable.tsx
Resilencia MQ           🔴 Ausente      Implementar DLQ + Retry
Consistencia inter-svc  🟡 Parcial      Unificar TransactionMessage
```

---

## 6. Prioridades Inmediatas Recomendadas

| Prioridad | Acción | Justificación |
|---|---|---|
| **P0** | Cerrar gap de test coverage en `TransactionServiceImpl` y `ReportServiceImpl` (tests para todos los métodos + edge cases) | Sin tests, cualquier refactoring futuro (incluyendo la migración hexagonal) es de alto riesgo. |
| **P0** | Agregar tests `@WebMvcTest` para controllers | Los endpoints REST son la frontera pública — deben tener tests de integración. |
| **P1** | Migrar estructura de paquetes a Hexagonal | Prerequisito para escalar el sistema con SOLID. |
| **P1** | Implementar DLQ y manejo de errores en RabbitMQ | Sin esto, los mensajes fallidos se pierden. |
| **P2** | Refactorizar `DataTable.tsx` | Extraer lógica a hooks (`useDataTable`, `useTransactionFilters`). |
| **P2** | Agregar tests para frontend services, hooks y stores | Cubrir la lógica de negocio del frontend. |
| **P3** | Eliminar `console.log` de `HttpClient.ts` | Limpiar logs de producción. |
| **P3** | Unificar `TransactionMessage` entre servicios | Asegurar consistencia en serialización. |

---

> **📌 Siguiente paso:** Con este informe como base, proceder a **Phase 2 — Refactoring de Arquitectura**, comenzando con TDD para establecer la red de seguridad de tests antes de cualquier migración estructural.

---

## 7. Registro de Documentación Javadoc (Activity 1.2)

> **Fecha de aplicación:** 2026-02-19
> **Actividad:** Phase 1, Activity 1.2 — Aplicación de Javadoc estándar a clases críticas

### 7.1 Clases Documentadas

Se identificaron las **3 clases más complejas y menos documentadas** del backend, priorizando por: (a) cantidad de lógica de negocio, (b) rol crítico en la arquitectura Event-Driven, y (c) ausencia total de documentación previa.

| # | Clase | Ubicación | Complejidad | Documentación Añadida |
|---|---|---|---|---|
| **1** | `ReportServiceImpl` | `report/.../service/impl/ReportServiceImpl.java` | **ALTA** — 5 métodos, lógica de agregación, get-or-create, cálculo de balance | Javadoc de clase (rol Event-Driven, lógica de negocio, 3 deudas técnicas) + Javadoc en 5 métodos (`getOrCreateReport`, `updateReport`, `getReport`, `getReportsByUserId`, `getReportsByPeriodRange`) |
| **2** | `TransactionServiceImpl` | `transaction/.../service/impl/TransactionServiceImpl.java` | **ALTA** — Orquestación create→persist→publish, punto de entrada de la cadena de eventos | Javadoc de clase (rol como productor originario, desacoplamiento del broker, 3 deudas técnicas) + Javadoc en 3 métodos (`create`, `getById`, `getAll`) |
| **3** | `ReportConsumer` | `report/.../infrastructure/ReportConsumer.java` | **MEDIA-ALTA** — Gateway RabbitMQ, dual-queue listener, puente broker→negocio | Javadoc de clase (adaptador de entrada, configuración de colas, 3 deudas técnicas) + Javadoc en 2 métodos (`consumeCreated`, `consumeUpdated`) |

### 7.2 Micro-Deudas Técnicas Descubiertas Durante la Documentación

La inspección detallada a nivel de método reveló **9 nuevas micro-deudas técnicas** no catalogadas en la evaluación inicial (§5). Se asignaron IDs con prefijo `DT-DOC-` para distinguirlas:

| ID | Clase Afectada | Severidad | Descripción |
|---|---|---|---|
| **DT-DOC-01** | `ReportServiceImpl` | BAJA | Los métodos de solo lectura (`getReport`, `getReportsByUserId`, `getReportsByPeriodRange`) usan `@Transactional` sin `readOnly = true`, forzando conexiones read-write innecesarias. |
| **DT-DOC-02** | `ReportServiceImpl` | ALTA | No hay mecanismo de **idempotencia** en `updateReport()`. Si un mensaje se procesa dos veces (retry sin DLQ), los totales se acumulan incorrectamente. Necesita registro de `transactionId` procesados. |
| **DT-DOC-03** | `ReportServiceImpl` | MEDIA | La interfaz `ReportService` importa `TransactionMessage` desde `infrastructure.dto`, acoplando el contrato del servicio al DTO de infraestructura en lugar de un Port del dominio. |
| **DT-DOC-04** | `TransactionServiceImpl` | BAJA | El método `create()` no tiene `@Transactional`. Si `save()` ocurre pero el evento falla antes de publicarse, no hay rollback automático. Mitigado parcialmente porque el evento es `@Async`. |
| **DT-DOC-05** | `TransactionServiceImpl` | MEDIA | Faltan operaciones `update()` y `delete()` — La interfaz `TransactionService` solo define `create`, `getById` y `getAll`. CRUD incompleto. |
| **DT-DOC-06** | `TransactionServiceImpl` | BAJA | `getById()` lanza `EntityNotFoundException` con mensaje genérico hardcodeado `"Transaction not found"` sin incluir el ID buscado, dificultando el debugging. |
| **DT-DOC-07** | `ReportConsumer` | ALTA | No hay manejo de errores en los consumidores. Si `updateReport()` falla, el mensaje se pierde sin retry ni DLQ. Refuerza el hallazgo DT-BE-03 de §5. |
| **DT-DOC-08** | `ReportConsumer` | MEDIA | `consumeCreated()` y `consumeUpdated()` ejecutan **la misma lógica**. Una actualización se trata como nueva acumulación en lugar de corrección (debería revertir valor anterior + aplicar nuevo). |
| **DT-DOC-09** | `ReportConsumer` | BAJA | No hay validación del `TransactionMessage` antes de procesarlo. Campos nulos o inválidos causan excepciones profundas en `ReportServiceImpl`, dificultando diagnóstico. |

### 7.3 Relación con Deudas Existentes (§5)

| Deuda Original (§5) | Micro-Deudas Relacionadas (§7) | Comentario |
|---|---|---|
| DT-BE-03: Manejo de Eventos Frágil | DT-DOC-02, DT-DOC-07, DT-DOC-08 | La documentación confirma y detalla las 3 variantes del problema: falta de idempotencia, falta de DLQ, y lógica idéntica para create/update. |
| DT-ARCH-01: Ausencia de Hexagonal | DT-DOC-03 | El acoplamiento del contrato `ReportService` al DTO de infraestructura es un síntoma directo de la falta de Ports. |
| DT-QA-01: Test Coverage | DT-DOC-05 | El CRUD incompleto (sin update/delete) implica que los tests futuros deben cubrir no solo lo existente, sino lo faltante cuando se implemente. |
| (Nuevo) | DT-DOC-01, DT-DOC-04, DT-DOC-06, DT-DOC-09 | Micro-deudas de calidad descubiertas que no estaban previamente catalogadas. |
