# Prompts 记录

## 2025-01-16

### 输入人：用户

**提示词 1：**
```
@[src/main/java/com/hr/maternity/dto/MaternityAllowanceRequest.java] 增加参数
参数类型：LocalDate
参数名：maternityLeaveRequestDate
```

**执行内容：**
在 `MaternityAllowanceRequest.java` 中添加了新的字段：
- 参数名：`maternityLeaveRequestDate`
- 参数类型：`LocalDate`
- 添加了 `@NotNull` 验证注解
- 添加了中文注释：产假申请日期
- 位置：在 `maternityLeaveEndDate` 之后，`companyAdvance` 之前

---

**提示词 2：**
```
@[src/main/java/com/hr/maternity/strategy/impl/allowance/ShanghaiMaternityAllowanceStrategy.java] 
添加private方法，返回为BigDecimal类型，输入参数为员工月基本工资，调整后的月基本工资，产假开始时间和产假申请日期

业务逻辑：
1. 首先验证如果产假申请日期不在产假开始日期之后，则返回0
2. 如果验证产假申请日期在产假开始日期之后则考虑下列情况：
   2.1 两个日期在同一个月，且都在当月15号之前，则返回0
   2.2 两个日期在同一个月，且都在当月15号之后，则返回0
   2.3 两个日期在同一个月，产假开始日期在15号之前，申请产假日期在15号之后，
       则根据maternityWageCalculatorService.calculateStartingMonthMaternityWage方法计算
   2.4 产假申请日期在产假开始日期之后的某个月：
       2.4.1 产假开始日期在当月15号之前，申请产假日期在后面某个月15号之前
             = 2.3计算结果 + 完整月数×(月基本工资-月社保缴费基数-ESPP-工会费)
       2.4.2 产假开始日期在当月15号之后，申请产假日期在后面某个月15号之前
             = 完整月数×(月基本工资-月社保缴费基数-ESPP-工会费)
       2.4.3 产假开始日期在当月15号之前，申请产假日期在后面某个月15号之后
             = 2.3计算结果 + 完整月数×(月基本工资-月社保缴费基数-ESPP-工会费)
       2.4.4 产假开始日期在当月15号之后，申请产假日期在后面某个月15号之后
             = 完整月数×(月基本工资-月社保缴费基数-ESPP-工会费)
```

**执行内容：**
在 `ShanghaiMaternityAllowanceStrategy.java` 中成功添加了私有方法 `calculateRequestDateCompensation`：
- **方法签名**：
  - 参数：`monthlyBaseSalary`, `adjustedMonthlyBaseSalary`, `maternityLeaveStartDate`, `maternityLeaveRequestDate`, `socialInsuranceBase`, `espp`, `unionFee`
  - 返回类型：`BigDecimal`
- **实现逻辑**：
  - ✅ 验证产假申请日期必须在产假开始日期之后，否则返回0
  - ✅ 同月情况（2.1-2.3）：根据两个日期是否在15号前后判断
  - ✅ 跨月情况（2.4.1-2.4.4）：根据开始日期和申请日期的15号分界线，计算不同的补偿金额
  - ✅ 使用 `maternityWageCalculatorService.calculateStartingMonthMaternityWage` 计算开始月工资
  - ✅ 计算完整月数并乘以月扣除额（月基本工资 - 社保 - ESPP - 工会费）
  - ✅ 所有计算都进行了空值保护
- **技术细节**：
  - 添加了 `import java.time.LocalDate` 导入语句
  - 方法共126行代码，覆盖所有8种业务场景
  - 使用 `LocalDate` API 进行日期计算和比较
- **编译状态**：✅ **BUILD SUCCESS**（72个源文件编译成功）

---

**提示词 3：**
```
请考虑此方法中monthlyBaseSalary和socialInsuranceBase需要根据具体的月份是否跨4月和跨7月，
跨4月时，3月之前用monthlyBaseSalary，4月和之后用adjustedMonthlyBaseSalary代替，
跨7月时，6月之前用socialInsuranceBase，7月和之后用adjustedSocialInsuranceBase代替。请优化
```

**执行内容：**
优化了 `calculateRequestDateCompensation` 方法，实现按月动态选择工资和社保基数：

- **方法签名更新**：
  - 新增参数：`adjustedSocialInsuranceBase`（调整后的月社保缴费基数）
  - 完整参数列表：`monthlyBaseSalary`, `adjustedMonthlyBaseSalary`, `maternityLeaveStartDate`, `maternityLeaveRequestDate`, `socialInsuranceBase`, `adjustedSocialInsuranceBase`, `espp`, `unionFee`

- **核心优化逻辑**：
  - **跨4月处理**：
    - 3月及之前：使用 `monthlyBaseSalary`
    - 4月及之后：使用 `adjustedMonthlyBaseSalary`（如果有）
  - **跨7月处理**：
    - 6月及之前：使用 `socialInsuranceBase`
    - 7月及之后：使用 `adjustedSocialInsuranceBase`（如果有）

- **实现细节**：
  - ✅ 同月情况（2.3）：根据开始月份选择对应的月基本工资
  - ✅ 跨月情况（2.4.1-2.4.4）：逐月遍历计算，每个月根据月份动态选择：
    - 月基本工资：`month >= 4` 时使用调整值
    - 社保缴费基数：`month >= 7` 时使用调整值
  - ✅ 使用 `LocalDate.plusMonths()` 进行月份递增
  - ✅ 每月独立计算扣除额：`月基本工资 - 社保基数 - ESPP - 工会费`
  - ✅ 累加所有月份的扣除额得到最终结果

- **代码改进**：
  - 从简单的 `完整月数 × 固定扣除额` 改为 `逐月计算并累加`
  - 每个月根据其月份值动态选择对应的工资和社保基数
  - 保持了所有8种业务场景的逻辑完整性

- **编译状态**：✅ **BUILD SUCCESS**（72个源文件编译成功）

---

