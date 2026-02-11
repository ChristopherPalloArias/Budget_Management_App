# Architecture Audit - Identified Anti-Patterns

1. **Controller-Orchestrated Integration**
   - **Location:** `app/backend-microservice/transaction/src/main/java/com/microservice/transaction/controller/TransactionController.java` (`create` method).
   - **Symptoms:** The controller handles HTTP concerns and directly publishes RabbitMQ events through `TransactionMessageProducer`, coupling web API logic to messaging infrastructure.

2. **Conditional Update Logic in Report Aggregation**
   - **Location:** `app/backend-microservice/report/src/main/java/com/microservice/report/service/impl/ReportServiceImpl.java` (`updateReport` method).
   - **Symptoms:** `if/else` branching on `TransactionType` mutates totals inline, making new transaction types require changes in core service logic and violating the Open/Closed Principle.

3. **Scattered Object Construction Rules**
   - **Location:** `ReportServiceImpl.getOrCreateReport` and `TransactionMessageProducer.toMessage`.
   - **Symptoms:** Report defaults and message-mapping rules are embedded in services, forcing multiple components to own creation details and making consistency checks difficult.
