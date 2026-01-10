# EasyCare Service - 登录与认证调用图

## 概述
本文档描述了 EasyCare Service 项目中登录和认证功能的完整调用流程。

---

## 1. 登录流程 (Login Flow)

### 1.1 主要流程图

```
客户端
  │
  ├─→ GET /api/auth/publicKey
  │     └─→ LoginController.getPublicKey()
  │           └─→ RSAUtil.getPublicKey()
  │                 └─→ 返回 PublicKeyResponse (modulus, exponent)
  │
  ├─→ POST /api/auth/generateNonce
  │     └─→ LoginController.generateNonce()
  │           └─→ RSAUtil.generateNonce(username)
  │                 └─→ 返回 NonceResponse (nonce, expiresAt, expiresIn)
  │
  └─→ POST /api/auth/login
        └─→ LoginController.login(LoginRequest, login-mock header)
              │
              ├─→ LoginService.login(loginRequest, skipRsaDecryption)
              │     │
              │     ├─→ [如果 RSA 启用] RSAUtil.decryptLogin(loginRequest)
              │     │     └─→ 解密密码
              │     │
              │     ├─→ [如果 LDAP 启用] LdapService.validateUserAndPassword(username, password)
              │     │     └─→ LdapAuthProvider.authenticate()
              │     │           └─→ 返回 Boolean (认证结果)
              │     │
              │     ├─→ [如果 LDAP 启用] LdapService.getUserInfo(username, mapper)
              │     │     └─→ 返回 List<LdapUserInfo>
              │     │
              │     ├─→ LoginServiceImpl.loadAndValidateUser(username, ldapUserInfo)
              │     │     │
              │     │     ├─→ [如果 LDAP 启用] findOrCreateUserFromLdap(lanId, ldapUserInfo)
              │     │     │     │
              │     │     │     ├─→ UserRepository.findByLanIdWithRoles(lanId)
              │     │     │     │
              │     │     │     ├─→ [如果用户不存在] createUserFromLdap(lanId, ldapUserInfo)
              │     │     │     │     ├─→ UserRepository.save(newUser)
              │     │     │     │     ├─→ assignRoleBasedOnDepartment(user, department)
              │     │     │     │     │     ├─→ RoleRepository.findByNormalizedName(roleName)
              │     │     │     │     │     └─→ UserRoleRepository.save(userRole)
              │     │     │     │     └─→ UserRepository.findByLanIdWithRoles(lanId)
              │     │     │     │
              │     │     │     └─→ [如果用户存在] updateUserFromLdap(user, ldapUserInfo)
              │     │     │           └─→ UserRepository.save(user)
              │     │     │
              │     │     ├─→ [如果 LDAP 未启用] UserRepository.findByLanIdWithRoles(username)
              │     │     │
              │     │     └─→ validateUserActive(user)
              │     │
              │     └─→ LoginServiceImpl.buildLoginResponse(user)
              │           │
              │           ├─→ JwtTokenService.generateAndSaveToken(user)
              │           │     │
              │           │     ├─→ JwtTokenService.revokeAllUserTokens(userId)
              │           │     │     └─→ TokenRepository.revokeAllUserTokens(userId, now)
              │           │     │
              │           │     ├─→ JwtUtil.generateAccessToken(lanId, userId, issuer, audience)
              │           │     │     ├─→ createClaims() - 创建 JWT Claims
              │           │     │     ├─→ createSigner() - 创建签名器 (RSA/HMAC)
              │           │     │     └─→ SignedJWT.sign() - 签名并序列化
              │           │     │
              │           │     ├─→ JwtUtil.generateRefreshToken(lanId, userId, issuer, audience)
              │           │     │     ├─→ createClaims() - 创建 JWT Claims
              │           │     │     ├─→ createSigner() - 创建签名器 (RSA/HMAC)
              │           │     │     └─→ SignedJWT.sign() - 签名并序列化
              │           │     │
              │           │     ├─→ RandomGeneratorUtil.generateRandomBase64String() - 生成 opaque token
              │           │     │
              │           │     └─→ TokenRepository.save(token)
              │           │           └─→ 返回 TokenInfo
              │           │
              │           └─→ buildUserInfo(user)
              │                 └─→ 返回 UserInfo (含角色信息)
              │
              └─→ 返回 LoginSimpleTokenResponse
                    └─→ Headers: x-acc-op, x-ref-token
```

