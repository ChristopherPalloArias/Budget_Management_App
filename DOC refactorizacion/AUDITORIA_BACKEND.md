# Backend Architecture & Codebase Audit Report

## 1. Architectural Overview
- **Pattern in Use:** Layered Architecture mapped into a Microservices format (`auth`, `report`, `transaction`). 
- **Folder Structure vs. Clean Architecture:** The package structure is highly technical (`controller`, `service`, `repository`, `dto`, `model`) rather than domain-centric (e.g., separating bounded contexts or use cases). This is a standard Spring Boot MVC layout, but it deviates from Clean Architecture, as Core Domain entities do not contain the business rules (Anemic Domain Model) and the inner layers depend heavily on external frameworks.
- **Separation of Concerns:** 
  - Controllers correctly delegate to Services.
  - However, the Service layer leaks boundaries. For example, `ReportServiceImpl` relies directly on Web/Servlet contexts (`HttpServletRequest`), thereby mixing HTTP-transport concerns with business logic.

## 2. SOLID Violations
- **SRP (Single Responsibility Principle):**
  - *Violation:* `ReportServiceImpl` is acting as a "God Class" (nearly 500 lines). It handles RabbitMQ event processing, aggregates balances, extracts JWT tokens from the HTTP context, executes synchronous REST calls to other microservices, and orchestrates repository saves. 
- **OCP (Open/Closed Principle):**
  - *Violation:* The `ReportServiceImpl.accumulateTransactionAmount` method uses an `if-else` chain to determine whether to add to `totalIncome` or `totalExpense` based on `TransactionType`. Adding a new transaction type (e.g., `TRANSFER` or `ADJUSTMENT`) requires modifying this class instead of extending it.
- **LSP (Liskov Substitution Principle):**
  - *Violation:* The `ReportService` interface is coupled to infrastructure-level DTOs (`TransactionMessage`, which is imported from `infrastructure.dto`). An interface should define domain-level contracts, not infrastructure messages. 
- **ISP (Interface Segregation Principle):**
  - *Violation:* `ReportService` is bloated. It forces clients to depend on HTTP recalculation logic (`recalculateReport`), async event consuming (`updateReport`), and read-only query fetching (`getReport`, `getReportsByUserId`). It should be split into `ReportQueryService` and `ReportCommandService` (CQRS).
- **DIP (Dependency Inversion Principle):**
  - *Violation:* `ReportServiceImpl` directly instantiates `HttpHeaders`, `HttpEntity` and uses a concrete `RestTemplate` to make HTTP calls. It should depend on an abstraction, such as a `TransactionClient` port (e.g., a Feign client interface).

## 3. Code Smells
- **Tight Coupling to Framework Contexts:** Extracts JWT token manually via `RequestContextHolder.getRequestAttributes()` inside `ReportServiceImpl`. Services should be completely isolated from the Servlet API.
- **Hardcoded Values & Magic Strings:** 
  - URL `http://transaction:8081/api/v1/transactions?period=...` is hardcoded in the service instead of using `application.yml` properties, Service Discovery (Eureka), or Feign.
  - Magic strings like `Bearer `, `INCOME`, `EXPENSE`.
- **Anemic Domain Model:** The `Report` and `Transaction` entities are data bags (only getters/setters). The logic for `calculateBalance(totalIncome, totalExpense)` is placed in the service layer, whereas the `Report` entity itself should be responsible for updating its own totals and invariants.
- **Improper Exception Handling:** `ReportServiceImpl.recalculateReport` catches a generic `Exception e` and wraps it in a non-specific `RuntimeException`, losing standard contextual HTTP/Error domain translation capabilities (e.g., 404 vs 500 errors).
- **Missing Idempotency:** Noted as technical debt inside the codebase (`DT-DOC-02`), event consumers (`updateReport`) process RabbitMQ messages without an idempotency key. Messages redelivered will duplicate income/expense accumulations.

## 4. Design Pattern Analysis
- **Patterns in Use:**
  - *Repository Pattern:* Via Spring Data JPA.
  - *Data Transfer Objects (DTO) & Mappers:* Clean usage separating external boundaries from internal models.
