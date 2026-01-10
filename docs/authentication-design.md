# EasyCare Service - 登录与认证系统设计文档

## 1. 概述

### 1.1 文档目的
本文档详细描述 EasyCare Service 项目的登录与认证系统的架构设计、技术选型、安全机制和实现细节。

### 1.2 系统简介
EasyCare Service 采用基于 JWT 的无状态认证方案，支持 LDAP 企业目录集成和本地用户管理，提供完整的用户登录、登出、令牌刷新和权限管理功能。

### 1.3 核心特性
- **多认证源支持**：LDAP 企业目录 + 本地数据库
- **双令牌机制**：JWT 令牌 + Opaque 令牌
- **灵活的加密方案**：支持 RSA 和 HMAC 算法
- **安全增强**：密码加密传输、防重放攻击、令牌撤销
- **角色权限管理**：基于部门的自动角色分配
- **开发友好**：Mock 模式支持开发测试

---

## 2. 架构设计

### 2.1 整体架构

```
┌─────────────────────────────────────────────────────────────┐
│                         前端应用                              │
│  - 获取 RSA 公钥                                              │
│  - 生成 nonce                                                │
│  - 加密用户密码                                               │
│  - 发送登录请求                                               │
└────────────────────┬────────────────────────────────────────┘
                     │ HTTPS
                     ▼
┌─────────────────────────────────────────────────────────────┐
│                    Controller 层                             │
│  LoginController: /api/auth/*                                │
│  - 公钥获取、nonce 生成                                       │
│  - 登录、登出、令牌刷新                                       │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│                     Service 层                               │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ LoginService │  │JwtTokenService│  │ LdapService  │      │
│  │              │  │               │  │              │      │
│  │ - 登录验证   │  │ - 令牌生成   │  │ - LDAP认证   │      │
│  │ - 用户管理   │  │ - 令牌验证   │  │ - 用户信息   │      │
│  │ - 角色分配   │  │ - 令牌撤销   │  │   获取       │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│                      Util 层                                 │
│  ┌──────────────┐  ┌──────────────┐                         │
│  │   JwtUtil    │  │   RSAUtil    │                         │
│  │              │  │              │                         │
│  │ - JWT 生成   │  │ - RSA 加解密 │                         │
│  │ - JWT 验证   │  │ - Nonce 管理 │                         │
│  │ - Claims提取 │  │ - 公钥提取   │                         │
│  └──────────────┘  └──────────────┘                         │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│                   Repository 层                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │UserRepository│  │TokenRepository│  │RoleRepository│      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│                   PostgreSQL 数据库                          │
│  - t_user (用户表)                                           │
│  - t_token (令牌表)                                          │
│  - t_role (角色表)                                           │
│  - t_user_role (用户角色关联表)                              │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                   外部 LDAP 服务器                           │
│  - 用户认证                                                  │
│  - 用户信息查询                                              │
│  - AD 组查询                                                 │
└─────────────────────────────────────────────────────────────┘
```

### 2.2 分层职责

#### 2.2.1 Controller 层
- **职责**：处理 HTTP 请求，参数验证，响应封装
- **核心类**：`LoginController`
- **主要接口**：
  - `GET /api/auth/publicKey` - 获取 RSA 公钥
  - `POST /api/auth/generateNonce` - 生成 nonce
  - `POST /api/auth/login` - 用户登录
  - `POST /api/auth/logout` - 用户登出
  - `POST /api/auth/refresh-token` - 刷新令牌
  - `GET /api/auth/validate-token` - 验证令牌

#### 2.2.2 Service 层
- **职责**：业务逻辑处理，事务管理
- **核心类**：
  - `LoginService`：登录业务逻辑
  - `JwtTokenService`：JWT 令牌管理
  - `LdapService`：LDAP 集成

#### 2.2.3 Util 层
- **职责**：工具类，加密解密，令牌操作
- **核心类**：
  - `JwtUtil`：JWT 令牌生成和验证
  - `RSAUtil`：RSA 加密解密

