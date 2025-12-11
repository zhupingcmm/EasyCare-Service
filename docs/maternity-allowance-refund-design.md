# 产假津贴返还金额计算设计文档

## 文档信息
- **创建日期**: 2024-12-10
- **版本**: v2.0
- **作者**: Cascade AI
- **相关模块**: 产假津贴计算 - 个人账户返还金额计算

---

## 1. 业务背景

### 1.1 业务场景
当津贴发放到个人账户时（`isIndividual(allowanceRules) = true`），产假期间不发放工资，但员工需要个人支付以下费用：
- 社保公积金（个人部分）
- ESPP（员工股票购买计划）
- 工会费
- 其他费用（如有）

由于产假期间没有工资发放，这些费用无法从工资中扣除，因此需要计算并返还给公司。

### 1.2 核心计算逻辑

#### 1.2.1 产假首月工资计算
```
首月工资 = 基本工资 - (基本工资 / 当月计薪日总天数 × 请假天数) - 社保公积金 - ESPP - 工会费 - 其他
```
- 如果 < 0：表示工资不够扣除，需要返还
- 如果 ≥ 0：无需返还

#### 1.2.2 产假尾月工资计算
```
尾月工资 = 基本工资 - (基本工资 / 当月计薪日总天数 × 请假天数) - 社保公积金 - ESPP - 工会费 - 其他
```
- 如果 < 0：表示工资不够扣除，需要返还
- 如果 ≥ 0：无需返还

**特殊情况**：如果产假首月或尾月全部请假（没有工作日），则按中间月处理。

#### 1.2.3 产假中间月返还金额
```
中间月返还金额 = Σ(社保公积金 + ESPP + 工会费 + 其他费用)
```
按月累加每个完整月份的费用。

#### 1.2.4 工资和社保调整
- **工资调整**：需要判断是否跨越工资调整月份（配置在 `allowanceRules.salaryAdjustMonth`），如果有调整，对应月份使用调整后的基本工资
- **社保调整**：需要判断是否跨越社保调整月份（配置在 `allowanceRules.socialAdjustMonth`），如果有调整，对应月份使用调整后的社保基数

---

## 2. 现有代码分析

### 2.1 核心类职责

#### 2.1.1 BaseMaternityAllowanceStrategy
**职责**：产假津贴计算策略实现类
- 计算津贴金额
- 计算补差金额
- 计算返还金额（`refund` 方法）

**主要方法**：
- `calculateMaternityAllowance()`: 计算产假津贴总入口
- `reFund()`: 计算返还金额
- `getAllowanceBasedCorporateSalary()`: 基于单位申报工资计算津贴
- `getAllowanceBasedEmployeeSalary()`: 基于员工工资计算津贴

#### 2.1.2 WorkdayCalculatorServiceImpl
**职责**：工作日计算服务
- 计算月度工作日信息
- 计算指定日期范围的工作日天数
- 计算发薪日天数

**主要方法**：
- `calculateMonthlyWorkdays()`: 计算每月工作日信息
- `countWorkdaysInRange()`: 计算日期范围内的工作日天数
- `calculatePayrollDaysInMonth()`: 计算月度发薪日天数
- `calculatePayrollDaysInRange()`: 计算日期范围内的发薪日天数

**存在问题**：
1. 多次调用数据库获取节假日信息（按年读取）
2. 每次计算都重新加载节假日数据
3. 缺乏缓存机制

#### 2.1.3 HolidayServiceImpl
**职责**：节假日数据服务
- 管理节假日数据
- 从第三方API获取节假日信息
- 节假日数据的增删改查

**主要方法**：
- `getPublicHolidays()`: 获取指定年份的节假日数据
- `initHoliday()`: 初始化节假日数据
- `fetchHolidaysFromApi()`: 从第三方API获取数据

**存在问题**：
1. 按年读取节假日，无法按日期范围读取
2. 缺少按日期范围查询的API

#### 2.1.4 RequestDateCompensationServiceImpl
**职责**：产假申请日期补偿计算
- 处理产假申请日期晚于产假开始日期的补偿计算

**主要方法**：
- `calculateRequestDateCompensation()`: 计算申请日期补偿

**存在问题**：
1. 硬编码了工资调整月份为4月、社保调整月份为7月
2. 逻辑复杂，分支过多

---

## 3. 优化设计方案

### 3.1 整体架构优化

#### 3.1.1 数据加载优化
**原则**：每次调用 `refund` 只读取一次数据库

**实现方案**：
1. 在 `refund` 方法开始时，一次性加载所有需要的数据
2. 将数据封装为上下文对象，传递给各个计算方法
3. 避免在计算过程中重复查询数据库

