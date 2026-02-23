# 📋 Informe Técnico: Migración de Firebase Auth a Backend Auth Propio

> **Proyecto:** Budget Management App  
> **Fecha del análisis:** 2026-02-23  
> **Rol:** Arquitecto de Software & Desarrollador Backend Senior  
> **Objetivo:** Diseñar un nuevo microservicio de autenticación que reemplaze Firebase Auth, siguiendo los patrones existentes de los backends `transaction` y `report`.

---

## 1. Resumen Ejecutivo

Actualmente, la autenticación del sistema depende directamente de **Firebase Authentication** desde el frontend. Firebase se utiliza para:

1. **Registro** de usuarios (`createUserWithEmailAndPassword`)
2. **Login** con email/password (`signInWithEmailAndPassword`)
3. **Login con Google OAuth** (`signInWithPopup` + `GoogleAuthProvider`)
4. **Logout** (`signOut`)
5. **Listener de estado de autenticación** (`onAuthStateChanged`)

El `userId` (Firebase `uid`) se propaga a los microservicios `transaction` y `report` como un `String` opaco — estos backends **no validan** la identidad del usuario; confían ciegamente en el ID que el frontend envía.

### ✅ Buena noticia: El frontend ya tiene un patrón desacoplado

El frontend implementa el **Repository Pattern** para Auth:
- **Interface:** `IAuthRepository` (contrato puro, sin mención de Firebase)
- **Implementación:** `FirebaseAuthRepository` (conoce Firebase)
- **Inyección:** `dependencies.ts` instancia `FirebaseAuthRepository` como `IAuthRepository`

Esto significa que crear una nueva implementación `ApiAuthRepository` es **intercambiar una sola línea** en `dependencies.ts`.

---

## 2. Mapa de Dependencias Firebase en el Frontend

### 2.1 Archivos con Dependencia DIRECTA a Firebase SDK

| # | Archivo | Dependencia | Rol | Acción Requerida |
|---|---------|-------------|-----|------------------|
| **F1** | `core/config/firebase.config.ts` | `firebase/app`, `firebase/auth` | Inicializa `FirebaseApp` y exporta `auth` | 🔴 **ELIMINAR** completamente |
| **F2** | `infrastructure/auth/FirebaseAuthRepository.ts` | `firebase/auth` (9 imports: `signInWithEmailAndPassword`, `signInWithPopup`, `signOut`, `createUserWithEmailAndPassword`, `updateProfile`, `onAuthStateChanged`, `GoogleAuthProvider`, `EmailAuthProvider`, `User`) | Implementación concreta del `IAuthRepository` | 🔴 **REEMPLAZAR** por `ApiAuthRepository` |
| **F3** | `shared/types/index.ts` | `import { type User as FirebaseUser } from 'firebase/auth'` | Define `mapFirebaseUser()` helper | 🔴 **ELIMINAR** import y función `mapFirebaseUser` |
| **F4** | `core/config/__mocks__/firebase.config.ts` | Mock de Firebase para tests | Test mock | 🟡 **ELIMINAR** cuando se elimine F1 |
| **F5** | `test/__mocks__/firebase/app.ts` | Mock de `firebase/app` | Test mock | 🟡 **ELIMINAR** |
| **F6** | `test/__mocks__/firebase/auth.ts` | Mock de `firebase/auth` | Test mock | 🟡 **ELIMINAR** |

### 2.2 Archivos con Dependencia INDIRECTA (via `IAuthRepository`)

Estos archivos **NO importan Firebase** — usan la abstracción `IAuthRepository`. **No requieren cambios funcionales**, pero deben ser retestados:

| # | Archivo | Usa | Impacto |
|---|---------|-----|---------|
| **I1** | `core/config/dependencies.ts` | `new FirebaseAuthRepository()` | 🔴 **Cambiar** a `new ApiAuthRepository()` (1 línea) |
| **I2** | `modules/auth/services/authService.ts` | `authRepository.signIn()`, `.signInWithProvider()`, `.register()`, `.signOut()` | ✅ Sin cambios — usa interfaz |
| **I3** | `modules/auth/store/useUserStore.ts` | `authRepository.signOut()`, `.onAuthStateChanged()` | ⚠️ **Cambio requerido** — `onAuthStateChanged` no aplica sin Firebase (ver §2.3) |
| **I4** | `modules/auth/hooks/useAuthInitialization.ts` | `useUserStore.initAuthListener()` | ⚠️ **Cambio requerido** — vinculado a `onAuthStateChanged` |
| **I5** | `modules/auth/hooks/useLoginForm.ts` | `loginWithEmail()`, `loginWithGoogle()` | ✅ Sin cambios |
| **I6** | `modules/auth/hooks/useAuthStatus.ts` | `useUserStore.isAuthenticated`, `.isLoading` | ✅ Sin cambios |
| **I7** | `modules/auth/components/AuthProvider.tsx` | `useAuthInitialization()` | ⚠️ Requiere ajuste si cambia el listener |
| **I8** | `modules/auth/components/ProtectedRoute.tsx` | `useAuthStatus()` | ✅ Sin cambios |
| **I9** | `modules/auth/components/PublicRoute.tsx` | `useUserStore.user`, `.isLoading` | ✅ Sin cambios |
| **I10** | `modules/auth/components/LoginForm.tsx` | `useLoginForm()` | ✅ Sin cambios |
| **I11** | `modules/auth/components/RegisterForm.tsx` | `registerWithEmail()` | ✅ Sin cambios |

### 2.3 Punto Crítico: `onAuthStateChanged`

Firebase proporciona un **listener reactivo** que detecta automáticamente cambios de sesión (login/logout/token refresh) vía WebSocket/Polling interno. Este patrón no existe en una API REST stateless.

**Alternativas para el nuevo backend:**

| Opción | Complejidad | Descripcion |
|--------|-------------|-------------|
| **A. Token en localStorage + verificación al iniciar** | Baja | Al cargar la app, leer el JWT del localStorage. Si existe y no ha expirado, el usuario está autenticado. No hay "listener", pero el `AuthProvider` verifica una vez al montarse. |
| **B. Polling periódico** | Media | Un `setInterval` que valida el token con `GET /api/v1/auth/me` periódicamente. |
| **C. SSE / WebSocket** | Alta | Mantener un canal abierto para eventos de sesión. Overkill para este caso. |

**Recomendación:** **Opción A** — Es la más simple, alineada con REST, y suficiente para esta aplicación. El `onAuthStateChanged` se reemplaza por una verificación del token en `localStorage` + un endpoint `GET /api/v1/auth/me` para validar la sesión al cargar la app.

### 2.4 Variables de Entorno Firebase a Eliminar

```
# En .env y .env.example del Frontend:
VITE_FIREBASE_API_KEY          → ELIMINAR
VITE_FIREBASE_AUTH_DOMAIN      → ELIMINAR
VITE_FIREBASE_PROJECT_ID       → ELIMINAR
VITE_FIREBASE_STORAGE_BUCKET   → ELIMINAR
VITE_FIREBASE_MESSAGING_SENDER_ID → ELIMINAR
VITE_FIREBASE_APP_ID           → ELIMINAR

# NUEVA variable:
VITE_API_AUTH_URL=http://localhost:8083/api   → AGREGAR
```

```
# En docker-compose.yml raíz — ELIMINAR build args de Firebase:
# VITE_FIREBASE_API_KEY, VITE_FIREBASE_AUTH_DOMAIN, etc.
# AGREGAR:
# VITE_API_AUTH_URL: http://auth:8083/api
```

```
# En vite-env.d.ts — ELIMINAR las 6 declaraciones VITE_FIREBASE_*
# AGREGAR: readonly VITE_API_AUTH_URL: string
```

### 2.5 Dependencia NPM a Eliminar

```json
// package.json → ELIMINAR:
"firebase": "12.9.0"
```

---

## 3. Puntos Críticos de `userId` en el Sistema

El `userId` actualmente es el Firebase `uid` (string tipo `"aB1cD2eF3gH4iJ5kL6mN7oP"`) que se envía desde el frontend como campo en los requests. **Los backends no lo validan.**