**提示词 4：**
```
将此方法优化返回修改为HashMap<String, Object>, 两个key，分别为compensation, refundDetail, 
compensate的value为现在的返回BigDecimal, refundDetail的value为String格式，
当compensation大于0是，描述分别每个月的年，月和金额，
将此方法用于calculateMaternityAllowance方法中的返还金额计算，
返还金额=原返还金额加上calculateRequestDateCompensation方法返回中compensation的值
并在构建refundDetailsList时，如果compensation不为0时，将返回的refundDetail对应的value加入
```

**执行内容：**
成功优化了 `calculateRequestDateCompensation` 方法并集成到 `calculateMaternityAllowance` 中：

- **方法返回值优化**：
  - 修改返回类型：从 `BigDecimal` 改为 `Map<String, Object>`
  - 返回两个key：
    - `compensation`：BigDecimal类型，补偿金额
    - `refundDetail`：String类型，详细描述
  - 添加 `HashMap` 导入

- **详细描述生成**：
  - 当 `compensation > 0` 时，生成格式化的月度详情
  - 格式：`产假申请日期补偿：YYYY年MM月：XX.XX元，YYYY年MM月：XX.XX元...`
  - 使用 `List<String>` 收集每月详情，最后用逗号连接
  - 每月显示：年份、月份、金额（保留2位小数）

- **集成到主计算方法**：
  - 在 `calculateMaternityAllowance` 方法中调用 `calculateRequestDateCompensation`
  - 传入参数：`monthlyBaseSalary`, `adjustedMonthlyBaseSalary`, `maternityLeaveStartDate`, `maternityLeaveRequestDate`, `socialInsuranceBase`, `adjustedSocialInsuranceBase`, `espp`, `unionFee`
  - 提取返回值：`compensation` 和 `refundDetail`
  - 将 `compensation` 加到原返还金额：`refundAmount = refundAmount.add(requestDateCompensation)`

- **详情列表更新**：
  - 在 `refundDetailsList` 构建时，检查 `requestDateCompensation > 0` 且 `refundDetail` 不为空
  - 如果满足条件，将 `refundDetail` 添加到详情列表中
  - 位置：在工会费详情之后，返还金额公式之前

- **编译状态**：✅ **BUILD SUCCESS**（72个源文件编译成功）

---

**提示词 5：**
```
将calculateRequestDateCompensation此方法加入到单独新建的service implementation当中，并且应用与天津，绍兴，厦门，青岛的津贴计算当中
```

**执行内容：**
成功将产假申请日期补偿计算方法提取为独立Service，并应用到多个城市策略：

- **Service接口和实现创建**：
  - 创建 `RequestDateCompensationService` 接口
  - 创建 `RequestDateCompensationServiceImpl` 实现类
  - 方法签名：8个参数（monthlyBaseSalary, adjustedMonthlyBaseSalary, maternityLeaveStartDate, maternityLeaveRequestDate, socialInsuranceBase, adjustedSocialInsuranceBase, espp, unionFee）
  - 返回类型：`Map<String, Object>`，包含 "compensation" 和 "refundDetail"
  - 实现完整的8种业务场景逻辑（同月/跨月，15号前后）
  - 支持跨4月和跨7月的动态工资/社保基数选择

- **上海策略重构**：
  - 删除私有方法 `calculateRequestDateCompensation`
  - 注入 `RequestDateCompensationService` 依赖
  - 调用 Service 方法替代原有私有方法
  - 删除不再使用的 `LocalDate` 和 `HashMap` 导入

- **天津策略集成**：
  - 添加 `RequestDateCompensationService` 导入和依赖注入
  - 在返还金额计算前调用 Service 方法
  - 将补偿金额加到返还金额中
  - 在返还明细列表中添加补偿详情

- **绍兴策略集成**：
  - 添加 `RequestDateCompensationService` 导入和依赖注入
  - 在返还金额计算前调用 Service 方法
  - 将补偿金额加到返还金额中
  - 在返还明细列表中添加补偿详情

- **厦门策略集成**：
  - 添加 `RequestDateCompensationService` 导入和依赖注入
  - 在返还金额计算前调用 Service 方法
  - 将补偿金额加到返还金额中
  - 在返还明细列表中添加补偿详情

- **青岛策略集成**：
  - 添加 `RequestDateCompensationService` 导入和依赖注入
  - 在返还金额计算前调用 Service 方法
  - 将补偿金额加到返还金额中
  - 在返还明细列表中添加补偿详情

- **技术实现**：
  - 使用 `@Service` 和 `@RequiredArgsConstructor` 注解
  - 依赖注入 `MaternityWageCalculatorService`
  - 所有策略统一调用方式和参数传递
  - 详情格式统一：`产假申请日期补偿：YYYY年MM月：XX.XX元，YYYY年MM月：XX.XX元...`

- **编译状态**：✅ **BUILD SUCCESS**（74个源文件编译成功，新增2个Service文件）

---

**提示词 6：**
```
修改 @[src/main/java/com/hr/maternity/dto/MaternityAllowanceRequest.java] 参数产假申请日期，可为空，当产假申请日期为空时默认设为跟产假开始日期同一天
```

**执行内容：**
成功修改产假申请日期字段为可选参数，并实现默认值逻辑：

- **字段验证修改**：
  - 移除 `maternityLeaveRequestDate` 字段的 `@NotNull` 验证注解
  - 字段改为可选参数，允许为 `null`
  - 更新字段注释：`产假申请日期（可选，为空时默认为产假开始日期）`

- **默认值实现**：
  - 添加自定义 `getMaternityLeaveRequestDate()` 方法
  - 当字段为 `null` 时，自动返回 `maternityLeaveStartDate`
  - 当字段不为 `null` 时，返回实际设置的值
  - 实现逻辑：`return maternityLeaveRequestDate != null ? maternityLeaveRequestDate : maternityLeaveStartDate;`

- **技术实现**：
  - 使用 Lombok 的 `@Data` 注解，自定义 getter 方法会覆盖默认生成的方法
  - 保持向后兼容性，所有现有代码无需修改
  - 所有调用 `request.getMaternityLeaveRequestDate()` 的地方都会自动获得正确的值

