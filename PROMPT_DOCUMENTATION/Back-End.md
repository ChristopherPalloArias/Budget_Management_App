📌 Prompts – Microservicios con Spring Boot + RabbitMQ
### Prompts

* En la enmtidad transaccion no quiero que le pongas cosas de relaciones entre tablas, porque no va a ver relaciones, se va a trabajar medaiaante eventos con rabbit, en ese caso como seria la plantilla de la entidad con esos campos, lo unico es que se va a usar jpa para almacenar en DB pero no va a ver relaciones, pon solo lo necesario en esa class, ten en ceunta que voy a usar lombok para no hacer esos getter y setter pero ten cuiadado con el @Data, a partir del modulo transaction podrias crear una entidad con estos campos  
id - UID - type: INCOME | EXPENSE - ENUN - amount - category - date - description - createdAt

* Ahora en esta estructura de carpeta ques es otro microservice, sigue el mismo patron, de crear una entidad para DB pero ten en cuenta que uso lombok y que userId va a ser proporcionado por un evento de rabbit que lo registro en base a la transaction, ahora lo que quiero es que coloquie bien los tipos y dentro de una carpeta model, tambien el id de report es autogenerado por DB ten en ceuenta eso, ten en cuenta que no hay relaciones, report no obtiene el id de transaction si no que solo el userId desde el evento de rabbit, estos son los campos  
reportId - userId - period (YYYY-MM) - totalIncome (decimal) - totalExpense (decimal) - balance (decimal) - createdAt - updatedAt

* Crea la estructura de carpetas para un proyecto backend en Java (Spring Boot) siguiendo arquitectura por capas.  
Debe incluir las siguientes carpetas principales dentro de src/main/java/com/microservice/report/:  
controller → para los controladores REST  
service → para la lógica de negocio  
repository → para el acceso a datos  
infrastructure → para componentes externos como RabbitMQ, seguridad, mensajería,  
etc.  
ya que la carpeta model ya existe solo crea las demas que te pido  
Cada carpeta debe tener al menos una clase de ejemplo vacía con su respectivo nombre (ejemplo: TransactionController, TransactionService, TransactionRepository, RabbitMQConfig), con comentarios indicando su responsabilidad.  
No incluir lógica real, solo la estructura y comentarios.

* Crea la configuración de base de datos para un proyecto Java Spring Boot usando MySQL con enfoque code-first (JPA/Hibernate). Requisitos:  
- En el archivo `application.properties` (o `application.yml`) incluir:  
- `spring.datasource.url=jdbc:mysql://localhost:3306/midatabase`  
- `spring.datasource.username=root`  
- `spring.datasource.password=tu_password`  
- `spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver`  
- `spring.jpa.hibernate.ddl-auto=update` (para code-first)  
- `spring.jpa.show-sql=true`  
- `spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect`

**

Genera el código para el microservicio Report en un proyecto Java Spring Boot que se comunica de forma asíncrona con RabbitMQ.  
Ya existen las capas y paquetes vacíos: `controller`, `service`, `repository`, `infrastructure`.

Requisitos:

1. Entidad `Report`:  
   - Debe tener los campos:  
     ```java
     @Entity
     public class Report {
         @Id
         @GeneratedValue(strategy = GenerationType.IDENTITY)
         private Long id;

         @Column(name = "total_income", nullable = false, precision = 19, scale = 2)
         private BigDecimal totalIncome;

         @Column(name = "total_expense", nullable = false, precision = 19, scale = 2)
         private BigDecimal totalExpense;

         @Column(name = "balance", nullable = false, precision = 19, scale = 2)
         private BigDecimal balance;
     }
     ```  
   - Inicializar los valores en cero.

2. En el paquete `infrastructure`:  
   - Crear `RabbitMQConfig` con configuración de cola `report-queue`, exchange `transaction-exchange` y binding con routing key `transaction.*`.

3. En el paquete `infrastructure`:  
   - Crear un listener `TransactionConsumer` anotado con `@RabbitListener(queues = "report-queue")`.  
   - Este listener recibe objetos `Transaction` publicados por el microservicio Transaction.  
   - Según el `type` de la transacción (`INCOME` o `EXPENSE`), actualizar los campos de `Report`:  
     - Si es `INCOME`: sumar al `totalIncome`.  
     - Si es `EXPENSE`: sumar al `totalExpense`.  
     - Calcular `balance = totalIncome - totalExpense`.  
   - Guardar el `Report` actualizado en la base de datos.

4. En el paquete `repository`:  
   - Crear `ReportRepository` que extienda `JpaRepository<Report, Long>`.

5. En el paquete `service`:  
   - Crear `ReportService` con métodos:  
     - `updateReport(Transaction transaction)` → lógica de actualización de totales.  
     - `getReport()` → devuelve el reporte actual.

6. En el paquete `controller`:  
   - Crear `ReportController` con endpoints REST:  
     - `GET /api/v1/reports` → devuelve el reporte actual con `totalIncome`, `totalExpense` y `balance`.

Objetivo: que el microservicio Report consuma eventos de Transaction vía RabbitMQ, calcule los totales de ingresos, gastos y balance, y exponga un endpoint REST para consultar el reporte.


### Prompt Docker

Crea un Dockerfile para el microservicio "transaction" en Spring Boot 4.0.2 con Java 17.  
Debe usar Maven como herramienta de build y generar un JAR ejecutable.  

Utiliza un Dockerfile multistage:  
- En la primera etapa compila el proyecto con `eclipse-temurin:17-jdk-alpine`.  
- En la segunda etapa copia el JAR al contenedor final usando `eclipse-temurin:17-jre-alpine`.  

El contenedor debe exponer el puerto `8081` y aceptar las variables de entorno `DB_USERNAME` y `DB_PASSWORD` para la conexión a MySQL.  

Optimiza las capas para que el build sea reproducible y eficiente.

### Prompt

Crea un archivo llamado docker-compose.yml dentro de una carpeta llamada docker-compose en la raíz del proyecto.  
El archivo debe levantar los siguientes servicios:  
- mysql (con volumen persistente, usuario y contraseña tomados de variables de entorno DB_USERNAME y DB_PASSWORD, base de datos transactions_db)  
- rabbitmq (con plugin de management, expone puertos 5672 y 15672)  
- transaction (se construye desde ./transaction, expone puerto 8081, depende de mysql y rabbitmq, usa las variables de entorno DB_USERNAME y DB_PASSWORD, y configura SPRING_DATASOURCE_URL apuntando a mysql y SPRING_RABBITMQ_HOST apuntando a rabbitmq)  
- report (se construye desde ./report, expone puerto 8082, depende de mysql y rabbitmq, usa las mismas variables de entorno y configuración que transaction)
