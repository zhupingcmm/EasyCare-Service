package com.hr.maternity.strategy.impl.allowance;

import com.hr.maternity.calculator.PayrollDayCalculator;
import com.hr.maternity.domain.HolidayInfo;
import com.hr.maternity.domain.MonthlyWageInfo;
import com.hr.maternity.domain.MonthlyWorkdayInfoDO;
import com.hr.maternity.domain.RefundCalculationContext;
import com.hr.maternity.domain.RefundCalculationResult;
import com.hr.maternity.dto.AllowanceRulesResponse;
import com.hr.maternity.dto.CompanyAdvanceMap;
import com.hr.maternity.dto.MaternityAllowanceRequest;
import com.hr.maternity.dto.MaternityAllowanceResponse;
import com.hr.maternity.enums.AddDeleteItemEnum;
import com.hr.maternity.enums.CityEnum;
import com.hr.maternity.service.AllowanceRulesService;
import com.hr.maternity.service.CityService;
import com.hr.maternity.service.HolidayService;
import com.hr.maternity.service.MaternityWageCalculatorService;
import com.hr.maternity.service.RequestDateCompensationService;
import com.hr.maternity.service.WorkdayCalculatorService;
import com.hr.maternity.strategy.MaternityAllowanceStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.hr.maternity.constant.AllowanceRuleConstants.PAYOUT_METHOD_CORPORATE;
import static com.hr.maternity.constant.AllowanceRuleConstants.PAYOUT_METHOD_INDIVIDUAL;