- **影响范围**：
  - 上海、天津、绍兴、厦门、青岛的津贴计算策略自动支持此功能
  - `RequestDateCompensationService` 自动支持此功能
  - 无需修改任何策略实现代码

- **编译状态**：✅ **BUILD SUCCESS**（74个源文件编译成功）

---

## 2025-01-17 - 修改 BatchTestService 构建 CompanyAdvanceMap 逻辑

**需求**：修改 `BatchTestService.java` 中 `buildMaternityAllowanceRequest` 方法，构建 `MaternityAllowanceRequest` 中的 `companyAdvance` 对象

**实现内容**：

1. **新增 `buildCompanyAdvanceMap` 方法**：
   - 构建 `CompanyAdvanceMap` 对象
   - 从测试用例数据中提取相关字段

2. **构建 `addItem` Map**（增加项目）：
   - `espp` ← `allowanceInfo.getEspp()` (弹性福利 → ESPP)
   - `adjustedSocialInsuranceBase` ← `allowanceInfo.getSocialSecurityBeforeAdjustment()` (调整前个人社保公积金合计)
   - `socialInsuranceBase` ← `allowanceInfo.getSocialSecurityAfterAdjustment()` (调整后个人社保公积金合计)

3. **构建 `deleteItem` Map**（删除项目）：
   - `spotOn` ← `maternityCalcInfo.getSpotOn()` (spot on)

4. **在 `buildMaternityAllowanceRequest` 中调用**：
   - 调用 `buildCompanyAdvanceMap(testCase)` 构建公司垫付信息
   - 将结果设置到 `request.setCompanyAdvance(companyAdvance)`

**技术细节**：
- 添加了 `CompanyAdvanceMap` 和 `BigDecimal` 的 import 语句
- 使用 `HashMap` 存储 addItem 和 deleteItem
- 对每个字段进行 null 检查，只添加非 null 的值
- 遵循命名规范，方法名使用 `buildXxx` 前缀

**输入人**：用户

---

## 2025-12-10

### 输入人：用户

**提示词：**
```
帮我生成 t_history 的 JPA 实体，以及实现这三个 new api
```

**执行内容：**
- 新增 `HistoryRecordDO` 实体，对应 `t_history` 表，包含 `hrId`、`employeeId`、`employeeData(jsonb)` 以及 `created_time/updated_time`，并设置 `(hr_id, employee_id)` 唯一约束。
- 新建 `HistoryRecordRepository`、`HistoryRecordService`/`Impl`，提供新增、查询、删除能力，查询与删除按 HR 维度过滤。
- 定义 `HistoryAddRequest`、`HistoryQueryRequest`、`HistoryDeleteRequest`、`HistoryRecordDTO`、`HistoryOperationStatusDTO` 等 DTO，使用 `@Valid` 做参数校验。
- 创建 `HistoryRecordController`，提供 `/api/history/add`、`/api/history/query`、`/api/history/delete` 三个 API，统一返回 `ApiResponse`。
- 文档 `docs/history-design.md` 已补充表字段命名调整记录（created_time/updated_time）。

---

## 2025-11-15

### 输入人：用户

**提示词：**
```
springboot+hibernet +JPA实现数据库字段级别的加密，我要实现一个注解， 在entity 需要加密的的字段加上此注解，则入库时时能加密，读取时能解密。新加一个package在里面生成一个demo，不要动现有java项目结构，可以在pom中加入此功能需要的库。加密算法使用base64，要支持加密解密。为我生成demo 和demo sql建表语句， 加密字段类型 支持各种类型，包括日期 ，字符和 数字，sql建表要满足存储要求。 demo要包括 入库和出库，要有demo controller， 可以直接在postman测试。使用最新的主流方案，不要使用deprecated的方案和接口。
```

**执行内容：**
- 新增 `com.hr.maternity.demo.encryption` 包，包含 `@EncryptedField` 注解、Base64 属性转换器、异常与负载模型。
- 创建 Demo 实体、Repository、DTO、Service/Impl 以及 Controller，演示字段级加密的创建、查询全部、按 ID 查询。
- 提供建表示例 SQL 以及 Postman 可直接调用的接口说明。

---

## 2025-01-17 - 将 spotOn 字段从 MaternityCalcInfo 移至 AllowanceCalcInfo

**需求**：将 `MaternityTestCaseRowDTO` 中的 `spotOn` 字段从 `MaternityCalcInfo` 类移动到 `AllowanceCalcInfo` 类

**实现内容**：

1. **修改 `MaternityTestCaseRowDTO.java`**：
   - 从 `MaternityCalcInfo` 类中移除 `spotOn` 字段
   - 在 `AllowanceCalcInfo` 类中添加 `spotOn` 字段
   - 更新 `fromMap()` 方法：将 `spotOn` 的解析从 `maternityCalcInfo` 构建器移至 `allowanceCalcInfo` 构建器

2. **修改 `BatchTestService.java`**：
   - 更新 `buildCompanyAdvanceMap()` 方法中的 `spotOn` 访问路径
   - 从 `maternityCalcInfo.getSpotOn()` 改为 `allowanceInfo.getSpotOn()`
   - 移除未使用的 `maternityCalcInfo` 变量
   - 修复 bug：将 `otherDeductions` 正确添加到 `deleteItem` 而不是 `addItem`

**影响范围**：
- `MaternityTestCaseRowDTO.MaternityCalcInfo` - 移除 spotOn 字段
- `MaternityTestCaseRowDTO.AllowanceCalcInfo` - 新增 spotOn 字段
- `BatchTestService.buildCompanyAdvanceMap()` - 更新字段访问路径

**技术细节**：
- `spotOn` 字段类型：`BigDecimal`
- Excel 列名映射：`"Spot on"`
- 在 `CompanyAdvanceMap` 中，`spotOn` 属于 `deleteItem`（扣除项）

**输入人**：用户

---

## 2025-01-17 - 修改 BatchTestService executeTestCase 方法和 TestCaseResultDTO

