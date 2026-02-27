# 🚀 Guía de Despliegue — Budget Management App

> **Archivo de referencia:** `docker-compose.deploy.yml`
> **Última actualización:** Febrero 2026

---

## 📋 Tabla de Contenidos

1. [Propósito del Archivo](#1--propósito-del-archivo)
2. [Requisitos Previos](#2--requisitos-previos)
3. [Configuración del Entorno](#3--configuración-del-entorno)
4. [Arquitectura y Orquestación](#4--arquitectura-y-orquestación)
5. [Persistencia de Datos](#5--persistencia-de-datos)
6. [Guía Rápida de Comandos (Cheatsheet)](#6--guía-rápida-de-comandos-cheatsheet)
7. [Mapa de Puertos](#7--mapa-de-puertos)
8. [Troubleshooting](#8--troubleshooting)

---

## 1. 🎯 Propósito del Archivo

El archivo `docker-compose.deploy.yml` es el **manifiesto de despliegue para entornos de Producción, Demo y QA**.

A diferencia del `docker-compose.yml` (usado en desarrollo local), este archivo **no compila el código fuente**. En su lugar, descarga **imágenes Docker pre-construidas y optimizadas** directamente desde el Registry de **Docker Hub**:

```yaml
# Ejemplo: las imágenes se descargan listas para usar
image: ${DOCKERHUB_USERNAME}/budget-transaction:${IMAGE_TAG:-latest}
image: ${DOCKERHUB_USERNAME}/budget-report:${IMAGE_TAG:-latest}
image: ${DOCKERHUB_USERNAME}/budget-auth:${IMAGE_TAG:-latest}
image: ${DOCKERHUB_USERNAME}/budget-frontend:${IMAGE_TAG:-latest}
```

### ¿Qué significa esto para el usuario?

| Característica | `docker-compose.yml` (DEV) | `docker-compose.deploy.yml` (PROD) |
|---|---|---|
| Compilación local | ✅ Compila desde código fuente | ❌ No requiere compilar |
| Origen de imágenes | Build local desde Dockerfile | Descarga desde Docker Hub |
| Requiere código fuente | ✅ Sí | ❌ No |
| Velocidad de arranque | Lenta (build + start) | **Rápida** (solo pull + start) |
| Uso ideal | Desarrollo y debugging | Producción, Demo, QA |

> 💡 **Ventaja clave:** Cualquier servidor o máquina con Docker instalado puede levantar toda la infraestructura **sin necesidad de tener Java, Node.js, Maven ni el código fuente del proyecto.**

---

## 2. 📦 Requisitos Previos

- **Docker Engine** ≥ 24.0
- **Docker Compose** ≥ 2.20 (integrado como plugin de Docker)
- **Acceso a Internet** (para descargar las imágenes desde Docker Hub la primera vez)
- **Archivo `.env`** configurado en la raíz del proyecto (ver sección siguiente)

Verificar la instalación:

```bash
docker --version
docker compose version
```

---

## 3. 🔐 Configuración del Entorno

Antes de levantar la infraestructura, **debe existir un archivo `.env`** en la raíz del proyecto junto al `docker-compose.deploy.yml`. Este archivo contiene todas las credenciales y configuraciones sensibles.

### Crear el archivo `.env`

Copie el archivo de ejemplo y edítelo con sus valores reales:

```bash
cp .env.example .env
nano .env   # o use su editor preferido
```

### Variables requeridas

| Variable | Descripción | Ejemplo |
|---|---|---|
| `MYSQL_ROOT_PASSWORD` | Contraseña del usuario root de MySQL | `S3cur3R00tP@ss!` |
| `DB_USERNAME` | Usuario de aplicación para MySQL | `finance_user` |
| `DB_PASSWORD` | Contraseña del usuario de aplicación | `f1n@nc3_s3cur3` |
| `RABBITMQ_DEFAULT_USER` | Usuario administrador de RabbitMQ | `admin` |
| `RABBITMQ_DEFAULT_PASS` | Contraseña de RabbitMQ | `r@bb1t_s3cur3` |
| `JWT_SECRET` | Clave secreta para firma de tokens JWT (mín. 32 caracteres, Base64 recomendado) | `yK8pQv3Lx9Tz6Wm2Rj...` |
| `DOCKERHUB_USERNAME` | Usuario de Docker Hub donde residen las imágenes | `eliancondor` |
| `IMAGE_TAG` | Versión/tag de las imágenes a desplegar | `1.2.1` |

> ⚠️ **IMPORTANTE:** El archivo `.env` **nunca** debe subirse al repositorio. Verifique que esté incluido en `.gitignore`.

---

## 4. 🏗️ Arquitectura y Orquestación

### Patrón Arquitectónico: Database-per-Service

La aplicación implementa el patrón **Database-per-Service**, una best practice de arquitectura de microservicios donde cada servicio posee y gestiona su propia base de datos de forma independiente:

```
┌─────────────────────────────────────────────────────────────────────┐
│                    finance-network (bridge)                         │
│                                                                     │
│   ┌─────────────┐    ┌─────────────┐    ┌─────────────┐            │
│   │  Frontend   │    │ Transaction │    │   Report    │            │
│   │   :3000     │    │   :8081     │◄──►│   :8082     │            │
│   │   (Nginx)   │    │  (Spring)   │    │  (Spring)   │            │
│   └─────────────┘    └──────┬──────┘    └──────┬──────┘            │
│                             │                   │                   │
│                      ┌──────▼──────┐     ┌──────▼──────┐           │
│   ┌─────────────┐    │   mysql-    │     │   mysql-    │           │
│   │    Auth     │    │transactions │     │  reports    │           │
│   │   :8083     │    │   :3307     │     │   :3308     │           │
│   │  (Spring)   │    │transactions_│     │ reports_db  │           │
│   └──────┬──────┘    │     db      │     └─────────────┘           │
│          │           └─────────────┘                                │
│   ┌──────▼──────┐                        ┌─────────────┐           │
│   │  mysql-auth │                        │  RabbitMQ   │           │
│   │   :3309     │                        │ :5672/:15672│           │
│   │  auth_db    │                        │(Msg Broker) │           │
│   └─────────────┘                        └─────────────┘           │
│                                                                     │
│   Transaction ──── publica eventos ────► RabbitMQ                  │
│   Report      ◄─── consume eventos ──── RabbitMQ                  │
└─────────────────────────────────────────────────────────────────────┘
```

### Componentes del Sistema

#### 🔷 Microservicios de Aplicación

| Servicio | Imagen | Puerto | Función |
|---|---|---|---|
| **Auth** | `budget-auth` | `8083` | Autenticación y autorización (JWT). Gestión de usuarios y credenciales. |
| **Transaction** | `budget-transaction` | `8081` | CRUD de transacciones financieras. Publica eventos a RabbitMQ al crear/modificar transacciones. |
| **Report** | `budget-report` | `8082` | Generación de reportes financieros. Consume eventos de transacciones desde RabbitMQ. |
| **Frontend** | `budget-frontend` | `3000` | Interfaz de usuario (React/Vite servido por Nginx). |

#### 🔶 Infraestructura de Soporte

| Servicio | Imagen | Puerto(s) | Función |
|---|---|---|---|
| **mysql-transactions** | `mysql:8.0` | `3307` | Base de datos exclusiva para el microservicio Transaction. |
| **mysql-reports** | `mysql:8.0` | `3308` | Base de datos exclusiva para el microservicio Report. |
| **mysql-auth** | `mysql:8.0` | `3309` | Base de datos exclusiva para el microservicio Auth. |
| **RabbitMQ** | `rabbitmq:4.0-management` | `5672` / `15672` | Message Broker para comunicación asíncrona entre Transaction y Report. El puerto `15672` expone el dashboard de administración web. |

### Comunicación entre Servicios

- **Síncrona (HTTP/REST):** El Frontend se comunica con los microservicios via API REST a través de los puertos expuestos.
- **Asíncrona (AMQP/RabbitMQ):** Cuando el servicio `Transaction` registra una operación financiera, publica un evento en RabbitMQ. El servicio `Report` consume ese evento y actualiza los reportes automáticamente. Este desacoplamiento garantiza que un fallo en Report **no bloquea** las operaciones de Transaction.
- **Red interna:** Todos los contenedores se comunican dentro de la red Docker `finance-network` usando nombres DNS internos (ej. `mysql-transactions`, `rabbitmq`), sin necesidad de IPs hardcodeadas.

---

## 5. 💾 Persistencia de Datos

> ### ⚡ CRÍTICO: Los datos están protegidos por Volúmenes Nombrados (Named Volumes)

La infraestructura utiliza **Named Volumes de Docker** para garantizar que toda la información generada por la aplicación (registros en bases de datos, colas de mensajes, configuraciones) **persista de forma segura e independiente del ciclo de vida de los contenedores.**

### Volúmenes Declarados

```yaml
volumes:
  mysql-transactions-data:   # → /var/lib/mysql en mysql-transactions
  mysql-reports-data:         # → /var/lib/mysql en mysql-reports
  mysql-auth-data:            # → /var/lib/mysql en mysql-auth
  rabbitmq-data:              # → /var/lib/rabbitmq en rabbitmq
```

### ¿Qué almacena cada volumen?

| Volumen | Servicio | Ruta interna del contenedor | Datos que persisten |
|---|---|---|---|
| `mysql-transactions-data` | mysql-transactions | `/var/lib/mysql` | Tablas, índices y registros de `transactions_db` |
| `mysql-reports-data` | mysql-reports | `/var/lib/mysql` | Tablas, índices y registros de `reports_db` |
| `mysql-auth-data` | mysql-auth | `/var/lib/mysql` | Usuarios, credenciales y tokens en `auth_db` |
| `rabbitmq-data` | rabbitmq | `/var/lib/rabbitmq` | Definiciones de colas, exchanges, bindings y mensajes pendientes |

### ¿Qué pasa con mis datos en cada escenario?

| Acción | Contenedores | Volúmenes | ¿Se pierden datos? |
|---|---|---|---|
| `docker compose stop` | ⏸️ Pausados | ✅ Intactos | ❌ **No** |
| `docker compose down` | 🗑️ Eliminados | ✅ **Intactos** | ❌ **No** |
| `docker compose down -v` | 🗑️ Eliminados | 🗑️ **Eliminados** | ⚠️ **SÍ, se pierden** |
| Reinicio del servidor/host | 🔄 Se reinician (`unless-stopped`) | ✅ Intactos | ❌ **No** |

> 🛡️ **Garantía:** Al ejecutar `docker compose down` (sin el flag `-v`), Docker destruye los contenedores y la red, pero los **Named Volumes permanecen intactos** en el filesystem del host. Cuando se vuelve a ejecutar `docker compose up -d`, Docker remonta los volúmenes existentes en los nuevos contenedores y los servicios arrancan con **todos los datos previos**.

> ⚠️ **Precaución:** El flag `-v` (`--volumes`) **elimina permanentemente los volúmenes y todos los datos**. Use este flag únicamente cuando desee reiniciar el entorno desde cero.

---

## 6. 📟 Guía Rápida de Comandos (Cheatsheet)

> Todos los comandos usan el flag `-f` para referenciar explícitamente el archivo de deployment.
> Deben ejecutarse desde la **raíz del proyecto** (donde se encuentra el archivo `docker-compose.deploy.yml`).

### 🟢 Levantar toda la infraestructura desde cero

Descarga las imágenes (si es la primera vez) y levanta todos los contenedores en segundo plano:

```bash
sudo docker compose -f docker-compose.deploy.yml up -d
```

### ⏸️ Pausar el entorno (sin destruir contenedores ni datos)

Detiene todos los contenedores sin eliminarlos. Ideal para liberar recursos temporalmente:

```bash
sudo docker compose -f docker-compose.deploy.yml stop
```

### ▶️ Reanudar el entorno pausado

Reinicia los contenedores previamente detenidos con `stop`:

```bash
sudo docker compose -f docker-compose.deploy.yml start
```

### 🔴 Destruir la infraestructura (manteniendo datos a salvo)

Elimina contenedores y redes, pero **conserva los volúmenes con todos los datos**:

```bash
sudo docker compose -f docker-compose.deploy.yml down
```

### 🔄 Actualizar a una nueva versión de las imágenes

Descarga las imágenes más recientes del tag configurado y recrea los contenedores:

```bash
sudo docker compose -f docker-compose.deploy.yml pull
sudo docker compose -f docker-compose.deploy.yml up -d
```

### 📊 Ver el estado de todos los contenedores

```bash
sudo docker compose -f docker-compose.deploy.yml ps
```

### 📜 Ver logs de un servicio específico en tiempo real

```bash
# Ejemplo: ver logs del microservicio auth
sudo docker compose -f docker-compose.deploy.yml logs -f auth

# Ver logs de todos los servicios
sudo docker compose -f docker-compose.deploy.yml logs -f
```

### 🧹 Reseteo completo (⚠️ ELIMINA TODOS LOS DATOS)

> **PELIGRO:** Este comando destruye contenedores, redes **Y volúmenes**. Todos los datos de MySQL y RabbitMQ se perderán permanentemente.

```bash
sudo docker compose -f docker-compose.deploy.yml down -v
```

---

## 7. 🗺️ Mapa de Puertos

| Puerto Host | Servicio | Protocolo | Descripción |
|---|---|---|---|
| `3000` | Frontend | HTTP | Interfaz de usuario (Nginx) |
| `8081` | Transaction API | HTTP | API REST de transacciones |
| `8082` | Report API | HTTP | API REST de reportes |
| `8083` | Auth API | HTTP | API REST de autenticación |
| `3307` | MySQL Transactions | TCP | Motor de BD — `transactions_db` |
| `3308` | MySQL Reports | TCP | Motor de BD — `reports_db` |
| `3309` | MySQL Auth | TCP | Motor de BD — `auth_db` |
| `5672` | RabbitMQ | AMQP | Protocolo de mensajería |
| `15672` | RabbitMQ Management | HTTP | Dashboard de administración web |

---

## 8. 🔧 Troubleshooting

### Los microservicios no se conectan a la base de datos

Verifique que los contenedores de MySQL estén corriendo y saludables:

```bash
sudo docker compose -f docker-compose.deploy.yml ps
```

Si un contenedor MySQL aparece como `unhealthy` o `restarting`, revise sus logs:

```bash
sudo docker compose -f docker-compose.deploy.yml logs mysql-auth
```

### El frontend no carga o muestra errores de red

Las URLs de las APIs están embebidas en el build del frontend. Verifique que los microservicios estén levantados y accesibles en los puertos `8081`, `8082` y `8083` del host.

### Verificar que los volúmenes existen

```bash
sudo docker volume ls | grep budget_management_app
```

Resultado esperado:

```
local     budget_management_app_mysql-transactions-data
local     budget_management_app_mysql-reports-data
local     budget_management_app_mysql-auth-data
local     budget_management_app_rabbitmq-data
```

### Acceder al dashboard de RabbitMQ

Navegue a `http://localhost:15672` e ingrese con las credenciales definidas en `RABBITMQ_DEFAULT_USER` y `RABBITMQ_DEFAULT_PASS` del archivo `.env`.

---

<p align="center">
  <strong>Budget Management App</strong> · Documentación de Despliegue<br>
  <em>Generado para el equipo de desarrollo — Fase 2: Contenerización</em>
</p>
