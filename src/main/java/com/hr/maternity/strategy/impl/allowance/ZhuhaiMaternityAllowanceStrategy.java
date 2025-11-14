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
import java.util.ArrayList;
import java.util.List;

/**
 * 珠海生育津贴计算策略
 */
@Component
@RequiredArgsConstructor
public class ZhuhaiMaternityAllowanceStrategy implements MaternityAllowanceStrategy {

    private final MaternityWageCalculatorService wageCalculatorService;

    @Override
    public MaternityAllowanceResponse calculateMaternityAllowance(MaternityAllowanceRequest request) {
        MaternityAllowanceResponse response = new MaternityAllowanceResponse();
        
        // 1. 计算生育津贴：产前12个月月均工资 / 30 * 产假天数，四舍五入保留2位小数
        BigDecimal maternityAllowance = request.getAverageSalaryPast12Months()
                .multiply(new BigDecimal(request.getMaternityLeaveDays()))
                .divide(new BigDecimal("30"), 2, RoundingMode.HALF_UP);
        
        // 2. 计算产假应付工资
        BigDecimal paidMaternityWage = wageCalculatorService.calculateMaternityWage(
                request.getMaternityLeaveStartDate(),
                request.getMaternityLeaveEndDate(),
                request.getMonthlyBaseSalary(),
                request.getAdjustedMonthlyBaseSalary()
        );
        
        // 3. 获取政府发放补贴金额
        BigDecimal governmentAllowance = request.getGovernmentAllowance();
        
        // 4. 员工应享受补贴（生育津贴和政府发放补贴金额取最大值）
        BigDecimal entitledSubsidy = maternityAllowance.max(governmentAllowance);
        
        // 5. 计算补差金额
        BigDecimal compensationAmount;
        List<String> compensationDetails = new ArrayList<>();
        
        // 添加各项详情到列表
        compensationDetails.add(String.format("产假期间发放工资: %.2f元", paidMaternityWage));
        compensationDetails.add(String.format("员工产前12个月月均工资计算补贴金额：%.2f元÷30天×%d天=%.2f元",
                request.getAverageSalaryPast12Months(),
                request.getMaternityLeaveDays(),
                maternityAllowance));
        compensationDetails.add(String.format("政府发放补贴金额: %.2f元", governmentAllowance));
        compensationDetails.add(String.format("员工应享受补贴: %.2f元", entitledSubsidy));
        
        // 计算补差金额
        if (governmentAllowance.compareTo(maternityAllowance) > 0) {
            compensationAmount = governmentAllowance.subtract(paidMaternityWage);
        } else {
            compensationAmount = maternityAllowance.subtract(paidMaternityWage);
        }
        
        // 处理负值情况
        if (compensationAmount.compareTo(BigDecimal.ZERO) < 0) {
            compensationDetails.add(String.format("补差金额: %.2f - %.2f = %.2f元 (计算结果为负，取0)",
                    entitledSubsidy, paidMaternityWage, compensationAmount));
            compensationAmount = BigDecimal.ZERO;
        } else {
            compensationDetails.add(String.format("补差金额: %.2f - %.2f = %.2f元",
                    entitledSubsidy, paidMaternityWage, compensationAmount));
        }
        
        // 设置响应结果
        response.setLanId(request.getLanId());
        response.setEmployeeName(request.getEmployeeName());
        response.setCityCode(request.getCityCode());
        response.setAllowanceDays(request.getMaternityLeaveDays());
        response.setMaternityAllowance(maternityAllowance);
        response.setPaidMaternityWage(paidMaternityWage);
        response.setCompensationAmount(compensationAmount);
        response.setAllowanceCompensationDetails(compensationDetails);
        
        return response;
    }

    @Override
    public String getSupportedCityCode() {
        return CityEnum.ZHUHAI.getCode();
    }
}