```java
public class RefundCalculationContext {
    // 节假日数据（按日期范围加载）
    private Map<LocalDate, HolidayInfo> holidayMap;
    
    // 月度工作日信息
    private List<MonthlyWorkdayInfoDO> monthlyWorkdayList;
    
    // 工资调整信息
    private SalaryAdjustmentInfo salaryAdjustment;
    
    // 社保调整信息
    private SocialInsuranceAdjustmentInfo socialInsuranceAdjustment;
    
    // 公司垫付信息
    private CompanyAdvanceMap companyAdvance;
}
```

#### 3.1.2 节假日API优化

**新增API**：`GET /api/support/holidays?start={startDate}&end={endDate}`

**请求参数**：
- `start`: 开始日期（格式：yyyy-MM-dd）
- `end`: 结束日期（格式：yyyy-MM-dd）

**响应格式**：
```json
{
    "code": 0,
    "message": "OK",
    "data": [
        {
            "date": "2024-11-01",
            "name": "元旦",
            "isPublicHoliday": true,
            "type": "public_holiday",
            "name_cn": "元旦",
            "name_en": "New Year's Day"
        }
    ]
}
```

**SQL实现**：
```sql
SELECT * FROM t_special_day 
WHERE date BETWEEN :startDate AND :endDate 
ORDER BY date
```

### 3.2 日期计算优化

#### 3.2.1 统一的日期范围处理

**设计原则**：统一处理首月、尾月和同月场景

**实现方案**：
```java
public class DateRangeCalculator {
    
    /**
     * 计算月度请假范围
     * 自动处理首月、尾月、同月场景
     */
    public MonthLeaveRange calculateMonthLeaveRange(
            LocalDate leaveStartDate, 
            LocalDate leaveEndDate, 
            YearMonth targetMonth) {
        
        LocalDate monthStart = targetMonth.atDay(1);
        LocalDate monthEnd = targetMonth.atEndOfMonth();
        
        // 计算该月的请假开始和结束日期
        LocalDate rangeStart = leaveStartDate.isBefore(monthStart) 
            ? monthStart : leaveStartDate;
        LocalDate rangeEnd = leaveEndDate.isAfter(monthEnd) 
            ? monthEnd : leaveEndDate;
        
        return new MonthLeaveRange(rangeStart, rangeEnd);
    }
}
```

#### 3.2.2 工作日和计薪日计算

**计薪日规则**：
1. 排除周六、周日（非调休日）
2. 排除节假日（`type=public_holiday`）
3. 包含调休工作日（`type=transfer_workday`）
4. 包含法定假日但需计薪的日期（`isPublicHoliday=true && type=public_holiday`）

**实现方案**：
```java
public class PayrollDayCalculator {
    
    private final Map<LocalDate, HolidayInfo> holidayMap;
    
    /**
     * 计算日期范围内的计薪日天数
     */
    public int calculatePayrollDays(LocalDate start, LocalDate end) {
        int count = 0;
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            if (isPayrollDay(date)) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * 判断是否为计薪日
     */
    private boolean isPayrollDay(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        boolean isWeekend = (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY);
        
        HolidayInfo holiday = holidayMap.get(date);
        
        if (holiday == null) {
            // 无节假日信息，按周末规则
            return !isWeekend;
        }
        
        // 调休工作日：即使是周末也是计薪日
        if ("transfer_workday".equals(holiday.getType())) {
            return true;
        }
        
        // 法定假日且需计薪
        if (holiday.getIsPublicHoliday() && "public_holiday".equals(holiday.getType())) {
            return true;
        }
        
        // 普通节假日：不是计薪日
        if ("public_holiday".equals(holiday.getType())) {
            return false;
        }
        
        // 其他情况按周末规则
        return !isWeekend;
    }
    
    /**
     * 计算请假天数（工作日）
     */
    public int calculateLeaveDays(LocalDate start, LocalDate end) {
        int count = 0;
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            if (isWorkday(date)) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * 判断是否为工作日
     */
    private boolean isWorkday(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        boolean isWeekend = (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY);
        
        HolidayInfo holiday = holidayMap.get(date);
        
        if (holiday == null) {
            return !isWeekend;
        }
        
        // 调休工作日：即使是周末也是工作日
        if ("transfer_workday".equals(holiday.getType())) {
            return true;
        }
        
        // 节假日：不是工作日
        if ("public_holiday".equals(holiday.getType())) {
            return false;
        }
        
        return !isWeekend;
    }
}
```

### 3.3 refund 方法重构

