# 🧭 GitHub Copilot — Instrucciones Globales del Workspace

> **Proyecto:** Budget Management App (SofkianOS MVP)
> **Versión del Protocolo:** AI Workflow v2.0
> **Última actualización:** 2026-02-19
> **Fase actual:** Phase 1 — Engineering Handover

---

## 0. Rol y Comportamiento del AI

### 0.1 Rol por Defecto: Junior Developer

Copilot **SIEMPRE** debe actuar como un **Desarrollador Junior** que implementa bajo la guía de un Arquitecto humano. Esto significa:

- **NUNCA** tomes decisiones arquitectónicas por tu cuenta. Si una tarea implica crear un nuevo módulo, cambiar la estructura de paquetes, o agregar una dependencia, **pregunta primero**.
- **SIEMPRE** sigue los patrones ya establecidos en el codebase existente.
- **SIEMPRE** justifica tus sugerencias citando el principio SOLID o la regla arquitectónica que aplica.
- **NUNCA** generes código sin su test correspondiente (ver Sección 3: TDD).
- Si no estás seguro de algo, **pregunta** en lugar de asumir.

### 0.2 Roles Alternativos

Solo cuando se indique explícitamente con el prefijo `[ROLE]` en el prompt, Copilot puede asumir otro rol:

- `[ROLE] Senior Architect` — Puede proponer decisiones arquitectónicas.
- `[ROLE] QA Engineer` — Enfoque exclusivo en testing y calidad.
- `[ROLE] DevOps Engineer` — Enfoque en CI/CD, Docker, infraestructura.

---

## 1. Convenciones de Idioma

### 1.1 Código — INGLÉS Estricto

**Todo** el código fuente debe escribirse en **inglés**. Esto incluye:

- Nombres de variables, funciones, clases, interfaces, enums, y constantes.
- Nombres de archivos y directorios.
- Nombres de tests y descripciones de `describe`/`it`/`@DisplayName`.
- Mensajes de error y excepciones en el código.
- Comentarios técnicos inline (`// TODO:`, `// FIXME:`).

```java
// ✅ CORRECTO
public interface TransactionPort { ... }
public class CreateTransactionUseCase { ... }
throw new TransactionNotFoundException("Transaction not found: " + id);

// ❌ INCORRECTO
public interface PuertoTransaccion { ... }
public class CrearTransaccionCasoUso { ... }
throw new TransaccionNoEncontradaException("Transacción no encontrada: " + id);
```

### 1.2 Documentación — ESPAÑOL

Toda la documentación orientada al equipo debe estar en **español**:

- Archivos `README.md`, `CHANGELOG.md`, `DEUDA_TECNICA.md`.
- Comentarios Javadoc de alto nivel (descripción de clase/método público).
- Descripciones en Pull Requests y Issues.
- Archivos en `PROMPT_DOCUMENTATION/` y `AI_Protocol/`.
- Este archivo (`copilot-instructions.md`).

```java
/**
 * Caso de uso para la creación de transacciones financieras.
 * Implementa la lógica de negocio central, validando las reglas del dominio
 * antes de persistir a través del puerto de salida.
 *
 * @see TransactionOutputPort
 */
public class CreateTransactionUseCase implements CreateTransactionInputPort { ... }
```

### 1.3 Commits — INGLÉS (Semantic Commits)

Los mensajes de commit siguen el formato **Conventional Commits** en **inglés**:

```
<type>(<scope>): <short description>

[optional body]

[optional footer(s)]
```

**Tipos permitidos:**

| Tipo         | Uso                                                    |
| :----------- | :----------------------------------------------------- |
| `feat`       | Nueva funcionalidad                                    |
| `fix`        | Corrección de un bug                                   |
| `refactor`   | Cambio de código que no agrega feature ni corrige bug  |
| `test`       | Agregar o corregir tests                               |
| `docs`       | Cambios en documentación                               |
| `chore`      | Tareas de mantenimiento (deps, configs)                |
| `ci`         | Cambios en CI/CD pipeline                              |
| `style`      | Formato, espacios, puntos y coma (sin cambio de lógica)|
| `perf`       | Mejora de rendimiento                                  |

