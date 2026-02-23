# TEST_PLAN.md — Plan de Pruebas Formal
### Budget Management App — Historias de Usuario US-017 a US-022
> **Tipo de Documento:** Plan de Pruebas (Pre-implementación — Fase TDD)
> **Fecha:** 2026-02-19
> **Estándar de referencia:** IEEE 829 / ISTQB Foundation Level v4.0
> **Alcance:** Eliminación de Reportes · Recalculación de Reportes · Descarga de PDF
> **Idioma:** Español

---

## 1. Teoría Aplicada

### 1.1 Principios de Pruebas Aplicables (ISTQB)

A continuación se presentan los **7 Principios de Pruebas** definidos por el ISTQB y cómo se aplican al contexto de las historias US-017 a US-022.

| # | Principio ISTQB | Aplicación al Proyecto |
|---|---|---|
| 1 | **Las pruebas muestran la presencia de defectos, no su ausencia** | Las pruebas diseñadas para US-017 a US-022 permitirán detectar defectos en la lógica de eliminación, recalculación y generación de PDF, pero su ejecución satisfactoria no garantiza la ausencia total de errores. Por ello, se complementan múltiples técnicas de diseño (partición de equivalencia, valores límite, tablas de decisión). |
| 2 | **Las pruebas exhaustivas son imposibles** | No es viable probar todas las combinaciones posibles de períodos, rangos, estados de autenticación y tipos de error. Se aplicarán criterios de selección basados en riesgo y en las clases de equivalencia identificadas para cada historia, priorizando los escenarios definidos en los criterios de aceptación. |
| 3 | **Las pruebas tempranas ahorran tiempo y dinero** | Este plan se elabora **antes de la implementación** (fase TDD). Diseñar los casos de prueba a partir de los criterios de aceptación Gherkin permite detectar ambigüedades, contradicciones o vacíos en los requisitos antes de escribir código, reduciendo el costo de corrección. |
| 4 | **Los defectos se agrupan** | Se anticipan concentraciones de defectos en: (a) la lógica de validación de permisos y propiedad de reportes (`userId`), (b) el manejo de períodos en curso con transacciones activas (US-017, US-018), y (c) la generación de PDF con datos parciales o inexistentes (US-021, US-022). Estos componentes recibirán mayor cobertura de pruebas. |
| 5 | **Las pruebas se desgastan (Paradoja del pesticida)** | Si los mismos escenarios Gherkin se ejecutan repetidamente sin variación, dejarán de revelar nuevos defectos. Se recomienda actualizar periódicamente los datos de prueba, agregar casos exploratorios y revisar los escenarios cada sprint para mantener su efectividad. |
| 6 | **Las pruebas dependen del contexto** | Las pruebas de eliminación (US-017, US-018) se centran en integridad de datos y protección contra operaciones destructivas. Las pruebas de recalculación (US-019, US-020) se centran en precisión numérica. Las pruebas de PDF (US-021, US-022) se centran en la correcta generación del archivo y su contenido. Cada funcionalidad exige un enfoque diferente. |
| 7 | **La ausencia de defectos es una falacia** | Un sistema que pasa todas las pruebas no es necesariamente un sistema útil. Es fundamental validar que las funcionalidades cubran las necesidades reales del usuario (eliminación de reportes erróneos, corrección de datos inconsistentes, exportación para uso fiscal). Las pruebas de aceptación basadas en los escenarios Gherkin aseguran la alineación con el valor de negocio. |

---

### 1.2 Niveles de Prueba (Unitario / Integración / Sistema)

#### 1.2.1 Pruebas Unitarias

Verifican el comportamiento aislado de los componentes individuales sin dependencias externas.

| Componente / Unidad | Historias Relacionadas | Qué se prueba |
|---|---|---|
| Validación de período (`yyyy-MM`) | US-017, US-018, US-019, US-021, US-022 | Formato válido, período futuro, período vacío, período nulo |
| Validación de rango de períodos | US-018, US-022 | Período inicio ≤ período fin, rango vacío, rango con un solo mes |
| Lógica de propiedad del reporte (`userId`) | US-017, US-018, US-019 | El usuario solo opera sobre sus propios reportes |
| Verificación de período en curso con transacciones activas | US-017, US-018 | Detección correcta del período actual y existencia de transacciones |
| Cálculo de totales (`totalIncome`, `totalExpense`, `balance`) | US-019, US-020 | Suma aritmética correcta a partir de las transacciones del período |
| Comparación de valores pre/post recalculación | US-020 | Detección de diferencias, generación del mensaje con deltas |
| Generación de nombre de archivo PDF | US-021, US-022 | Formato `reporte-yyyy-MM.pdf` y `resumen-reporte-yyyy-MM_yyyy-MM.pdf` |
| Contenido esperado del PDF (estructura de datos) | US-021, US-022 | Inclusión de todos los campos requeridos (usuario, período, totales, fecha de generación) |

#### 1.2.2 Pruebas de Integración

Verifican la interacción correcta entre módulos y con dependencias externas.

| Interacción | Historias Relacionadas | Qué se prueba |
|---|---|---|
| Servicio de Reportes ↔ Base de Datos | US-017, US-018, US-019 | Eliminación efectiva del registro, persistencia de la recalculación, integridad referencial |
| Servicio de Reportes ↔ Servicio de Transacciones | US-017, US-019 | Verificación de transacciones activas antes de eliminar; lectura de transacciones para recalcular |
| Servicio de Reportes ↔ Generador de PDF | US-021, US-022 | Paso correcto de datos al generador, recepción del archivo generado |
| API REST ↔ Autenticación | US-017 a US-022 | Rechazo de solicitudes sin token válido; asociación correcta del `userId` |
| Controlador ↔ Servicio de Reportes | US-017 a US-022 | Transformación correcta de solicitudes HTTP a operaciones de servicio, códigos de respuesta HTTP adecuados |

#### 1.2.3 Pruebas de Sistema (End-to-End)

Verifican los flujos completos desde la interfaz de usuario hasta la persistencia, incluyendo feedback visual.

| Flujo E2E | Historias Relacionadas | Qué se prueba |
|---|---|---|
| Eliminar reporte → Confirmación → Tabla actualizada | US-017 | Flujo completo de eliminación individual con diálogo, notificación y actualización de la UI |
| Eliminar por rango → Confirmación → Resultados | US-018 | Flujo completo de eliminación masiva incluyendo exclusión de período en curso |
| Recalcular reporte → Estado de carga → Valores actualizados | US-019 | Flujo de recalculación con indicador de procesamiento y actualización en tiempo real |
| Recalcular → Notificación de diferencia | US-019 + US-020 | Flujo combinado: recalculación seguida de notificación con detalle de cambios |
| Descargar PDF individual → Generación → Descarga | US-021 | Generación y descarga del archivo con nombre correcto y contenido esperado |
| Descargar PDF de rango → Generación → Descarga | US-022 | Generación del resumen consolidado con períodos parciales y nota al pie |
| Acceso sin autenticación → Redirección | US-021 | Protección de ruta para usuarios no autenticados |

---

## 2. Técnicas de Diseño de Casos de Prueba

### 2.1 Partición de Equivalencia (por US)

La **Partición de Equivalencia (EP)** divide el dominio de entrada en clases donde se espera un comportamiento equivalente. Se selecciona al menos un representante por clase.

---

#### US-017 — Eliminar un Reporte Financiero de un Período

| Clase de Equivalencia | Tipo | Entrada representativa | Resultado esperado |
|---|---|---|---|
| Período válido con reporte existente propio | Válida | `"2025-03"` (reporte del usuario) | Eliminación exitosa tras confirmación |
| Período válido sin reporte | Inválida | `"2023-01"` (sin reporte) | Error: "El reporte no existe o ya fue eliminado" |
| Período en curso con transacciones activas | Inválida | `"2026-02"` (período actual) | Advertencia: eliminación bloqueada |
| Período con formato inválido | Inválida | `"03-2025"`, `"2025"`, `""` | Error de validación de formato |
| Reporte de otro usuario | Inválida | ID de reporte ajeno | Error de autorización / reporte no encontrado |