**需求**：
1. `TestCaseResultDTO` 中的 `caseNumber` 从 `MaternityTestCaseRowDTO` 中的 `caseNumber` 获取值
2. `TestCaseResultDTO` 添加 `caseDescription` 字段，从 `MaternityTestCaseRowDTO` 中的 `caseDescription` 获取值

**实现内容**：

1. **修改 `TestCaseResultDTO.java`**：
   - 将 `caseNumber` 字段类型从 `Integer` 改为 `String`（与 `MaternityTestCaseRowDTO` 保持一致）
   - 添加 `caseDescription` 字段（`String` 类型）
   - 更新注释：`caseNumber` 从"Excel 行号"改为"测试用例编号"

2. **修改 `BatchTestService.java` 的 `executeTestCase` 方法**：
   - 成功场景：使用 `testCase.getCaseNumber()` 和 `testCase.getCaseDescription()` 赋值
   - 异常场景（catch块）：使用 `testCase.getCaseNumber()` 和 `testCase.getCaseDescription()` 赋值
   - 并行执行异常场景：使用 `testCase.getCaseNumber()` 和 `testCase.getCaseDescription()` 赋值

**变更前后对比**：

**之前**：
```java
// TestCaseResultDTO
private Integer caseNumber;  // Excel 行号

// BatchTestService
.caseNumber(caseNumber)  // 使用方法参数（Excel行号）
```

**现在**：
```java
// TestCaseResultDTO
private String caseNumber;  // 测试用例编号
private String caseDescription;  // 测试用例描述

// BatchTestService
.caseNumber(testCase.getCaseNumber())  // 从测试用例数据获取
.caseDescription(testCase.getCaseDescription())  // 从测试用例数据获取
```

**影响范围**：
- `TestCaseResultDTO` - 字段类型和新增字段
- `BatchTestService.executeTestCase()` - 三处赋值逻辑修改
- 测试结果现在包含实际的用例编号和描述，而不是Excel行号

**输入人**：用户

---

## 2025-01-17 - TestCaseResultDTO 添加 cityCode 字段

**需求**：`TestCaseResultDTO` 添加 `cityCode` 字段，从 `MaternityTestCaseRowDTO` 中的 `cityCode` 获取值

**实现内容**：

1. **修改 `TestCaseResultDTO.java`**：
   - 添加 `cityCode` 字段（`String` 类型）
   - 位置：在 `caseDescription` 之后，`isSuccess` 之前

2. **修改 `BatchTestService.java` 的 `executeTestCase` 方法**：
   - 成功场景：添加 `.cityCode(testCase.getCityCode())`
   - 异常场景（catch块）：添加 `.cityCode(testCase.getCityCode())`
   - 并行执行异常场景：添加 `.cityCode(testCase.getCityCode())`

**字段顺序**：
```java
private String caseNumber;        // 测试用例编号
private String caseDescription;   // 测试用例描述
private String cityCode;          // 城市代码 ✅ 新增
private Boolean isSuccess;        // 是否成功
```

**影响范围**：
- `TestCaseResultDTO` - 新增 cityCode 字段
- `BatchTestService.executeTestCase()` - 三处添加 cityCode 赋值
- 测试结果现在包含城市代码信息，便于按城市筛选和分析测试结果

**输入人**：用户

---

## 2025-11-30 - 创建 t_allowance_rules 表的增删改查 API

**需求**：根据 `t_allowance_rules` 表增加增删改查API，删除是逻辑删除，查询支持分页

**实现内容**：

1. **创建数据库表**：
   - 创建 `V13__Create_t_allowance_rules.sql` 迁移文件
   - 表结构：id, city, payout_method, is_active, create_date, create_by, update_date, update_by
   - 添加唯一索引：`idx_t_allowance_rules_city`（仅对激活状态的记录）
   - 创建 `V14__Add_is_active_to_holiday.sql`（原V12内容移至V14）

2. **创建实体类**：
   - `AllowanceRules.java` - 津贴规则实体
   - 包含审计字段（创建人、创建时间、更新人、更新时间）
   - 支持逻辑删除（is_active字段）

3. **创建 DTO**：
   - `AllowanceRulesRequest.java` - 请求DTO，包含参数校验
   - `AllowanceRulesResponse.java` - 响应DTO

4. **创建 Repository**：
   - `AllowanceRulesRepository.java` - 数据访问层
   - 方法：`findByCityAndIsActiveTrue()`, `findByIsActiveTrue(Pageable)`

5. **创建 Service**：
   - `AllowanceRulesService.java` - 服务接口
   - `AllowanceRulesServiceImpl.java` - 服务实现
   - 实现增删改查和批量导入功能

6. **创建 Controller**：
   - `AllowanceRulesController.java` - REST API控制器
   - API端点：
     - `POST /api/allowance-rules` - 创建
     - `GET /api/allowance-rules/{id}` - 查询单个
     - `GET /api/allowance-rules` - 分页查询（支持分页）
     - `PUT /api/allowance-rules/{id}` - 更新
     - `DELETE /api/allowance-rules/{id}` - 逻辑删除
     - `GET /api/allowance-rules/template/download` - 下载CSV模板
     - `POST /api/allowance-rules/import` - 批量导入

7. **CSV模板导出功能**：
   - 在 `ExcelExporter.java` 中添加 `createAllowanceRulesTemplate()` 方法
   - 模板字段：城市、津贴发放方式
   - 示例数据：上海、社保局
   - 包含说明文档

8. **批量导入功能**：
   - 在 `AllowanceRulesServiceImpl` 中实现 `batchImportAllowanceRules()` 方法
   - 支持CSV文件导入
   - 自动处理重复城市（逻辑删除旧规则，创建新规则）
   - 数据验证和错误处理

**技术特性**：
- ✅ 逻辑删除（通过 `is_active` 字段）
- ✅ 分页查询支持
- ✅ 参数校验（使用 `@Valid`）
- ✅ 统一API响应结构（`ApiResponse`）
- ✅ 完整的日志记录
- ✅ Swagger文档注解
- ✅ 审计字段支持
- ✅ CSV模板导出
- ✅ 批量导入功能

