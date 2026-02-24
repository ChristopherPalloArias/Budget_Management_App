# Análisis Arquitectónico: Budget Management App

## Debate Arquitectónico — Microservicios Event-Driven y Oportunidades de Evolución hacia Clean Architecture


## Tabla de Contenidos

1. [Introducción](#1-introducción)
2. [Diagramación Arquitectónica](#2-diagramación-arquitectónica)
3. [Contexto del Sistema Actual](#3-contexto-del-sistema-actual)
4. [Análisis de la Arquitectura Actual](#4-análisis-de-la-arquitectura-actual)
5. [Oportunidades de Mejora Interna](#5-oportunidades-de-mejora-interna)
6. [Clean Architecture: Principios Relevantes](#6-clean-architecture-principios-relevantes)
7. [Contraste: Arquitectura Actual vs Clean Architecture](#7-contraste-arquitectura-actual-vs-clean-architecture)
8. [Arquitectura Propuesta (Evolución Interna con Clean Architecture)](#8-arquitectura-propuesta-evolución-interna-con-clean-architecture)
9. [Conclusión y Justificación de la Decisión Arquitectónica](#9-conclusión-y-justificación-de-la-decisión-arquitectónica)

---

## 1. Introducción

El presente documento constituye un **Architecture Decision Record (ADR)** formal que evalúa la arquitectura actual del sistema **Budget Management App** — una arquitectura de **microservicios event-driven** correctamente implementada — e identifica oportunidades de evolución interna mediante la adopción de principios de **Clean Architecture**.

El sistema ya posee una base arquitectónica sólida: tres microservicios independientes, comunicación asíncrona vía RabbitMQ, bases de datos dedicadas por servicio, autenticación propia con JWT y un frontend modular en React. La arquitectura cumple con los estándares de una solución distribuida moderna y funcional.

Este análisis tiene como objetivo reconocer las **fortalezas existentes**, identificar **oportunidades puntuales de mejora** en la estructura interna de cada servicio, y proponer una evolución gradual que refuerce la calidad del sistema sin comprometer lo que ya funciona bien.

**Objetivo del análisis:**

- Reconocer y documentar las fortalezas de la arquitectura actual.
- Identificar oportunidades de mejora interna en cada microservicio.
- Proponer la evolución hacia Clean Architecture como refinamiento interno.
- Documentar formalmente la decisión arquitectónica.

---

## 2. Diagramación Arquitectónica

### 2.1 Nivel 1 — Diagrama de Contexto del Sistema

```
                          ┌─────────────────────┐
                          │   👤 Usuario Final   │
                          │      [Persona]       │
                          │  Gestiona finanzas   │
                          │ personales vía web   │
                          └──────────┬──────────┘
                                     │
                          Registra transacciones,
                          consulta reportes,
                           se autentica [HTTPS]
                                     │
                                     ▼
                  ┌──────────────────────────────────┐
                  │    🏦 Budget Management App      │
                  │      [Sistema de Software]       │
                  │                                  │
                  │  Plataforma de gestión financiera │
                  │  personal: transacciones,        │
                  │  reportes y autenticación propia  │
                  └──────────────────────────────────┘
```

**Descripción:** Budget Management App es un sistema auto-contenido utilizado por usuarios finales vía navegador web. El sistema no depende de proveedores de autenticación externos — utiliza su propio servicio de autenticación con JWT y BCrypt.

---

### 2.2 Nivel 2 — Diagrama de Contenedores

```
                              👤 Usuario
                                  │
                            [HTTPS/JSON]
                                  │
                                  ▼
  ╔══════════════════════════════════════════════════════════════════════════╗
  ║                        Budget Management App                          ║
  ║                                                                        ║
  ║    ┌──────────────────────────────────────────────────────────────┐    ║
  ║    │              📱 Frontend SPA (Port 3000)                     │    ║
  ║    │         React 19 + TypeScript + Vite + Zustand               │    ║
  ║    │         Módulos: auth | transactions | reports | home        │    ║
  ║    └──────┬───────────────────┬───────────────────┬──────────────┘    ║
  ║           │                   │                   │                    ║
  ║      REST API            REST API + JWT       REST API + JWT          ║
  ║    /auth/*               /transactions/*      /reports/*              ║
  ║           │                   │                   │                    ║
  ║           ▼                   ▼                   ▼                    ║
  ║    ┌─────────────┐   ┌────────────────┐   ┌────────────────┐         ║
  ║    │ 🔐 Auth     │   │ 💰 Transaction │   │ 📊 Report      │         ║
  ║    │  Service    │   │   Service      │   │   Service      │         ║
  ║    │ Port 8083   │   │  Port 8081     │   │  Port 8082     │         ║
  ║    │             │   │                │   │                │         ║
  ║    │ Spring Boot │   │  Spring Boot   │   │  Spring Boot   │         ║
  ║    │ JWT+BCrypt  │   │  CRUD + Events │   │  Reportes+PDF  │         ║
  ║    └──────┬──────┘   └───┬───────┬───┘   └───┬───────┬───┘         ║
  ║           │              │       │            │       │              ║
  ║           │              │       │  ┌─────────┘       │              ║
  ║           │              │       │  │  ┌──────────────┘              ║
  ║           │              │       ▼  ▼  │                             ║
  ║           │              │   ┌──────────┐                            ║
  ║           │              │   │ 🐰       │                            ║
  ║           │              ├──▶│ RabbitMQ  │──▶ Report Service          ║
  ║           │              │   │ Port 5672 │   (consume eventos)       ║
  ║           │              │   └──────────┘                            ║
  ║           │              │   TopicExchange                           ║
  ║           │              │   Colas: created,                         ║
  ║           │              │   updated, deleted                        ║
  ║           ▼              ▼                    ▼                       ║
  ║    ┌─────────────┐ ┌─────────────┐  ┌─────────────┐                 ║
  ║    │ 🗄️ MySQL    │ │ 🗄️ MySQL    │  │ 🗄️ MySQL    │                 ║
  ║    │  auth_db    │ │transactions_db│ │  reports_db  │                 ║
  ║    │ Port 3309   │ │  Port 3307   │  │  Port 3308  │                 ║
  ║    └─────────────┘ └─────────────┘  └─────────────┘                 ║
  ╚══════════════════════════════════════════════════════════════════════════╝
```


---



## 3. Contexto del Sistema Actual

### 3.1 Stack Tecnológico

| Capa | Tecnología | Versión |
|:---|:---|:---|
| Frontend | React + TypeScript + Vite | 19 / 5.9 / 7 |
| UI/UX | Tailwind CSS + Shadcn/UI + Framer Motion | 4 / 1.4 / 12 |
| Estado Frontend | Zustand (global) + TanStack Query (server) | 5 / 5.90 |
| Backend (×3) | Java + Spring Boot | 17 / 4.0.2 |
| Persistencia | MySQL + Spring Data JPA | 8.0 |
| Mensajería | RabbitMQ (AMQP) | 4.0 |
| Autenticación | JWT propio (jjwt 0.12.6) + BCrypt | — |
| Orquestación | Docker + Docker Compose | — |
| CI/CD | GitHub Actions + SonarCloud | — |

### 3.2 Topología de Microservicios

| Servicio | Puerto | BD | Responsabilidad |
|:---|:---|:---|:---|
| **Auth** | 8083 | `auth_db` (3309) | Registro, login, JWT, gestión usuarios |
| **Transaction** | 8081 | `transactions_db` (3307) | CRUD transacciones, publicación de eventos |
| **Report** | 8082 | `reports_db` (3308) | Reportes agregados, consumo de eventos, PDFs |
| **RabbitMQ** | 5672 | — | Broker de mensajería (TopicExchange) |
| **Frontend** | 3000 | — | SPA React con 4 módulos |

### 3.3 Endpoints REST

| Servicio | Endpoints Principales |
|:---|:---|
| **Auth** | `POST /register`, `POST /login`, `GET /me`, `POST /logout` |
| **Transaction** | `POST`, `GET`, `GET /{id}`, `PUT /{id}`, `DELETE /{id}` (con filtro por `?period=`) |
| **Report** | `GET /reports`, `GET /all`, `GET /summary`, `POST /recalculate`, `DELETE /{id}`, `DELETE /{period}` |

---

## 4. Análisis de la Arquitectura Actual

### 4.1 Fortalezas Arquitectónicas (Fundación Sólida)

El sistema presenta una base arquitectónica robusta que cumple con patrones reconocidos de la industria:

| # | Fortaleza | Patrón | Evidencia en Código |
|:---|:---|:---|:---|
| **F-01** | **Database-per-service** | Microservices Pattern | 3 BDs MySQL independientes sin cross-database queries |
| **F-02** | **Event-driven communication** | Async Messaging | RabbitMQ con TopicExchange, routing keys por tipo (`created`, `updated`, `deleted`) |
| **F-03** | **Autenticación propia sin vendor lock-in** | Self-hosted Auth | JWT + BCrypt, sin dependencia de Firebase/Auth0/Keycloak |
| **F-04** | **DTOs inmutables** | API Security | Java Records (`TransactionRequest`, `TransactionResponse`), controllers nunca exponen entidades |
| **F-05** | **Controllers delegados** | SRP | 100% delegación al servicio, cero lógica de negocio en controllers |
| **F-06** | **Port parcial implementado** | Ports & Adapters | `TransactionEventPublisherPort` → `SpringTransactionEventPublisher` |
| **F-07** | **Aislamiento de datos por usuario** | Data Isolation | Cada operación CRUD valida `userId` extraído del JWT |
| **F-08** | **Configuración externalizada** | 12-Factor App | Variables de entorno vía Docker Compose (`JWT_SECRET`, `DB_*`, `RABBITMQ_*`) |
| **F-09** | **Frontend modular con Adapter Pattern** | Module Pattern + DIP | 4 módulos independientes, `IAuthRepository` como interfaz, adapters API→dominio UI |
| **F-10** | **Validación declarativa** | Input Validation | Bean Validation en DTOs + `@ValidPeriod` custom en Report Service |
| **F-11** | **CRUD completo** | RESTful API | Transaction Service con create, read, update, delete + filtro por período |
| **F-12** | **Eventos por tipo de operación** | Event Granularity | Eventos separados: `TransactionCreated`, `TransactionUpdated`, `TransactionDeleted` |

### 4.2 Patrones Arquitectónicos Correctamente Implementados

| Patrón | Estado | Evidencia |
|:---|:---|:---|
| Database-per-Service | ✅ Implementado | 3 MySQL independientes |
| Event-Driven (Async) | ✅ Implementado | RabbitMQ con TopicExchange |
| Stateless Auth | ✅ Implementado | JWT sin sesiones server-side |
| Containerization | ✅ Implementado | Docker + Docker Compose |
| CI/CD Pipeline | ✅ Implementado | GitHub Actions + SonarCloud |
| DTO Pattern | ✅ Implementado | Records inmutables en API boundary |

### 4.3 Estructura Interna de los Microservicios

Cada microservicio sigue una **arquitectura en capas** (Layered Architecture) con separación de responsabilidades:

```
com.microservice.{auth|transaction|report}/
├── controller/          ← Presentación (REST endpoints)
├── service/             ← Negocio (interfaces + implementaciones)
│   ├── port/            ← Ports parciales (Transaction Service)
│   └── impl/            ← Implementaciones
├── repository/          ← Acceso a datos (Spring Data JPA)
├── model/               ← Entidades JPA
├── dto/                 ← DTOs de API (Records)
├── security/            ← JWT Filter + Provider
├── exception/           ← Manejo global de errores
├── infrastructure/      ← Messaging (RabbitMQ)
└── config/              ← CORS, Security
```

Esta estructura es funcional y organizada. Cada capa tiene su responsabilidad definida y los servicios se comunican a través de interfaces.

---

## 5. Oportunidades de Mejora Interna

Si bien la arquitectura es sólida, se identifican oportunidades puntuales para llevarla al siguiente nivel de madurez:

### 5.1 Oportunidad #1: Separar el Modelo de Dominio de la Entidad JPA

**Estado actual:** Las clases `Transaction.java`, `Report.java` y `User.java` cumplen doble rol: son tanto el modelo conceptual del negocio como la entidad de persistencia JPA.

```java
// Actualmente — modelo y entidad JPA son la misma clase
@Entity @Table(name = "transactions")
public class Transaction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Long transactionId;
    // ...
}
```

**¿Por qué mejorar?** Separar el dominio de JPA permitiría que el modelo de negocio evolucione independientemente de la base de datos. No es un problema crítico actualmente — el sistema funciona correctamente — pero es un refinamiento que facilita la testabilidad y futura evolución.

**Beneficio:** Tests de lógica de negocio sin necesidad de contexto Spring ni mocking de JPA.

### 5.2 Oportunidad #2: Enriquecer el Modelo de Dominio

**Estado actual:** Las entidades usan Lombok (`@Getter @Setter @Builder`) y son modelos de datos sin métodos de negocio. La lógica como validación de montos y cálculo de balances reside en los `*ServiceImpl`.

**¿Por qué mejorar?** Mover lógica como `validateAmount()`, `Report.accumulate()` o `User.verifyPassword()` directamente a las entidades mejoraría la cohesión. No es un error — es una oportunidad de encapsulamiento.

### 5.3 Oportunidad #3: Extender el Patrón de Ports

**Estado actual:** Transaction Service ya tiene `TransactionEventPublisherPort` — un port correctamente implementado. Este patrón aún no se ha extendido a:
- Persistencia (`TransactionPersistencePort`)
- Generación de tokens (`TokenGeneratorPort`)
- Hashing de passwords (`PasswordHasherPort`)

**¿Por qué mejorar?** Extender los Ports permitiría intercambiar implementaciones (ej. cambiar de RabbitMQ a Kafka) sin tocar la lógica de negocio. La base ya está — solo falta ampliar el patrón.

### 5.4 Oportunidad #4: Unificar Componentes Compartidos

**Estado actual:** `JwtTokenProvider` y `JwtAuthenticationFilter` están replicados en los 3 servicios con código casi idéntico.

| Componente | Auth | Transaction | Report |
|:---|:---|:---|:---|
| `JwtTokenProvider` | ✅ (genera + valida) | ✅ (solo valida) | ✅ (solo valida) |
| `JwtAuthenticationFilter` | ✅ | ✅ | ✅ |
| `SecurityConfig` | ✅ | ✅ | ✅ |
| `PaginatedResponse` | — | ✅ | ✅ |

**¿Por qué mejorar?** Extraer una librería compartida (`shared-security`) eliminaría duplicación y centralizaría los cambios de seguridad. No es un defecto — es una optimización natural conforme el sistema crece.

### 5.5 Oportunidad #5: Ampliar la Cobertura de Tests

**Estado actual:** La cobertura de tests es funcional pero tiene espacio para crecer:

| Servicio | Tests Existentes | Oportunidad |
|:---|:---|:---|
| Auth | Controller + Service + JWT tests | Ampliar edge cases |
| Transaction | Controller + Service tests | Agregar tests de eventos, mappers |
| Report | Controller + Service + Consumer + PDF tests | Ampliar integración |
| Frontend | Tests de componentes UI | Agregar tests de services, hooks, stores |

**¿Por qué mejorar?** Mayor cobertura proporciona mayor confianza para refactorings futuros.

### 5.6 Oportunidad #6: Resiliencia del Messaging

**Estado actual:** La comunicación vía RabbitMQ funciona correctamente. Oportunidades de robustez:

- Agregar **Dead Letter Queue (DLQ)** para mensajes que fallen en el consumo.
- Implementar **idempotencia** registrando transacciones ya procesadas.
- Diferenciar la lógica entre `consumeCreated` y `consumeUpdated`.

Estos son refinamientos para entornos de producción de alta disponibilidad.

---

## 6. Clean Architecture: Principios Relevantes

### 6.1 Separación de Capas

| Capa | Responsabilidad | Estado Actual |
|:---|:---|:---|
| **Entities (Dominio)** | Reglas de negocio invariantes | Parcial — modelos como entidades JPA |
| **Use Cases** | Flujos de aplicación | Implementado en `*ServiceImpl` |
| **Interface Adapters** | Conversión de formatos | Implementado (Controllers, DTOs, Mappers) |
| **Frameworks** | Detalles técnicos | Correctamente usado (Spring, JPA, RabbitMQ) |

### 6.2 Regla de Dependencias

Las dependencias deben apuntar **hacia el interior** (hacia el dominio). Actualmente las dependencias fluyen correctamente de Controller → Service → Repository, con la excepción de que el modelo de dominio (`model/`) tiene dependencias hacia JPA. Este es el principal punto donde Clean Architecture aportaría mejora.

### 6.3 Independencia de Frameworks

El sistema ya aplica este principio **parcialmente**: el frontend usa `IAuthRepository` (interfaz) → implementación concreta, y Transaction Service usa `TransactionEventPublisherPort`. Extender este patrón a persistencia completaría la independencia.

---

## 7. Contraste: Arquitectura Actual vs Clean Architecture

### 7.1 Comparativa

| Dimensión | Estado Actual | Con Clean Architecture Interna |
|:---|:---|:---|
| Modelo de dominio | `@Entity` JPA (funcional) | POJO puro con lógica de negocio |
| Interfaz del servicio | Usa `Pageable` de Spring | Usaría tipos propios del dominio |
| Eventos | `ApplicationEvent` de Spring | Records POJO puros |
| Persistencia | `extends JpaRepository` directo | `PersistencePort` → `JpaAdapter` |
| Port de eventos | ✅ Ya implementado | Ya implementado (mantener) |
| JWT | Funcional, replicado ×3 | Librería compartida |
| Testabilidad | Requiere contexto Spring | Tests unitarios puros |

### 7.2 Impacto por Atributo de Calidad

| Atributo | Estado Actual | Con Mejoras | Impacto |
|:---|:---|:---|:---|
| **Funcionalidad** | 🟢 Completa | 🟢 Igual | — |
| **Mantenibilidad** | � Buena | 🟢 Excelente | ↑ |
| **Testabilidad** | � Mejorable | 🟢 Excelente | ↑↑ |
| **Escalabilidad** | � Buena (microservicios) | 🟢 Excelente | ↑ |
| **Seguridad** | � Buena (JWT + BCrypt) | 🟢 Mejor (unificada) | ↑ |
| **Evolución** | � Mejorable | 🟢 Excelente | ↑↑ |
| **Portabilidad** | � Parcial | 🟢 Alta | ↑ |

### 7.3 Debate Arquitectónico (Actividad 1.1): Dolores Internos vs. Beneficios de Clean Architecture

Aunque la topología de microservicios es correcta, la **estructura interna en capas** de cada servicio presenta características heredadas de un monolito tradicional. La siguiente tabla sintetiza el debate:

| Dolor del Monolito Interno (Layered) | Evidencia Concreta | Beneficio de Clean Architecture |
|:---|:---|:---|
| **Dominio acoplado a persistencia** — El modelo de negocio no puede existir sin el ORM | `@Entity @Table @Column` en `model/` de los 3 servicios | Dominio como POJO puro; JPA solo en `adapter/out/persistence/` |
| **Modelo anémico** — Entidades sin lógica, servicios "gordos" | `TransactionServiceImpl` contiene validación y lógica que debería estar en la entidad | Modelo rico con `validate()`, `toEvent()`, `accumulate()` |
| **Interfaz acoplada al framework** — Contratos de servicio importan tipos de Spring | `TransactionService` importa `Pageable` de Spring Data | Tipos propios del dominio, sin imports de framework |
| **Eventos acoplados** — Eventos extienden clases de Spring | `TransactionCreatedEvent` extiende `ApplicationEvent` | Records POJO puros sin dependencia de framework |
| **Código duplicado** — Lógica de seguridad replicada | `JwtTokenProvider` copiado ×3 en auth, transaction, report | Port `TokenValidationPort` con adapter único |
| **Tests dependientes del framework** — Probar lógica requiere Spring context | Cobertura limitada por complejidad del setup | Tests unitarios puros del dominio (< 100ms) |

> **Síntesis:** El sistema **no** es un monolito — es una arquitectura de microservicios correcta. Sin embargo, cada microservicio **internamente** hereda patrones de un monolito en capas donde el dominio depende de la infraestructura. Clean Architecture resuelve esto invirtiendo las dependencias internas sin alterar la topología distribuida.

---

## 8. Arquitectura Propuesta (Evolución Interna con Clean Architecture)

### 8.1 Estructura Interna Propuesta por Microservicio

La propuesta **no** cambia la topología de microservicios — solo refina la estructura interna:

```
com.microservice.transaction/
├── domain/                          ← EVOLUCIÓN: Dominio puro
│   ├── model/
│   │   ├── Transaction.java         (POJO con validate(), toEvent())
│   │   └── TransactionType.java
│   ├── event/
│   │   └── TransactionCreatedEvent.java  (Record puro)
│   └── port/
│       ├── in/CreateTransactionPort.java
│       └── out/
│           ├── TransactionPersistencePort.java  (NUEVO)
│           └── TransactionEventPublisherPort.java  (YA EXISTE ✅)
├── application/                     ← EVOLUCIÓN: Use Cases explícitos
│   ├── CreateTransactionUseCase.java
│   └── GetTransactionUseCase.java
├── adapter/                         ← EVOLUCIÓN: Adapters formales
│   ├── in/web/TransactionController.java  (SE MANTIENE)
│   ├── out/persistence/
│   │   ├── JpaTransactionEntity.java    (@Entity AQUÍ)
│   │   └── TransactionPersistenceAdapter.java
│   └── out/messaging/
│       └── RabbitMQEventPublisherAdapter.java  (SE MANTIENE)
└── config/BeanConfiguration.java
```

### 8.2 Ejemplo: Modelo de Dominio Enriquecido

```java
// domain/model/Transaction.java — Evolución del modelo actual
public class Transaction {
    private final Long id;
    private final String userId;
    private TransactionType type;
    private BigDecimal amount;
    private String category;
    private LocalDate date;

    public static Transaction create(String userId, TransactionType type,
                                      BigDecimal amount, String category, LocalDate date) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Amount must be > 0");
        return new Transaction(null, userId, type, amount, category, date);
    }

    public TransactionCreatedEvent toCreatedEvent() {
        return new TransactionCreatedEvent(id, userId, type, amount, date);
    }
}
```

---



## 9. Conclusión y Justificación de la Decisión Arquitectónica

### 9.1 Evaluación General

La arquitectura actual de Budget Management App es **sólida y funcional**. El sistema implementa correctamente:

- ✅ **Microservicios** con responsabilidades bien definidas (Auth, Transaction, Report).
- ✅ **Event-driven communication** vía RabbitMQ con TopicExchange.
- ✅ **Database-per-service** con 3 MySQL independientes.
- ✅ **Autenticación propia** sin vendor lock-in (JWT + BCrypt).
- ✅ **Frontend modular** con Adapter Pattern y separación de concerns.
- ✅ **DTOs inmutables**, controllers delegados, validación declarativa.
- ✅ **Port parcial** ya implementado (`TransactionEventPublisherPort`).
- ✅ **Containerización** completa con Docker Compose.
- ✅ **CI/CD** automatizado con GitHub Actions + SonarCloud.

### 9.2 Oportunidades de Evolución

Las mejoras identificadas son **refinamientos internos**, no correcciones de defectos fundamentales:

| Oportunidad | Prioridad | Impacto |
|:---|:---|:---|
| Separar dominio de JPA | Media | Mejora testabilidad y evolución |
| Enriquecer modelo de dominio | Media | Mejora cohesión y encapsulamiento |
| Extender Ports a persistencia | Media | Completa el patrón ya iniciado |
| Unificar JWT en librería compartida | Baja | Reduce duplicación |
| Ampliar test coverage | Media | Mayor confianza en refactorings |
| Agregar DLQ y idempotencia | Baja | Robustez en producción |

### 9.3 Decisión Arquitectónica

> **DECISIÓN:** Mantener la arquitectura de microservicios event-driven actual como base sólida, y evolucionar **gradualmente** la estructura interna de cada servicio hacia Clean Architecture, extendiendo el patrón de Ports que ya existe en el Transaction Service.

**Principios guía:**

1. **No romper lo que funciona** — La topología y comunicación actuales se mantienen intactas.
2. **Evolución incremental** — Se comienza por Transaction Service (que ya tiene el Port de eventos).
3. **Extender, no reescribir** — El `TransactionEventPublisherPort` existente es la semilla del patrón.
4. **Test-first** — Ampliar cobertura antes de cualquier refactoring interno.

### 9.4 Roadmap Sugerido

| Fase | Alcance | Prioridad |
|:---|:---|:---|
| **Fase 1** | Ampliar test coverage en los 3 servicios | Alta |
| **Fase 2** | Transaction Service: extraer dominio puro + `PersistencePort` | Media |
| **Fase 3** | Auth Service: extraer `TokenPort` + `PasswordHasherPort` + unificar JWT | Media |
| **Fase 4** | Report Service: enriquecer dominio + DLQ + idempotencia | Media |