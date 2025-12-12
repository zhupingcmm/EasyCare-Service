package com.ocbc.ms.easy.care.util;


import com.ocbc.ms.easy.care.dto.AllowanceRulesResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 补差详细信息领域模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AllowanceCompensationDetailsDO {
    
    private BigDecimal paidWageInMaternity;
    
    private BigDecimal unitMonthlyAverageSalary;
    
    private BigDecimal allowanceBasedCorporateSalary;
    
    private Integer maternityLeaveDays;
    
    private BigDecimal averageSalaryPast12Months;
    
    private BigDecimal monthDays;
    
    private BigDecimal allowanceBasedEmployeeSalary;
    
    private BigDecimal maxAllowance;
    
    private BigDecimal governmentAllowance;
    
    private BigDecimal compensationAmount;
    
    private AllowanceRulesResponse allowanceRules;
}
