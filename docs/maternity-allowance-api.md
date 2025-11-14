# 生育津贴接口文档（统一返回包装）

- 控制器：`src/main/java/com/hr/maternity/controller/MaternityAllowanceController.java`
- 服务实现：`src/main/java/com/hr/maternity/service/impl/MaternityAllowanceServiceImpl.java`
- 统一返回包装：`com.hr.maternity.common.ApiResponse`、`GlobalResponseAdvice`、`GlobalExceptionHandler`
- 城市策略：`src/main/java/com/hr/maternity/strategy/impl/allowance/`
  - 示例：`ShanghaiMaternityAllowanceStrategy`、`ShenzhenMaternityAllowanceStrategy`、`GuangzhouMaternityAllowanceStrategy` 等

> 命名与响应结构遵循项目命名规范：实体 DO 结尾、Controller 复数 REST 路径、PostgreSQL 表名与字段小写下划线、API 响应统一 `{ code, message, data }`。

## 接口概览

| 项 | 值 |
|---|---|
| 方法 | POST |
| 路径 | /api/maternity-allowance/calculate |
| 描述 | 根据城市与个人信息计算生育津贴 |
| 控制器方法 | `MaternityAllowanceController.calculateMaternityAllowance()` |
| 请求体 | `MaternityAllowanceRequest`（JSON） |
| 返回体（统一包装） | `{ code, message, data: MaternityAllowanceResponse }` |

## 请求参数（MaternityAllowanceRequest）

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| lanId | string | 是 | 工号（已统一字段名为 lanId） |
| employeeName | string | 是 | 姓名 |
| cityCode | string | 是 | 城市代码（DB 驱动，如 SH、SZ、GZ、BJ 等） |
| maternityLeaveDays | int | 是 | 享受生育津贴天数 |
| maternityLeaveStartDate | string(date) | 是 | 产假开始时间，格式：YYYY-MM-DD（ISO-8601） |
| maternityLeaveEndDate | string(date) | 是 | 产假结束时间，格式：YYYY-MM-DD（ISO-8601）；不得早于开始时间 |
| unitMonthlyAverageSalary | number | 否 | 单位申报的上年度月平均工资（可为 0） |
| monthlyBaseSalary | number | 否 | 月基本工资（可选，可为 0） |
| adjustedMonthlyBaseSalary | number | 否 | 调整后月基本工资（用于跨 4 月后当年调整，系统自动判断是否需要，提供即参与计算） |
| averageSalaryPast12Months | number | 是 | 员工产前 12 个月月均工资（可为 0） |
| companyAdvance | object | 否 | 公司垫付信息Map结构（上海用于补差扣减，包含社保缴费基数） |
| companyAdvanceSum | number | 否 | 公司垫付总额（与companyAdvance二选一，不能同时使用） |
| governmentAllowance | number | 是 | 政府发放补贴金额（可为 0） |
| isMultipleBirth | boolean | 否 | 是否多胞胎（默认 false） |

### companyAdvance 子字段（Map结构）

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| addItem | Map<String, BigDecimal> | 否 | 增加项目的Map，key为项目名称，value为金额 |
| deleteItem | Map<String, BigDecimal> | 否 | 删除项目的Map，key为项目名称，value为金额 |

#### addItem Map 常见项目示例
- "socialInsuranceBase": 社保缴费基数（每月金额）
- "adjustedSocialInsuranceBase": 调整后社保缴费基数（每月金额，跨 7 月后的月份使用）
- "espp": 员工持股计划金额（每月金额）
- "unionFee": 工会费金额（每月金额）
- "flexibleBenefit": 弹性福利金额
- 其他自定义项目名称

#### deleteItem Map 常见项目示例
- "spotOn": Spot On金额
- 其他自定义项目名称

> **项目键名规范**：建议使用 `AddDeleteItemEnum` 中定义的标准键名（驼峰命名），以确保系统能正确识别和处理特殊项目（如上海的社保缴费基数、ESPP和工会费按月计算逻辑）。

