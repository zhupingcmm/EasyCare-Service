# Excel 批量测试使用说明

## 功能概述

本系统提供了一套完整的、可重用的 Excel 解析和批量测试解决方案。

## 核心组件

### 1. ExcelParser（通用 Excel 解析工具）

位置：`com.easy.care.util.ExcelParser`

**主要功能：**
- 支持 `.xls` 和 `.xlsx` 格式
- 自动识别单元格类型（字符串、数字、日期、布尔值）
- 提供两种解析方式：
  - `parseToMapList()` - 解析为 Map 列表
  - `parseToObjectList()` - 解析为自定义对象列表

**工具方法：**
- `getStringValue()` - 安全获取字符串值
- `getIntegerValue()` - 安全获取整数值
- `getBooleanValue()` - 安全获取布尔值
- `getLocalDateValue()` - 安全获取日期值

### 2. BatchTestService（批量测试服务）

位置：`com.easy.care.service.BatchTestService`

**核心方法：**
- `runBatchTest(MultipartFile file)` - 执行批量测试

**可扩展点：**
- `executeTestCase()` - 执行单个测试用例（需根据业务逻辑实现）
- `performBusinessLogic()` - 实际业务逻辑（需根据业务逻辑实现）
- `compareResults()` - 结果比较逻辑（可自定义）

### 3. BatchTestController（批量测试控制器）

位置：`com.easy.care.controller.BatchTestController`

**API 接口：**
```
POST /api/batch-test/run
Content-Type: multipart/form-data
参数：file（Excel 文件）
```

## Excel 文件格式要求

### 基本要求
1. 第一行必须是表头（列名）
2. 从第二行开始是数据行
3. 支持 `.xls` 和 `.xlsx` 格式

### 示例格式

| 输入 | 期望输出 | 备注 |
|------|----------|------|
| 测试数据1 | 期望结果1 | 说明1 |
| 测试数据2 | 期望结果2 | 说明2 |

**注意：** 表头名称需要与代码中使用的列名一致。

## 使用示例

### 1. 基本使用（使用现有实现）

```bash
curl -X POST http://localhost:8080/api/batch-test/run \
  -F "file=@test-cases.xlsx"
```

### 2. 自定义业务逻辑

修改 `BatchTestService.executeTestCase()` 方法：

```java
private TestCaseResultDTO executeTestCase(int caseNumber, Map<String, Object> testCase) {
    // 1. 从 Excel 行数据中提取输入参数
    String cityCode = ExcelParser.getStringValue(testCase, "城市代码");
    LocalDate startDate = ExcelParser.getLocalDateValue(testCase, "开始日期");
    Integer expectedDays = ExcelParser.getIntegerValue(testCase, "期望天数");
    
    // 2. 调用实际业务服务
    MaternityLeaveRequest request = MaternityLeaveRequest.builder()
        .cityCode(cityCode)
        .startDate(startDate)
        .build();
    
    MaternityLeaveResponse response = maternityLeaveService.calculateMaternityLeave(request);
    Integer actualDays = response.getTotalDays();
    
    // 3. 比较结果
    boolean isSuccess = expectedDays.equals(actualDays);
    
    // 4. 返回测试结果
    return TestCaseResultDTO.builder()
        .caseNumber(caseNumber)
        .isSuccess(isSuccess)
        .inputData(testCase)
        .expectedResult(expectedDays)
        .actualResult(actualDays)
        .errorMessage(isSuccess ? null : "期望天数: " + expectedDays + ", 实际天数: " + actualDays)
        .build();
}
```

### 3. 使用 ExcelParser 进行自定义解析

```java
// 解析为 Map 列表
List<Map<String, Object>> data = ExcelParser.parseToMapList(inputStream, fileName);

// 解析为自定义对象列表
List<MyObject> objects = ExcelParser.parseToObjectList(inputStream, fileName, map -> {
    return MyObject.builder()
        .field1(ExcelParser.getStringValue(map, "列名1"))
        .field2(ExcelParser.getIntegerValue(map, "列名2"))
        .field3(ExcelParser.getLocalDateValue(map, "列名3"))
        .build();
});
```

## API 响应格式

### 成功响应

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
    "details": [
      {
        "caseNumber": 2,
        "isSuccess": true,
        "inputData": {
          "输入": "测试数据1",
          "期望输出": "期望结果1"
        },
        "expectedResult": "期望结果1",
        "actualResult": "期望结果1",
        "errorMessage": null
      },
      {
        "caseNumber": 3,
        "isSuccess": false,
        "inputData": {
          "输入": "测试数据2",
          "期望输出": "期望结果2"
        },
        "expectedResult": "期望结果2",
        "actualResult": "实际结果2",
        "errorMessage": "结果不匹配"
      }
    ]
  }
}
```

### 错误响应

```json
{
  "code": 400,
  "message": "不支持的文件格式，仅支持 .xls 和 .xlsx 格式",
  "data": null
}
```

## 扩展建议

### 1. 支持更多测试场景

在 `BatchTestService` 中添加不同的测试方法：

```java
public BatchTestResultDTO runMaternityLeaveTest(MultipartFile file) {
    // 产假计算专用测试
}

public BatchTestResultDTO runAllowanceTest(MultipartFile file) {
    // 生育津贴计算专用测试
}
```

### 2. 添加测试报告导出

```java
public byte[] exportTestReport(BatchTestResultDTO result) {
    // 将测试结果导出为 Excel 或 PDF
}
```

### 3. 支持异步批量测试

```java
@Async
public CompletableFuture<BatchTestResultDTO> runBatchTestAsync(MultipartFile file) {
    // 异步执行大批量测试
}
```

## 注意事项

1. **文件大小限制**：默认 Spring Boot 文件上传限制为 1MB，如需上传更大文件，请在 `application.properties` 中配置：
   ```properties
   spring.servlet.multipart.max-file-size=10MB
   spring.servlet.multipart.max-request-size=10MB
   ```

2. **内存管理**：处理大型 Excel 文件时注意内存使用，建议分批处理。

3. **线程安全**：`ExcelParser` 是无状态工具类，线程安全。

4. **日期格式**：Excel 中的日期会自动转换为 `LocalDate`，确保 Excel 单元格格式设置为日期类型。

## 依赖版本

```xml
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi</artifactId>
    <version>5.2.5</version>
</dependency>
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-ooxml</artifactId>
    <version>5.2.5</version>
</dependency>
```
