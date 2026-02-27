# TEST_PLAN_V3.md — Plan de Pruebas Formal
### Budget Management App — Consolidación de Calidad Post-MVP
> **Tipo de Documento:** Plan de Pruebas Formal (V3 — Integración como prioridad, gestión de riesgos)
> **Estándar de Referencia:** IEEE 829 / ISTQB Foundation Level v4.0
> **Alcance:** Infraestructura NFR · Edición de Transacciones · Gestión de Reportes · Exportación PDF
> **Idioma:** Español

---

## SECCIÓN 1 — Control del Documento

### 1.1 Historial de Versiones

| Versión | Fecha | Autor | Cambios |
|---|---|---|---|
| V1 | 2026-02-10 | Equipo QA | Plan de pruebas inicial. Cubrió US-017 a US-022 (eliminación de reportes, recalculación manual, descarga de PDF). Enfoque TDD con principios ISTQB, partición de equivalencia, análisis de valores límite y tablas de decisión. |
| V2 | 2026-02-19 | Equipo QA | Escenarios Gherkin refinados por historia. Se agregaron técnicas de diseño de pruebas (partición, BVA, tablas de decisión). Guía del ciclo de commits TDD ampliada por escenario. Cobertura de endpoints limitada a operaciones de nivel de reporte. |
| V3 | 2026-02-24 | QA Lead | **Este documento.** Plan evolucionado para cubrir el backlog refinado completo. Se agregaron historias de infraestructura NFR (NFR-001 a NFR-004), edición de transacciones (US-023) y sincronización automática (US-024). Se eliminaron US-019 y US-020 (recalculación manual eliminada del backlog). Se incorporó análisis INVEST formal, sección dedicada de Gestión de Riesgos y cobertura completa de pruebas de integración por endpoint REST. |

### 1.2 Propósito del Documento

Este documento define la estrategia de calidad de la Budget Management App durante su fase de consolidación post-MVP. Sirve como referencia autoritativa para todas las actividades de planificación, ejecución y aprobación de pruebas, cubriendo el backlog refinado completo definido en `refactor-new-stories.md`.

**Declaración de Alcance:** Este plan cubre todas las historias de usuario y requerimientos no funcionales introducidos en el backlog refinado, con énfasis particular en las pruebas de integración de la API REST. Reemplaza a V2 en todas las secciones donde el contenido se superpone e introduce nuevas secciones para gestión de riesgos y cumplimiento formal del principio INVEST.

### 1.3 Cambios de V2 a V3

| Área | V2 | V3 |
|---|---|---|
| Cobertura de historias | US-017, US-018, US-019, US-020, US-021, US-022 | NFR-001, NFR-002, NFR-003, NFR-004, US-017, US-018, US-021, US-022, US-023, US-024 |
| Historias eliminadas | N/A | US-019 (recalculación manual — eliminada), US-020 (notificación de diferencias — eliminada) |
| Pruebas de integración | Descripción conceptual | Tablas completas de casos de prueba por endpoint |
| Gestión de riesgos | No presente | Sección 5 completa con matrices de riesgos de proyecto y producto |
| Análisis INVEST | No presente | Sección 4 completa por historia |
| Arquitectura | Firebase (legado) | PostgreSQL + JWT + Docker (decisiones cerradas) |

---

## SECCIÓN 2 — Visión General del Plan de Pruebas

### 2.1 Contexto del Proyecto y Objetivos

La Budget Management App es un sistema de gestión financiera personal cuyo backend está expuesto como una API REST. El sistema permite a los usuarios registrados registrar transacciones financieras, generar reportes mensuales automáticamente y exportar resúmenes financieros en formato PDF.

**Esta fase de pruebas cubre el sprint de consolidación de calidad post-MVP.** Las decisiones arquitectónicas principales están cerradas:

| Decisión | Detalle |
|---|---|
| Persistencia | PostgreSQL (Firebase completamente eliminado) |
| Autenticación | JWT (JSON Web Tokens) — todos los endpoints protegidos |
| Interfaz | API REST con convenciones HTTP estándar |
| Contenerización | Docker + Docker Compose (obligatorio) |
| Actualización de Reportes | Automática, por eventos de cambio en transacciones |
| Edición de Transacciones | Soportada con propagación automática a reportes |

**Objetivos de Prueba:**
1. Validar que todos los nuevos endpoints de la API REST se comportan según sus criterios de aceptación.
2. Verificar la integridad de datos durante y después de la migración de Firebase a PostgreSQL.
3. Confirmar que la autenticación JWT y el aislamiento de datos por usuario se aplican en todo el sistema.
4. Garantizar que la sincronización automática de reportes se dispara correctamente ante todas las mutaciones de transacciones.
5. Validar las salidas de generación de PDF para exportaciones individuales y por rango.
6. Identificar y mitigar riesgos de calidad antes de que el sistema escale a producción.

### 2.2 Alcance

**Dentro del Alcance:**
- Todos los endpoints de la API REST definidos en `refactor-new-stories.md`
- Flujos de autenticación y autorización con JWT
- Persistencia e integridad de datos en PostgreSQL
- Recalculación automática de reportes ante creación/edición/eliminación de transacciones
- Generación y descarga de PDF (individual y por rango)
- Validación de la contenerización Docker
- Manejo de errores y cumplimiento de códigos de estado HTTP

**Fuera del Alcance:**
- Pruebas de frontend/UI (no se define interfaz en el backlog actual)
- Pruebas de carga y estrés de rendimiento
- Sistema legado Firebase (migración asumida como completada antes de la ejecución de pruebas)
- Pruebas de cliente móvil
- Integraciones con pasarelas de pago de terceros
- Endpoints de recalculación manual (US-019 y US-020 — eliminados del backlog)

### 2.3 Enfoque y Filosofía de Pruebas

Este plan adopta un **enfoque shift-left, basado en riesgo, con integración como prioridad**:

- **Shift-Left:** Los casos de prueba se diseñan antes de comenzar la implementación (compatible con TDD). Los escenarios Gherkin de los criterios de aceptación son la fuente principal de verdad para el diseño de pruebas.
- **Basado en Riesgo:** La cobertura de pruebas es proporcional al impacto en el negocio y a la probabilidad de defectos. Las historias de prioridad crítica reciben la mayor densidad de casos de prueba.
- **Integración como Prioridad:** Dada la arquitectura orientada a microservicios y la sincronización de reportes por eventos, las pruebas de integración son la puerta de calidad principal. Las pruebas unitarias apoyan pero no reemplazan la cobertura de integración.
- **Seguridad por Defecto:** Cada prueba de integración de endpoint incluye un grupo obligatorio de escenarios de autenticación y autorización. Ningún endpoint se considera probado sin validación de autenticación.

### 2.4 Criterios de Entrada y Salida

#### Pruebas Unitarias

| | Criterios |
|---|---|
| **Entrada** | El componente de código está implementado y compila sin errores. El framework de pruebas está configurado. |
| **Salida** | Todas las pruebas unitarias pasan. Cobertura de líneas ≥ 80% en clases de lógica de negocio. Sin fallos de prueba sin resolver. |

#### Pruebas de Integración

| | Criterios |
|---|---|
| **Entrada** | El endpoint objetivo está desplegado en un entorno de prueba basado en Docker. Migraciones del esquema PostgreSQL aplicadas. Middleware JWT activo. Datos de prueba sembrados por suite. |
| **Salida** | Todos los casos de prueba de integración de alta prioridad (P1) pasan. ≥ 90% de los de prioridad media (P2) pasan. Todos los escenarios de autenticación pasan. Sin defectos P1 abiertos. |

#### Pruebas End-to-End

| | Criterios |
|---|---|
| **Entrada** | Todas las suites de prueba de integración pasan. El stack Docker Compose completo está saludable. |
| **Salida** | Todos los flujos de usuario críticos se ejecutan sin error. Sin regresiones respecto a los escenarios de prueba de V2. |

#### Pruebas de Regresión

| | Criterios |
|---|---|
| **Entrada** | Cualquier cambio de código que afecte la lógica de reportes, la autenticación o el procesamiento de transacciones. |
| **Salida** | Todos los casos de prueba de regresión pasan. Sin nuevos fallos introducidos. |

---

## SECCIÓN 3 — Niveles y Estrategia de Pruebas

### 3.1 Pruebas Unitarias

**Alcance y Responsabilidad:** A cargo del equipo de desarrollo. Cada desarrollador escribe pruebas unitarias para los componentes de lógica que implementa, dirigidas a las reglas de negocio, validadores y cálculos de la capa de servicio.

**Objetivos de Cobertura:**

| Capa | Objetivo de Cobertura |
|---|---|
| Capa de servicio (lógica de negocio) | ≥ 80% cobertura de líneas |
| Validadores y parsers | ≥ 95% cobertura de líneas |
| Mapeadores y transformadores de datos | ≥ 75% cobertura de líneas |
| Capa de controlador (solo enrutamiento) | ≥ 60% cobertura de líneas |

**Herramientas y Frameworks:**

| Propósito | Herramienta |
|---|---|
| Ejecutor de pruebas | Jest (Node.js) o JUnit 5 (Java) según lenguaje del backend |
| Mocking | Jest Mocks / Mockito |
| Reporte de cobertura | Istanbul (Jest) / JaCoCo (Java) |

**Objetivos Clave de Prueba Unitaria (derivados de las historias):**