### 3.1 Uso en Transaction Service

| Archivo | Uso de `userId` | Riesgo |
|---------|----------------|--------|
| `TransactionRequest.java` (Record) | `@NotBlank String userId` — Campo requerido | El backend acepta **cualquier** string como userId |
| `Transaction.java` (Entity) | `@Column(name = "user_id") private String userId` | Se persiste tal cual |
| `TransactionController.java` | No filtra por userId — `getAll()` retorna TODO | 🔴 **Riesgo de seguridad** — Un usuario puede ver transacciones de otros |

### 3.2 Uso en Report Service

| Archivo | Uso de `userId` |
|---------|----------------|
| `Report.java` (Entity) | `@Column(name = "user_id") private String userId` |
| `ReportController.java` | `@PathVariable String userId` — Cualquiera puede consultar reportes de otro usuario |

### 3.3 Uso en Frontend → Backend

| Flujo | Cómo se envía `userId` |
|-------|----------------------|
| Crear transacción | `POST /api/v1/transactions` con `{ userId: user.id, ... }` — El `user.id` viene del store Zustand (originalmente Firebase `uid`) |
| Listar transacciones | `GET /api/v1/transactions?userId=xxx` |
| Obtener reportes | `GET /api/v1/reports/{userId}/summary` |

**Implicación para la migración:** Al reemplazar Firebase, el nuevo backend generará sus propios user IDs. Se debe:
1. Decidir el formato del nuevo `userId` (UUID recomendado)
2. Considerar un período de migración de datos si hay datos en producción con Firebase UIDs

---

## 4. Diseño del Backend Auth — Endpoints RESTful

### 4.1 Especificación de Endpoints

#### `POST /api/v1/auth/register`

Registra un nuevo usuario.

**Request:**
```json
{
  "displayName": "Juan Pérez",
  "email": "juan@example.com",
  "password": "MiContraseña123"
}
```

**Response `201 CREATED`:**
```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "email": "juan@example.com",
  "displayName": "Juan Pérez",
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

**Errores:**

| HTTP Status | Código | Caso |
|-------------|--------|------|
| `400` | `VALIDATION_ERROR` | Campos inválidos (email malformado, contraseña débil) |
| `409` | `EMAIL_ALREADY_EXISTS` | El email ya está registrado |

---

#### `POST /api/v1/auth/login`

Autentica un usuario existente.

**Request:**
```json
{
  "email": "juan@example.com",
  "password": "MiContraseña123"
}
```

**Response `200 OK`:**
```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "email": "juan@example.com",
  "displayName": "Juan Pérez",
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

**Errores:**

| HTTP Status | Código | Caso |
|-------------|--------|------|
| `401` | `INVALID_CREDENTIALS` | Email o contraseña incorrectos |
| `403` | `ACCOUNT_DISABLED` | Cuenta deshabilitada |

---

#### `GET /api/v1/auth/me`

Obtiene el usuario autenticado a partir del JWT en el header `Authorization`.

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**Response `200 OK`:**
```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "email": "juan@example.com",
  "displayName": "Juan Pérez",
  "photoURL": null
}
```

**Errores:**

| HTTP Status | Código | Caso |
|-------------|--------|------|
| `401` | `TOKEN_EXPIRED` | Token JWT expirado |
| `401` | `TOKEN_INVALID` | Token JWT inválido o manipulado |

---

#### `POST /api/v1/auth/logout`

Invalida el token del usuario (si se implementa blacklist de tokens).

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**Response `204 NO CONTENT`** (sin body)

> **Nota:** En una implementación JWT stateless pura, el logout se maneja solo en el cliente (eliminar el token del localStorage). El endpoint es opcional pero recomendable para auditoría y futuro soporte de token blacklist.

---

### 4.2 Modelo de Datos — Entidad `User`