> 控制器与服务层会根据 `cityCode` 选择并执行对应城市策略计算，城市清单来源于数据库（已移除 `CityEnum`）。
>
> 金额相关字段（`unitMonthlyAverageSalary`、`monthlyBaseSalary`、`adjustedMonthlyBaseSalary`、`averageSalaryPast12Months`、`companyAdvanceSum`、`governmentAllowance`）均允许为 0。社保缴费基数通过 `companyAdvance.addItem.socialInsuranceBase` 与 `companyAdvance.addItem.adjustedSocialInsuranceBase` 提供（如需）。
>
> **重要更新**：产假应付工资通过 `MaternityWageCalculatorService` 基于 `monthlyBaseSalary`、`maternityLeaveStartDate`、`maternityLeaveEndDate` 和实际工作日自动计算得出。
>
> 校验规则：`maternityLeaveEndDate` 不得早于 `maternityLeaveStartDate`；且 `maternityLeaveDays` 必须等于开始与结束日期之间的自然日数（含首尾）。

> 自动调整说明：系统会基于产假期间的月份范围自动判断是否跨过关键调整月份。
> - 跨 4 月：视为月基本工资当年调整生效点。若跨越，结束月等处将使用 `adjustedMonthlyBaseSalary` 参与计算（需提供）。
> - 跨 7 月：视为社保缴费基数当年调整生效点。若跨越，完整月份中 7 月及之后使用 `adjustedSocialInsuranceBase`，之前使用 `socialInsuranceBase`。
> 判断逻辑在服务层以公共方法实现（例如：`crossesApril(...)`、`crossesJuly(...)`）。

## 返回数据（MaternityAllowanceResponse in data）

| 字段 | 类型 | 说明 |
|---|---|---|
| lanId | string | 工号 |
| employeeName | string | 姓名 |
| cityCode | string | 城市代码 |
| cityName | string | 城市名称（由城市库/服务层填充） |
| allowanceDays | int | 享受津贴天数 |
| extraAllowance | number | 额外补贴（若有） |
| maternityAllowance | number | 生育津贴金额（最终结果） |
| compensationAmount | number | 补差金额（策略计算得出） |
| employeeRefundAmount | number | 员工返还金额 |
| allowanceCompensationDetails | array/string | 津贴补差计算详情（支持数组或字符串格式） |
| refundDetails | array/string | 返还计算详情（支持数组或字符串格式） |

## 城市规则（要点）

### 上海（SH）

- 基础：
  - a = 单位申报上年度月均工资 ÷ 30 × 享受生育津贴天数
  - b = 员工产前12个月月均工资 ÷ 30 × 享受生育津贴天数
  - c = 政府发放补贴金额
  - d = MAX(a, b, c) = 员工能享受到的补贴（上海）
  - 补差 = d - c
- 返还金额计算（特殊处理社保缴费基数、ESPP和工会费）：
  - 完整月份数按 `WorkdayCalculatorService.calculateMonthlyWorkdays` 中 `fullMonth=true` 的月份统计。
  - 社保缴费基数：若跨 7 月，7 月之前使用 `socialInsuranceBase`，7 月及之后使用 `adjustedSocialInsuranceBase`；否则按 `socialInsuranceBase × 完整月数`。
  - ESPP、工会费：按完整月份数累乘。
  - 公司垫付净额 = addItem 总和（含上述按月逻辑） - deleteItem 总和。
  - 结束月应发工资 = 当月基本工资（如跨 4 月则用调整后） - 产假期间工资 - 个人社保公积金 - ESPP - 工会费（可能为负）。
  - 返还金额 = 公司垫付净额 - 结束月应发工资 + 开始月工资不够扣时的差额；最终若为负则取 0。
- 支持的addItem项目键名：
  - 社保缴费基数: "socialInsuranceBase"（驼峰命名）
  - ESPP: "espp"（驼峰命名）
  - 工会费: "unionFee"（驼峰命名）
  - 弹性福利: "flexibleBenefit"（驼峰命名）
  - 其他项目: 自定义名称