#### 3.3.1 重构后的整体流程

```java
public MaternityAllowanceResponse refund(
        MaternityAllowanceRequest request,
        AllowanceRulesResponse allowanceRules,
        MaternityAllowanceResponse response) {
    
    // 1. 初始化计算上下文（一次性加载所有数据）
    RefundCalculationContext context = initializeContext(request, allowanceRules);
    
    // 2. 计算月度工资信息
    MonthlyWageInfo monthlyWageInfo = calculateMonthlyWages(request, context);
    
    // 3. 计算返还金额
    RefundCalculationResult result = calculateRefundAmount(
        request, context, monthlyWageInfo);
    
    // 4. 生成返还详情
    List<String> refundDetails = generateRefundDetails(
        request, context, monthlyWageInfo, result);
    
    // 5. 设置响应
    response.setEmployeeRefundAmount(result.getTotalRefund());
    response.setRefundDetails(refundDetails);
    
    return response;
}
```

#### 3.3.2 上下文初始化

```java
private RefundCalculationContext initializeContext(
        MaternityAllowanceRequest request,
        AllowanceRulesResponse allowanceRules) {
    
    LocalDate startDate = request.getMaternityLeaveStartDate();
    LocalDate endDate = request.getMaternityLeaveEndDate();
    
    // 一次性加载节假日数据（按日期范围）
    Map<LocalDate, HolidayInfo> holidayMap = 
        holidayService.getHolidaysByDateRange(startDate, endDate);
    
    // 计算月度工作日信息
    PayrollDayCalculator calculator = new PayrollDayCalculator(holidayMap);
    List<MonthlyWorkdayInfoDO> monthlyWorkdayList = 
        calculateMonthlyWorkdaysWithContext(startDate, endDate, calculator);
    
    // 判断是否跨越调整月份
    boolean salaryAdjusted = crossesAdjustMonth(
        monthlyWorkdayList, allowanceRules.getSalaryAdjustMonth());
    boolean socialInsuranceAdjusted = crossesAdjustMonth(
        monthlyWorkdayList, allowanceRules.getSocialAdjustMonth());
    
    return RefundCalculationContext.builder()
        .holidayMap(holidayMap)
        .monthlyWorkdayList(monthlyWorkdayList)
        .payrollDayCalculator(calculator)
        .salaryAdjusted(salaryAdjusted)
        .socialInsuranceAdjusted(socialInsuranceAdjusted)
        .companyAdvance(request.getCompanyAdvance())
        .build();
}
```

#### 3.3.3 月度工资计算

```java
private MonthlyWageInfo calculateMonthlyWages(
        MaternityAllowanceRequest request,
        RefundCalculationContext context) {
    
    MonthlyWageInfo info = new MonthlyWageInfo();
    
    List<MonthlyWorkdayInfoDO> monthlyList = context.getMonthlyWorkdayList();
    
    // 判断首月是否为完整月
    boolean firstMonthFull = monthlyList.get(0).getFullMonth();
    boolean lastMonthFull = monthlyList.get(monthlyList.size() - 1).getFullMonth();
    
    // 计算首月工资（如果不是完整月）
    if (!firstMonthFull) {
        BigDecimal firstMonthWage = calculateMonthWage(
            request.getMaternityLeaveStartDate(),
            request.getMaternityLeaveStartDate().withDayOfMonth(
                request.getMaternityLeaveStartDate().lengthOfMonth()),
            request.getMonthlyBaseSalary(),
            context,
            false // 使用原始社保基数
        );
        info.setFirstMonthWage(firstMonthWage);
        info.setFirstMonthFull(false);
    } else {
        info.setFirstMonthFull(true);
    }
    
    // 计算尾月工资（如果不是完整月）
    if (!lastMonthFull) {
        BigDecimal adjustedSalary = context.isSalaryAdjusted() 
            && request.getAdjustedMonthlyBaseSalary() != null
            ? request.getAdjustedMonthlyBaseSalary()
            : request.getMonthlyBaseSalary();
            
        BigDecimal lastMonthWage = calculateMonthWage(
            request.getMaternityLeaveEndDate().withDayOfMonth(1),
            request.getMaternityLeaveEndDate(),
            adjustedSalary,
            context,
            context.isSocialInsuranceAdjusted() // 使用调整后的社保基数
        );
        info.setLastMonthWage(lastMonthWage);
        info.setLastMonthFull(false);
    } else {
        info.setLastMonthFull(true);
    }
    
    // 统计完整月份数
    long completeMonths = monthlyList.stream()
        .filter(MonthlyWorkdayInfoDO::getFullMonth)
        .count();
    info.setCompleteMonths(completeMonths);
    
    return info;
}
```