```
┌───────────────────────────────────────────────────────────┐
│  auth_users                                               │
├───────────────┬──────────────────┬────────────────────────┤
│ Column        │ Type             │ Constraints            │
├───────────────┼──────────────────┼────────────────────────┤
│ user_id       │ VARCHAR(36)      │ PK (UUID)              │
│ email         │ VARCHAR(255)     │ UNIQUE, NOT NULL       │
│ password_hash │ VARCHAR(255)     │ NOT NULL               │
│ display_name  │ VARCHAR(100)     │ NOT NULL               │
│ photo_url     │ VARCHAR(500)     │ NULLABLE               │
│ enabled       │ BOOLEAN          │ NOT NULL, DEFAULT TRUE │
│ created_at    │ TIMESTAMP(6)     │ NOT NULL, auto-set     │
│ updated_at    │ TIMESTAMP(6)     │ NOT NULL, auto-set     │
└───────────────┴──────────────────┴────────────────────────┘
```

### 4.3 Estructura del Proyecto — Alineado a Patrones Existentes

Siguiendo la estructura **exacta** de `transaction` y `report`:

```
app/backend-microservice/auth/
├── Dockerfile
├── pom.xml
├── mvnw, mvnw.cmd, .mvn/
└── src/
    ├── main/
    │   ├── java/com/microservice/auth/
    │   │   ├── AuthApplication.java
    │   │   ├── config/
    │   │   │   ├── CorsConfig.java              ← Mismo patrón que transaction
    │   │   │   └── SecurityConfig.java           ← NUEVO: Config Spring Security
    │   │   ├── controller/
    │   │   │   └── AuthController.java           ← @RestController + @RequestMapping("api/v1/auth")
    │   │   ├── dto/
    │   │   │   ├── RegisterRequest.java          ← Java Record con @NotBlank, @Email, @Size
    │   │   │   ├── LoginRequest.java             ← Java Record con @NotBlank
    │   │   │   ├── AuthResponse.java             ← Java Record (userId, email, displayName, token)
    │   │   │   ├── UserResponse.java             ← Java Record (para GET /me, sin token)
    │   │   │   └── AuthMapper.java               ← Mapeo Entity ↔ DTOs
    │   │   ├── service/
    │   │   │   ├── AuthService.java              ← Interface
    │   │   │   └── impl/
    │   │   │       └── AuthServiceImpl.java      ← @Service con lógica de negocio
    │   │   ├── security/
    │   │   │   ├── JwtTokenProvider.java          ← Generación y validación JWT
    │   │   │   ├── JwtAuthenticationFilter.java   ← OncePerRequestFilter para interceptar requests
    │   │   │   └── UserDetailsServiceImpl.java    ← Implementación de UserDetailsService
    │   │   ├── model/
    │   │   │   └── User.java                      ← @Entity JPA
    │   │   ├── repository/
    │   │   │   └── UserRepository.java            ← JpaRepository<User, String>
    │   │   └── exception/
    │   │       ├── AuthException.java
    │   │       ├── EmailAlreadyExistsException.java
    │   │       ├── CustomErrorResponse.java       ← Mismo patrón que transaction
    │   │       └── GlobalExceptionHandler.java    ← Mismo patrón que transaction
    │   └── resources/
    │       └── application.yaml
    └── test/
        ├── java/com/microservice/auth/
        │   ├── controller/AuthControllerTest.java
        │   ├── service/impl/AuthServiceImplTest.java
        │   └── security/JwtTokenProviderTest.java
        └── resources/
            └── application-test.yaml
```

### 4.4 Dependencias — `pom.xml`

Siguiendo el patrón del `pom.xml` de `transaction` (Spring Boot 4.0.2, Java 17):

```xml
<!-- Las mismas que transaction, EXCEPTO: -->
<!-- QUITAR: spring-boot-starter-amqp (Auth no publica eventos) -->
<!-- AGREGAR: -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.6</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>
```

### 4.5 Configuración — `application.yaml`

```yaml
server:
  port: 8083

spring:
  application:
    name: auth
  datasource:
    url: jdbc:mysql://mysql-auth:3306/auth_db
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

app:
  cors:
    allowed-origins: "http://localhost:3000,http://localhost:4200"
  jwt:
    secret: ${JWT_SECRET}
    expiration-ms: 86400000  # 24 horas
```