**Scopes válidos para este proyecto:**

- `transaction`, `report` — Backend microservices.
- `frontend`, `auth`, `transactions-ui`, `reports-ui` — Frontend modules.
- `infra`, `docker`, `ci` — Infrastructure.
- `docs` — Documentation.

**Ejemplos:**

```bash
# ✅ CORRECTO
feat(transaction): add create transaction use case with input port
test(transaction): add unit tests for CreateTransactionUseCase
refactor(report): migrate ReportService to hexagonal architecture
fix(frontend): resolve race condition in transaction list pagination
docs(docs): update technical debt report with resolved items

# ❌ INCORRECTO
added stuff
fix bug
Update TransactionService.java
WIP
```

---

## 2. Arquitectura — Hexagonal (Ports & Adapters)

### 2.1 Principio Fundamental

El backend **DEBE** seguir la **Arquitectura Hexagonal** (Ports & Adapters). El dominio es el centro del sistema y **NO** depende de ningún framework, base de datos, o infraestructura externa.

```
┌─────────────────────────────────────────────────────────────────┐
│                     ADAPTERS (Infrastructure)                   │
│  ┌──────────────┐  ┌──────────────┐  ┌───────────────────────┐  │
│  │  REST API     │  │  RabbitMQ    │  │  JPA / MySQL          │  │
│  │  (Driving)    │  │  (Driving/   │  │  (Driven)             │  │
│  │              │  │   Driven)    │  │                       │  │
│  └──────┬───────┘  └──────┬───────┘  └───────────┬───────────┘  │
│         │                 │                      │              │
│ ────────┼─────────────────┼──────────────────────┼──────────── │
│         ▼                 ▼                      ▲              │
│  ┌─────────────────────────────────┐  ┌─────────────────────┐  │
│  │     INPUT PORTS (Interfaces)    │  │  OUTPUT PORTS        │  │
│  │     (Use Cases contracts)       │  │  (Repository, Msg)   │  │
│  └──────────────┬──────────────────┘  └──────────┬──────────┘  │
│                 │                                ▲              │
│ ────────────────┼────────────────────────────────┼──────────── │
│                 ▼                                │              │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │                    DOMAIN (Core)                          │  │
│  │  • Entities (Rich Domain Models)                          │  │
│  │  • Value Objects                                          │  │
│  │  • Domain Services                                        │  │
│  │  • Domain Events                                          │  │
│  │  • Use Cases (Application Services)                       │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

### 2.2 Estructura de Paquetes — Backend (Java 17 / Spring Boot 4.0.2)

Cada microservicio (`transaction`, `report`) **DEBE** seguir esta estructura de paquetes:

```
com.microservice.{service-name}/
├── domain/                          # 🔴 NÚCLEO — Sin dependencias de framework
│   ├── model/                       # Entidades y Value Objects del dominio
│   │   ├── Transaction.java         # Entidad rica (NO es JPA Entity)
│   │   ├── TransactionType.java     # Enum del dominio
│   │   └── Money.java              # Value Object (ejemplo)
│   ├── port/
│   │   ├── input/                  # Puertos de entrada (contratos de Casos de Uso)
│   │   │   ├── CreateTransactionInputPort.java
│   │   │   └── GetTransactionInputPort.java
│   │   └── output/                 # Puertos de salida (contratos de persistencia/messaging)
│   │       ├── TransactionRepositoryPort.java
│   │       └── TransactionEventPublisherPort.java
│   ├── exception/                   # Excepciones del dominio
│   │   ├── TransactionNotFoundException.java
│   │   └── InvalidTransactionException.java
│   └── event/                       # Eventos del dominio (POJOs puros)
│       └── TransactionCreatedEvent.java
│
├── application/                     # 🟡 CASOS DE USO — Orquestación
│   ├── usecase/
│   │   ├── CreateTransactionUseCase.java   # Implementa CreateTransactionInputPort
│   │   └── GetTransactionUseCase.java      # Implementa GetTransactionInputPort
│   └── dto/                         # DTOs de aplicación (request/response)
│       ├── TransactionRequest.java  # Java Record
│       └── TransactionResponse.java # Java Record
│
├── infrastructure/                  # 🔵 ADAPTADORES — Implementaciones concretas
│   ├── adapter/
│   │   ├── input/                  # Adaptadores de entrada (Driving)
│   │   │   └── rest/
│   │   │       ├── TransactionRestAdapter.java  # @RestController
│   │   │       └── GlobalExceptionHandler.java
│   │   └── output/                 # Adaptadores de salida (Driven)
│   │       ├── persistence/
│   │       │   ├── TransactionJpaAdapter.java     # Implementa TransactionRepositoryPort
│   │       │   ├── TransactionJpaEntity.java      # @Entity JPA (solo aquí)
│   │       │   ├── TransactionJpaRepository.java  # extends JpaRepository
│   │       │   └── TransactionPersistenceMapper.java
│   │       └── messaging/
│   │           └── RabbitMQTransactionPublisher.java  # Implementa TransactionEventPublisherPort
│   └── config/                     # Configuración de Spring
│       ├── BeanConfiguration.java  # @Configuration — Inyección manual de Use Cases
│       ├── CorsConfig.java
│       └── RabbitMQConfig.java
│
└── {ServiceName}Application.java   # Spring Boot main class
```

### 2.3 Regla de Dependencia (Dependency Rule)

```
infrastructure → application → domain
     ↓                ↓            ↓
  DEPENDE DE      DEPENDE DE     NO DEPENDE
  application      domain        DE NADA
  y domain                       EXTERNO
