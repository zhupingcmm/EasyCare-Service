# 部署指南

## 📋 目录

- [前置条件](#前置条件)
- [部署架构](#部署架构)
- [快速开始](#快速开始)
- [详细步骤](#详细步骤)
- [常见问题](#常见问题)
- [运维操作](#运维操作)

---

## 前置条件

### 1. Docker Hub 账号

- 注册 Docker Hub 账号：https://hub.docker.com/
- 创建 Access Token：
  1. 登录 Docker Hub
  2. Settings → Security → New Access Token
  3. 保存生成的 Token（只显示一次）

### 2. 远程服务器要求

- **操作系统**：Ubuntu 20.04+ / CentOS 7+ / Debian 10+
- **最低配置**：2 核 CPU，4GB 内存，20GB 磁盘
- **推荐配置**：4 核 CPU，8GB 内存，50GB 磁盘
- **网络要求**：
  - 开放端口：8080（应用）
  - 可选端口：5432（PostgreSQL，如需外部访问）
  - SSH 访问权限（默认端口 22）
- **软件要求**：
  - Docker 20.10+
  - Docker Compose 2.0+
  - curl（用于健康检查）

### 3. GitHub 仓库配置

需要配置以下 Secrets（Settings → Secrets and variables → Actions）：

| Secret 名称 | 说明 | 示例值 |
|------------|------|--------|
| `DOCKER_USERNAME` | Docker Hub 用户名 | `your-username` |
| `DOCKER_PASSWORD` | Docker Hub 密码或 Token | `dckr_pat_xxxxx` |
| `REMOTE_HOST` | 远程服务器 IP 或域名 | `192.168.1.100` 或 `server.example.com` |
| `REMOTE_USER` | 远程服务器 SSH 用户名 | `ubuntu` 或 `root` |
| `REMOTE_SSH_KEY` | 远程服务器 SSH 私钥 | `-----BEGIN RSA PRIVATE KEY-----...` |
| `REMOTE_PORT` | 远程服务器 SSH 端口 | `22` |

---

## 部署架构

```
┌─────────────────────────────────────────────────────────┐
│                    GitHub Repository                     │
│  ┌────────────────────────────────────────────────────┐ │
│  │  Push to main branch                               │ │
│  └────────────────┬───────────────────────────────────┘ │
└───────────────────┼─────────────────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────────────────────┐
│                  GitHub Actions                          │
│  ┌────────────┐  ┌────────────┐  ┌──────────────────┐  │
│  │ Build JAR  │→ │Build Docker│→ │Push to Docker Hub│  │
│  └────────────┘  └────────────┘  └──────────────────┘  │
└───────────────────┬─────────────────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────────────────────┐
│                   Docker Hub                             │
│         easycare-service:latest                          │
│         easycare-service:1.0.0                           │
│         easycare-service:commit-sha                      │
└───────────────────┬─────────────────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────────────────────┐
│                  Remote Server                           │
│  ┌────────────────────────────────────────────────────┐ │
│  │  Docker Compose                                    │ │
│  │  ┌──────────────────┐  ┌────────────────────────┐ │ │
│  │  │ easycare-service │  │  PostgreSQL Database   │ │ │
│  │  │   Port: 8080     │  │    Port: 5432          │ │ │
│  │  └──────────────────┘  └────────────────────────┘ │ │
│  └────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────┘
```

---

## 快速开始

### 一键部署（3 步完成）

```bash
# 1. 在远程服务器上运行初始化脚本
curl -O https://raw.githubusercontent.com/your-org/EasyCare-Service/main/scripts/setup-remote-server.sh
chmod +x setup-remote-server.sh
./setup-remote-server.sh

# 2. 编辑配置文件
sudo nano /opt/easycare/.env

# 3. 在 GitHub 配置 Secrets 后，推送代码到 main 分支
git push origin main
```

---

## 详细步骤

### 步骤 1：准备远程服务器

#### 1.1 SSH 登录远程服务器

```bash
ssh user@your-server-ip
```

#### 1.2 下载并运行初始化脚本

```bash
# 下载脚本
curl -O https://raw.githubusercontent.com/your-org/EasyCare-Service/main/scripts/setup-remote-server.sh

# 添加执行权限
chmod +x setup-remote-server.sh

# 运行脚本
./setup-remote-server.sh
```

脚本会自动完成：
- ✅ 安装 Docker 和 Docker Compose
- ✅ 创建 `/opt/easycare` 目录
- ✅ 生成 `docker-compose.yml` 配置文件
- ✅ 生成 `.env` 环境变量模板

#### 1.3 配置环境变量

```bash
# 编辑 .env 文件
sudo nano /opt/easycare/.env
```

修改以下配置：

```env
# Docker Hub 配置（必须修改）
DOCKER_USERNAME=your-dockerhub-username

# 数据库配置（根据实际情况修改）
DB_USERNAME=hr-maternity-cn
DB_PASSWORD=your-secure-password

# 应用配置
SPRING_PROFILES_ACTIVE=prod
```

---

### 步骤 2：配置 GitHub Secrets

#### 2.1 生成 SSH 密钥对（如果没有）

在**本地机器**上运行：

```bash
# 生成新的 SSH 密钥对
ssh-keygen -t rsa -b 4096 -C "github-actions@easycare" -f ~/.ssh/easycare_deploy

# 查看公钥
cat ~/.ssh/easycare_deploy.pub

# 查看私钥（用于 GitHub Secret）
cat ~/.ssh/easycare_deploy
```

#### 2.2 将公钥添加到远程服务器

```bash
# 复制公钥内容
cat ~/.ssh/easycare_deploy.pub

# SSH 登录远程服务器
ssh user@your-server-ip

# 添加公钥到 authorized_keys
echo "your-public-key-content" >> ~/.ssh/authorized_keys
chmod 600 ~/.ssh/authorized_keys
```

#### 2.3 在 GitHub 配置 Secrets

1. 打开 GitHub 仓库
2. 进入 **Settings** → **Secrets and variables** → **Actions**
3. 点击 **New repository secret**
4. 添加以下 Secrets：

**DOCKER_USERNAME**
```
your-dockerhub-username
```

**DOCKER_PASSWORD**
```
dckr_pat_xxxxxxxxxxxxxxxxxxxxxxxxxx
```

**REMOTE_HOST**
```
192.168.1.100
```

**REMOTE_USER**
```
ubuntu
```

**REMOTE_SSH_KEY**
```
-----BEGIN RSA PRIVATE KEY-----
MIIEpAIBAAKCAQEA...
（完整的私钥内容）
...
-----END RSA PRIVATE KEY-----
```

**REMOTE_PORT**
```
22
```

---

### 步骤 3：触发部署

#### 方式 1：推送代码到 main 分支

```bash
# 切换到 main 分支
git checkout main

# 合并开发分支
git merge develop

# 推送到远程仓库
git push origin main
```

#### 方式 2：手动触发工作流

1. 打开 GitHub 仓库
2. 进入 **Actions** 标签
3. 选择 **Build and Deploy to Production** 工作流
4. 点击 **Run workflow**
5. 选择 **main** 分支
6. 点击 **Run workflow** 按钮

---

### 步骤 4：验证部署

#### 4.1 查看 GitHub Actions 日志

在 GitHub Actions 页面查看部署进度和日志。

#### 4.2 SSH 登录远程服务器验证

```bash
# SSH 登录
ssh user@your-server-ip

# 进入应用目录
cd /opt/easycare

# 查看容器状态
docker-compose ps

# 查看应用日志
docker-compose logs -f easycare-app

# 测试健康检查
curl http://localhost:8080/health/alive

# 测试 API
curl http://localhost:8080/api/support/cities
```

---

## 常见问题

### 1. 构建失败

**问题**：Maven 构建失败

**解决方案**：
```bash
# 检查 pom.xml 依赖是否正确
# 检查 JDK 版本是否为 21
# 查看 GitHub Actions 日志中的错误信息
```

---

### 2. Docker 镜像推送失败

**问题**：无法推送镜像到 Docker Hub

**解决方案**：
```bash
# 1. 检查 DOCKER_USERNAME 和 DOCKER_PASSWORD 是否正确
# 2. 确认 Docker Hub Token 有推送权限
# 3. 检查网络连接
```

---

### 3. SSH 连接失败

**问题**：GitHub Actions 无法连接到远程服务器

**解决方案**：
```bash
# 1. 检查 REMOTE_HOST、REMOTE_USER、REMOTE_PORT 是否正确
# 2. 验证 SSH 私钥格式是否正确（包含完整的 BEGIN 和 END 标记）
# 3. 确认远程服务器防火墙允许 SSH 连接
# 4. 测试 SSH 连接
ssh -i ~/.ssh/easycare_deploy user@your-server-ip
```

---

### 4. 应用启动失败

**问题**：容器启动后立即退出

**解决方案**：
```bash
# 查看容器日志
docker-compose logs easycare-app

# 常见原因：
# 1. 数据库连接失败 - 检查 .env 中的数据库配置
# 2. 端口被占用 - 检查 8080 端口是否被占用
# 3. 环境变量错误 - 检查 .env 文件配置

# 检查端口占用
sudo netstat -tlnp | grep 8080

# 重启服务
docker-compose down
docker-compose up -d
```

---

### 5. 健康检查失败

**问题**：健康检查接口返回错误

**解决方案**：
```bash
# 1. 检查应用是否完全启动（可能需要等待更长时间）
sleep 30
curl http://localhost:8080/health/alive

# 2. 检查应用日志
docker-compose logs easycare-app | grep -i error

# 3. 进入容器检查
docker-compose exec easycare-app sh
curl http://localhost:8080/health/alive
```

---

## 运维操作

### 查看日志

```bash
# 实时查看所有服务日志
docker-compose logs -f

# 查看应用日志
docker-compose logs -f easycare-app

# 查看数据库日志
docker-compose logs -f postgres

# 查看最近 100 行日志
docker-compose logs --tail=100 easycare-app
```

---

### 重启服务

```bash
# 重启所有服务
docker-compose restart

# 重启应用服务
docker-compose restart easycare-app

# 重启数据库服务
docker-compose restart postgres
```

---

### 停止和启动服务

```bash
# 停止所有服务
docker-compose down

# 启动所有服务
docker-compose up -d

# 停止并删除所有容器和网络（保留数据卷）
docker-compose down

# 停止并删除所有内容（包括数据卷）
docker-compose down -v
```

---

### 更新应用

```bash
# 拉取最新镜像
docker pull your-username/easycare-service:latest

# 重启服务
docker-compose down
docker-compose up -d
```

---

### 回滚到指定版本

```bash
# 查看可用的镜像版本
docker images | grep easycare-service

# 拉取指定版本
docker pull your-username/easycare-service:1.0.0

# 修改 docker-compose.yml 中的镜像版本
# 或者使用 tag 命令
docker tag your-username/easycare-service:1.0.0 your-username/easycare-service:latest

# 重启服务
docker-compose down
docker-compose up -d
```

---

### 数据库备份

```bash
# 备份数据库
docker-compose exec postgres pg_dump -U hr-maternity-cn hr_maternity > backup_$(date +%Y%m%d_%H%M%S).sql

# 恢复数据库
docker-compose exec -T postgres psql -U hr-maternity-cn hr_maternity < backup_20260105_200000.sql
```

---

### 清理旧镜像

```bash
# 清理未使用的镜像
docker image prune -f

# 清理所有未使用的资源（镜像、容器、网络、卷）
docker system prune -a -f
```

---

### 监控资源使用

```bash
# 查看容器资源使用情况
docker stats

# 查看磁盘使用情况
df -h

# 查看 Docker 磁盘使用
docker system df
```

---

### 进入容器调试

```bash
# 进入应用容器
docker-compose exec easycare-app sh

# 进入数据库容器
docker-compose exec postgres sh

# 在容器内执行命令
docker-compose exec easycare-app curl http://localhost:8080/health/alive
```

---

## 安全建议

1. **使用强密码**：数据库密码应使用强密码
2. **定期更新**：定期更新 Docker 镜像和系统软件
3. **限制访问**：使用防火墙限制不必要的端口访问
4. **备份数据**：定期备份数据库和重要配置文件
5. **监控日志**：定期检查应用和系统日志
6. **使用 HTTPS**：生产环境建议配置 HTTPS（使用 Nginx 反向代理）

---

## 附录

### A. 完整的文件结构

```
EasyCare-Service/
├── .github/
│   └── workflows/
│       └── deploy-to-production.yml    # GitHub Actions 工作流
├── deployment/
│   ├── docker-compose.yml              # Docker Compose 配置
│   └── .env.example                    # 环境变量示例
├── scripts/
│   └── setup-remote-server.sh          # 服务器初始化脚本
├── docs/
│   └── deployment-guide.md             # 本文档
└── Dockerfile                          # Docker 镜像构建文件
```

### B. 相关链接

- Docker Hub: https://hub.docker.com/
- Docker 文档: https://docs.docker.com/
- GitHub Actions 文档: https://docs.github.com/en/actions
- PostgreSQL 文档: https://www.postgresql.org/docs/

---

**最后更新时间**：2026-01-05