### 4.6 Docker Compose — Nuevos servicios a agregar

```yaml
# AGREGAR en docker-compose.yml raíz:

  mysql-auth:
    image: mysql:8.0
    container_name: mysql-auth
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
      MYSQL_USER: ${DB_USERNAME}
      MYSQL_PASSWORD: ${DB_PASSWORD}
      MYSQL_DATABASE: auth_db
    ports:
      - "3309:3306"
    volumes:
      - mysql-auth-data:/var/lib/mysql
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-u", "root", "-p${MYSQL_ROOT_PASSWORD}"]
      timeout: 20s
      retries: 10
      interval: 3s
    networks:
      - finance-network

  auth:
    build:
      context: ./app/backend-microservice/auth
    container_name: auth
    ports:
      - "8083:8083"
    depends_on:
      mysql-auth:
        condition: service_healthy
    environment:
      DB_USERNAME: ${DB_USERNAME}
      DB_PASSWORD: ${DB_PASSWORD}
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql-auth:3306/auth_db
      SPRING_DATASOURCE_DRIVER_CLASS_NAME: com.mysql.cj.jdbc.Driver
      SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT: org.hibernate.dialect.MySQLDialect
      JWT_SECRET: ${JWT_SECRET}
    restart: unless-stopped
    networks:
      - finance-network

# AGREGAR en volumes:
  mysql-auth-data:
```

---

## 5. Diseño del Adaptador Frontend — `ApiAuthRepository`

### 5.1 Nueva implementación del `IAuthRepository`

El nuevo `ApiAuthRepository` implementará la misma interfaz `IAuthRepository` pero contra la API REST:

```
infrastructure/auth/ApiAuthRepository.ts    ← NUEVO
infrastructure/auth/FirebaseAuthRepository.ts  ← SE MANTIENE (temporal, durante migración)
```

**Contrato de `IAuthRepository` existente (NO cambia):**
```typescript
interface IAuthRepository {
  signIn(credentials: IAuthCredentials): Promise<IAuthUser>;
  signInWithProvider(provider: AuthProvider): Promise<IAuthUser>;
  signOut(): Promise<void>;
  register(credentials: IRegisterCredentials): Promise<IAuthUser>;
  onAuthStateChanged(callback: (user: IAuthUser | null) => void): Unsubscribe;
}
```

**Mapeo de métodos Firebase → API REST:**

| Método `IAuthRepository` | Firebase (actual) | API REST (nuevo) |
|--------------------------|-------------------|-----------------|
| `signIn()` | `signInWithEmailAndPassword()` | `POST /api/v1/auth/login` + guardar JWT en localStorage |
| `register()` | `createUserWithEmailAndPassword()` + `updateProfile()` | `POST /api/v1/auth/register` + guardar JWT en localStorage |
| `signOut()` | `signOut(auth)` | Eliminar JWT de localStorage (+ opcional `POST /api/v1/auth/logout`) |
| `signInWithProvider('GOOGLE')` | `signInWithPopup(auth, GoogleAuthProvider)` | ⚠️ **No implementar en Phase 1** — Ver §5.2 |
| `onAuthStateChanged()` | Listener nativo Firebase | Verificar token en localStorage + `GET /api/v1/auth/me` al cargar app |

### 5.2 Google OAuth — Decisión Clave

El login con Google actualmente usa **Firebase como broker OAuth**. Sin Firebase, hay dos opciones:

| Opción | Descripción | Complejidad |
|--------|-------------|-------------|
| **A. OAuth2 en el backend** | El nuevo backend Auth implementa el flujo OAuth2 con Google (Authorization Code Flow). El frontend redirige a Google, Google redirige al backend con el `code`, el backend lo intercambia por tokens y crea/autentica el usuario. | **Media-Alta** |
| **B. Eliminar Google OAuth (Phase 1)** | Soportar solo email/password inicialmente. Agregar Google OAuth como feature separada después. | **Baja** |