| Unidad Bajo Prueba | Historia | Qué Verificar |
|---|---|---|
| Validador de formato de período (`yyyy-MM`) | US-017, US-018, US-021, US-022 | Formato válido, formato inválido, nulo/vacío |
| Validador de rango de fechas (`from` ≤ `to`) | US-018, US-022 | Rango válido, rango invertido, rango de un solo mes |
| Detector de período actual | US-017, US-018 | Identifica correctamente el período `yyyy-MM` actual |
| Extractor de claim JWT (`userId`) | NFR-002 | Token válido, token expirado, token malformado |
| Verificador de propiedad de reporte | US-017, US-018 | Retorna booleano correcto para reportes propios vs. ajenos |
| Validador de monto de transacción | US-023 | Monto > 0, monto = 0, monto negativo |
| Recalculador de totales de reporte | US-024 | SUM correcto de ingresos/gastos tras mutación |
| Constructor de nombre de archivo PDF | US-021, US-022 | `reporte-yyyy-MM.pdf`, `resumen-yyyy-MM_yyyy-MM.pdf` |

---

### 3.2 Pruebas de Integración (FOCO PRINCIPAL)

**Entorno:** Stack Docker Compose con servicios `api` y `postgres`. Todas las solicitudes realizadas mediante cliente HTTP (ej. Supertest, RestAssured o Postman/Newman).

**Convenciones de Datos de Prueba:**
- `usuario-a@test.com` / `usuario-b@test.com` — dos usuarios registrados distintos para pruebas de aislamiento
- `txn-uuid-123` — transacción propiedad del usuario-a
- `txn-uuid-456` — transacción propiedad del usuario-b
- Período `2025-03` — período histórico con reporte existente, sin conflicto activo
- Período `2026-02` — período actual (según fecha del documento)
- JWT_VALIDO_A — JWT válido para el usuario-a
- JWT_VALIDO_B — JWT válido para el usuario-b
- JWT_EXPIRADO — JWT sintácticamente válido pero expirado
- JWT_MALFORMADO — cadena inválida arbitraria

---

#### 3.2.1 Endpoint: `POST /api/v1/auth/register`

**Historia:** NFR-002
**Descripción:** Registra una nueva cuenta de usuario. Persiste al usuario con contraseña hasheada en PostgreSQL y retorna un JWT válido.
**Precondiciones:** PostgreSQL en ejecución, tabla `users` existente, ningún usuario previo con el mismo correo.

| ID CdP | Descripción | Entrada | Resultado Esperado | Estado HTTP | Prioridad |
|---|---|---|---|---|---|
| IT-AUTH-001 | Registro exitoso de nuevo usuario | `{ email: "nuevo@test.com", password: "Seguro123!" }` | Usuario persistido en BD con contraseña hasheada; JWT retornado en el cuerpo de la respuesta | 201 | P1 |
| IT-AUTH-002 | Registro con correo duplicado | `{ email: "existente@test.com", password: "Seguro123!" }` | Mensaje de error: "El correo ya está registrado" | 409 | P1 |
| IT-AUTH-003 | Registro con formato de correo inválido | `{ email: "no-es-correo", password: "Seguro123!" }` | Error de validación indicando correo inválido | 400 | P1 |
| IT-AUTH-004 | Registro con contraseña vacía | `{ email: "nuevo@test.com", password: "" }` | Error de validación: contraseña requerida | 400 | P1 |
| IT-AUTH-005 | Registro con campos faltantes | `{}` | Error de validación listando todos los campos requeridos | 400 | P2 |
| IT-AUTH-006 | Verificar que la contraseña está hasheada en BD | Registro válido, luego consulta a BD | La columna `password` NO debe ser igual al texto plano ingresado | N/A | P1 |
| IT-AUTH-007 | El JWT retornado contiene el claim `userId` | Registro válido | El payload del JWT decodificado contiene `userId` correspondiente al usuario recién creado | 201 | P1 |

**Ruta Feliz:** IT-AUTH-001, IT-AUTH-007
**Casos Límite:** IT-AUTH-005
**Escenarios de Error:** IT-AUTH-002, IT-AUTH-003, IT-AUTH-004
**Escenarios de Seguridad:** IT-AUTH-006, IT-AUTH-007

---

#### 3.2.2 Endpoint: `POST /api/v1/auth/login`

**Historia:** NFR-002
**Descripción:** Autentica a un usuario existente con correo y contraseña. Retorna un JWT con el `userId` para uso en solicitudes posteriores.
**Precondiciones:** Usuario con `usuario-a@test.com` registrado y persistido en PostgreSQL.

| ID CdP | Descripción | Entrada | Resultado Esperado | Estado HTTP | Prioridad |
|---|---|---|---|---|---|
| IT-AUTH-010 | Login exitoso con credenciales válidas | `{ email: "usuario-a@test.com", password: "Seguro123!" }` | JWT retornado; respuesta 200 | 200 | P1 |
| IT-AUTH-011 | Login con contraseña incorrecta | `{ email: "usuario-a@test.com", password: "Incorrecto!" }` | Error: "Credenciales inválidas" | 401 | P1 |
| IT-AUTH-012 | Login con correo inexistente | `{ email: "fantasma@test.com", password: "Seguro123!" }` | Error: "Credenciales inválidas" | 401 | P1 |
| IT-AUTH-013 | El JWT retornado tiene claim de expiración | Login válido | El JWT decodificado contiene claim `exp` con timestamp futuro | 200 | P1 |
| IT-AUTH-014 | El JWT contiene el `userId` correcto | Login válido para usuario-a | El `userId` decodificado coincide con el ID del usuario-a en BD | 200 | P1 |
| IT-AUTH-015 | Login con campos faltantes en el body | `{}` | Error de validación | 400 | P2 |
| IT-AUTH-016 | La respuesta de login no expone la contraseña | Login válido | El cuerpo de la respuesta no contiene el campo `password` | 200 | P1 |

**Ruta Feliz:** IT-AUTH-010, IT-AUTH-013, IT-AUTH-014
**Casos Límite:** IT-AUTH-015
**Escenarios de Error:** IT-AUTH-011, IT-AUTH-012
**Escenarios de Seguridad:** IT-AUTH-016

---

#### 3.2.3 Endpoint: `DELETE /api/v1/reports/{period}`

**Historia:** US-017
**Descripción:** Elimina el reporte financiero de un período mensual específico propiedad del usuario autenticado. No elimina las transacciones asociadas. Bloqueado para el período actual si existen transacciones activas.
**Precondiciones:** usuario-a autenticado con JWT_VALIDO_A; reporte para `2025-03` existe y pertenece a usuario-a; reporte para `2026-02` (período actual) existe con transacciones activas.

| ID CdP | Descripción | Entrada | Resultado Esperado | Estado HTTP | Prioridad |
|---|---|---|---|---|---|
| IT-RPT-001 | Eliminación exitosa de reporte histórico propio | `DELETE /api/v1/reports/2025-03`, JWT_VALIDO_A | 200 con mensaje de éxito; reporte ya no existe en BD; transacciones de `2025-03` sin afectación | 200 | P1 |
| IT-RPT-002 | Transacciones preservadas tras eliminación del reporte | Igual que IT-RPT-001, luego `GET /api/v1/transactions?period=2025-03` | Las transacciones siguen existiendo; el conteo no cambia | 200 | P1 |
| IT-RPT-003 | Eliminación bloqueada para período actual con transacciones activas | `DELETE /api/v1/reports/2026-02`, JWT_VALIDO_A | Error: "No es posible eliminar el reporte del período en curso con transacciones activas" | 409 | P1 |
| IT-RPT-004 | Eliminación de reporte inexistente | `DELETE /api/v1/reports/2020-01`, JWT_VALIDO_A | Error: "Reporte no encontrado" | 404 | P1 |
| IT-RPT-005 | Intento de eliminar reporte de otro usuario | `DELETE /api/v1/reports/2025-03`, JWT_VALIDO_B (usuario-b no tiene reporte `2025-03`) | Error: "Reporte no encontrado" | 404 | P1 |
| IT-RPT-006 | Solicitud sin header Authorization | `DELETE /api/v1/reports/2025-03`, sin JWT | Error: "Token de autenticación requerido" | 401 | P1 |
| IT-RPT-007 | Solicitud con JWT expirado | `DELETE /api/v1/reports/2025-03`, JWT_EXPIRADO | Error: "Token expirado" | 401 | P1 |
| IT-RPT-008 | Formato de período inválido | `DELETE /api/v1/reports/03-2025`, JWT_VALIDO_A | Error de validación: formato de período inválido | 400 | P2 |
| IT-RPT-009 | Eliminación de período actual SIN transacciones activas | `DELETE /api/v1/reports/2026-02`, JWT_VALIDO_A (sin transacciones en `2026-02`) | Eliminación exitosa | 200 | P2 |
| IT-RPT-010 | Eliminación del último reporte existente | `DELETE /api/v1/reports/{único-período}`, JWT_VALIDO_A | Reporte eliminado; colección de reportes del usuario queda vacía | 200 | P2 |

**Ruta Feliz:** IT-RPT-001, IT-RPT-002
**Casos Límite:** IT-RPT-003, IT-RPT-009, IT-RPT-010
**Escenarios de Error:** IT-RPT-004, IT-RPT-008
**Escenarios de Autenticación/Autorización:** IT-RPT-005, IT-RPT-006, IT-RPT-007

---

#### 3.2.4 Endpoint: `DELETE /api/v1/reports?from={period}&to={period}`