---

#### US-018 — Eliminación Masiva de Reportes por Rango

| Clase de Equivalencia | Tipo | Entrada representativa | Resultado esperado |
|---|---|---|---|
| Rango válido con todos los reportes existentes | Válida | `"2024-01"` a `"2024-06"` (6 reportes) | Eliminación masiva exitosa de 6 reportes |
| Rango válido que incluye período en curso | Válida parcial | `"2025-12"` a `"2026-02"` (período actual incluido) | Solo se eliminan los períodos anteriores al actual |
| Rango sin reportes | Inválida | `"2020-01"` a `"2020-06"` (vacío) | Mensaje: "No se encontraron reportes en el rango" |
| Rango con inicio > fin | Inválida | `"2025-06"` a `"2025-01"` | Error de validación de rango |
| Rango con formato inválido | Inválida | `"enero"` a `"junio"` | Error de validación de formato |

---

#### US-019 — Recalcular un Reporte Financiero

| Clase de Equivalencia | Tipo | Entrada representativa | Resultado esperado |
|---|---|---|---|
| Período con reporte y transacciones existentes | Válida | `"2025-11"` | Recalculación exitosa con valores actualizados |
| Período con reporte pero sin transacciones | Inválida | `"2024-09"` (sin transacciones) | Error: "No se encontraron transacciones para el período" |
| Período con reporte inexistente | Inválida | `"2023-07"` (eliminado) | Error: "El reporte no existe" |
| Período con formato inválido | Inválida | `"2025/11"`, `null` | Error de validación de formato |

---

#### US-020 — Notificación de Diferencia tras Recalculación

| Clase de Equivalencia | Tipo | Entrada representativa | Resultado esperado |
|---|---|---|---|
| Recalculación con cambios en totales | Válida | Ingresos: $1,000 → $1,200 | Notificación con detalle de diferencias y deltas |
| Recalculación sin cambios | Válida | Valores idénticos pre/post | Notificación: "No se detectaron diferencias" |
| Cambio solo en ingresos | Válida | Income cambia, expense igual | Notificación muestra solo los campos alterados |
| Cambio solo en gastos | Válida | Expense cambia, income igual | Notificación muestra solo los campos alterados |
| Cambio en todos los campos | Válida | Income, expense y balance cambian | Notificación muestra los tres campos con deltas |

---

#### US-021 — Descargar Reporte de un Período como PDF

| Clase de Equivalencia | Tipo | Entrada representativa | Resultado esperado |
|---|---|---|---|
| Período con reporte existente, usuario autenticado | Válida | `"2025-10"` | PDF generado y descargado como `reporte-2025-10.pdf` |
| Período con reporte inexistente | Inválida | `"2023-01"` (inexistente) | Error: "El reporte no existe" |
| Usuario no autenticado | Inválida | Sin token de sesión | Redirección a login |
| Error de sistema durante generación | Inválida | Fallo interno | Error: "No fue posible generar el PDF" |

---

#### US-022 — Descargar Resumen por Rango como PDF

| Clase de Equivalencia | Tipo | Entrada representativa | Resultado esperado |
|---|---|---|---|
| Rango con reportes en todos los períodos | Válida | `"2025-01"` a `"2025-06"` (6 reportes) | PDF consolidado con 6 períodos |
| Rango con reportes parciales | Válida | `"2025-01"` a `"2025-06"` (3 de 6 existen) | PDF con los 3 períodos disponibles + nota al pie |
| Rango sin reportes | Inválida | `"2020-01"` a `"2020-06"` | Error: "No existen reportes en el rango" |
| Rango con inicio > fin | Inválida | `"2025-06"` a `"2025-01"` | Error de validación de rango |
| Error de sistema durante generación | Inválida | Fallo interno | Error: "No fue posible generar el PDF del resumen" |

---

### 2.2 Valores Límite (por US)

El **Análisis de Valores Límite (BVA)** se enfoca en los bordes de las clases de equivalencia donde los defectos son más probables.

---

#### US-017 — Eliminar un Reporte Financiero

| Valor Límite | Descripción | Resultado esperado |
|---|---|---|
| Período = mes inmediatamente anterior al actual | Último mes histórico permitido (e.g., `"2026-01"`) | Eliminación permitida |
| Período = mes actual (con transacciones) | Borde inferior de restricción (`"2026-02"`) | Eliminación bloqueada |
| Período = mes actual (sin transacciones) | Período en curso sin restricción activa | Eliminación permitida (no hay transacciones activas) |
| Período = primer reporte del usuario | Límite inferior del historial | Eliminación permitida |
| Período = único reporte restante | Límite de estado del historial | Eliminación permitida, tabla queda vacía |

---

#### US-018 — Eliminación Masiva por Rango

| Valor Límite | Descripción | Resultado esperado |
|---|---|---|
| Rango de 1 solo mes (inicio = fin) | Rango mínimo: e.g., `"2024-03"` a `"2024-03"` | Eliminación de 1 reporte |
| Rango de 2 meses | Rango mínimo no trivial | Eliminación de 2 reportes |
| Rango que termina justo antes del período actual | Borde del período protegido: `"2024-01"` a `"2026-01"` | Todos los reportes del rango eliminados |
| Rango que incluye exactamente el período actual | Borde inclusivo: `"2025-06"` a `"2026-02"` | Se excluye `"2026-02"` si tiene transacciones activas |
| Rango de 12 meses (un año completo) | Rango amplio | Eliminación masiva correcta |
| Rango donde todos los meses tienen reporte | Cobertura total | Coincide el conteo mostrado con el real |
| Rango donde ningún mes tiene reporte | Cobertura cero | Mensaje de rango vacío |

---

#### US-019 — Recalcular un Reporte Financiero

| Valor Límite | Descripción | Resultado esperado |
|---|---|---|
| Período con exactamente 1 transacción | Mínimo de datos para recalcular | Totales = valores de esa única transacción |
| Período con cantidad máxima de transacciones | Límite superior de volumen | Recalculación correcta sin timeout |
| Transacción de monto $0.00 | Límite inferior de valor monetario | Incluida en el cálculo sin distorsión |
| Transacción con monto negativo (gasto) | Borde de signo | Clasificada correctamente como gasto |
| Balance resultante = $0.00 | Caso de equilibrio perfecto | Balance mostrado como $0.00, no como vacío |

---

#### US-020 — Notificación de Diferencia

| Valor Límite | Descripción | Resultado esperado |
|---|---|---|
| Diferencia de $0.01 (mínima detectable) | Cambio mínimo | Notificación con diferencia mostrada |
| Diferencia de $0.00 (sin cambio) | Borde exacto entre "cambió" y "no cambió" | Notificación: "No se detectaron diferencias" |
| Diferencia en un solo campo (ingreso/gasto/balance) | Cambio parcial | Notificación muestra solo el campo que cambió |
| Todos los campos cambian | Cambio completo | Notificación muestra los 3 campos con deltas |

---

#### US-021 — Descargar Reporte como PDF

| Valor Límite | Descripción | Resultado esperado |
|---|---|---|
| Reporte con todos los campos en $0.00 | Contenido mínimo | PDF generado con valores en $0.00 |
| Reporte con montos muy grandes (e.g., $9,999,999.99) | Límite superior de representación | PDF generado sin desbordamiento visual |
| Nombre de archivo con período `"2025-01"` | Formato estándar | Archivo: `reporte-2025-01.pdf` |
| Descarga inmediatamente después de recalculación | Dependencia temporal | PDF refleja datos recalculados más recientes |

