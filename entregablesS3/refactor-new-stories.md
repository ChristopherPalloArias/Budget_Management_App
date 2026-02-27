# 📋 new-stories-refined.md — Historias de Usuario Refinadas
### Budget Management App — Backlog Refinado para Desarrollo

---

## 1. Visión General

### 1.1 Descripción del Sistema

La **Budget Management App** es una aplicación de gestión financiera personal cuyo backend está expuesto como una **API REST**. El sistema permite a los usuarios registrar transacciones financieras, generar reportes automáticos por período y exportar información en formato PDF.

Este documento define el backlog completo de Historias de Usuario refinadas, cubriendo tanto requisitos funcionales como no funcionales, alineados con las decisiones arquitectónicas vigentes.

### 1.2 Decisiones Arquitectónicas (Cerradas)

| Decisión | Detalle |
|---|---|
| **Persistencia** | PostgreSQL (se elimina Firebase) |
| **Autenticación/Autorización** | JWT (JSON Web Tokens) |
| **Interfaz de Servicio** | API REST |
| **Contenerización** | Docker obligatorio |
| **Actualización de Reportes** | Automática por eventos de cambio en transacciones |
| **Edición de Transacciones** | Soportada con propagación automática a reportes |

### 1.3 Objetivo de Negocio

- Proveer control completo al usuario sobre sus datos financieros (CRUD de transacciones y reportes).
- Garantizar consistencia automática entre transacciones y reportes sin intervención manual.
- Permitir la exportación de reportes para uso externo (contabilidad, impuestos).
- Asegurar la integridad, seguridad y escalabilidad del sistema mediante JWT, PostgreSQL y Docker.

### 1.4 Actores Involucrados

| Actor | Descripción |
|---|---|
| **Usuario Registrado** | Propietario de transacciones y reportes. Opera exclusivamente sobre sus propios datos. |
| **Sistema (API Backend)** | Procesador encargado de validar, ejecutar y registrar operaciones. Gestiona la sincronización automática de reportes. |
| **Generador de PDF** | Componente backend responsable de producir documentos PDF a partir de datos de reportes. |

---

## 2. Suposiciones

1. Un usuario solo opera sobre sus propios recursos, identificados por su `userId` extraído del JWT.
2. Los reportes están organizados por período mensual en formato `yyyy-MM`.
3. Un reporte existe únicamente si al menos una transacción fue registrada en ese período.
4. La eliminación de un reporte **no** elimina las transacciones originales.
5. Los reportes se **recalculan automáticamente** cuando se crean, editan o eliminan transacciones; no existe recalculación manual.
6. El sistema se despliega en contenedores Docker con variables de entorno para configuración.
7. PostgreSQL es la única base de datos del sistema; la migración desde Firebase es un prerequisito.
8. Toda comunicación con la API requiere un token JWT válido en el header `Authorization`.

---

## 3. Restricciones

1. Ninguna operación es accesible sin autenticación JWT válida.
2. No se permite eliminar reportes del período en curso si tienen transacciones activas.
3. El PDF refleja los datos en el momento de la descarga; no se actualiza retroactivamente.
4. No existe recalculación manual de reportes; toda actualización se dispara por cambios en transacciones.
5. Las transacciones solo pueden ser editadas por su propietario.
6. El sistema debe funcionar completamente en contenedores Docker.
7. La API debe seguir convenciones REST estándar con códigos HTTP semánticos.

---

## 4. Historias de Usuario

---

### ⚙️ Historias No Funcionales / Técnicas

---

#### NFR-001 — Migración de Persistencia de Firebase a PostgreSQL

**Descripción:**

> Como **Equipo de Desarrollo**,
> quiero **migrar la capa de persistencia de Firebase a PostgreSQL**,
> para **contar con una base de datos relacional robusta, con soporte transaccional completo y control total del esquema de datos.**

**Prioridad:** 🔴 Crítica (Bloqueante)

**Validación INVEST:**

| Principio | Justificación |
|---|---|
| **Independiente** | Es una historia de infraestructura base que no depende de funcionalidades de usuario. |
| **Negociable** | El esquema exacto de tablas, índices y constraints es negociable con el equipo. |
| **Valiosa** | Habilita transaccionalidad ACID, consultas SQL avanzadas y eliminación de vendor lock-in. |
| **Estimable** | Alcance claro: definir esquema, crear migraciones, adaptar repositorios, validar datos. |
| **Pequeña** | Se limita a la migración de la capa de datos; no modifica lógica de negocio. |
| **Testeable** | Verificable ejecutando operaciones CRUD y validando integridad referencial en PostgreSQL. |

