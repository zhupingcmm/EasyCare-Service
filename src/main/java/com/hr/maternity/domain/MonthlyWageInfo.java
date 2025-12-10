package com.hr.maternity.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 月度工资信息
 * 用于存储首月和尾月的工资计算结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
    
    /** 首月产假工资折算 */
    private BigDecimal firstMonthMaternityWage;
    
    /** 尾月产假工资折算 */
    private BigDecimal lastMonthMaternityWage;
}
