# RSA公钥配置说明

## 概述

为了提高安全性和性能，RSA公钥的 `modulus` 和 `exponent` 现在直接配置在配置文件中，而不是每次请求时从私钥提取。

## 优势

### 🔒 安全性提升
- **私钥隔离**：获取公钥时不再访问私钥
- **减少暴露**：私钥仅在解密时使用
- **配置分离**：公钥可以独立配置和分发

### ⚡ 性能优化
- **避免重复计算**：不需要每次都从私钥提取公钥
- **快速响应**：直接从配置读取，响应速度更快
- **减少CPU负载**：避免RSA密钥转换操作

### 🛠️ 运维友好
- **配置化管理**：公钥作为配置项，便于管理
- **版本控制**：配置变更可追溯
- **环境隔离**：不同环境可使用不同的密钥对

## 配置结构

### application.properties（默认配置）
```properties
# RSA公钥配置占位符
encryption.rsa-public-modulus=
encryption.rsa-public-exponent=
```

### application-dev.properties（开发环境）
```properties
# RSA公钥配置（Base64编码）
encryption.rsa-public-modulus=tx2qZ8G2mXv0LxF9yJ3L5bY0kC2lvb5x0o3WzYvPpX1xCw0GZp6zX0oKZ6k6YV2p7xQ9cTqYJlvPZP7hJcL4s5p7gq1g4Gq7uVQjdlXeX0cS9p2b3w+7pL2oY1wK1t2wM2sgUrI1/nFXu5lC8nwFpi7jB8V7M3J6O1cLxZ5xQUu9cN1a40F9qP4iQyZ7my3nV9tL2qK0xM5J7H0f1bOqJ3dM8uQ2L5sV7iK6qL8cH2oT7nD3wN1pV5qU8eK2qZ4sw1vP9wE6nB2uM0qU9rL7bV3xF7uH2pQ9tL1oM3J5nLyxPS8vbE2goLTA
encryption.rsa-public-exponent=AQAB

# RSA私钥（仅用于解密）
encryption.rsa-private-key-pem=-----BEGIN PRIVATE KEY-----
...
-----END PRIVATE KEY-----
```

## 私钥更新流程

当需要更换RSA密钥对时，按以下步骤操作：

### 步骤1：更新私钥

在 `application-dev.properties` 中更新私钥：
```properties
encryption.rsa-private-key-pem=-----BEGIN PRIVATE KEY-----
新的私钥内容
-----END PRIVATE KEY-----
```

### 步骤2：生成公钥配置

#### 方法A：使用测试类（推荐）

1. 运行测试：
```bash
mvn test -Dtest=RSAKeyGeneratorTest#generatePublicKeyConfig
```

2. 从控制台输出获取配置：
```
================================================================================
从私钥生成公钥配置
================================================================================

请将以下配置添加到 application-dev.properties:
--------------------------------------------------------------------------------
encryption.rsa-public-modulus=tx2qZ8G2mXv0LxF9yJ3L...
encryption.rsa-public-exponent=AQAB
--------------------------------------------------------------------------------

模数长度: 344 字符
指数长度: 4 字符
指数值(十进制): 65537
指数值(十六进制): 10001
================================================================================
```

#### 方法B：使用API（开发环境）

临时添加一个管理端点（仅开发环境）：
```java
@GetMapping("/admin/extract-public-key")
public Map<String, String> extractPublicKey() {
    return rsaUtil.extractPublicKeyFromPrivateKey();
}
```

访问端点并从日志中获取配置值。

### 步骤3：更新公钥配置

将生成的配置值更新到 `application-dev.properties`：
```properties
encryption.rsa-public-modulus=新生成的modulus值
encryption.rsa-public-exponent=新生成的exponent值
```

### 步骤4：重启应用

重启应用使配置生效。

### 步骤5：验证

访问公钥接口验证：
```bash
curl http://localhost:8080/api/auth/publicKey
```

响应应包含新的公钥值：
```json
{
  "code": 0,
  "message": "OK",
  "data": {
    "modulus": "新的modulus值",
    "exponent": "AQAB"
  }
}
```

## 代码实现

### RSAUtil 方法说明

#### getPublicKey()
```java
public Map<String, String> getPublicKey()
```
- **用途**：获取公钥信息（运行时调用）
- **来源**：从配置文件读取
- **性能**：快速，无需密钥转换
- **安全**：不访问私钥

#### extractPublicKeyFromPrivateKey()
```java
public Map<String, String> extractPublicKeyFromPrivateKey()
```
- **用途**：从私钥提取公钥（配置更新时调用）
- **来源**：从私钥计算
- **性能**：较慢，涉及密钥转换
- **安全**：访问私钥，记录警告日志

## 安全建议

### ✅ 推荐做法

1. **配置分离**：生产环境的公钥和私钥配置在环境变量或密钥管理服务中
2. **访问控制**：限制对私钥配置的访问权限
3. **定期轮换**：定期更换密钥对（如每季度）
4. **版本管理**：不要将私钥提交到版本控制系统

### ❌ 避免做法

1. **频繁提取**：不要在运行时频繁调用 `extractPublicKeyFromPrivateKey()`
2. **硬编码**：不要在代码中硬编码密钥
3. **共享私钥**：不要在多个环境共享同一私钥
4. **明文存储**：生产环境不要明文存储私钥

## 故障排查

### 问题1：公钥未配置

**错误信息：**
```
RSA公钥模数未配置，请在配置文件中设置 encryption.rsa-public-modulus
```

**解决方案：**
运行 `RSAKeyGeneratorTest` 生成公钥配置并更新配置文件。

### 问题2：公钥与私钥不匹配

**症状：**
- 前端加密成功，但后端解密失败
- 错误信息：`javax.crypto.BadPaddingException`

**解决方案：**
1. 确认公钥配置是从当前私钥提取的
2. 重新运行 `RSAKeyGeneratorTest` 生成正确的公钥配置
3. 更新配置并重启应用

### 问题3：私钥格式错误

**错误信息：**
```
私钥不是RSA CRT格式，无法提取公钥信息
```

**解决方案：**
确保私钥是 PKCS#8 格式的 RSA 私钥。

## 监控建议

### 日志监控

监控以下日志：
- `从配置文件获取公钥信息` - 正常的公钥获取
- `正在从私钥提取公钥信息` - 警告，应仅在配置更新时出现
- `RSA公钥模数未配置` - 错误，需要立即处理

### 性能监控

对比优化前后的性能指标：
- `/api/auth/publicKey` 接口响应时间
- CPU使用率
- 内存使用情况

## 总结

通过将公钥配置化，我们实现了：
- ✅ 更高的安全性（私钥隔离）
- ✅ 更好的性能（避免重复计算）
- ✅ 更易的运维（配置化管理）

当私钥需要更新时，只需运行测试类生成新的公钥配置即可。
