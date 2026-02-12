# Frontend Best Practices Audit — Budget Management App

- Modular, domain-driven frontend structure: `src/core`, `src/modules`, `src/shared`, `src/components/ui` (DDD-inspired organization).
- Clear separation of server state (TanStack Query) vs client UI state (Zustand) and adapters for Backend → UI mapping.
- Strict TypeScript usage with domain types and adapters; emphasis on single source of truth for UI models.
- UI system with shadcn/ui and Tailwind CSS promoting consistency and reusable components.
- Routing layouts like `PublicLayout` and a structured `AppSidebar` component.
- Documentation-first approach: Architecture, setup, and task prompts guiding quality standards (type-safety, responsibility segregation).
- CI/CD (linting, tests, build) noted in the root README with Vitest + React Testing Library.
---

## 1. Code Structure & Readability

Recommendations:
- Component organization: Co-locate UI components, hooks, services, adapters, and types in each module (`transactions`, `reports`, `auth`), and export clean barrels per domain.
- Naming conventions: Use PascalCase for components, camelCase for functions/variables, and consistent suffixes (`*.adapter.ts`, `*.service.ts`, `*.types.ts`, `*.hook.ts`).
- Separation of concerns: 
  - UI components should be stateless/presentational when possible.
  - Hooks encapsulate data fetching and business logic.
  - Adapters map API responses to UI-safe models.
  - Services isolate HTTP calls.

Example directory structure reminder (already aligned with repo guidance):
```text
src/
  core/
    api/
    config/
    router/
    store/
  modules/
    transactions/
      adapters/
      components/
      hooks/
      services/
      types/
    reports/
      adapters/
      components/
      hooks/
      services/
      types/
  shared/
    layouts/
    types/
    utils/
  components/
    ui/
  hooks/
```

Example: Barrel export for a module to keep imports clean
```typescript name=src/modules/transactions/index.ts
export * from "./components";
export * from "./hooks";
export * from "./services";
export * from "./adapters";
export * from "./types";
```

Adapter pattern for clean UI models
```typescript name=src/modules/transactions/adapters/transaction.adapter.ts
import type { TransactionResponse, TransactionModel } from "../types/transaction.types";

export const transactionAdapter = (response: TransactionResponse): TransactionModel => ({
  id: response.transaction_id,
  date: new Date(response.date_iso),
  amount: Number(response.amount ?? 0),
  category: response.category ?? "uncategorized",
  description: response.description ?? "",
});
```

Presentational component vs. logic hook
```tsx name=src/modules/transactions/components/TransactionRow.tsx
import React from "react";
import type { TransactionModel } from "../types/transaction.types";

export const TransactionRow: React.FC<{ tx: TransactionModel }> = ({ tx }) => (
  <tr>
    <td>{tx.date.toLocaleDateString()}</td>
    <td className="text-right">{tx.amount.toFixed(2)}</td>
    <td>{tx.category}</td>
    <td>{tx.description}</td>
  </tr>
);
```

---

## 2. Performance

Recommendations:
- Prevent unnecessary re-renders with `React.memo`, `useMemo`, `useCallback`.
- Use Zustand selectors with shallow comparison.
- Leverage TanStack Query’s `select`, caching, and `staleTime`.
- Prefer lazy loading for routes and heavy components.
- Consider virtualization for large tables/lists.

React.memo and memoized derived values
```tsx name=src/modules/transactions/components/TransactionsTable.tsx
import React, { useMemo } from "react";
import { TransactionRow } from "./TransactionRow";
import type { TransactionModel } from "../types/transaction.types";

type Props = { data: TransactionModel[]; filterText: string };

export const TransactionsTable = React.memo(({ data, filterText }: Props) => {
  const filtered = useMemo(() => {
    const term = filterText.trim().toLowerCase();
    if (!term) return data;
    return data.filter((d) =>
      [d.category, d.description].some((v) => v.toLowerCase().includes(term))
    );
  }, [data, filterText]);

  return (
    <table className="min-w-full">
      <tbody>{filtered.map((tx) => <TransactionRow key={tx.id} tx={tx} />)}</tbody>
    </table>
  );
});
```

Zustand selector to minimize re-renders
```typescript name=src/modules/auth/store/useUserStore.ts
import { create } from "zustand";
import { shallow } from "zustand/shallow";

type UserState = { userId?: string; displayName?: string; setUser: (u: Partial<UserState>) => void };

export const useUserStore = create<UserState>((set) => ({
  userId: undefined,
  displayName: undefined,
  setUser: (u) => set(u),
}));

// Usage with selector + shallow
// const { userId, displayName } = useUserStore((s) => ({ userId: s.userId, displayName: s.displayName }), shallow);
```

TanStack Query: `select`, caching, and partial updates
```typescript name=src/modules/reports/hooks/useReports.ts
import { useQuery } from "@tanstack/react-query";
import { getReports } from "../services/reportService";
import { reportListAdapter } from "../adapters/report.adapter";

export const useReports = (filters: { period?: string; start?: string; end?: string }) => {
  return useQuery({
    queryKey: ["reports", filters],
    queryFn: () => getReports(filters),
    select: (resp) => reportListAdapter(resp.data),
    staleTime: 60_000, // 1 minute
    gcTime: 10 * 60_000, // 10 minutes
    retry: 1,
  });
};
```

