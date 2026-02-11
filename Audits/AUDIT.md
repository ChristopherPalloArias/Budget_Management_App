# Architecture & Code Quality Report — Budget Management System

**Date:** 2026-02-10  
**Assessment Type:** Technical Audit & Architecture Review  
**Technology Stack:** Spring Boot Microservices + React SPA  
**Scope:** Backend, Frontend, Infrastructure  

---

## Executive Summary

| Category | Critical | High | Medium | Low | Total |
|----------|----------|------|--------|-----|-------|
| **Backend Anti-patterns** | 2 | 0 | 0 | 0 | 2 |
| **Backend Code Smells** | 0 | 1 | 2 | 0 | 3 |
| **Frontend Anti-patterns** | 0 | 3 | 0 | 0 | 3 |
| **Frontend Code Smells** | 0 | 0 | 4 | 1 | 5 |
| **Overall** | **2** | **4** | **6** | **1** | **13** |

---

## Section A: Backend (Microservices)

### 🔴 Architectural Anti-patterns

#### 1. Exposing JPA Entities in REST API
**Severity:** Critical

> [!CAUTION] **Abstraction Leak & Encapsulation Violation**

**Finding:** REST controllers return JPA entities (`Transaction`, `Report`) directly in HTTP responses, violating clean architecture separation between persistence models and API contracts.

