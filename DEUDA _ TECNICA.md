# 

## 1. Introduction

This document reflects the **actual** state of the project's technical debt following an exhaustive review of the source code. Unlike previous versions based on obsolete documentation, this report focuses on existing issues in the current implementation of the microservices and the frontend.

**Note:** It has been detected that much of the critical architectural debt reported previously (Smart Controllers, Entities in API) **has already been resolved** in the current version of the code.

---

## 2. Current Technical Debt Summary

| ID           | Title                                         | Type (Fowler Quadrant) | Impact   | Priority |
| :----------- | :-------------------------------------------- | :--------------------- | :------- | :------- |
| **DT-QA-01** | Absence of Significant Unit Tests             | Reckless / Deliberate  | Critical | **P0**   |
| **DT-FE-01** | "God" Component (DataTable)                   | Reckless / Inadvertent | Medium   | **P1**   |
| **DT-FE-02** | Manual Date Utilities (Reinventing Wheel)     | Prudent / Inadvertent  | Low      | **P2**   |
| **DT-BE-01** | Manual Object Mapping (Boilerplate)           | Prudent / Deliberate   | Low      | **P3**   |
| **DT-FE-03** | Hardcoded Values and Styles ("Magic Numbers") | Reckless / Inadvertent | Medium   | **P2**   |

---

## 3. Detail of Active Debts

### 3.1 QA / Testing (Critical)

#### [DT-QA-01] Absence of Significant Unit Tests

- **Code Evidence:**
  - `TransactionApplicationTests.java` only contains a `@SpringBootTest` test that verifies context loading (`contextLoads`).
  - There are no tests for `TransactionServiceImpl` or `TransactionMapper`.
  - There are no component tests in Frontend (`components` or `modules`).
- **Root Cause:** Prioritization of development speed over quality assurance.
- **Impact:**
  - **Fragility:** Any refactoring in `TransactionService` may break business logic without being detected.
  - **False Positives:** The CI pipeline passes "green" but validates nothing functional.
- **Risk:** Regressive bugs in production.
- **Remediation Plan:**
  1.  Create `TransactionServiceTest.java` using **Mockito** and JUnit 5. Test `create` and `getAll` logic.
  2.  Verify that events (`TransactionCreatedEvent`) are published.
  3.  Implement tests for `DataTable` in frontend using React Testing Library.

---

### 3.2 Frontend

#### [DT-FE-01] "God" Component (DataTable.tsx)

- **Code Evidence:**
  - File `app/Frontend/src/modules/transactions/components/DataTable.tsx` (238 lines).
  - Mixes responsibilities:
    - UI Logic (Table rendering).
    - Business Logic (Filtering by `description`, `type`, `category`).
    - State Logic (Manual pagination `pageIndex`, slices).
    - Formatting (Currency, Dates, Category colors).
- **Impact:** Difficult to read, maintain, and reuse. If another table is needed in the future, code will be copied and pasted.
- **Remediation Plan:**
  - Extract logic to `useDataTable` hook (pagination, filters).
  - Extract configuration constants (colors, page size).

#### [DT-FE-02] Manual Date Utilities

- **Code Evidence:**
  - `app/Frontend/src/lib/date-utils.ts` manually implements `format` with arrays of month names in English/mix.
  - Risk of errors in leap years or time zones.
- **Remediation Plan:**
  - Replace with lightweight standard library like `date-fns` or use native `Intl.DateTimeFormat` consistently.

#### [DT-FE-03] Hardcoded Values and Styles

- **Code Evidence:**
  - `pageSize = 10` defined inside the component.
  - Color map `getCategoryColor` hardcoded inside the file.
  - Spanish texts hardcoded in the UI.
- **Remediation Plan:**
  - Move configurations to constants file (`constants.ts` or theme config).

---

### 3.3 Backend

#### [DT-BE-01] Manual Object Mapping

- **Code Evidence:**
  - `TransactionMapper.java` performs field-to-field mapping manually (`entity.setUserId(dto.userId())`, etc.).
  - Although functional, it is boilerplate code that tends to become outdated if fields are added.
- **Remediation Plan:**
  - Migrate to **MapStruct** for automatic and safe mapper generation.

---

## 4. Resolved / Mitigated Debts (History)

Code review confirms that the following previously reported issues **NO LONGER EXIST** or have been mitigated:

- ✅ **[Backend] JPA Entity Exposure:** The code uses `TransactionRequest` and `TransactionResponse` (Records). Controllers return DTOs, not Entities.
- ✅ **[Backend] Smart Controllers:** `TransactionController` delegates to the service. Messaging is handled via events (`TransactionEventListener`), fully decoupling infrastructure.
- ✅ **[Backend] Hardcoded Configuration:** `RabbitMQConfiguration` uses `@Value` to inject properties.
- ✅ **[Backend] Mass Assignment:** Use of Java Records in Request prevents injection of unwanted fields.
- ✅ **[Frontend] Firebase Lock-in:** `useUserStore` uses `authRepository` (interface), decoupling specific Firebase implementation from global store.

---

## 5. Immediate Action Plan

Given that the base architecture is much better than expected, effort should focus 100% on **QA and Frontend Cleanup**.

1.  **Priority 0:** Write unit tests for `TransactionServiceImpl`.
2.  **Priority 1:** Refactor `DataTable.tsx` to remove business logic.
3.  **Priority 2:** Standardize date handling in Frontend.
