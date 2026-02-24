# Plan de Refactorización: Report Service & Domain

## 1. Alcance (Scope)
Este plan se centra en resolver los problemas críticos detallados en la Auditoría de Arquitectura del Backend, específicamente atacando la deuda técnica y las violaciones a los principios SOLID en el microservicio de reportes (`report`).

### **Qué se va a tocar (In Scope):**
- `ReportServiceImpl`: Descomposición de esta "God Class". Se eliminará el acoplamiento con la API de Servlets (`RequestContextHolder`), se extraerán las llamadas de red a un cliente dedicado y se añadirá manejo de idempotencia.
- `ReportService` (Interfaz): Segregación de interfaces aplicando el principio CQRS (separar `ReportQueryService` y `ReportCommandService`).
- `Report` (Entidad): Evolucionar de un Modelo de Dominio Anémico a un Modelo Rico, moviendo la lógica de cálculo de balances (`accumulateTransactionAmount`) dentro de la entidad.
- **Comunicación HTTP:** Reemplazar el uso de `RestTemplate` y URLs "hardcodeadas" (`http://transaction:8081`) por un cliente declarativo (ej. Feign Client) y configuración por propiedades/descubrimiento.
- **Manejo de Excepciones:** Sustituir excepciones genéricas por excepciones de dominio/integración (ej. `ServiceIntegrationException`).
- **Consumidor RabbitMQ:** Implementación de idempotencia en `updateReport` mediante una tabla de control (`ProcessedMessage`).

### **Qué NO se va a tocar (Out of Scope):**
- Reescritura profunda de los microservicios `auth` o `transaction`, salvo adaptaciones menores necesarias para integraciones.
- Cambios de paradigma o reemplazo de la base de datos subyacente o del Message Broker (RabbitMQ no será reemplazado).
- Migración de frameworks (se mantiene Spring Boot).

---

## 2. Objetivos Concretos
- **Reducir el Acoplamiento:** Aislar la capa de servicio de contextos web (`HttpServletRequest`) y de dependencias directas en la infraestructura.
- **Mejorar la Cohesión (SRP y ISP):** Dividir responsabilidades. Un servicio no debe calcular, orquestar red, extraer tokens y manejar eventos simultáneamente.
- **Mejorar la Legibilidad y Mantenibilidad:** Eliminar magic strings, hardcoded URLs y sentencias `if-else` extensas usando polimorfismo o modelos ricos.
- **Garantizar la Integridad de Datos (Idempotencia):** Prevenir la corrupción financiera (duplicidad de saldos) en casos de reintentos por parte de RabbitMQ.
- **Aumentar la Testabilidad:** Hacer posible el 100% de cobertura en tests unitarios aislando la lógica de negocio de la infraestructura y el framework.

---

## 3. Riesgos Identificados
- **Corrupción de Datos durante la Transformación:** Al cambiar cómo se calculan los saldos en el `Report`, podríamos introducir errores lógicos que desfasen los balances.
- **Fallos en Flujos Asíncronos:** Modificar la lógica de consumo de RabbitMQ para incluir idempotencia podría interrumpir el flujo normal de eventos si no se manejan bien las transacciones de BD.
- **Ruptura de la Comunicación Inter-servicios:** El cambio de `RestTemplate` "hardcodeado" a Feign Clients o configuración dinámica puede generar problemas de resolución y causar Timeouts o errores 404/501.
- **Regresión por Dependencias Ocultas:** Podría haber código no documentado que dependa subrepticiamente del estado actual del `RequestContextHolder`.

---

## 4. Plan de Rollback (Mitigación)
1. **Versionado de Código Seguro:** Todo el refactor se realizará en una rama específica (`feature/reports-refactor`). Bajo ningún concepto se trabajará directamente sobre `main` o `develop`.
2. **Reversión de Commits:** Si una vez fusionada la rama se detectan fallos críticos en el entorno de staging o producción, proceder inmediatamente con un `git revert` del merge commit de la funcionalidad completada y redesplegar el estado anterior.
3. **Rollback de Esquema de Base de Datos:** Si se introducen migraciones (ej. Flyway/Liquibase) para la tabla `ProcessedMessage`, se debe disponer de un script "down" o de reversión. Si no interfiere, la tabla se puede dejar inactiva sin afectar la versión anterior del código.

---

## 5. Regla de Oro / Prerrequisito Técnico (TDD)
> 🚨 **IMPORTANTE: TESTS ANTES QUE EL CÓDIGO** 🚨

Ninguna línea de código de producción detallada en el Alcance será modificada sin **antes** tener tests escritos.
No podemos saber si hemos roto algo sin una red de seguridad. El flujo de trabajo obligatorio será:
1. **Escribir Tests de Caracterización (Behavior Tests):** Escribir tests que validen el comportamiento **actual** de las funciones antes de borrarlas o cambiarlas (para asegurar que entendemos la entrada y salida actuales).
2. **Escribir Nuevos Tests (Red):** Definir el comportamiento esperado de las nuevas clases (ej. Feign Client, Modelo Rico, Idempotencia).
3. **Refactorizar / Implementar (Green):** Mover el código y asegurar que los nuevos tests, y los modificados, pasen de manera exitosa.
4. **Limpiar (Refactor):** Pulir el código.
