package com.hr.maternity.service;

import com.hr.maternity.calculator.PayrollDayCalculator;

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
    
    /**
     * 计算基于产假申请日期的补偿金额（优化版本，使用配置的调整月份）
     * 
     * @param monthlyBaseSalary 月基本工资
     * @param adjustedMonthlyBaseSalary 调整后的月基本工资
     * @param maternityLeaveStartDate 产假开始时间
     * @param maternityLeaveRequestDate 产假申请日期
     * @param socialInsuranceBase 月社保缴费基数
     * @param adjustedSocialInsuranceBase 调整后的月社保缴费基数
     * @param espp ESPP金额
     * @param unionFee 工会费
     * @param salaryAdjustMonth 工资调整月份
     * @param socialAdjustMonth 社保调整月份
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
            BigDecimal unionFee,
            Integer salaryAdjustMonth,
            Integer socialAdjustMonth);
    
    /**
     * 计算基于产假申请日期的补偿金额（优化版本，使用PayrollDayCalculator避免重复查询数据库）
     * 
     * @param monthlyBaseSalary 月基本工资
     * @param adjustedMonthlyBaseSalary 调整后的月基本工资
     * @param maternityLeaveStartDate 产假开始时间
     * @param maternityLeaveRequestDate 产假申请日期
     * @param socialInsuranceBase 月社保缴费基数
     * @param adjustedSocialInsuranceBase 调整后的月社保缴费基数
     * @param espp ESPP金额
     * @param unionFee 工会费
     * @param salaryAdjustMonth 工资调整月份
     * @param socialAdjustMonth 社保调整月份
     * @param calculator 计薪日计算器（避免重复查询数据库）
     * @return HashMap包含compensation(BigDecimal)和refundDetail(String)
     */
    Map<String, Object> calculateRequestDateCompensationWithCalculator(
            BigDecimal monthlyBaseSalary,
            BigDecimal adjustedMonthlyBaseSalary,
            LocalDate maternityLeaveStartDate,
            LocalDate maternityLeaveRequestDate,
            BigDecimal socialInsuranceBase,
            BigDecimal adjustedSocialInsuranceBase,
            BigDecimal espp,
            BigDecimal unionFee,
            Integer salaryAdjustMonth,
            Integer socialAdjustMonth,
            PayrollDayCalculator calculator);
}