---

#### US-022 — Descargar Resumen por Rango como PDF

| Valor Límite | Descripción | Resultado esperado |
|---|---|---|
| Rango de 1 solo mes | Rango mínimo | PDF con un solo período (sin totales acumulados significativos) |
| Rango de 12 meses | Rango de un año | PDF con 12 filas + totales acumulados |
| Rango donde solo existe 1 reporte de N posibles | Cobertura mínima | PDF incluye 1 período + nota al pie con N-1 períodos faltantes |
| Rango completo (todos los períodos con datos) | Cobertura total | PDF sin nota al pie de períodos faltantes |
| Nombre de archivo para rango `"2025-01"` a `"2025-06"` | Formato estándar | Archivo: `resumen-reporte-2025-01_2025-06.pdf` |

---

### 2.3 Tabla de Decisión (por US con lógica condicional)

Las **Tablas de Decisión** formalizan combinaciones de condiciones y sus acciones esperadas, ideales para lógica con múltiples reglas de negocio.

---

#### US-017 — Eliminar un Reporte Financiero

| Regla | Autenticado | Reporte existe | Es propio | Período en curso | Tiene tx activas | Confirma | Resultado |
|---|---|---|---|---|---|---|---|
| R1 | ✅ | ✅ | ✅ | ❌ | — | ✅ | ✅ Eliminación exitosa |
| R2 | ✅ | ✅ | ✅ | ❌ | — | ❌ | ❌ Cancelación, sin cambios |
| R3 | ✅ | ✅ | ✅ | ✅ | ✅ | — | ❌ Bloqueado: período en curso con tx |
| R4 | ✅ | ✅ | ✅ | ✅ | ❌ | ✅ | ✅ Eliminación permitida |
| R5 | ✅ | ❌ | — | — | — | — | ❌ Error: reporte inexistente |
| R6 | ✅ | ✅ | ❌ | — | — | — | ❌ Error: no autorizado |
| R7 | ❌ | — | — | — | — | — | ❌ Acceso denegado |
| R8 | ✅ | ✅ | ✅ | ❌ | — | ✅ (falla sistema) | ❌ Error interno; reporte sin cambios |

> **Leyenda:** `tx` = transacciones · `—` = no aplica / indiferente

---

#### US-018 — Eliminación Masiva por Rango

| Regla | Autenticado | Rango válido | Reportes en rango | Incluye período actual | Tx activas en actual | Confirma | Resultado |
|---|---|---|---|---|---|---|---|
| R1 | ✅ | ✅ | ✅ (todos) | ❌ | — | ✅ | ✅ Todos eliminados |
| R2 | ✅ | ✅ | ✅ (parcial) | ✅ | ✅ | ✅ | ✅ Se eliminan solo los anteriores al actual |
| R3 | ✅ | ✅ | ❌ | — | — | — | ❌ "No se encontraron reportes en el rango" |
| R4 | ✅ | ❌ (inicio > fin) | — | — | — | — | ❌ Error de validación de rango |
| R5 | ✅ | ✅ | ✅ | — | — | ❌ | ❌ Cancelación, sin cambios |
| R6 | ❌ | — | — | — | — | — | ❌ Acceso denegado |

---

#### US-019 — Recalcular un Reporte Financiero

| Regla | Autenticado | Reporte existe | Es propio | Hay transacciones | Valores cambian | Resultado |
|---|---|---|---|---|---|---|
| R1 | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ Recalculación exitosa, valores actualizados |
| R2 | ✅ | ✅ | ✅ | ✅ | ❌ | ✅ Notificación: sin diferencias (→ US-020) |
| R3 | ✅ | ✅ | ✅ | ❌ | — | ❌ Error: sin transacciones para el período |
| R4 | ✅ | ❌ | — | — | — | ❌ Error: reporte inexistente |
| R5 | ✅ | ✅ | ❌ | — | — | ❌ Error: no autorizado |
| R6 | ❌ | — | — | — | — | ❌ Acceso denegado |
| R7 | ✅ | ✅ | ✅ | ✅ | — (falla sistema) | ❌ Error interno; datos sin cambios |

---

#### US-020 — Notificación de Diferencia tras Recalculación

| Regla | Recalculación ejecutada | Income cambió | Expense cambió | Balance cambió | Resultado |
|---|---|---|---|---|---|
| R1 | ✅ | ✅ | ❌ | ✅ | Notificación: diferencia en income y balance con deltas |
| R2 | ✅ | ❌ | ✅ | ✅ | Notificación: diferencia en expense y balance con deltas |
| R3 | ✅ | ✅ | ✅ | ✅ | Notificación: diferencia en los 3 campos con deltas |
| R4 | ✅ | ❌ | ❌ | ❌ | Notificación: "No se detectaron diferencias" |
| R5 | ❌ (error) | — | — | — | Sin notificación de diferencia (error manejado en US-019) |

---

#### US-021 — Descargar Reporte como PDF

| Regla | Autenticado | Reporte existe | Generación OK | Resultado |
|---|---|---|---|---|
| R1 | ✅ | ✅ | ✅ | ✅ Descarga de `reporte-yyyy-MM.pdf` |
| R2 | ✅ | ❌ | — | ❌ Error: reporte inexistente |
| R3 | ✅ | ✅ | ❌ (error) | ❌ Error: fallo en generación |
| R4 | ❌ | — | — | ❌ Redirección a login |

---

#### US-022 — Descargar Resumen por Rango como PDF

| Regla | Autenticado | Rango válido | Reportes en rango | Períodos parciales | Generación OK | Resultado |
|---|---|---|---|---|---|---|
| R1 | ✅ | ✅ | ✅ (todos) | ❌ | ✅ | ✅ PDF con todos los períodos |
| R2 | ✅ | ✅ | ✅ (parcial) | ✅ | ✅ | ✅ PDF con períodos disponibles + nota al pie |
| R3 | ✅ | ✅ | ❌ | — | — | ❌ "No existen reportes en el rango" |
| R4 | ✅ | ❌ | — | — | — | ❌ Error de validación de rango |
| R5 | ✅ | ✅ | ✅ | — | ❌ (error) | ❌ Error: fallo en generación |
| R6 | ❌ | — | — | — | — | ❌ Acceso denegado |

---

## 3. Actividad 3.1 — Escenarios Gherkin / Checklists por Historia

> Los escenarios a continuación se derivan de los criterios de aceptación en `new-stories.md`. Cada escenario se presenta de forma **individual** e incluye su **flujo de commits TDD** (RED → GREEN → REFACTOR) para guiar la implementación incremental.

### 🔄 Metodología TDD — Flujo de Commits por Escenario

Cada escenario Gherkin se implementa siguiendo un ciclo estricto de 3 commits:

| Fase | Commit Prefix | Descripción | Ejemplo de mensaje de commit |
|---|---|---|---|
| 🔴 **RED** | `test:` | Escribir el test que **falla**. Se codifica el escenario Gherkin como test automatizado. No se escribe lógica de negocio. | `test: US-017 — eliminación exitosa de reporte (RED)` |
| 🟢 **GREEN** | `feat:` | Implementar el **código mínimo** para que el test pase. Usar GitHub Copilot para generar boilerplate. | `feat: US-017 — implementar eliminación de reporte (GREEN)` |
| 🔵 **REFACTOR** | `refactor:` | Mejorar el código **sin romper el test**. Limpiar duplicaciones, mejorar nombres, aplicar patrones. | `refactor: US-017 — extraer validación de período a servicio (REFACTOR)` |

> **Regla:** No avanzar al siguiente escenario hasta completar el ciclo RED → GREEN → REFACTOR del escenario actual.

---