**Historia:** US-018
**Descripción:** Elimina todos los reportes financieros dentro de un rango de períodos especificado, propiedad del usuario autenticado. El período actual se excluye si tiene transacciones activas. Los reportes de otros usuarios no se ven afectados.
**Precondiciones:** usuario-a tiene reportes para `2024-01` a `2024-06`; período actual es `2026-02` con transacciones activas.

| ID CdP | Descripción | Entrada | Resultado Esperado | Estado HTTP | Prioridad |
|---|---|---|---|---|---|
| IT-RPT-020 | Eliminación masiva exitosa de rango completo | `DELETE /api/v1/reports?from=2024-01&to=2024-06`, JWT_VALIDO_A | 200; "6 reportes eliminados correctamente"; los 6 reportes eliminados de la BD | 200 | P1 |
| IT-RPT-021 | Rango incluye período actual con transacciones activas | `DELETE /api/v1/reports?from=2025-12&to=2026-02`, JWT_VALIDO_A | 200; "2 reportes eliminados. El período 2026-02 fue excluido por tener transacciones activas" | 200 | P1 |
| IT-RPT-022 | Rango sin reportes coincidentes | `DELETE /api/v1/reports?from=2020-01&to=2020-06`, JWT_VALIDO_A | Error: "No se encontraron reportes en el rango seleccionado" | 404 | P1 |
| IT-RPT-023 | Rango invertido (`from` > `to`) | `DELETE /api/v1/reports?from=2025-06&to=2025-01`, JWT_VALIDO_A | Error: "El período de inicio debe ser anterior al período de fin" | 400 | P1 |
| IT-RPT-024 | Rango de exactamente un mes | `DELETE /api/v1/reports?from=2024-03&to=2024-03`, JWT_VALIDO_A | 200; "1 reporte eliminado correctamente" | 200 | P2 |
| IT-RPT-025 | Rango que solo cubre reportes de otro usuario | `DELETE /api/v1/reports?from=2024-01&to=2024-06`, JWT_VALIDO_B | 404; sin reportes encontrados en el rango para usuario-b | 404 | P1 |
| IT-RPT-026 | Solicitud sin header Authorization | Sin JWT | Error: "Token de autenticación requerido" | 401 | P1 |
| IT-RPT-027 | Formato inválido en parámetro `from` | `from=enero&to=2024-06` | Error de validación: formato de período inválido | 400 | P2 |
| IT-RPT-028 | Parámetro `from` o `to` faltante | Solo `from=2024-01` proporcionado | Error de validación: parámetros requeridos faltantes | 400 | P2 |
| IT-RPT-029 | Rango que termina el mes anterior al período actual | `from=2025-01&to=2026-01` | Todos los reportes del rango eliminados; `2026-02` intacto | 200 | P2 |

**Ruta Feliz:** IT-RPT-020, IT-RPT-024
**Casos Límite:** IT-RPT-021, IT-RPT-029
**Escenarios de Error:** IT-RPT-022, IT-RPT-023, IT-RPT-027, IT-RPT-028
**Escenarios de Autenticación/Autorización:** IT-RPT-025, IT-RPT-026

---

#### 3.2.5 Endpoint: `PUT /api/v1/transactions/{transactionId}`

**Historia:** US-023
**Descripción:** Actualiza uno o más campos de una transacción existente propiedad del usuario autenticado. Campos editables: `amount`, `category`, `description`, `date`. Dispara la resincronización automática del reporte (US-024) al completarse con éxito.
**Precondiciones:** usuario-a es propietario de la transacción `txn-uuid-123` en el período `2025-03`; usuario-b es propietario de `txn-uuid-456`.

| ID CdP | Descripción | Entrada | Resultado Esperado | Estado HTTP | Prioridad |
|---|---|---|---|---|---|
| IT-TXN-001 | Actualización completa de monto y categoría | `PUT /api/v1/transactions/txn-uuid-123`, body `{ amount: 150.00, category: "Alimentación" }`, JWT_VALIDO_A | 200; datos actualizados retornados; cambios persistidos en BD | 200 | P1 |
| IT-TXN-002 | Actualización parcial (solo descripción) | `PUT /api/v1/transactions/txn-uuid-123`, body `{ description: "Actualizado" }`, JWT_VALIDO_A | 200; solo `description` modificado; `amount`, `category`, `date` sin cambios | 200 | P1 |
| IT-TXN-003 | Idempotencia — mismo PUT enviado dos veces | Mismo body enviado dos veces | Ambas retornan 200; estado en BD idéntico tras la segunda llamada | 200 | P2 |
| IT-TXN-004 | Actualización de transacción inexistente | `PUT /api/v1/transactions/txn-uuid-999`, JWT_VALIDO_A | Error: "Transacción no encontrada" | 404 | P1 |
| IT-TXN-005 | Intento de actualizar transacción de otro usuario | `PUT /api/v1/transactions/txn-uuid-456`, JWT_VALIDO_A | Error: "Transacción no encontrada" | 404 | P1 |
| IT-TXN-006 | Monto igual a cero (inválido) | `{ amount: 0 }` | Error: "El monto debe ser mayor a cero" | 400 | P1 |
| IT-TXN-007 | Monto con valor negativo | `{ amount: -50.00 }` | Error: "El monto debe ser mayor a cero" | 400 | P1 |
| IT-TXN-008 | Solicitud sin header Authorization | Sin JWT | Error: "Token de autenticación requerido" | 401 | P1 |
| IT-TXN-009 | Solicitud con JWT expirado | JWT_EXPIRADO | Error: "Token expirado" | 401 | P1 |
| IT-TXN-010 | Body de solicitud vacío | `{}` | Error de validación: se requiere al menos un campo | 400 | P2 |
| IT-TXN-011 | El reporte se sincroniza automáticamente tras actualización de monto | Actualizar `txn-uuid-123` de 100 a 150; luego `GET /api/v1/reports/2025-03` | El total del reporte refleja el nuevo monto; balance actualizado | 200 | P1 |
| IT-TXN-012 | Cambio de fecha modifica el período — período origen decrementado | Mover `txn-uuid-123` de `2025-03` a `2025-04` | Total del reporte `2025-03` reducido; total del reporte `2025-04` incrementado | 200 | P1 |
| IT-TXN-013 | Cambio de fecha a período nuevo — reporte destino creado si no existe | Mover transacción a período sin reporte existente | Nuevo reporte para el período destino creado automáticamente | 200 | P1 |
| IT-TXN-014 | Formato de fecha inválido | `{ date: "03/2025/15" }` | Error de validación: formato de fecha inválido | 400 | P2 |

**Ruta Feliz:** IT-TXN-001, IT-TXN-002
**Casos Límite:** IT-TXN-003, IT-TXN-010, IT-TXN-012, IT-TXN-013
**Escenarios de Error:** IT-TXN-004, IT-TXN-006, IT-TXN-007, IT-TXN-014
**Escenarios de Autenticación/Autorización:** IT-TXN-005, IT-TXN-008, IT-TXN-009
**Escenarios de Sincronización Automática (US-024):** IT-TXN-011, IT-TXN-012, IT-TXN-013

---

#### 3.2.6 Endpoint: `GET /api/v1/reports/{period}/pdf`

**Historia:** US-021
**Descripción:** Genera y retorna un archivo PDF del reporte financiero para un período especificado. El PDF contiene: nombre del usuario, período, total de ingresos, total de gastos, balance neto y marca de tiempo de generación. El PDF refleja los datos en el momento de la descarga.
**Precondiciones:** usuario-a tiene un reporte para `2025-10`; no existe reporte para `2020-01`.

| ID CdP | Descripción | Entrada | Resultado Esperado | Estado HTTP | Prioridad |
|---|---|---|---|---|---|
| IT-PDF-001 | Descarga exitosa de PDF | `GET /api/v1/reports/2025-10/pdf`, JWT_VALIDO_A | 200; `Content-Type: application/pdf`; `Content-Disposition` contiene `reporte-2025-10.pdf` | 200 | P1 |
| IT-PDF-002 | El PDF contiene los campos requeridos | Descarga del PDF para `2025-10` | El PDF incluye: período, totalIngresos, totalGastos, balanceNeto, fechaGeneración | 200 | P1 |
| IT-PDF-003 | PDF de reporte inexistente | `GET /api/v1/reports/2020-01/pdf`, JWT_VALIDO_A | Error: "Reporte no encontrado" | 404 | P1 |
| IT-PDF-004 | El PDF refleja los datos más recientes de transacciones | Actualizar una transacción, luego descargar el PDF | El PDF muestra los valores recalculados, no datos desactualizados | 200 | P1 |
| IT-PDF-005 | Fallo interno durante la generación del PDF | Simular error de la librería PDF | Error: "No fue posible generar el PDF. Inténtalo más tarde" | 500 | P2 |
| IT-PDF-006 | Solicitud sin header Authorization | Sin JWT | Error: "Token de autenticación requerido" | 401 | P1 |
| IT-PDF-007 | Usuario-b no puede descargar el PDF del usuario-a | `GET /api/v1/reports/2025-10/pdf`, JWT_VALIDO_B | Error: "Reporte no encontrado" | 404 | P1 |
| IT-PDF-008 | Formato de período inválido | `GET /api/v1/reports/2025/pdf` | Error de validación | 400 | P2 |
| IT-PDF-009 | Reporte con valores en cero genera PDF válido | Reporte con $0.00 en ingresos y gastos | PDF generado correctamente con valores $0.00 | 200 | P2 |
| IT-PDF-010 | Formato del nombre de archivo en Content-Disposition | Descarga válida para `2025-10` | El valor del header contiene exactamente `reporte-2025-10.pdf` | 200 | P2 |

