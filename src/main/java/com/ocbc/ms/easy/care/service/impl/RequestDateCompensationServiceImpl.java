package com.ocbc.ms.easy.care.service.impl;

import com.ocbc.ms.easy.care.calculator.PayrollDayCalculator;
import com.ocbc.ms.easy.care.service.MaternityWageCalculatorService;
import com.ocbc.ms.easy.care.service.RequestDateCompensationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
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
        // 为保持向后兼容，使用硬编码的调整月份
        return calculateRequestDateCompensation(
                monthlyBaseSalary,
                adjustedMonthlyBaseSalary,
                maternityLeaveStartDate,
                maternityLeaveRequestDate,
                socialInsuranceBase,
                adjustedSocialInsuranceBase,
                espp,
                unionFee,
                4,  // 默认工资调整月份为4月
                7   // 默认社保调整月份为7月
        );
    }
    
    @Override
    public Map<String, Object> calculateRequestDateCompensation(
            BigDecimal monthlyBaseSalary,
            BigDecimal adjustedMonthlyBaseSalary,
            LocalDate maternityLeaveStartDate,
            LocalDate maternityLeaveRequestDate,
            BigDecimal socialInsuranceBase,
            BigDecimal adjustedSocialInsuranceBase,
            BigDecimal espp,
            BigDecimal unionFee,
            Integer salaryAdjustMonth,
            Integer socialAdjustMonth) {
        
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
                BigDecimal baseSalary = startMonth >= salaryAdjustMonth && adjustedMonthlyBaseSalary != null 
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
                BigDecimal baseSalary = startMonth >= salaryAdjustMonth && adjustedMonthlyBaseSalary != null 
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
                        
                        BigDecimal monthBaseSalary = month >= salaryAdjustMonth && adjustedMonthlyBaseSalary != null 
                                ? adjustedMonthlyBaseSalary : monthlyBaseSalary;
                        BigDecimal monthSocialInsurance = month >= socialAdjustMonth && adjustedSocialInsuranceBase != null 
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
                        
                        BigDecimal monthBaseSalary = month >= salaryAdjustMonth && adjustedMonthlyBaseSalary != null 
                                ? adjustedMonthlyBaseSalary : monthlyBaseSalary;
                        BigDecimal monthSocialInsurance = month >= socialAdjustMonth && adjustedSocialInsuranceBase != null 
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
                BigDecimal baseSalary = startMonth >= salaryAdjustMonth && adjustedMonthlyBaseSalary != null 
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
                        
                        BigDecimal monthBaseSalary = month >= salaryAdjustMonth && adjustedMonthlyBaseSalary != null 
                                ? adjustedMonthlyBaseSalary : monthlyBaseSalary;
                        BigDecimal monthSocialInsurance = month >= socialAdjustMonth && adjustedSocialInsuranceBase != null 
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
                        
                        BigDecimal monthBaseSalary = month >= salaryAdjustMonth && adjustedMonthlyBaseSalary != null 
                                ? adjustedMonthlyBaseSalary : monthlyBaseSalary;
                        BigDecimal monthSocialInsurance = month >= socialAdjustMonth && adjustedSocialInsuranceBase != null 
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
    
    @Override
    public Map<String, Object> calculateRequestDateCompensationWithCalculator(
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
            PayrollDayCalculator calculator) {
        
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
                BigDecimal baseSalary = startMonth >= salaryAdjustMonth && adjustedMonthlyBaseSalary != null 
                        ? adjustedMonthlyBaseSalary : monthlyBaseSalary;
                // 使用 PayrollDayCalculator 计算产假开始月的产假期间工资
                LocalDate monthEnd = maternityLeaveStartDate.withDayOfMonth(
                        maternityLeaveStartDate.lengthOfMonth());
                BigDecimal startingMonthWage = calculateStartingMonthWageWithCalculator(
                        maternityLeaveStartDate, monthEnd, baseSalary, calculator);
                
                totalCompensation = startingMonthWage;
                monthlyDetails.add(String.format("%d年%d月：%.2f元", startYear, startMonth, startingMonthWage));
            }
        } else {
            // 2.4 产假申请日期在产假开始日期之后的某个月
            int monthsBetween = (requestYear - startYear) * 12 + (requestMonth - startMonth);
            
            // 2.4.1 产假开始日期在当月15号之前，申请产假日期在后面某个月15号之前
            if (startDay <= 15 && requestDay <= 15) {
                // 根据开始月份选择对应的月基本工资
                BigDecimal baseSalary = startMonth >= salaryAdjustMonth && adjustedMonthlyBaseSalary != null 
                        ? adjustedMonthlyBaseSalary : monthlyBaseSalary;
                // 使用 PayrollDayCalculator 计算产假开始月的产假工资
                LocalDate monthEnd = maternityLeaveStartDate.withDayOfMonth(
                        maternityLeaveStartDate.lengthOfMonth());
                BigDecimal startingMonthWage = calculateStartingMonthWageWithCalculator(
                        maternityLeaveStartDate, monthEnd, baseSalary, calculator);
                
                totalCompensation = totalCompensation.add(startingMonthWage);
                monthlyDetails.add(String.format("%d年%d月：%.2f元", startYear, startMonth, startingMonthWage));
                
                // 加上完整月数（不包括开始月和申请月）按月计算扣除额
                int completeMonths = monthsBetween - 1;
                if (completeMonths > 0) {
                    LocalDate currentMonth = maternityLeaveStartDate.plusMonths(1);
                    for (int i = 0; i < completeMonths; i++) {
                        int year = currentMonth.getYear();
                        int month = currentMonth.getMonthValue();
                        
                        BigDecimal monthBaseSalary = month >= salaryAdjustMonth && adjustedMonthlyBaseSalary != null 
                                ? adjustedMonthlyBaseSalary : monthlyBaseSalary;
                        BigDecimal monthSocialInsurance = month >= socialAdjustMonth && adjustedSocialInsuranceBase != null 
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
                        
                        BigDecimal monthBaseSalary = month >= salaryAdjustMonth && adjustedMonthlyBaseSalary != null 
                                ? adjustedMonthlyBaseSalary : monthlyBaseSalary;
                        BigDecimal monthSocialInsurance = month >= socialAdjustMonth && adjustedSocialInsuranceBase != null 
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
                BigDecimal baseSalary = startMonth >= salaryAdjustMonth && adjustedMonthlyBaseSalary != null 
                        ? adjustedMonthlyBaseSalary : monthlyBaseSalary;
                // 使用 PayrollDayCalculator 计算产假开始月的产假工资
                LocalDate monthEnd = maternityLeaveStartDate.withDayOfMonth(
                        maternityLeaveStartDate.lengthOfMonth());
                BigDecimal startingMonthWage = calculateStartingMonthWageWithCalculator(
                        maternityLeaveStartDate, monthEnd, baseSalary, calculator);
                
                totalCompensation = totalCompensation.add(startingMonthWage);
                monthlyDetails.add(String.format("%d年%d月：%.2f元", startYear, startMonth, startingMonthWage));
                
                // 加上从开始月下月到申请月的完整月数按月计算扣除额
                int completeMonths = monthsBetween;
                if (completeMonths > 0) {
                    LocalDate currentMonth = maternityLeaveStartDate.plusMonths(1);
                    for (int i = 0; i < completeMonths; i++) {
                        int year = currentMonth.getYear();
                        int month = currentMonth.getMonthValue();
                        
                        BigDecimal monthBaseSalary = month >= salaryAdjustMonth && adjustedMonthlyBaseSalary != null 
                                ? adjustedMonthlyBaseSalary : monthlyBaseSalary;
                        BigDecimal monthSocialInsurance = month >= socialAdjustMonth && adjustedSocialInsuranceBase != null 
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
                        
                        BigDecimal monthBaseSalary = month >= salaryAdjustMonth && adjustedMonthlyBaseSalary != null 
                                ? adjustedMonthlyBaseSalary : monthlyBaseSalary;
                        BigDecimal monthSocialInsurance = month >= socialAdjustMonth && adjustedSocialInsuranceBase != null 
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
        
        log.debug("产假申请日期补偿计算完成（使用PayrollDayCalculator），补偿金额：{}，详情：{}", 
                totalCompensation, detailBuilder.toString());
        
        return result;
    }
    
    /**
     * 使用 PayrollDayCalculator 计算产假开始月的工资折算
     */
    private BigDecimal calculateStartingMonthWageWithCalculator(
            LocalDate maternityLeaveStartDate,
            LocalDate maternityLeaveEndDate,
            BigDecimal monthlyBaseSalary,
            PayrollDayCalculator calculator) {
        
        YearMonth startingYearMonth = YearMonth.from(maternityLeaveStartDate);
        
        // 计算该月总计薪日
        int totalPayrollDays = calculator.calculateMonthPayrollDays(startingYearMonth);
        // 计算产假期间的计薪日
        int maternityPayrollDays = calculator.calculatePayrollDays(
                maternityLeaveStartDate, maternityLeaveEndDate);
        
        if (totalPayrollDays == 0) {
            log.warn("产假开始月计薪日天数为0，返回0");
            return BigDecimal.ZERO;
        }
        
        // 计算产假期间计薪日占比
        BigDecimal payrollDayRatio = new BigDecimal(maternityPayrollDays)
                .divide(new BigDecimal(totalPayrollDays), 6, RoundingMode.HALF_UP);
        
        // 计算产假开始月的工资 = 月基本工资 * 产假计薪日占比
        BigDecimal startingMonthWage = monthlyBaseSalary.multiply(payrollDayRatio)
                .setScale(2, RoundingMode.HALF_UP);
        
        log.debug("产假开始月({}年{}月)工资计算完成（使用PayrollDayCalculator），该月总计薪日: {}，产假计薪日: {}，计薪日占比: {}，月基本工资: {}，产假工资: {}", 
                maternityLeaveStartDate.getYear(), maternityLeaveStartDate.getMonthValue(),
                totalPayrollDays, maternityPayrollDays, payrollDayRatio, monthlyBaseSalary, startingMonthWage);
        
        return startingMonthWage;
    }
}
