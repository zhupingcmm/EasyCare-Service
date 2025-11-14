package com.hr.maternity.service.impl;

import com.hr.maternity.service.MaternityWageCalculatorService;
import com.hr.maternity.service.RequestDateCompensationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 产假申请日期补偿计算服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RequestDateCompensationServiceImpl implements RequestDateCompensationService {
    
    private final MaternityWageCalculatorService maternityWageCalculatorService;
    
    @Override
    public Map<String, Object> calculateRequestDateCompensation(
            BigDecimal monthlyBaseSalary,
            BigDecimal adjustedMonthlyBaseSalary,
            LocalDate maternityLeaveStartDate,
            LocalDate maternityLeaveRequestDate,
            BigDecimal socialInsuranceBase,
            BigDecimal adjustedSocialInsuranceBase,
            BigDecimal espp,
            BigDecimal unionFee) {
        
        Map<String, Object> result = new HashMap<>();
        result.put("compensation", BigDecimal.ZERO);
        result.put("refundDetail", "");
        
        // 1. 验证产假申请日期是否在产假开始日期之后
        if (maternityLeaveRequestDate == null || maternityLeaveStartDate == null 
                || !maternityLeaveRequestDate.isAfter(maternityLeaveStartDate)) {
            return result;
        }
        
        // 获取日期信息
        int startYear = maternityLeaveStartDate.getYear();
        int startMonth = maternityLeaveStartDate.getMonthValue();
        int startDay = maternityLeaveStartDate.getDayOfMonth();
        
        int requestYear = maternityLeaveRequestDate.getYear();
        int requestMonth = maternityLeaveRequestDate.getMonthValue();
        int requestDay = maternityLeaveRequestDate.getDayOfMonth();
        
        BigDecimal totalCompensation = BigDecimal.ZERO;
        StringBuilder detailBuilder = new StringBuilder();
        List<String> monthlyDetails = new ArrayList<>();
        
        // 2. 两个日期在同一个月的情况
        if (startYear == requestYear && startMonth == requestMonth) {
            // 2.1 都在当月15号之前
            if (startDay <= 15 && requestDay <= 15) {
                return result;
            }
            // 2.2 都在当月15号之后
            if (startDay > 15 && requestDay > 15) {
                return result;
            }
            // 2.3 产假开始日期在15号之前，申请产假日期在15号之后
            if (startDay <= 15 && requestDay > 15) {
                // 根据开始月份选择对应的月基本工资
                BigDecimal baseSalary = startMonth >= 4 && adjustedMonthlyBaseSalary != null 
                        ? adjustedMonthlyBaseSalary : monthlyBaseSalary;
                // 计算产假开始月的产假期间工资
                LocalDate monthEnd = maternityLeaveStartDate.withDayOfMonth(
                        maternityLeaveStartDate.lengthOfMonth());
                BigDecimal startingMonthWage = maternityWageCalculatorService.calculateStartingMonthMaternityWage(
                        maternityLeaveStartDate, monthEnd, baseSalary);
                
                totalCompensation = startingMonthWage;
                monthlyDetails.add(String.format("%d年%d月：%.2f元", startYear, startMonth, startingMonthWage));
            }
        } else {
            // 2.4 产假申请日期在产假开始日期之后的某个月
            int monthsBetween = (requestYear - startYear) * 12 + (requestMonth - startMonth);
            
            // 2.4.1 产假开始日期在当月15号之前，申请产假日期在后面某个月15号之前
            if (startDay <= 15 && requestDay <= 15) {
                // 根据开始月份选择对应的月基本工资
                BigDecimal baseSalary = startMonth >= 4 && adjustedMonthlyBaseSalary != null 
                        ? adjustedMonthlyBaseSalary : monthlyBaseSalary;
                // 先计算产假开始月的产假工资
                LocalDate monthEnd = maternityLeaveStartDate.withDayOfMonth(
                        maternityLeaveStartDate.lengthOfMonth());
                BigDecimal startingMonthWage = maternityWageCalculatorService.calculateStartingMonthMaternityWage(
                        maternityLeaveStartDate, monthEnd, baseSalary);
                
                totalCompensation = totalCompensation.add(startingMonthWage);
                monthlyDetails.add(String.format("%d年%d月：%.2f元", startYear, startMonth, startingMonthWage));
                
                // 加上完整月数（不包括开始月和申请月）按月计算扣除额
                int completeMonths = monthsBetween - 1;
                if (completeMonths > 0) {
                    LocalDate currentMonth = maternityLeaveStartDate.plusMonths(1);
                    for (int i = 0; i < completeMonths; i++) {
                        int year = currentMonth.getYear();
                        int month = currentMonth.getMonthValue();
                        
                        BigDecimal monthBaseSalary = month >= 4 && adjustedMonthlyBaseSalary != null 
                                ? adjustedMonthlyBaseSalary : monthlyBaseSalary;
                        BigDecimal monthSocialInsurance = month >= 7 && adjustedSocialInsuranceBase != null 
                                ? adjustedSocialInsuranceBase : (socialInsuranceBase != null ? socialInsuranceBase : BigDecimal.ZERO);
                        
                        BigDecimal monthlyDeduction = monthBaseSalary
                                .subtract(monthSocialInsurance)
                                .subtract(espp != null ? espp : BigDecimal.ZERO)
                                .subtract(unionFee != null ? unionFee : BigDecimal.ZERO);
                        
                        totalCompensation = totalCompensation.add(monthlyDeduction);
                        monthlyDetails.add(String.format("%d年%d月：%.2f元", year, month, monthlyDeduction));
                        currentMonth = currentMonth.plusMonths(1);
                    }
                }
            }
            // 2.4.2 产假开始日期在当月15号之后，申请产假日期在后面某个月15号之前
            else if (startDay > 15 && requestDay <= 15) {
                int completeMonths = monthsBetween - 1;
                if (completeMonths > 0) {
                    LocalDate currentMonth = maternityLeaveStartDate.plusMonths(1);
                    for (int i = 0; i < completeMonths; i++) {
                        int year = currentMonth.getYear();
                        int month = currentMonth.getMonthValue();
                        
                        BigDecimal monthBaseSalary = month >= 4 && adjustedMonthlyBaseSalary != null 
                                ? adjustedMonthlyBaseSalary : monthlyBaseSalary;
                        BigDecimal monthSocialInsurance = month >= 7 && adjustedSocialInsuranceBase != null 
                                ? adjustedSocialInsuranceBase : (socialInsuranceBase != null ? socialInsuranceBase : BigDecimal.ZERO);
                        
                        BigDecimal monthlyDeduction = monthBaseSalary
                                .subtract(monthSocialInsurance)
                                .subtract(espp != null ? espp : BigDecimal.ZERO)
                                .subtract(unionFee != null ? unionFee : BigDecimal.ZERO);
                        
                        totalCompensation = totalCompensation.add(monthlyDeduction);
                        monthlyDetails.add(String.format("%d年%d月：%.2f元", year, month, monthlyDeduction));
                        currentMonth = currentMonth.plusMonths(1);
                    }
                }
            }
            // 2.4.3 产假开始日期在当月15号之前，申请产假日期在后面某个月15号之后
            else if (startDay <= 15 && requestDay > 15) {
                // 根据开始月份选择对应的月基本工资
                BigDecimal baseSalary = startMonth >= 4 && adjustedMonthlyBaseSalary != null 
                        ? adjustedMonthlyBaseSalary : monthlyBaseSalary;
                // 先计算产假开始月的产假工资
                LocalDate monthEnd = maternityLeaveStartDate.withDayOfMonth(
                        maternityLeaveStartDate.lengthOfMonth());
                BigDecimal startingMonthWage = maternityWageCalculatorService.calculateStartingMonthMaternityWage(
                        maternityLeaveStartDate, monthEnd, baseSalary);
                
                totalCompensation = totalCompensation.add(startingMonthWage);
                monthlyDetails.add(String.format("%d年%d月：%.2f元", startYear, startMonth, startingMonthWage));
                
                // 加上从开始月下月到申请月的完整月数按月计算扣除额
                int completeMonths = monthsBetween;
                if (completeMonths > 0) {
                    LocalDate currentMonth = maternityLeaveStartDate.plusMonths(1);
                    for (int i = 0; i < completeMonths; i++) {
                        int year = currentMonth.getYear();
                        int month = currentMonth.getMonthValue();
                        
                        BigDecimal monthBaseSalary = month >= 4 && adjustedMonthlyBaseSalary != null 
                                ? adjustedMonthlyBaseSalary : monthlyBaseSalary;
                        BigDecimal monthSocialInsurance = month >= 7 && adjustedSocialInsuranceBase != null 
                                ? adjustedSocialInsuranceBase : (socialInsuranceBase != null ? socialInsuranceBase : BigDecimal.ZERO);
                        
                        BigDecimal monthlyDeduction = monthBaseSalary
                                .subtract(monthSocialInsurance)
                                .subtract(espp != null ? espp : BigDecimal.ZERO)
                                .subtract(unionFee != null ? unionFee : BigDecimal.ZERO);
                        
                        totalCompensation = totalCompensation.add(monthlyDeduction);
                        monthlyDetails.add(String.format("%d年%d月：%.2f元", year, month, monthlyDeduction));
                        currentMonth = currentMonth.plusMonths(1);
                    }
                }
            }
            // 2.4.4 产假开始日期在当月15号之后，申请产假日期在后面某个月15号之后
            else if (startDay > 15 && requestDay > 15) {
                int completeMonths = monthsBetween;
                if (completeMonths > 0) {
                    LocalDate currentMonth = maternityLeaveStartDate.plusMonths(1);
                    for (int i = 0; i < completeMonths; i++) {
                        int year = currentMonth.getYear();
                        int month = currentMonth.getMonthValue();
                        
                        BigDecimal monthBaseSalary = month >= 4 && adjustedMonthlyBaseSalary != null 
                                ? adjustedMonthlyBaseSalary : monthlyBaseSalary;
                        BigDecimal monthSocialInsurance = month >= 7 && adjustedSocialInsuranceBase != null 
                                ? adjustedSocialInsuranceBase : (socialInsuranceBase != null ? socialInsuranceBase : BigDecimal.ZERO);
                        
                        BigDecimal monthlyDeduction = monthBaseSalary
                                .subtract(monthSocialInsurance)
                                .subtract(espp != null ? espp : BigDecimal.ZERO)
                                .subtract(unionFee != null ? unionFee : BigDecimal.ZERO);
                        
                        totalCompensation = totalCompensation.add(monthlyDeduction);
                        monthlyDetails.add(String.format("%d年%d月：%.2f元", year, month, monthlyDeduction));
                        currentMonth = currentMonth.plusMonths(1);
                    }
                }
            }
        }
        
        // 构建详细描述
        if (totalCompensation.compareTo(BigDecimal.ZERO) > 0) {
            detailBuilder.append("产假申请日期补偿：");
            for (int i = 0; i < monthlyDetails.size(); i++) {
                if (i > 0) {
                    detailBuilder.append("，");
                }
                detailBuilder.append(monthlyDetails.get(i));
            }
        }
        
        result.put("compensation", totalCompensation);
        result.put("refundDetail", detailBuilder.toString());
        
        log.debug("产假申请日期补偿计算完成，补偿金额：{}，详情：{}", totalCompensation, detailBuilder.toString());
        
        return result;
    }
}
