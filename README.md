# 🧭 Budget Management App (AI-Driven)

> **A modern, event-driven financial tracking application built in a 2.5-day AI-First sprint.**

[![Build](https://github.com/majoymajo/Budget_Management_App/actions/workflows/ci.yml/badge.svg)](https://github.com/majoymajo/Budget_Management_App/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](#license)
[![SpringBoot](https://img.shields.io/badge/Spring_Boot-4.0.2-6DB33F?logo=spring&logoColor=white)](https://spring.io/projects/spring-boot)
[![RabbitMQ](https://img.shields.io/badge/RabbitMQ-3.9-FF6600?logo=rabbitmq&logoColor=white)](https://www.rabbitmq.com/)
[![React](https://img.shields.io/badge/React-19-61DAFB?logo=react&logoColor=white)](https://react.dev/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.9-3178C6?logo=typescript&logoColor=white)](https://www.typescriptlang.org/)

---

## 📖 Project Overview

**Budget Management App** is a full-stack solution designed to help users track their income and expenses while generating real-time financial reports.

What makes this project unique is its **methodology**: it was architected, developed, and deployed in an intensive **2.5-day micro-sprint** by a team of 3 developers working in tandem with AI Assistants (GitHub Copilot, Open Code). The project serves as a case study for **AI-First Software Development**.

---

## 🏗️ Architecture

The system follows a **Microservices Architecture** with an Event-Driven design for decoupling heavy processing.

### 1. Frontend (Modular Monolith)

- **Main Tech**: React 19, TypeScript 5.9, Vite 7.
- **Styling**: Tailwind CSS 4, Shadcn/UI (Radix UI).
- **State Management**: Zustand (Global UI state), TanStack Query (Server state & caching).
- **Auth**: Firebase Authentication.
- **Routing**: React Router DOM (Modular Architecture).
- **Testing**: Vitest + React Testing Library.
- **CI/CD**: GitHub Actions (Linting, Unit Tests, Build).

### 2. Backend (Microservices)

- **Language**: Java 17 (Eclipse Temurin).
- **Framework**: Spring Boot 4.0.2.
- **Architecture**: Event-Driven Microservices.
  - **Transaction Service**: Handles income/expense CRUD operations and publishes `TransactionCreated` events to RabbitMQ.
  - **Report Service**: Listens to RabbitMQ events to aggregate financial summaries and analytics.
- **Database**: MySQL 8.0 (One DB per service: `transactions_db`, `reports_db`).
- **Communication**: RabbitMQ (Message Broker).

### 3. devOps & Infrastructure

- **Infrastructure**: Docker & Docker Compose for orchestration.
- **CI/CD**: Fully automated pipeline via GitHub Actions.

---

## 🤖 AI Workflow Methodology

We adopted a hybrid **Agile-DevOps** workflow specifically adapted for AI collaboration:

- **Micro-Sprints**: Intensive 2.5-day development cycle.
- **Roles**: Frontend, Backend, and QA Engineers working with AI.
- **Prompt Engineering**: We standardized prompts for code generation, QA testing, and documentation (see `PROMPT_DOCUMENTATION/`).
- **Automation**: CI/CD pipelines generated and optimized by AI.

👉 **[Read the full AI Protocol & Methodology here](./AI_Protocol/AI_Workflow.md)**.

---

## 🚀 Getting Started

### Prerequisites

- **Docker & Docker Compose** (Essential for Backend)
- **Node.js 18+** (For Frontend)
- **Java 17 JDK** (Optional, if running backend without Docker)

### Step 1: Start the Backend (Infrastructure)

The backend services (Transactions, Reports, MySQL, RabbitMQ) are containerized.

```bash
cd app/backend-microservice/docker-compose/production
docker-compose up -d
```

_This starts the MySQL databases, RabbitMQ broker, and both Spring Boot Microservices._

### Step 2: Start the Frontend

Run the React application locally.

```bash
cd app/Frontend
npm install
# Ensure you have a .env file configured (see .env.example)
npm run dev
```

_Access the application at `http://localhost:5173` (or as specified by Vite)._

---

## 📂 Project Structure

```
📁 Budget_Management_App
├── 📂 AI_Protocol/                 # Methodology & AI Guidelines
│   └── 📄 AI_Workflow.md
├── 📂 Automatic Debugging/         # Debugging scripts & reports
│   └── 📄 bug_report.md
├── 📂 CI-CD Pipeline/              # CI/CD Configuration
│   └── 📄 CI_CD_Pipeline.yml
├── 📂 PROMPT_DOCUMENTATION/        # Prompt Library (Frontend, Backend, QA)
│   ├── 📄 Front-End.md
│   ├── 📄 Back-End.md
│   └── 📄 QA.md
├── 📂 Tests/                       # Test Suites (Mapped to .github/workflows)
│   └── 📄 ci.yml
├── 📂 app/                         # Main Application Code
│   ├── 📂 Frontend/                # React Application (Modular Monolith)
│   │   ├── 📂 src/
│   │   ├── 📄 package.json
│   │   ├── 📄 vite.config.ts
│   │   └── 📄 vitest.config.ts
│   └── 📂 backend-microservice/    # Spring Boot Microservices
│       ├── 📂 transaction/         # Transaction Service (Producer)
│       │   ├── 📂 src/
│       │   ├── 📄 pom.xml
│       │   └── 📄 Dockerfile
│       ├── 📂 report/              # Report Service (Consumer)
│       │   ├── 📂 src/
│       │   ├── 📄 pom.xml
│       │   └── 📄 Dockerfile
│       └── 📂 docker-compose/      # Orchestration configs
└── 📄 README.md                    # Project Entry Point
```

---

## ✨ Key Features

- **Smart Transactions**: Categorize and track spending with instant visual feedback.
- **Event-Driven Reports**: Financial summaries update asynchronously via RabbitMQ events.
- **Secure Auth**: Integration with Firebase Authentication.
- **Modern UI**: Dark/Light mode, responsive design, and accessible components built with Shadcn/UI.

---

## 👥 Contributors

- **Jacob** - Backend Engineer
- **Agus** - Frontend Engineer
- **Majo** - QA & DevOps Engineer
- **Gabriel** - Software Architect
- **AI Assistants** - GitHub Copilot & Open Code

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