### US-017 — Eliminar un Reporte Financiero de un Período

**Antecedentes comunes:** El usuario está autenticado y se encuentra en la página de Reportes.

---

#### Escenario 1/5: Eliminación exitosa de un reporte

```gherkin
Escenario: Eliminación exitosa de un reporte
  Dado que existe un reporte propio para el período "2025-03"
  Cuando el usuario selecciona "Eliminar" para el reporte de "2025-03"
  Entonces el sistema muestra un diálogo de confirmación con el mensaje:
    "¿Estás seguro de que deseas eliminar el reporte del período 2025-03? Esta acción no se puede deshacer."
  Cuando el usuario confirma la eliminación haciendo clic en "Confirmar"
  Entonces el reporte es eliminado del sistema
  Y la tabla de historial se actualiza y ya no muestra el reporte de "2025-03"
  Y se muestra una notificación de éxito: "Reporte eliminado correctamente"
```

**Ciclo TDD:**

| Fase | Commit | Qué hacer |
|---|---|---|
| 🔴 RED | `test: US-017-E1 eliminación exitosa de reporte` | Crear test que invoque `deleteReport("2025-03")`, verifique que retorna éxito y que el reporte ya no existe en el repositorio. El test **debe fallar**. |
| 🟢 GREEN | `feat: US-017-E1 implementar eliminación de reporte` | Implementar `ReportService.deleteReport()` con lógica mínima: buscar reporte → eliminar → retornar resultado. Usar Copilot para boilerplate del servicio. |
| 🔵 REFACTOR | `refactor: US-017-E1 extraer validación de propiedad` | Extraer la verificación de `userId` a un método reutilizable. Mejorar nombres de variables. |

---

#### Escenario 2/5: El usuario cancela la eliminación

```gherkin
Escenario: El usuario cancela la eliminación
  Dado que existe un reporte para el período "2025-05"
  Cuando el usuario selecciona "Eliminar" para el reporte de "2025-05"
  Y el sistema muestra el diálogo de confirmación
  Cuando el usuario hace clic en "Cancelar"
  Entonces el diálogo se cierra
  Y el reporte de "2025-05" permanece intacto en el sistema
```

**Ciclo TDD:**

| Fase | Commit | Qué hacer |
|---|---|---|
| 🔴 RED | `test: US-017-E2 cancelación de eliminación` | Crear test que simule cancelación y verifique que el reporte **sigue existiendo** sin modificaciones. |
| 🟢 GREEN | `feat: US-017-E2 manejar cancelación en diálogo` | Agregar lógica de cancelación en el componente UI (el servicio ya existe del E1). |
| 🔵 REFACTOR | `refactor: US-017-E2 unificar manejo de diálogos` | Extraer componente de diálogo de confirmación reutilizable. |

---

#### Escenario 3/5: Período en curso con transacciones activas

```gherkin
Escenario: Intento de eliminar un reporte del período en curso con transacciones activas
  Dado que el período actual es "2026-02"
  Y el reporte del período "2026-02" tiene transacciones registradas
  Cuando el usuario intenta eliminar el reporte de "2026-02"
  Entonces el sistema muestra un mensaje de advertencia:
    "No es posible eliminar el reporte del período en curso mientras existan transacciones activas asociadas."
  Y la opción de confirmar la eliminación está deshabilitada
```

**Ciclo TDD:**

| Fase | Commit | Qué hacer |
|---|---|---|
| 🔴 RED | `test: US-017-E3 bloqueo eliminación período en curso` | Crear test que intente eliminar reporte del período actual con transacciones activas y verifique que lanza excepción/error de validación. |
| 🟢 GREEN | `feat: US-017-E3 validar período en curso antes de eliminar` | Agregar validación en `ReportService`: verificar si el período es el actual y si tiene transacciones activas. |
| 🔵 REFACTOR | `refactor: US-017-E3 extraer regla de negocio a dominio` | Mover la regla de "período en curso protegido" al modelo de dominio (`Report.isDeletable()`). |

---

#### Escenario 4/5: Reporte inexistente

```gherkin
Escenario: Intento de eliminar un reporte que no existe
  Cuando el sistema intenta procesar una solicitud de eliminación para un reporte inexistente
  Entonces el sistema muestra un mensaje de error:
    "El reporte que intentas eliminar no existe o ya fue eliminado."
  Y la tabla de historial permanece sin cambios
```

**Ciclo TDD:**

| Fase | Commit | Qué hacer |
|---|---|---|
| 🔴 RED | `test: US-017-E4 eliminación de reporte inexistente` | Crear test que intente eliminar un `reportId` inexistente y verifique que retorna error `NOT_FOUND`. |
| 🟢 GREEN | `feat: US-017-E4 manejar reporte no encontrado` | Agregar verificación de existencia en el servicio antes de eliminar. Lanzar `ReportNotFoundException`. |
| 🔵 REFACTOR | `refactor: US-017-E4 unificar excepciones de dominio` | Crear jerarquía de excepciones de dominio si no existe. |

---

#### Escenario 5/5: Error interno del sistema

```gherkin
Escenario: La eliminación falla por un error del sistema
  Dado que el usuario confirma la eliminación de un reporte
  Y ocurre un error interno durante el proceso
  Entonces el sistema muestra un mensaje de error:
    "No fue posible eliminar el reporte. Por favor, inténtalo de nuevo más tarde."
  Y el reporte permanece en el sistema sin cambios
```

**Ciclo TDD:**

| Fase | Commit | Qué hacer |
|---|---|---|
| 🔴 RED | `test: US-017-E5 error interno durante eliminación` | Crear test que simule fallo de base de datos (mock) y verifique que el reporte **no se elimina** y se retorna error 500. |
| 🟢 GREEN | `feat: US-017-E5 manejo de errores en eliminación` | Agregar `try-catch` en el servicio, retornar respuesta de error apropiada. |
| 🔵 REFACTOR | `refactor: US-017-E5 centralizar manejo de errores` | Implementar `@ControllerAdvice` o handler global de excepciones si no existe. |

---

#### Checklist complementario US-017

- [ ] Las transacciones originales del período NO se eliminan tras borrar el reporte
- [ ] El usuario no puede eliminar reportes de otro usuario (verificación de `userId`)
- [ ] El diálogo de confirmación muestra el período correcto
- [ ] Tras eliminación exitosa, la tabla se actualiza sin recargar la página completa
- [ ] Si el reporte es el único, la tabla queda vacía con mensaje apropiado

#### Resumen de commits US-017

| # | Mensaje de Commit | Fase |
|---|---|---|
| 1 | `test: US-017-E1 eliminación exitosa de reporte` | 🔴 |
| 2 | `feat: US-017-E1 implementar eliminación de reporte` | 🟢 |
| 3 | `refactor: US-017-E1 extraer validación de propiedad` | 🔵 |
| 4 | `test: US-017-E2 cancelación de eliminación` | 🔴 |
| 5 | `feat: US-017-E2 manejar cancelación en diálogo` | 🟢 |
| 6 | `refactor: US-017-E2 unificar manejo de diálogos` | 🔵 |
| 7 | `test: US-017-E3 bloqueo eliminación período en curso` | 🔴 |
| 8 | `feat: US-017-E3 validar período en curso antes de eliminar` | 🟢 |
| 9 | `refactor: US-017-E3 extraer regla de negocio a dominio` | 🔵 |
| 10 | `test: US-017-E4 eliminación de reporte inexistente` | 🔴 |
| 11 | `feat: US-017-E4 manejar reporte no encontrado` | 🟢 |
| 12 | `refactor: US-017-E4 unificar excepciones de dominio` | 🔵 |
| 13 | `test: US-017-E5 error interno durante eliminación` | 🔴 |
| 14 | `feat: US-017-E5 manejo de errores en eliminación` | 🟢 |
| 15 | `refactor: US-017-E5 centralizar manejo de errores` | 🔵 |

