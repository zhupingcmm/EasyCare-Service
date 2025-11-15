package com.hr.maternity.strategy.impl.allowance;

import com.hr.maternity.dto.MaternityAllowanceRequest;
import com.hr.maternity.dto.MaternityAllowanceResponse;
import com.hr.maternity.enums.CityEnum;
import com.hr.maternity.service.MaternityWageCalculatorService;
import com.hr.maternity.strategy.MaternityAllowanceStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;

/**
 * 深圳市生育津贴计算策略实现
 */
@Component
@RequiredArgsConstructor
public class ShenzhenMaternityAllowanceStrategy implements MaternityAllowanceStrategy {

    private final MaternityWageCalculatorService maternityWageCalculatorService;

    @Override
    public MaternityAllowanceResponse calculateMaternityAllowance(MaternityAllowanceRequest request) {
        // 计算生育津贴：产前12个月月均工资 / 30 * 产假天数，四舍五入保留2位小数
        BigDecimal maternityAllowance = request.getAverageSalaryPast12Months()
                .multiply(new BigDecimal(request.getMaternityLeaveDays()))
                .divide(new BigDecimal("30"), 2, RoundingMode.HALF_UP);
        
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
        
        // 计算各项补贴金额
        // a. 单位申报上年度月均工资计算补贴金额
        BigDecimal unitDeclaredAllowance = BigDecimal.ZERO;
        if (request.getUnitMonthlyAverageSalary() != null) {
            unitDeclaredAllowance = request.getUnitMonthlyAverageSalary()
                    .multiply(new BigDecimal(request.getMaternityLeaveDays()))
                    .divide(new BigDecimal("30"), 2, RoundingMode.HALF_UP);
        }

        // c. 政府发放补贴金额
        BigDecimal governmentAllowance = request.getGovernmentAllowance() != null ?
                request.getGovernmentAllowance() : BigDecimal.ZERO;

        // d. 员工应享受补贴：MAX(a,b,c)
        BigDecimal maxAllowance = unitDeclaredAllowance
                .max(maternityAllowance)
                .max(governmentAllowance);

        // 计算补差金额
        BigDecimal compensationAmount = calculateCompensation(
            maternityAllowance, 
            request.getGovernmentAllowance(), 
            paidMaternityWage
        );

        // 构建详细说明列表
        List<String> allowanceCompensationDetails = Arrays.asList(
            String.format("产假期间发放工资：%.2f元", paidMaternityWage),
            String.format("员工产前12个月月均工资计算补贴金额：%.2f元÷30天×%d天=%.2f元",
                request.getAverageSalaryPast12Months(),
                request.getMaternityLeaveDays(),
                maternityAllowance),
            String.format("政府发放补贴金额：%.2f元", governmentAllowance),
            String.format("员工应享受补贴：%.2f元", maxAllowance),
            compensationAmount.compareTo(BigDecimal.ZERO) < 0 ?
                String.format("补差金额：%.2f元-%.2f元=%.2f元，计算结果为负，取0", maxAllowance, paidMaternityWage, compensationAmount) :
                String.format("补差金额：%.2f元-%.2f元=%.2f元", maxAllowance, paidMaternityWage, compensationAmount)
        );

        // 如果补差金额为负数，则返回0
        BigDecimal finalCompensationAmount = compensationAmount.compareTo(BigDecimal.ZERO) < 0 ? 
            BigDecimal.ZERO : compensationAmount;

        return new MaternityAllowanceResponse(
                null, // requestId - 由Service层设置
                null, // resultId - 由Service层设置
                request.getLanId(),
                request.getEmployeeName(),
                request.getCityCode(),
                CityEnum.SHENZHEN.getChineseName(),
                request.getMaternityLeaveDays(),
                BigDecimal.ZERO, // 额外补贴暂设为0
                maxAllowance, // 应享受补贴金额
                finalCompensationAmount, // 补差金额
                paidMaternityWage, // 产假应付工资,
                null,
                allowanceCompensationDetails,
                null
        );
    }
    
    /**
     * 计算补差金额
     */
    private BigDecimal calculateCompensation(BigDecimal maternityAllowance, 
                                           BigDecimal governmentAllowance, 
                                           BigDecimal paidMaternityWage) {
        if (governmentAllowance == null) {
            governmentAllowance = BigDecimal.ZERO;
        }
        if (paidMaternityWage == null) {
            paidMaternityWage = BigDecimal.ZERO;
        }
        
        // 如果政府发放金额大于生育津贴，补差 = 政府发放金额 - 已发放产假期间工资
        if (governmentAllowance.compareTo(maternityAllowance) > 0) {
            return governmentAllowance.subtract(paidMaternityWage);
        } else {
            // 否则补差 = 生育津贴 - 已发放产假期间工资
            return maternityAllowance.subtract(paidMaternityWage);
        }
    }

    @Override
    public String getSupportedCityCode() {
        return  CityEnum.SHENZHEN.getCode();
    }


}

