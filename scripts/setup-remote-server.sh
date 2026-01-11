#!/bin/bash
# 远程服务器初始化脚本

set -e

echo "🚀 开始初始化远程服务器..."

# 创建应用目录
sudo mkdir -p /opt/easycare
cd /opt/easycare

# 安装 Docker（如果未安装）
if ! command -v docker &> /dev/null; then
    echo "📦 安装 Docker..."
    curl -fsSL https://get.docker.com -o get-docker.sh
    sudo sh get-docker.sh
    sudo usermod -aG docker $USER
    rm get-docker.sh
    echo "✅ Docker 安装完成"
else
    echo "✅ Docker 已安装"
fi

# 安装 Docker Compose（如果未安装）
if ! command -v docker-compose &> /dev/null; then
    echo "📦 安装 Docker Compose..."
    sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
    sudo chmod +x /usr/local/bin/docker-compose
    echo "✅ Docker Compose 安装完成"
else
    echo "✅ Docker Compose 已安装"
fi

# 创建 docker-compose.yml 文件
cat > docker-compose.yml << 'EOF'
version: '3.8'

services:
  easycare-app:
    image: ${DOCKER_USERNAME}/easycare-service:latest
    container_name: easycare-service
    restart: unless-stopped
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/hr_maternity
      - SPRING_DATASOURCE_USERNAME=${DB_USERNAME}
      - SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}
      - TZ=Asia/Shanghai
    networks:
      - easycare-network
    depends_on:
      - postgres
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 40s
    logging:
      driver: "json-file"
      options:
        max-size: "10m"
        max-file: "3"

  postgres:
    image: postgres:15-alpine
    container_name: easycare-postgres
    restart: unless-stopped
    environment:
      - POSTGRES_DB=hr_maternity
      - POSTGRES_USER=${DB_USERNAME}
      - POSTGRES_PASSWORD=${DB_PASSWORD}
      - TZ=Asia/Shanghai
    volumes:
      - postgres-data:/var/lib/postgresql/data
    networks:
      - easycare-network
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${DB_USERNAME}"]
      interval: 10s
      timeout: 5s
      retries: 5

networks:
  easycare-network:
    driver: bridge

volumes:
  postgres-data:
    driver: local
EOF

echo "✅ docker-compose.yml 创建完成"

# 创建 .env 文件
cat > .env << 'EOF'
# Docker Hub 配置
DOCKER_USERNAME=your-dockerhub-username

# 数据库配置
DB_USERNAME=hr-maternity-cn
DB_PASSWORD=hrdb@ocbc

# 应用配置
SPRING_PROFILES_ACTIVE=prod
EOF

echo "✅ .env 文件创建完成"

# 设置文件权限
sudo chown -R $USER:$USER /opt/easycare
chmod 600 .env

echo ""
echo "✅ 远程服务器初始化完成！"
echo ""
echo "📝 下一步操作："
echo "   1. 编辑 /opt/easycare/.env 文件，填入正确的配置信息"
echo "      sudo nano /opt/easycare/.env"
echo ""
echo "   2. 在 GitHub 仓库设置 Secrets（Settings → Secrets and variables → Actions）："
echo "      - DOCKER_USERNAME: Docker Hub 用户名"
echo "      - DOCKER_PASSWORD: Docker Hub 密码或 Token"
echo "      - REMOTE_HOST: 本服务器的 IP 地址"
echo "      - REMOTE_USER: SSH 用户名（当前用户: $USER）"
echo "      - REMOTE_SSH_KEY: SSH 私钥"
echo "      - REMOTE_PORT: SSH 端口（默认: 22）"
echo ""
echo "   3. 推送代码到 main 分支触发自动部署"
echo "      git push origin main"
echo ""