**Criterios de Aceptación (Gherkin):**

```gherkin
Feature: Migración de persistencia a PostgreSQL

  Scenario: Esquema de base de datos creado correctamente
    Given que el contenedor de PostgreSQL está ejecutándose
    When se ejecutan los scripts de migración
    Then las tablas users, transactions y reports deben existir
    And cada tabla debe tener sus constraints de integridad referencial
    And los índices para userId y period deben estar creados

  Scenario: Operaciones CRUD funcionan sobre PostgreSQL
    Given que la migración fue ejecutada exitosamente
    When la API procesa una solicitud POST /api/v1/transactions con datos válidos
    Then la transacción debe persistirse en la tabla transactions de PostgreSQL
    And una consulta GET /api/v1/transactions/{id} debe retornar los datos insertados

  Scenario: Firebase completamente desconectado
    Given que la migración fue completada
    When se revisa la configuración del sistema
    Then no deben existir dependencias, imports ni configuraciones de Firebase
    And todas las variables de entorno deben apuntar a PostgreSQL

  Scenario: Integridad referencial validada
    Given que existen transacciones asociadas a un usuario
    When se intenta eliminar el usuario sin eliminar sus transacciones
    Then la base de datos debe rechazar la operación con un error de constraint
```

**Dependencias:** Ninguna. Esta es una historia bloqueante para el resto del backlog.

---

#### NFR-002 — Autenticación y Autorización con JWT

**Descripción:**

> Como **Equipo de Desarrollo**,
> quiero **implementar autenticación y autorización basada en JWT para todos los endpoints de la API**,
> para **garantizar que solo usuarios autenticados accedan al sistema y que cada usuario opere únicamente sobre sus propios recursos.**

**Prioridad:** 🔴 Crítica (Bloqueante)

**Validación INVEST:**

| Principio | Justificación |
|---|---|
| **Independiente** | La capa de seguridad es transversal pero se implementa de forma independiente a la lógica de negocio. |
| **Negociable** | Algoritmo de firma, tiempo de expiración del token y claims son negociables. |
| **Valiosa** | Garantiza seguridad de acceso, protección de datos y aislamiento entre usuarios. |
| **Estimable** | Alcance definido: endpoints de login/registro, middleware de validación, extracción de userId. |
| **Pequeña** | Se limita a la capa de autenticación/autorización; no modifica lógica de negocio. |
| **Testeable** | Verificable con requests autenticados y no autenticados contra endpoints protegidos. |

**Criterios de Aceptación (Gherkin):**

```gherkin
Feature: Autenticación y autorización con JWT

  Scenario: Registro exitoso de usuario
    Given que un usuario nuevo envía POST /api/v1/auth/register con email y password válidos
    When el sistema procesa la solicitud
    Then debe responder con 201 Created
    And el usuario debe persistirse en la tabla users con el password hasheado
    And debe retornar un token JWT válido en el body de la respuesta

  Scenario: Login exitoso
    Given que existe un usuario registrado con email "user@test.com"
    When envía POST /api/v1/auth/login con credenciales correctas
    Then el sistema debe responder con 200 OK
    And debe retornar un token JWT con el userId en los claims
    And el token debe tener una expiración configurada

  Scenario: Acceso denegado sin token
    Given que un cliente envía GET /api/v1/transactions sin header Authorization
    When el sistema procesa la solicitud
    Then debe responder con 401 Unauthorized
    And el body debe contener el mensaje "Token de autenticación requerido"

  Scenario: Acceso denegado con token expirado
    Given que un cliente envía una solicitud con un token JWT expirado
    When el sistema valida el token
    Then debe responder con 401 Unauthorized
    And el body debe contener el mensaje "Token expirado"

  Scenario: Aislamiento de datos entre usuarios
    Given que el usuario A está autenticado con su JWT
    When envía GET /api/v1/transactions
    Then solo debe recibir las transacciones donde userId coincide con el claim del JWT
    And no debe recibir transacciones de otros usuarios

  Scenario: Login con credenciales inválidas
    Given que un usuario envía POST /api/v1/auth/login con password incorrecto
    When el sistema procesa la solicitud
    Then debe responder con 401 Unauthorized
    And debe retornar el mensaje "Credenciales inválidas"
```