**Recomendación:** **Opción B** para Phase 1. Google OAuth es un "nice-to-have" que puede implementarse como feature independiente después de que el flujo email/password funcione sin Firebase.

En el `ApiAuthRepository`, `signInWithProvider('GOOGLE')` lanzará temporalmente:
```typescript
throw new Error('Inicio de sesión con Google no disponible temporalmente. Use email y contraseña.');
```

### 5.3 Cambios en el `HttpClient`

```typescript
// HttpClient.ts → AGREGAR al ServiceType:
export type ServiceType = 'transactions' | 'reports' | 'auth';  // ← AGREGAR 'auth'

// En getBaseURL():
case 'auth':
  return import.meta.env.VITE_API_AUTH_URL;
```

### 5.4 Manejo de JWT en el Frontend

Se necesita un interceptor en el `HttpClient` de `transactions` y `reports` que adjunte el JWT a cada request:

```typescript
// El interceptor de request debe agregar:
config.headers.Authorization = `Bearer ${localStorage.getItem('auth_token')}`;
```

Esto es **imprescindible** para que en el futuro los microservicios `transaction` y `report` puedan validar el token JWT y extraer el `userId` del mismo, en lugar de confiar en el campo `userId` del body.

---

## 6. Resumen de Cambios por Categoría

### 6.1 Archivos del Frontend a MODIFICAR

| # | Archivo | Cambio |
|---|---------|--------|
| 1 | `core/config/dependencies.ts` | Cambiar `new FirebaseAuthRepository()` → `new ApiAuthRepository()` |
| 2 | `core/api/HttpClient.ts` | Agregar `'auth'` a `ServiceType`, agregar case en `getBaseURL()`, agregar interceptor de JWT |
| 3 | `core/constants/app.constants.ts` | Agregar `AUTH: '/v1/auth'` a `API_ENDPOINTS` |
| 4 | `shared/types/index.ts` | Eliminar import de `firebase/auth` y función `mapFirebaseUser` |
| 5 | `vite-env.d.ts` | Eliminar 6 vars `VITE_FIREBASE_*`, agregar `VITE_API_AUTH_URL` |
| 6 | `.env.example` | Eliminar 6 vars Firebase, agregar `VITE_API_AUTH_URL` |
| 7 | `modules/auth/store/useUserStore.ts` | Adaptar `initAuthListener` para usar token check + `/auth/me` |
| 8 | `modules/auth/hooks/useAuthInitialization.ts` | Adaptar para nueva lógica de verificación de sesión |

### 6.2 Archivos del Frontend a ELIMINAR

| # | Archivo |
|---|---------|
| 1 | `core/config/firebase.config.ts` |
| 2 | `infrastructure/auth/FirebaseAuthRepository.ts` (al finalizar migración) |
| 3 | `core/config/__mocks__/firebase.config.ts` |
| 4 | `test/__mocks__/firebase/app.ts` |
| 5 | `test/__mocks__/firebase/auth.ts` |

### 6.3 Archivos del Frontend a CREAR

| # | Archivo |
|---|---------|
| 1 | `infrastructure/auth/ApiAuthRepository.ts` |

### 6.4 Archivos de Infraestructura a MODIFICAR

| # | Archivo | Cambio |
|---|---------|--------|
| 1 | `docker-compose.yml` (raíz) | Agregar `mysql-auth` + `auth` services; eliminar build args Firebase del frontend |
| 2 | `package.json` (Frontend) | Eliminar `"firebase": "12.9.0"` |

### 6.5 Backend Auth — Archivos NUEVOS a CREAR

Toda la carpeta `app/backend-microservice/auth/` (detallada en §4.3).

---

## 7. Plan de Migración — Git Workflow

### Fase 0: Preparación (Branch: `feature/auth-backend-scaffold`)

```
Objetivo: Crear el microservicio Auth sin tocar el frontend.
Duración estimada: 2-3 días
Base: develop
```