---

## 2. 登出流程 (Logout Flow)

```
客户端
  │
  └─→ POST /api/auth/logout
        └─→ LoginController.logout(Authorization header)
              │
              ├─→ extractTokenFromAuthorization(authorization)
              │
              └─→ LoginService.logout(token)
                    │
                    ├─→ JwtTokenService.getUserFromToken(token)
                    │     ├─→ JwtUtil.validateToken(token)
                    │     │     └─→ parseAndValidate() - 验证签名和过期时间
                    │     ├─→ JwtUtil.getUserIdFromToken(token)
                    │     └─→ UserRepository.findById(userId)
                    │
                    └─→ JwtTokenService.revokeAllUserTokens(userId)
                          └─→ TokenRepository.revokeAllUserTokens(userId, now)
```

---

## 3. 令牌刷新流程 (Refresh Token Flow)

```
客户端
  │
  └─→ POST /api/auth/refresh-token
        └─→ LoginController.refreshToken(RefreshTokenRequest)
              │
              └─→ LoginService.refreshToken(refreshToken)
                    │
                    └─→ JwtTokenService.refreshToken(refreshToken)
                          │
                          ├─→ JwtUtil.validateToken(refreshToken)
                          │     └─→ parseAndValidate() - 验证签名和过期时间
                          │
                          ├─→ JwtUtil.getUserIdFromToken(refreshToken)
                          │
                          ├─→ UserRepository.findById(userId)
                          │
                          ├─→ TokenRepository.findByOpRefTokenAndRevokedFalse(refreshToken)
                          │
                          └─→ JwtTokenService.generateAndSaveToken(user)
                                └─→ [参见登录流程中的 generateAndSaveToken]
```

---

## 4. 令牌验证流程 (Token Validation Flow)

```
客户端
  │
  └─→ GET /api/auth/validate-token
        └─→ LoginController.validateToken(Authorization header)
              │
              ├─→ extractTokenFromAuthorization(authorization)
              │
              └─→ 返回 TokenValidationResponse
```

---

## 5. 核心组件说明

### 5.1 Controller 层
- **LoginController** (`/api/auth`)
  - `GET /publicKey` - 获取 RSA 公钥
  - `POST /generateNonce` - 生成防重放攻击的 nonce
  - `POST /login` - 用户登录
  - `POST /logout` - 用户登出
  - `POST /refresh-token` - 刷新令牌
  - `GET /validate-token` - 验证令牌
  - `GET /dev/extract-public-key` - 开发环境提取公钥

### 5.2 Service 层
- **LoginService / LoginServiceImpl**
  - `login()` - 处理登录逻辑
  - `logout()` - 处理登出逻辑
  - `refreshToken()` - 刷新令牌
  - `validateUserCredentials()` - 验证用户凭据

- **JwtTokenService / JwtTokenServiceImpl**
  - `generateAndSaveToken()` - 生成并保存 JWT 令牌
  - `validateToken()` - 验证令牌
  - `revokeAllUserTokens()` - 撤销用户所有令牌
  - `refreshToken()` - 刷新令牌
  - `getUserFromToken()` - 从令牌获取用户信息
  - `cleanExpiredTokens()` - 清理过期令牌

- **LdapService / LdapServiceImpl**
  - `validateUserAndPassword()` - LDAP 用户密码验证
  - `getUserInfo()` - 获取 LDAP 用户信息
  - `searchAdGroup()` - 搜索 AD 组

### 5.3 Util 层
- **JwtUtil**
  - `generateAccessToken()` - 生成访问令牌
  - `generateRefreshToken()` - 生成刷新令牌
  - `validateToken()` - 验证令牌
  - `getClaimsFromToken()` - 从令牌获取声明
  - `getLanIdFromToken()` - 从令牌获取 LAN ID
  - `getUserIdFromToken()` - 从令牌获取用户 ID
  - `isTokenExpired()` - 检查令牌是否过期

- **RSAUtil**
  - `getPublicKey()` - 获取公钥
  - `generateNonce()` - 生成 nonce
  - `decryptLogin()` - 解密登录信息
  - `extractPublicKeyFromPrivateKey()` - 从私钥提取公钥

### 5.4 Repository 层
- **UserRepository**
  - `findByLanIdWithRoles()` - 根据 LAN ID 查找用户（含角色）
  - `findByLanIdAndIsActiveTrue()` - 查找激活用户
  - `save()` - 保存用户