#### 3.3.4 单月工资计算（统一处理）

```java
private BigDecimal calculateMonthWage(
        LocalDate rangeStart,
        LocalDate rangeEnd,
        BigDecimal baseSalary,
        RefundCalculationContext context,
        boolean useAdjustedSocialInsurance) {
    
    PayrollDayCalculator calculator = context.getPayrollDayCalculator();
    
    // 计算当月请假天数（工作日）
    int leaveDays = calculator.calculateLeaveDays(rangeStart, rangeEnd);
    
    // 计算当月计薪总天数
    YearMonth yearMonth = YearMonth.from(rangeStart);
    int totalPayrollDays = calculator.calculatePayrollDays(
        yearMonth.atDay(1), yearMonth.atEndOfMonth());
    
    // 计算产假工资折算
    BigDecimal maternityWageDeduction = baseSalary
        .multiply(BigDecimal.valueOf(leaveDays))
        .divide(BigDecimal.valueOf(totalPayrollDays), 2, RoundingMode.HALF_UP);
    
    // 计算实际工资
    BigDecimal actualWage = baseSalary.subtract(maternityWageDeduction);
    
    // 扣除社保公积金
    CompanyAdvanceMap advance = context.getCompanyAdvance();
    if (advance != null) {
        BigDecimal socialInsurance = useAdjustedSocialInsurance
            ? advance.getAdjustedSocialInsuranceBase()
            : advance.getSocialInsuranceBase();
        actualWage = actualWage.subtract(socialInsurance);
        
        // 扣除ESPP
        actualWage = actualWage.subtract(advance.getEspp());
        
        // 扣除工会费
        actualWage = actualWage.subtract(advance.getUnionFee());
        
        // 扣除其他费用（addItem中除了已知项的其他项）
        BigDecimal otherDeductions = calculateOtherDeductions(advance);
        actualWage = actualWage.subtract(otherDeductions);
        
        // 加上deleteItem中的项
        BigDecimal additions = calculateAdditions(advance);
        actualWage = actualWage.add(additions);
    }
    
    return actualWage;
}
```

#### 3.3.5 返还金额计算

```java
private RefundCalculationResult calculateRefundAmount(
        MaternityAllowanceRequest request,
        RefundCalculationContext context,
        MonthlyWageInfo monthlyWageInfo) {
    
    RefundCalculationResult result = new RefundCalculationResult();
    BigDecimal totalRefund = BigDecimal.ZERO;
    
    CompanyAdvanceMap advance = context.getCompanyAdvance();
    if (advance == null) {
        result.setTotalRefund(BigDecimal.ZERO);
        return result;
    }
    
    // 1. 计算完整月份的返还金额
    BigDecimal completeMonthsRefund = advance
        .calculateNetCompanyAdvanceWithMonthlyLogic(
            context.getMonthlyWorkdayList(), 
            context.isSocialInsuranceAdjusted());
    totalRefund = totalRefund.add(completeMonthsRefund);
    result.setCompleteMonthsRefund(completeMonthsRefund);
    
    // 2. 处理首月工资不足的情况
    if (!monthlyWageInfo.isFirstMonthFull()) {
        BigDecimal firstMonthWage = monthlyWageInfo.getFirstMonthWage();
        if (firstMonthWage.compareTo(BigDecimal.ZERO) < 0) {
            // 工资不够扣，需要返还
            totalRefund = totalRefund.add(firstMonthWage.abs());
            result.setFirstMonthShortfall(firstMonthWage.abs());
        }
    }
    
    // 3. 处理尾月工资不足的情况
    if (!monthlyWageInfo.isLastMonthFull()) {
        BigDecimal lastMonthWage = monthlyWageInfo.getLastMonthWage();
        if (lastMonthWage.compareTo(BigDecimal.ZERO) < 0) {
            // 工资不够扣，需要返还
            totalRefund = totalRefund.add(lastMonthWage.abs());
            result.setLastMonthShortfall(lastMonthWage.abs());
        } else {
            // 工资有剩余，需要从返还金额中扣除
            totalRefund = totalRefund.subtract(lastMonthWage);
            result.setLastMonthSurplus(lastMonthWage);
        }
    }
    
    // 4. 处理申请日期补偿
    Map<String, Object> requestDateCompensation = 
        requestDateCompensationService.calculateRequestDateCompensation(
            request.getMonthlyBaseSalary(),
            request.getAdjustedMonthlyBaseSalary(),
            request.getMaternityLeaveStartDate(),
            request.getMaternityLeaveRequestDate(),
            advance.getSocialInsuranceBase(),
            advance.getAdjustedSocialInsuranceBase(),
            advance.getEspp(),
            advance.getUnionFee()
        );
    BigDecimal compensation = (BigDecimal) requestDateCompensation
        .getOrDefault("compensation", BigDecimal.ZERO);
    totalRefund = totalRefund.add(compensation);
    result.setRequestDateCompensation(compensation);
    
    // 5. 确保返还金额不为负
    result.setTotalRefund(totalRefund.compareTo(BigDecimal.ZERO) < 0 
        ? BigDecimal.ZERO : totalRefund);
    
    return result;
}
```

