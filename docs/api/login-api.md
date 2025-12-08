# JWT令牌认证 API 文档

## 基础信息

**Base URL**: `/api/auth`

**描述**: 用户认证、登录、登出、令牌管理相关接口

---

## JWT Token Payload 结构

JWT令牌包含以下标准和自定义声明：

```json
{
  "sub": "API",
  "aud": "OCBC",
  "unique_name": "AXXXXXXX",
  "nbf": 1764854416,
  "iss": "HR",
  "exp": 1764854416,
  "iat": 1764854416,
  "user_id": "550e8400-e29b-41d4-a716-446655440000"
}
```

**字段说明**:

| 字段名 | 类型 | 说明 |
|--------|------|------|
| sub | String | 主体，固定值 "API" |
| aud | String | 受众，固定值 "OCBC" |
| unique_name | String | 用户LAN账号，来自user表的lanId字段 |
| nbf | Long | 令牌生效时间（Unix时间戳） |
| iss | String | 签发者，固定值 "HR" |
| exp | Long | 令牌过期时间（Unix时间戳） |
| iat | Long | 令牌签发时间（Unix时间戳） |
| user_id | String | 用户ID，来自user表的id字段 |

---

## 接口列表

### 1. 用户登录

**接口**: `POST /api/auth/login`

**描述**: 用户使用用户名和密码进行登录认证，成功后返回JWT令牌和用户信息

**请求参数**:

```json
{
  "username": "AXXXXXXX",
  "password": "123456"
}
```

**参数说明**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| username | String | 是 | 用户名（LAN账号） |
| password | String | 是 | 密码 |

**响应示例**:

```json
{
  "code": 0,
  "message": "OK",
  "data": {
    "tokenInfo": {
      "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
      "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
      "tokenType": "Bearer",
      "accessTokenExpiry": "2025-12-05T10:30:00",
      "refreshTokenExpiry": "2025-12-06T09:30:00"
    },
    "userInfo": {
      "userId": "550e8400-e29b-41d4-a716-446655440000",
      "lanId": "AXXXXXXX",
      "userName": "Test Admin",
      "displayName": "Test Administrator",
      "email": "admin@ocbc.com",
      "roles": [
        {
          "roleId": 1,
          "roleName": "HR_ADMIN",
          "normalizedName": "hr_admin"
        }
      ]
    }
  }
}
```

**错误响应**:

```json
{
  "code": 401,
  "message": "用户名或密码错误",
  "data": null
}
```

---

### 2. 用户登出

**接口**: `POST /api/auth/logout`

**描述**: 撤销用户的所有有效令牌，实现安全登出

**请求头**:

```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**响应示例**:

```json
{
  "code": 0,
  "message": "登出成功",
  "data": null
}
```

**错误响应**:

```json
{
  "code": 400,
  "message": "令牌无效",
  "data": null
}
```

---

### 3. 刷新令牌

**接口**: `POST /api/auth/refresh-token`

**描述**: 使用刷新令牌获取新的访问令牌

**请求参数**:

```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**响应示例**:

```json
{
  "code": 0,
  "message": "OK",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "accessTokenExpiry": "2025-12-05T11:30:00",
    "refreshTokenExpiry": "2025-12-06T10:30:00"
  }
}
```

---

### 4. 验证令牌

**接口**: `GET /api/auth/validate-token`

**描述**: 验证JWT令牌是否有效

**请求头**:

