package com.easy.care.domain;

import com.easy.care.calculator.PayrollDayCalculator;
import com.easy.care.dto.CompanyAdvanceMap;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 返还金额计算上下文
 * 封装计算过程中需要的所有数据，避免重复查询数据库
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