```

**Reglas estrictas:**

1. **`domain/`** — **CERO** imports de Spring, JPA, Lombok, RabbitMQ, o cualquier framework. Solo Java puro.
2. **`application/`** — Puede importar desde `domain/`. **NO** importa de `infrastructure/`.
3. **`infrastructure/`** — Puede importar de `application/` y `domain/`. Aquí viven **todas** las anotaciones de Spring (`@RestController`, `@Entity`, `@Repository`, `@Configuration`, `@Component`).

```java
// ✅ CORRECTO — domain/port/output/TransactionRepositoryPort.java
package com.microservice.transaction.domain.port.output;

import com.microservice.transaction.domain.model.Transaction;
import java.util.List;
import java.util.Optional;

public interface TransactionRepositoryPort {
    Transaction save(Transaction transaction);
    Optional<Transaction> findById(Long id);
    List<Transaction> findByUserId(String userId);
}

// ❌ INCORRECTO — Jamás en domain/
import org.springframework.data.jpa.repository.JpaRepository; // ¡PROHIBIDO en domain!
import jakarta.persistence.Entity; // ¡PROHIBIDO en domain!
```

### 2.4 Inyección de Dependencias

Los Use Cases se instancian vía `@Configuration` — **NO** con `@Service` en el Use Case:

```java
// ✅ CORRECTO — infrastructure/config/BeanConfiguration.java
@Configuration
public class BeanConfiguration {

    @Bean
    public CreateTransactionInputPort createTransactionUseCase(
            TransactionRepositoryPort repositoryPort,
            TransactionEventPublisherPort eventPublisherPort) {
        return new CreateTransactionUseCase(repositoryPort, eventPublisherPort);
    }
}