```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**响应示例**:

```json
{
  "code": 0,
  "message": "OK",
  "data": {
    "valid": true,
    "message": "令牌有效"
  }
}
```

**错误响应**:

```json
{
  "code": 401,
  "message": "令牌无效",
  "data": {
    "valid": false,
    "message": "令牌已过期"
  }
}
```

---

## Mock用户数据

系统内置了以下测试用户数据：

| 用户名 | 密码 | 角色 | 说明 |
|--------|------|------|------|
| AXXXXXXX | 123456 或 AXXXXXXX 或 password | HR_ADMIN | 管理员用户 |
| BXXXXXXX | 123456 或 BXXXXXXX 或 password | HR_USER | HR用户 |
| CXXXXXXX | 123456 或 CXXXXXXX 或 password | EMPLOYEE | 普通员工 |

**Mock验证规则**:
1. 密码不能为空且长度至少6位
2. 支持三种密码：`123456`、用户名本身、`password`
3. 用户必须在数据库中存在且处于激活状态

---

## 数据库表结构

### 用户表 (users)
- 存储用户基本信息
- `lan_id`字段对应JWT中的`unique_name`
- `id`字段对应JWT中的`user_id`

### 角色表 (roles)
- 存储系统角色信息
- 预置三个角色：HR_ADMIN、HR_USER、EMPLOYEE

### 用户角色关联表 (user_roles)
- 存储用户与角色的多对多关系
- 支持一个用户拥有多个角色

### 令牌表 (tokens)
- 存储JWT令牌信息
- 支持令牌撤销和过期管理
- 使用分区表提高查询性能

---

## 统一响应格式

所有接口都使用统一的响应格式：

```json
{
  "code": 0,
  "message": "OK",
  "data": {}
}
```

**字段说明**:

| 字段 | 类型 | 说明 |
|------|------|------|
| code | Integer | 状态码，0表示成功，非0表示失败 |
| message | String | 提示信息 |
| data | Object | 数据载体 |

---

## 错误码

| 错误码 | 说明 |
|--------|------|
| 0 | 成功 |
| 400 | 请求参数错误 |
| 401 | 认证失败（用户名密码错误、令牌无效等） |
| 403 | 权限不足 |
| 500 | 服务器内部错误 |

---

## 使用示例

### 场景1：用户登录流程

1. **发送登录请求**
   ```bash
   curl -X POST http://localhost:8080/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{"username": "AXXXXXXX", "password": "123456"}'
   ```

2. **获取令牌和用户信息**
   ```json
   {
     "code": 0,
     "data": {
       "tokenInfo": { "accessToken": "...", "refreshToken": "..." },
       "userInfo": { "userId": "...", "lanId": "AXXXXXXX", "roles": [...] }
     }
   }
   ```

3. **使用访问令牌访问受保护的接口**
   ```bash
   curl -X GET http://localhost:8080/api/protected-endpoint \
     -H "Authorization: Bearer <accessToken>"
   ```

### 场景2：令牌刷新流程

1. **当访问令牌即将过期时，使用刷新令牌获取新令牌**
   ```bash
   curl -X POST http://localhost:8080/api/auth/refresh-token \
     -H "Content-Type: application/json" \
     -d '{"refreshToken": "<refreshToken>"}'
   ```

2. **获取新的令牌对**
   ```json
   {
     "code": 0,
     "data": {
       "accessToken": "...",
       "refreshToken": "...",
       "tokenType": "Bearer",
       "accessTokenExpiry": "...",
       "refreshTokenExpiry": "..."
     }
   }
   ```

### 场景3：安全登出流程

1. **发送登出请求（撤销所有令牌）**
   ```bash
   curl -X POST http://localhost:8080/api/auth/logout \
     -H "Authorization: Bearer <accessToken>"
   ```

2. **确认登出成功**
   ```json
   {
     "code": 0,
     "message": "登出成功",
     "data": null
   }
   ```

---

## 注意事项

1. **令牌安全**：
   - 访问令牌具有较短的有效期（默认1小时）
   - 刷新令牌具有较长的有效期（默认24小时）
   - 所有令牌都存储在数据库中，支持撤销

2. **Mock实现**：
   - 当前密码验证是Mock实现，实际项目中需要集成LDAP/AD
   - 用户数据需要预先在数据库中创建

3. **数据库依赖**：
   - 需要先运行数据库迁移脚本创建表结构
   - 需要插入测试数据才能正常登录

4. **JWT依赖**：
   - 需要在pom.xml中添加相关JWT库依赖
   - 需要配置JWT密钥和过期时间

---

## 更新日志

- **2025-12-04**: 初始版本
  - 实现用户登录、登出、令牌刷新功能
  - 支持JWT令牌生成和验证
  - 实现用户角色级联查询
  - 添加Mock用户验证逻辑
  - 创建完整的数据库表结构
