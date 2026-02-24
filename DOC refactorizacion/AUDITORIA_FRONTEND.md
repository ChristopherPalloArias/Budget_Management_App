# Frontend Architecture & Codebase Audit Report

## 1. Architectural Overview
- **Pattern in Use:** Feature-based architecture (Feature-Sliced Design variant). The codebase is structured into `modules` (`auth`, `transactions`, `reports`), layered with `core`, `shared`, and UI `components`.
- **Folder Structure vs. Scalable Architecture:** The structure is excellent and highly scalable. The usage of `adapters`, `services`, `store`, and `hooks` inside each module cleanly maps external data structures to internal frontend models.
- **Separation of Concerns:** 
  - Data fetching is well-abstracted using React Query inside custom hooks (`useTransactions`, `useGetReportsSummary`).
  - However, page-level component logic (`TransactionPage`, `ReportsPage`) remains bloated with inline loading states and heavy state orchestration.

## 2. SOLID Principles Applied to Frontend
- **SRP (Single Responsibility Principle):**
  - *Violation:* `ReportsPage.tsx` fetches data via React Query, orchestrates filter state, and renders ~40 lines of inline raw Skeleton JSX. It should delegate loading UI to a dedicated `<ReportsPageSkeleton />`.
  - *Violation:* `useTransactionPage.ts` acts as a "God Hook" for `TransactionPage`. It manages state and handlers for the Create Dialog, Edit Dialog, and Delete Dialog simultaneously, returning 18 items.
- **LSP (Liskov Substitution Principle) / Reusability:**
  - *Violation:* Inline Skeleton blocks in Pages cannot be substituted or reused.
- **DIP (Dependency Inversion Principle):**
  - *Violation:* `HttpClient.ts` (Core API) directly implements `localStorage.getItem('auth_token')`. An abstraction layer (e.g., a `TokenStorageService`) should be injected into the Interceptors to decouple the client from browser-exclusive Web APIs.

## 3. Component Design Smells
- **God Hooks:** `useTransactionPage.ts` handles too much page-level state. Modals should ideally manage their own open/close local state or use URL-based routing (e.g., `?edit=123`) to avoid rendering bottlenecks.
- **Missing Component Abstraction:** Copy-pasted JSX blocks for Skeletons exist directly inside `TransactionPage.tsx` and `ReportsPage.tsx`.
- **Prop Drilling Risk:** `TransactionPage.tsx` passes multiple open/close modal dispatchers explicitly down to `<DataTable />`. Moving context or composing children would reduce this tight coupling.
- **Inconsistent/Bloated File Sizes:** While logical, page files grow rapidly because supplementary inner-functions (`TransactionPageSkeleton`, `TransactionPageError`) are kept in the same file rather than isolated.

## 4. State Management Analysis
- **Approach:** Zustand (Global Client State) + TanStack React Query (Server State). This is an industry standard and highly effective.
- **Local State Leaking to Global (Store Misuse):**
  - *Violation:* `useTransactionStore.ts` stores `currentTransaction` (the transaction being edited) globally. 
  - *Symptom:* The store explicitly notes: `reset: () => ... Debe invocarse durante el logout para evitar phantom data.`
  - *Assessment:* The state of a form modal is strictly *local UI state*. Putting it in Zustand causes "phantom data" risks and unnecessary global re-renders. Edit states should be handled locally inside the `<TransactionForm />` or a local context provider.

## 5. Design Patterns & Frontend Architecture
- **Patterns in Use:**
  - *Container/Presentational:* Mostly adhered to (Custom Hooks mapping to Page components).
  - *Adapter Pattern:* Excellent implementation (`transactionAdapter.ts`) to isolate backend JSON contracts from frontend UI types.
- **Anti-patterns:** Direct DOM/Browser API manipulation (`localStorage`) inside generic utility classes like `HttpClient.ts`.

## 6. Performance Concerns
- **Unnecessary Re-renders:** Opening or closing *any* modal in `TransactionPage.tsx` (via `useTransactionPage`) causes the entire page, including the `<DataTable />`, to re-render because the state lives at the root page level.
- **Bundle Optimization:** `TransactionForm.tsx` properly uses `react-hook-form` with `zodResolver`, which isolates form re-renders effectively.

## 7. Accessibility & UX Quality
- **UX Quality:** Outstanding perceived performance usage with React Query caching (`staleTime: 1000 * 60 * 5`) and immediate UI feedback via `sonner` toasts.
- **Accessibility:** By utilizing `radix-ui` and standard HTML5 inputs in the highly controlled `<TransactionForm />`, ARIA labels and keyboard navigation are mostly reliable.

## 8. Testability
- Hardcoded `localStorage` in `HttpClient.ts` makes it difficult to mock during Jest isolated unit tests without patching global Node environments.
- Pages like `ReportsPage.tsx` are harder to unit test due to the tightly coupled inline skeleton logic intertwined with the `useGetReportsSummary` hook mock.

## 9. Per-Finding Documentation

| Affected Component | Violated Principle / Smell | Severity | Impact | Suggested Direction |
| :--- | :--- | :--- | :--- | :--- |
| `useTransactionStore.ts` | **Store Misuse** (Local state in Global Store) | High | Phantom data risk; unexpected cross-component rerenders. | Move `currentTransaction` editing state perfectly into React local state or a local Provider. |
| `HttpClient.ts` | **DIP** (Coupling to localStorage) | Medium | Breaks SSR (if ever adopted) and complicates Jest unit tests. | Inject a `TokenProvider` or `StorageAdapter` into the axios instance setup. |
| `useTransactionPage.ts` | **God Hook / God Component** | Medium | Changing a modal's state forces the whole table to re-render. | Break down modal states. Use URL queries (`?modal=edit&id=1`) or extract a `<TransactionModalsContainer />`. |
| `ReportsPage.tsx` | **SRP / Missing Abstraction** | Low | UI Bloat. Decreases code readability. | Extract Skeleton inline JSX to a `<ReportsPageSkeleton />`. |

## 10. Summary Table

| Component | Violation / Smell | Severity | Impact |
| :--- | :--- | :--- | :--- |
| `useTransactionStore.ts` | State Leakage (Globalizing Local State) | High | Memory/Data leaks, phantom data. |
| `useTransactionPage.ts` | God Hook (Too much orchestration) | Medium | Root-level re-render bottlenecks. |
| `HttpClient.ts` | Direct Browser API Coupling | Medium | Difficult testing, SSR incompatibility. |
| `ReportsPage.tsx` | SRP / Copy-pasted Skeletons | Low | Component bloated by layout markup. |
| `TransactionPage.tsx` | Prop drilling modal handlers | Low | Tight coupling with `DataTable`. |