- **TokenRepository**
  - `save()` - 保存令牌
  - `revokeAllUserTokens()` - 撤销用户所有令牌
  - `findByOpRefTokenAndRevokedFalse()` - 查找有效刷新令牌
  - `deleteExpiredTokens()` - 删除过期令牌

- **RoleRepository**
  - `findByNormalizedName()` - 根据规范化名称查找角色

- **UserRoleRepository**
  - `save()` - 保存用户角色关联

### 5.5 配置类
- **LoginConfigurationProperties**
  - `ldap.enabled` - LDAP 认证开关
  - `mock.enabled` - Mock 登录开关

---

## 6. 认证模式

### 6.1 LDAP 认证模式
当 `login.ldap.enabled=true` 时：
1. 使用 LDAP 验证用户名和密码
2. 从 LDAP 获取用户信息（姓名、邮箱、部门）
3. 自动创建或更新本地用户
4. 根据部门分配角色（HR 部门 → HR_Admin，其他 → Employee）

### 6.2 本地认证模式
当 `login.ldap.enabled=false` 时：
1. 从本地数据库查找用户
2. 验证用户是否激活
3. 生成 JWT 令牌

### 6.3 Mock 模式
当 `login.mock.enabled=true` 且请求头 `login-mock=true` 时：
- 跳过 RSA 解密步骤
- 用于开发和测试环境

---

## 7. 安全机制

### 7.1 密码加密
- 前端使用 RSA 公钥加密密码
- 后端使用 RSA 私钥解密密码
- 支持 nonce 防重放攻击

### 7.2 JWT 令牌
- 支持 RSA (RS256/RS384/RS512) 和 HMAC (HS256/HS384/HS512) 算法
- 访问令牌和刷新令牌分离
- 令牌包含用户 ID、LAN ID、签发者、受众等信息
- 支持令牌撤销机制

### 7.3 Opaque Token
- 生成随机 Base64 字符串作为 opaque token
- 用于数据库查询和令牌管理
- 与 JWT 令牌关联存储

---

## 8. 数据库实体

### 8.1 User (用户表)
- `id` - 用户 ID (UUID)
- `lan_id` - LAN ID
- `user_name` - 用户名
- `display_name` - 显示名称
- `email` - 邮箱
- `is_active` - 是否激活

### 8.2 Token (令牌表)
- `id` - 令牌 ID (UUID)
- `user_id` - 用户 ID
- `op_acc_token` - Opaque 访问令牌
- `op_ref_token` - Opaque 刷新令牌
- `acc_token` - JWT 访问令牌
- `ref_token` - JWT 刷新令牌
- `exp_time` - 过期时间
- `revoked` - 是否已撤销

### 8.3 Role (角色表)
- `id` - 角色 ID
- `name` - 角色名称
- `normalized_name` - 规范化名称

### 8.4 UserRole (用户角色关联表)
- `user_id` - 用户 ID
- `role_id` - 角色 ID

---

## 9. API 响应格式

所有 API 统一使用 `ApiResponse` 结构：

```json
{
  "code": 0,
  "message": "OK",
  "data": {
    // 具体数据
  }
}
```

登录成功响应：
- **Headers**: 
  - `x-acc-op`: Opaque 访问令牌
  - `x-ref-token`: Opaque 刷新令牌
- **Body**:
  ```json
  {
    "tokenType": "Bearer",
    "expiresIn": "600"
  }
  ```

---

## 10. 配置参数

### JWT 配置
- `jwt.secret` - HMAC 密钥
- `jwt.access-token.expiration` - 访问令牌过期时间（秒）
- `jwt.refresh-token.expiration` - 刷新令牌过期时间（秒）
- `jwt.algorithm` - 签名算法 (RS256/HS256 等)
- `jwt.issuer` - 签发者
- `jwt.audience` - 受众
- `jwt.private-key-pem` - RSA 私钥
- `jwt.public-key-pem` - RSA 公钥

### 加密配置
- `encryption.rsa-enabled` - RSA 加密开关
- `encryption.nonce-expiration-minutes` - Nonce 过期时间（分钟）

### 登录配置
- `login.ldap.enabled` - LDAP 认证开关
- `login.mock.enabled` - Mock 登录开关

### 用户配置
- `user.role.hr-department` - HR 部门名称

---

## 生成日期
2024-12-14