**Dependencias:** NFR-001 (PostgreSQL debe estar disponible para persistir usuarios).

---

#### NFR-003 — Contenerización del Sistema con Docker

**Descripción:**

> Como **Equipo de DevOps**,
> quiero **contenerizar todos los componentes del sistema (API backend y PostgreSQL) usando Docker y Docker Compose**,
> para **garantizar portabilidad, reproducibilidad y despliegue consistente en cualquier entorno.**

**Prioridad:** 🔴 Crítica (Bloqueante)

**Validación INVEST:**

| Principio | Justificación |
|---|---|
| **Independiente** | La contenerización es una capa de infraestructura independiente de la lógica funcional. |
| **Negociable** | Las imágenes base, puertos, volúmenes y configuración de health checks son negociables. |
| **Valiosa** | Elimina problemas de entorno, habilita CI/CD y garantiza paridad dev/prod. |
| **Estimable** | Alcance claro: Dockerfile para la API, docker-compose.yml con servicios definidos. |
| **Pequeña** | Se limita a la definición de contenedores y orquestación local. |
| **Testeable** | Verificable ejecutando `docker compose up` y validando que todos los servicios responden correctamente. |

**Criterios de Aceptación (Gherkin):**

```gherkin
Feature: Contenerización con Docker

  Scenario: Construcción exitosa de la imagen Docker de la API
    Given que existe un Dockerfile válido en el directorio del proyecto
    When se ejecuta docker build -t budget-api .
    Then la imagen debe construirse sin errores
    And el tamaño de la imagen debe ser razonable (multi-stage build)

  Scenario: Orquestación completa con Docker Compose
    Given que existe un archivo docker-compose.yml con los servicios api y postgres
    When se ejecuta docker compose up -d
    Then el contenedor de PostgreSQL debe estar en estado healthy
    And el contenedor de la API debe estar en estado healthy
    And la API debe responder en el puerto configurado

  Scenario: Variables de entorno configuradas correctamente
    Given que el archivo .env contiene las variables DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD y JWT_SECRET
    When los contenedores se inician
    Then la API debe conectarse exitosamente a PostgreSQL usando las variables del .env
    And no deben existir credenciales hardcodeadas en el código fuente

  Scenario: Persistencia de datos entre reinicios
    Given que existen datos en PostgreSQL
    When se ejecuta docker compose down seguido de docker compose up
    Then los datos previamente insertados deben seguir disponibles
    And los volúmenes de datos deben estar configurados correctamente
```

**Dependencias:** NFR-001 (el esquema de PostgreSQL debe estar definido).

---

#### NFR-004 — Arquitectura de API REST con Estándares de Calidad

**Descripción:**

> Como **Equipo de Desarrollo**,
> quiero **que la API REST siga convenciones estándar de diseño, versionado y manejo de errores**,
> para **garantizar consistencia, mantenibilidad y una experiencia predecible para los consumidores de la API.**

**Prioridad:** 🟡 Alta

**Validación INVEST:**

| Principio | Justificación |
|---|---|
| **Independiente** | Los estándares de API se definen como una capa transversal sin dependencia funcional. |
| **Negociable** | El formato de errores, convenciones de paginación y versionado son negociables. |
| **Valiosa** | Reduce fricción de integración, mejora la mantenibilidad y facilita el testing. |
| **Estimable** | Alcance definido: definir estructura de respuestas, middleware de error handling, versionado. |
| **Pequeña** | Se limita a la capa de presentación de la API; no modifica lógica de negocio. |
| **Testeable** | Verificable validando la estructura de las respuestas HTTP y códigos de estado. |

**Criterios de Aceptación (Gherkin):**

```gherkin
Feature: Estándares de API REST

  Scenario: Respuestas exitosas con formato consistente
    Given que un endpoint procesa una solicitud válida
    When retorna una respuesta exitosa
    Then debe usar el código HTTP apropiado (200, 201, 204)
    And el body debe seguir la estructura: { data, message, timestamp }

  Scenario: Errores con formato estandarizado
    Given que un endpoint recibe una solicitud inválida
    When procesa el error
    Then debe retornar el código HTTP apropiado (400, 401, 403, 404, 500)
    And el body debe seguir la estructura: { error, message, statusCode, timestamp }

  Scenario: Versionado de API
    Given que la API está versionada
    When un cliente accede a /api/v1/*
    Then todos los endpoints deben responder bajo el prefijo /api/v1
    And la versión debe ser consistente en toda la API

  Scenario: Validación de entrada en todos los endpoints
    Given que un endpoint recibe datos de entrada
    When los datos no cumplen con las validaciones requeridas
    Then debe retornar 400 Bad Request
    And el mensaje debe indicar específicamente qué campos son inválidos
```