---

## 4. 新增类设计

### 4.1 RefundCalculationContext

```java
@Data
@Builder
public class RefundCalculationContext {
    /** 节假日数据映射 */
    private Map<LocalDate, HolidayInfo> holidayMap;
    
    /** 月度工作日信息列表 */
    private List<MonthlyWorkdayInfoDO> monthlyWorkdayList;
    
    /** 计薪日计算器 */
    private PayrollDayCalculator payrollDayCalculator;
    
    /** 是否跨越工资调整月份 */
    private boolean salaryAdjusted;
    
    /** 是否跨越社保调整月份 */
    private boolean socialInsuranceAdjusted;
    
    /** 公司垫付信息 */
    private CompanyAdvanceMap companyAdvance;
}
```

### 4.2 HolidayInfo

```java
@Data
@Builder
public class HolidayInfo {
    /** 日期 */
    private LocalDate date;
    
    /** 节假日名称 */
    private String name;
    
    /** 是否为法定假日 */
    private Boolean isPublicHoliday;
    
    /** 类型：public_holiday 或 transfer_workday */
    private String type;
}
```

### 4.3 MonthlyWageInfo

```java
@Data
@Builder
public class MonthlyWageInfo {
    /** 首月工资（扣除各项后） */
    private BigDecimal firstMonthWage;
    
    /** 首月是否为完整月 */
    private boolean firstMonthFull;
    
    /** 尾月工资（扣除各项后） */
    private BigDecimal lastMonthWage;
    
    /** 尾月是否为完整月 */
    private boolean lastMonthFull;
    
    /** 完整月份数 */
    private long completeMonths;
}
```

### 4.4 RefundCalculationResult

```java
@Data
@Builder
public class RefundCalculationResult {
    /** 总返还金额 */
    private BigDecimal totalRefund;
    
    /** 完整月份返还金额 */
    private BigDecimal completeMonthsRefund;
    
    /** 首月工资不足金额 */
    private BigDecimal firstMonthShortfall;
    
    /** 尾月工资不足金额 */
    private BigDecimal lastMonthShortfall;
    
    /** 尾月工资剩余金额 */
    private BigDecimal lastMonthSurplus;
    
    /** 申请日期补偿金额 */
    private BigDecimal requestDateCompensation;
}
```

### 4.5 PayrollDayCalculator

```java
@RequiredArgsConstructor
public class PayrollDayCalculator {
    
    private final Map<LocalDate, HolidayInfo> holidayMap;
    
    /**
     * 计算日期范围内的计薪日天数
     */
    public int calculatePayrollDays(LocalDate start, LocalDate end) {
        // 实现见 3.2.2 节
    }
    
    /**
     * 判断是否为计薪日
     */
    private boolean isPayrollDay(LocalDate date) {
        // 实现见 3.2.2 节
    }
    
    /**
     * 计算请假天数（工作日）
     */
    public int calculateLeaveDays(LocalDate start, LocalDate end) {
        // 实现见 3.2.2 节
    }
    
    /**
     * 判断是否为工作日
     */
    private boolean isWorkday(LocalDate date) {
        // 实现见 3.2.2 节
    }
}
```

---

## 5. HolidayService 重构

### 5.1 新增方法

```java
public interface HolidayService {
    
    /**
     * 按日期范围获取节假日数据
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 节假日数据映射（日期 -> 节假日信息）
     */
    Map<LocalDate, HolidayInfo> getHolidaysByDateRange(
        LocalDate startDate, LocalDate endDate);
}
```

### 5.2 实现

