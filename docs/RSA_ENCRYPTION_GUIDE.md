# RSA加密登录实现说明

## 概述

本系统实现了基于RSA非对称加密的登录机制，结合nonce防重放攻击策略，确保登录过程的安全性。

## 核心组件

### 1. RSAUtil 工具类

位置：`util.com.ocbc.ms.easy.care.RSAUtil`

主要方法：
- `decryptLogin(LoginRequest)` - 解密登录请求中的密码
- `validateNonce(String userId, String nonce)` - 验证nonce有效性
- `cleanupExpiredNonces()` - 清理过期的nonce记录
- `cleanupUsedNonces(int daysToKeep)` - 清理已使用的nonce记录

### 2. Nonce实体

位置：`entity.com.ocbc.ms.easy.care.Nonce`

字段说明：
- `id` - 主键ID
- `nonceValue` - nonce值（唯一）
- `userId` - 用户ID
- `used` - 是否已使用
- `createdAt` - 创建时间
- `usedAt` - 使用时间
- `expiresAt` - 过期时间

### 3. LoginRequest DTO

新增字段：
- `nonce` - 服务端生成的随机字符串

### 4. API接口

#### GET /api/auth/publicKey
获取RSA公钥接口

响应：
```json
{
  "code": 0,
  "message": "OK",
  "data": {
    "modulus": "tx2qZ8G2mXv0LxF9yJ3L5bY0kC2lvb5x0o3WzYvPpX1xCw0GZp6zX0oKZ6k6YV2p7xQ9cTqYJlvPZP7hJcL4s5p7gq1g4Gq7uVQjdlXeX0cS9p2b3w+7pL2oY1wK1t2wM2sgUrI1/nFXu5lC8nwFpi7jB8V7M3J6O1cLxZ5xQUu9cN1a40F9qP4iQyZ7my3nV9tL2qK0xM5J7H0f1bOqJ3dM8uQ2L5sV7iK6qL8cH2oT7nD3wN1pV5qU8eK2qZ4sw1vP9wE6nB2uM0qU9rL7bV3xF7uH2pQ9tL1oM3J5nLyxPS8vbE2goLTA",
    "exponent": "AQAB"
  }
}
```

**字段说明：**
- `modulus` - RSA模数（Base64编码）
- `exponent` - RSA指数（Base64编码，通常为 "AQAB"，对应十进制65537）

**重要提示：** 出于安全考虑，padding 和 keySize 信息不在API中返回，请联系后端团队获取加密参数配置。

**RSA加密参数（需要前端配置）：**
- **填充方式**：OAEP with SHA-256 and MGF1 padding
- **密钥长度**：2048位
- **算法**：RSA
- **哈希算法**：SHA-256

**前端加密库支持：**
- **node-rsa**：`encryptionScheme: "pkcs1_oaep"` ✅ 推荐
- **node-forge**：`forge.pki.rsa.encrypt(data, publicKey, 'RSA-OAEP', {md: forge.md.sha256.create()})`
- **Web Crypto API**：指定 `{name: "RSA-OAEP", hash: "SHA-256"}`
- **JSEncrypt**：不支持 OAEP，请使用其他库

**注意：** 必须使用 OAEP padding with SHA-256，不能使用 PKCS#1 v1.5，否则后端无法解密。

#### POST /api/auth/generateNonce
生成nonce接口

请求体：
```json
{
  "userId": "A5132253"
}
```

响应：
```json
{
  "code": 0,
  "message": "OK",
  "data": {
    "nonce": "a1b2c3d4e5f6a7b8c9d0",
    "expiresAt": 1702123456789,
    "expiresIn": 300
  }
}
```

**字段说明：**
- `nonce` - 十六进制格式的随机字符串（10字节 = 20个十六进制字符）
- `expiresAt` - 过期时间戳（毫秒）
- `expiresIn` - 有效期（秒）

**Nonce生成机制：**
1. 使用 `SecureRandom` 生成指定长度的随机字节数组（默认10字节）
2. 将字节数组转换为十六进制字符串（每字节转换为2个十六进制字符）
3. Nonce与userId绑定，存储在数据库中
4. 每个用户同时只能有一个未使用的有效nonce
5. 字节长度可通过 `encryption.nonce-byte-length` 配置（推荐值：10、15、20）

**示例：**
- 10字节 → 20个十六进制字符：`a1b2c3d4e5f6a7b8c9d0`
- 15字节 → 30个十六进制字符：`a1b2c3d4e5f6a7b8c9d0e1f2a3b4c5`
- 20字节 → 40个十六进制字符：`a1b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6e7f8a9b0`

