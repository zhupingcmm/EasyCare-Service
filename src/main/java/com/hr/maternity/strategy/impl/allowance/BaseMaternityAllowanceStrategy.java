package com.hr.maternity.strategy.impl.allowance;

import com.hr.maternity.dto.AllowanceRulesResponse;
import com.hr.maternity.dto.MaternityAllowanceRequest;
import com.hr.maternity.dto.MaternityAllowanceResponse;
import com.hr.maternity.enums.CityEnum;
import com.hr.maternity.service.AllowanceRulesService;
import com.hr.maternity.service.CityService;
import com.hr.maternity.service.MaternityWageCalculatorService;
import com.hr.maternity.strategy.MaternityAllowanceStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.hr.maternity.constant.AllowanceRuleConstants.PAYOUT_METHOD_INDIVIDUAL;

/**
 * 深圳市生育津贴计算策略实现
 */
@Component
@RequiredArgsConstructor
public class BaseMaternityAllowanceStrategy implements MaternityAllowanceStrategy {

    private final MaternityWageCalculatorService maternityWageCalculatorService;
    private final CityService cityService;
    private final AllowanceRulesService allowanceRulesService;


    @Override
    public MaternityAllowanceResponse calculateMaternityAllowance(MaternityAllowanceRequest request) {

        String cityName = cityService.getEnabledCityChineseName(request.getCityCode());
        AllowanceRulesResponse allowanceRules = allowanceRulesService.getEnabledAllowanceRulesByCity(cityName);
        if (allowanceRules == null) {
            throw new IllegalArgumentException("不支持的城市代码: " + request.getCityCode());
        }
        BigDecimal paidWageInMaternity = getMaternityWage(request);

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
        if (allowanceRules.getPayoutMethod() == PAYOUT_METHOD_INDIVIDUAL) {
            compensationAmount = maxAllowance.subtract(governmentAllowance);
        }
        else {
            compensationAmount = maxAllowance.subtract(paidWageInMaternity);
        }

        // 构建详细说明列表
        List<String> allowanceCompensationDetails = new ArrayList<>();

        // 添加产假工资信息
        if (Objects.nonNull(paidWageInMaternity) && paidWageInMaternity.compareTo(BigDecimal.ZERO) > 0) {
            allowanceCompensationDetails.add(String.format("产假期间发放工资：%.2f元", paidWageInMaternity));
        }

        // 添加员工工资补贴信息
        if (allowanceBasedEmployeeSalary.compareTo(BigDecimal.ZERO) > 0) {
            allowanceCompensationDetails.add(String.format("员工产前12个月月均工资计算补贴金额：%.2f元÷30天×%d天=%.2f元",
                    request.getAverageSalaryPast12Months(),
                    request.getMaternityLeaveDays(),
                    allowanceBasedEmployeeSalary));
        }

        allowanceCompensationDetails.add(String.format("政府发放补贴金额：%.2f元", governmentAllowance));
        allowanceCompensationDetails.add(String.format("员工应享受补贴：%.2f元", maxAllowance));

        if (compensationAmount.compareTo(BigDecimal.ZERO) < 0) {
            allowanceCompensationDetails.add(String.format("补差金额：%.2f元-%.2f元=%.2f元，计算结果为负，取0",
                    maxAllowance, paidWageInMaternity, compensationAmount));
        } else {
            allowanceCompensationDetails.add(String.format("补差金额：%.2f元-%.2f元=%.2f元", 
                    maxAllowance, paidWageInMaternity, compensationAmount));
        }

        // 如果补差金额为负数，则返回0
        BigDecimal finalCompensationAmount = compensationAmount.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : compensationAmount;

        return MaternityAllowanceResponse.builder()
                .requestId(null) // 由Service层设置
                .resultId(null) // 由Service层设置
                .lanId(request.getLanId())
                .employeeName(request.getEmployeeName())
                .cityCode(request.getCityCode())
                .cityName(CityEnum.SHENZHEN.getChineseName())
                .allowanceDays(request.getMaternityLeaveDays())
                .extraAllowance(BigDecimal.ZERO) // 额外补贴暂设为0
                .maternityAllowance(maxAllowance) // 应享受补贴金额
                .compensationAmount(finalCompensationAmount) // 补差金额
                .paidMaternityWage(paidWageInMaternity) // 产假应付工资
                .employeeRefundAmount(null)
                .allowanceCompensationDetails(allowanceCompensationDetails)
                .refundDetails(null)
                .build();
    }

    private BigDecimal getAllowanceBasedCorporateSalary(MaternityAllowanceRequest request,Integer monthDays) {
        // a. 单位申报上年度月均工资计算补贴金额
        BigDecimal unitDeclaredAllowance = BigDecimal.ZERO;
        if (request.getUnitMonthlyAverageSalary() != null) {
            unitDeclaredAllowance = request.getUnitMonthlyAverageSalary()
                    .multiply(new BigDecimal(request.getMaternityLeaveDays()))
                    .divide(new BigDecimal(monthDays), 2, RoundingMode.HALF_UP);
        }
        return unitDeclaredAllowance;
    }

    private BigDecimal getMaternityWage(MaternityAllowanceRequest request) {
        // 调用 MaternityWageCalculatorService 计算产假应付工资
        BigDecimal paidMaternityWage = BigDecimal.ZERO;
        if (request.getMonthlyBaseSalary() != null) {
            paidMaternityWage = maternityWageCalculatorService.calculateMaternityWage(
                    request.getMaternityLeaveStartDate(),
                    request.getMaternityLeaveEndDate(),
                    request.getMonthlyBaseSalary(),
                    request.getAdjustedMonthlyBaseSalary()
            );
        }
        return paidMaternityWage;
    }

    private BigDecimal getAllowanceBasedEmployeeSalary(MaternityAllowanceRequest request,Integer monthDays) {
        // 计算生育津贴：产前12个月月均工资 / 30 * 产假天数，四舍五入保留2位小数
        BigDecimal maternityAllowance = request.getAverageSalaryPast12Months()
                .multiply(new BigDecimal(request.getMaternityLeaveDays()))
                .divide(new BigDecimal(monthDays), 2, RoundingMode.HALF_UP);
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

}