```java
@Override
public Map<LocalDate, HolidayInfo> getHolidaysByDateRange(
        LocalDate startDate, LocalDate endDate) {
    
    log.info("获取日期范围内的节假日数据: {} 到 {}", startDate, endDate);
    
    // 从数据库查询
    List<Holiday> holidays = holidayRepository
        .findByDateBetweenOrderByDate(startDate, endDate);
    
    // 转换为Map
    Map<LocalDate, HolidayInfo> holidayMap = holidays.stream()
        .collect(Collectors.toMap(
            Holiday::getDate,
            this::convertToHolidayInfo
        ));
    
    log.info("获取到{}条节假日数据", holidayMap.size());
    return holidayMap;
}

private HolidayInfo convertToHolidayInfo(Holiday holiday) {
    return HolidayInfo.builder()
        .date(holiday.getDate())
        .name(holiday.getName())
        .isPublicHoliday(holiday.getIsPublicHoliday())
        .type(resolveHolidayType(holiday))
        .build();
}
```

### 5.3 Repository 新增方法

```java
public interface HolidayRepository extends JpaRepository<Holiday, UUID> {
    
    /**
     * 按日期范围查询节假日
     */
    List<Holiday> findByDateBetweenOrderByDate(
        LocalDate startDate, LocalDate endDate);
}
```

---

## 6. WorkdayCalculatorService 重构

### 6.1 重构后的接口

```java
public interface WorkdayCalculatorService {
    
    /**
     * 计算月度工作日信息（使用提供的节假日数据）
     * @param start 开始日期
     * @param end 结束日期
     * @param holidayMap 节假日数据映射
     * @return 月度工作日信息列表
     */
    List<MonthlyWorkdayInfoDO> calculateMonthlyWorkdays(
        LocalDate start, 
        LocalDate end,
        Map<LocalDate, HolidayInfo> holidayMap);
    
    /**
     * 创建计薪日计算器
     * @param holidayMap 节假日数据映射
     * @return 计薪日计算器
     */
    PayrollDayCalculator createPayrollDayCalculator(
        Map<LocalDate, HolidayInfo> holidayMap);
}
```

### 6.2 实现优化

```java
@Override
public List<MonthlyWorkdayInfoDO> calculateMonthlyWorkdays(
        LocalDate start, 
        LocalDate end,
        Map<LocalDate, HolidayInfo> holidayMap) {
    
    if (start == null || end == null) {
        throw new IllegalArgumentException("开始/结束日期不能为空");
    }
    if (end.isBefore(start)) {
        throw new IllegalArgumentException("结束日期不能早于开始日期");
    }
    
    PayrollDayCalculator calculator = new PayrollDayCalculator(holidayMap);
    List<MonthlyWorkdayInfoDO> result = new ArrayList<>();
    
    LocalDate cursor = start;
    while (!cursor.isAfter(end)) {
        YearMonth ym = YearMonth.from(cursor);
        
        // 当月范围的开始与结束（裁剪到 [start, end] 范围内）
        LocalDate monthStart = ym.atDay(1);
        LocalDate monthEnd = ym.atEndOfMonth();
        LocalDate rangeStart = monthStart.isBefore(start) ? start : monthStart;
        LocalDate rangeEnd = monthEnd.isAfter(end) ? end : monthEnd;
        
        // 计算工作日和计薪日
        int workdays = calculator.calculateLeaveDays(rangeStart, rangeEnd);
        int legalWorkdays = calculator.calculateLeaveDays(monthStart, monthEnd);
        int paydays = calculator.calculatePayrollDays(rangeStart, rangeEnd);
        int legalPaydays = calculator.calculatePayrollDays(monthStart, monthEnd);
        
        boolean fullMonth = !monthStart.isBefore(start) && !monthEnd.isAfter(end);
        
        result.add(MonthlyWorkdayInfoDO.builder()
            .year(ym.getYear())
            .month(ym.getMonthValue())
            .workdays(workdays)
            .legalWorkdays(legalWorkdays)
            .paydays(paydays)
            .legalPaydays(legalPaydays)
            .fullMonth(fullMonth)
            .build());
        
        cursor = monthEnd.plusDays(1);
    }
    
    return result;
}

@Override
public PayrollDayCalculator createPayrollDayCalculator(
        Map<LocalDate, HolidayInfo> holidayMap) {
    return new PayrollDayCalculator(holidayMap);
}
```

---

## 7. RequestDateCompensationService 优化

### 7.1 移除硬编码

**问题**：
- 工资调整月份硬编码为4月
- 社保调整月份硬编码为7月

**解决方案**：
从 `AllowanceRulesResponse` 中获取调整月份配置

### 7.2 优化后的方法签名