- 返回：
  - maternityAllowance = d（员工应享受补贴）
  - compensationAmount = d - c（补差金额）
  - employeeRefundAmount = 返还金额
  - allowanceCompensationDetails 展示津贴计算步骤
  - refundDetails 展示返还金额计算步骤（包含社保缴费基数、ESPP和工会费的详细计算）
- 实现：`ShanghaiMaternityAllowanceStrategy.calculateMaternityAllowance()`（已按新公式实现，空值做 0，HALF_UP）

### 深圳（SZ）

- 基础：
  - 生育津贴 = 产前12个月月均工资 ÷ 30 × 产假天数（四舍五入到 2 位）
- 补差：
  - 若政府发放金额 > 生育津贴：补差 = 政府发放金额 − 已发放产假期间工资
  - 否则：补差 = 生育津贴 − 已发放产假期间工资
- 返回：`calculationDetails` 含详细补差逻辑说明
- 实现：`ShenzhenMaternityAllowanceStrategy`

### 广州（GZ）

- 基础：
  - 生育津贴 = 产前12个月月均工资 ÷ 30 × 产假天数（四舍五入到 2 位）
- 产假应付工资计算：
  - 通过 `MaternityWageCalculatorService` 基于 `monthlyBaseSalary`、产假期间和实际工作日计算
- 补差：
  - 若政府发放金额 > 生育津贴：补差 = 政府发放金额 − 产假应付工资
  - 否则：补差 = 生育津贴 − 产假应付工资
- 返回：`calculationDetails` 含详细补差逻辑说明，包括产假应付工资计算结果
- 实现：`GuangzhouMaternityAllowanceStrategy`（已集成 `MaternityWageCalculatorService`）

### 苏州（SU）

- 基础：
  - 生育津贴 = 产前12个月月均工资 × 产假天数 ÷ 30（四舍五入到 2 位）
- 产假应付工资计算：
  - 通过 `MaternityWageCalculatorService` 基于 `monthlyBaseSalary`、产假期间和实际工作日计算
- 补差：
  - 若政府发放金额 > 生育津贴：补差 = 政府发放金额 − 产假应付工资
  - 否则：补差 = 生育津贴 − 产假应付工资
- 返回：`calculationDetails` 含详细补差逻辑说明，包括产假应付工资计算结果
- 实现：`SuzhouMaternityAllowanceStrategy`（已集成 `MaternityWageCalculatorService`）

> 其他城市请参考目录 `strategy/impl/allowance/` 的具体实现。若地方法规更新，请以策略实现为准。

## 统一错误返回

| 场景 | HTTP | 响应体（统一包装） |
|---|---|---|
| 参数校验失败（@Valid） | 400 | `{ "code": 400, "message": "字段错误1; 字段错误2", "data": null }` |
| 非法参数（业务异常） | 422 | `{ "code": 422, "message": "不支持的城市代码: XXX", "data": null }` |
| 服务器内部错误 | 500 | `{ "code": 500, "message": "服务器内部错误", "data": null }` |

## 示例

### 上海请求示例
```json
{
    "lanId": "A2345678",
    "employeeName": "1212",
    "cityCode": "SH",
    "maternityLeaveDays": 173,
    "maternityLeaveStartDate": "2024-11-01",
    "maternityLeaveEndDate": "2025-04-26",
    "averageSalaryPast12Months": 21821.79,
    "governmentAllowance": 212911.1,
    "paidMaternityWage": 23240.8,
    "unitMonthlyAverageSalary": 49753.8,
    "monthlyBaseSalary": 20000,
    "companyAdvance": {
        "addItem": {
            "socialInsuranceBase": 4648.16,
            "flexibleBenefit": 305,
            "unionFee": 50
        },
        "deleteItem": {
            "spotOn": 100
        }
    }
}
```

### 深圳请求示例
```json
{
  "lanId": "A12345",
  "employeeName": "李四",
  "cityCode": "SZ",
  "maternityLeaveDays": 178,
  "maternityLeaveStartDate": "2025-09-15",
  "maternityLeaveEndDate": "2026-03-11",
  "averageSalaryPast12Months": 18000,
  "governmentAllowance": 28000,
  "monthlyBaseSalary": 18000
}
```