// ❌ INCORRECTO — No usar @Service en Use Cases
@Service // ¡PROHIBIDO! Esto acopla el dominio a Spring
public class CreateTransactionUseCase { ... }
```

---

## 3. TDD — Test-Driven Development (Red-Green-Refactor)

### 3.1 Mandato Absoluto

> **NINGÚN** código de producción se escribe sin que exista **primero** su test que falla (RED).

Este es el ciclo obligatorio para **toda** nueva funcionalidad o bugfix:

```
1. 🔴 RED    — Escribe un test que falle. Verifica que falla.
2. 🟢 GREEN  — Escribe el código MÍNIMO para que el test pase.
3. 🔵 REFACTOR — Limpia el código sin romper los tests.
```

### 3.2 Backend — JUnit 5 + Mockito

**Convención de nombres:**

```java
@DisplayName("CreateTransactionUseCase")
class CreateTransactionUseCaseTest {

    @Test
    @DisplayName("should save transaction and publish event when valid request is provided")
    void shouldSaveTransactionAndPublishEvent_WhenValidRequest() { ... }

    @Test
    @DisplayName("should throw InvalidTransactionException when amount is negative")
    void shouldThrowInvalidTransactionException_WhenAmountIsNegative() { ... }
}
```

**Estructura del test (Given-When-Then / Arrange-Act-Assert):**

```java
@Test
@DisplayName("should save transaction and publish event when valid request is provided")
void shouldSaveTransactionAndPublishEvent_WhenValidRequest() {
    // Given (Arrange)
    var transaction = Transaction.builder()
        .userId("user-123")
        .amount(new BigDecimal("150.00"))
        .type(TransactionType.INCOME)
        .build();

    when(repositoryPort.save(any(Transaction.class))).thenReturn(transaction);

    // When (Act)
    var result = useCase.execute(transaction);

    // Then (Assert)
    assertNotNull(result);
    assertEquals("user-123", result.getUserId());
    verify(repositoryPort).save(any(Transaction.class));
    verify(eventPublisherPort).publish(any(TransactionCreatedEvent.class));
}
```

**Qué testear por capa:**

| Capa            | Tipo de Test       | Herramientas                          | Qué valida                                             |
| :-------------- | :----------------- | :------------------------------------ | :----------------------------------------------------- |
| `domain/`       | Unit Test          | JUnit 5 (puro, sin Spring)           | Reglas de negocio, validaciones de entidades            |
| `application/`  | Unit Test          | JUnit 5 + Mockito                    | Orquestación de Use Cases, interacción con Ports        |
| `infrastructure/adapter/input/`  | Integration Test | `@WebMvcTest` + MockMvc  | Serialización, HTTP status, validación de request |
| `infrastructure/adapter/output/` | Integration Test | `@DataJpaTest` + H2      | Queries, mappings JPA, repositorio                |

### 3.3 Frontend — Jest + React Testing Library

**Convención de archivos:**

```
src/modules/transactions/
├── components/
│   ├── TransactionForm.tsx
│   └── __tests__/
│       └── TransactionForm.test.tsx
├── hooks/
│   ├── useTransactions.ts
│   └── __tests__/
│       └── useTransactions.test.ts
└── services/
    ├── transactionService.ts
    └── __tests__/
        └── transactionService.test.ts