## 加密流程

### 第一步：获取公钥

客户端首先调用 `/api/auth/publicKey` 接口获取RSA公钥：

```javascript
// 1. 获取公钥
const publicKeyResponse = await fetch('/api/auth/publicKey');
const { data: publicKeyData } = await publicKeyResponse.json();
// publicKeyData = { 
//   modulus: "tx2qZ8G2..." (Base64), 
//   exponent: "AQAB" (Base64)
// }

// 使用 node-rsa 库（支持 OAEP padding）
import NodeRSA from 'node-rsa';

// 将 Base64 转换为 Hex 格式
const modulusHex = Buffer.from(publicKeyData.modulus, 'base64').toString('hex');
const exponentHex = Buffer.from(publicKeyData.exponent, 'base64').toString('hex');

// 创建 RSA 密钥
const key = new NodeRSA();
key.importKey({
  n: Buffer.from(modulusHex, 'hex'),
  e: parseInt(exponentHex, 16)
}, 'components-public');

// 设置 OAEP padding
key.setOptions({
  encryptionScheme: 'pkcs1_oaep'
});
```

### 第二步：获取Nonce

客户端调用 `/api/auth/generateNonce` 接口获取服务端生成的nonce：

```javascript
// 2. 请求nonce
const nonceResponse = await fetch('/api/auth/generateNonce', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ userId: 'A5132253' })
});

const { data: nonceData } = await nonceResponse.json();
// nonceData = { nonce: "a1b2c3d4e5f6a7b8c9d0", expiresAt: 1702123456789, expiresIn: 300 }
```

### 第三步：客户端加密

1. 使用服务端返回的nonce
2. 拼接密码和nonce：`<password><nonceSeparator><nonce>`
3. 使用服务器公钥对拼接后的字符串进行RSA加密
4. 发送加密后的密码和nonce到服务器

示例：
```javascript
// 3. 前端加密示例
const password = "myPassword123";
const nonce = nonceData.nonce; // 使用服务端返回的nonce
const separator = "XXX_Z123";
const plainText = password + separator + nonce;

// 使用 node-rsa 加密（OAEP padding）
const encryptedPassword = key.encrypt(plainText, 'base64');

// 4. 发送登录请求
const loginRequest = {
  username: "A5132253",
  password: encryptedPassword,
  nonce: nonce
};
```

### 服务端解密

1. 验证nonce是否存在且未被使用
2. 使用私钥解密密码
3. 从解密结果中分离密码和nonce
4. 验证nonce是否匹配
5. 标记nonce为已使用
6. 使用解密后的密码进行认证

## Nonce机制

### 作用
- 防止重放攻击（Replay Attack）
- 确保每次登录请求的唯一性

### 验证规则
1. nonce必须存在
2. nonce未被使用过（`used = false`）
3. nonce未过期（`expiresAt > now`）
4. 解密后的nonce必须与请求中的nonce匹配

### 生命周期
- 创建：首次验证时自动创建
- 使用：解密成功后标记为已使用
- 过期：默认5分钟后过期
- 清理：定时任务每10分钟清理过期和已使用的nonce

## 配置说明

### application.properties

```properties
# RSA Encryption Configuration
encryption.rsa-enabled=false
encryption.rsa-algorithm=RSA
encryption.rsa-transformation=RSA/ECB/OAEPWithSHA-256AndMGF1Padding
encryption.rsa-padding=OAEP

# Nonce Configuration
encryption.nonce-expiration-minutes=5
encryption.nonce-byte-length=10

# Nonce Cleanup Task
encryption.nonce-cleanup-cron=0 */10 * * * ?
```

**配置说明：**
- `rsa-algorithm` - RSA算法名称（默认：RSA）
- `rsa-transformation` - RSA转换方式，包含算法/模式/填充（默认：RSA/ECB/OAEPWithSHA-256AndMGF1Padding）
- `rsa-padding` - RSA填充方式标识（默认：OAEP）
- `nonce-byte-length` - Nonce字节长度，可配置为10、15、20等（默认：10）
- `nonce-cleanup-cron` - Nonce清理任务cron表达式（默认：每10分钟）

### application-dev.properties

```properties
# RSA公钥配置（Base64编码，从私钥提取）
# 当私钥改变时，运行 RSAKeyGeneratorTest 重新生成这些值
encryption.rsa-public-modulus=tx2qZ8G2mXv0LxF9yJ3L...
encryption.rsa-public-exponent=AQAB

# RSA私钥（仅用于解密）
encryption.rsa-private-key-pem=-----BEGIN PRIVATE KEY-----
...
-----END PRIVATE KEY-----
```