**Evidence:**
- TransactionController: returns `ResponseEntity<Transaction>` and full list in `getAll`  
  [app/backend-microservice/transaction/src/main/java/com/microservice/transaction/controller/TransactionController.java](https://github.com/majoymajo/Budget_Management_App/blob/1083c51890b3b9a285dca3ad4059d1f70b09c5e3/app/backend-microservice/transaction/src/main/java/com/microservice/transaction/controller/TransactionController.java#L1-L42)
- ReportController: returns `ResponseEntity<Report>` and lists of `Report`  
  [app/backend-microservice/report/src/main/java/com/microservice/report/controller/ReportController.java](https://github.com/majoymajo/Budget_Management_App/blob/1083c51890b3b9a285dca3ad4059d1f70b09c5e3/app/backend-microservice/report/src/main/java/com/microservice/report/controller/ReportController.java#L1-L40)

**Violated Principles:**
- Clean Architecture: Infrastructure layer leaking into presentation layer
- Encapsulation & Information Hiding
- Separation of Concerns

**Impact:** Fragile API contracts, database schema changes break clients, increased coupling, maintenance overhead

**Proposed Refactor:** Implement DTOs with MapStruct mapping between entities and DTOs. Create separate API model packages to maintain clean separation between persistence and presentation layers.

---

#### 2. Controllers Orchestrating Infrastructure (Smart/Fat Controller)
**Severity:** Critical

> [!CAUTION] **Single Responsibility Principle Violation**

**Finding:** `TransactionController` orchestrates business logic and messaging (RabbitMQ), invoking producers directly and mixing presentation with infrastructure concerns.

**Evidence:**
- `TransactionMessageProducer` used directly from controller  
  [app/backend-microservice/transaction/src/main/java/com/microservice/transaction/infrastructure/TransactionMessageProducer.java](https://github.com/majoymajo/Budget_Management_App/blob/1083c51890b3b9a285dca3ad4059d1f70b09c5e3/app/backend-microservice/transaction/src/main/java/com/microservice/transaction/infrastructure/TransactionMessageProducer.java#L1-L35)

**Violated Principles:**
- SRP (Single Responsibility Principle)
- DIP (Dependency Inversion Principle)

**Impact:** Complex unit tests, tight coupling, reduced flexibility for changing broker/event strategies

**Proposed Refactor:** Extract business logic to service layer and implement event publishing through interfaces. Use dependency injection to decouple from specific message brokers.

---

### 🟡 Code Smells

#### 3. Hardcoded RabbitMQ Configuration
**Severity:** High

**Finding:** Exchange names, queues, and routing keys defined as string literals in code, repeated across microservices.

**Evidence:**
- Configuration in `transaction`:  
  [app/backend-microservice/transaction/src/main/java/com/microservice/transaction/infrastructure/RabbitMQConfiguration.java](https://github.com/majoymajo/Budget_Management_App/blob/1083c51890b3b9a285dca3ad4059d1f70b09c5e3/app/backend-microservice/transaction/src/main/java/com/microservice/transaction/infrastructure/RabbitMQConfiguration.java#L1-L61)
- Configuration in `report`:  
  [app/backend-microservice/report/src/main/java/com/microservice/report/infrastructure/RabbitMQConfiguration.java](https://github.com/majoymajo/Budget_Management_App/blob/1083c51890b3b9a285dca3ad4059d1f70b09c5e3/app/backend-microservice/report/src/main/java/com/microservice/report/infrastructure/RabbitMQConfiguration.java#L1-L61)

**Violated Principles:**
- Twelve-Factor App (Configuration in environment)
- DRY (Don't Repeat Yourself)

**Impact:** Requires recompilation for configuration changes, risk of inconsistencies, operational complexity

**Proposed Refactor:** Externalize configuration to `application.yml` with environment-specific profiles. Use Spring Cloud Config or environment variables for deployment flexibility.

---

#### 4. Hardcoded CORS Configuration
**Severity:** Medium

**Finding:** Allowed origins defined in code with `allowedOrigins("http://localhost:3000")`.

**Evidence:**
- ReportApplication:  
  [app/backend-microservice/report/src/main/java/com/microservice/report/ReportApplication.java](https://github.com/majoymajo/Budget_Management_App/blob/1083c51890b3b9a285dca3ad4059d1f70b09c5e3/app/backend-microservice/report/src/main/java/com/microservice/report/ReportApplication.java#L1-L28)
- TransactionApplication:  
  [app/backend-microservice/transaction/src/main/java/com/microservice/transaction/TransactionApplication.java](https://github.com/majoymajo/Budget_Management_App/blob/1083c51890b3b9a285dca3ad4059d1f70b09c5e3/app/backend-microservice/transaction/src/main/java/com/microservice/transaction/TransactionApplication.java#L1-L27)

**Violated Principles:**
- Twelve-Factor App (Configuration in environment)

**Impact:** Difficult multi-environment support, deployment required for origin changes

**Proposed Refactor:** Move CORS configuration to `application.yml` with environment-specific profiles. Use `${CORS_ALLOWED_ORIGINS}` environment variable for flexibility.

---

#### 5. List Endpoints Without Pagination
**Severity:** Medium

**Finding:** `getAll()` returns complete list of transactions without pagination or filtering.

**Evidence:**
- TransactionController `getAll`:  
  [app/backend-microservice/transaction/src/main/java/com/microservice/transaction/controller/TransactionController.java](https://github.com/majoymajo/Budget_Management_App/blob/1083c51890b3b9a285dca3ad4059d1f70b09c5e3/app/backend-microservice/transaction/src/main/java/com/microservice/transaction/controller/TransactionController.java#L1-L42)

**Violated Principles:**
- API design best practices for scalability

**Impact:** Performance/memory/network issues with growing datasets, poor client control

**Proposed Refactor:** Implement Spring Data pagination with `Pageable` interface. Add sorting, filtering, and page size limits for resource protection.

---

#### 6. Inconsistent Error Handling
**Severity:** Low

**Finding:** No centralized `@ControllerAdvice` for standardized exception mapping.

**Violated Principles:**
- Robust API contract design
- Observability

**Proposed Refactor:** Implement global `@ControllerAdvice` with standardized error responses and HTTP status codes.

---

## Section B: Frontend (React SPA)

### 🟡 Architectural Anti-patterns

#### 7. Firebase Vendor Lock-in
**Severity:** High

> [!WARNING] **Infrastructure Coupling in Domain Layer**

**Finding:** User store and authentication services import Firebase Auth SDK directly, creating tight coupling to vendor-specific infrastructure.

**Evidence:**
- Store:  
  [app/Frontend/src/modules/auth/store/useUserStore.ts](https://github.com/majoymajo/Budget_Management_App/blob/1083c51890b3b9a285dca3ad4059d1f70b09c5e3/app/Frontend/src/modules/auth/store/useUserStore.ts#L1-L67)
- Configuration:  
  [app/Frontend/src/core/config/firebase.config.ts](https://github.com/majoymajo/Budget_Management_App/blob/1083c51890b3b9a285dca3ad4059d1f70b09c5e3/app/Frontend/src/core/config/firebase.config.ts#L1-L15)
- Service:  
  [app/Frontend/src/modules/auth/services/authService.ts](https://github.com/majoymajo/Budget_Management_App/blob/1083c51890b3b9a285dca3ad4059d1f70b09c5e3/app/Frontend/src/modules/auth/services/authService.ts#L1-L103)

**Violated Principles:**
- DIP (Dependency Inversion Principle)
- Clean Architecture (Infrastructure leaking into state layer)

**Impact:** Difficult migration, reduced testability, domain/UI dependence on provider

**Proposed Refactor:** Create authentication abstraction interfaces and dependency injection. Implement adapter pattern for Firebase, with easy swap capability for other providers.

---

#### 8. Side Effects on Store Load
**Severity:** High

> [!WARNING] **Unpredictable Initialization Behavior**

**Finding:** `useUserStore.getState().initAuthListener()` executes on module load, triggering side effects during import.

**Evidence:**
- End of file `useUserStore.ts`  
  [app/Frontend/src/modules/auth/store/useUserStore.ts](https://github.com/majoymajo/Budget_Management_App/blob/1083c51890b3b9a285dca3ad4059d1f70b09c5e3/app/Frontend/src/modules/auth/store/useUserStore.ts#L1-L67)

**Violated Principles:**
- Explicit effect control (Clean Code/Architecture)
- Predictable initialization

**Impact:** SSR/test complications, lifecycle management issues, error risks

**Proposed Refactor:** Move initialization to explicit app bootstrap or React component lifecycle. Use custom hooks for controlled effect management.

---

### 🟢 Code Smells

#### 9. Reinventing Date Utilities
**Severity:** Medium

**Finding:** Manual date formatting and month names using string arrays instead of established libraries.

**Evidence:**
- `date-utils.ts`:  
  [app/Frontend/src/lib/date-utils.ts](https://github.com/majoymajo/Budget_Management_App/blob/1083c51890b3b9a285dca3ad4059d1f70b09c5e3/app/Frontend/src/lib/date-utils.ts#L1-L50)

**Violated Principles:**
- DRY/KISS
- "Don't Reinvent the Wheel"

**Impact:** Larger error surface, manual maintenance costs, internationalization difficulties

**Proposed Refactor:** Replace with established libraries like `date-fns` or `dayjs` for comprehensive date handling and i18n support.

---

#### 10. Inline Components and Hardcoded Presentation
**Severity:** Medium

**Finding:** Page components defined inline in router and category color maps embedded in components.

**Evidence:**
- Router with inline components:  
  [app/Frontend/src/core/router/AppRouter.tsx](https://github.com/majoymajo/Budget_Management_App/blob/1083c51890b3b9a285dca3ad4059d1f70b09c5e3/app/Frontend/src/core/router/AppRouter.tsx#L1-L81)
- Hardcoded colors:  
  [app/Frontend/src/modules/transactions/components/DataTable.tsx](https://github.com/majoymajo/Budget_Management_App/blob/1083c51890b3b9a285dca3ad4059d1f70b09c5e3/app/Frontend/src/modules/transactions/components/DataTable.tsx#L1-L93)

**Violated Principles:**
- Separation of Concerns
- Theming/Centralized Configuration

**Impact:** Poor reusability, theming difficulties, frequent deployment requirements

**Proposed Refactor:** Extract components to separate files, implement theme system with CSS variables or styled-components, centralize configuration in dedicated theme files.

---

#### 11. God Components
**Severity:** Medium

**Finding:** `DataTable.tsx` (238 lines) handles multiple responsibilities: state management (pagination, filters, search), formatting/filtering logic, and complex UI rendering.

**Evidence:**
- Component size and multiple concerns in single file  
  [app/Frontend/src/modules/transactions/components/DataTable.tsx](https://github.com/majoymajo/Budget_Management_App/blob/1083c51890b3b9a285dca3ad4059d1f70b09c5e3/app/Frontend/src/modules/transactions/components/DataTable.tsx#L1-L238)

**Violated Principles:**
- SRP (Single Responsibility Principle)
- Component Composition

**Impact:** Difficult testing, reduced maintainability, tight coupling

**Proposed Refactor:** Extract pagination logic to custom hooks (`usePagination`), filtering to separate utility functions, and UI sub-components to dedicated files.

---

#### 12. Hardcoded Values & Magic Numbers
**Severity:** Medium

**Finding:** Multiple hardcoded values across components and services without configuration.

**Evidence:**
- Currency formatting logic: DataTable.tsx:15-20
- Category color mapping: DataTable.tsx:22-36  
- Date logic: useTransactionStore.ts:18
- Page size magic number: DataTable.tsx:48 (`pageSize = 10`)
- Endpoint strings: transactionService.ts:10
- Timeout value: HttpClient.ts:16 (`15000ms`)

**Violated Principles:**
- Configuration Management
- Magic Number Elimination

**Impact:** Maintenance difficulties, no runtime configurability, inconsistent behavior

**Proposed Refactor:** Move hardcoded values to configuration files/environment variables. Create constants files for magic numbers. Use environment-specific config for API endpoints and timeouts.

---

### 🔴 Additional Architectural Anti-patterns

#### 13. Fragmented Logic
**Severity:** High

> [!WARNING] **Business Logic Scattered Across Components**

**Finding:** Business logic mixed with rendering in multiple components, creating fragmented and duplicated logic.

**Evidence:**
- TransactionPage.tsx:29: Business logic mixed with conditional rendering
- LoginForm.tsx:37-56: Error handling with `any` types and duplicated logic

**Violated Principles:**
- Separation of Concerns
- Single Responsibility Principle

**Impact:** Reduced reusability, testing complexity, maintenance overhead

**Proposed Refactor:** Extract business logic to custom hooks or service functions. Implement proper error handling with typed interfaces. Separate rendering from business concerns.

---

#### 14. Props Inconsistency & Type Safety Issues
**Severity:** High

**Finding:** Inconsistent prop typing and use of `any` types, reducing type safety.

**Evidence:**
- createTransactionAdapter.ts:18: Returns `any` instead of specific type
- useTransactions.ts:35: Error parameter typed as `any` in error handler

**Violated Principles:**
- Type Safety
- Interface Consistency

**Impact:** Reduced compile-time safety, runtime errors, poor IDE support

**Proposed Refactor:** Implement proper TypeScript interfaces for all props and return types. Remove `any` types and create specific error types for better error handling.

---

#### 15. Missing Error Boundaries
**Severity:** Medium

**Finding:** Absence of Error Boundaries for centralized error handling, leading to replicated error management across components.

**Evidence:**
- Error handling logic duplicated in multiple components
- No centralized error boundary implementation

**Violated Principles:**
- Error Handling Best Practices
- DRY (Don't Repeat Yourself)

**Impact:** Inconsistent user experience, debugging difficulties, code duplication

**Proposed Refactor:** Implement React Error Boundaries at route and component levels. Create centralized error reporting service. Standardize error fallback components.

---

#### 16. Duplicated Types/Models
**Severity:** Low

**Finding:** Multiple modules define similar types without shared domain consolidation.

**Evidence:**
- Types in `transactions` and `shared` with separate maps/adapters  
  [app/Frontend/src/modules/transactions/types/transaction.types.ts](https://github.com/majoymajo/Budget_Management_App/blob/1083c51890b3b9a285dca3ad4059d1f70b09c5e3/app/Frontend/src/modules/transactions/types/transaction.types.ts#L1-L21)  
  [app/Frontend/src/shared/types/index.ts](https://github.com/majoymajo/Budget_Management_App/blob/1083c51890b3b9a285dca3ad4059d1f70b09c5e3/app/Frontend/src/shared/types/index.ts#L1-L49)

**Violated Principles:**
- DRY
- Domain-Driven Design

**Proposed Refactor:** Consolidate types in `src/shared/domain` package with clear domain boundaries and shared adapters.

---

## Additional Technical Observations

### Spring Boot Version Concern
**Finding:** `spring-boot-starter-parent` declared as version `4.0.2` in `pom.xml`, which appears misaligned with the stable 3.x line.

**Evidence:**
- [app/backend-microservice/report/pom.xml](https://github.com/majoymajo/Budget_Management_App/blob/1083c51890b3b9a285dca3ad4059d1f70b09c5e3/app/backend-microservice/report/pom.xml#L1-L25)  
- [app/backend-microservice/transaction/pom.xml](https://github.com/majoymajo/Budget_Management_App/blob/1083c51890b3b9a285dca3ad4059d1f70b09c5e3/app/backend-microservice/transaction/pom.xml#L1-L25)

**Recommendation:** Validate version compatibility and consider alignment with stable Spring Boot 3.x for better support and ecosystem compatibility.

---

## Recommended Implementation Priority

| Priority | Findings | Business Impact |
|----------|----------|-----------------|
| **P0** | Backend Critical Anti-patterns (1-2) | API stability, system maintainability |
| **P1** | Frontend High Anti-patterns (7-8, 13-14) | Testability, vendor flexibility, type safety |
| **P2** | Backend High Code Smells (3) | Operational efficiency |
| **P3** | Medium Code Smells (4-5, 9-12, 15) | Code quality, development velocity |
| **P4** | Low Code Smells (6, 16) | Long-term maintainability |

---

**Report Generated:** 2026-02-10  
**Assessment Framework:** SOLID Principles, Clean Architecture, 12-Factor App, Domain-Driven Design