**Ruta Feliz:** IT-PDF-001, IT-PDF-002
**Casos Límite:** IT-PDF-004, IT-PDF-009
**Escenarios de Error:** IT-PDF-003, IT-PDF-005, IT-PDF-008
**Escenarios de Autenticación/Autorización:** IT-PDF-006, IT-PDF-007
**Validación de Contenido:** IT-PDF-010

---

#### 3.2.7 Endpoint: `GET /api/v1/reports/pdf?from={period}&to={period}`

**Historia:** US-022
**Descripción:** Genera y retorna un PDF consolidado que resume todos los reportes financieros dentro de un rango de períodos especificado. Incluye desglose por período y totales acumulados. Los períodos sin datos quedan anotados en el documento.
**Precondiciones:** usuario-a tiene reportes para `2025-01`, `2025-03` y `2025-05` (3 de 6 meses en el rango `2025-01` a `2025-06`).

| ID CdP | Descripción | Entrada | Resultado Esperado | Estado HTTP | Prioridad |
|---|---|---|---|---|---|
| IT-PDF-020 | Descarga exitosa de PDF consolidado para rango completo | `GET /api/v1/reports/pdf?from=2025-01&to=2025-06`, JWT_VALIDO_A | 200; `Content-Type: application/pdf`; `Content-Disposition` contiene `resumen-2025-01_2025-06.pdf` | 200 | P1 |
| IT-PDF-021 | El PDF contiene todos los períodos del rango | Igual que IT-PDF-020 con datos completos | El PDF incluye el desglose de cada uno de los 6 períodos más los totales acumulados | 200 | P1 |
| IT-PDF-022 | PDF con datos parciales en el rango | Rango `2025-01` a `2025-06`, solo existen 3 reportes | El PDF incluye los 3 períodos con datos; nota listando los 3 períodos faltantes (`2025-02`, `2025-04`, `2025-06`) | 200 | P1 |
| IT-PDF-023 | Rango sin reportes | `GET /api/v1/reports/pdf?from=2020-01&to=2020-06`, JWT_VALIDO_A | Error: "No existen reportes en el rango seleccionado" | 404 | P1 |
| IT-PDF-024 | Rango invertido (`from` > `to`) | `from=2025-06&to=2025-01` | Error: "El período de inicio debe ser anterior al período de fin" | 400 | P1 |
| IT-PDF-025 | Rango de un solo mes | `from=2025-01&to=2025-01` | 200; PDF con datos de un único período | 200 | P2 |
| IT-PDF-026 | Solicitud sin header Authorization | Sin JWT | Error: "Token de autenticación requerido" | 401 | P1 |
| IT-PDF-027 | Fallo interno durante la generación del PDF | Simular error de la librería PDF | Error: "No fue posible generar el PDF. Inténtalo más tarde" | 500 | P2 |
| IT-PDF-028 | Formato del nombre de archivo en Content-Disposition | Descarga válida para `2025-01` a `2025-06` | El header contiene exactamente `resumen-2025-01_2025-06.pdf` | 200 | P2 |
| IT-PDF-029 | Usuario-b no puede acceder al PDF por rango del usuario-a | JWT_VALIDO_B para un rango donde usuario-a tiene datos | 404; sin reportes encontrados | 404 | P1 |
| IT-PDF-030 | Parámetro `from` o `to` faltante | Solo `from=2025-01` proporcionado | Error de validación: parámetros requeridos faltantes | 400 | P2 |

**Ruta Feliz:** IT-PDF-020, IT-PDF-021
**Casos Límite:** IT-PDF-022, IT-PDF-025
**Escenarios de Error:** IT-PDF-023, IT-PDF-024, IT-PDF-027, IT-PDF-030
**Escenarios de Autenticación/Autorización:** IT-PDF-026, IT-PDF-029
**Validación de Contenido:** IT-PDF-028

---

### 3.3 Pruebas End-to-End

**Herramientas:** Stack Docker Compose completo + cliente de pruebas de integración HTTP (colección Newman/Postman o suite Supertest).
**Entorno:** Instancia PostgreSQL limpia con datos de prueba sembrados. API completa desplegada en Docker.

**Flujos Críticos de Usuario:**

| ID Flujo | Flujo de Usuario | Historias Cubiertas | Pasos |
|---|---|---|---|
| E2E-001 | Registro → Login → Crear Transacción → Verificar Auto-Reporte | NFR-002, US-024 | 1. Registrar nuevo usuario. 2. Login para obtener JWT. 3. POST transacción para `2026-02`. 4. GET reporte para `2026-02` — debe existir automáticamente. |
| E2E-002 | Login → Editar Transacción → Verificar Sincronización de Reporte | US-023, US-024 | 1. Login. 2. GET reporte `2025-03` — anotar totales. 3. PUT transacción `txn-uuid-123` con nuevo monto. 4. GET reporte `2025-03` — los totales deben estar actualizados. |
| E2E-003 | Login → Mover Transacción a Nuevo Período → Verificar Ambos Reportes Actualizados | US-023, US-024 | 1. Login. 2. PUT fecha de transacción de `2025-03` a `2025-04`. 3. GET `2025-03` — monto reducido. 4. GET `2025-04` — monto incrementado. |
| E2E-004 | Login → Eliminar Reporte → Verificar Transacciones Intactas | US-017 | 1. Login. 2. GET transacciones para `2025-03` — anotar conteo. 3. DELETE reporte `2025-03`. 4. GET transacciones para `2025-03` — conteo sin cambios. |
| E2E-005 | Login → Eliminación Masiva por Rango → Verificar Exclusión Parcial | US-018 | 1. Login. 2. DELETE rango `2025-12` a `2026-02`. 3. Verificar que el reporte `2026-02` sigue existiendo (tiene transacciones activas). |
| E2E-006 | Login → Descargar PDF → Verificar tras Edición de Transacción | US-021, US-023 | 1. Login. 2. Descargar PDF para `2025-03`. 3. Editar transacción. 4. Descargar PDF para `2025-03` nuevamente. 5. Verificar que el nuevo PDF refleja los valores actualizados. |
| E2E-007 | Login → Descargar PDF por Rango con Vacíos | US-022 | 1. Login. 2. GET `pdf?from=2025-01&to=2025-06` donde solo 3 meses tienen datos. 3. Verificar que el PDF incluye la nota sobre períodos faltantes. |
| E2E-008 | Aislamiento entre usuarios de extremo a extremo | NFR-002 | 1. Registrar usuario-a y usuario-b. 2. Usuario-a crea reportes. 3. Usuario-b intenta acceder a los recursos del usuario-a. 4. Todos los intentos retornan 404 o 401. |

---

### 3.4 Pruebas de Regresión

**Alcance de Regresión:** Cualquier cambio en los siguientes módulos dispara la ejecución de regresión:
- Middleware JWT (NFR-002) — re-ejecutar todos los escenarios de autenticación
- Cambios en el esquema PostgreSQL (NFR-001) — re-ejecutar todas las pruebas de persistencia
- Servicio de reportes (US-017, US-018, US-024) — re-ejecutar todas las pruebas de integración de reportes
- Servicio de transacciones (US-023) — re-ejecutar todas las pruebas de transacciones y sincronización automática

**Estrategia de Automatización:**
- Todos los casos de prueba de integración (Sección 3.2) están automatizados en CI y se ejecutan en cada pull request.
- Los flujos E2E están automatizados y se ejecutan al fusionar a la rama `main`.
- La suite de regresión etiquetada `@regression` se dispara de forma independiente cuando cambian los módulos especificados.
- Objetivo: tiempo de ejecución de regresión total < 15 minutos en CI.

---

## SECCIÓN 4 — Historias de Usuario: Análisis INVEST

> Todas las historias de usuario en `refactor-new-stories.md` incluyen tablas INVEST elaboradas por el equipo de desarrollo. Esta sección formaliza la revisión del equipo de QA, valida cada criterio, identifica posibles vacíos y confirma o reescribe los criterios de aceptación en formato Given/When/Then.

---

### NFR-001 — Migración de Persistencia de Firebase a PostgreSQL

| Criterio INVEST | Definición | Evaluación | Notas |
|---|---|---|---|
| Independiente | Se puede desarrollar sin depender de otras historias | **APROBADO** | Historia de nivel de infraestructura sin dependencias funcionales. Se define como bloqueante para otras, pero ella misma no tiene prerrequisitos. |
| Negociable | Los detalles pueden discutirse y ajustarse | **APROBADO** | Los detalles del esquema, índices y restricciones están explícitamente marcados como negociables. |
| Valiosa | Entrega valor claro al usuario o al negocio | **APROBADO** | Habilita transacciones ACID, elimina el vendor lock-in y desbloquea capacidades de consulta SQL avanzadas. |
| Estimable | El equipo puede estimar el esfuerzo con confianza | **APROBADO** | Alcance bien definido: diseño del esquema, scripts de migración, adaptación de repositorios, validación. |
| Pequeña | Cabe en un solo sprint | **APROBADO** | Limitada a la capa de datos; no modifica la lógica de negocio. Asignación al Sprint 1 apropiada. |
| Testeable | Existen criterios de aceptación claros | **APROBADO** | Los escenarios Gherkin cubren creación del esquema, validación CRUD, eliminación de Firebase e integridad referencial. |

**Resultado INVEST: TODOS APROBADOS** — No se requiere reescritura.