Lazy-loaded route/component
```tsx name=src/core/router/AppRouter.tsx
import { Suspense, lazy } from "react";
import { createBrowserRouter, RouterProvider } from "react-router-dom";
import { PublicLayout } from "@/shared/layouts/PublicLayout";

const TransactionsPage = lazy(() => import("@/modules/transactions/pages/TransactionsPage"));
const ReportsPage = lazy(() => import("@/modules/reports/pages/ReportsPage"));

const router = createBrowserRouter([
  {
    element: <PublicLayout />,
    children: [
      { path: "/transactions", element: <Suspense fallback={<div>Loading...</div>}><TransactionsPage /></Suspense> },
      { path: "/dashboard", element: <Suspense fallback={<div>Loading...</div>}><ReportsPage /></Suspense> },
    ],
  },
]);

export const AppRouter = () => <RouterProvider router={router} />;
```

---

## 3. Maintainability

Recommendations:
- Reusable components in `src/components/ui` and `src/shared`.
- Document public hooks/components with JSDoc/TS doc comments.
- Keep adapters/services testable and pure; avoid side effects.
- Align with patterns:
  - Adapter (already used)
  - Repository/Service abstraction for HTTP
  - Observer pattern implicit via hooks & query subscriptions

Reusable UI pattern
```tsx name=src/shared/components/PageHeader.tsx
import React from "react";

export const PageHeader: React.FC<{ title: string; rightSlot?: React.ReactNode }> = ({ title, rightSlot }) => (
  <div className="flex items-center justify-between border-b pb-4 mb-6">
    <h1 className="text-xl font-semibold">{title}</h1>
    <div>{rightSlot}</div>
  </div>
);
```

Service abstraction (Repository pattern for HTTP)
```typescript name=src/modules/transactions/services/transactionService.ts
import { httpClient } from "@/core/api/httpClient";
import type { TransactionResponse } from "../types/transaction.types";

export const TransactionService = {
  async list(params?: Record<string, string>) {
    return httpClient.get<{ data: TransactionResponse[] }>("/transactions", { params });
  },
  async getById(id: string) {
    return httpClient.get<{ data: TransactionResponse }>(`/transactions/${id}`);
  },
  // mutation methods implemented separately when needed
};
```

Doc comments on hooks
```typescript name=src/modules/transactions/hooks/useTransactions.ts
/**
 * Returns the adapted list of transactions for the UI, given optional filters.
 * Internally uses TanStack Query for caching and fetching, and adapters for type-safe models.
 */
export const useTransactions = (filters?: { q?: string }) => {
  /* ... */
};
```

---

## 4. Testing & Quality

Recommendations:
- Unit tests for adapters (pure functions) and services.
- Integration tests for hooks + components with React Testing Library.
- Maintain strict ESLint + TypeScript rules; consider SonarQube for static analysis.

Adapter unit test (Vitest)
```typescript name=src/modules/transactions/adapters/transaction.adapter.spec.ts
import { describe, it, expect } from "vitest";
import { transactionAdapter } from "./transaction.adapter";

describe("transactionAdapter", () => {
  it("maps API response to UI model safely", () => {
    const api = { transaction_id: "1", date_iso: "2024-12-22T00:00:00Z", amount: "10.50", category: "food", description: "Lunch" };
    const ui = transactionAdapter(api as any);
    expect(ui.id).toBe("1");
    expect(ui.date).toBeInstanceOf(Date);
    expect(ui.amount).toBe(10.5);
    expect(ui.category).toBe("food");
    expect(ui.description).toBe("Lunch");
  });
});
```

Component integration test (React Testing Library)
```tsx name=src/modules/auth/components/LoginForm.spec.tsx
import { render, screen, fireEvent } from "@testing-library/react";
import { describe, it, expect, vi } from "vitest";
import { LoginForm } from "./LoginForm";

describe("LoginForm", () => {
  it("submits when email and password are provided", async () => {
    const onSubmit = vi.fn();
    render(<LoginForm onSubmit={onSubmit} />);
    fireEvent.change(screen.getByLabelText(/email/i), { target: { value: "user@example.com" } });
    fireEvent.change(screen.getByLabelText(/password/i), { target: { value: "securePass123" } });
    fireEvent.click(screen.getByRole("button", { name: /sign in/i }));
    expect(onSubmit).toHaveBeenCalled();
  });
});
```

ESLint strict config baseline
```json name=.eslintrc.json
{
  "root": true,
  "env": { "browser": true, "es2022": true, "node": true },
  "parser": "@typescript-eslint/parser",
  "plugins": ["@typescript-eslint", "react", "react-hooks", "jsx-a11y"],
  "extends": [
    "eslint:recommended",
    "plugin:@typescript-eslint/strict",
    "plugin:react/recommended",
    "plugin:react-hooks/recommended",
    "plugin:jsx-a11y/recommended",
    "prettier"
  ],
  "settings": { "react": { "version": "detect" } },
  "rules": {
    "react/jsx-no-useless-fragment": "warn",
    "react-hooks/exhaustive-deps": "warn",
    "@typescript-eslint/consistent-type-imports": "error",
    "@typescript-eslint/no-explicit-any": "warn"
  }
}
```

