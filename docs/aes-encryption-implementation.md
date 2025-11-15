# AES加密实现文档

## 概述
将Base64字段级加密升级为AES加密，使用配置的密钥进行加密，然后Base64编码存储。

## 实现日期
2025-11-15

## 输入人
用户

## 实现内容

### 1. 配置类
- 创建 `EncryptionProperties` 类
- 支持配置加密密钥和算法
- 默认使用AES算法，32位密钥

### 2. 加密转换器
- 更新 `Base64AttributeConverter` 类
- 添加AES加密/解密逻辑
- 使用配置的密钥进行加密
- 先AES加密，再Base64编码存储

### 3. 配置文件
- 在 `application.yml` 中添加加密配置
- 可配置密钥和算法

## 配置说明

### application.yml 配置
```yaml
encryption:
  secret-key: MySecretKey123456789012345678901234  # 32位AES密钥
  algorithm: AES
```

## 使用说明

1. 在配置文件中设置您的密钥
2. 密钥必须是32位字符（AES-256）
3. 可以更改算法类型（如AES、DES等）

## 安全建议

1. 不要在代码中硬编码密钥
2. 使用环境变量或配置中心管理密钥
3. 定期更换密钥
4. 密钥应符合企业安全标准
