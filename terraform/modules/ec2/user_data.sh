#!/bin/bash
set -e

echo "======================================"
echo "Instalando Docker y dependencias..."
echo "======================================"

# Update system
sudo dnf update -y

# Install Git
sudo dnf install -y git

# Install Docker
sudo dnf install -y docker
sudo systemctl enable docker
sudo systemctl start docker

# Install Docker Compose (standalone version)
sudo curl -L "https://github.com/docker/compose/releases/download/v2.24.6/docker-compose-$(uname -s)-$(uname -m)" \
-o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Install Docker Buildx
sudo mkdir -p /usr/local/lib/docker/cli-plugins
sudo curl -L "https://github.com/docker/buildx/releases/download/v0.17.1/buildx-v0.17.1.linux-amd64" \
-o /usr/local/lib/docker/cli-plugins/docker-buildx
sudo chmod +x /usr/local/lib/docker/cli-plugins/docker-buildx

# Add ec2-user to docker group
sudo usermod -aG docker ec2-user

echo "======================================"
echo "Clonando repositorio..."
echo "======================================"

# Clone the repository
cd /home/ec2-user
if [ -d "sofkianos-mvp" ]; then
    echo "Directorio ya existe, actualizando..."
    cd sofkianos-mvp
    sudo -u ec2-user git pull
else
    sudo -u ec2-user git clone https://github.com/ChristopherPalloArias/Budget_Management_App.git
    cd Budget_Management_App
fi

# Checkout release branch
sudo -u ec2-user git checkout release/1.2.1

# Setup environment file
if [ ! -f ".env" ]; then
    sudo -u ec2-user cp .env.example .env
fi

# Fix permissions
sudo chown -R ec2-user:ec2-user /home/ec2-user/Budget_Management_App

# Remove version line from docker-compose.yml to avoid warning
sudo sed -i '/^version:/d' docker-compose.yml

echo "======================================"
echo "Verificando instalación..."
echo "======================================"
docker --version
docker-compose --version
docker buildx version

echo "======================================"
echo "Iniciando contenedores..."
echo "======================================"
sudo docker-compose -f docker-compose.deploy.yml up -d --build

# Build and start containers using screen to avoid SSH disconnection

echo "======================================"
echo "Verificando estado de contenedores..."
echo "======================================"
sleep 5
sudo docker-compose -f docker-compose.prod.yml ps

echo "======================================"
echo "¡Instalación completada!"
echo "======================================"
echo ""
echo "IMPORTANTE: Cierra sesión y vuelve a entrar para usar Docker sin sudo:"
echo "  exit"
echo ""
echo "Servicios disponibles en:"
echo "  - Frontend: http://$(curl -s ifconfig.me):5173"
echo "  - Producer API: http://$(curl -s ifconfig.me):8082"
echo "  - Consumer Worker: http://$(curl -s ifconfig.me):8081"
echo "  - RabbitMQ Management: http://$(curl -s ifconfig.me):15672"
echo "  - Log Viewer: http://$(curl -s ifconfig.me):8888"
echo ""
echo "Para ver logs: sudo docker-compose -f docker-compose.prod.yml logs -f"
echo "Para ver estado: sudo docker-compose -f docker-compose.prod.yml ps"
echo "======================================"