---

## 5. Accessibility & UX

Recommendations:
- Use semantic HTML tags (`header`, `main`, `nav`, `section`).
- Ensure keyboard navigation and focus states are correct; respect ARIA roles where needed.
- Provide labels for form inputs; ensure color contrast meets WCAG.
- Announce dynamic content changes where appropriate (live regions).

Semantic layout and skip link
```tsx name=src/shared/layouts/AppLayout.tsx
export const AppLayout: React.FC<{ children: React.ReactNode }> = ({ children }) => (
  <>
    <a href="#main" className="sr-only focus:not-sr-only">Skip to content</a>
    <header role="banner" className="border-b p-4">Budget Management</header>
    <nav aria-label="Primary" className="border-r p-4">/* sidebar */</nav>
    <main id="main" role="main" className="p-6">{children}</main>
  </>
);
```

Accessible dropdown/menu patterns (role/aria, keyboard)
```tsx name=src/components/ui/AccessibleMenu.tsx
import React from "react";

export const AccessibleMenu: React.FC<{ label: string; items: { label: string; onSelect: () => void }[] }> = ({ label, items }) => (
  <div role="menu" aria-label={label}>
    <button aria-haspopup="menu" aria-expanded="false" className="btn">Open</button>
    <ul role="menu" className="mt-2 border rounded shadow">
      {items.map((item, i) => (
        <li role="menuitem" tabIndex={0} key={i} onKeyDown={(e) => e.key === "Enter" && item.onSelect()} onClick={item.onSelect} className="px-3 py-2 cursor-pointer focus:outline focus:outline-2 focus:outline-blue-600">
          {item.label}
        </li>
      ))}
    </ul>
  </div>
);
```

Form labels and descriptions
```tsx name=src/shared/components/AccessibleInput.tsx
export const AccessibleInput: React.FC<{ id: string; label: string; description?: string; type?: string }> = ({ id, label, description, type = "text" }) => (
  <div>
    <label htmlFor={id} className="block text-sm font-medium">{label}</label>
    {description && <p id={`${id}-desc`} className="text-xs text-muted-foreground">{description}</p>}
    <input id={id} type={type} aria-describedby={description ? `${id}-desc` : undefined} className="mt-1 block w-full border rounded px-3 py-2" />
  </div>
);
```

---

## 6. Security

Recommendations:
- Input validation and sanitization (Zod for schema validation; DOMPurify for HTML content).
- Avoid rendering untrusted HTML via `dangerouslySetInnerHTML` unless sanitized.
- Token handling: store auth tokens securely; prefer HTTPS; set `SameSite` cookies when applicable; avoid localStorage for sensitive data if possible.
- Avoid common vulnerabilities: XSS (sanitize), CSRF (tokens for same-site or double-submit; consider Firebase auth specifics), SSRF/injection (never interpolate untrusted into queries/URLs without validation).

Zod validation for forms
```typescript name=src/shared/validation/auth.ts
import { z } from "zod";

export const loginSchema = z.object({
  email: z.string().email("Invalid email"),
  password: z.string().min(8, "Minimum 8 characters"),
});
export type LoginInput = z.infer<typeof loginSchema>;
```

Controlled submit with sanitization
```tsx name=src/modules/auth/components/LoginForm.tsx
import React from "react";
import DOMPurify from "dompurify";
import { loginSchema } from "@/shared/validation/auth";

export const LoginForm: React.FC<{ onSubmit?: (input: { email: string; password: string }) => void }> = ({ onSubmit }) => {
  const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    const form = new FormData(e.currentTarget);
    const rawEmail = String(form.get("email") ?? "");
    const rawPass = String(form.get("password") ?? "");
    // sanitize strings (defense-in-depth; input fields typically not HTML, but sanitize anyway)
    const email = DOMPurify.sanitize(rawEmail);
    const password = DOMPurify.sanitize(rawPass);
    const parsed = loginSchema.safeParse({ email, password });
    if (!parsed.success) {
      // surface validation errors
      return;
    }
    onSubmit?.(parsed.data);
  };

  return (
    <form onSubmit={handleSubmit} noValidate>
      {/* inputs with labels */}
      <button type="submit" className="btn-primary">Sign in</button>
    </form>
  );
};
```

Axios interceptor for auth headers
```typescript name=src/core/api/httpClient.ts
import axios from "axios";

export const httpClient = axios.create({
  baseURL: import.meta.env.VITE_API_URL,
  timeout: 15_000,
});

httpClient.interceptors.request.use((config) => {
  const token = sessionStorage.getItem("auth_token"); // prefer HTTP-only cookies if using backend sessions
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

httpClient.interceptors.response.use(
  (res) => res,
  (err) => {
    // consider centralized logging
    return Promise.reject(err);
  }
);
```


---