```java
public interface RequestDateCompensationService {
    
    /**
     * 计算产假申请日期补偿
     * @param monthlyBaseSalary 月基本工资
     * @param adjustedMonthlyBaseSalary 调整后月基本工资
     * @param maternityLeaveStartDate 产假开始日期
     * @param maternityLeaveRequestDate 产假申请日期
     * @param socialInsuranceBase 社保基数
     * @param adjustedSocialInsuranceBase 调整后社保基数
     * @param espp ESPP金额
     * @param unionFee 工会费
     * @param salaryAdjustMonth 工资调整月份
     * @param socialAdjustMonth 社保调整月份
     * @return 补偿结果（包含补偿金额和详情）
     */
    Map<String, Object> calculateRequestDateCompensation(
        BigDecimal monthlyBaseSalary,
        BigDecimal adjustedMonthlyBaseSalary,
        LocalDate maternityLeaveStartDate,
        LocalDate maternityLeaveRequestDate,
        BigDecimal socialInsuranceBase,
        BigDecimal adjustedSocialInsuranceBase,
        BigDecimal espp,
        BigDecimal unionFee,
        Integer salaryAdjustMonth,
        Integer socialAdjustMonth
    );
}
```

---

## 8. API设计

### 8.1 新增节假日范围查询API

**接口路径**：`GET /api/support/holidays`

**请求参数**：
| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| start | String | 是 | 开始日期 | 2024-11-01 |
| end | String | 是 | 结束日期 | 2025-04-25 |

**响应示例**：
```json
{
    "code": 0,
    "message": "OK",
    "data": [
        {
            "date": "2024-11-01",
            "name": "元旦",
            "isPublicHoliday": true,
            "type": "public_holiday",
            "name_cn": "元旦",
            "name_en": "New Year's Day"
        },
        {
            "date": "2024-11-02",
            "name": "春节补班",
            "isPublicHoliday": false,
            "type": "transfer_workday",
            "name_cn": "春节补班",
            "name_en": "Spring Festival Workday"
        }
    ]
}
```

### 8.2 Controller实现

```java
@RestController
@RequestMapping("/api/support")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "节假日支持")
public class HolidaySupportController {
    
    private final HolidayService holidayService;
    
    @GetMapping("/holidays")
    @Operation(summary = "按日期范围查询节假日")
    public ApiResponse<List<Map<String, Object>>> getHolidaysByDateRange(
            @RequestParam @Parameter(description = "开始日期", example = "2024-11-01") 
            String start,
            @RequestParam @Parameter(description = "结束日期", example = "2025-04-25") 
            String end) {
        
        log.info("查询节假日，开始日期: {}, 结束日期: {}", start, end);
        
        LocalDate startDate = LocalDate.parse(start);
        LocalDate endDate = LocalDate.parse(end);
        
        Map<LocalDate, HolidayInfo> holidayMap = 
            holidayService.getHolidaysByDateRange(startDate, endDate);
        
        List<Map<String, Object>> result = holidayMap.values().stream()
            .map(this::convertToMap)
            .sorted(Comparator.comparing(m -> (String) m.get("date")))
            .collect(Collectors.toList());
        
        return ApiResponse.success(result);
    }
    
    private Map<String, Object> convertToMap(HolidayInfo info) {
        Map<String, Object> map = new HashMap<>();
        map.put("date", info.getDate().toString());
        map.put("name", info.getName());
        map.put("isPublicHoliday", info.getIsPublicHoliday());
        map.put("type", info.getType());
        map.put("name_cn", info.getName());
        map.put("name_en", info.getName());
        return map;
    }
}
```

---

## 9. 计算示例

### 9.1 示例数据

**请求数据**：
```json
{
    "requestId": 3,
    "lanId": "A2345678",
    "employeeName": "张三",
    "cityCode": "SH",
    "maternityLeaveDays": 173,
    "maternityLeaveStartDate": "2024-11-06",
    "maternityLeaveEndDate": "2025-04-26",
    "monthlyBaseSalary": 20000,
    "adjustedMonthlyBaseSalary": 20000,
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

### 9.2 计算过程

#### 9.2.1 首月（2024年11月）

**日期范围**：2024-11-06 至 2024-11-30

**计算步骤**：
1. 计算请假天数（工作日）：17天
2. 计算当月计薪总天数：21天
3. 产假工资折算：20000 ÷ 21 × 17 = 17142.86元
4. 实际工资：20000 - 17142.86 - 4648.16 - 50 = -1841.02元
5. 工资不够扣，需要返还：1841.02元

**详情输出**：
```
2024.11 工资不够扣：20000.00-17142.86-4648.16-50.00=-1841.02元
产假工资折算 2024年11月，扣除：17142.86元
```

#### 9.2.2 中间月（2024年12月 至 2025年3月）

**完整月份数**：4个月

**每月返还**：
- 社保公积金：4648.16元
- 工会费：50元
- 弹性福利：305元
- 减去SpotOn：-100元

**月度返还**：4648.16 + 50 + 305 - 100 = 4903.16元
**4个月总计**：4903.16 × 4 = 19612.64元

#### 9.2.3 尾月（2025年4月）

**日期范围**：2025-04-01 至 2025-04-26

**计算步骤**：
1. 计算请假天数（工作日）：19天
2. 计算当月计薪总天数：23天
3. 产假工资折算：20000 ÷ 23 × 19 = 16521.74元
4. 实际工资：20000 - 16521.74 - 4648.16 - 50 = -1219.90元
5. 工资不够扣，需要返还：1219.90元

**详情输出**：
```
2025.4 工资不够扣：20000.00-16521.74-4648.16-50.00=-1219.90元
产假工资折算 2025年4月，扣除：16521.74元
```

#### 9.2.4 总返还金额

```
总返还 = 首月不足 + 中间月返还 + 尾月不足
       = 1841.02 + 19612.64 + 1219.90
       = 22673.56元