---

### US-018 — Eliminación Masiva de Reportes por Rango de Período

**Antecedentes comunes:** El usuario está autenticado y se encuentra en la página de Reportes.

---

#### Escenario 1/4: Eliminación masiva exitosa en rango válido

```gherkin
Escenario: Eliminación masiva exitosa de reportes en un rango válido
  Dado que existen reportes para los períodos "2024-01" a "2024-06"
  Cuando el usuario selecciona "Eliminar por rango"
  Y especifica inicio "2024-01" y fin "2024-06"
  Entonces el sistema muestra confirmación: "Se eliminarán 6 reportes del período 2024-01 al 2024-06."
  Cuando el usuario confirma
  Entonces todos los reportes del rango son eliminados
  Y se muestra: "6 reportes eliminados correctamente."
```

**Ciclo TDD:**

| Fase | Commit | Qué hacer |
|---|---|---|
| 🔴 RED | `test: US-018-E1 eliminación masiva por rango` | Test que invoque `deleteReportsByRange("2024-01", "2024-06")` y verifique que los 6 reportes se eliminan. |
| 🟢 GREEN | `feat: US-018-E1 implementar eliminación por rango` | Crear `ReportService.deleteByRange()` con query al repositorio por rango de períodos. Copilot para boilerplate. |
| 🔵 REFACTOR | `refactor: US-018-E1 reutilizar lógica de eliminación individual` | Extraer lógica compartida con US-017. Aplicar DRY. |

---

#### Escenario 2/4: Rango incluye período en curso

```gherkin
Escenario: El rango incluye reportes del período en curso
  Dado que el usuario selecciona un rango que incluye el período actual con transacciones activas
  Cuando el sistema valida el rango
  Entonces muestra advertencia: "Solo se eliminarán los períodos anteriores."
  Y al confirmar, únicamente se eliminan los reportes anteriores al período actual
```

**Ciclo TDD:**

| Fase | Commit | Qué hacer |
|---|---|---|
| 🔴 RED | `test: US-018-E2 rango con período en curso` | Test que incluya período actual en el rango y verifique que se excluye de la eliminación. |
| 🟢 GREEN | `feat: US-018-E2 filtrar período en curso del rango` | Agregar filtro en `deleteByRange()` que excluya el período actual si tiene transacciones activas. |
| 🔵 REFACTOR | `refactor: US-018-E2 extraer filtro de períodos protegidos` | Mover lógica de filtrado a un método reutilizable `filterDeletablePeriods()`. |

---

#### Escenario 3/4: Rango sin reportes

```gherkin
Escenario: El rango seleccionado no contiene reportes
  Dado que el usuario define un rango sin reportes existentes
  Cuando confirma la operación
  Entonces el sistema muestra: "No se encontraron reportes en el rango seleccionado."
  Y no se realiza ninguna eliminación
```

**Ciclo TDD:**

| Fase | Commit | Qué hacer |
|---|---|---|
| 🔴 RED | `test: US-018-E3 rango vacío sin reportes` | Test con rango donde no existen reportes, verificar que retorna mensaje informativo y count = 0. |
| 🟢 GREEN | `feat: US-018-E3 manejar rango sin resultados` | Agregar validación de resultados vacíos antes de ejecutar eliminación. |
| 🔵 REFACTOR | `refactor: US-018-E3 mejorar respuesta del servicio` | Retornar DTO con conteo y mensaje en vez de solo boolean. |

---

#### Escenario 4/4: Cancelación de eliminación masiva

```gherkin
Escenario: El usuario cancela la eliminación masiva
  Dado que el sistema muestra el diálogo de confirmación
  Cuando el usuario hace clic en "Cancelar"
  Entonces el diálogo se cierra y ningún reporte es eliminado
```

**Ciclo TDD:**

| Fase | Commit | Qué hacer |
|---|---|---|
| 🔴 RED | `test: US-018-E4 cancelación eliminación masiva` | Test que simule cancelación y verifique que **ningún** reporte fue eliminado. |
| 🟢 GREEN | `feat: US-018-E4 cancelación en diálogo masivo` | Reutilizar componente de diálogo de US-017. |
| 🔵 REFACTOR | `refactor: US-018-E4 parametrizar diálogo de confirmación` | Hacer configurable el mensaje del diálogo (individual vs. masivo). |

---

#### Checklist complementario US-018

- [ ] El conteo de reportes a eliminar coincide con la cantidad real en el rango
- [ ] Validación de que el período inicio ≤ período fin
- [ ] Si el rango incluye el período actual, el mensaje de advertencia es claro sobre qué se excluye
- [ ] Las transacciones subyacentes no se ven afectadas por la eliminación masiva
- [ ] Formato del rango mostrado en el diálogo es consistente (`yyyy-MM`)

#### Resumen de commits US-018

| # | Mensaje de Commit | Fase |
|---|---|---|
| 1 | `test: US-018-E1 eliminación masiva por rango` | 🔴 |
| 2 | `feat: US-018-E1 implementar eliminación por rango` | 🟢 |
| 3 | `refactor: US-018-E1 reutilizar lógica de eliminación individual` | 🔵 |
| 4 | `test: US-018-E2 rango con período en curso` | 🔴 |
| 5 | `feat: US-018-E2 filtrar período en curso del rango` | 🟢 |
| 6 | `refactor: US-018-E2 extraer filtro de períodos protegidos` | 🔵 |
| 7 | `test: US-018-E3 rango vacío sin reportes` | 🔴 |
| 8 | `feat: US-018-E3 manejar rango sin resultados` | 🟢 |
| 9 | `refactor: US-018-E3 mejorar respuesta del servicio` | 🔵 |
| 10 | `test: US-018-E4 cancelación eliminación masiva` | 🔴 |
| 11 | `feat: US-018-E4 cancelación en diálogo masivo` | 🟢 |
| 12 | `refactor: US-018-E4 parametrizar diálogo de confirmación` | 🔵 |

---

### US-019 — Recalcular un Reporte Financiero

**Antecedentes comunes:** El usuario está autenticado y se encuentra en la página de Reportes.

---

#### Escenario 1/5: Recalculación exitosa

```gherkin
Escenario: Recalculación exitosa de un reporte
  Dado que existe un reporte propio para el período "2025-11"
  Cuando el usuario selecciona "Actualizar / Recalcular" sobre el reporte de "2025-11"
  Entonces el sistema recalcula totalIncome, totalExpense y balance
  Y el reporte muestra los valores actualizados
  Y se muestra: "Reporte del período 2025-11 actualizado correctamente."
```

**Ciclo TDD:**

| Fase | Commit | Qué hacer |
|---|---|---|
| 🔴 RED | `test: US-019-E1 recalculación exitosa` | Test que invoque `recalculateReport("2025-11")` con transacciones mock y verifique que los totales coinciden con la suma aritmética. |
| 🟢 GREEN | `feat: US-019-E1 implementar recalculación` | Crear `ReportService.recalculate()`: obtener transacciones → sumar → actualizar reporte. Copilot para boilerplate. |
| 🔵 REFACTOR | `refactor: US-019-E1 extraer calculadora de totales` | Mover lógica de suma a un componente `ReportCalculator` reutilizable. |

---

#### Escenario 2/5: Estado de procesamiento durante la recalculación

```gherkin
Escenario: El sistema indica estado de procesamiento
  Dado que el usuario solicita la recalculación de un reporte
  Cuando el sistema está procesando
  Entonces el botón "Actualizar / Recalcular" se deshabilita y muestra "Procesando..."
  Y la fila muestra un indicador de carga
  Al completarse, los datos actualizados son mostrados automáticamente
```

**Ciclo TDD:**