### 广州请求示例
```json
{
  "lanId": "A12346",
  "employeeName": "王五",
  "cityCode": "GZ",
  "maternityLeaveDays": 178,
  "maternityLeaveStartDate": "2025-08-20",
  "maternityLeaveEndDate": "2026-02-13",
  "averageSalaryPast12Months": 20000,
  "governmentAllowance": 32000,
  "monthlyBaseSalary": 20000
}
```

### 苏州请求示例
```json
{
  "lanId": "A12347",
  "employeeName": "赵六",
  "cityCode": "SU",
  "maternityLeaveDays": 158,
  "maternityLeaveStartDate": "2025-10-10",
  "maternityLeaveEndDate": "2026-03-16",
  "averageSalaryPast12Months": 19000,
  "governmentAllowance": 25000,
  "monthlyBaseSalary": 19000
}
```

### 上海成功响应示例
```json
{
    "code": 0,
    "message": "OK",
    "data": {
        "lanId": "A2345678",
        "employeeName": "1212",
        "cityCode": "SH",
        "cityName": "上海",
        "allowanceDays": 173,
        "extraAllowance": 0,
        "maternityAllowance": 286913.58,
        "compensationAmount": 74002.48,
        "employeeRefundAmount": 24915.70,
        "allowanceCompensationDetails": [
            "单位申报上年度月均工资计算补贴金额：49753.80元÷30天×173天=286913.58元",
            "员工产前12个月月均工资计算补贴金额：21821.79元÷30天×173天=125838.47元",
            "政府发放补贴金额：212911.10元",
            "员工应享受补贴：286913.58元",
            "补差金额：286913.58元-212911.10元=74002.48元"
        ],
        "refundDetails": [
            "2025.4 工资不够扣：20000.00元-16521.74元-4648.16元-50.00元=1219.90元",
            "产假工资折算 2025年4月，扣除：16521.74元",
            "月度个人部分社保公积金合计：4648.16元",
            "2024.11-2025.3月工会费：50.00元×5=250.00元",
            "返还金额：23240.80+250.00+305.00元-100.00元+1219.90元=24915.70元"
        ]
    }
}
```

### 广州成功响应示例
```json
{
  "code": 0,
  "message": "OK",
  "data": {
    "lanId": "A12346",
    "employeeName": "王五",
    "cityCode": "GZ",
    "cityName": "广州市",
    "allowanceDays": 178,
    "extraAllowance": 0.00,
    "maternityAllowance": 35600.00,
    "compensationAmount": 17600.00,
    "paidMaternityWage": 18000.00,
    "allowanceCompensationDetails": "广州市生育津贴计算：产前12个月月均工资(20000.00) ÷ 30 × 产假天数(178) = 35600.00元；产假应付工资 = 18000.00元；补差计算：生育津贴(35600.00) - 产假应付工资(18000.00) = 17600.00元"
  }
}
```

### 苏州成功响应示例
```json
{
  "code": 0,
  "message": "OK",
  "data": {
    "lanId": "A12347",
    "employeeName": "赵六",
    "cityCode": "SU",
    "cityName": "苏州市",
    "allowanceDays": 158,
    "extraAllowance": 0.00,
    "maternityAllowance": 31733.33,
    "compensationAmount": 9000.00,
    "paidMaternityWage": 16000.00,
    "calculationDetails": "苏州市生育津贴计算：产前12个月月均工资(19000.00) ÷ 30 × 产假天数(158) = 31733.33元；产假应付工资 = 16000.00元；补差计算：政府发放金额(25000.00) - 产假应付工资(16000.00) = 9000.00元"
  }
}
```

### 失败响应（示例：不支持城市）
```json
{
  "code": 422,
  "message": "不支持的城市代码: XX",
  "data": null
}
```

## 调用说明

- Header：`Content-Type: application/json`
- URL：`POST /api/maternity-allowance/calculate`
- 统一返回由 `GlobalResponseAdvice` 自动包装；控制器可直接返回业务对象或 `ResponseEntity`，Advice 会封装为 `{ code, message, data }`。