**Criterios de Aceptación (Confirmados):**

```gherkin
Dado que el contenedor de PostgreSQL está en ejecución
Cuando se ejecutan los scripts de migración
Entonces las tablas users, transactions y reports deben existir
Y los constraints de integridad referencial deben estar activos
Y los índices sobre userId y period deben estar creados

Dado que la migración fue ejecutada exitosamente
Cuando la API procesa POST /api/v1/transactions con datos válidos
Entonces la transacción debe persistirse en la tabla transactions de PostgreSQL
Y una consulta GET /api/v1/transactions/{id} debe retornar los datos insertados

Dado que la migración fue completada
Cuando se revisa la configuración del sistema
Entonces no deben existir dependencias, imports ni variables de entorno de Firebase
```

---

### NFR-002 — Autenticación y Autorización con JWT

| Criterio INVEST | Definición | Evaluación | Notas |
|---|---|---|---|
| Independiente | Se puede desarrollar sin depender de otras historias | **APROBADO** | La capa de autenticación es transversal y se implementa de forma independiente a la lógica de negocio. La dependencia de NFR-001 para persistencia de usuarios es un prerrequisito técnico, no una dependencia de historia. |
| Negociable | Los detalles pueden discutirse y ajustarse | **APROBADO** | Algoritmo (HS256/RS256), tiempo de expiración del token y claims son explícitamente negociables. |
| Valiosa | Entrega valor claro al usuario o al negocio | **APROBADO** | La seguridad es fundamental; el control de acceso y el aislamiento de datos por usuario son requerimientos centrales del negocio. |
| Estimable | El equipo puede estimar el esfuerzo con confianza | **APROBADO** | Alcance claramente acotado: endpoint de registro, endpoint de login, middleware de validación, extracción de userId. |
| Pequeña | Cabe en un solo sprint | **APROBADO** | Limitada a la capa de autenticación/autorización. Asignación al Sprint 1 apropiada. |
| Testeable | Existen criterios de aceptación claros | **APROBADO** | Los escenarios Gherkin cubren registro, login, acceso sin autenticación, tokens expirados y aislamiento entre usuarios. |

**Resultado INVEST: TODOS APROBADOS** — No se requiere reescritura.

**Criterios de Aceptación (Confirmados):**

```gherkin
Dado que un usuario nuevo envía POST /api/v1/auth/register con email y password válidos
Cuando el sistema procesa la solicitud
Entonces debe responder con 201 Created
Y el usuario debe persistirse con la contraseña hasheada
Y debe retornar un JWT válido en el cuerpo de la respuesta

Dado que un usuario autenticado envía una solicitud a un endpoint protegido sin header Authorization
Cuando el sistema procesa la solicitud
Entonces debe responder con 401 Unauthorized
Y el body debe contener "Token de autenticación requerido"

Dado que el usuario-a está autenticado
Cuando envía GET /api/v1/transactions
Entonces la respuesta debe contener únicamente las transacciones cuyo userId coincida con el claim del JWT del usuario-a
```

---

### NFR-003 — Contenerización del Sistema con Docker

| Criterio INVEST | Definición | Evaluación | Notas |
|---|---|---|---|
| Independiente | Se puede desarrollar sin depender de otras historias | **APROBADO** | Capa de infraestructura; el Dockerfile y el docker-compose son independientes de la lógica funcional. |
| Negociable | Los detalles pueden discutirse y ajustarse | **APROBADO** | Imágenes base, mapeo de puertos, configuración de volúmenes e intervalos de health-check son negociables. |
| Valiosa | Entrega valor claro al usuario o al negocio | **APROBADO** | Elimina fallos de entorno específicos, habilita CI/CD y garantiza paridad entre desarrollo y producción. |
| Estimable | El equipo puede estimar el esfuerzo con confianza | **APROBADO** | Alcance claramente acotado: Dockerfile (multi-stage), docker-compose.yml con servicios api y postgres, configuración .env. |
| Pequeña | Cabe en un solo sprint | **APROBADO** | La configuración de contenedores es una tarea de infraestructura enfocada. Sprint 1 apropiado. |
| Testeable | Existen criterios de aceptación claros | **APROBADO** | Verificable ejecutando `docker compose up -d` y validando health checks en todos los servicios. |

**Resultado INVEST: TODOS APROBADOS** — No se requiere reescritura.

**Criterios de Aceptación (Confirmados):**

```gherkin
Dado que existe un Dockerfile válido en el directorio raíz del proyecto
Cuando se ejecuta docker build -t budget-api .
Entonces la imagen debe construirse sin errores

Dado que docker-compose.yml define los servicios api y postgres
Cuando se ejecuta docker compose up -d
Entonces ambos contenedores deben alcanzar estado healthy
Y la API debe responder en el puerto configurado

Dado que existen datos en PostgreSQL
Cuando se ejecuta docker compose down && docker compose up
Entonces los datos previamente insertados deben seguir disponibles
```

---

### NFR-004 — Arquitectura de API REST con Estándares de Calidad

| Criterio INVEST | Definición | Evaluación | Notas |
|---|---|---|---|
| Independiente | Se puede desarrollar sin depender de otras historias | **APROBADO** | Los estándares de API son una preocupación transversal implementable de forma independiente. |
| Negociable | Los detalles pueden discutirse y ajustarse | **APROBADO** | Formato de errores, convenciones de paginación y estrategia de versionado son negociables con el equipo. |
| Valiosa | Entrega valor claro al usuario o al negocio | **APROBADO** | Reduce la fricción de integración, mejora la mantenibilidad y hace el testing predecible. |
| Estimable | El equipo puede estimar el esfuerzo con confianza | **APROBADO** | Acotado a: estandarización de estructura de respuestas, middleware de error handling, convenciones de códigos HTTP. |
| Pequeña | Cabe en un solo sprint | **APROBADO** | Solo afecta la capa de presentación de la API; Sprint 1 en paralelo con historias funcionales. |
| Testeable | Existen criterios de aceptación claros | **APROBADO** | Verificable inspeccionando la estructura de respuestas HTTP y los códigos de estado en cualquier endpoint. |

**Resultado INVEST: TODOS APROBADOS** — No se requiere reescritura.

**Criterios de Aceptación (Confirmados):**

```gherkin
Dado que cualquier endpoint procesa una solicitud válida
Cuando retorna una respuesta exitosa
Entonces el código HTTP debe ser 200, 201 o 204 según corresponda
Y el body debe seguir la estructura: { data, message, timestamp }

Dado que cualquier endpoint recibe una solicitud inválida
Cuando retorna una respuesta de error
Entonces el código HTTP debe ser 400, 401, 403, 404 o 500 según corresponda
Y el body debe seguir la estructura: { error, message, statusCode, timestamp }

Dado que la API está versionada
Cuando se accede a cualquier endpoint bajo /api/v1/*
Entonces todas las respuestas deben mantener el prefijo /api/v1 de forma consistente
```

---

### US-017 — Eliminar un Reporte Financiero de un Período

| Criterio INVEST | Definición | Evaluación | Notas |
|---|---|---|---|
| Independiente | Se puede desarrollar sin depender de otras historias | **APROBADO** | Operación DELETE sobre un solo registro; autónoma respecto a edición de reportes y descarga de PDF. |
| Negociable | Los detalles pueden discutirse y ajustarse | **APROBADO** | Las reglas de eliminación para el período actual y el texto de los mensajes son negociables. |
| Valiosa | Entrega valor claro al usuario o al negocio | **APROBADO** | Permite al usuario gestionar y curar su historial financiero. |
| Estimable | El equipo puede estimar el esfuerzo con confianza | **APROBADO** | CRUD estándar: validar propiedad → verificar reglas de período → DELETE → responder. |
| Pequeña | Cabe en un solo sprint | **APROBADO** | Un único endpoint, flujo claro. Sprint 2 apropiado. |
| Testeable | Existen criterios de aceptación claros | **APROBADO** | Los escenarios Gherkin cubren éxito, período bloqueado, no encontrado, acceso cruzado entre usuarios y sin autenticación. |

**Resultado INVEST: TODOS APROBADOS** — No se requiere reescritura.

**Criterios de Aceptación (Confirmados):**

```gherkin
Dado que el usuario está autenticado con un JWT válido
Y existe un reporte del período "2025-03" de su propiedad
Cuando envía DELETE /api/v1/reports/2025-03
Entonces el sistema debe responder con 200 OK
Y el reporte ya no debe existir en la base de datos
Y las transacciones del período "2025-03" deben permanecer intactas

Dado que el usuario está autenticado
Y el período actual es "2026-02" con transacciones activas
Cuando envía DELETE /api/v1/reports/2026-02
Entonces el sistema debe responder con 409 Conflict
Y el mensaje debe ser "No es posible eliminar el reporte del período en curso con transacciones activas"
```

---

### US-018 — Eliminación Masiva de Reportes por Rango de Período

| Criterio INVEST | Definición | Evaluación | Notas |
|---|---|---|---|
| Independiente | Se puede desarrollar sin depender de otras historias | **APROBADO** | La eliminación masiva es independiente de la eliminación individual y de las operaciones de PDF. |
| Negociable | Los detalles pueden discutirse y ajustarse | **APROBADO** | Límites del rango, comportamiento de exclusión parcial y formato del mensaje de respuesta son negociables. |
| Valiosa | Entrega valor claro al usuario o al negocio | **APROBADO** | Ahorra tiempo significativo al limpiar múltiples períodos históricos. |
| Estimable | El equipo puede estimar el esfuerzo con confianza | **APROBADO** | Extiende la lógica de US-017 a un rango validado; operación en lote de alcance acotado. |
| Pequeña | Cabe en un solo sprint | **APROBADO** | Un endpoint adicional con lógica de iteración sobre el rango. Asignación al Sprint 3 apropiada. |
| Testeable | Existen criterios de aceptación claros | **APROBADO** | Los escenarios cubren rango completo, exclusión parcial, rango vacío, rango inválido y autenticación. |