**重要说明：**
- 公钥的 `modulus` 和 `exponent` 直接配置在配置文件中
- 获取公钥时直接从配置读取，不会访问私钥
- 当私钥更新时，运行 `RSAKeyGeneratorTest` 测试类重新生成公钥配置

## 更新公钥配置

当私钥发生变化时，需要重新生成公钥配置：

### 方法1：运行测试类

1. 运行测试类：
```bash
mvn test -Dtest=RSAKeyGeneratorTest#generatePublicKeyConfig
```

2. 从控制台输出复制配置值：
```
encryption.rsa-public-modulus=tx2qZ8G2mXv0LxF9yJ3L...
encryption.rsa-public-exponent=AQAB
```

3. 更新 `application-dev.properties` 配置文件

### 方法2：调用工具方法

在代码中调用：
```java
Map<String, String> publicKey = rsaUtil.extractPublicKeyFromPrivateKey();
// 从日志中获取配置值
```

**注意：** `extractPublicKeyFromPrivateKey()` 方法会记录警告日志，提醒这是配置更新操作，不应在运行时频繁调用。

## 启用RSA加密

1. 设置配置：
```properties
encryption.rsa-enabled=true
```

2. 确保RSA密钥对已配置在 `application-dev.properties`

3. 前端需要使用对应的公钥进行加密

## 安全特性

### 1. 非对称加密
- 使用RSA 2048位密钥
- 公钥用于加密（客户端）
- 私钥用于解密（服务端）
- 私钥仅保存在服务端，不对外暴露

### 2. 防重放攻击
- 每个nonce只能使用一次
- nonce有时效性（默认5分钟）
- 使用后立即标记，防止重复使用

### 3. 数据完整性
- 解密后验证nonce匹配
- 确保数据未被篡改

### 4. 自动清理
- 定时清理过期nonce
- 清理已使用的历史记录（保留7天）
- 防止数据库膨胀

## 数据库表结构

```sql
CREATE TABLE nonce (
    id VARCHAR(36) PRIMARY KEY,
    nonce_value VARCHAR(256) NOT NULL UNIQUE,
    user_id VARCHAR(36) NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP,
    expires_at TIMESTAMP NOT NULL
);

-- 索引
CREATE INDEX idx_nonce_value ON nonce(nonce_value);
CREATE INDEX idx_user_id ON nonce(user_id);
CREATE INDEX idx_created_at ON nonce(created_at);
CREATE INDEX idx_expires_at ON nonce(expires_at);
```

## 异常处理

### 常见异常

1. **nonce为空**
   - 错误信息：`nonce不能为空`
   - 原因：请求中未包含nonce字段

2. **nonce已被使用**
   - 错误信息：`nonce已被使用，请重新登录`
   - 原因：重放攻击或重复提交

3. **nonce已过期**
   - 错误信息：`nonce已过期，请重新登录`
   - 原因：请求时间超过5分钟

4. **nonce不匹配**
   - 错误信息：`nonce验证失败`
   - 原因：解密后的nonce与请求中的nonce不一致

5. **解密失败**
   - 错误信息：`密码解密失败`
   - 原因：加密数据格式错误或密钥不匹配

## 测试建议

### 单元测试
- 测试RSA加密解密
- 测试nonce验证逻辑
- 测试过期nonce处理
- 测试重复使用nonce

### 集成测试
- 测试完整登录流程
- 测试并发登录
- 测试nonce清理任务

### 性能测试
- 测试大量nonce记录的查询性能
- 测试清理任务的执行效率

## 注意事项

1. **密钥管理**
   - 私钥必须妥善保管
   - 不要将私钥提交到版本控制系统
   - 生产环境使用环境变量或密钥管理服务

2. **时间同步**
   - 确保服务器时间准确
   - nonce过期依赖服务器时间

3. **数据库性能**
   - 定期监控nonce表大小
   - 根据需要调整清理策略

4. **向后兼容**
   - `encryption.rsa-enabled=false` 时不影响现有登录流程
   - 可以逐步迁移到RSA加密

## 参考资料

- RSA加密算法：https://en.wikipedia.org/wiki/RSA_(cryptosystem)
- 防重放攻击：https://en.wikipedia.org/wiki/Replay_attack
- Nonce概念：https://en.wikipedia.org/wiki/Cryptographic_nonce
