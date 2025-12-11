package com.easy.care.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 返还金额计算结果
 * 包含各项返还金额的明细
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
