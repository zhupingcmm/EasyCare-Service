# Excel 批量测试系统

## 快速开始

### 1. API 接口

```
POST /api/batch-test/run
Content-Type: multipart/form-data
参数：file（Excel 文件，支持 .xls 和 .xlsx）
```

### 2. 使用 Postman 测试

1. 选择 POST 方法
2. URL: `http://localhost:8080/api/batch-test/run`
3. Body 选择 `form-data`
4. Key 输入 `file`，类型选择 `File`
5. Value 选择你的 Excel 文件
6. 点击 Send

### 3. 使用 curl 测试

```bash
curl -X POST http://localhost:8080/api/batch-test/run \
  -F "file=@your-test-file.xlsx"
```

## Excel 文件格式示例

| 输入 | 期望输出 | 备注 |
|------|----------|------|
| 测试1 | 结果1 | 说明1 |
| 测试2 | 结果2 | 说明2 |

## 核心组件

### 1. ExcelParser（可重用工具类）
- 位置：`com.hr.maternity.util.ExcelParser`
- 功能：通用 Excel 解析，支持 .xls 和 .xlsx
- 特性：自动类型识别、安全取值方法

### 2. BatchTestService（业务服务）
- 位置：`com.hr.maternity.service.BatchTestService`
- 功能：批量测试执行、结果统计
- 扩展点：`executeTestCase()` 方法需根据业务逻辑实现

### 3. BatchTestController（REST 接口）
- 位置：`com.hr.maternity.controller.BatchTestController`
- 功能：文件上传、参数验证、结果返回

## 自定义业务逻辑

修改 `BatchTestService.executeTestCase()` 方法来实现你的业务逻辑：

```java
private TestCaseResultDTO executeTestCase(int caseNumber, Map<String, Object> testCase) {
    // 1. 提取输入参数
    String input1 = ExcelParser.getStringValue(testCase, "列名1");
    Integer input2 = ExcelParser.getIntegerValue(testCase, "列名2");
    
    // 2. 调用业务服务
    YourResponse response = yourService.doSomething(input1, input2);
    
    // 3. 获取期望结果
    Object expected = testCase.get("期望结果");
    
    // 4. 比较结果
    boolean isSuccess = expected.equals(response.getResult());
    
    // 5. 返回测试结果
    return TestCaseResultDTO.builder()
        .caseNumber(caseNumber)
        .isSuccess(isSuccess)
        .inputData(testCase)
        .expectedResult(expected)
        .actualResult(response.getResult())
        .errorMessage(isSuccess ? null : "结果不匹配")
        .build();
}
```

## 响应示例

```json
{
  "code": 0,
  "message": "OK",
  "data": {
    "totalCount": 10,
    "successCount": 8,
    "failureCount": 2,
    "successRate": 0.8,
    "executionTimeMs": 1234,
    "details": [...]
  }
}
```

## 配置文件大小限制

在 `application.properties` 或 `application.yml` 中配置：

```properties
# 单个文件最大大小
spring.servlet.multipart.max-file-size=10MB
# 整个请求最大大小
spring.servlet.multipart.max-request-size=10MB
```

## 详细文档

查看 [docs/batch-test-usage.md](docs/batch-test-usage.md) 获取完整使用说明。

## 技术栈

- Spring Boot 3.2.10
- Apache POI 5.2.5
- Lombok
- Swagger/OpenAPI

## 遵循的代码规范

- Controller 以 `XxxController` 结尾
- Service 以 `Service` 结尾
- DTO 以 `DTO` 结尾
- API 响应格式：`{ code, message, data }`
- 日志使用 `@Slf4j`
- 依赖注入使用 `@RequiredArgsConstructor`