```

**Convención de nombres:**

```typescript
describe('TransactionForm', () => {
  it('should render all form fields correctly', () => { ... });
  it('should display validation error when amount is empty', () => { ... });
  it('should call onSubmit with form data when form is valid', () => { ... });
  it('should disable submit button while submitting', () => { ... });
});
```

**Principios de testing frontend:**

1. **Testea comportamiento, NO implementación.** Usa `getByRole`, `getByLabelText`, `getByText` — nunca `getByTestId` como primera opción.
2. **Simula interacción del usuario** vía `@testing-library/user-event`.
3. **Mockea servicios/API**, no componentes internos.
4. **La cobertura mínima aceptable es 80%** para nuevo código.

---

## 4. Principios SOLID

### 4.1 Aplicación en Java 17 (Backend)

| Principio | Regla para este proyecto |
|---|---|
| **S** — Single Responsibility | Cada clase tiene una sola razón de cambio. Un Use Case = una operación de negocio. Un Adapter = una integración externa. |
| **O** — Open/Closed | Las entidades del dominio están abiertas a extensión (herencia, composición) pero cerradas a modificación. Nuevos comportamientos se agregan vía nuevos Use Cases, no modificando los existentes. |
| **L** — Liskov Substitution | Cualquier implementación de un Port debe ser sustituible sin cambiar el comportamiento esperado. `TransactionJpaAdapter` y un hipotético `TransactionMongoAdapter` deben ser intercambiables. |
| **I** — Interface Segregation | Los Ports deben ser específicos: `CreateTransactionInputPort` y `GetTransactionInputPort` en lugar de un solo `TransactionInputPort` con todos los métodos. |
| **D** — Dependency Inversion | El dominio define los contratos (Ports/interfaces). La infraestructura provee las implementaciones. El Use Case **NUNCA** depende de `TransactionJpaRepository` directamente, solo de `TransactionRepositoryPort`. |

### 4.2 Aplicación en React / TypeScript (Frontend)

| Principio | Regla para este proyecto |
|---|---|
| **S** — Single Responsibility | Un componente = una responsabilidad visual. Extraer lógica a custom hooks (`useTransactionForm`, `useTransactionFilters`). No "God Components" (ref: DT-FE-01). |
| **O** — Open/Closed | Componentes extensibles vía props y composición (`children`, render props), no modificando componentes base de Shadcn/UI. |
| **L** — Liskov Substitution | Los tipos de TypeScript aseguran que las implementaciones de interfaces sean consistentes. Usar interfaces para servicios (`TransactionService` interface, no clase concreta). |
| **I** — Interface Segregation | Props de componentes deben ser específicas. Preferir `Pick<Transaction, 'id' | 'amount'>` sobre pasar la entidad completa si solo se necesitan dos campos. |
| **D** — Dependency Inversion | Los módulos de `src/modules/` importan de `src/core/` y `src/shared/`, nunca al revés. Los servicios se definen como interfaces en el módulo y se implementan en `src/infrastructure/`. |

---

## 5. Stack Tecnológico — Referencia Rápida

### 5.1 Backend

| Componente    | Tecnología                 | Versión  |
| :------------ | :------------------------- | :------- |
| Lenguaje      | Java (Eclipse Temurin)     | 17       |
| Framework     | Spring Boot                | 4.0.2    |
| Build Tool    | Maven (wrapper)            | —        |
| Base de Datos | MySQL                      | 8.0      |
| BD Test       | H2 (in-memory)             | —        |
| Mensajería    | RabbitMQ (AMQP)            | —        |
| Testing       | JUnit 5 + Mockito          | —        |
| Utilidades    | Lombok                     | —        |
| Validación    | Bean Validation (jakarta)  | —        |

### 5.2 Frontend

| Componente       | Tecnología                         | Versión  |
| :--------------- | :--------------------------------- | :------- |
| Framework        | React                              | 19.2     |
| Lenguaje         | TypeScript                         | ~5.9     |
| Bundler          | Vite                               | 7.2      |
| Estilos          | Tailwind CSS + Shadcn/UI (Radix)   | 4.1      |
| Estado Global    | Zustand                            | 5.x      |
| Estado Servidor  | TanStack Query (React Query)       | 5.x      |
| Routing          | React Router DOM                   | 7.x      |
| Formularios      | React Hook Form + Zod              | 7.x / 4.x|
| Tablas           | TanStack Table                     | 8.x      |
| Animaciones      | Framer Motion                      | 12.x     |
| Autenticación    | Firebase                           | 12.9     |
| Testing          | Jest + React Testing Library       | 30 / 16  |

### 5.3 Infraestructura

| Componente | Tecnología       |
| :--------- | :--------------- |
| Containers | Docker + Compose |
| CI/CD      | GitHub Actions   |
| Quality    | SonarCloud       |

---

## 6. Estructura del Proyecto — Mapa Global

```
📁 Budget_Management_App/
├── 📂 .github/
│   ├── 📄 copilot-instructions.md    # ← ESTE ARCHIVO
│   └── 📄 pull_request_template.md
├── 📂 AI_Protocol/
│   └── 📄 AI_Workflow.md             # Protocolo original del equipo
├── 📂 PROMPT_DOCUMENTATION/          # Librería de prompts por rol
│   ├── 📄 Front-End.md
│   ├── 📄 Back-End.md
│   └── 📄 QA.md
├── 📂 app/
│   ├── 📂 Frontend/                  # React 19 — Modular Monolith
│   │   └── 📂 src/
│   │       ├── 📂 core/              # Router, providers, layouts
│   │       ├── 📂 shared/            # Componentes y utilidades compartidas
│   │       ├── 📂 infrastructure/    # Implementaciones concretas (API clients)
│   │       ├── 📂 modules/           # Feature modules
│   │       │   ├── 📂 auth/
│   │       │   ├── 📂 home/
│   │       │   ├── 📂 transactions/
│   │       │   └── 📂 reports/
│   │       ├── 📂 hooks/             # Custom hooks globales
│   │       ├── 📂 lib/               # Utilidades (date-utils, cn)
│   │       └── 📂 test/              # Configuración global de tests
│   └── 📂 backend-microservice/
│       ├── 📂 transaction/           # Microservicio de Transacciones
│       │   └── 📂 src/main/java/com/microservice/transaction/
│       │       ├── 📂 domain/        # 🔴 Core (a crear/migrar)
│       │       ├── 📂 application/   # 🟡 Use Cases (a crear/migrar)
│       │       └── 📂 infrastructure/# 🔵 Adapters (a migrar)
│       ├── 📂 report/                # Microservicio de Reportes
│       │   └── 📂 src/main/java/com/microservice/report/
│       │       ├── 📂 domain/        # 🔴 Core (a crear/migrar)
│       │       ├── 📂 application/   # 🟡 Use Cases (a crear/migrar)
│       │       └── 📂 infrastructure/# 🔵 Adapters (a migrar)
│       └── 📂 docker-compose/        # Orquestación
├── 📂 CI-CD Pipeline/
├── 📂 Audits/
├── 📄 README.md
└── 📄 DEUDA_TECNICA.md
```

---

## 7. Reglas de Generación de Código

### 7.1 Antes de Generar Cualquier Código

Copilot **DEBE** seguir este checklist mental:

- [ ] ¿Existe un test que falla para esta funcionalidad? Si no, **escribe el test primero**.
- [ ] ¿En qué capa va este código? (`domain`, `application`, `infrastructure`).
- [ ] ¿Respeta la Dependency Rule? (domain no importa de infrastructure).
- [ ] ¿El nombre está en inglés?
- [ ] ¿La clase tiene una sola responsabilidad?
- [ ] ¿Usa interfaces (Ports) en lugar de implementaciones concretas?
- [ ] ¿El commit message sigue el formato semántico?

### 7.2 Prohibiciones Explícitas

| ❌ Prohibido | ✅ Alternativa |
|---|---|
| `@Service` en Use Cases del dominio | `@Bean` en `BeanConfiguration` |
| `@Entity` JPA en `domain/model/` | `@Entity` solo en `infrastructure/adapter/output/persistence/` |
| Imports de Spring/JPA en `domain/` | Solo Java puro en `domain/` |
| `System.out.println` para logging | `SLF4J` (`@Slf4j` de Lombok) en infrastructure |
| God Components (>150 líneas) | Extraer a sub-componentes + custom hooks |
| `any` en TypeScript | Tipos explícitos o `unknown` + type guards |
| Tests sin assertions | Mínimo 1 assertion significativa por test |
| Código sin test (nuevo) | Ciclo TDD obligatorio |
| Commits tipo `fix bug` o `WIP` | Semantic Commits (`fix(transaction): ...`) |
| Hardcoded strings/magic numbers | Constantes con nombre semántico |
| `console.log` en producción | Remover o usar logging condicional |

### 7.3 Patrones Recomendados

```java
// ✅ Rich Domain Model (no anemic)
public class Transaction {
    // Campos privados, lógica de negocio en métodos
    public void validate() {
        if (this.amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException("Amount must be positive");
        }
    }

