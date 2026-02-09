# Frontend de Gestión Financiera

![React](https://img.shields.io/badge/React-20232A?style=for-the-badge&logo=react&logoColor=61DAFB)
![Vite](https://img.shields.io/badge/Vite-646CFF?style=for-the-badge&logo=Vite&logoColor=white)
![TypeScript](https://img.shields.io/badge/TypeScript-007ACC?style=for-the-badge&logo=typescript&logoColor=white)
![Tailwind CSS](https://img.shields.io/badge/Tailwind_CSS-38B2AC?style=for-the-badge&logo=tailwind-css&logoColor=white)
![pnpm](https://img.shields.io/badge/pnpm-F69220?style=for-the-badge&logo=pnpm&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)

Este proyecto es una aplicación web moderna para la gestión financiera, construida con un stack tecnológico robusto y escalable. Utiliza **React** y **Vite** para el frontend, estilizado con **Tailwind CSS** y componentes de **shadcn/ui**.

## 📋 Prerrequisitos

Antes de comenzar, asegúrate de tener instaladas las siguientes herramientas en tu entorno:

- **Node.js**: Versión v20.0.0 o superior.
- **pnpm**: Gestor de paquetes recomendado.
- **Docker** y **Docker Compose**: Para contenedorización y despliegue.

## 🚀 Instalación Local

Sigue estos pasos para levantar el proyecto en tu máquina local:

### 1. Clonar el repositorio

```bash
git clone <URL_DEL_REPOSITORIO>
cd Frontend
```

### 2. Instalar dependencias

Utiliza `pnpm` para instalar todas las librerías necesarias:

```bash
pnpm install
```

### 3. Configuración de Variables de Entorno

Crea un archivo `.env` en la raíz del proyecto basándote en el ejemplo proporcionado (`.env.example`).

```bash
cp .env.example .env
```

Configura las siguientes variables:

**Configuración de Firebase:**
Estas credenciales son necesarias para la autenticación y otros servicios de Firebase.
- `VITE_FIREBASE_API_KEY`: Tu API Key de Firebase.
- `VITE_FIREBASE_AUTH_DOMAIN`: Dominio de autenticación de tu proyecto.
- `VITE_FIREBASE_PROJECT_ID`: ID del proyecto en Firebase.
- `VITE_FIREBASE_STORAGE_BUCKET`: Bucket de almacenamiento.
- `VITE_FIREBASE_MESSAGING_SENDER_ID`: ID del remitente para mensajería.
- `VITE_FIREBASE_APP_ID`: ID de la aplicación.

**Configuración de API Backend:**
URLs base para conectar con los microservicios del backend.
- `VITE_API_TRANSACTIONS_URL`: URL del servicio de transacciones (ej. `http://localhost:8081/api`).
- `VITE_API_REPORTS_URL`: URL del servicio de reportes (ej. `http://localhost:8082/api`).

### 4. Ejecutar en modo desarrollo

Inicia el servidor de desarrollo local:

```bash
pnpm run dev
```

La aplicación estará disponible en la URL que indique la consola (usualmente `http://localhost:5173`).

## 🐳 Despliegue con Docker

El proyecto incluye configuración para desplegarse mediante contenedores Docker, servido por Nginx en producción.

### Construcción y Ejecución

Para construir la imagen y levantar el servicio utilizando Docker Compose:

```bash
docker compose up --build -d
```

Este comando:
1. Construirá la imagen Docker del frontend.
2. Iniciará el contenedor `finance-frontend`.
3. Expondrá la aplicación en el puerto **3000**.

Accede a la aplicación en: `http://localhost:3000`

> **Nota:** Docker Compose tomará automáticamente las variables definidas en tu archivo `.env`.

## 📜 Scripts Disponibles

En el `package.json` encontrarás los siguientes comandos útiles:

- **`pnpm run dev`**: Inicia el servidor de desarrollo con Hot Module Replacement (HMR).
- **`pnpm run build`**: Compila el proyecto TypeScript y genera los archivos estáticos optimizados para producción en la carpeta `dist`.
- **`pnpm run preview`**: Sirve localmente los archivos de la carpeta `dist` para previsualizar el build de producción.
- **`pnpm run lint`**: Ejecuta ESLint para analizar el código en busca de errores y problemas de estilo.