#### 2.2.4 Repository 层
- **职责**：数据访问，数据库操作
- **核心类**：
  - `UserRepository`：用户数据访问
  - `TokenRepository`：令牌数据访问
  - `RoleRepository`：角色数据访问
  - `UserRoleRepository`：用户角色关联

---

## 3. 技术选型

### 3.1 核心技术栈

| 技术 | 版本/库 | 用途 |
|------|---------|------|
| Spring Boot | 3.x | 应用框架 |
| Spring Security LDAP | - | LDAP 集成 |
| Nimbus JOSE + JWT | - | JWT 令牌处理 |
| PostgreSQL | - | 数据持久化 |
| Lombok | - | 代码简化 |
| Swagger/OpenAPI | 3.x | API 文档 |

### 3.2 JWT 库选择

**选择 Nimbus JOSE + JWT 的原因**：
1. **功能完整**：支持 JWS、JWE、JWK 等完整的 JOSE 规范
2. **算法丰富**：支持 RSA、HMAC、ECDSA 等多种签名算法
3. **性能优秀**：高效的 JWT 处理性能
4. **社区活跃**：持续维护和更新
5. **类型安全**：强类型 API，减少错误

### 3.3 加密算法选择

#### 3.3.1 密码传输加密
- **算法**：RSA-2048
- **用途**：前端加密密码，后端解密
- **优势**：非对称加密，公钥可公开

#### 3.3.2 JWT 签名算法
支持两类算法：

**RSA 算法（推荐生产环境）**：
- RS256 (RSA with SHA-256)
- RS384 (RSA with SHA-384)
- RS512 (RSA with SHA-512)
- **优势**：私钥签名，公钥验证，密钥分离

**HMAC 算法（适用简单场景）**：
- HS256 (HMAC with SHA-256)
- HS384 (HMAC with SHA-384)
- HS512 (HMAC with SHA-512)
- **优势**：简单高效，单密钥签名验证

---

## 4. 安全设计

### 4.1 密码安全

#### 4.1.1 传输加密
```
前端流程：
1. 获取 RSA 公钥 (modulus, exponent)
2. 生成 nonce（防重放攻击）
3. 使用公钥加密：RSA(password + nonce)
4. 发送加密后的密码

后端流程：
1. 使用 RSA 私钥解密
2. 验证 nonce 有效性（时间窗口：5分钟）
3. 提取原始密码
4. 进行 LDAP 认证
```

#### 4.1.2 Nonce 防重放攻击
- **生成**：UUID + 时间戳
- **存储**：内存缓存（ConcurrentHashMap）
- **过期**：5 分钟自动清理
- **验证**：一次性使用，验证后立即删除

### 4.2 JWT 令牌安全

#### 4.2.1 双令牌机制

**访问令牌（Access Token）**：
- **有效期**：10 分钟（可配置）
- **用途**：API 访问授权
- **存储**：前端内存（不持久化）

**刷新令牌（Refresh Token）**：
- **有效期**：10 分钟（可配置，通常更长）
- **用途**：获取新的访问令牌
- **存储**：前端安全存储（HttpOnly Cookie 或 Secure Storage）

#### 4.2.2 Opaque Token

除了 JWT 令牌，系统还生成 Opaque Token：
- **生成**：24 字节随机 Base64 字符串
- **用途**：
  - 数据库查询和索引
  - 令牌撤销管理
  - 减少 JWT 暴露
- **关联**：与 JWT 令牌一一对应存储

#### 4.2.3 令牌撤销机制

```java
// 登出时撤销所有令牌
tokenRepository.revokeAllUserTokens(userId, LocalDateTime.now());

// 数据库更新
UPDATE t_token 
SET revoked = true, 
    update_date = NOW() 
WHERE user_id = ? AND revoked = false
```

#### 4.2.4 JWT Claims 设计

```json
{
  "sub": "API",                    // 令牌类型：API 或 REFRESH
  "aud": "OCBC",                   // 受众
  "iss": "HR",                     // 签发者
  "iat": 1702540800,               // 签发时间
  "nbf": 1702540800,               // 生效时间
  "exp": 1702541400,               // 过期时间
  "unique_name": "john.doe",       // LAN ID
  "user_id": "uuid-string"         // 用户 ID
}
```