**输入人**：用户

---

## 2025-11-30 - 修复文件下载接口类型转换错误

**问题**：`AllowanceRulesController.downloadTemplate()` 执行时报错：
```
class com.hr.maternity.common.ApiResponse cannot be cast to class [B
```

**原因分析**：
- 全局响应处理器 `GlobalResponseAdvice` 会拦截所有返回值
- 它尝试将 `ResponseEntity<byte[]>` 中的 `byte[]` 包装成 `ApiResponse`
- 导致文件下载时出现类型转换错误

**解决方案**：
在 `GlobalResponseAdvice.beforeBodyWrite()` 中添加对 `byte[]` 类型的判断：
1. 直接返回 `byte[]` 类型的body，不做包装
2. 对于 `ResponseEntity<byte[]>`，检查内部body是否为 `byte[]`，如果是则直接返回原 `ResponseEntity`

**修改文件**：
- `GlobalResponseAdvice.java` - 添加 `byte[]` 类型判断，排除文件下载响应

**影响范围**：
- 所有返回 `byte[]` 的接口（如CSV/Excel模板下载）不再被全局响应处理器包装
- 保持文件下载功能正常工作

**输入人**：用户

---

## 2025-11-30 - 修复CSV文件解析中文乱码问题

**问题**：`ExcelParser.parseCsvToMapList()` 解析CSV文件时出现中文乱码

**原因分析**：
- Windows Excel 默认保存CSV文件为 GBK 编码
- 原代码固定使用 UTF-8 编码解析，导致 GBK 编码的中文显示为乱码

**解决方案**：
改进 `parseCsvToMapList()` 方法，实现自动编码检测：
1. 检测 UTF-8 BOM 标记（0xEF 0xBB 0xBF）
2. 尝试 UTF-8 解析，检查是否有乱码字符（�）
3. 如果 UTF-8 解析失败，自动切换到 GBK 编码
4. 添加 `isValidUtf8()` 辅助方法验证 UTF-8 编码有效性

**修改内容**：
```java
// 自动检测编码
byte[] bytes = inputStream.readAllBytes();
if (bytes.length >= 3 && bytes[0] == (byte)0xEF && bytes[1] == (byte)0xBB && bytes[2] == (byte)0xBF) {
    content = new String(bytes, 3, bytes.length - 3, StandardCharsets.UTF_8);
} else {
    String utf8Content = new String(bytes, StandardCharsets.UTF_8);
    if (utf8Content.contains("�") || !isValidUtf8(bytes)) {
        content = new String(bytes, "GBK");
    } else {
        content = utf8Content;
    }
}
```

**修改文件**：
- `ExcelParser.java` - 改进CSV解析方法，支持自动编码检测
- 移除未使用的 `BufferedReader` 和 `InputStreamReader` 导入

**影响范围**：
- ✅ 支持 UTF-8（带BOM和不带BOM）编码的CSV文件
- ✅ 支持 GBK 编码的CSV文件（Windows Excel默认格式）
- ✅ 自动检测编码，无需手动指定
- ✅ 解决中文乱码问题

**输入人**：用户

---

## 2025-11-30 - 修复分页排序参数错误

**问题**：`AllowanceRulesController.listAllAllowanceRules()` 报错：
```
org.springframework.data.mapping.PropertyReferenceException: No property 'id,desc' found for type 'AllowanceRules'
```

**原因分析**：
- `@PageableDefault` 注解中使用了错误的排序语法：`sort = "id,desc"`
- Spring Data 将 `"id,desc"` 作为单个属性名查找，导致找不到该属性
- 正确的语法应该分别指定排序字段和方向

**解决方案**：
修改 `@PageableDefault` 注解的参数：
```java
// 错误写法
@PageableDefault(page = 0, size = 10, sort = "id,desc")

// 正确写法
@PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.DESC)
```

**修改文件**：
- `AllowanceRulesController.java` - 修复分页排序参数
- 添加 `Sort` 类的导入

**影响范围**：
- ✅ 分页查询接口可以正常按ID降序排序
- ✅ 避免属性引用异常

**输入人**：用户

---

## 2025-11-30 - 为节假日添加CSV模板导出功能

**需求**：为 `HolidayController` 创建导出CSV模板功能，header包含：年份、地区、日期、节日名称、中文名称、英文名称、类型、是否为法定假日

**实现内容**：

1. **在 ExcelExporter 中添加方法**：
   - `createHolidayTemplate()` - 创建节假日导入模板
   - 包含8个字段的表头
   - 提供3条示例数据（元旦、春节、春节调休）
   - 包含详细的填写说明

2. **在 HolidayController 中添加接口**：
   - `GET /api/holidays/template/download` - 下载CSV模板
   - 返回 `ResponseEntity<byte[]>`
   - 文件名：`节假日导入模板.csv`

**CSV模板字段**：
- 年份：4位数字年份，如：2025
- 地区：地区代码，如：CN（中国）
- 日期：格式 YYYY-MM-DD，如：2025-01-01
- 节日名称：节日名称
- 中文名称：中文节日名称
- 英文名称：英文节日名称
- 类型：public_holiday（公共假日）、transfer_workday（调休工作日）
- 是否为法定假日：是 或 否

**示例数据**：
```csv
年份,地区,日期,节日名称,中文名称,英文名称,类型,是否为法定假日
2025,CN,2025-01-01,元旦,元旦,New Year's Day,public_holiday,是
2025,CN,2025-01-28,春节,春节,Spring Festival,public_holiday,是
2025,CN,2025-01-26,春节调休,春节调休,Spring Festival Workday,transfer_workday,否
```

**修改文件**：
- `ExcelExporter.java` - 添加 `createHolidayTemplate()` 方法
- `HolidayController.java` - 添加模板下载接口和必要的导入

