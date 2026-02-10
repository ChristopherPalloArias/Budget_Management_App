# Architectural Modernization Plan (GoF Patterns)

1. **Creational: Factory Method**
- **Anti-pattern Addressed:** Scattered object construction rules in `ReportServiceImpl.getOrCreateReport` and `TransactionMessageProducer.toMessage` (see `audit.md` item 3).
- **Proposed Solution:** Introduce factory classes (e.g., `ReportFactory` and `TransactionMessageFactory`) that encapsulate default values, period derivation, and mapping rules. Services call the factory methods instead of building objects inline, keeping creation logic in a single place.
- **Architectural Justification:** Centralizing creation logic decouples services from construction details, reinforces SRP by separating "how to build" from "how to use," and reduces maintenance overhead when defaults or mapping rules change. This improves scalability because new consumers can reuse the factories without duplicating mapping logic, and it keeps code paths consistent and testable.
- **Comparison:** The previous structure was **construction-heavy services** because object defaults and mappings were embedded in multiple service methods, but this pattern improves it by **isolating creation logic in dedicated factories** that are reusable and easier to evolve.

2. **Structural: Facade**
- **Anti-pattern Addressed:** Controller-orchestrated integration in `TransactionController#create`, which coordinates persistence and message publishing directly (see `audit.md` item 1).
- **Proposed Solution:** Add a `TransactionWorkflowFacade` that exposes a single `createTransaction` method. The facade delegates to `TransactionService` and `TransactionMessageProducer` internally, so the controller only depends on the facade and focuses on HTTP concerns.
- **Architectural Justification:** A facade decouples the web layer from messaging infrastructure, aligning with SRP and clean architecture boundaries. It simplifies controller logic, reduces coupling, and makes it easier to change messaging or persistence workflows without modifying controller code, improving maintainability and readability.
- **Comparison:** The previous structure was **tightly coupled orchestration** because the controller knew about both persistence and RabbitMQ publishing, but this pattern improves it by **introducing a single facade boundary** that hides integration details and keeps the API layer lean.

3. **Behavioral: Strategy**
- **Anti-pattern Addressed:** Conditional update logic in `ReportServiceImpl.updateReport` driven by `TransactionType` branching (see `audit.md` item 2).
- **Proposed Solution:** Create a `ReportUpdateStrategy` interface with concrete strategies such as `IncomeUpdateStrategy` and `ExpenseUpdateStrategy`. A resolver (e.g., map keyed by `TransactionType`) selects the strategy, and the service delegates update behavior to it.
- **Architectural Justification:** Strategy removes conditional complexity, enforces SRP by isolating each update rule, and allows new transaction types to be added without modifying core service logic. This improves extensibility, reduces risk of regression, and keeps the update flow readable and testable.
- **Comparison:** The previous structure was **branch-heavy service logic** because the service had to evaluate `TransactionType` and mutate totals inline, but this pattern improves it by **encapsulating each update algorithm in a dedicated strategy** selected at runtime.