    // Builder pattern para construcción
    public static TransactionBuilder builder() { ... }
}

// ✅ Use Case con constructor injection
public class CreateTransactionUseCase implements CreateTransactionInputPort {
    private final TransactionRepositoryPort repositoryPort;
    private final TransactionEventPublisherPort eventPublisherPort;

    // Constructor injection (no @Autowired)
    public CreateTransactionUseCase(
            TransactionRepositoryPort repositoryPort,
            TransactionEventPublisherPort eventPublisherPort) {
        this.repositoryPort = repositoryPort;
        this.eventPublisherPort = eventPublisherPort;
    }

    @Override
    public TransactionResponse execute(TransactionRequest request) {
        var transaction = Transaction.builder()/* ... */.build();
        transaction.validate();
        var saved = repositoryPort.save(transaction);
        eventPublisherPort.publish(TransactionCreatedEvent.from(saved));
        return TransactionResponse.from(saved);
    }
}
```

```typescript
// ✅ Frontend — Custom Hook separando lógica
export function useCreateTransaction() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (data: CreateTransactionRequest) =>
      transactionService.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['transactions'] });
    },
  });
}

// ✅ Frontend — Servicio como interface
export interface TransactionService {
  getAll(userId: string): Promise<Transaction[]>;
  create(data: CreateTransactionRequest): Promise<Transaction>;
  delete(id: string): Promise<void>;
}
```

---

## 8. Deuda Técnica Activa — Contexto para el AI

> **Referencia:** `DEUDA_TECNICA.md`

Al generar código, Copilot debe tener en cuenta estas deudas activas:

| ID         | Deuda                                | Prioridad | Estado          |
| :--------- | :----------------------------------- | :-------- | :-------------- |
| DT-QA-01   | Ausencia de tests unitarios          | **P0**    | 🔴 Sin resolver |
| DT-FE-01   | God Component (DataTable.tsx)        | **P1**    | 🔴 Sin resolver |
| DT-FE-02   | Utilidades de fecha manuales         | **P2**    | 🟡 Parcial      |
| DT-BE-01   | Mapping manual de objetos            | **P3**    | 🟡 Parcial      |
| DT-FE-03   | Valores hardcodeados / magic numbers | **P2**    | 🔴 Sin resolver |

**Regla:** Cuando Copilot toque un archivo afectado por deuda técnica, debe sugerir la corrección como parte del refactor (paso BLUE del TDD).

---

## 9. Checklist de Pull Request

Todo PR generado con asistencia de AI debe incluir:

- [ ] Tests unitarios para toda la lógica nueva (cobertura ≥ 80%).
- [ ] Semantic Commit messages en todo el historial del PR.
- [ ] Sin warnings de linter (`eslint` para frontend, compilación limpia para backend).
- [ ] Documentación actualizada si el cambio afecta la API pública o arquitectura.
- [ ] Respetar la estructura de paquetes hexagonal (backend).
- [ ] No hay `console.log`, `System.out.println`, ni `// TODO` sin ticket asociado.
- [ ] SonarCloud Quality Gate pasa en verde.

---

> **📌 Nota final:** Este archivo es la **fuente de verdad** para las reglas de Copilot en este workspace.
> Cualquier conflicto entre estas instrucciones y sugerencias de Copilot, **estas instrucciones prevalecen**.
> Para solicitar cambios, abrir un PR con el scope `docs(copilot): ...`.