- **Anti-Patterns Present:**
  - *Service Locator (Variant):* Using `RequestContextHolder` to globally look up the current request state inside a lower layer.
  - *Hardcoded RPC:* Using `RestTemplate` with hardcoded URIs.
- **Recommended Patterns to Implement:**
  - *Strategy Pattern:* To manage different `TransactionType` calculations dynamically.
  - *API Gateway / Feign Clients:* For declarative, clean REST communications between microservices.
  - *Rich Domain Model:* Moving state-mutating logic into the entities.

## 5. Scalability & Maintainability Assessment
- **Module Coupling:** High coupling between `report` and `transaction` microservices due to hardcoded network locations. Modifying the deployment landscape will break `report`.
- **Readiness for Horizontal Scaling:** *Low in certain areas.* The lack of idempotency in RabbitMQ message consumption means scaling the `report` consumer pods or experiencing network instability will corrupt financial data via duplicate processing.
- **Testability:** Methods retrieving Servlet contexts staticly (`RequestContextHolder`) in `ReportServiceImpl` are virtually untestable via pure Unit Tests without heavy mocking of the Spring Web Context. 

## 6. Security & Configuration
- **Token Leakage Risk:** `ReportServiceImpl` blindly grabs the user's `Authorization` header from the current request thread and forwards it to the `transaction` service. If the process is triggered asynchronously or outside a web request, it will crash with a `NullPointerException` or result in an Unauthenticated error. Service-to-service communication should ideally use a dedicated system-level token (Client Credentials grant) or explicit token propagation architectures.
- **Hardcoded Routing:** Hardcoding `"http://transaction:8081..."` bypasses environmentally secure configuration injection. 

## 7. Per-Finding Documentation

| Affected Component | Violated Principle / Smell | Severity | Impact | Suggested Direction |
| :--- | :--- | :--- | :--- | :--- |
| `ReportServiceImpl.recalculateReport` | **ISP / SRP** (God Class) | High | Difficult to test; business logic mixed with infrastructure. | Extract REST call logic into a dedicated `TransactionClient` adapter. |
| `ReportServiceImpl.getJwtFromContext` | **Coupling to Servlet API** | Critical | Service crashes in async threads; impossible pure unit testing. | Pass tokens securely via method arguments or configure a Feign Interceptor. |
| `ReportServiceImpl` | **Hardcoded URLs** | High | Prevents dynamic scaling/deployment. | Move URIs to `application.yml` and use `@Value` or Spring Cloud Discovery. |
| `ReportServiceImpl.accumulate...` | **OCP / Anemic Model** | Medium | Logic duplication; risky to add new types. | Move balance calculation and accumulation into the `Report` entity. |
| `ReportServiceImpl.updateReport` | **Lack of Idempotency** | Critical | Data corruption (duplicate aggregates) if RabbitMQ retries. | Create a `ProcessedMessage` table to store processed message IDs. |
| `ReportService` (Interface) | **LSP / DIP** | Low | UI/Domain depends on Messaging DTOs (`TransactionMessage`). | Map RabbitMQ generic messages to Domain representations before calling the Service. |
| `ReportServiceImpl` (Catch block) | **Swallowed Errors** | Medium | Silent/Generic failures obscure microservice timeouts. | Throw specific Custom Exceptions like `ServiceIntegrationException`. |

## 8. Summary Table

| Component | Violation / Smell | Severity | Impact |
| :--- | :--- | :--- | :--- |
| `ReportServiceImpl` | Web Context Coupling (RequestContext) | Critical | Prevents testing, crashes async tasks |
| `ReportServiceImpl` | Missing Idempotency in Consumer | Critical | Financial data corruption on retries |
| `ReportServiceImpl` | Hardcoded Microservice URIs | High | Fails container orchestration/scaling |
| `ReportServiceImpl` | SRP / God Class | High | Poor maintainability and bloat |
| `ReportServiceImpl` | Exception Swallowing | Medium | Hinders debugging inter-service errors |
| `Report` Entity | Anemic Domain Model / OCP | Medium | Business rules scattered in services |
| `ReportService` | Interface Segregation Principle | Low | Unnecessary dependency linking |