### 4.3 LDAP 安全

#### 4.3.1 认证流程
```java
// 1. 创建认证令牌
UsernamePasswordAuthenticationToken authentication = 
    new UsernamePasswordAuthenticationToken(lanId, password);

// 2. LDAP 认证
authentication = ldapAuthProvider.authenticate(authentication);

// 3. 获取用户详情
LdapUserDetails userDetails = (LdapUserDetails) authentication.getPrincipal();
```

#### 4.3.2 用户信息同步
- **首次登录**：自动创建本地用户
- **后续登录**：更新用户信息（姓名、邮箱）
- **角色分配**：根据部门自动分配角色

### 4.4 配置安全

#### 4.4.1 敏感信息管理
```properties
# 私钥不应硬编码，应使用环境变量或密钥管理服务
jwt.private-key-pem=${JWT_PRIVATE_KEY}
jwt.public-key-pem=${JWT_PUBLIC_KEY}
jwt.secret=${JWT_SECRET}

# LDAP 配置
spring.ldap.urls=${LDAP_URL}
spring.ldap.username=${LDAP_USERNAME}
spring.ldap.password=${LDAP_PASSWORD}
```

#### 4.4.2 开发环境配置
```properties
# Mock 模式（仅开发环境）
login.mock.enabled=true
encryption.rsa-enabled=false

# 公钥提取（仅开发环境）
app.dev.extract-key-enabled=true
```

---

## 5. 数据模型设计

### 5.1 用户表 (t_user)

```sql
CREATE TABLE t_user (
    id VARCHAR(255) PRIMARY KEY,           -- UUID
    lan_id VARCHAR(255) NOT NULL UNIQUE,   -- LAN ID（工号）
    user_name VARCHAR(255),                -- 用户名
    normalized_user_name VARCHAR(255),     -- 规范化用户名（大写）
    email VARCHAR(255),                    -- 邮箱
    normalized_email VARCHAR(255),         -- 规范化邮箱（大写）
    display_name VARCHAR(255),             -- 显示名称
    is_active BOOLEAN DEFAULT true,        -- 是否激活
    created_time TIMESTAMP,                -- 创建时间
    updated_time TIMESTAMP                 -- 更新时间
);

CREATE INDEX idx_user_lan_id ON t_user(lan_id);
CREATE INDEX idx_user_email ON t_user(normalized_email);
```

### 5.2 令牌表 (t_token)

```sql
CREATE TABLE t_token (
    id UUID PRIMARY KEY,                   -- 令牌 ID
    user_id VARCHAR(255) NOT NULL,         -- 用户 ID
    op_acc_token VARCHAR(255) NOT NULL,    -- Opaque 访问令牌
    op_ref_token VARCHAR(255) NOT NULL,    -- Opaque 刷新令牌
    acc_token TEXT NOT NULL,               -- JWT 访问令牌
    ref_token TEXT NOT NULL,               -- JWT 刷新令牌
    exp_time TIMESTAMP NOT NULL,           -- 过期时间
    revoked BOOLEAN DEFAULT false,         -- 是否已撤销
    created_by VARCHAR(255),               -- 创建人
    created_time TIMESTAMP,                -- 创建时间
    updated_time TIMESTAMP,                -- 更新时间
    FOREIGN KEY (user_id) REFERENCES t_user(id)
);

CREATE INDEX idx_token_user_id ON t_token(user_id);
CREATE INDEX idx_token_op_acc ON t_token(op_acc_token);
CREATE INDEX idx_token_op_ref ON t_token(op_ref_token);
CREATE INDEX idx_token_exp_time ON t_token(exp_time);
```

### 5.3 角色表 (t_role)

```sql
CREATE TABLE t_role (
    id VARCHAR(255) PRIMARY KEY,           -- 角色 ID
    name VARCHAR(255) NOT NULL,            -- 角色名称
    normalized_name VARCHAR(255) UNIQUE,   -- 规范化名称（大写）
    created_time TIMESTAMP,                -- 创建时间
    updated_time TIMESTAMP                 -- 更新时间
);

-- 预置角色
INSERT INTO t_role (id, name, normalized_name) VALUES
('hr-admin-id', 'HR_Admin', 'HR_ADMIN'),
('employee-id', 'Employee', 'EMPLOYEE');
```

