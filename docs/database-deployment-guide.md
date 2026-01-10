# PostgreSQL 数据库独立部署指南

本文档说明如何在远程服务器上独立部署 PostgreSQL 数据库，供 EasyCare 应用使用。

---

## 📋 目录

- [部署方式选择](#部署方式选择)
- [方式1：Docker 部署 PostgreSQL](#方式1docker-部署-postgresql)
- [方式2：系统原生安装](#方式2系统原生安装)
- [数据库初始化](#数据库初始化)
- [应用配置](#应用配置)
- [安全配置](#安全配置)
- [备份和恢复](#备份和恢复)

---

## 部署方式选择

### 方式对比

| 方式 | 优点 | 缺点 | 适用场景 |
|------|------|------|---------|
| Docker 部署 | 快速部署、易于迁移、版本控制 | 性能略低 | 测试环境、小型应用 |
| 系统原生安装 | 性能最佳、稳定性高 | 配置复杂 | 生产环境、大型应用 |
| 云数据库服务 | 免运维、高可用 | 成本较高 | 企业级应用 |

---

## 方式1：Docker 部署 PostgreSQL

### 步骤 1：创建数据库容器

```bash
# 创建数据目录
sudo mkdir -p /opt/postgres/data

# 运行 PostgreSQL 容器
docker run -d \
  --name easycare-postgres \
  --restart unless-stopped \
  -e POSTGRES_DB=hr_maternity \
  -e POSTGRES_USER=hr-maternity-cn \
  -e POSTGRES_PASSWORD=your-secure-password \
  -e TZ=Asia/Shanghai \
  -v /opt/postgres/data:/var/lib/postgresql/data \
  -p 5432:5432 \
  postgres:15-alpine
```

### 步骤 2：验证数据库运行

```bash
# 查看容器状态
docker ps | grep postgres

# 查看日志
docker logs easycare-postgres

# 测试连接
docker exec -it easycare-postgres psql -U hr-maternity-cn -d hr_maternity
```

### 步骤 3：配置应用连接

编辑 `/opt/easycare/.env` 文件：

```env
DB_URL=jdbc:postgresql://localhost:5432/hr_maternity
DB_USERNAME=hr-maternity-cn
DB_PASSWORD=your-secure-password
```

---

## 方式2：系统原生安装

### Ubuntu/Debian 系统

#### 步骤 1：安装 PostgreSQL

```bash
# 更新软件包列表
sudo apt update

# 安装 PostgreSQL 15
sudo apt install -y postgresql-15 postgresql-contrib-15

# 启动服务
sudo systemctl start postgresql
sudo systemctl enable postgresql

# 检查状态
sudo systemctl status postgresql
```

#### 步骤 2：创建数据库和用户

```bash
# 切换到 postgres 用户
sudo -u postgres psql

# 在 PostgreSQL 命令行中执行：
CREATE DATABASE hr_maternity;
CREATE USER "hr-maternity-cn" WITH PASSWORD 'your-secure-password';
GRANT ALL PRIVILEGES ON DATABASE hr_maternity TO "hr-maternity-cn";

# 退出
\q
```

#### 步骤 3：配置远程访问（如需要）

编辑 PostgreSQL 配置文件：

```bash
# 编辑 postgresql.conf
sudo nano /etc/postgresql/15/main/postgresql.conf

# 修改监听地址（允许所有IP访问，生产环境建议指定具体IP）
listen_addresses = '*'

# 编辑 pg_hba.conf
sudo nano /etc/postgresql/15/main/pg_hba.conf

# 添加以下行（允许密码认证）
host    hr_maternity    hr-maternity-cn    0.0.0.0/0    md5

# 重启 PostgreSQL
sudo systemctl restart postgresql
```

#### 步骤 4：配置防火墙

```bash
# 允许 PostgreSQL 端口
sudo ufw allow 5432/tcp

# 或者只允许特定 IP
sudo ufw allow from 192.168.1.100 to any port 5432
```

### CentOS/RHEL 系统

```bash
# 安装 PostgreSQL 15
sudo dnf install -y postgresql15-server postgresql15-contrib

# 初始化数据库
sudo /usr/pgsql-15/bin/postgresql-15-setup initdb

# 启动服务
sudo systemctl start postgresql-15
sudo systemctl enable postgresql-15

# 后续步骤与 Ubuntu 相同
```

---

## 数据库初始化

### 执行 Flyway 迁移脚本

应用启动时会自动执行 Flyway 数据库迁移脚本，位于：

```
src/main/resources/db/migration/
├── V1__init_schema.sql
├── V2__add_tables.sql
└── ...
```

### 手动初始化（如需要）

```bash
# 连接到数据库
psql -h localhost -U hr-maternity-cn -d hr_maternity

# 执行初始化脚本
\i /path/to/init.sql

# 查看表
\dt

# 退出
\q
```

---

## 应用配置

### 配置文件位置

`/opt/easycare/.env`

### 配置示例

```env
# Docker Hub 配置
DOCKER_USERNAME=pingzhu

# 数据库配置
# 本地数据库
DB_URL=jdbc:postgresql://localhost:5432/hr_maternity

# 远程数据库
# DB_URL=jdbc:postgresql://192.168.1.100:5432/hr_maternity

# 云数据库（示例）
# DB_URL=jdbc:postgresql://your-db.rds.amazonaws.com:5432/hr_maternity

DB_USERNAME=hr-maternity-cn
DB_PASSWORD=your-secure-password

# 应用配置
SPRING_PROFILES_ACTIVE=prod
```

### 连接池配置（可选）

在 `application-prod.properties` 中配置：

```properties
# 连接池配置
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.idle-timeout=600000
spring.datasource.hikari.max-lifetime=1800000
```

---

## 安全配置

### 1. 使用强密码

```bash
# 生成随机密码
openssl rand -base64 32
```

### 2. 限制网络访问

```bash
# pg_hba.conf 中只允许特定 IP
host    hr_maternity    hr-maternity-cn    192.168.1.100/32    md5
```

### 3. 启用 SSL 连接（生产环境推荐）

```bash
# 生成自签名证书
sudo openssl req -new -x509 -days 365 -nodes -text \
  -out /etc/postgresql/15/main/server.crt \
  -keyout /etc/postgresql/15/main/server.key

# 设置权限
sudo chmod 600 /etc/postgresql/15/main/server.key
sudo chown postgres:postgres /etc/postgresql/15/main/server.*

# 编辑 postgresql.conf
ssl = on
ssl_cert_file = '/etc/postgresql/15/main/server.crt'
ssl_key_file = '/etc/postgresql/15/main/server.key'

# 重启 PostgreSQL
sudo systemctl restart postgresql
```

应用配置：

```env
DB_URL=jdbc:postgresql://localhost:5432/hr_maternity?ssl=true&sslmode=require
```

### 4. 定期更新密码

```sql
-- 连接到数据库
ALTER USER "hr-maternity-cn" WITH PASSWORD 'new-secure-password';
```

---

## 备份和恢复

### 自动备份脚本

创建备份脚本 `/opt/scripts/backup-postgres.sh`：

```bash
#!/bin/bash
# PostgreSQL 自动备份脚本

BACKUP_DIR="/opt/backups/postgres"
DATE=$(date +%Y%m%d_%H%M%S)
DB_NAME="hr_maternity"
DB_USER="hr-maternity-cn"

# 创建备份目录
mkdir -p $BACKUP_DIR

# 执行备份
docker exec easycare-postgres pg_dump -U $DB_USER $DB_NAME > $BACKUP_DIR/backup_${DATE}.sql

# 或者原生安装使用：
# pg_dump -U $DB_USER -h localhost $DB_NAME > $BACKUP_DIR/backup_${DATE}.sql

# 压缩备份文件
gzip $BACKUP_DIR/backup_${DATE}.sql

# 删除 7 天前的备份
find $BACKUP_DIR -name "backup_*.sql.gz" -mtime +7 -delete

echo "备份完成: backup_${DATE}.sql.gz"
```

设置定时任务：

```bash
# 添加执行权限
chmod +x /opt/scripts/backup-postgres.sh

# 编辑 crontab
crontab -e

# 每天凌晨 2 点执行备份
0 2 * * * /opt/scripts/backup-postgres.sh >> /var/log/postgres-backup.log 2>&1
```

### 手动备份

```bash
# Docker 部署
docker exec easycare-postgres pg_dump -U hr-maternity-cn hr_maternity > backup.sql

# 原生安装
pg_dump -U hr-maternity-cn -h localhost hr_maternity > backup.sql

# 压缩备份
gzip backup.sql
```

### 恢复数据库

```bash
# Docker 部署
gunzip -c backup.sql.gz | docker exec -i easycare-postgres psql -U hr-maternity-cn hr_maternity

# 原生安装
gunzip -c backup.sql.gz | psql -U hr-maternity-cn -h localhost hr_maternity
```

---

## 监控和维护

### 查看数据库状态

```sql
-- 连接到数据库
psql -U hr-maternity-cn -d hr_maternity

-- 查看数据库大小
SELECT pg_size_pretty(pg_database_size('hr_maternity'));

-- 查看表大小
SELECT 
    schemaname,
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) AS size
FROM pg_tables
WHERE schemaname = 'public'
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;

-- 查看活动连接
SELECT * FROM pg_stat_activity WHERE datname = 'hr_maternity';

-- 查看慢查询
SELECT query, calls, total_time, mean_time 
FROM pg_stat_statements 
ORDER BY mean_time DESC 
LIMIT 10;
```

### 性能优化

```sql
-- 分析表统计信息
ANALYZE;

-- 清理死元组
VACUUM;

-- 完全清理和分析
VACUUM FULL ANALYZE;

-- 重建索引
REINDEX DATABASE hr_maternity;
```

### 日志查看

```bash
# Docker 部署
docker logs easycare-postgres

# 原生安装
sudo tail -f /var/log/postgresql/postgresql-15-main.log
```

---

## 故障排查

### 问题1：无法连接数据库

**检查步骤**：

```bash
# 1. 检查 PostgreSQL 是否运行
docker ps | grep postgres
# 或
sudo systemctl status postgresql

# 2. 检查端口是否监听
sudo netstat -tlnp | grep 5432

# 3. 测试本地连接
psql -U hr-maternity-cn -h localhost -d hr_maternity

# 4. 检查防火墙
sudo ufw status
```

### 问题2：权限不足

```sql
-- 授予所有权限
GRANT ALL PRIVILEGES ON DATABASE hr_maternity TO "hr-maternity-cn";
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO "hr-maternity-cn";
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO "hr-maternity-cn";
```

### 问题3：磁盘空间不足

```bash
# 查看磁盘使用
df -h

# 清理旧的 WAL 日志
# 编辑 postgresql.conf
wal_keep_size = 1GB

# 清理旧备份
find /opt/backups/postgres -name "*.sql.gz" -mtime +30 -delete
```

---

## 云数据库服务

### AWS RDS PostgreSQL

```env
DB_URL=jdbc:postgresql://your-instance.rds.amazonaws.com:5432/hr_maternity
DB_USERNAME=hr_maternity_cn
DB_PASSWORD=your-secure-password
```

### 阿里云 RDS

```env
DB_URL=jdbc:postgresql://your-instance.pg.rds.aliyuncs.com:5432/hr_maternity
DB_USERNAME=hr_maternity_cn
DB_PASSWORD=your-secure-password
```

### 腾讯云 PostgreSQL

```env
DB_URL=jdbc:postgresql://your-instance.tencentcdb.com:5432/hr_maternity
DB_USERNAME=hr_maternity_cn
DB_PASSWORD=your-secure-password
```

---

## 总结

- ✅ **Docker 部署**：快速、简单，适合测试和小型应用
- ✅ **原生安装**：性能最佳，适合生产环境
- ✅ **云数据库**：免运维，适合企业级应用
- ✅ **定期备份**：确保数据安全
- ✅ **监控维护**：保持数据库健康运行

**最后更新时间**：2026-01-06