**Tareas:**
1. Crear estructura de carpetas `app/backend-microservice/auth/`
2. Inicializar proyecto Spring Boot (`pom.xml`, `AuthApplication.java`, `application.yaml`)
3. Implementar entidad `User` + `UserRepository`
4. Implementar `AuthService` + `AuthServiceImpl` (register, login, getCurrentUser)
5. Implementar `JwtTokenProvider` (generación y validación de tokens)
6. Implementar `AuthController` con los 4 endpoints
7. Implementar `GlobalExceptionHandler` (copiar patrón de `transaction`)
8. Implementar `CorsConfig` (copiar patrón de `transaction`)
9. Implementar `SecurityConfig` (rutas públicas vs protegidas)
10. Escribir tests: Unit (`AuthServiceImplTest`, `JwtTokenProviderTest`) + Integration (`@WebMvcTest AuthControllerTest`)
11. Crear `Dockerfile` (copiar patrón de `transaction`)
12. Agregar `mysql-auth` y `auth` al `docker-compose.yml` raíz

**PR hacia:** `develop`  
**Criterio de merge:** CI verde, backend funcional independiente, **frontend intacto**.

---

### Fase 1: Adaptador Frontend (Branch: `feature/auth-frontend-adapter`)

```
Objetivo: Crear ApiAuthRepository y hacer el swap en dependencies.ts
Duración estimada: 1-2 días
Base: develop (con Fase 0 ya mergeada)
```

**Tareas:**
1. Crear `infrastructure/auth/ApiAuthRepository.ts`
2. Agregar `'auth'` a `HttpClient.ServiceType`
3. Agregar `VITE_API_AUTH_URL` a env vars
4. Adaptar `useUserStore.initAuthListener()` para token-based auth
5. Adaptar `useAuthInitialization.ts`
6. Actualizar `app.constants.ts` con endpoint AUTH
7. **NO cambiar `dependencies.ts` todavía** — Mantener Firebase como default
8. Agregar feature flag o variable de entorno para elegir repo: `VITE_AUTH_PROVIDER=api|firebase`
9. Escribir tests para `ApiAuthRepository` (mocking Axios)

**PR hacia:** `develop`  
**Criterio de merge:** CI verde. Feature flag permite probar ambos proveedores.

---

### Fase 2: Swap & Cleanup (Branch: `feature/auth-remove-firebase`)

```
Objetivo: Hacer el switch definitivo y eliminar Firebase
Duración estimada: 1 día
Base: develop (con Fase 0 y Fase 1 ya mergeadas)
```

**Tareas:**
1. Cambiar `dependencies.ts`: `new ApiAuthRepository()`
2. Eliminar feature flag
3. Eliminar `firebase.config.ts`
4. Eliminar `FirebaseAuthRepository.ts`
5. Eliminar mocks de Firebase
6. Eliminar `mapFirebaseUser` de `shared/types/index.ts`
7. Eliminar `firebase` de `package.json`
8. Eliminar vars `VITE_FIREBASE_*` de `.env.example`, `vite-env.d.ts`, `Dockerfile`, `docker-compose.yml`
9. Ejecutar `pnpm install` para regenerar lockfile sin Firebase
10. Test de regresión completo (todos los tests de auth, manual smoke test)

**PR hacia:** `develop`  
**Criterio de merge:** CI verde. Smoke test manual confirmando registro, login, logout, persistencia de sesión.

---

### Fase 3 (Futura): Seguridad E2E (Branch: `feature/auth-jwt-validation`)

```
Objetivo: Los microservicios transaction y report validan el JWT
Duración estimada: 2-3 días
Base: develop (con Fases 0-2 mergeadas)
```

**Tareas:**
1. Agregar `spring-boot-starter-security` + `jjwt` a `transaction` y `report`
2. Implementar `JwtAuthenticationFilter` en ambos servicios
3. Extraer `userId` del token en lugar del request body
4. El frontend envía `Authorization: Bearer <token>` en cada request
5. Eliminar `userId` del `TransactionRequest` (ya no lo envía el frontend)
6. Actualizar tests

> ⚠️ **Esta fase cambia el contrato de la API de transacciones y reportes**. Requiere coordinación con el frontend.

---