**Dependencias:** Ninguna directa. Se implementa en paralelo con las historias funcionales.

---

### 📦 Historias Funcionales

---

#### 📦 Funcionalidad 1: Eliminación de Reportes

---

#### US-017 — Eliminar un Reporte Financiero de un Período

**Descripción:**

> Como **Usuario Registrado**,
> quiero **eliminar un reporte financiero de un período mensual específico a través de la API**,
> para **mantener mi historial de reportes limpio y libre de información que ya no es relevante.**

**Prioridad:** 🟡 Alta

**Validación INVEST:**

| Principio | Justificación |
|---|---|
| **Independiente** | La eliminación es una operación autónoma que no depende de descarga ni edición de transacciones. |
| **Negociable** | Las reglas sobre qué reportes pueden eliminarse y las restricciones de período actual son negociables. |
| **Valiosa** | Permite al usuario gestionar su historial financiero y eliminar datos no deseados o erróneos. |
| **Estimable** | Operación CRUD estándar: validación de propiedad → verificación de reglas → DELETE en PostgreSQL. |
| **Pequeña** | Un solo endpoint DELETE para un recurso específico; flujo simple. |
| **Testeable** | Verificable mediante requests HTTP con distintos escenarios (éxito, no encontrado, período protegido). |

**Criterios de Aceptación (Gherkin):**

```gherkin
Feature: Eliminación de Reporte Financiero via API

  Scenario: Eliminación exitosa de un reporte
    Given que el usuario está autenticado con un JWT válido
    And existe un reporte de su propiedad para el período "2025-03"
    When envía DELETE /api/v1/reports/2025-03
    Then el sistema debe responder con 200 OK
    And el mensaje debe ser "Reporte eliminado correctamente"
    And el reporte ya no debe existir en la base de datos
    And las transacciones del período "2025-03" deben permanecer intactas

  Scenario: Intento de eliminar reporte del período en curso con transacciones activas
    Given que el usuario está autenticado
    And el período actual es "2026-02"
    And existen transacciones activas para "2026-02"
    When envía DELETE /api/v1/reports/2026-02
    Then el sistema debe responder con 409 Conflict
    And el mensaje debe ser "No es posible eliminar el reporte del período en curso con transacciones activas"

  Scenario: Intento de eliminar un reporte inexistente
    Given que el usuario está autenticado
    When envía DELETE /api/v1/reports/2020-01
    And no existe un reporte para ese período
    Then el sistema debe responder con 404 Not Found
    And el mensaje debe ser "Reporte no encontrado"

  Scenario: Intento de eliminar reporte de otro usuario
    Given que el usuario A está autenticado
    And existe un reporte del período "2025-03" perteneciente al usuario B
    When el usuario A envía DELETE /api/v1/reports/2025-03
    Then el sistema debe responder con 404 Not Found

  Scenario: Intento de eliminar sin autenticación
    Given que no se incluye header Authorization en la solicitud
    When se envía DELETE /api/v1/reports/2025-03
    Then el sistema debe responder con 401 Unauthorized
```

**Endpoint REST:** `DELETE /api/v1/reports/{period}`
**Dependencias:** NFR-001, NFR-002, NFR-003

---

#### US-018 — Eliminación Masiva de Reportes por Rango de Período

**Descripción:**

> Como **Usuario Registrado**,
> quiero **eliminar múltiples reportes financieros dentro de un rango de períodos a través de la API**,
> para **limpiar mi historial de forma eficiente sin eliminar cada reporte individualmente.**

**Prioridad:** 🟢 Media

**Validación INVEST:**

| Principio | Justificación |
|---|---|
| **Independiente** | La eliminación masiva es independiente de la eliminación individual y de otras operaciones. |
| **Negociable** | Los límites del rango y el manejo de reportes protegidos son negociables. |
| **Valiosa** | Ahorra tiempo significativo al usuario cuando necesita limpiar múltiples períodos. |
| **Estimable** | Extiende la lógica de US-017 a un rango; operación en lote acotada. |
| **Pequeña** | Un endpoint adicional con lógica de iteración sobre el rango validado. |
| **Testeable** | Verificable con rangos válidos, rangos vacíos y rangos que incluyen el período actual. |