### 5.4 用户角色关联表 (t_user_role)

```sql
CREATE TABLE t_user_role (
    user_id VARCHAR(255) NOT NULL,         -- 用户 ID
    role_id VARCHAR(255) NOT NULL,         -- 角色 ID
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES t_user(id),
    FOREIGN KEY (role_id) REFERENCES t_role(id)
);
```

---

## 6. 业务流程设计

### 6.1 完整登录流程

```
┌─────────┐                                    ┌─────────┐
│  前端   │                                    │  后端   │
└────┬────┘                                    └────┬────┘
     │                                              │
     │ 1. GET /api/auth/publicKey                  │
     │─────────────────────────────────────────────>│
     │                                              │
     │ 2. PublicKeyResponse (modulus, exponent)    │
     │<─────────────────────────────────────────────│
     │                                              │
     │ 3. POST /api/auth/generateNonce             │
     │    { username: "john.doe" }                 │
     │─────────────────────────────────────────────>│
     │                                              │
     │ 4. NonceResponse (nonce, expiresAt)         │
     │<─────────────────────────────────────────────│
     │                                              │
     │ [前端：使用公钥加密密码]                     │
     │ encryptedPassword = RSA(password + nonce)   │
     │                                              │
     │ 5. POST /api/auth/login                     │
     │    { username, encryptedPassword, nonce }   │
     │─────────────────────────────────────────────>│
     │                                              │
     │                                   [RSA 解密密码]
     │                                   [LDAP 认证]
     │                                   [创建/更新用户]
     │                                   [分配角色]
     │                                   [生成 JWT]
     │                                   [保存令牌]
     │                                              │
     │ 6. LoginResponse                            │
     │    Headers: x-acc-op, x-ref-token           │
     │    Body: { tokenType, expiresIn }           │
     │<─────────────────────────────────────────────│
     │                                              │
     │ [前端：存储令牌]                             │
     │                                              │
```

### 6.2 角色分配逻辑

```java
// 根据 LDAP 部门信息自动分配角色
String department = ldapUserInfo.getDepartment();

if ("CHN E2P Human Resources".equals(department)) {
    // HR 部门 → HR_Admin 角色
    assignRole(user, "HR_ADMIN");
} else {
    // 其他部门 → Employee 角色
    assignRole(user, "EMPLOYEE");
}
```

### 6.3 令牌刷新流程

```
┌─────────┐                                    ┌─────────┐
│  前端   │                                    │  后端   │
└────┬────┘                                    └────┬────┘
     │                                              │
     │ [访问令牌即将过期]                           │
     │                                              │
     │ POST /api/auth/refresh-token                │
     │ { refreshToken: "opaque-refresh-token" }    │
     │─────────────────────────────────────────────>│
     │                                              │
     │                                   [验证刷新令牌]
     │                                   [检查是否撤销]
     │                                   [撤销旧令牌]
     │                                   [生成新令牌]
     │                                              │
     │ TokenInfo (新的访问令牌和刷新令牌)           │
     │<─────────────────────────────────────────────│
     │                                              │
```

---

## 7. 配置管理

### 7.1 JWT 配置

```properties
# JWT 签名算法（RS256 或 HS256）
jwt.algorithm=RS256

# JWT 签发者和受众
jwt.issuer=HR
jwt.audience=OCBC

# 令牌有效期（秒）
jwt.access-token.expiration=600      # 10 分钟
jwt.refresh-token.expiration=86400   # 24 小时

# RSA 密钥（生产环境使用环境变量）
jwt.private-key-pem=${JWT_PRIVATE_KEY}
jwt.public-key-pem=${JWT_PUBLIC_KEY}

# HMAC 密钥（至少 32 字节）
jwt.secret=${JWT_SECRET}
```

### 7.2 加密配置

