### Arquitectura Detectada
- **Microservicios**: 2 servicios principales (transaction y report)
- **Tecnologías**: Java 17, Spring Boot 4.0.2, Maven, MySQL 8.0, RabbitMQ 4.0
- **Comunicación**: Asíncrona vía RabbitMQ
- **Bases de datos**: MySQL separadas por servicio
- **Contenerización**: Docker Compose para orquestación

### Estructura de Directorios
```
backend-microservice/
├── transaction/          # Microservicio de transacciones (puerto 8081)
├── report/              # Microservicio de reportes (puerto 8082)
├── docker-compose/
│   └── production/
│       └── docker-compose.yaml
└── README.md
```

###  Introducción
- Propósito del sistema de microservicios
- Visión general de funcionalidades
- Stack tecnológico principal

### Arquitectura
#### Macro (Microservicios)
- Descripción de Transaction Service
- Descripción de Report Service
- Comunicación vía RabbitMQ
- Independencia de datos

#### Micro (Por Capas)
- Explicación de layered architecture
- Estructura de paquetes detectada:
    - `controller/` (Presentación)
    - `service/` (Aplicación)
    - `model/` (Dominio)
    - `repository/` (Infraestructura persistencia)
    - `infrastructure/` (Configuración y messaging)
    - `exception/` (Manejo de errores)

### Requisitos Previos
- Java 17+
- Maven 3.8+
- Docker 20.10+
- Docker Compose 2.0+
- Variables de entorno requeridas (sin .env example según user preference)

### Comandos de Ejecución (Completos con ejemplos según user preference)

#### Desarrollo Local con Maven
```
# Transaction service
cd transaction && mvn spring-boot:run

# Report service  
cd report && mvn spring-boot:run

# Compilar ambos servicios
mvn clean compile -pl transaction,report

```

#### Docker Compose
```
cd docker-compose/production
docker compose up -d

# Verificar estado
docker compose -f docker-compose.yaml ps

# Logs específicos
docker compose -f docker-compose.yaml logs -f transaction
```

### Configuración RabbitMQ
- Interfaz web: http://localhost:15672
- Colas configuradas automáticamente
- Producer/Consumer patterns detectados

## Detalles Técnicos Clave Detectados

### Configuración de Puertos
- Transaction: 8081
- Report: 8082
- MySQL Transactions: 3307 (externo) / 3306 (interno)
- MySQL Reports: 3308 (externo) / 3306 (interno)
- RabbitMQ: 5672 (AMQP), 15672 (Management)

### Variables de Entorno
- `MYSQL_ROOT_PASSWORD`
- `DB_USERNAME`, `DB_PASSWORD`
- `RABBITMQ_DEFAULT_USER`, `RABBITMQ_DEFAULT_PASS`

### Dependencias Maven Clave
- Spring Boot Starter AMQP
- Spring Boot Starter Data JPA
- Spring Boot Starter Web MVC
- MySQL Connector
- Lombok
- Spring Boot Test (varios módulos)

### Configuraciones
- JPA DDL Auto: update
- Show SQL: true
- RabbitMQ acknowledgment: auto
- Retry attempts: 3 (report service)

## Estrategia de Implementación

1. **Crear archivo README.md** en la raíz del backend-microservice
2. **Estructurar contenido** con markdown técnico y claro
3. **Incluir comandos completos** según requerimiento del usuario
4. **Mencionar variables de entorno** en texto (sin archivo .env example)
5. **Enfocar en desarrolladores** que se integran al proyecto
6. **Mantener estilo técnico** y conciso

## Validaciones Post-Creación

- [ ] Verificar que todos los comandos sean correctos
- [ ] Asegurar que puertos y configuraciones coincidan
- [ ] Confirmar que estructura de directorios sea precisa
- [ ] Validar que instrucciones de RabbitMQ sean claras
- [ ] Revisar que sección de troubleshooting cubra casos comunes