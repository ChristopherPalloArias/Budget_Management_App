# Plan de Refactorización: Arquitectura Frontend

## 1. Alcance (Scope)
Este plan se centra en resolver la deuda técnica y las violaciones a los principios SOLID y de diseño de componentes identificados en la Auditoría de Arquitectura del Frontend (`AUDITORIA_FRONTEND.md`).

### **Qué se va a tocar (In Scope):**
- **Corrección de Bug Visual (Gastos Totales):** Solucionar la inconsistencia en la tabla del historial de reportes donde la columna "Gastos Totales" aparece en $0.00 en la UI a pesar de mostrarse correctamente en el PDF. Esto requiere revisar las interfaces (`types`) y el mapeo/adapter de la respuesta del backend para asegurar que la propiedad correcta (ej. `totalExpenses`) se está inyectando en la columna.
- **Estado Global (`useTransactionStore.ts`):** Eliminar la fuga de estado local hacia el store global. El estado de edición (`currentTransaction`) se migrará a un estado local de React o a un Context Provider dedicado.
- **Cliente HTTP (`HttpClient.ts`):** Resolver la violación de Inversión de Dependencias (DIP). Se eliminará el acoplamiento directo con `localStorage` inyectando un `TokenProvider` o `StorageAdapter` en la configuración de Axios.
- **Hooks Sobrecargados (`useTransactionPage.ts`):** Descomponer este "God Hook". La gestión del estado de los modales (Crear, Editar, Eliminar) se delegará a componentes individuales o se manejará a través de parámetros de URL (ej. `?modal=edit&id=1`).
- **Componentes de Página (`ReportsPage.tsx` y `TransactionPage.tsx`):**
  - Aplicar el Principio de Responsabilidad Única (SRP) extrayendo el marcado JSX de carga (Skeletons) a componentes dedicados como `<ReportsPageSkeleton />`.
  - Reducir el *prop drilling* en `TransactionPage.tsx` al pasar manejadores de modales al `<DataTable />`, utilizando composición de componentes o contexto.

### **Qué NO se va a tocar (Out of Scope):**
- Reescritura de la estructura modular base (`auth`, `transactions`, `reports`, `core`, `shared`).
- Cambio o reemplazo de las librerías principales de estado (Zustand y TanStack React Query se mantienen).
- Modificación del manejo de formularios (`react-hook-form` + `zodResolver`).
- Migración general de UI/estilos preexistentes que no estén directamente relacionados con los problemas de arquitectura detectados.

---

## 2. Objetivos Concretos
- **Reducir el Acoplamiento:** Desvincular la lógica de negocio HTTP de APIs exclusivas del navegador (`localStorage`), habilitando simulaciones en pruebas y compatibilidad futura con SSR.
- **Mejorar la Cohesión (SRP):** Cada componente y hook debe tener una única responsabilidad. La página no debe preocuparse por cómo se dibuja el skeleton ni por los estados internos de cada posible modal.
- **Optimizar el Rendimiento:** Eliminar los cuellos de botella de re-renderizado a nivel de página (root-level) previniendo que la apertura de un modal re-renderice tablas pesadas como `<DataTable />`.
- **Prevenir Bugs de Estado ("Phantom Data"):** Eliminar el riesgo de fugas de memoria o datos persistentes al mantener estados de UI efímeros (como formularios en modales) fuera del store global.
- **Aumentar la Testabilidad:** Hacer posible aislar el cliente HTTP para pruebas unitarias limpias en Jest, e independizar el testeo de componentes de interfaz.

---

## 3. Riesgos Identificados
- **Ruptura de Flujos de Edición:** Al mover `currentTransaction` del store global al estado local, podríamos perder la referencia de los datos entre la selección en la tabla y la renderización en el modal si la composición no es correcta.
- **Fallos de Autenticación de Red:** Refactorizar cómo `HttpClient.ts` obtiene el token de acceso podría desincronizar peticiones aseguradas si el `StorageAdapter` no se inyecta o no se lee a tiempo antes del interceptor.
- **Problemas de Renderizado Cíclico:** Cambiar el control de modales a parámetros de URL o a contextos locales puede causar re-renders infinitos si los efectos (useEffect) que escuchan estos cambios no están correctamente balanceados.

---

## 4. Plan de Rollback (Mitigación)
1. **Versionado de Código Seguro:** Todo el código deberá trabajarse en una rama feature aislada (ej. `feature/frontend-refactor`). No se realizarán commits directos sobre las ramas principales de desarrollo.
2. **Reversión Rápida de Commits:** En caso de detectar fallos bloqueantes en integraciones o pipelines, se ejecutará inmediatamente un `git revert` del merge problemático para restaurar la estabilidad.
3. **Mantenimiento Temporal de Interfaces (Si aplica):** Para refactorizaciones profundas (ej. God Hooks), se podrá conservar temporalmente la implementación original bajo un nombre con sufijo `_deprecated` hasta que el nuevo flujo haya sido validado al 100% en integración continua, antes de eliminar el archivo viejo.

---

## 5. Regla de Oro / Prerrequisito Técnico (TDD)
> 🚨 **IMPORTANTE: TESTS ANTES QUE EL CÓDIGO** 🚨

De forma análoga al backend, ninguna línea de código funcional en el Alcance será modificada sin **antes** garantizar su testabilidad. Como no podemos confirmar refactorizaciones seguras en el frontend sin red de seguridad, el flujo será:

1. **Escribir / Asegurar Tests de Comportamiento:** Si el componente actual carece de tests, escribir pruebas sobre su comportamiento presente (ej. montaje del modal, envío de red) en React Testing Library o Cypress.
2. **Escribir Nuevos Tests (Red):** Definir el nuevo escenario esperado (ej. el test de `HttpClient` comprobando la obtención de token sin llamar directamente a la API Web).
3. **Refactorizar (Green):** Modificar la implementación dividiendo los Gook Hooks o inyectando los adaptadores correspondientes hasta que las pruebas pasen en verde.
4. **Limpiar (Refactor):** Eliminar código obsoleto local, estandarizar convenciones y confirmar que el performance de renderizado mejoró (Profiler de React).