```properties
# RSA 加密开关
encryption.rsa-enabled=true

# RSA 公钥（用于前端加密）
encryption.rsa-public-modulus=${RSA_PUBLIC_MODULUS}
encryption.rsa-public-exponent=${RSA_PUBLIC_EXPONENT}

# RSA 私钥（用于后端解密）
encryption.rsa-private-key=${RSA_PRIVATE_KEY}

# Nonce 过期时间（分钟）
encryption.nonce-expiration-minutes=5

# Opaque Token 长度（字节）
jwt.opaque-code-length=24
```

### 7.3 LDAP 配置

```properties
# LDAP 服务器地址
spring.ldap.urls=ldap://ldap.example.com:389

# LDAP 基础 DN
spring.ldap.base=dc=example,dc=com

# LDAP 管理员账号
spring.ldap.username=cn=admin,dc=example,dc=com
spring.ldap.password=${LDAP_PASSWORD}

# 用户搜索过滤器
ldap.user-search-filter=(uid={0})
ldap.user-search-base=ou=users

# 是否启用 LDAP 认证
login.ldap.enabled=true
```

### 7.4 应用配置

```properties
# Mock 登录开关（仅开发环境）
login.mock.enabled=false

# HR 部门名称（用于角色分配）
user.role.hr-department=CHN E2P Human Resources

# 开发工具开关
app.dev.extract-key-enabled=false
```

---

## 8. API 设计规范

### 8.1 统一响应格式

```json
{
  "code": 0,
  "message": "OK",
  "data": {
    // 具体数据
  }
}
```

**状态码约定**：
- `0`：成功
- `400`：请求参数错误
- `401`：未授权（令牌无效或过期）
- `403`：禁止访问（权限不足）
- `500`：服务器内部错误

### 8.2 登录响应设计

**Headers**：
```
x-acc-op: <opaque-access-token>
x-ref-token: <opaque-refresh-token>
```

**Body**：
```json
{
  "tokenType": "Bearer",
  "expiresIn": "600"
}
```

**设计理由**：
1. **安全性**：敏感令牌放在 Header 中，不在 Body 中暴露
2. **标准化**：符合 OAuth 2.0 规范
3. **灵活性**：前端可自由选择存储方式

### 8.3 错误处理

```json
{
  "code": 401,
  "message": "LDAP认证失败：用户名或密码错误",
  "data": null
}
```

---

## 9. 性能优化

### 9.1 密钥缓存

```java
// RSA 密钥缓存，避免重复解析
private volatile PrivateKey cachedPrivateKey;
private volatile PublicKey cachedPublicKey;

private PrivateKey getPrivateKey() throws Exception {
    if (cachedPrivateKey != null) {
        return cachedPrivateKey;
    }
    synchronized (this) {
        if (cachedPrivateKey != null) {
            return cachedPrivateKey;
        }
        // 解析并缓存私钥
        cachedPrivateKey = parsePrivateKey(jwtPrivateKeyPem);
        return cachedPrivateKey;
    }
}
```

### 9.2 Nonce 清理策略

```java
// 定时清理过期 nonce（每分钟执行一次）
@Scheduled(fixedRate = 60000)
public void cleanExpiredNonces() {
    long now = System.currentTimeMillis();
    nonceCache.entrySet().removeIf(entry -> 
        now - entry.getValue() > nonceExpirationMillis
    );
}
```

### 9.3 令牌清理

```java
// 定时清理过期令牌（每天凌晨执行）
@Scheduled(cron = "0 0 0 * * ?")
public void cleanExpiredTokens() {
    tokenRepository.deleteExpiredTokens(LocalDateTime.now());
    log.info("清理过期令牌完成");
}
```

---

## 10. 测试策略

### 10.1 单元测试

**测试覆盖**：
- JwtUtil：令牌生成、验证、Claims 提取
- RSAUtil：加密解密、Nonce 生成验证
- LoginService：登录逻辑、用户创建、角色分配
- JwtTokenService：令牌管理、撤销机制

### 10.2 集成测试

