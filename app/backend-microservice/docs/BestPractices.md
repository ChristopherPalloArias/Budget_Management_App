# Reporte de Buenas Prácticas - Backend (Transaction & Report)

Este documento detalla las prácticas de ingeniería de software implementadas en los microservicios `transaction` y `report`.

## 1. Arquitectura y Estructura

El sistema sigue un patrón de **Microservicios en capas**, orquestados por una **Arquitectura Orientada a Eventos**.

### Separación de Responsabilidades (Layered Architecture)
Ambos microservicios mantienen una estricta separación de capas, facilitando la mantenibilidad y el testing.

-   **Controller Layer**: Maneja exclusivamente peticiones HTTP y validación de entrada.
-   **Service Layer**: Contiene la lógica de negocio pura. No conoce detalles de HTTP ni de la infraestructura de mensajería.
-   **Repository Layer**: Abstrae el acceso a datos (JPA).
-   **Infrastructure Layer**: Aísla la comunicación con RabbitMQ (Producers/Consumers).

### Desacoplamiento por Eventos (Event-Driven)
La comunicación entre microservicios es asíncrona, lo que mejora la resiliencia y escalabilidad. `transaction` no llama directamente a `report`.

-   **Internal Domain Events (Transaction)**:
    El servicio de transacciones no publica directamente a RabbitMQ. En su lugar, publica un evento interno de Spring (`TransactionCreatedEvent`), desacoplando el dominio de la infraestructura de mensajería.
    -   *Publisher*: `com.microservice.transaction.service.impl.TransactionServiceImpl`
    -   *Listener*: `com.microservice.transaction.infrastructure.listener.TransactionEventListener` (Maneja el evento de forma `@Async`).

## 2. Patrones de Diseño Implementados

### Repository Pattern
Uso de interfaces que extienden `JpaRepository` para abstraer consultas de datos.
*   **Transaction**: `com.microservice.transaction.repository.TransactionRepository`
*   **Report**: `com.microservice.report.repository.ReportRepository`

### DTO (Data Transfer Object) & Records
Uso de **Java Records** para crear objetos de transferencia de datos inmutables, concisos y seguros. Se evita exponer las Entidades JPA directamente en la API.
*   **Transaction**: `com.microservice.transaction.dto.TransactionRequest`
*   **Report**: `com.microservice.report.dto.ReportResponse`

### Observer / Pub-Sub (Internal & External)
1.  **Interno (Spring Events)**: `TransactionServiceImpl` publica un evento, `TransactionEventListener` lo escucha. Esto permite que el servicio "guardar transacción" no se bloquee esperando a que se envíe el mensaje a RabbitMQ (gracias a `@Async`).
2.  **Externo (RabbitMQ)**: `TransactionMessageProducer` envía el mensaje, `ReportConsumer` lo recibe.

### Dependency Injection (Inversion of Control)
Uso extensivo de inyección por constructor mediante `@RequiredArgsConstructor` de Lombok, promoviendo componentes inmutables y testables.

### Mapper Pattern
Uso de clases utilitarias (`Mapper`) con métodos estáticos para convertir entre Entidades y DTOs, manteniendo los servicios limpios de lógica de transformación.
*   **Transaction**: `com.microservice.transaction.dto.TransactionMapper`
*   **Report**: `com.microservice.report.mapper.ReportMapper`

## 3. Principios de Desarrollo (SOLID/DRY)

-   **Single Responsibility Principle (SRP)**:
    -   `GlobalExceptionHandler` maneja errores globalmente.
    -   `TransactionEventListener` solo se encarga de reaccionar a eventos creados.
    -   `TransactionService` solo maneja lógica de negocio de transacciones.
-   **Open/Closed Principle (OCP)**:
    -   El uso de eventos (`TransactionCreatedEvent`) permite agregar nuevas reacciones (ej. enviar notificación de correo) creando un nuevo Listener sin modificar el código del servicio existente.

## 4. Referencias de Implementación

### Transaction Microservice

**Controller (Validación `@Valid`)**
`src/main/java/com/microservice/transaction/controller/TransactionController.java`
```java
@PostMapping
public ResponseEntity<TransactionResponse> create(@Valid @RequestBody TransactionRequest dto) { ... }
```

**Service (Domain Event Publishing)**
`src/main/java/com/microservice/transaction/service/impl/TransactionServiceImpl.java`
```java
eventPublisher.publishEvent(new TransactionCreatedEvent(this, saved));
```

**Infrastructure (Async Event Listener)**
`src/main/java/com/microservice/transaction/infrastructure/listener/TransactionEventListener.java`
```java
@Async
@EventListener
public void handleTransactionCreatedEvent(TransactionCreatedEvent event) {
    transactionMessageProducer.sendCreated(event.getTransaction());
}
```

### Report Microservice

**Consumer (RabbitMQ Listener)**
`src/main/java/com/microservice/report/infrastructure/ReportConsumer.java`
```java
@RabbitListener(queues = RabbitMQConfiguration.TRANSACTION_CREATED_QUEUE)
public void consumeCreated(TransactionMessage transactionMessage) { ... }
```

**Repository (Custom Query Methods)**
`src/main/java/com/microservice/report/repository/ReportRepository.java`
```java
List<Report> findByUserIdAndPeriodBetweenOrderByPeriodAsc(...);
```

## 5. Calidad de Código y Estilo

-   **Manejo de Excepciones Global**: Implementado con `@RestControllerAdvice` en ambos servicios.
-   **Tipado Fuerte**: Uso correcto de `BigDecimal` para dinero y `OffsetDateTime`/`LocalDate` para fechas.
-   **Lombok**: Reducción drástica de código boilerplate.
-   **Convenciones de Nombres**: Uso consistente de sufijos (`Controller`, `Service`, `impl`, `Repository`, `Dto`).
