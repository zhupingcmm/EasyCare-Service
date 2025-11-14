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