| Fase | Commit | Qué hacer |
|---|---|---|
| 🔴 RED | `test: US-019-E2 estado de carga durante recalculación` | Test de componente UI que verifique: botón deshabilitado durante proceso, indicador visible, restauración al completar. |
| 🟢 GREEN | `feat: US-019-E2 indicador de carga en recalculación` | Agregar estado `isProcessing` al componente, deshabilitar botón, mostrar spinner. |
| 🔵 REFACTOR | `refactor: US-019-E2 crear hook useAsyncAction` | Extraer patrón de estado de carga a un hook o utilidad reutilizable para operaciones async. |

---

#### Escenario 3/5: Sin transacciones para el período

```gherkin
Escenario: No existen transacciones para el período a recalcular
  Dado que el usuario solicita recalcular el reporte de "2024-09"
  Y no existen transacciones registradas para ese período
  Entonces el sistema muestra: "No se encontraron transacciones para el período seleccionado."
  Y el reporte permanece sin cambios
```

**Ciclo TDD:**

| Fase | Commit | Qué hacer |
|---|---|---|
| 🔴 RED | `test: US-019-E3 recalculación sin transacciones` | Test que invoque recalculación para período sin transacciones y verifique error descriptivo. |
| 🟢 GREEN | `feat: US-019-E3 validar existencia de transacciones` | Agregar verificación previa en el servicio: si no hay transacciones, lanzar `NoTransactionsException`. |
| 🔵 REFACTOR | `refactor: US-019-E3 unificar validaciones pre-recalculación` | Agrupar las validaciones (existencia de reporte, transacciones) en un método `validateRecalculation()`. |

---

#### Escenario 4/5: Error interno del sistema

```gherkin
Escenario: La recalculación falla por un error del sistema
  Dado que el usuario solicita la recalculación de un reporte
  Y ocurre un error interno durante el procesamiento
  Entonces el sistema muestra: "No fue posible actualizar el reporte. Inténtalo de nuevo más tarde."
  Y los datos del reporte permanecen sin cambios
```

**Ciclo TDD:**

| Fase | Commit | Qué hacer |
|---|---|---|
| 🔴 RED | `test: US-019-E4 error interno en recalculación` | Test que simule fallo del repositorio (mock) y verifique que los datos del reporte **no cambian**. |
| 🟢 GREEN | `feat: US-019-E4 manejo de errores en recalculación` | Agregar `try-catch`, rollback implícito, respuesta de error. |
| 🔵 REFACTOR | `refactor: US-019-E4 reutilizar handler de errores de US-017` | Integrar con el `@ControllerAdvice` creado en US-017-E5. |

---

#### Escenario 5/5: Reporte inexistente

```gherkin
Escenario: Recalcular un reporte que no existe
  Dado que el usuario intenta recalcular un reporte eliminado previamente
  Cuando el sistema procesa la solicitud
  Entonces se muestra: "El reporte que intentas actualizar no existe."
  Y no se realiza ningún cambio
```

**Ciclo TDD:**

| Fase | Commit | Qué hacer |
|---|---|---|
| 🔴 RED | `test: US-019-E5 recalcular reporte inexistente` | Test que intente recalcular un `reportId` inexistente y verifique error `NOT_FOUND`. |
| 🟢 GREEN | `feat: US-019-E5 validar existencia antes de recalcular` | Reutilizar `ReportNotFoundException` de US-017-E4. |
| 🔵 REFACTOR | `refactor: US-019-E5 crear base service con validaciones comunes` | Extraer validaciones compartidas (existencia, propiedad) a una clase base o servicio común. |

---

#### Checklist complementario US-019

- [ ] Los valores recalculados coinciden con la suma aritmética de las transacciones del período
- [ ] El campo `balance` = `totalIncome` − `totalExpense` tras la recalculación
- [ ] Un usuario no puede recalcular reportes de otro usuario
- [ ] El indicador de carga se muestra y se oculta correctamente
- [ ] Si la recalculación produce los mismos valores, se notifica que no hubo cambios (→ US-020)
- [ ] Las transacciones no se modifican como consecuencia de la recalculación

#### Resumen de commits US-019

| # | Mensaje de Commit | Fase |
|---|---|---|
| 1 | `test: US-019-E1 recalculación exitosa` | 🔴 |
| 2 | `feat: US-019-E1 implementar recalculación` | 🟢 |
| 3 | `refactor: US-019-E1 extraer calculadora de totales` | 🔵 |
| 4 | `test: US-019-E2 estado de carga durante recalculación` | 🔴 |
| 5 | `feat: US-019-E2 indicador de carga en recalculación` | 🟢 |
| 6 | `refactor: US-019-E2 crear hook useAsyncAction` | 🔵 |
| 7 | `test: US-019-E3 recalculación sin transacciones` | 🔴 |
| 8 | `feat: US-019-E3 validar existencia de transacciones` | 🟢 |
| 9 | `refactor: US-019-E3 unificar validaciones pre-recalculación` | 🔵 |
| 10 | `test: US-019-E4 error interno en recalculación` | 🔴 |
| 11 | `feat: US-019-E4 manejo de errores en recalculación` | 🟢 |
| 12 | `refactor: US-019-E4 reutilizar handler de errores de US-017` | 🔵 |
| 13 | `test: US-019-E5 recalcular reporte inexistente` | 🔴 |
| 14 | `feat: US-019-E5 validar existencia antes de recalcular` | 🟢 |
| 15 | `refactor: US-019-E5 crear base service con validaciones comunes` | 🔵 |

---

### US-020 — Notificación de Diferencia Detectada tras Recalculación

---

#### Escenario 1/2: La recalculación detecta diferencias

```gherkin
Escenario: La recalculación detecta diferencias en los totales
  Dado que el usuario solicita la recalculación del reporte de "2025-08"
  Y los valores anteriores eran: Ingresos $1,000 / Gastos $400 / Balance $600
  Cuando el sistema recalcula y obtiene: Ingresos $1,200 / Gastos $400 / Balance $800
  Entonces se muestra notificación detallada con los campos que cambiaron y sus deltas
```

**Ciclo TDD:**

| Fase | Commit | Qué hacer |
|---|---|---|
| 🔴 RED | `test: US-020-E1 notificación con diferencias detectadas` | Test que compare valores pre/post y verifique que la notificación incluye solo los campos con cambios y deltas correctos (+/-). |
| 🟢 GREEN | `feat: US-020-E1 comparar y notificar diferencias` | Crear `DifferenceDetector.compare(oldReport, newReport)` que retorne lista de cambios con deltas. |
| 🔵 REFACTOR | `refactor: US-020-E1 extraer formateador de montos` | Crear utilidad `MoneyFormatter` para formateo consistente de montos y deltas. |

---

#### Escenario 2/2: Sin diferencias detectadas

```gherkin
Escenario: La recalculación no detecta diferencias
  Dado que el usuario solicita la recalculación del reporte de "2025-04"
  Y los valores no han cambiado
  Entonces el sistema muestra: "El reporte de 2025-04 ya está actualizado. No se detectaron diferencias."
  Y el reporte permanece sin cambios
```

**Ciclo TDD:**

| Fase | Commit | Qué hacer |
|---|---|---|
| 🔴 RED | `test: US-020-E2 notificación sin diferencias` | Test que compare valores idénticos y verifique mensaje de "sin diferencias". |
| 🟢 GREEN | `feat: US-020-E2 manejar resultado sin cambios` | Agregar branch en `DifferenceDetector`: si no hay cambios, retornar mensaje informativo. |
| 🔵 REFACTOR | `refactor: US-020-E2 integrar detector con servicio de recalculación` | Encadenar `recalculate()` → `compare()` → `notify()` en un flujo limpio. |

---

#### Checklist complementario US-020