**Criterios de Aceptación (Gherkin):**

```gherkin
Feature: Eliminación Masiva de Reportes por Rango

  Scenario: Eliminación masiva exitosa
    Given que el usuario está autenticado
    And existen reportes propios para "2024-01" a "2024-06"
    When envía DELETE /api/v1/reports?from=2024-01&to=2024-06
    Then el sistema debe responder con 200 OK
    And el mensaje debe indicar "6 reportes eliminados correctamente"
    And ninguno de esos reportes debe existir en la base de datos

  Scenario: Rango incluye período en curso con transacciones activas
    Given que el usuario selecciona rango "2025-12" a "2026-02"
    And el período actual "2026-02" tiene transacciones activas
    When envía la solicitud de eliminación por rango
    Then el sistema debe eliminar solo los reportes de "2025-12" y "2026-01"
    And debe responder con 200 OK
    And el mensaje debe indicar "2 reportes eliminados. El período 2026-02 fue excluido por tener transacciones activas"

  Scenario: Rango sin reportes
    Given que el usuario envía DELETE /api/v1/reports?from=2020-01&to=2020-06
    And no existen reportes en ese rango
    Then el sistema debe responder con 404 Not Found
    And el mensaje debe ser "No se encontraron reportes en el rango seleccionado"

  Scenario: Rango inválido
    Given que el usuario envía from=2025-06 y to=2025-01 (invertido)
    When el sistema valida los parámetros
    Then debe responder con 400 Bad Request
    And el mensaje debe indicar "El período de inicio debe ser anterior al período de fin"
```

**Endpoint REST:** `DELETE /api/v1/reports?from={period}&to={period}`
**Dependencias:** US-017, NFR-001, NFR-002

---

#### 📦 Funcionalidad 2: Edición de Transacciones Financieras

---

#### US-023 — Editar una Transacción Financiera Existente

**Descripción:**

> Como **Usuario Registrado**,
> quiero **modificar los datos de una transacción previamente registrada (monto, categoría, descripción o fecha)**,
> para **corregir errores de entrada y mantener mi historial financiero preciso.**

**Prioridad:** 🔴 Crítica

**Validación INVEST:**

| Principio | Justificación |
|---|---|
| **Independiente** | La edición de transacciones es una operación CRUD independiente de reportes y descargas. |
| **Negociable** | Los campos editables y las restricciones de edición (ej. transacciones antiguas) son negociables. |
| **Valiosa** | Sin capacidad de edición, el usuario no puede corregir errores, degradando la calidad de sus datos. |
| **Estimable** | Operación CRUD estándar: validación → actualización en PostgreSQL → respuesta. |
| **Pequeña** | Un solo endpoint PUT para un recurso existente; mutación acotada. |
| **Testeable** | Verificable con pruebas de integración: actualización exitosa, validaciones, recurso inexistente. |

**Criterios de Aceptación (Gherkin):**

```gherkin
Feature: Edición de Transacción Financiera

  Scenario: Actualización exitosa de monto y categoría
    Given que el usuario está autenticado con JWT válido
    And posee una transacción con ID "txn-uuid-123"
    When envía PUT /api/v1/transactions/txn-uuid-123 con body:
      | field    | value         |
      | amount   | 150.00        |
      | category | Alimentación  |
    Then el sistema debe responder con 200 OK
    And los datos actualizados deben persistirse en PostgreSQL
    And la respuesta debe contener los datos de la transacción actualizada

  Scenario: Intento de actualizar transacción inexistente
    Given que el usuario está autenticado
    When envía PUT /api/v1/transactions/txn-uuid-999
    And no existe una transacción con ese ID
    Then el sistema debe responder con 404 Not Found
    And el mensaje debe ser "Transacción no encontrada"

  Scenario: Validación de datos de entrada inválidos
    Given que el usuario envía PUT con amount negativo (-50.00)
    When el sistema valida la solicitud
    Then debe responder con 400 Bad Request
    And el mensaje debe indicar "El monto debe ser mayor a cero"

  Scenario: Intento de editar transacción de otro usuario
    Given que el usuario A está autenticado
    And la transacción "txn-uuid-456" pertenece al usuario B
    When el usuario A envía PUT /api/v1/transactions/txn-uuid-456
    Then el sistema debe responder con 404 Not Found

  Scenario: Campos opcionales en la actualización parcial
    Given que el usuario envía PUT con solo el campo description actualizado
    When el sistema procesa la solicitud
    Then debe actualizar únicamente el campo description
    And los demás campos deben mantener sus valores originales
    And debe responder con 200 OK
```