**Resultado INVEST: TODOS APROBADOS** — No se requiere reescritura.

**Criterios de Aceptación (Confirmados):**

```gherkin
Dado que el usuario está autenticado
Y existen reportes propios para los períodos "2024-01" a "2024-06"
Cuando envía DELETE /api/v1/reports?from=2024-01&to=2024-06
Entonces el sistema debe responder con 200 OK
Y el mensaje debe indicar "6 reportes eliminados correctamente"
Y ninguno de esos reportes debe existir en la base de datos

Dado que el rango incluye el período actual "2026-02" con transacciones activas
Cuando se procesa la solicitud de eliminación masiva
Entonces solo los períodos anteriores a "2026-02" deben eliminarse
Y el mensaje de respuesta debe indicar el período excluido

Dado que los parámetros del rango están invertidos (from > to)
Cuando se envía la solicitud
Entonces el sistema debe responder con 400 Bad Request
Y el mensaje debe indicar "El período de inicio debe ser anterior al período de fin"
```

---

### US-023 — Editar una Transacción Financiera Existente

| Criterio INVEST | Definición | Evaluación | Notas |
|---|---|---|---|
| Independiente | Se puede desarrollar sin depender de otras historias | **APROBADO** | La edición de transacciones es una operación CRUD independiente de la gestión de reportes y la exportación PDF. |
| Negociable | Los detalles pueden discutirse y ajustarse | **APROBADO** | Los campos editables, la semántica de actualización parcial vs. completa y las restricciones sobre transacciones antiguas son negociables. |
| Valiosa | Entrega valor claro al usuario o al negocio | **APROBADO** | Sin capacidad de edición, el usuario no puede corregir errores de entrada, degradando la calidad de sus datos financieros. |
| Estimable | El equipo puede estimar el esfuerzo con confianza | **APROBADO** | CRUD estándar con validación de entrada; alcance bien definido. |
| Pequeña | Cabe en un solo sprint | **APROBADO** | Un único endpoint PUT; Sprint 2 apropiado junto con la historia complementaria de sincronización automática. |
| Testeable | Existen criterios de aceptación claros | **APROBADO** | Los escenarios Gherkin cubren actualización completa, parcial, entrada inválida, propiedad y autenticación. |

**Resultado INVEST: TODOS APROBADOS** — No se requiere reescritura.

**Criterios de Aceptación (Confirmados):**

```gherkin
Dado que el usuario está autenticado con un JWT válido
Y posee la transacción "txn-uuid-123"
Cuando envía PUT /api/v1/transactions/txn-uuid-123 con { amount: 150.00, category: "Alimentación" }
Entonces el sistema debe responder con 200 OK
Y los campos actualizados deben persistirse en PostgreSQL
Y la respuesta debe retornar el objeto completo de la transacción actualizada

Dado que el usuario envía PUT con solo el campo description actualizado
Cuando el sistema procesa la solicitud
Entonces solo description debe cambiar
Y todos los demás campos deben conservar sus valores originales

Dado que el usuario envía PUT con amount: -50.00
Cuando el sistema valida la entrada
Entonces debe responder con 400 Bad Request
Y el mensaje debe indicar "El monto debe ser mayor a cero"
```

---

### US-024 — Sincronización Automática de Reportes por Cambio en Transacciones

| Criterio INVEST | Definición | Evaluación | Notas |
|---|---|---|---|
| Independiente | Se puede desarrollar sin depender de otras historias | **APROBADO CONDICIONAL** | Técnicamente reactiva a US-023, pero la lógica de sincronización es un comportamiento interno del sistema aislado. No añade funcionalidades visibles al usuario por encima de US-023; es un efecto secundario automático del sistema. Independencia suficiente para la planificación del sprint. |
| Negociable | Los detalles pueden discutirse y ajustarse | **APROBADO** | La estrategia de recálculo (diferencial vs. SUM total completo) está explícitamente marcada como negociable. |
| Valiosa | Entrega valor claro al usuario o al negocio | **APROBADO** | Garantiza que los reportes financieros sean siempre precisos sin requerir intervención manual del usuario. |
| Estimable | El equipo puede estimar el esfuerzo con confianza | **APROBADO** | Requiere lógica de recálculo de agregados e integración `@Transactional`. Alcance acotado. |
| Pequeña | Cabe en un solo sprint | **APROBADO** | Cambio de comportamiento interno disparado por mutaciones existentes; Sprint 2 con US-023 es correcto. |
| Testeable | Existen criterios de aceptación claros | **APROBADO** | Verificable ejecutando una mutación de transacción y consultando los totales del reporte afectado. |

**Resultado INVEST: TODOS APROBADOS (con nota sobre Independencia)** — No se requiere reescritura. La nota de independencia condicional es reconocida pero no constituye un fallo; US-023 y US-024 se co-desarrollan intencionalmente en el mismo sprint.

**Criterios de Aceptación (Confirmados):**

```gherkin
Dado que el usuario edita la transacción "txn-123" del período "2025-03" cambiando el monto de 100.00 a 150.00
Cuando el sistema procesa la actualización exitosamente
Entonces el reporte del período "2025-03" debe recalcular totalIngresos o totalGastos
Y el balance debe reflejar el nuevo monto
Y todos los cambios deben persistirse atómicamente en la misma transacción de base de datos

Dado que el usuario cambia la fecha de la transacción "txn-123" del período "2025-03" a "2025-04"
Cuando el sistema procesa la actualización
Entonces el reporte de "2025-03" debe reducirse en el monto de la transacción
Y el reporte de "2025-04" debe incrementarse en el mismo monto
Y si no existía reporte para "2025-04", debe crearse automáticamente

Dado que el usuario crea una nueva transacción para el período "2025-06" donde no existe reporte
Cuando la transacción es persistida
Entonces un reporte para el período "2025-06" debe crearse automáticamente con los valores de la transacción
```

---

### US-021 — Descargar Reporte de un Período como PDF

| Criterio INVEST | Definición | Evaluación | Notas |
|---|---|---|---|
| Independiente | Se puede desarrollar sin depender de otras historias | **APROBADO** | La descarga de PDF es autónoma; no depende de operaciones de eliminación ni de edición. |
| Negociable | Los detalles pueden discutirse y ajustarse | **APROBADO** | El contenido, diseño y campos incluidos en el PDF son explícitamente negociables con producto. |
| Valiosa | Entrega valor claro al usuario o al negocio | **APROBADO** | Permite el uso externo de datos financieros (impuestos, contabilidad, archivo). |
| Estimable | El equipo puede estimar el esfuerzo con confianza | **APROBADO** | Acotado a: solicitud → generación con librería PDF → respuesta binaria. Alcance claro. |
| Pequeña | Cabe en un solo sprint | **APROBADO** | Un único endpoint que retorna contenido binario; Sprint 3 apropiado. |
| Testeable | Existen criterios de aceptación claros | **APROBADO** | Verificable mediante el header Content-Type, el nombre del archivo en Content-Disposition e inspección del contenido del PDF. |

**Resultado INVEST: TODOS APROBADOS** — No se requiere reescritura.

**Criterios de Aceptación (Confirmados):**

```gherkin
Dado que el usuario está autenticado y tiene un reporte para el período "2025-10"
Cuando envía GET /api/v1/reports/2025-10/pdf
Entonces el sistema debe responder con 200 OK
Y Content-Type debe ser "application/pdf"
Y Content-Disposition debe contener "reporte-2025-10.pdf"
Y el PDF debe contener: período, totalIngresos, totalGastos, balanceNeto, marcaDeTiempoDeGeneración

Dado que el usuario solicita el PDF de un período inexistente
Cuando el sistema procesa la solicitud
Entonces debe responder con 404 Not Found
Y el mensaje debe ser "Reporte no encontrado"
```

---

### US-022 — Descargar Resumen Consolidado de Reportes por Rango como PDF

| Criterio INVEST | Definición | Evaluación | Notas |
|---|---|---|---|
| Independiente | Se puede desarrollar sin depender de otras historias | **APROBADO** | El PDF por rango es independiente del PDF individual; puede desarrollarse en paralelo una vez cumplidas las dependencias NFR. |
| Negociable | Los detalles pueden discutirse y ajustarse | **APROBADO** | El nivel de detalle (desglose mensual vs. solo totales acumulados) y el diseño son negociables. |
| Valiosa | Entrega valor claro al usuario o al negocio | **APROBADO** | Provee una visión holística del rendimiento financiero durante períodos extendidos. |
| Estimable | El equipo puede estimar el esfuerzo con confianza | **APROBADO** | Extiende la lógica de US-021 para agregar múltiples reportes; alcance acotado. |
| Pequeña | Cabe en un solo sprint | **APROBADO** | Un endpoint que agrega múltiples registros de reporte; Sprint 3 junto a US-021. |
| Testeable | Existen criterios de aceptación claros | **APROBADO** | Verificable comprobando el contenido del PDF para todos los períodos, las notas de datos parciales y el formato del nombre de archivo. |