/**
 * 深圳市生育津贴计算策略实现
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BaseMaternityAllowanceStrategy implements MaternityAllowanceStrategy {

    private final MaternityWageCalculatorService maternityWageCalculatorService;
    private final CityService cityService;
    private final AllowanceRulesService allowanceRulesService;
    private final WorkdayCalculatorService workdayCalculatorService;
    private final RequestDateCompensationService requestDateCompensationService;
    private final HolidayService holidayService;
    @Override
    public MaternityAllowanceResponse calculateMaternityAllowance(MaternityAllowanceRequest request) {

        String cityName = cityService.getEnabledCityChineseName(request.getCityCode());
        AllowanceRulesResponse allowanceRules = allowanceRulesService.getEnabledAllowanceRulesByCity(cityName);
        if (allowanceRules == null) {
            throw new IllegalArgumentException("不支持的城市代码: " + request.getCityCode());
        }

        // 提前初始化上下文，避免重复查询节假日数据
        RefundCalculationContext context = initializeContext(request, allowanceRules);
        
        BigDecimal paidWageInMaternity = getMaternityWage(request, context);
        if(isIndividual (allowanceRules)){
            validateRequest( request);
            paidWageInMaternity = null;
        }

        // a. 单位申报上年度月均工资计算补贴金额
        BigDecimal allowanceBasedCorporateSalary = getAllowanceBasedCorporateSalary(request,allowanceRules.getMonthDays());
        // b. 员工产前12个月月均工资计算补贴金额
        BigDecimal allowanceBasedEmployeeSalary = getAllowanceBasedEmployeeSalary(request,allowanceRules.getMonthDays());
        // c. 政府发放补贴金额
        BigDecimal governmentAllowance = request.getGovernmentAllowance() != null ?
                request.getGovernmentAllowance() : BigDecimal.ZERO;
        // d. 员工应享受补贴：MAX(a,b,c)
        BigDecimal maxAllowance = allowanceBasedCorporateSalary
                .max(allowanceBasedEmployeeSalary)
                .max(governmentAllowance);

        //  e. 补差 = 计算补差
        BigDecimal compensationAmount = BigDecimal.ZERO;
        if(isIndividual(allowanceRules)){
            compensationAmount = maxAllowance.subtract(governmentAllowance);
        }
        else {
            compensationAmount = maxAllowance.subtract(paidWageInMaternity);
        }

        // 构建详细说明列表
        List<String> allowanceCompensationDetails = new ArrayList<>();
        // 添加产假工资信息
        if (isCorporate(allowanceRules) ) {
            allowanceCompensationDetails.add(String.format("产假期间发放工资：%.2f元", paidWageInMaternity));
        }

        allowanceCompensationDetails.add(String.format("单位申报上年度月均工资计算补贴金额：%.2f÷%.2f天×%d=%.2f元",
                request.getUnitMonthlyAverageSalary(),
                allowanceBasedCorporateSalary,
                request.getMaternityLeaveDays(),
                allowanceBasedCorporateSalary));

        allowanceCompensationDetails.add(String.format("员工产前12个月月均工资计算补贴金额：%.2f元÷%.2f天×%d天=%.2f元",
                request.getAverageSalaryPast12Months(),
                allowanceRules.getMonthDays(),
                request.getMaternityLeaveDays(),
                allowanceBasedEmployeeSalary));

        allowanceCompensationDetails.add(String.format("员工应享受补贴：%.2f元", maxAllowance));

        allowanceCompensationDetails.add(String.format("政府发放补贴金额：%.2f元", governmentAllowance));

        if(isIndividual(allowanceRules)) {
            allowanceCompensationDetails.add(String.format("补差金额：员工应享受补贴%.2f元-政府发放补贴金额%.2f元=%.2f元",
                    maxAllowance, governmentAllowance, compensationAmount));
        }
        if(isCorporate(allowanceRules)) {
            allowanceCompensationDetails.add(String.format("补差金额：员工应享受补贴%.2f元-产假期间发放工资%.2f元=%.2f元",
                    maxAllowance, paidWageInMaternity, compensationAmount));
        }
        // 如果补差金额为负数，则返回0
        BigDecimal finalCompensationAmount = compensationAmount.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : compensationAmount;

        MaternityAllowanceResponse response =
                MaternityAllowanceResponse.builder()
                .requestId(null) // 由Service层设置
                .resultId(null) // 由Service层设置
                .lanId(request.getLanId())
                .employeeName(request.getEmployeeName())
                .cityCode(request.getCityCode())
                .cityName(cityName)
                .allowanceDays(request.getMaternityLeaveDays())
                .extraAllowance(BigDecimal.ZERO) // 额外补贴暂设为0
                .maternityAllowance(maxAllowance) // 应享受补贴金额
                .compensationAmount(finalCompensationAmount) // 补差金额
                .paidMaternityWage(paidWageInMaternity) // 产假应付工资
                .employeeRefundAmount(null)
                .allowanceCompensationDetails(allowanceCompensationDetails)
                .refundDetails(null)
                .build();
        if(isCorporate(allowanceRules)){
            return response;
        }

        //发放到个人账户，需要计算返还
        return reFund(request, allowanceRules, response, context);
    }

    private MaternityAllowanceResponse reFund(MaternityAllowanceRequest request, AllowanceRulesResponse allowanceRules, MaternityAllowanceResponse response, RefundCalculationContext context){
        
        log.info("开始计算返还金额，员工：{}", request.getLanId());
        
        // 1. 计算月度工资信息
        MonthlyWageInfo monthlyWageInfo = calculateMonthlyWages(request, context);
        
        // 2. 计算返还金额
        RefundCalculationResult refundResult = calculateRefundAmount(
            request, allowanceRules, context, monthlyWageInfo);
        
        // 3. 生成返还详情
        List<String> refundDetails = generateRefundDetails(
            request, allowanceRules, context, monthlyWageInfo, refundResult);
        
        // 4. 设置响应
        response.setEmployeeRefundAmount(refundResult.getTotalRefund());
        response.setRefundDetails(refundDetails);
        
        log.info("返还金额计算完成，总计：{}", refundResult.getTotalRefund());
        return response;
    }

    private BigDecimal getAllowanceBasedCorporateSalary(MaternityAllowanceRequest request,BigDecimal monthDays) {
        // a. 单位申报上年度月均工资计算补贴金额
        BigDecimal unitDeclaredAllowance = BigDecimal.ZERO;
        if (request.getUnitMonthlyAverageSalary() != null) {
            unitDeclaredAllowance = request.getUnitMonthlyAverageSalary()
                    .multiply(new BigDecimal(request.getMaternityLeaveDays()))
                    .divide(monthDays, 2, RoundingMode.HALF_UP);
        }
        return unitDeclaredAllowance;
    }

    private BigDecimal getMaternityWage(MaternityAllowanceRequest request, RefundCalculationContext context) {
        // 使用上下文中的月度工作日信息计算产假应付工资，避免重复查询数据库
        BigDecimal paidMaternityWage = BigDecimal.ZERO;
        if (request.getMonthlyBaseSalary() == null) {
            return paidMaternityWage;
        }
        
        List<MonthlyWorkdayInfoDO> monthlyWorkdayList = context.getMonthlyWorkdayList();
        
        for (MonthlyWorkdayInfoDO monthlyWorkday : monthlyWorkdayList) {
            BigDecimal monthlyWage;
            
            // 确定当月使用的基本工资（4月及之后使用调整后的工资）
            BigDecimal currentMonthBaseSalary = request.getMonthlyBaseSalary();
            if (request.getAdjustedMonthlyBaseSalary() != null && monthlyWorkday.getMonth() >= 4) {
                currentMonthBaseSalary = request.getAdjustedMonthlyBaseSalary();
            }

            if (monthlyWorkday.getFullMonth()) {
                // 完整自然月：直接使用月基本工资
                monthlyWage = currentMonthBaseSalary;
            } else {
                // 非完整月：按比例计算
                if (monthlyWorkday.getLegalPaydays() == null || monthlyWorkday.getLegalPaydays() == 0) {
                    log.warn("{}年{}月法定工作天数为0，跳过该月", 
                            monthlyWorkday.getYear(), monthlyWorkday.getMonth());
                    continue;
                }
                
                BigDecimal dailyWage = currentMonthBaseSalary.divide(
                        new BigDecimal(monthlyWorkday.getLegalPaydays()),
                        4, 
                        RoundingMode.HALF_UP);
                monthlyWage = dailyWage.multiply(new BigDecimal(monthlyWorkday.getPaydays()));
            }

            paidMaternityWage = paidMaternityWage.add(monthlyWage);
        }
        
        return paidMaternityWage.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal getAllowanceBasedEmployeeSalary(MaternityAllowanceRequest request, BigDecimal monthDays) {
        // 计算生育津贴：产前12个月月均工资 / 30 * 产假天数，四舍五入保留2位小数
        BigDecimal maternityAllowance = request.getAverageSalaryPast12Months()
                .multiply(new BigDecimal(request.getMaternityLeaveDays()))
                .divide(monthDays, 2, RoundingMode.HALF_UP);
        return maternityAllowance;
    }
    
    /**
     * 计算补差金额
     */
    private BigDecimal calculateCompensation(BigDecimal allowanceBasedEmployeeSalary, 
                                           BigDecimal governmentAllowance, 
                                           BigDecimal paidMaternityWage) {
        if (governmentAllowance == null) {
            governmentAllowance = BigDecimal.ZERO;
        }
        if (paidMaternityWage == null) {
            paidMaternityWage = BigDecimal.ZERO;
        }
        
        // 如果政府发放金额大于生育津贴，补差 = 政府发放金额 - 已发放产假期间工资
        if (governmentAllowance.compareTo(allowanceBasedEmployeeSalary) > 0) {
            return governmentAllowance.subtract(paidMaternityWage);
        } else {
            // 否则补差 = 生育津贴 - 已发放产假期间工资
            return allowanceBasedEmployeeSalary.subtract(paidMaternityWage);
        }
    }

    @Override
    public String getSupportedCityCode() {
        return  CityEnum.SHENZHEN.getCode();
    }

    private boolean isIndividual(AllowanceRulesResponse allowanceRules) {
        return allowanceRules.getPayoutMethod() == PAYOUT_METHOD_INDIVIDUAL;
    }
    private boolean isCorporate(AllowanceRulesResponse allowanceRules) {
        return allowanceRules.getPayoutMethod() == PAYOUT_METHOD_CORPORATE;
    }
    private void validateRequest(MaternityAllowanceRequest request) {
        // 验证上海特有的计算所需字段
        if (request.getMonthlyBaseSalary() == null) {
            throw new IllegalArgumentException("生育津贴计算需要提供月基本工资");
        }

        if ((request.getCompanyAdvance().getAddItem().get(AddDeleteItemEnum.SOCIAL_INSURANCE_BASE.getCode()) == null)) {
            throw new IllegalArgumentException("生育津贴计算需要提供社保基数总和");
        }
    }
    
    // ==================== 优化后的返还金额计算方法 ====================
    
    /**
     * 初始化计算上下文（一次性加载所有数据）
     */
    private RefundCalculationContext initializeContext(
            MaternityAllowanceRequest request,
            AllowanceRulesResponse allowanceRules) {
        
        LocalDate startDate = request.getMaternityLeaveStartDate();
        LocalDate endDate = request.getMaternityLeaveEndDate();
        
        log.info("初始化返还金额计算上下文，产假日期：{} 至 {}", startDate, endDate);
        
        // 一次性加载节假日数据（按日期范围）
        Map<LocalDate, HolidayInfo> holidayMap = 
            holidayService.getHolidaysByDateRange(startDate, endDate);
        
        // 创建计薪日计算器
        PayrollDayCalculator calculator = new PayrollDayCalculator(holidayMap);
        
        // 计算月度工作日信息
        List<MonthlyWorkdayInfoDO> monthlyWorkdayList = 
            workdayCalculatorService.calculateMonthlyWorkdaysWithHolidayMap(
                startDate, endDate, holidayMap);
        
        // 判断是否跨越调整月份
        boolean salaryAdjusted = maternityWageCalculatorService.crossesSalaryAdjustMonth(
            monthlyWorkdayList, allowanceRules.getSalaryAdjustMonth());
        boolean socialInsuranceAdjusted = maternityWageCalculatorService.crossesSocialAdjustMonth(
            monthlyWorkdayList, allowanceRules.getSocialAdjustMonth());
        
        log.debug("工资调整：{}，社保调整：{}", salaryAdjusted, socialInsuranceAdjusted);
        
        return RefundCalculationContext.builder()
            .holidayMap(holidayMap)
            .monthlyWorkdayList(monthlyWorkdayList)
            .payrollDayCalculator(calculator)
            .salaryAdjusted(salaryAdjusted)
            .socialInsuranceAdjusted(socialInsuranceAdjusted)
            .companyAdvance(request.getCompanyAdvance())
            .build();
    }
    
    /**
     * 计算月度工资信息
     */
    private MonthlyWageInfo calculateMonthlyWages(
            MaternityAllowanceRequest request,
            RefundCalculationContext context) {
        
        MonthlyWageInfo info = new MonthlyWageInfo();
        List<MonthlyWorkdayInfoDO> monthlyList = context.getMonthlyWorkdayList();
        
        if (monthlyList.isEmpty()) {
            return info;
        }
        
        // 判断首月是否为完整月
        boolean firstMonthFull = monthlyList.get(0).getFullMonth();
        boolean lastMonthFull = monthlyList.get(monthlyList.size() - 1).getFullMonth();
        
        info.setFirstMonthFull(firstMonthFull);
        info.setLastMonthFull(lastMonthFull);
        
        // 计算首月工资（如果不是完整月）
        if (!firstMonthFull) {
            // 使用上下文中的计薪日计算器，避免重复查询数据库
            LocalDate startDate = request.getMaternityLeaveStartDate();
            LocalDate startMonthEnd = startDate.withDayOfMonth(startDate.lengthOfMonth());
            LocalDate actualEndInStartMonth = request.getMaternityLeaveEndDate().isBefore(startMonthEnd) 
                ? request.getMaternityLeaveEndDate() : startMonthEnd;
            
            PayrollDayCalculator calculator = context.getPayrollDayCalculator();
            YearMonth startYearMonth = YearMonth.from(startDate);
            
            // 计算该月总计薪日
            int totalPayrollDays = calculator.calculateMonthPayrollDays(startYearMonth);
            // 计算请假期间的计薪日（从请假开始到月末）
            int maternityPayrollDays = calculator.calculatePayrollDays(startDate, actualEndInStartMonth);
            
            // 计算请假期间工资折算
            BigDecimal firstMonthMaternityWage = BigDecimal.ZERO;
            if (totalPayrollDays > 0) {
                BigDecimal ratio = new BigDecimal(maternityPayrollDays)
                    .divide(new BigDecimal(totalPayrollDays), 6, RoundingMode.HALF_UP);
                firstMonthMaternityWage = request.getMonthlyBaseSalary().multiply(ratio)
                    .setScale(2, RoundingMode.HALF_UP);
            }
            info.setFirstMonthMaternityWage(firstMonthMaternityWage);
            
            log.debug("首月工资折算：{}年{}月，总计薪日{}天，请假计薪日{}天，折算工资{}元",
                startYearMonth.getYear(), startYearMonth.getMonthValue(),
                totalPayrollDays, maternityPayrollDays, firstMonthMaternityWage);
            
            // 计算首月实际工资
            BigDecimal firstMonthWage = calculateMonthWage(
                request.getMonthlyBaseSalary(),
                firstMonthMaternityWage,
                context.getCompanyAdvance().getSocialInsuranceBase(),
                context.getCompanyAdvance().getEspp(),
                context.getCompanyAdvance().getUnionFee()
            );
            info.setFirstMonthWage(firstMonthWage);
        }
        
        // 计算尾月工资（如果不是完整月）
        if (!lastMonthFull) {
            BigDecimal adjustedSalary = context.isSalaryAdjusted() 
                && request.getAdjustedMonthlyBaseSalary() != null
                ? request.getAdjustedMonthlyBaseSalary()
                : request.getMonthlyBaseSalary();
            
            // 使用上下文中的计薪日计算器，避免重复查询数据库
            LocalDate endDate = request.getMaternityLeaveEndDate();
            LocalDate endMonthStart = endDate.withDayOfMonth(1);
            LocalDate actualStartInEndMonth = request.getMaternityLeaveStartDate().isAfter(endMonthStart)
                ? request.getMaternityLeaveStartDate() : endMonthStart;
            
            PayrollDayCalculator calculator = context.getPayrollDayCalculator();
            YearMonth endYearMonth = YearMonth.from(endDate);
            
            // 计算该月总计薪日
            int totalPayrollDays = calculator.calculateMonthPayrollDays(endYearMonth);
            // 计算请假期间的计薪日（从月初到请假结束）
            int maternityPayrollDays = calculator.calculatePayrollDays(actualStartInEndMonth, endDate);
            
            // 计算请假期间工资折算
            BigDecimal lastMonthMaternityWage = BigDecimal.ZERO;
            if (totalPayrollDays > 0) {
                BigDecimal ratio = new BigDecimal(maternityPayrollDays)
                    .divide(new BigDecimal(totalPayrollDays), 6, RoundingMode.HALF_UP);
                lastMonthMaternityWage = adjustedSalary.multiply(ratio)
                    .setScale(2, RoundingMode.HALF_UP);
            }
            info.setLastMonthMaternityWage(lastMonthMaternityWage);
            
            log.debug("尾月工资折算：{}年{}月，总计薪日{}天，请假计薪日{}天，折算工资{}元",
                endYearMonth.getYear(), endYearMonth.getMonthValue(),
                totalPayrollDays, maternityPayrollDays, lastMonthMaternityWage);
            
            // 计算尾月实际工资
            BigDecimal adjustedSocialInsurance = context.isSocialInsuranceAdjusted()
                ? context.getCompanyAdvance().getAdjustedSocialInsuranceBase()
                : context.getCompanyAdvance().getSocialInsuranceBase();
            
            BigDecimal lastMonthWage = calculateMonthWage(
                adjustedSalary,
                lastMonthMaternityWage,
                adjustedSocialInsurance,
                context.getCompanyAdvance().getEspp(),
                context.getCompanyAdvance().getUnionFee()
            );
            info.setLastMonthWage(lastMonthWage);
        }
        
        // 统计完整月份数
        long completeMonths = monthlyList.stream()
            .filter(MonthlyWorkdayInfoDO::getFullMonth)
            .count();
        info.setCompleteMonths(completeMonths);
        
        return info;
    }
    
    /**
     * 计算单月工资（统一处理）
     */
    private BigDecimal calculateMonthWage(
            BigDecimal baseSalary,
            BigDecimal maternityWageDeduction,
            BigDecimal socialInsurance,
            BigDecimal espp,
            BigDecimal unionFee) {
        
        BigDecimal actualWage = baseSalary.subtract(maternityWageDeduction);
        
        if (socialInsurance != null && socialInsurance.compareTo(BigDecimal.ZERO) > 0) {
            actualWage = actualWage.subtract(socialInsurance);
        }
        if (espp != null && espp.compareTo(BigDecimal.ZERO) > 0) {
            actualWage = actualWage.subtract(espp);
        }
        if (unionFee != null && unionFee.compareTo(BigDecimal.ZERO) > 0) {
            actualWage = actualWage.subtract(unionFee);
        }
        
        return actualWage;
    }
    
    /**
     * 计算返还金额
     */
    private RefundCalculationResult calculateRefundAmount(
            MaternityAllowanceRequest request,
            AllowanceRulesResponse allowanceRules,
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
            if (firstMonthWage != null && firstMonthWage.compareTo(BigDecimal.ZERO) < 0) {
                // 工资不够扣，需要返还
                totalRefund = totalRefund.add(firstMonthWage.abs());
                result.setFirstMonthShortfall(firstMonthWage.abs());
            }
        }
        
        // 3. 处理尾月工资情况
        if (!monthlyWageInfo.isLastMonthFull()) {
            BigDecimal lastMonthWage = monthlyWageInfo.getLastMonthWage();
            if (lastMonthWage != null) {
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
        }
        
        // 4. 处理申请日期补偿（使用配置的调整月份）
        Map<String, Object> requestDateCompensation = 
            requestDateCompensationService.calculateRequestDateCompensation(
                request.getMonthlyBaseSalary(),
                request.getAdjustedMonthlyBaseSalary(),
                request.getMaternityLeaveStartDate(),
                request.getMaternityLeaveRequestDate(),
                advance.getSocialInsuranceBase(),
                advance.getAdjustedSocialInsuranceBase(),
                advance.getEspp(),
                advance.getUnionFee(),
                allowanceRules.getSalaryAdjustMonth(),
                allowanceRules.getSocialAdjustMonth()
            );
        BigDecimal compensation = (BigDecimal) requestDateCompensation
            .getOrDefault("compensation", BigDecimal.ZERO);
        totalRefund = totalRefund.add(compensation);
        result.setRequestDateCompensation(compensation);
        
        // 5. 确保返还金额不为负
        result.setTotalRefund(totalRefund.compareTo(BigDecimal.ZERO) < 0 
            ? BigDecimal.ZERO : totalRefund);
        
        log.info("返还金额计算完成，总计：{}", result.getTotalRefund());
        return result;
    }
    
    /**
     * 生成返还详情
     */
    private List<String> generateRefundDetails(
            MaternityAllowanceRequest request,
            AllowanceRulesResponse allowanceRules,
            RefundCalculationContext context,
            MonthlyWageInfo monthlyWageInfo,
            RefundCalculationResult result) {
        
        List<String> refundDetailsList = new ArrayList<>();
        
        LocalDate startDate = request.getMaternityLeaveStartDate();
        LocalDate endDate = request.getMaternityLeaveEndDate();
        int startingYear = startDate.getYear();
        int startingMonth = startDate.getMonthValue();
        int endingYear = endDate.getYear();
        int endingMonth = endDate.getMonthValue();
        
        CompanyAdvanceMap advance = context.getCompanyAdvance();
        if (advance == null) {
            return refundDetailsList;
        }
        
        BigDecimal socialInsuranceBase = advance.getSocialInsuranceBase();
        BigDecimal adjustedSocialInsuranceBase = context.isSocialInsuranceAdjusted()
            ? advance.getAdjustedSocialInsuranceBase()
            : socialInsuranceBase;
        BigDecimal espp = advance.getEspp();
        BigDecimal unionFee = advance.getUnionFee();
        
        // 1. 首月详情
        if (!monthlyWageInfo.isFirstMonthFull() && monthlyWageInfo.getFirstMonthWage() != null) {
            BigDecimal firstMonthWage = monthlyWageInfo.getFirstMonthWage();
            BigDecimal firstMonthMaternityWage = monthlyWageInfo.getFirstMonthMaternityWage();
            
            StringBuilder wageFormula = new StringBuilder();
            wageFormula.append(String.format("%.2f-%.2f",
                request.getMonthlyBaseSalary(), firstMonthMaternityWage));
            
            if (socialInsuranceBase != null && socialInsuranceBase.compareTo(BigDecimal.ZERO) > 0) {
                wageFormula.append(String.format("-%.2f", socialInsuranceBase));
            }
            if (espp != null && espp.compareTo(BigDecimal.ZERO) > 0) {
                wageFormula.append(String.format("-%.2f", espp));
            }
            if (unionFee != null && unionFee.compareTo(BigDecimal.ZERO) > 0) {
                wageFormula.append(String.format("-%.2f", unionFee));
            }
            
            if (firstMonthWage.compareTo(BigDecimal.ZERO) < 0) {
                refundDetailsList.add(String.format("%d.%d 工资不够扣：%s=%.2f元",
                    startingYear, startingMonth, wageFormula, firstMonthWage.abs()));
            }
            refundDetailsList.add(String.format("产假工资折算 %d年%d月，扣除：%.2f元", 
                startingYear, startingMonth, firstMonthMaternityWage));
        }
        
        // 2. 尾月详情
        if (!monthlyWageInfo.isLastMonthFull() && monthlyWageInfo.getLastMonthWage() != null) {
            BigDecimal lastMonthWage = monthlyWageInfo.getLastMonthWage();
            BigDecimal lastMonthMaternityWage = monthlyWageInfo.getLastMonthMaternityWage();
            BigDecimal adjustedSalary = context.isSalaryAdjusted() 
                && request.getAdjustedMonthlyBaseSalary() != null
                ? request.getAdjustedMonthlyBaseSalary()
                : request.getMonthlyBaseSalary();
            
            StringBuilder wageFormula = new StringBuilder();
            wageFormula.append(String.format("%.2f-%.2f",
                adjustedSalary, lastMonthMaternityWage));
            
            if (adjustedSocialInsuranceBase != null && adjustedSocialInsuranceBase.compareTo(BigDecimal.ZERO) > 0) {
                wageFormula.append(String.format("-%.2f", adjustedSocialInsuranceBase));
            }
            if (espp != null && espp.compareTo(BigDecimal.ZERO) > 0) {
                wageFormula.append(String.format("-%.2f", espp));
            }
            if (unionFee != null && unionFee.compareTo(BigDecimal.ZERO) > 0) {
                wageFormula.append(String.format("-%.2f", unionFee));
            }
            
            if (lastMonthWage.compareTo(BigDecimal.ZERO) >= 0) {
                refundDetailsList.add(String.format("%d.%d 工资剩余：%s=%.2f元",
                    endingYear, endingMonth, wageFormula, lastMonthWage.abs()));
            } else {
                refundDetailsList.add(String.format("%d.%d 工资不够扣：%s=%.2f元",
                    endingYear, endingMonth, wageFormula, lastMonthWage.abs()));
            }
            refundDetailsList.add(String.format("产假工资折算 %d年%d月，扣除：%.2f元", 
                endingYear, endingMonth, lastMonthMaternityWage));
        }
        
        // 3. 完整月份详情（按月份详细列出）
        List<MonthlyWorkdayInfoDO> completeMonthsList = context.getMonthlyWorkdayList().stream()
            .filter(MonthlyWorkdayInfoDO::getFullMonth)
            .toList();
        
        // 3.1 社保公积金详情 - 按月份详细列出（只有完整月才显示）
        if (!completeMonthsList.isEmpty()) {
            if (socialInsuranceBase != null && socialInsuranceBase.compareTo(BigDecimal.ZERO) > 0) {
                // 如果有社保调整，需要分别显示调整前和调整后的月份
                if (context.isSocialInsuranceAdjusted() && adjustedSocialInsuranceBase != null) {
                    // 从配置获取社保调整月份
                    Integer socialAdjustMonth = allowanceRules.getSocialAdjustMonth();
                    
                    // 调整前的月份
                    List<MonthlyWorkdayInfoDO> beforeAdjustMonths = completeMonthsList.stream()
                        .filter(m -> {
                            int year = completeMonthsList.get(completeMonthsList.size() - 1).getYear();
                            return m.getYear() < year || (m.getYear() == year && m.getMonth() < socialAdjustMonth);
                        })
                        .toList();
                    
                    if (!beforeAdjustMonths.isEmpty()) {
                        MonthlyWorkdayInfoDO firstMonth = beforeAdjustMonths.get(0);
                        MonthlyWorkdayInfoDO lastMonth = beforeAdjustMonths.get(beforeAdjustMonths.size() - 1);
                        int monthCount = beforeAdjustMonths.size();
                        
                        refundDetailsList.add(String.format("%d.%d-%d.%d月社保公积金：%.2f×%d=%.2f元",
                            firstMonth.getYear(), firstMonth.getMonth(),
                            lastMonth.getYear(), lastMonth.getMonth(),
                            socialInsuranceBase, monthCount, 
                            socialInsuranceBase.multiply(new BigDecimal(monthCount))));
                    }
                    
                    // 调整后的月份
                    List<MonthlyWorkdayInfoDO> afterAdjustMonths = completeMonthsList.stream()
                        .filter(m -> {
                            int year = completeMonthsList.get(completeMonthsList.size() - 1).getYear();
                            return m.getYear() == year && m.getMonth() >= socialAdjustMonth;
                        })
                        .toList();
                    
                    if (!afterAdjustMonths.isEmpty()) {
                        MonthlyWorkdayInfoDO firstMonth = afterAdjustMonths.get(0);
                        MonthlyWorkdayInfoDO lastMonth = afterAdjustMonths.get(afterAdjustMonths.size() - 1);
                        int monthCount = afterAdjustMonths.size();
                        
                        refundDetailsList.add(String.format("%d.%d-%d.%d月社保公积金（调整后）：%.2f×%d=%.2f元",
                            firstMonth.getYear(), firstMonth.getMonth(),
                            lastMonth.getYear(), lastMonth.getMonth(),
                            adjustedSocialInsuranceBase, monthCount, 
                            adjustedSocialInsuranceBase.multiply(new BigDecimal(monthCount))));
                    }
                } else {
                    // 没有调整，统一显示
                    MonthlyWorkdayInfoDO firstMonth = completeMonthsList.get(0);
                    MonthlyWorkdayInfoDO lastMonth = completeMonthsList.get(completeMonthsList.size() - 1);
                    int monthCount = completeMonthsList.size();
                    
                    refundDetailsList.add(String.format("%d.%d-%d.%d月社保公积金：%.2f×%d=%.2f元",
                        firstMonth.getYear(), firstMonth.getMonth(),
                        lastMonth.getYear(), lastMonth.getMonth(),
                        socialInsuranceBase, monthCount, 
                        socialInsuranceBase.multiply(new BigDecimal(monthCount))));
                }
            }
            
            // 3.2 ESPP详情
            if (espp != null && espp.compareTo(BigDecimal.ZERO) > 0) {
                MonthlyWorkdayInfoDO firstMonth = completeMonthsList.get(0);
                MonthlyWorkdayInfoDO lastMonth = completeMonthsList.get(completeMonthsList.size() - 1);
                long completeMonths = monthlyWageInfo.getCompleteMonths();
                
                refundDetailsList.add(String.format("%d.%d-%d.%d月ESPP：%.2f×%d=%.2f元",
                    firstMonth.getYear(), firstMonth.getMonth(),
                    lastMonth.getYear(), lastMonth.getMonth(),
                    espp, completeMonths, espp.multiply(new BigDecimal(completeMonths))));
            }
            
            // 3.3 工会费详情
            if (unionFee != null && unionFee.compareTo(BigDecimal.ZERO) > 0) {
                MonthlyWorkdayInfoDO firstMonth = completeMonthsList.get(0);
                MonthlyWorkdayInfoDO lastMonth = completeMonthsList.get(completeMonthsList.size() - 1);
                long completeMonths = monthlyWageInfo.getCompleteMonths();
                
                refundDetailsList.add(String.format("%d.%d-%d.%d月工会费：%.2f×%d=%.2f元",
                    firstMonth.getYear(), firstMonth.getMonth(),
                    lastMonth.getYear(), lastMonth.getMonth(),
                    unionFee, completeMonths, unionFee.multiply(new BigDecimal(completeMonths))));
            }
        } else {
            // 没有完整月的情况（只有首月和/或尾月）
            // 仍然需要显示社保、ESPP、工会费的说明
            if (socialInsuranceBase != null && socialInsuranceBase.compareTo(BigDecimal.ZERO) > 0) {
                refundDetailsList.add(String.format("月度个人部分社保公积金：%.2f元", socialInsuranceBase));
            }
            if (context.isSocialInsuranceAdjusted() && adjustedSocialInsuranceBase != null 
                && adjustedSocialInsuranceBase.compareTo(BigDecimal.ZERO) > 0) {
                refundDetailsList.add(String.format("调整后月度个人部分社保公积金：%.2f元", adjustedSocialInsuranceBase));
            }
            if (espp != null && espp.compareTo(BigDecimal.ZERO) > 0) {
                refundDetailsList.add(String.format("月度ESPP：%.2f元", espp));
            }
            if (unionFee != null && unionFee.compareTo(BigDecimal.ZERO) > 0) {
                refundDetailsList.add(String.format("月度工会费：%.2f元", unionFee));
            }
        }
        
        // 5. 申请日期补偿详情
        if (result.getRequestDateCompensation() != null 
            && result.getRequestDateCompensation().compareTo(BigDecimal.ZERO) > 0) {
            // 使用优化版本的方法，传入 PayrollDayCalculator 避免重复查询数据库
            Map<String, Object> compensationResult = requestDateCompensationService.calculateRequestDateCompensationWithCalculator(
                request.getMonthlyBaseSalary(),
                request.getAdjustedMonthlyBaseSalary(),
                request.getMaternityLeaveStartDate(),
                request.getMaternityLeaveRequestDate(),
                socialInsuranceBase,
                adjustedSocialInsuranceBase,
                espp,
                unionFee,
                allowanceRules.getSalaryAdjustMonth(),
                allowanceRules.getSocialAdjustMonth(),
                context.getPayrollDayCalculator()
            );
            String compensationDetail = (String) compensationResult.getOrDefault("refundDetail", "");
            if (!compensationDetail.isEmpty()) {
                refundDetailsList.add(compensationDetail);
            }
        }
        
        // 6. 总计公式
        StringBuilder formula = new StringBuilder("返还金额：");
        boolean hasItems = false;
        
        if (socialInsuranceBase != null && socialInsuranceBase.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal socialTotal = advance.calculateSocialInsuranceBaseByMonth(
                socialInsuranceBase, context.getMonthlyWorkdayList());
            formula.append(String.format("%.2f", socialTotal));
            hasItems = true;
        }
        
        if (espp != null && espp.compareTo(BigDecimal.ZERO) > 0) {
            if (hasItems) formula.append("+");
            formula.append(String.format("%.2f", espp.multiply(new BigDecimal(monthlyWageInfo.getCompleteMonths()))));
            hasItems = true;
        }
        
        if (unionFee != null && unionFee.compareTo(BigDecimal.ZERO) > 0) {
            if (hasItems) formula.append("+");
            formula.append(String.format("%.2f", unionFee.multiply(new BigDecimal(monthlyWageInfo.getCompleteMonths()))));
            hasItems = true;
        }
        
        // 添加其他增加项
        if (advance.getAddItem() != null) {
            for (Map.Entry<String, BigDecimal> entry : advance.getAddItem().entrySet()) {
                if (!entry.getKey().equalsIgnoreCase(AddDeleteItemEnum.ESPP.getCode())
                    && !entry.getKey().equals(AddDeleteItemEnum.UNION_FEE.getCode())
                    && !entry.getKey().equals(AddDeleteItemEnum.SOCIAL_INSURANCE_BASE.getCode())
                    && !entry.getKey().equals(AddDeleteItemEnum.ADJUSTED_SOCIAL_INSURANCE_BASE.getCode())) {
                    if (hasItems) formula.append("+");
                    formula.append(String.format("%.2f", entry.getValue()));
                    hasItems = true;
                }
            }
        }
        
        // 减去删除项
        if (advance.getDeleteItem() != null) {
            for (Map.Entry<String, BigDecimal> entry : advance.getDeleteItem().entrySet()) {
                formula.append("-").append(String.format("%.2f", entry.getValue()));
            }
        }
        
        // 处理尾月工资
        if (!monthlyWageInfo.isLastMonthFull() && monthlyWageInfo.getLastMonthWage() != null) {
            BigDecimal lastMonthWage = monthlyWageInfo.getLastMonthWage();
            if (lastMonthWage.compareTo(BigDecimal.ZERO) > 0) {
                formula.append("-").append(String.format("%.2f", lastMonthWage));
            } else if (lastMonthWage.compareTo(BigDecimal.ZERO) < 0) {
                formula.append("+").append(String.format("%.2f", lastMonthWage.abs()));
            }
        }
        
        // 处理首月工资折算
        if (!monthlyWageInfo.isFirstMonthFull() && monthlyWageInfo.getFirstMonthMaternityWage() != null) {
            formula.append("+").append(String.format("%.2f", monthlyWageInfo.getFirstMonthMaternityWage().abs()));
        }
        
        formula.append("=").append(String.format("%.2f元", result.getTotalRefund()));
        if (result.getTotalRefund().compareTo(BigDecimal.ZERO) < 0) {
            formula.append("（计算结果为负，取0）");
        }
        
        refundDetailsList.add(formula.toString());
        
        return refundDetailsList;
    }

}