**Endpoint REST:** `PUT /api/v1/transactions/{transactionId}`
**Dependencias:** NFR-001, NFR-002, NFR-003
**Notas técnicas:**
- La operación PUT debe ser idempotente.
- Al actualizar exitosamente, se debe disparar la sincronización automática del reporte afectado (US-024).

---

#### US-024 — Sincronización Automática de Reportes por Cambio en Transacciones

**Descripción:**

> Como **Sistema (API Backend)**,
> quiero **recalcular automáticamente los totales del reporte afectado cuando se crea, edita o elimina una transacción**,
> para **garantizar que los reportes financieros del usuario siempre reflejen datos consistentes sin intervención manual.**

**Prioridad:** 🔴 Crítica

**Validación INVEST:**

| Principio | Justificación |
|---|---|
| **Independiente** | La sincronización es una reacción interna del sistema, desacoplada de la acción del usuario sobre la transacción. |
| **Negociable** | La estrategia de recálculo (diferencial vs. SUM total) es negociable con el equipo técnico. |
| **Valiosa** | Asegura que los reportes y KPIs del usuario sean verídicos sin requerir acción manual. |
| **Estimable** | Requiere lógica de recálculo de agregados tras operaciones sobre transacciones. |
| **Pequeña** | Se limita a la reacción ante cambios en transacciones para un período específico. |
| **Testeable** | Verificable editando una transacción y comprobando que los totales del reporte se actualizan automáticamente. |

**Criterios de Aceptación (Gherkin):**

```gherkin
Feature: Sincronización Automática de Reportes

  Scenario: Reporte actualizado tras edición de monto
    Given que el usuario edita la transacción "txn-123" del período "2025-03"
    And el monto anterior era 100.00 y el nuevo es 150.00
    When el sistema procesa la actualización exitosamente
    Then el reporte del período "2025-03" debe recalcular totalIncome o totalExpense
    And el balance debe reflejar el nuevo valor
    And los cambios deben persistirse en PostgreSQL

  Scenario: Reporte actualizado tras cambio de período (fecha)
    Given que el usuario cambia la fecha de la transacción "txn-123" de "2025-03" a "2025-04"
    When el sistema procesa la actualización
    Then el reporte de "2025-03" debe restar el monto de la transacción
    And el reporte de "2025-04" debe sumar el monto
    And si el reporte de "2025-04" no existía, debe crearse automáticamente

  Scenario: Reporte actualizado tras creación de transacción
    Given que el usuario crea una nueva transacción para el período "2025-06"
    When el sistema la persiste exitosamente
    Then el reporte de "2025-06" debe actualizarse con los nuevos totales
    And si no existía reporte para "2025-06", debe crearse automáticamente

  Scenario: Reporte actualizado tras eliminación de transacción
    Given que el usuario elimina la transacción "txn-456" del período "2025-03"
    When el sistema procesa la eliminación
    Then el reporte de "2025-03" debe restar el monto de la transacción eliminada
    And el balance debe actualizarse automáticamente
```

**Dependencias:** US-023, NFR-001
**Notas técnicas:**
- La sincronización se ejecuta como parte de la misma transacción de base de datos (@Transactional) para garantizar consistencia.
- No requiere acción del usuario; es un efecto colateral automático de las operaciones sobre transacciones.

---

#### 📦 Funcionalidad 3: Descarga de Reportes en PDF

---

#### US-021 — Descargar Reporte de un Período como PDF

**Descripción:**

> Como **Usuario Registrado**,
> quiero **descargar el reporte financiero de un período específico en formato PDF a través de la API**,
> para **conservar un registro imprimible y compartible de mi actividad financiera mensual.**

**Prioridad:** 🟡 Alta

**Validación INVEST:**

