package com.ocbc.ms.easy.care.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/**
 * 产假申请日期补偿计算服务
 */
public interface RequestDateCompensationService {
    
    /**
     * 计算基于产假申请日期的补偿金额
     * 
     * @param monthlyBaseSalary 月基本工资
     * @param adjustedMonthlyBaseSalary 调整后的月基本工资
     * @param maternityLeaveStartDate 产假开始时间
     * @param maternityLeaveRequestDate 产假申请日期
     * @param socialInsuranceBase 月社保缴费基数
     * @param adjustedSocialInsuranceBase 调整后的月社保缴费基数
     * @param espp ESPP金额
     * @param unionFee 工会费
     * @return HashMap包含compensation(BigDecimal)和refundDetail(String)
     */
    Map<String, Object> calculateRequestDateCompensation(
            BigDecimal monthlyBaseSalary,
            BigDecimal adjustedMonthlyBaseSalary,
            LocalDate maternityLeaveStartDate,
            LocalDate maternityLeaveRequestDate,
            BigDecimal socialInsuranceBase,
            BigDecimal adjustedSocialInsuranceBase,
            BigDecimal espp,
            BigDecimal unionFee);
}
