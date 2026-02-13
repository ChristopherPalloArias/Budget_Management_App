# Refactorización - Centralización de Constantes y Magic Numbers

## F-05: Magic Numbers y Valores Hardcoded 🟡 MEDIO

### Problema Identificado

Valores distribuidos por todo el código sin control centralizado:

- Números mágicos (pageSize, timeout)
- Endpoints hardcoded
- Configuración de moneda repetida
- Colores de categorías en múltiples lugares

**Evidencia:**

- `DataTable.tsx` → `pageSize: 10`
- `HttpClient.ts` → `timeout: 15000`
- `transactionService.ts` → `/v1/transactions`
- Formato de moneda repetido en múltiples componentes

**Principios vulnerados:**

- **DRY** (Don't Repeat Yourself)
- **Single Source of Truth**
- **Magic Numbers** (evitar valores sin explicación)

---

### Comparación: Antes vs Después

**ANTES (Valores Dispersos)**

```typescript
// DataTable.tsx
const pageSize = 10;

// HttpClient.ts
timeout: 15000,
case 401: ... // magic number

// transactionService.ts
const endpoint = "/v1/transactions";

// TransactionTableRow.tsx
new Intl.NumberFormat("es-CO", {
  style: "currency",
  currency: "COP",
}).format(amount);

// Componente cualquier
const colors = {
  "ALIMENTACION": "bg-blue-100 text-blue-800",
  // ...
}
```

Problemas:

- Cambiar un valor = buscar en múltiples archivos
- Sin tipado - errores solo se detectan en runtime
- Duplicación de lógica

---

### Después (Single Source of Truth)

**1. Constantes Generales**

```typescript
// src/core/constants/app.constants.ts

export const DEFAULT_PAGE_SIZE = 10;
export const DEFAULT_PAGE_INDEX = 0;
export const API_TIMEOUT = 15000;

export const CURRENCY_CONFIG = {
  DEFAULT: { code: 'COP', locale: 'es-CO', currencyDisplay: 'symbol' },
  USD: { code: 'USD', locale: 'en-US', currencyDisplay: 'symbol' },
} as const;

export const API_ENDPOINTS = {
  TRANSACTIONS: '/v1/transactions',
  REPORTS: '/v1/reports',
} as const;

export const HTTP_STATUS = {
  OK: 200,
  UNAUTHORIZED: 401,
  NOT_FOUND: 404,
  // ...
} as const;
```

**2. Constantes de Categorías**

```typescript
// src/core/constants/categories.constants.ts

export const TRANSACTION_CATEGORIES = [
  "ALIMENTACION",
  "TRANSPORTE",
  // ...
] as const;

export type TransactionCategory = (typeof TRANSACTION_CATEGORIES)[number];

export const CATEGORY_COLORS: Record<TransactionCategory, string> = {
  ALIMENTACION: "bg-blue-100 text-blue-800",
  // ...
};

export const getCategoryColor = (category: string): string => { ... };
export const getCategoryLabel = (category: string): string => { ... };
```

**3. Utilidades Reutilizables**

```typescript
// src/shared/utils/currencyUtils.ts

import { CURRENCY_CONFIG } from "@/core/constants/app.constants";

export const formatCurrency = (
  amount: number,
  currencyCode: CurrencyCode = 'DEFAULT'
): string => {
  const config = CURRENCY_CONFIG[currencyCode];
  return new Intl.NumberFormat(config.locale, {
    style: 'currency',
    currency: config.code,
  }).format(amount);
};
```

---

### Archivos Modificados

| Archivo | Cambio |
|---------|--------|
| `src/core/constants/app.constants.ts` | ✅ NUEVO - Constantes generales |
| `src/core/constants/categories.constants.ts` | ✅ NUEVO - Categorías y colores |
| `src/shared/utils/currencyUtils.ts` | ✅ NUEVO - Utilidad de moneda |
| `src/core/api/HttpClient.ts` | ✅ Usa `API_TIMEOUT`, `HTTP_STATUS` |
| `src/modules/transactions/services/transactionService.ts` | ✅ Usa `API_ENDPOINTS` |
| `src/modules/transactions/components/DataTable.tsx` | ✅ Usa `DEFAULT_PAGE_SIZE` |
| `src/modules/transactions/components/TransactionTableRow.tsx` | ✅ Usa `formatCurrency()` |

---

### Estructura Final

```
src/
├── core/
│   └── constants/
│       ├── app.constants.ts        # ✅ Valores generales
│       └── categories.constants.ts # ✅ Categorías
├── shared/
│   ├── hooks/
│   │   └── useDataTableLogic.ts
│   └── utils/
│       └── currencyUtils.ts        # ✅ Formato de moneda
└── modules/
    └── transactions/
        ├── services/
        │   └── transactionService.ts  # ✅ Endpoints centralizados
        └── components/
            ├── DataTable.tsx          # ✅ PAGE_SIZE desde constants
            └── TransactionTableRow.tsx # ✅ formatCurrency()
```

---

### Beneficios

| Aspecto | Antes | Después |
|---------|-------|---------|
| Cambiar timeout | Buscar en N archivos | 1 cambio en `app.constants.ts` |
| Cambiar color de categoría | Buscar en N archivos | 1 cambio en `categories.constants.ts` |
| Agregar moneda | Modificar N componentes | 1 cambio en `CURRENCY_CONFIG` |
| Tipado | Nulo o basic | TypeScript estricto |

---

### Ejemplo de Uso

```typescript
// ❌ ANTES
const timeout = 15000;
const endpoint = "/v1/transactions";
new Intl.NumberFormat("es-CO", { style: "currency", currency: "COP" }).format(amount);

// ✅ DESPUÉS
import { API_TIMEOUT } from "@/core/constants/app.constants";
import { API_ENDPOINTS } from "@/core/constants/app.constants";
import { formatCurrency } from "@/shared/utils/currencyUtils";

const timeout = API_TIMEOUT;
const endpoint = API_ENDPOINTS.TRANSACTIONS;
formatCurrency(amount);
```

---

### .env.example

```bash
# API URLs
VITE_API_TRANSACTIONS_URL=http://localhost:8081/api
VITE_API_REPORTS_URL=http://localhost:8082/api

# Firebase (optional)
VITE_FIREBASE_API_KEY=your_api_key_here
```

---

### Convenciones de Nomenclatura

| Tipo | Formato | Ejemplo |
|------|---------|---------|
| Constantes | `SCREAMING_SNAKE_CASE` | `DEFAULT_PAGE_SIZE` |
| Tipos | `PascalCase` | `TransactionCategory` |
| Utilidades | `camelCase` | `formatCurrency()` |
| Endpoints | `SCREAMING_SNAKE_CASE` | `API_ENDPOINTS.TRANSACTIONS` |