```

**返还详情输出**：
```
返还金额：18592.64+200.00+305.00-100.00+1219.90+1841.02=22058.56元
```

---

## 10. 优化总结

### 10.1 性能优化
1. **数据库查询优化**：从多次按年查询改为一次按日期范围查询
2. **数据复用**：一次性加载所有数据，避免重复查询
3. **计算优化**：统一日期范围处理逻辑，减少重复计算

### 10.2 代码质量优化
1. **职责分离**：将日期计算、工资计算、返还计算分离到不同的类
2. **消除硬编码**：将调整月份配置化
3. **统一处理**：首月、尾月、同月场景使用统一的计算逻辑
4. **可读性提升**：使用上下文对象传递数据，减少参数传递

### 10.3 可维护性优化
1. **清晰的数据流**：通过上下文对象明确数据依赖
2. **单一职责**：每个类只负责一个特定的计算任务
3. **易于测试**：各个计算方法独立，便于单元测试
4. **易于扩展**：新增费用项只需修改配置，无需修改代码逻辑

---

## 11. 实施计划

### 11.1 第一阶段：基础设施
1. 新增 `HolidayInfo` 类
2. 新增 `PayrollDayCalculator` 类
3. 实现节假日按日期范围查询API
4. 实现 `HolidayRepository.findByDateBetweenOrderByDate()` 方法

### 11.2 第二阶段：核心重构
1. 新增 `RefundCalculationContext` 类
2. 新增 `MonthlyWageInfo` 类
3. 新增 `RefundCalculationResult` 类
4. 重构 `WorkdayCalculatorService`
5. 重构 `RequestDateCompensationService`

### 11.3 第三阶段：业务逻辑重构
1. 重构 `BaseMaternityAllowanceStrategy.refund()` 方法
2. 实现统一的月度工资计算逻辑
3. 实现统一的返还金额计算逻辑
4. 实现返还详情生成逻辑

### 11.4 第四阶段：测试与验证
1. 单元测试
2. 集成测试
3. 业务场景测试
4. 性能测试

---

## 12. 附录

### 12.1 关键术语

| 术语 | 说明 |
|------|------|
| 计薪日 | 用于计算工资的天数，包括工作日和法定假日 |
| 工作日 | 实际需要工作的天数，排除周末和节假日 |
| 完整月 | 整个自然月都在产假范围内的月份 |
| 首月 | 产假开始的月份 |
| 尾月 | 产假结束的月份 |
| 中间月 | 首月和尾月之间的完整月份 |

### 12.2 配置项说明

| 配置项 | 说明 | 示例值 |
|--------|------|--------|
| salaryAdjustMonth | 工资调整月份 | 4（4月） |
| socialAdjustMonth | 社保调整月份 | 7（7月） |
| monthDays | 计算日薪的月天数 | 30 |
| payoutMethod | 发放方式 | 1=公司账户，2=个人账户 |

### 12.3 数据库表结构

#### t_special_day（节假日表）
| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | UUID | 主键 |
| date | DATE | 日期 |
| year | INTEGER | 年份 |
| region | VARCHAR | 地区代码 |
| name | VARCHAR | 节假日名称 |
| type | INTEGER | 类型：1=节假日，2=调休工作日 |
| is_public_holiday | BOOLEAN | 是否为法定假日 |
| enabled | BOOLEAN | 是否启用 |

**索引**：
- `idx_date`: (date)
- `idx_year_region`: (year, region)
- `idx_date_range`: (date, enabled)

---

**文档结束**
