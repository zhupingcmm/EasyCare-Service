# PDF邮件发送功能实现文档

## 概述
实现PDF文件发送到指定邮箱的功能，支持自定义PDF文件路径和邮箱地址。

## 实现日期
2025-11-15

## 输入人
用户

## 实现内容

### 1. 新增包结构
- `com.easy.care.email` - 邮件功能包
- `com.easy.care.email.config` - 邮件配置
- `com.easy.care.email.dto` - 请求响应DTO
- `com.easy.care.email.service` - 邮件服务接口
- `com.easy.care.email.service.impl` - 邮件服务实现
- `com.easy.care.email.controller` - 邮件控制器

### 2. 核心功能
- 支持PDF文件路径参数
- 支持邮箱地址参数
- 支持自定义邮件主题和内容
- PDF文件存在性验证
- 邮件发送结果反馈

### 3. API接口
```
POST /api/pdf-email/send
Content-Type: application/json

{
  "pdfPath": "D:\\path\\to\\file.pdf",
  "emailAddress": "recipient@example.com",
  "subject": "PDF文件",
  "content": "请查收PDF文件"
}
```

### 4. 配置说明
需要在 `application-dev.properties` 中配置邮件服务器信息：

```properties
# email configuration
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
spring.mail.from=your-email@gmail.com
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

## 使用说明

1. 配置邮件服务器信息
2. 调用 `/api/pdf-email/send` 接口
3. 传入PDF文件路径和邮箱地址
4. 系统自动验证文件并发送邮件

## 注意事项

1. 需要添加Spring Boot Mail依赖到pom.xml
2. Gmail需要使用应用专用密码
3. PDF文件路径必须是绝对路径
4. 邮箱地址格式需要正确