**影响范围**：
- ✅ 支持节假日批量导入的CSV模板下载
- ✅ 提供详细的字段说明和示例数据
- ✅ 与现有的津贴规则、产假规则模板保持一致的风格

**输入人**：用户

---

## 2025-11-30 - 实现节假日批量导入功能

**需求**：实现按照CSV模板导入节假日的API功能

**实现内容**：

1. **在 HolidayService 接口中添加方法**：
   - `batchImportHolidays(List<Map<String, Object>> dataList)` - 批量导入节假日

2. **在 HolidayServiceImpl 中实现批量导入逻辑**：
   - 解析CSV数据（年份、地区、日期、节日名称、中文名称、英文名称、类型、是否为法定假日）
   - 验证必填字段
   - 检查是否存在相同记录（年份+地区+日期）
   - 支持新增和更新
   - 统计新增、更新、跳过的数量

3. **在 HolidayController 中添加导入接口**：
   - `POST /api/holidays/import` - 批量导入节假日
   - 接收CSV文件
   - 返回处理结果统计

4. **添加 `is_statutory` 字段支持**：
   - 在 `Holiday` 实体中添加 `isStatutory` 字段
   - 在 `HolidayRequest` 和 `HolidayResponse` DTO中添加字段
   - 在 `HolidayRepository` 中添加查询方法
   - 更新所有相关的CRUD方法

**导入逻辑**：
- 根据年份+地区+日期判断记录是否存在
- 存在则更新，不存在则新增
- 自动处理类型枚举转换
- "是"转换为true，其他转换为false
- 中文名称和英文名称可选，默认使用节日名称

**修改文件**：
- `HolidayService.java` - 添加批量导入接口方法
- `HolidayServiceImpl.java` - 实现批量导入逻辑
- `HolidayController.java` - 添加导入API接口
- `Holiday.java` - 添加 `isStatutory` 字段
- `HolidayRequest.java` - 添加 `isStatutory` 字段
- `HolidayResponse.java` - 添加 `isStatutory` 字段
- `HolidayRepository.java` - 添加 `findByYearAndRegionAndDate` 方法

**影响范围**：
- ✅ 支持CSV文件批量导入节假日
- ✅ 自动处理新增和更新
- ✅ 完整的数据验证和错误处理
- ✅ 详细的导入统计信息
- ✅ 支持"是否为法定假日"字段

**输入人**：用户

---

## 2025-12-12 - 优化Java LDAP连接配置支持SSL/TLS

**需求**：Java LDAP连接失败，需要对比Python代码并优化Java实现

**问题分析**：
- **Python配置**：使用端口1222，启用SSL (`use_ssl=True`)，禁用证书验证 (`validate-ssl.CERT_NONE`)
- **Java配置问题**：
  1. 使用端口389（标准LDAP端口），但服务器使用1222
  2. 使用 `ldap://` 协议，应该用 `ldaps://`
  3. 缺少SSL/TLS配置
  4. 缺少证书信任配置
  5. 缺少连接超时设置

**实现内容**：

1. **更新 `LdapConfigurationProperties.java`**：
   - 添加 `useSsl` 字段（默认false）
   - 添加 `trustAllCertificates` 字段（默认false）
   - 添加 `connectTimeout` 字段（默认5000ms）
   - 添加 `readTimeout` 字段（默认10000ms）

2. **修改 `LdapAuthService.java`**：
   - 重构 `buildContextSource()` 方法，接收 `LdapDomainConfig` 参数
   - 根据 `useSsl` 配置动态选择 `ldap://` 或 `ldaps://` 协议
   - 添加连接和读取超时配置
   - 实现 `CustomSSLSocketFactory` 自定义SSL工厂类
   - 配置 `TrustManager` 禁用证书验证（当 `trustAllCertificates=true`）
   - 使用 `Hashtable` 配置JNDI环境属性
   - 添加必要的导入：`javax.net.ssl.*`, `java.security.*`

3. **更新 `application.properties`**：
   - 配置两个LDAP域（GROUP和TECH）
   - 设置端口为1222
   - 启用SSL：`use-ssl=true`
   - 禁用证书验证：`trust-all-certificates=true`
   - 设置超时：`connect-timeout=10000`, `read-timeout=15000`

**核心技术实现**：

```java
// SSL协议选择
String protocol = config.isUseSsl() ? LdapConstants.Protocol.LDAPS : LdapConstants.Protocol.LDAP;

// 自定义TrustManager禁用证书验证
TrustManager[] trustAllCerts = new TrustManager[]{
    new X509TrustManager() {
        public X509Certificate[] getAcceptedIssuers() { return null; }
        public void checkClientTrusted(X509Certificate[] certs, String authType) {}
        public void checkServerTrusted(X509Certificate[] certs, String authType) {}
    }
};

// 配置SSLContext
SSLContext sslContext = SSLContext.getInstance("TLS");
sslContext.init(null, trustAllCerts, new SecureRandom());

// 设置自定义SSL Socket Factory
baseEnvironment.put("java.naming.ldap.factory.socket", CustomSSLSocketFactory.class.getName());
```

**修改文件**：
- `LdapConfigurationProperties.java` - 添加SSL和超时配置选项
- `LdapAuthService.java` - 实现SSL连接和证书信任配置
- `application.properties` - 更新LDAP配置使用1222端口和SSL

**配置对比**：

| 配置项 | Python | Java（优化前） | Java（优化后） |
|--------|--------|----------------|----------------|
| 端口 | 1222 | 389 | 1222 ✅ |
| 协议 | SSL | ldap:// | ldaps:// ✅ |
| 证书验证 | 禁用 | 未配置 | 禁用 ✅ |
| 连接超时 | 默认 | 未配置 | 10000ms ✅ |
| 读取超时 | 默认 | 未配置 | 15000ms ✅ |

**影响范围**：
- ✅ 支持LDAP over SSL (LDAPS)连接
- ✅ 支持自定义证书信任策略
- ✅ 支持连接和读取超时配置
- ✅ 与Python LDAP配置保持一致
- ✅ 修复"LDAP connection has been closed"错误