## 8. Diagrama de Arquitectura Objetivo

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              FRONTEND                                      │
│                     React 19 + TypeScript 5.9                              │
│                                                                             │
│   ┌──────────────┐  ┌──────────────┐  ┌──────────┐  ┌──────────────────┐  │
│   │  Auth Module  │  │ Transactions │  │  Reports │  │   Home Module    │  │
│   │(ApiAuthRepo)  │  │   Module     │  │  Module  │  │                  │  │
│   └──────┬───────┘  └──────┬───────┘  └─────┬────┘  └──────────────────┘  │
│          │                 │                │                               │
│          │     Axios HttpClient (con JWT interceptor)                      │
└──────────┼─────────────────┼────────────────┼──────────────────────────────┘
           │                 │                │
      Port 8083          Port 8081         Port 8082
           │                 │                │
           ▼                 ▼                ▼
┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐
│   AUTH SERVICE   │  │   TRANSACTION    │  │     REPORT       │
│  (Spring Boot)   │  │    SERVICE       │  │    SERVICE       │
│                  │  │  (Spring Boot)   │  │  (Spring Boot)   │
│  Controller      │  │                  │  │                  │
│      ↓           │  │  Controller      │  │  Controller      │
│  Service         │  │       ↓          │  │       ↓          │
│      ↓           │  │  Service         │  │  Service         │
│  JwtProvider     │  │       ↓          │  │       ↑          │
│      ↓           │  │  Repository      │  │  Repository      │
│  Repository      │  │       ↓          │  │       ↑          │
│      ↓           │  │  EventPublish    │  │  Consumer  ◄─────┤
└──────┼───────────┘  └───────┼──────────┘  └──────────────────┘
       │                      │                      ▲
       ▼                      ▼                      │
┌──────────────┐   ┌──────────────┐         ┌──────────────┐
│ mysql-auth   │   │ mysql-       │         │ mysql-       │
│ auth_db      │   │ transactions │         │ reports      │
│ (Port 3309)  │   │ (Port 3307)  │         │ (Port 3308)  │
└──────────────┘   └──────────────┘         └──────────────┘
```

---

## 9. Riesgos y Mitigaciones

| # | Riesgo | Probabilidad | Impacto | Mitigación |
|---|--------|-------------|---------|------------|
| R1 | Pérdida de sesiones activas al hacer el swap | Alta | Bajo | Los usuarios simplemente deberán re-loguearse. Comunicar vía release notes. |
| R2 | Datos existentes con Firebase UIDs | Media | Alto | Si hay datos en producción, crear script de migración que reasigne `userId` en `transactions` y `reports` tables. |
| R3 | Google OAuth no disponible en Phase 1 | Alta | Medio | Comunicar a usuarios. UI debe ocultar botón de Google temporalmente, no solo deshabilitarlo. |
| R4 | JWT secret comprometido | Baja | Crítico | Usar variable de entorno, nunca hardcodear. Implementar rotación de secrets. |
| R5 | Microservicios `transaction`/`report` no validan JWT (hasta Fase 3) | Alta (temporal) | Medio | Aceptable como riesgo temporal. Priorizar Fase 3 después del swap. |

---

## 10. Checklist de Aceptación Final

- [ ] Backend Auth levanta en Docker con `docker compose up`
- [ ] `POST /api/v1/auth/register` crea usuario y retorna JWT
- [ ] `POST /api/v1/auth/login` autentica y retorna JWT
- [ ] `GET /api/v1/auth/me` retorna datos del usuario desde JWT
- [ ] Frontend se conecta al backend Auth en lugar de Firebase
- [ ] Login con email/password funciona end-to-end
- [ ] Registro funciona end-to-end
- [ ] Logout limpia el estado y redirige a login
- [ ] Refresh de página mantiene la sesión (token en localStorage)
- [ ] Rutas protegidas redirigen a login si no hay token
- [ ] Dependencia `firebase` eliminada del `package.json`
- [ ] Todas las variables `VITE_FIREBASE_*` eliminadas
- [ ] CI/CD pipeline pasa en verde
- [ ] Tests de auth actualizados y pasando