**测试场景**：
- LDAP 认证流程
- 完整登录流程
- 令牌刷新流程
- 令牌撤销验证

### 10.3 Mock 模式

```java
// 开发环境跳过 RSA 解密
@RequestHeader(value = "login-mock", required = false) String loginMock

if (loginConfig.getMock().isEnabled() && "true".equals(loginMock)) {
    // 跳过 RSA 解密，直接使用明文密码
    skipRsaDecryption = true;
}
```

---

## 11. 部署建议

### 11.1 生产环境配置

```properties
# 使用 RSA 算法
jwt.algorithm=RS256

# 启用 RSA 加密
encryption.rsa-enabled=true

# 启用 LDAP 认证
login.ldap.enabled=true

# 禁用 Mock 模式
login.mock.enabled=false

# 禁用开发工具
app.dev.extract-key-enabled=false

# 合理的令牌有效期
jwt.access-token.expiration=900      # 15 分钟
jwt.refresh-token.expiration=604800  # 7 天
```

### 11.2 密钥管理

**推荐方案**：
1. **AWS Secrets Manager**：存储敏感密钥
2. **环境变量**：通过环境变量注入
3. **Kubernetes Secrets**：K8s 环境使用 Secrets

**密钥轮换**：
- 定期更新 JWT 签名密钥
- 更新时保留旧密钥一段时间（验证旧令牌）
- 通知用户重新登录

### 11.3 监控告警

**关键指标**：
- 登录成功率
- 登录失败次数（检测暴力破解）
- 令牌刷新频率
- LDAP 响应时间
- 过期令牌清理数量

---

## 12. 安全最佳实践

### 12.1 传输安全
- ✅ 强制使用 HTTPS
- ✅ 启用 HSTS (HTTP Strict Transport Security)
- ✅ 配置安全的 TLS 版本（TLS 1.2+）

### 12.2 令牌安全
- ✅ 短有效期访问令牌（10-15 分钟）
- ✅ 令牌撤销机制
- ✅ 不在 URL 中传递令牌
- ✅ 使用 HttpOnly Cookie 存储刷新令牌

### 12.3 密码安全
- ✅ RSA 加密传输
- ✅ Nonce 防重放攻击
- ✅ 不记录明文密码
- ✅ LDAP 认证（不存储密码）

### 12.4 防护措施
- ✅ 登录失败次数限制
- ✅ IP 白名单（可选）
- ✅ 异常登录检测
- ✅ 审计日志记录

---

## 13. 扩展性设计

### 13.1 多认证源支持

当前支持：
- LDAP 认证
- 本地数据库认证

未来可扩展：
- OAuth 2.0 / OpenID Connect
- SAML 2.0
- 多因素认证 (MFA)

### 13.2 权限模型扩展

当前：基于角色的访问控制 (RBAC)

未来可扩展：
- 基于属性的访问控制 (ABAC)
- 细粒度权限控制
- 动态权限分配

---

## 14. 附录

### 14.1 关键术语

| 术语 | 说明 |
|------|------|
| JWT | JSON Web Token，基于 JSON 的开放标准令牌 |
| JWS | JSON Web Signature，JWT 的签名规范 |
| LDAP | Lightweight Directory Access Protocol，轻量级目录访问协议 |
| Opaque Token | 不透明令牌，随机字符串，需查询服务器验证 |
| Nonce | Number used once，一次性随机数，防重放攻击 |
| RBAC | Role-Based Access Control，基于角色的访问控制 |

### 14.2 参考资料

- [RFC 7519 - JSON Web Token (JWT)](https://tools.ietf.org/html/rfc7519)
- [RFC 7515 - JSON Web Signature (JWS)](https://tools.ietf.org/html/rfc7515)
- [Spring Security Reference](https://docs.spring.io/spring-security/reference/)
- [Nimbus JOSE + JWT Documentation](https://connect2id.com/products/nimbus-jose-jwt)

### 14.3 变更历史

| 版本 | 日期 | 作者 | 变更内容 |
|------|------|------|----------|
| 1.0 | 2024-12-14 | System | 初始版本 |

---

**文档结束**