- [ ] La notificación muestra solo los campos que realmente cambiaron (no campos sin cambio)
- [ ] Los deltas tienen signo correcto: `+` para incremento, `-` para decremento
- [ ] Los montos se formatean consistentemente (e.g., `$1,200` no `$1200` ni `$1,200.00`)
- [ ] La notificación desaparece o se puede cerrar manualmente
- [ ] Si la recalculación falló (US-019 error), no se muestra notificación de diferencia
- [ ] Diferencias mínimas ($0.01) son detectadas y reportadas

#### Resumen de commits US-020

| # | Mensaje de Commit | Fase |
|---|---|---|
| 1 | `test: US-020-E1 notificación con diferencias detectadas` | 🔴 |
| 2 | `feat: US-020-E1 comparar y notificar diferencias` | 🟢 |
| 3 | `refactor: US-020-E1 extraer formateador de montos` | 🔵 |
| 4 | `test: US-020-E2 notificación sin diferencias` | 🔴 |
| 5 | `feat: US-020-E2 manejar resultado sin cambios` | 🟢 |
| 6 | `refactor: US-020-E2 integrar detector con servicio de recalculación` | 🔵 |

---

### US-021 — Descargar Reporte de un Período como PDF

**Antecedentes comunes:** El usuario está autenticado y se encuentra en la página de Reportes.

---

#### Escenario 1/5: Descarga exitosa del PDF

```gherkin
Escenario: Descarga exitosa del PDF de un reporte de período
  Dado que existe un reporte para el período "2025-10"
  Cuando el usuario selecciona "Descargar PDF" para el reporte de "2025-10"
  Entonces el sistema genera un PDF con los datos del reporte
  Y el nombre del archivo descargado es: "reporte-2025-10.pdf"
  Y el archivo se descarga automáticamente
```

**Ciclo TDD:**

| Fase | Commit | Qué hacer |
|---|---|---|
| 🔴 RED | `test: US-021-E1 descarga exitosa de PDF` | Test que invoque `generatePdf("2025-10")` y verifique: tipo MIME `application/pdf`, nombre de archivo correcto, contenido no vacío. |
| 🟢 GREEN | `feat: US-021-E1 implementar generación de PDF` | Crear `PdfGeneratorService.generate(report)` con librería PDF. Copilot para boilerplate de template. |
| 🔵 REFACTOR | `refactor: US-021-E1 extraer template PDF reutilizable` | Separar lógica de datos y plantilla visual del PDF. |

---

#### Escenario 2/5: Estado de carga durante generación

```gherkin
Escenario: Estado de carga durante la generación del PDF
  Dado que el usuario solicita la descarga del PDF
  Cuando el sistema está generando el documento
  Entonces el botón "Descargar PDF" se deshabilita y muestra "Generando PDF..."
  Y al completarse, la descarga se inicia automáticamente
```

**Ciclo TDD:**

| Fase | Commit | Qué hacer |
|---|---|---|
| 🔴 RED | `test: US-021-E2 estado de carga durante generación PDF` | Test UI que verifique botón deshabilitado durante generación y restauración al completar. |
| 🟢 GREEN | `feat: US-021-E2 indicador de carga en descarga PDF` | Reutilizar patrón `useAsyncAction` de US-019-E2. |
| 🔵 REFACTOR | `refactor: US-021-E2 unificar estados de carga` | Asegurar consistencia de UX entre recalculación y descarga. |

---

#### Escenario 3/5: Reporte inexistente

```gherkin
Escenario: El reporte no existe para generar el PDF
  Dado que el usuario intenta descargar el PDF de un reporte eliminado
  Entonces se muestra: "No fue posible generar el PDF. El reporte seleccionado no existe."
  Y no se descarga ningún archivo
```

**Ciclo TDD:**

| Fase | Commit | Qué hacer |
|---|---|---|
| 🔴 RED | `test: US-021-E3 PDF de reporte inexistente` | Test que intente generar PDF para reporte inexistente y verifique error `NOT_FOUND`. |
| 🟢 GREEN | `feat: US-021-E3 validar existencia antes de generar PDF` | Reutilizar validación de existencia de reportes. |
| 🔵 REFACTOR | `refactor: US-021-E3 unificar validación pre-operación` | Integrar con las validaciones comunes creadas en US-019-E5. |

---

#### Escenario 4/5: Error interno durante generación

```gherkin
Escenario: La generación del PDF falla por error del sistema
  Dado que el usuario solicita la descarga del PDF
  Y ocurre un error interno durante la generación
  Entonces se muestra: "No fue posible generar el PDF. Inténtalo de nuevo más tarde."
  Y no se descarga ningún archivo
```

**Ciclo TDD:**

| Fase | Commit | Qué hacer |
|---|---|---|
| 🔴 RED | `test: US-021-E4 error interno en generación PDF` | Test que simule fallo del generador de PDF (mock) y verifique error 500. |
| 🟢 GREEN | `feat: US-021-E4 manejo de errores en generación PDF` | Agregar `try-catch` en el servicio de PDF, retornar error apropiado. |
| 🔵 REFACTOR | `refactor: US-021-E4 integrar con handler global de errores` | Reutilizar `@ControllerAdvice` para manejar `PdfGenerationException`. |

---

#### Escenario 5/5: Usuario no autenticado

```gherkin
Escenario: Descarga bloqueada para usuario no autenticado
  Dado que el usuario no está autenticado
  Cuando intenta acceder a la funcionalidad de descarga de PDF
  Entonces el sistema lo redirige a la página de inicio de sesión
```

**Ciclo TDD:**

| Fase | Commit | Qué hacer |
|---|---|---|
| 🔴 RED | `test: US-021-E5 descarga bloqueada sin autenticación` | Test que invoque endpoint de PDF sin token y verifique redirección 401/302. |
| 🟢 GREEN | `feat: US-021-E5 proteger endpoint de PDF` | Asegurar que la configuración de seguridad protege la ruta de descarga. |
| 🔵 REFACTOR | `refactor: US-021-E5 centralizar reglas de seguridad` | Verificar que todas las rutas de reportes están protegidas uniformemente. |

---

#### Checklist complementario US-021

- [ ] El PDF contiene: nombre del usuario, período, totalIncome, totalExpense, balance, fecha/hora de generación
- [ ] El nombre del archivo sigue el formato `reporte-yyyy-MM.pdf`
- [ ] El PDF es legible y bien formateado (sin texto cortado ni tablas rotas)
- [ ] El PDF refleja los datos al momento de la descarga (no datos cacheados)
- [ ] El tamaño del archivo es razonable (< 1 MB para un reporte individual)
- [ ] El PDF se puede abrir con lectores estándar (Adobe Reader, navegadores)

#### Resumen de commits US-021

| # | Mensaje de Commit | Fase |
|---|---|---|
| 1 | `test: US-021-E1 descarga exitosa de PDF` | 🔴 |
| 2 | `feat: US-021-E1 implementar generación de PDF` | 🟢 |
| 3 | `refactor: US-021-E1 extraer template PDF reutilizable` | 🔵 |
| 4 | `test: US-021-E2 estado de carga durante generación PDF` | 🔴 |
| 5 | `feat: US-021-E2 indicador de carga en descarga PDF` | 🟢 |
| 6 | `refactor: US-021-E2 unificar estados de carga` | 🔵 |
| 7 | `test: US-021-E3 PDF de reporte inexistente` | 🔴 |
| 8 | `feat: US-021-E3 validar existencia antes de generar PDF` | 🟢 |
| 9 | `refactor: US-021-E3 unificar validación pre-operación` | 🔵 |
| 10 | `test: US-021-E4 error interno en generación PDF` | 🔴 |
| 11 | `feat: US-021-E4 manejo de errores en generación PDF` | 🟢 |
| 12 | `refactor: US-021-E4 integrar con handler global de errores` | 🔵 |
| 13 | `test: US-021-E5 descarga bloqueada sin autenticación` | 🔴 |
| 14 | `feat: US-021-E5 proteger endpoint de PDF` | 🟢 |
| 15 | `refactor: US-021-E5 centralizar reglas de seguridad` | 🔵 |

