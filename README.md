# 🧭 Budget Management App
Take control of your money with a fast, modern, and delightful budgeting experience.

[![Build](https://github.com/majoymajo/Budget_Management_App/actions/workflows/ci.yml/badge.svg)](https://github.com/majoymajo/Budget_Management_App/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](#license)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.9-3178C6?logo=typescript&logoColor=white)](https://www.typescriptlang.org/)
[![React](https://img.shields.io/badge/React-19-61DAFB?logo=react&logoColor=white)](https://react.dev/)
[![Vite](https://img.shields.io/badge/Vite-7-646CFF?logo=vite&logoColor=white)](https://vite.dev/)
[![Tailwind CSS](https://img.shields.io/badge/Tailwind-4-06B6D4?logo=tailwindcss&logoColor=white)](https://tailwindcss.com/)
[![Vitest](https://img.shields.io/badge/Tests-Vitest-6E9F18?logo=vitest&logoColor=white)](https://vitest.dev/)

> A modern budget tracking app built with React + TypeScript, designed to simplify your financial life. Track income, categorize expenses, visualize trends, and stay on top of your goals with a sleek UI and smooth developer experience.

---

## Table of Contents
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Installation](#installation)
  - [Environment Setup](#environment-setup)
  - [Run Locally](#run-locally)
- [Usage](#usage)
- [Available Scripts](#available-scripts)
- [Testing](#testing)
- [Contributing](#contributing)
- [Roadmap](#roadmap)
- [Acknowledgments](#acknowledgments)
- [License](#license)
- [Contact](#contact)

---

## Features
- Clean, responsive UI powered by Tailwind CSS
- Dark mode support out of the box
- Category-based income and expense tracking
- Fast client-side data syncing with TanStack Query
- Keyboard-friendly UX with accessible components (Radix UI)
- Local mock API for development via JSON Server
- Firebase-ready for authentication and cloud persistence
- Type-safe forms and validation with React Hook Form + Zod
- Modular architecture with reusable UI primitives

---

## Tech Stack
- Frontend: React 19, TypeScript, Vite
- Styling: Tailwind CSS 4, tw-animate-css, CSS variables
- State & Data: TanStack Query, Zustand
- Forms & Validation: React Hook Form, Zod
- UI & Icons: Radix UI, lucide-react
- Routing: React Router
- Backend (dev): JSON Server
- Cloud (optional): Firebase
- Testing: Vitest, Testing Library (React + Jest DOM)
- Tooling: ESLint (flat config), TypeScript ESLint

---

## Project Structure
```text
Budget_Management_App/
├─ .github/                   # GitHub configs / workflows (CI)
├─ AI_Protocol/               # AI-related docs (internal)
├─ CI-CD Pipeline/            # CI/CD configuration & assets
├─ PROMPT_DOCUMENTATION/      # Prompt docs (internal)
└─ app/
   └─ Frontend/
      ├─ index.html
      ├─ package.json
      ├─ vite.config.ts
      ├─ vitest.config.ts
      ├─ setupTests.ts
      └─ src/
         ├─ main.tsx
         ├─ index.css
         ├─ core/            # app router, query client, etc.
         ├─ components/      # UI primitives & features
         └─ lib/             # utilities (e.g., cn)
```

---

## Getting Started

### Prerequisites
- Node.js 18+ (LTS recommended)
- npm 9+ or pnpm/yarn
- Optional: Firebase project (for auth/persistence)

### Installation
Clone the repository and install dependencies:

```bash
# Clone
git clone https://github.com/majoymajo/Budget_Management_App.git
cd Budget_Management_App/app/Frontend

# Install
npm install
```

### Environment Setup
Create a `.env` file in `app/Frontend` with your Firebase credentials (optional but recommended):

```bash
# app/Frontend/.env
VITE_FIREBASE_API_KEY=your_api_key
VITE_FIREBASE_AUTH_DOMAIN=your_project.firebaseapp.com
VITE_FIREBASE_PROJECT_ID=your_project_id
VITE_FIREBASE_STORAGE_BUCKET=your_project.appspot.com
VITE_FIREBASE_MESSAGING_SENDER_ID=your_sender_id
VITE_FIREBASE_APP_ID=your_app_id
```

### Run Locally
Start the dev server and (optionally) the mock API:

```bash
# in app/Frontend
npm run dev          # starts Vite at http://localhost:3000
npm run json-server  # starts JSON Server at http://localhost:3001
```

> Note: The app is configured to run on port 3000. JSON Server runs on 3001 by default.

---

## Usage

### Example: Adding a Transaction (mock API)
If you're using JSON Server, you can seed or post transactions like:

```json
{
  "id": 1,
  "type": "expense",
  "category": "Groceries",
  "amount": 42.50,
  "date": "2026-02-08",
  "notes": "Weekly essentials"
}
```

Post via curl:
```bash
curl -X POST http://localhost:3001/transactions \
  -H "Content-Type: application/json" \
  -d '{"type":"income","category":"Salary","amount":1200,"date":"2026-02-08"}'
```

### Screenshots
Add screenshots to `docs/screenshots/` and reference them here:

```markdown
![Dashboard Screenshot](docs/screenshots/dashboard.png)
![Transaction View](docs/screenshots/transactions.png)
```

---

## Available Scripts
From `app/Frontend`:

```bash
npm run dev            # Start local dev server
npm run build          # Type-check + build production assets
npm run preview        # Preview production build
npm run lint           # Lint codebase with ESLint
npm run test           # Run unit tests (Vitest)
npm run test:ci        # CI-friendly test run with coverage
npm run test:coverage  # Generate coverage reports
npm run json-server    # Run local mock API on port 3001
```

---

## Testing
Vitest is configured with JSDOM and Testing Library:

```ts
// vitest.config.ts
test: {
  globals: true,
  environment: 'jsdom',
  setupFiles: './setupTests.ts',
  include: ['src/**/*.{test,spec}.{ts,tsx}'],
  coverage: { provider: 'v8', reporter: ['text','lcov','html'] }
}
```

Run:
```bash
npm run test
npm run test:coverage
```

---

## Contributing
Contributions are welcome! To propose changes:

1. Fork the repo and create a feature branch:
   ```bash
   git checkout -b feat/awesome-improvement
   ```
2. Follow good commit hygiene (e.g., Conventional Commits).
3. Ensure lint and tests pass:
   ```bash
   npm run lint && npm run test
   ```
4. Open a pull request with a clear description, screenshots, and any relevant context.

---

## Roadmap
- Budget templates and multi-currency support
- Recurring transactions and reminders
- Advanced analytics (cash flow, category trends)
- Import/export (CSV/Excel)
- PWA offline mode

---

## Acknowledgments
- [React](https://react.dev/), [Vite](https://vite.dev/), [Tailwind CSS](https://tailwindcss.com/), [TanStack Query](https://tanstack.com/query)
- [Radix UI](https://www.radix-ui.com/), [Testing Library](https://testing-library.com/), [Vitest](https://vitest.dev/)
- Icons by [lucide](https://lucide.dev/)

---

## License
This project is licensed under the MIT License. See the [LICENSE](./LICENSE) file for details.

---

## Contact
Questions or ideas? Reach out:
- GitHub: [@majoymajo](https://github.com/majoymajo)