**Resultado INVEST: TODOS APROBADOS** — No se requiere reescritura.

**Criterios de Aceptación (Confirmados):**

```gherkin
Dado que el usuario está autenticado y tiene reportes para "2025-01" a "2025-06"
Cuando envía GET /api/v1/reports/pdf?from=2025-01&to=2025-06
Entonces el sistema debe responder con 200 OK
Y Content-Disposition debe contener "resumen-2025-01_2025-06.pdf"
Y el PDF debe incluir un desglose por período y los totales acumulados

Dado que solo 3 de los 6 períodos del rango tienen datos de reporte
Cuando el usuario solicita el PDF consolidado
Entonces el PDF debe incluir datos de los 3 períodos disponibles
Y debe incluir una nota listando los 3 períodos faltantes

Dado que los parámetros del rango están invertidos (from > to)
Cuando se envía la solicitud
Entonces el sistema debe responder con 400 Bad Request
```

---

## SECCIÓN 5 — Gestión de Riesgos

### 5.1 Riesgos del Proyecto

Riesgos relacionados con la **ejecución** del proyecto — cronograma, equipo, recursos, proceso y dependencias externas.

| ID Riesgo | Descripción | Probabilidad | Impacto | Exposición | Estrategia de Mitigación | Responsable |
|---|---|---|---|---|---|---|
| PR-001 | Cambio de alcance: historias adicionales incorporadas al sprint a mitad del ciclo, reduciendo el tiempo de pruebas | M | A | **ALTO** | Aplicar congelamiento del sprint después de la planificación. Cualquier historia nueva pasa al siguiente backlog de sprint. | Product Owner |
| PR-002 | Brecha de habilidades del equipo en funciones avanzadas de PostgreSQL (constraints, transacciones, migraciones) | B | A | **MEDIO** | Realizar una sesión de formación de PostgreSQL de 1 día en Sprint 1. Emparejar desarrolladores junior y senior en tareas de migración. | Tech Lead |
| PR-003 | La librería de generación PDF no es adecuada para carga de producción o carece de las funciones de formato requeridas | B | M | **BAJO** | Evaluar la librería PDF en un spike durante el Sprint 2 antes de comprometerse con las historias PDF del Sprint 3. | Tech Lead |
| PR-004 | Inconsistencias del entorno Docker entre máquinas de desarrolladores y el pipeline CI/CD | M | M | **MEDIO** | Fijar todas las versiones de imágenes Docker. Usar el mismo docker-compose.yml para desarrollo local y CI. Ejecutar smoke test de `docker compose up` en CI. | DevOps |
| PR-005 | Presión de cronograma que lleva a omitir o apresurar la aprobación de QA | M | A | **ALTO** | Definir la aprobación de QA como criterio de salida obligatorio por sprint. Sin despliegue sin aprobación de QA. | QA Lead |
| PR-006 | Indisponibilidad de desarrollador clave (enfermedad, renuncia) durante un sprint crítico | B | A | **MEDIO** | Garantizar la transferencia de conocimiento mediante code reviews y documentación. Ninguna persona es la única propietaria de un módulo crítico. | Tech Lead |
| PR-007 | Retraso en la entrega del entorno de pruebas (Docker/PostgreSQL) bloqueando la ejecución de pruebas de integración | M | A | **ALTO** | Configurar el entorno de pruebas CI como entregable del Sprint 1. Bloquear las historias del Sprint 2 en la disponibilidad del entorno. | DevOps |

---

### 5.2 Riesgos del Producto

Riesgos relacionados con la **calidad y el comportamiento** del software — fallos funcionales, seguridad, integridad de datos y fallos de integración.

| ID Riesgo | Descripción | Probabilidad | Impacto | Exposición | Estrategia de Mitigación | Cobertura de Pruebas |
|---|---|---|---|---|---|---|
| PPR-001 | Vulnerabilidad de seguridad JWT: tokens que no expiran, tokens malformados aceptados o `userId` falsificable | B | A | **MEDIO** | Usar una librería JWT probada en producción. Configurar expiración explícita. Validar todos los claims en el middleware. | IT-AUTH-007, IT-AUTH-013, IT-RPT-007, IT-TXN-009 |
| PPR-002 | Fallo de integridad de datos durante la migración de Firebase a PostgreSQL: pérdida o corrupción de datos | A | A | **CRÍTICO** | Ejecutar la migración en un sprint dedicado con validación de checksum antes y después. Mantener Firebase en solo lectura hasta validar PostgreSQL. | Pruebas de integración NFR-001 + consultas a BD |
| PPR-003 | Inconsistencia en reportes tras mutación de transacción: la sincronización automática (US-024) falla o se ejecuta parcialmente | M | A | **ALTO** | Forzar el alcance `@Transactional`. Probar unitariamente la lógica de recálculo por separado. Pruebas de integración para todos los escenarios de sincronización. | IT-TXN-011, IT-TXN-012, IT-TXN-013, E2E-002, E2E-003 |
| PPR-004 | Violación del aislamiento de datos: usuario-b puede acceder o modificar recursos del usuario-a | B | A | **MEDIO** | Extraer `userId` únicamente de los claims del JWT (nunca del body o path de la solicitud). Pruebas de integración para todos los escenarios de acceso cruzado. | IT-RPT-005, IT-TXN-005, IT-PDF-007, IT-PDF-029, E2E-008 |
| PPR-005 | Eliminación de reporte en cascada hacia registros de transacciones por error | B | A | **MEDIO** | Asegurar que DELETE en reporte NO tenga cascada hacia transacciones en el esquema de BD. Verificar con IT-RPT-001 e IT-RPT-002. | IT-RPT-001, IT-RPT-002 |
| PPR-006 | Bypass de la protección del período actual: eliminación del reporte del período activo a pesar de tener transacciones activas | M | M | **MEDIO** | Probar unitariamente el componente "detector de período actual". Prueba de integración IT-RPT-003. | IT-RPT-003, IT-RPT-009, IT-RPT-021 |
| PPR-007 | Desbordamiento del alcance de eliminación masiva: más reportes eliminados de lo esperado en el rango | B | A | **MEDIO** | Verificar que la consulta a BD use límites estrictos `>=` / `<=` con filtro por `userId`. Confirmar conteos en IT-RPT-020 e IT-RPT-029. | IT-RPT-020, IT-RPT-025, IT-RPT-029 |
| PPR-008 | Contenido del PDF desactualizado: el PDF refleja datos anteriores no actualizados tras ediciones recientes de transacciones | M | M | **MEDIO** | La generación del PDF debe consultar la BD en tiempo real (sin caché). Validar con IT-PDF-004. | IT-PDF-004, E2E-006 |
| PPR-009 | Inconsistencia en el formato de respuesta de la API: algunos endpoints retornan estructura no estándar | M | B | **BAJO** | Definir middleware de formato de respuesta en NFR-004. Auditar todos los endpoints con pruebas de contrato. | Pruebas de integración NFR-004 |
| PPR-010 | La creación automática de reporte falla silenciosamente cuando se agrega una transacción a un período sin reporte existente | B | A | **MEDIO** | Probar unitariamente el disparador de creación de reporte. Prueba de integración IT-TXN-013 y E2E-001. | IT-TXN-013, E2E-001 |

---

### 5.3 Matriz de Riesgos

**Exposición = Probabilidad × Impacto**
Escala: A = Alto, M = Medio, B = Bajo. Exposición: CRÍTICO (A×A), ALTO (A×M o M×A), MEDIO (M×M o B×A o A×B), BAJO (B×M o M×B o B×B).

```
                           IMPACTO
                  Bajo (B) | Medio (M) | Alto (A)
             ┌────────────┬────────────┬──────────────┐
Alto   (A)   │    BAJO    │    ALTO    │   CRÍTICO    │
             │            │            │   PPR-002    │
             ├────────────┼────────────┼──────────────┤
Medio  (M)   │    BAJO    │   MEDIO    │    ALTO      │
P            │  PPR-009   │  PR-004    │  PR-001      │
R            │            │  PPR-006   │  PR-005      │
O            │            │  PPR-008   │  PR-007      │
B            │            │            │  PPR-003     │
A            ├────────────┼────────────┼──────────────┤
B            │    BAJO    │    BAJO    │   MEDIO      │
I Bajo  (B)  │            │  PR-003    │  PR-002      │
L            │            │            │  PR-006      │
I            │            │            │  PPR-001     │
D            │            │            │  PPR-004     │
A            │            │            │  PPR-005     │
D            │            │            │  PPR-007     │
             │            │            │  PPR-010     │
             └────────────┴────────────┴──────────────┘
```

**Resumen de Riesgos:**

| Nivel de Exposición | Riesgos |
|---|---|
| CRÍTICO | PPR-002 (Integridad de datos en migración) |
| ALTO | PR-001, PR-005, PR-007, PPR-003 |
| MEDIO | PR-002, PR-004, PR-006, PPR-001, PPR-004, PPR-005, PPR-006, PPR-007, PPR-008, PPR-010 |
| BAJO | PR-003, PPR-009 |

---

### 5.4 Estrategia de Respuesta ante Riesgos

**Umbrales de Aceptación:**
- Los riesgos **CRÍTICOS** deben tener una mitigación activa antes de que comience el sprint. Ninguna historia que dependa del área mitigada puede aceptarse en un sprint a menos que el riesgo se reduzca a ALTO o inferior.
- Los riesgos **ALTOS** requieren un plan de mitigación documentado y un responsable designado antes de la planificación del sprint.
- Los riesgos **MEDIOS** se monitorean semanalmente y se revisan en las retrospectivas de sprint.
- Los riesgos **BAJOS** se registran y se revisan solo al final del sprint.