---

### US-022 — Descargar Resumen de Reportes por Rango como PDF

**Antecedentes comunes:** El usuario está autenticado y se encuentra en la página de Reportes.

---

#### Escenario 1/4: Descarga exitosa del resumen de rango

```gherkin
Escenario: Descarga exitosa del resumen de un rango de períodos
  Dado que el usuario ha aplicado filtros de período "2025-01" a "2025-06"
  Y existen reportes para todos los meses del rango
  Cuando el usuario selecciona "Descargar Resumen PDF"
  Entonces el sistema genera un PDF consolidado con detalle por período y totales acumulados
  Y el nombre del archivo es: "resumen-reporte-2025-01_2025-06.pdf"
```

**Ciclo TDD:**

| Fase | Commit | Qué hacer |
|---|---|---|
| 🔴 RED | `test: US-022-E1 descarga exitosa resumen por rango` | Test que invoque `generateSummaryPdf("2025-01", "2025-06")` y verifique: contenido con 6 períodos, totales acumulados correctos, nombre de archivo. |
| 🟢 GREEN | `feat: US-022-E1 implementar generación PDF de resumen` | Extender `PdfGeneratorService` con método `generateSummary(reports)`. Reutilizar template de US-021. Copilot para tabla multi-período. |
| 🔵 REFACTOR | `refactor: US-022-E1 unificar generador PDF individual y rango` | Aplicar patrón Strategy o Template Method para compartir lógica entre PDF individual y de rango. |

---

#### Escenario 2/4: Rango sin reportes

```gherkin
Escenario: El rango seleccionado no contiene reportes
  Dado que el usuario define un rango sin reportes
  Cuando solicita la descarga del resumen en PDF
  Entonces el sistema muestra: "No existen reportes en el rango seleccionado."
  Y no se descarga ningún archivo
```

**Ciclo TDD:**

| Fase | Commit | Qué hacer |
|---|---|---|
| 🔴 RED | `test: US-022-E2 resumen PDF rango vacío` | Test con rango sin reportes, verificar que no se genera PDF y se retorna mensaje informativo. |
| 🟢 GREEN | `feat: US-022-E2 validar rango antes de generar resumen` | Agregar verificación de reportes existentes en el rango antes de invocar el generador. |
| 🔵 REFACTOR | `refactor: US-022-E2 reutilizar validación de rango de US-018` | Compartir lógica de validación de rango con la eliminación masiva. |

---

#### Escenario 3/4: Rango con períodos parciales

```gherkin
Escenario: El rango incluye períodos con y sin reportes
  Dado que el usuario selecciona el rango "2025-01" a "2025-06"
  Y solo existen reportes para "2025-01", "2025-03" y "2025-05"
  Cuando solicita la descarga del resumen
  Entonces el PDF incluye solo los períodos con datos
  Y muestra nota al pie: "No se encontraron reportes para los períodos: 2025-02, 2025-04, 2025-06."
```

**Ciclo TDD:**

| Fase | Commit | Qué hacer |
|---|---|---|
| 🔴 RED | `test: US-022-E3 resumen PDF con períodos parciales` | Test con 3 de 6 períodos existentes, verificar que el PDF solo incluye los 3 y la nota al pie lista los 3 faltantes. |
| 🟢 GREEN | `feat: US-022-E3 generar PDF parcial con nota al pie` | Agregar lógica para identificar períodos faltantes y generar nota al pie en el template PDF. |
| 🔵 REFACTOR | `refactor: US-022-E3 extraer detector de períodos faltantes` | Crear utilidad `MissingPeriodDetector` reutilizable. |

---

#### Escenario 4/4: Error durante generación del resumen

```gherkin
Escenario: La generación del PDF del resumen falla
  Dado que el usuario solicita la descarga del resumen en PDF
  Y ocurre un error durante la generación
  Entonces el sistema muestra: "No fue posible generar el PDF del resumen. Inténtalo de nuevo más tarde."
  Y no se descarga ningún archivo
```

**Ciclo TDD:**

| Fase | Commit | Qué hacer |
|---|---|---|
| 🔴 RED | `test: US-022-E4 error en generación resumen PDF` | Test que simule fallo del generador y verifique error 500 sin descarga. |
| 🟢 GREEN | `feat: US-022-E4 manejo de errores en resumen PDF` | Reutilizar `try-catch` y `PdfGenerationException` de US-021-E4. |
| 🔵 REFACTOR | `refactor: US-022-E4 unificar manejo de errores PDF` | Asegurar que individual y rango comparten mismo handler de errores. |

---

#### Checklist complementario US-022

- [ ] El PDF contiene: nombre del usuario, rango de períodos, tabla por período, totales acumulados, fecha/hora de generación
- [ ] El nombre del archivo sigue el formato `resumen-reporte-yyyy-MM_yyyy-MM.pdf`
- [ ] La nota al pie de períodos faltantes lista correctamente los meses sin datos
- [ ] Los totales acumulados son la suma aritmética de los períodos incluidos
- [ ] El PDF es legible con múltiples períodos (diseño no se rompe con 12+ filas)
- [ ] El contenido refleja datos actualizados al momento de la descarga
- [ ] Validación de que inicio ≤ fin en el rango de períodos

#### Resumen de commits US-022

| # | Mensaje de Commit | Fase |
|---|---|---|
| 1 | `test: US-022-E1 descarga exitosa resumen por rango` | 🔴 |
| 2 | `feat: US-022-E1 implementar generación PDF de resumen` | 🟢 |
| 3 | `refactor: US-022-E1 unificar generador PDF individual y rango` | 🔵 |
| 4 | `test: US-022-E2 resumen PDF rango vacío` | 🔴 |
| 5 | `feat: US-022-E2 validar rango antes de generar resumen` | 🟢 |
| 6 | `refactor: US-022-E2 reutilizar validación de rango de US-018` | 🔵 |
| 7 | `test: US-022-E3 resumen PDF con períodos parciales` | 🔴 |
| 8 | `feat: US-022-E3 generar PDF parcial con nota al pie` | 🟢 |
| 9 | `refactor: US-022-E3 extraer detector de períodos faltantes` | 🔵 |
| 10 | `test: US-022-E4 error en generación resumen PDF` | 🔴 |
| 11 | `feat: US-022-E4 manejo de errores en resumen PDF` | 🟢 |
| 12 | `refactor: US-022-E4 unificar manejo de errores PDF` | 🔵 |

---

## Resumen Global de Commits TDD

| Historia | Escenarios | Commits (RED+GREEN+REFACTOR) |
|---|---|---|
| US-017 | 5 | 15 |
| US-018 | 4 | 12 |
| US-019 | 5 | 15 |
| US-020 | 2 | 6 |
| US-021 | 5 | 15 |
| US-022 | 4 | 12 |
| **Total** | **25** | **75** |

> **Convenciones de commit utilizadas:**
> - `test:` → Fase RED (test que falla)
> - `feat:` → Fase GREEN (código mínimo para pasar el test)
> - `refactor:` → Fase REFACTOR (mejorar sin romper tests)
>
> **Cada grupo de 3 commits** (RED → GREEN → REFACTOR) corresponde a un solo escenario Gherkin equivalente a un ciclo TDD completo.

---

*Documento generado el 2026-02-19 como parte de la fase TDD pre-implementación. Basado en las historias de usuario definidas en `new-stories.md` y los estándares ISTQB Foundation Level v4.0.*