| Principio | Justificación |
|---|---|
| **Independiente** | La descarga de PDF es autónoma; no depende de eliminación ni edición de transacciones. |
| **Negociable** | El contenido, diseño y campos incluidos en el PDF son negociables con producto. |
| **Valiosa** | Permite exportar información financiera para uso externo (impuestos, contabilidad, archivos). |
| **Estimable** | Operación bien delimitada: solicitud → generación con librería PDF → respuesta con archivo. |
| **Pequeña** | Un solo endpoint que retorna un archivo binario; un reporte por descarga. |
| **Testeable** | Verificable comprobando que el endpoint retorna un archivo PDF válido con Content-Type correcto. |

**Criterios de Aceptación (Gherkin):**

```gherkin
Feature: Descarga de Reporte en PDF

  Scenario: Descarga exitosa del PDF
    Given que el usuario está autenticado
    And existe un reporte propio para el período "2025-10"
    When envía GET /api/v1/reports/2025-10/pdf
    Then el sistema debe responder con 200 OK
    And el Content-Type debe ser "application/pdf"
    And el header Content-Disposition debe contener "reporte-2025-10.pdf"
    And el PDF debe contener: período, total ingresos, total gastos, balance neto y fecha de generación

  Scenario: Intento de descargar PDF de reporte inexistente
    Given que el usuario está autenticado
    When envía GET /api/v1/reports/2020-01/pdf
    And no existe reporte para ese período
    Then el sistema debe responder con 404 Not Found
    And el mensaje debe ser "Reporte no encontrado"

  Scenario: Error interno durante generación del PDF
    Given que el usuario solicita la descarga del PDF
    And ocurre un error interno durante la generación
    Then el sistema debe responder con 500 Internal Server Error
    And el mensaje debe ser "No fue posible generar el PDF. Inténtalo más tarde"

  Scenario: Descarga bloqueada sin autenticación
    Given que no se incluye header Authorization
    When se envía GET /api/v1/reports/2025-10/pdf
    Then el sistema debe responder con 401 Unauthorized
```

**Endpoint REST:** `GET /api/v1/reports/{period}/pdf`
**Dependencias:** NFR-001, NFR-002, NFR-003
**Contenido esperado del PDF:** nombre del usuario, período, total ingresos, total gastos, balance neto, fecha/hora de generación.

---

#### US-022 — Descargar Resumen Consolidado de Reportes por Rango como PDF

**Descripción:**

> Como **Usuario Registrado**,
> quiero **descargar un resumen consolidado de mis reportes financieros para un rango de períodos en formato PDF**,
> para **tener un documento completo de mi actividad financiera en un intervalo de tiempo determinado.**

**Prioridad:** 🟢 Media

**Validación INVEST:**

| Principio | Justificación |
|---|---|
| **Independiente** | La descarga por rango es independiente de la descarga individual de período. |
| **Negociable** | El nivel de detalle del resumen (desglose mensual vs. solo acumulados) y el diseño son negociables. |
| **Valiosa** | Permite obtener una visión consolidada del desempeño financiero en un intervalo amplio. |
| **Estimable** | Extiende la lógica de US-021 a múltiples períodos; operación acotada. |
| **Pequeña** | Un endpoint adicional que agrega datos de múltiples reportes en un solo PDF. |
| **Testeable** | Verificable comprobando que el PDF contiene datos de todos los períodos del rango con totales correctos. |

**Criterios de Aceptación (Gherkin):**

```gherkin
Feature: Descarga de Resumen de Reportes por Rango en PDF

  Scenario: Descarga exitosa del resumen por rango
    Given que el usuario está autenticado
    And existen reportes propios para "2025-01" a "2025-06"
    When envía GET /api/v1/reports/pdf?from=2025-01&to=2025-06
    Then el sistema debe responder con 200 OK
    And el Content-Type debe ser "application/pdf"
    And el header Content-Disposition debe contener "resumen-2025-01_2025-06.pdf"
    And el PDF debe incluir desglose por período y totales acumulados

  Scenario: Rango sin reportes
    Given que el usuario envía GET /api/v1/reports/pdf?from=2020-01&to=2020-06
    And no existen reportes en ese rango
    Then el sistema debe responder con 404 Not Found
    And el mensaje debe ser "No existen reportes en el rango seleccionado"

  Scenario: Rango con períodos parciales
    Given que en el rango "2025-01" a "2025-06" solo existen reportes para "2025-01", "2025-03" y "2025-05"
    When el usuario solicita el resumen PDF
    Then el PDF debe incluir solo los períodos con datos disponibles
    And debe incluir una nota indicando los períodos sin reportes

  Scenario: Rango inválido
    Given que el usuario envía from=2025-06 y to=2025-01
    When el sistema valida los parámetros
    Then debe responder con 400 Bad Request
    And el mensaje debe indicar "El período de inicio debe ser anterior al período de fin"
```