**Ruta de Escalación para Riesgos Críticos:**

| Paso | Acción | Responsable | Disparador |
|---|---|---|---|
| 1 | Riesgo identificado y documentado | QA Lead | Durante la planificación de pruebas o la ejecución del sprint |
| 2 | Riesgo evaluado y mitigación propuesta | Tech Lead + QA Lead | Dentro de las 24 horas posteriores a la identificación |
| 3 | Plan de mitigación aprobado | Product Owner | Dentro de las 48 horas |
| 4 | Escalación a interesados si la mitigación falla | Project Manager | Si el riesgo no se resuelve dentro del sprint |
| 5 | Ajuste del alcance del sprint (historia removida o diferida) | Product Owner | Si la acción de escalación es insuficiente |

**Acciones de Contingencia:**

| ID Riesgo | Acción de Contingencia |
|---|---|
| PPR-002 | Mantener la base de datos Firebase en modo solo lectura durante todo el Sprint 2. Restaurar desde Firebase si los datos de PostgreSQL presentan corrupción. |
| PPR-003 | Si se detectan fallos en la sincronización automática durante la integración, revertir temporalmente a una estrategia de sincronización por polling hasta que se implemente una corrección. |
| PR-007 | Si el entorno de pruebas CI no está listo el día 1 del Sprint 2, trasladar las pruebas de integración a un entorno Docker Compose local hasta que CI sea corregido. |
| PR-001 | Mantener la regla de "congelamiento del sprint": no se aceptan nuevas historias después del día 2 del sprint. |
| PPR-004 | Si se detecta un fallo de aislamiento entre usuarios en producción, deshabilitar el endpoint afectado mediante feature flag hasta que se publique la corrección. |

---

## SECCIÓN 6 — Entorno de Pruebas y Herramientas

### 6.1 Herramientas y Stack Tecnológico

| Categoría | Herramienta / Tecnología | Propósito |
|---|---|---|
| Pruebas Unitarias | Jest (Node.js) o JUnit 5 (Java) | Ejecutor de pruebas unitarias para componentes de servicio, validadores y utilidades |
| Mocking (Unitario) | Jest Mocks / Mockito | Aislar componentes de dependencias externas durante pruebas unitarias |
| Pruebas de Integración | Supertest (Node.js) o RestAssured (Java) | Ejecutar solicitudes HTTP contra la API en ejecución dentro del entorno de prueba |
| Pruebas de API (Manual) | Postman + Newman CLI | Pruebas exploratorias y ejecución automatizada de colecciones disparadas desde CI |
| Gestión de Pruebas | GitHub Issues / Jira | Seguimiento de casos de prueba, defectos y progreso de cobertura |
| Integración CI/CD | GitHub Actions | Disparar suites de pruebas unitarias y de integración en cada PR y push a main |
| Reporte de Cobertura | Istanbul/nyc (Node.js) o JaCoCo (Java) | Generar reportes HTML de cobertura por módulo |
| Contenerización | Docker + Docker Compose | Proveer entorno de prueba aislado y reproducible con PostgreSQL |
| Inspección de Base de Datos | psql / DBeaver | Validar persistencia e integridad de datos durante y después de la ejecución de pruebas |
| Validación de PDF | pdf-parse (Node.js) o Apache PDFBox (Java) | Parsear y validar el contenido del PDF (campos, estructura, valores) en pruebas automatizadas |

### 6.2 Requerimientos de Configuración del Entorno

| Requerimiento | Detalle |
|---|---|
| Versión de Docker | ≥ 24.0 |
| Versión de Docker Compose | ≥ 2.20 |
| Versión de PostgreSQL | ≥ 15.0 (contenerizado) |
| Runtime Node.js / Java | Debe coincidir exactamente con la versión de runtime de producción |
| Archivo `.env` | Debe contener: `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET`, `JWT_EXPIRATION` |
| Red | Los servicios `api` y `postgres` deben estar en la misma red Docker |
| Health checks | Ambos contenedores deben pasar los health checks antes de que comiencen las pruebas |
| Aislamiento de puertos | El entorno de prueba debe usar puertos distintos al desarrollo local para evitar conflictos |

### 6.3 Estrategia de Gestión de Datos de Prueba

| Aspecto | Estrategia |
|---|---|
| Datos de siembra | Un script SQL crea dos usuarios (`usuario-a@test.com`, `usuario-b@test.com`), 6 transacciones por usuario en 3 períodos y sus reportes correspondientes antes de cada suite de prueba. |
| Aislamiento de datos | Cada suite de prueba opera sobre un esquema PostgreSQL fresco. El esquema se reinicia entre suites principales (no entre casos de prueba individuales). |
| IDs deterministas | Los IDs de transacciones (`txn-uuid-123`, `txn-uuid-456`) y los períodos de reporte son fijos en los datos de siembra para garantizar reproducibilidad. |
| Período actual | El período `2026-02` se designa como "período actual" para todas las pruebas de protección de período activo. |
| Limpieza | Un script de teardown trunca las tablas de prueba tras cada ejecución de suite. Los constraints de clave foránea se difieren temporalmente durante la limpieza. |

### 6.4 Estrategia de Mocking y Stubbing

| Componente | Estrategia |
|---|---|
| Librería de generación PDF | Stub en la capa de servicio para pruebas unitarias. Se usa como dependencia real en pruebas de integración. Para el escenario de error 500 (IT-PDF-005), un stub de inyección de fallos simula el fallo de la librería. |
| Secreto JWT | Secreto de prueba fijo configurado mediante `.env.test`. Los tokens con payloads conocidos se pre-generan como fixtures de prueba. |
| Reloj del sistema (período actual) | La lógica de "período actual" se inyecta mediante una abstracción `ClockProvider`. En pruebas, se hace stub para retornar `2026-02` de forma consistente. |
| PostgreSQL | No se mockea. Las pruebas de integración usan una instancia PostgreSQL contenerizada real para validar el comportamiento de persistencia efectivo. |


## SECCIÓN 7 — Métricas y Reportes

### 7.1 KPIs a Rastrear

| KPI | Objetivo | Frecuencia de Medición |
|---|---|---|
| **Cobertura de pruebas unitarias (capa de servicio)** | ≥ 80% cobertura de líneas | Por pull request (reporte CI) |
| **Cobertura de pruebas unitarias (validadores)** | ≥ 95% cobertura de líneas | Por pull request |
| **Tasa de aprobación de pruebas de integración (P1)** | 100% al final del sprint | Diario durante la fase de ejecución de pruebas |
| **Tasa de aprobación de pruebas de integración (P2)** | ≥ 90% al final del sprint | Diario durante la fase de ejecución de pruebas |
| **Densidad de defectos** | < 2 defectos P1 por historia al final del sprint | Retrospectiva de sprint |
| **Defectos escapados** | 0 defectos críticos escapan a producción | Revisión post-sprint |
| **Tasa de aprobación de regresión** | 100% en cada ejecución de regresión | Después de cada ejecución de regresión |
| **Tasa de aprobación de flujos E2E críticos** | 100% de E2E-001 a E2E-008 | Antes de la aprobación del sprint |

### 7.2 Frecuencia y Formato de Reportes

| Tipo de Reporte | Frecuencia | Audiencia | Formato |
|---|---|---|---|
| Estado diario de ejecución de pruebas | Diario (durante fases activas de ejecución) | QA Lead, Tech Lead | Mensaje en Slack con conteos de éxito/fallo y resumen de bloqueos |
| Resumen de pruebas del sprint | Al final de cada sprint | Product Owner, Tech Lead, QA Lead | Reporte en Markdown en GitHub / Confluence con KPIs, tabla de defectos y actualización de riesgos |
| Registro de defectos | Actualización en tiempo real | Todos los miembros del equipo | GitHub Issues con etiquetas: `severidad:crítica`, `severidad:mayor`, `severidad:menor`, `tipo:integración`, `tipo:unitaria` |
| Reporte final de aprobación QA | Una vez, antes del despliegue en producción | Product Owner, Interesados | Documento PDF con resumen completo de KPIs, estado de riesgos, problemas pendientes y firmas de aprobación |
| Reporte de cobertura | Por PR + al final del sprint | Desarrolladores, QA Lead | Reporte HTML de Istanbul/JaCoCo embebido en artefactos CI |

### 7.3 Definición de Done desde la Perspectiva de QA

Una historia se considera **QA Completada** cuando se cumplen todas las condiciones siguientes:

- [ ] Todos los escenarios Gherkin de criterios de aceptación pasan como pruebas de integración automatizadas.
- [ ] Todos los casos de prueba de integración P1 del endpoint(s) de la historia pasan.
- [ ] Los casos de prueba de autenticación y aislamiento entre usuarios pasan.
- [ ] Sin defectos P1 (críticos) o P2 (mayores) abiertos asignados a la historia.
- [ ] La cobertura de pruebas unitarias cumple los objetivos para todos los componentes introducidos por la historia.
- [ ] El endpoint de la historia ha sido verificado dentro del entorno Docker Compose.
- [ ] La estructura de respuesta de la API cumple el estándar NFR-004 (validado por una prueba de contrato).
- [ ] La historia fue demostrada al Product Owner en la revisión del sprint.
- [ ] El QA Lead revisó y aprobó todos los resultados de prueba de la historia.

---