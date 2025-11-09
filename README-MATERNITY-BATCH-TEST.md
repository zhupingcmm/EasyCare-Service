# 产假津贴批量测试快速指南

## 快速开始

### API 接口
```
POST /api/batch-test/maternity/run
```

### 使用 curl
```bash
curl -X POST http://localhost:8080/api/batch-test/maternity/run \
  -F "file=@maternity-test-cases.xlsx"
```

### 使用 Postman
1. POST → `http://localhost:8080/api/batch-test/maternity/run`
2. Body → form-data
3. Key: `file` (类型: File)
4. Value: 选择 Excel 文件

## Excel 表头（必须完全一致）

```
用例编号
用例描述
员工工号
员工姓名
城市代码
生育方式
胎数
是否有奖励假
广州难产类型1  剖腹产、会阴III度破裂
广州难产类型2  吸引产、钳产、臀位牵引产
产假开始时间
员工产前12个月的月平均工资
政府发放津贴
单位申报上年度月平均工资
是否跨4月调薪
调薪前工资
调薪后工资
基本工资
发放时间
提交核定表时间
是否跨7月社保调整
调整前个人社保公积金合计
调整后个人社保公积金合计
月度个人社保公积金合计
弹性福利
ESPP
个人工会费
其他奖励项目
Spot on
其他扣除项
产假结束日期
总产假天数
预计返岗日期
津贴天数
应享受津贴
需补差金额
返还金额
```

## 核心组件

### 1. MaternityTestCaseDTO
- 映射 Excel 中的所有字段
- 包含基本信息、产假相关、工资相关、社保相关、其他项目、期望结果

### 2. MaternityBatchTestService
- `runMaternityBatchTest()` - 执行批量测试
- `parseExcelToTestCases()` - 解析 Excel
- `executeMaternityTestCase()` - 执行单个测试用例（可扩展）

### 3. BatchTestController
- `POST /api/batch-test/maternity/run` - 产假津贴批量测试接口

## 数据类型说明

| 类型 | 示例 | Excel 格式 |
|------|------|-----------|
| 文本 | SH, NORMAL, 张三 | 文本 |
| 整数 | 158, 1 | 数值（无小数） |
| 数值 | 15000, 79000.50 | 数值（可有小数） |
| 日期 | 2024-01-15 | 日期（yyyy-MM-dd） |
| 布尔 | 是/否, true/false, 1/0 | 文本或布尔 |

## 重要提示

### ⚠️ 日期列必须设置为日期格式
1. 选中日期列
2. 右键 → 设置单元格格式
3. 选择"日期" → yyyy-MM-dd

### ⚠️ 表头必须完全一致
- 包括空格、标点符号
- 复制粘贴上面的表头列表最安全

### ⚠️ 期望结果字段必填
- 产假结束日期
- 总产假天数
- 预计返岗日期
- 津贴天数
- 应享受津贴
- 需补差金额
- 返还金额

## 示例测试用例

### 最小示例（必填字段）

| 用例编号 | 用例描述 | 员工工号 | 员工姓名 | 城市代码 | 生育方式 | 产假开始时间 | 员工产前12个月的月平均工资 | 单位申报上年度月平均工资 | 基本工资 | 产假结束日期 | 总产假天数 | 预计返岗日期 | 津贴天数 | 应享受津贴 | 需补差金额 | 返还金额 |
|---------|---------|---------|---------|---------|---------|-------------|----------------------|---------------------|---------|-------------|-----------|-------------|---------|-----------|-----------|---------|
| TC001 | 上海正常生育 | E001 | 张三 | SH | NORMAL | 2024-01-15 | 15000 | 14000 | 12000 | 2024-06-20 | 158 | 2024-06-21 | 158 | 79000 | 5000 | 0 |

## 响应格式

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

## 文件位置

- **DTO**: `MaternityTestCaseDTO.java`
- **Service**: `MaternityBatchTestService.java`
- **Controller**: `BatchTestController.java`
- **工具类**: `ExcelParser.java`（可重用）
- **详细文档**: `docs/maternity-batch-test-template.md`

## 扩展开发

修改 `MaternityBatchTestService.executeMaternityTestCase()` 方法集成实际业务服务：

```java
// TODO: 注入服务
private final MaternityLeaveService maternityLeaveService;
private final MaternityAllowanceService maternityAllowanceService;

// TODO: 实现业务逻辑
private TestCaseResultDTO executeMaternityTestCase(int rowNumber, MaternityTestCaseDTO testCase) {
    // 1. 调用产假计算服务
    // 2. 调用津贴计算服务
    // 3. 比较期望结果和实际结果
    // 4. 返回测试结果
}
```

## 配置文件大小限制

在 `application.properties` 中配置：

```properties
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
```

## 技术栈

- Spring Boot 3.2.10
- Apache POI 5.2.5
- Lombok
- Swagger/OpenAPI

## 遵循的代码规范

✅ Controller 以 `XxxController` 结尾  
✅ Service 以 `Service` 结尾  
✅ DTO 以 `DTO` 结尾  
✅ API 响应格式：`{ code, message, data }`  
✅ 日志使用 `@Slf4j`  
✅ 依赖注入使用 `@RequiredArgsConstructor`  