**Endpoint REST:** `GET /api/v1/reports/pdf?from={period}&to={period}`
**Dependencias:** US-021, NFR-001, NFR-002

---

## 5. Resumen del Backlog

### Historias No Funcionales / Técnicas

| ID | Título | Prioridad | Sprint sugerido |
|---|---|---|---|
| NFR-001 | Migración de Firebase a PostgreSQL | 🔴 Crítica | Sprint 1 |
| NFR-002 | Autenticación y autorización con JWT | 🔴 Crítica | Sprint 1 |
| NFR-003 | Contenerización con Docker | 🔴 Crítica | Sprint 1 |
| NFR-004 | Estándares de API REST | 🟡 Alta | Sprint 1 |

### Historias Funcionales

| ID | Título | Prioridad | Sprint sugerido |
|---|---|---|---|
| US-023 | Editar transacción financiera | 🔴 Crítica | Sprint 2 |
| US-024 | Sincronización automática de reportes | 🔴 Crítica | Sprint 2 |
| US-017 | Eliminar reporte de un período | 🟡 Alta | Sprint 2 |
| US-021 | Descargar reporte como PDF | 🟡 Alta | Sprint 3 |
| US-018 | Eliminación masiva por rango | 🟢 Media | Sprint 3 |
| US-022 | Descargar resumen por rango como PDF | 🟢 Media | Sprint 3 |

### Historias Eliminadas del Backlog Original

| ID Original | Título | Motivo de eliminación |
|---|---|---|
| US-019 | Recalcular un reporte financiero | ❌ La recalculación manual contradice la decisión arquitectónica de actualización automática de reportes (US-024). |
| US-020 | Notificación de diferencia tras recalculación | ❌ Dependía de US-019 (recalculación manual). Al eliminarse la recalculación manual, esta historia pierde su contexto y valor. |

---

## 6. Dependencias entre Historias

```
NFR-001 (PostgreSQL) ──────────────┐
                                   ├── NFR-003 (Docker)
NFR-002 (JWT) ─── depende de ──── NFR-001
                                   │
                                   ├── US-023 (Editar Transacción)
                                   │     └── US-024 (Sincronización Automática) ← depende de US-023
                                   │
                                   ├── US-017 (Eliminar Reporte)
                                   │     └── US-018 (Eliminación Masiva) ← extiende US-017
                                   │
                                   └── US-021 (Descargar PDF Individual)
                                         └── US-022 (Descargar PDF Rango) ← extiende US-021
```

**Ruta crítica:** `NFR-001 → NFR-002 → NFR-003 → US-023 → US-024`

---

## 7. Definition of Ready (DoR)

Antes de que una historia entre a un sprint de desarrollo, debe cumplir:

- [ ] Historia revisada y aprobada por el Product Owner.
- [ ] Criterios de aceptación (Gherkin) revisados por QA.
- [ ] Dependencias técnicas identificadas y resueltas o planificadas.
- [ ] Endpoint REST definido (método HTTP, ruta, request/response).
- [ ] Estimación de esfuerzo completada por el equipo de desarrollo.
- [ ] Sin dependencias bloqueantes no resueltas.
- [ ] Variables de entorno y configuración de Docker documentadas (si aplica).

---

## 8. Definition of Done (DoD)

Una historia se considera **completada** cuando:

- [ ] Todos los escenarios Gherkin pasan satisfactoriamente en pruebas automatizadas.
- [ ] El endpoint responde correctamente con los códigos HTTP esperados.
- [ ] Las pruebas de integración validan la persistencia en PostgreSQL.
- [ ] La autenticación JWT fue verificada en todos los endpoints.
- [ ] El código superó la revisión de pares (code review).
- [ ] El servicio funciona correctamente dentro del contenedor Docker.
- [ ] La documentación de la API fue actualizada (Swagger/OpenAPI si aplica).
- [ ] La historia fue demostrada al Product Owner en la revisión del sprint.
- [ ] No existen defectos críticos o bloqueantes abiertos.

---