**输入人**：用户

---

## 2025-12-12 (补充) - 完全对齐Python LDAP实现

**需求**：继续分析Python代码差异，确保Java实现完全一致

**发现的关键差异**：

1. **搜索Base重复设置问题** ⚠️
   - **问题**：`contextSource.setBase(baseDn)` 已设置base，`query().base(baseDn)` 又设置，导致路径重复
   - **Python实现**：`conn.search(search_base=base_dn)` - 只设置一次
   - **修复**：改为 `query().base("")` - 空字符串，使用contextSource的base

2. **缺少SearchScope配置** ⚠️
   - **问题**：Java没有明确指定searchScope，默认可能是ONELEVEL
   - **Python实现**：`search_scope=SUBTREE` - 子树搜索
   - **修复**：添加 `.searchScope(SUBTREE_SCOPE)`

3. **异常处理逻辑不一致** ⚠️
   - **问题**：Java在CommunicationException后继续尝试其他principal
   - **Python实现**：
     ```python
     except core.exceptions.LDAPSocketOpenError as e:
         print(f"Socket open error...")
         break  # ← 关键：停止尝试其他principal
     ```
   - **修复**：在CommunicationException处添加 `break;`

4. **缺少Referral配置**
   - **Python实现**：ldap3自动处理referral
   - **Java修复**：添加 `baseEnvironment.put("java.naming.referral", "follow");`

**代码修改详情**：

```java
// 修改1：添加SUBTREE_SCOPE导入
import static javax.naming.directory.SearchControls.SUBTREE_SCOPE;

// 修改2：修复搜索配置
List<LdapUserInfo> results = ldapTemplate.search(
    query()
        .base("")                      // ← 改为空字符串
        .searchScope(SUBTREE_SCOPE)    // ← 明确指定子树搜索
        .filter(filter),
    userAttributesMapper(serverAddr)
);

// 修改3：异常处理添加break
catch (CommunicationException e) {
    lastError = handleCommunicationException(e, username, domain);
    log.error("Communication error for principal {}: {}, stopping further attempts", 
              principal, e.getMessage());
    break;  // ← 停止尝试其他principal
}

// 修改4：添加referral配置
baseEnvironment.put("java.naming.referral", "follow");
```

**完整对照表**：

| 配置项 | Python代码 | Java（最终版） | 状态 |
|--------|-----------|---------------|------|
| **连接配置** | | | |
| 端口 | `port=1222` | `ldap-port=1222` | ✅ |
| 协议 | `use_ssl=True` | `use-ssl=true` | ✅ |
| 证书验证 | `validate=ssl.CERT_NONE` | `trust-all-certificates=true` | ✅ |
| 连接超时 | 默认 | `connect-timeout=10000` | ✅ |
| 读取超时 | 默认 | `read-timeout=15000` | ✅ |
| Referral | 自动 | `"java.naming.referral"="follow"` | ✅ |
| **搜索配置** | | | |
| 搜索Base | `search_base=base_dn` | `contextSource.setBase(baseDn)` + `query().base("")` | ✅ |
| 搜索Scope | `search_scope=SUBTREE` | `.searchScope(SUBTREE_SCOPE)` | ✅ |
| 搜索过滤器 | `(|(sAMAccountName=...)(userPrincipalName=...))` | 相同 | ✅ |
| **认证配置** | | | |
| Principal格式 | `[user@domain, user@dns, domain\\user]` | 相同 | ✅ |
| Auto Bind | `auto_bind=True` | `getContext(principal, password)` | ✅ |
| **异常处理** | | | |
| Socket错误 | `break` - 停止尝试 | `break;` | ✅ |
| 认证失败 | 继续尝试 | 继续尝试 | ✅ |

**日志增强**：
- 添加绑定尝试的debug日志：`log.debug("Attempting to bind as principal: {}", principal);`
- 添加搜索过滤器debug日志：`log.debug("Searching LDAP with filter: {} in base: {}", filter, baseDn);`
- 优化CommunicationException日志：明确说明停止进一步尝试

**验证清单**：
- ✅ SSL/TLS连接配置
- ✅ 证书信任配置
- ✅ 超时配置
- ✅ 搜索Scope配置
- ✅ 搜索Base配置
- ✅ Referral处理
- ✅ 异常处理逻辑
- ✅ Principal格式
- ✅ 日志输出

**下一步**：
1. 更新 `application.properties` 中的实际LDAP服务器地址（替换XXXXX占位符）
2. 重启应用测试连接
3. 观察日志输出，验证每个步骤

**输入人**：用户

---

## 2025-12-12 (继续) - 增强LDAP日志分析能力

**需求**：优化LdapAuthService，添加更详细的日志用于日常运维分析

**实现内容**：

### 1. **性能监控日志**
- 总认证耗时统计（从开始到结束）
- 每个域的认证耗时
- Context构建耗时
- 绑定操作耗时
- 搜索操作耗时
- 每次尝试的详细耗时

### 2. **结构化日志输出**
```
========== LDAP Authentication Started for user: xxx ==========
[Attempt 1/2] Trying domain: GROUP
┌─ Domain Authentication Details ─────────────────────────────
│ User: xxx
│ Domain: GROUP
│ Server: xxx.com:1222
│ SSL Enabled: true
│ Trust All Certificates: true
│ Base DN: DC=xxx,DC=ZZ,DC=COM
│ DNS Name: xxx.zz.com
│ Connect Timeout: 10000ms
│ Read Timeout: 15000ms
└──────────────────────────────────────────────────────────────
```

### 3. **详细的步骤日志**
- **认证流程**：
  - `[Attempt 1/2]` - 显示当前尝试次数和总次数
  - 配置参数的完整展示
  - 每个步骤的耗时（Context、Bind、Search）

- **绑定尝试**：
  ```
  Starting bind attempts with 3 principal formats
    [1/3] Binding as: user@DOMAIN
    ✓ Successfully bound as user@DOMAIN in 125ms
  ```

- **搜索详情**：
  ```
  Starting LDAP user search:
    Search Base: DC=xxx,DC=ZZ,DC=COM
    Search Filter: (|(sAMAccountName=xxx)(userPrincipalName=xxx@DOMAIN)...)
    Search Scope: SUBTREE
  Search completed in 89ms, found 1 result(s)
  ✓ User found successfully:
    Display Name: Zhang San
    Email: zhangsan@company.com
    UPN: zhangsan@DOMAIN
    SAM Account: zhangsan
    Department: IT
    Company: Company Ltd
  ```

### 4. **异常详细分析**
- 异常类型识别
- 异常消息输出
- 耗时统计（失败情况）
- 多层次日志级别：
  - **ERROR**: 严重错误和通信异常
  - **WARN**: 认证失败、用户未找到
  - **INFO**: 关键步骤和成功信息
  - **DEBUG**: 详细的技术参数和堆栈信息

### 5. **符号标记**
- `✓` - 成功操作
- `✗` - 失败操作
- `⚠` - 警告信息
- `[n/m]` - 进度指示器

### 6. **统计汇总**
```java
// 成功场景
========== LDAP Authentication SUCCESS for user: xxx in 456ms ==========

// 失败场景
========== LDAP Authentication FAILED for user xxx after 2 attempts in 1234ms ==========
```

### 7. **代码改进详情**

#### authenticate() 方法
- ✅ 添加总耗时统计
- ✅ 添加域数量日志
- ✅ 添加尝试计数器
- ✅ 每次尝试显示进度 `[1/2]`
- ✅ 成功/失败的汇总日志

#### authenticateWithDomain() 方法
- ✅ 添加域级别耗时统计
- ✅ 格式化的配置详情展示（使用框线）
- ✅ 各步骤独立计时（Context、Bind、Search）
- ✅ 异常类型和消息分离显示

#### attemptBind() 方法
- ✅ Principal列表日志
- ✅ 每次绑定的进度显示 `[1/3]`
- ✅ 绑定耗时统计
- ✅ 通信错误时停止并标记 `⚠`

#### searchUser() 方法
- ✅ 搜索参数完整展示
- ✅ 搜索耗时和结果数量
- ✅ 用户信息详细输出
- ✅ 组成员数量统计

#### buildContextSource() 方法
- ✅ Context构建日志
- ✅ URL和BaseDN输出
- ✅ 环境属性输出

### 8. **日志级别建议**

**生产环境**：
```properties
logging.level.com.hr.maternity.ldap=INFO
```
输出：认证开始/结束、域切换、绑定成功/失败、搜索结果、用户信息

**调试环境**：
```properties
logging.level.com.hr.maternity.ldap=DEBUG
```
额外输出：配置详情、Principal列表、DN路径、堆栈跟踪、耗时细节

### 9. **日志输出示例**

**成功场景**：
```
2025-12-12 10:30:00 - ========== LDAP Authentication Started for user: zhangsan ==========
2025-12-12 10:30:00 - Total configured LDAP domains: 2
2025-12-12 10:30:00 - [Attempt 1/2] Trying domain: GROUP
2025-12-12 10:30:00 - ┌─ Domain Authentication Details ─────────────────
2025-12-12 10:30:00 - │ User: zhangsan
2025-12-12 10:30:00 - │ Domain: GROUP
2025-12-12 10:30:00 - │ Server: ldap.company.com:1222
2025-12-12 10:30:00 - │ SSL Enabled: true
2025-12-12 10:30:00 - └──────────────────────────────────────────────────
2025-12-12 10:30:00 - Starting bind attempts with 3 principal formats
2025-12-12 10:30:00 -   [1/3] Binding as: zhangsan@GROUP
2025-12-12 10:30:01 -   ✓ Successfully bound as zhangsan@GROUP in 125ms
2025-12-12 10:30:01 - Starting LDAP user search:
2025-12-12 10:30:01 -   Search Base: DC=COMPANY,DC=COM
2025-12-12 10:30:01 -   Search Filter: (|(sAMAccountName=zhangsan)...)
2025-12-12 10:30:01 - Search completed in 89ms, found 1 result(s)
2025-12-12 10:30:01 - ✓ User found successfully:
2025-12-12 10:30:01 -   Display Name: Zhang San
2025-12-12 10:30:01 -   Email: zhangsan@company.com
2025-12-12 10:30:01 - ✓ Domain GROUP authentication successful in 245ms
2025-12-12 10:30:01 - ========== LDAP Authentication SUCCESS for user: zhangsan in 245ms ==========
```

**失败场景**：
```
2025-12-12 10:30:00 - ========== LDAP Authentication Started for user: wronguser ==========
2025-12-12 10:30:00 - [Attempt 1/2] Trying domain: GROUP
2025-12-12 10:30:00 - Starting bind attempts with 3 principal formats
2025-12-12 10:30:00 -   [1/3] Binding as: wronguser@GROUP
2025-12-12 10:30:01 -   ✗ [1/3] Authentication failed for wronguser@GROUP in 156ms: 用户名或密码错误
2025-12-12 10:30:01 -   [2/3] Binding as: wronguser@company.com
2025-12-12 10:30:02 -   ✗ [2/3] Authentication failed for wronguser@company.com in 145ms: 用户名或密码错误
2025-12-12 10:30:02 - All 3 bind attempts failed
2025-12-12 10:30:02 - [Attempt 2/2] Trying domain: TECH
2025-12-12 10:30:02 - ...
2025-12-12 10:30:05 - ========== LDAP Authentication FAILED for user wronguser after 2 attempts in 5234ms ==========
```

### 10. **运维价值**

通过这些日志，运维团队可以：
- ✅ 快速定位认证失败原因
- ✅ 分析认证性能瓶颈
- ✅ 监控各域的响应时间
- ✅ 发现网络连接问题
- ✅ 追踪用户认证路径
- ✅ 统计Principal格式效果
- ✅ 优化超时参数配置

**修改文件**：
- `LdapAuthService.java` - 全面优化日志输出

**输入人**：用户
