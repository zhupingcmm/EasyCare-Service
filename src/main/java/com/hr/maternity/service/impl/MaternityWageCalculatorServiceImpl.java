package com.hr.maternity.service.impl;

import com.hr.maternity.domain.MonthlyWorkdayInfoDO;
import com.hr.maternity.service.MaternityWageCalculatorService;
import com.hr.maternity.service.WorkdayCalculatorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

/**
 * 产假应付工资计算服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MaternityWageCalculatorServiceImpl implements MaternityWageCalculatorService {

    private final WorkdayCalculatorService workdayCalculatorService;

    @Override
    public BigDecimal calculateMaternityWage(LocalDate maternityLeaveStartDate,
                                           LocalDate maternityLeaveEndDate, 
                                           BigDecimal monthlyBaseSalary,
                                           BigDecimal adjustedMonthlyBaseSalary) {
        if (maternityLeaveStartDate == null || maternityLeaveEndDate == null) {
            throw new IllegalArgumentException("产假开始时间和结束时间不能为空");
        }
        if (monthlyBaseSalary == null || monthlyBaseSalary.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("月基本工资不能为空且必须大于等于0");
        }
        if (maternityLeaveEndDate.isBefore(maternityLeaveStartDate)) {
            throw new IllegalArgumentException("产假结束时间不能早于开始时间");
        }

        // 调用 WorkdayCalculatorService 获取产假期间每个月的工作日信息
        List<MonthlyWorkdayInfoDO> monthlyWorkdayList = workdayCalculatorService
                .calculateMonthlyWorkdays(maternityLeaveStartDate, maternityLeaveEndDate);

        BigDecimal totalMaternityWage = BigDecimal.ZERO;

        // 遍历每个月的工作日信息，计算当月工资
        for (MonthlyWorkdayInfoDO monthlyWorkday : monthlyWorkdayList) {
            BigDecimal monthlyWage;
            
            // 确定当月使用的基本工资
            BigDecimal currentMonthBaseSalary = monthlyBaseSalary;
            if (adjustedMonthlyBaseSalary != null && monthlyWorkday.getMonth() >= 4) {
                currentMonthBaseSalary = adjustedMonthlyBaseSalary;
                log.debug("{}年{}月使用调整后的月基本工资: {}", 
                         monthlyWorkday.getYear(), monthlyWorkday.getMonth(), currentMonthBaseSalary);
            }

            if (monthlyWorkday.getFullMonth()) {
                // 完整自然月：直接使用月基本工资
                monthlyWage = currentMonthBaseSalary;
                log.debug("{}年{}月为完整月，当月工资: {}", 
                         monthlyWorkday.getYear(), monthlyWorkday.getMonth(), monthlyWage);
            } else {
                // 非完整月：按比例计算
                // 当月工资 = (月基本工资 / 该月法定工作天数) * 该月范围内的工作日天数
                if (monthlyWorkday.getLegalPaydays() == null || monthlyWorkday.getLegalPaydays() == 0) {
                    log.warn("{}年{}月法定工作天数为0，跳过该月", 
                            monthlyWorkday.getYear(), monthlyWorkday.getMonth());
                    continue;
                }
                
                BigDecimal dailyWage = currentMonthBaseSalary.divide(
                        new BigDecimal(monthlyWorkday.getLegalPaydays()),
                        4, 
                        RoundingMode.HALF_UP);
                monthlyWage = dailyWage.multiply(new BigDecimal(monthlyWorkday.getPaydays()));
                
                log.debug("{}年{}月为非完整月，法定工作天数: {}，实际工作天数: {}，当月工资: {}", 
                         monthlyWorkday.getYear(), monthlyWorkday.getMonth(), 
                         monthlyWorkday.getLegalPaydays(), monthlyWorkday.getPaydays(), monthlyWage);
            }

            totalMaternityWage = totalMaternityWage.add(monthlyWage);
        }

        log.info("产假期间应付工资计算完成，总金额: {}", totalMaternityWage);
        return totalMaternityWage.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal calculateStartingMonthMaternityWage(LocalDate maternityLeaveStartDate,
                                                        LocalDate maternityLeaveEndDate,
                                                        BigDecimal monthlyBaseSalary) {
        if (maternityLeaveStartDate == null || maternityLeaveEndDate == null) {
            throw new IllegalArgumentException("产假开始时间和结束时间不能为空");
        }
        if (monthlyBaseSalary == null || monthlyBaseSalary.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("月基本工资不能为空且必须大于等于0");
        }
        if (maternityLeaveEndDate.isBefore(maternityLeaveStartDate)) {
            throw new IllegalArgumentException("产假结束时间不能早于开始时间");
        }

        // 获取产假开始月的第一天和最后一天
        LocalDate startingMonthStart = maternityLeaveStartDate.withDayOfMonth(1);
        LocalDate startingMonthEnd = maternityLeaveStartDate.withDayOfMonth(maternityLeaveStartDate.lengthOfMonth());
        
        // 计算产假在开始月的实际开始和结束日期
        LocalDate actualStartInStartingMonth = maternityLeaveStartDate;
        LocalDate actualEndInStartingMonth = maternityLeaveEndDate.isBefore(startingMonthEnd) ? 
                                           maternityLeaveEndDate : startingMonthEnd;

        try {
            // 使用新的calculatePayrollDaysInRange方法计算该月总计薪日
            YearMonth startingYearMonth = YearMonth.from(maternityLeaveStartDate);
            int totalPayrollDaysInMonth = workdayCalculatorService.calculatePayrollDaysInMonth(startingYearMonth);
            
            // 计算产假期间的计薪日
            int maternityPayrollDays = workdayCalculatorService.calculatePayrollDaysInRange(
                actualStartInStartingMonth, actualEndInStartingMonth);
            
            // 避免除零错误
            if (totalPayrollDaysInMonth == 0) {
                log.warn("产假开始月计薪日天数为0，返回0");
                return BigDecimal.ZERO;
            }
            
            // 计算产假期间计薪日占比
            BigDecimal payrollDayRatio = new BigDecimal(maternityPayrollDays)
                .divide(new BigDecimal(totalPayrollDaysInMonth), 6, RoundingMode.HALF_UP);
            
            // 计算产假开始月的工资 = 月基本工资 * 产假计薪日占比
            BigDecimal startingMonthWage = monthlyBaseSalary.multiply(payrollDayRatio);
            
            log.info("产假开始月({}年{}月)工资计算完成，该月总计薪日: {}，产假计薪日: {}，计薪日占比: {}，月基本工资: {}，产假工资: {}", 
                    maternityLeaveStartDate.getYear(), maternityLeaveStartDate.getMonthValue(),
                    totalPayrollDaysInMonth, maternityPayrollDays, payrollDayRatio, monthlyBaseSalary, startingMonthWage);
            
            return startingMonthWage.setScale(2, RoundingMode.HALF_UP);
            
        } catch (Exception e) {
            log.warn("使用新计薪日计算方法失败，降级为简化计算: {}", e.getMessage());
            // 降级为简化计算
            return calculateSimplifiedStartingMonthWage(actualStartInStartingMonth, actualEndInStartingMonth, 
                                                      startingMonthStart, startingMonthEnd, monthlyBaseSalary);
        }
    }

    @Override
    public BigDecimal calculateEndingMonthMaternityWage(LocalDate maternityLeaveStartDate,
                                                      LocalDate maternityLeaveEndDate,
                                                      BigDecimal monthlyBaseSalary) {
        if (maternityLeaveStartDate == null || maternityLeaveEndDate == null) {
            throw new IllegalArgumentException("产假开始时间和结束时间不能为空");
        }
        if (monthlyBaseSalary == null || monthlyBaseSalary.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("月基本工资不能为空且必须大于等于0");
        }
        if (maternityLeaveEndDate.isBefore(maternityLeaveStartDate)) {
            throw new IllegalArgumentException("产假结束时间不能早于开始时间");
        }

        // 获取产假结束月的第一天和最后一天
        LocalDate endingMonthStart = maternityLeaveEndDate.withDayOfMonth(1);
        LocalDate endingMonthEnd = maternityLeaveEndDate.withDayOfMonth(maternityLeaveEndDate.lengthOfMonth());
        
        // 计算产假在结束月的实际开始和结束日期
        LocalDate actualStartInEndingMonth = maternityLeaveStartDate.isAfter(endingMonthStart) ? 
                                           maternityLeaveStartDate : endingMonthStart;
        LocalDate actualEndInEndingMonth = maternityLeaveEndDate;

        try {
            // 使用新的calculatePayrollDaysInRange方法计算该月总计薪日
            YearMonth endingYearMonth = YearMonth.from(maternityLeaveEndDate);
            int totalPayrollDaysInMonth = workdayCalculatorService.calculatePayrollDaysInMonth(endingYearMonth);
            
            // 计算产假期间的计薪日
            int maternityPayrollDays = workdayCalculatorService.calculatePayrollDaysInRange(
                actualStartInEndingMonth, actualEndInEndingMonth);
            
            // 避免除零错误
            if (totalPayrollDaysInMonth == 0) {
                log.warn("产假结束月计薪日天数为0，返回0");
                return BigDecimal.ZERO;
            }
            
            // 计算产假期间计薪日占比
            BigDecimal payrollDayRatio = new BigDecimal(maternityPayrollDays)
                .divide(new BigDecimal(totalPayrollDaysInMonth), 6, RoundingMode.HALF_UP);
            
            // 计算产假结束月的工资 = 月基本工资 * 产假计薪日占比
            BigDecimal endingMonthWage = monthlyBaseSalary.multiply(payrollDayRatio);
            
            log.info("产假结束月({}年{}月)工资计算完成，该月总计薪日: {}，产假计薪日: {}，计薪日占比: {}，月基本工资: {}，产假工资: {}", 
                    maternityLeaveEndDate.getYear(), maternityLeaveEndDate.getMonthValue(),
                    totalPayrollDaysInMonth, maternityPayrollDays, payrollDayRatio, monthlyBaseSalary, endingMonthWage);
            
            return endingMonthWage.setScale(2, RoundingMode.HALF_UP);
            
        } catch (Exception e) {
            log.warn("使用新计薪日计算方法失败，降级为简化计算: {}", e.getMessage());
            // 降级为简化计算
            return calculateSimplifiedEndingMonthWage(actualStartInEndingMonth, actualEndInEndingMonth, 
                                                    endingMonthStart, endingMonthEnd, monthlyBaseSalary);
        }
    }
    
    /**
     * 简化的产假开始月工资计算（当无法获取工作日信息时使用）
     * 仅考虑周末为非工作日，不考虑调休
     */
    private BigDecimal calculateSimplifiedStartingMonthWage(LocalDate actualStartInStartingMonth,
                                                          LocalDate actualEndInStartingMonth,
                                                          LocalDate startingMonthStart,
                                                          LocalDate startingMonthEnd,
                                                          BigDecimal monthlyBaseSalary) {
        // 计算该月总天数
        int totalDaysInMonth = startingMonthEnd.getDayOfMonth();
        
        // 计算产假在该月的天数
        int maternityDaysInMonth = (int) (actualEndInStartingMonth.toEpochDay() - actualStartInStartingMonth.toEpochDay() + 1);
        
        // 计算该月的周末天数（周六和周日）
        int weekendDaysInMonth = 0;
        LocalDate currentDate = startingMonthStart;
        while (!currentDate.isAfter(startingMonthEnd)) {
            if (currentDate.getDayOfWeek().getValue() == 6 || currentDate.getDayOfWeek().getValue() == 7) {
                weekendDaysInMonth++;
            }
            currentDate = currentDate.plusDays(1);
        }
        
        // 计算产假期间的周末天数
        int weekendDaysInMaternity = 0;
        currentDate = actualStartInStartingMonth;
        while (!currentDate.isAfter(actualEndInStartingMonth)) {
            if (currentDate.getDayOfWeek().getValue() == 6 || currentDate.getDayOfWeek().getValue() == 7) {
                weekendDaysInMaternity++;
            }
            currentDate = currentDate.plusDays(1);
        }
        
        // 简化计算：发薪日 = 总天数 - 周末天数（不考虑调休）
        int payrollDaysInMonth = totalDaysInMonth - weekendDaysInMonth;
        int payrollDaysInMaternity = maternityDaysInMonth - weekendDaysInMaternity;
        
        // 避免除零错误
        if (payrollDaysInMonth == 0) {
            log.warn("产假开始月发薪日天数为0，返回0");
            return BigDecimal.ZERO;
        }
        
        // 计算产假期间计薪日占比
        BigDecimal payrollDayRatio = new BigDecimal(payrollDaysInMaternity)
            .divide(new BigDecimal(payrollDaysInMonth), 4, RoundingMode.HALF_UP);
        
        // 计算产假开始月的工资 = 月基本工资 * 产假计薪日占比
        BigDecimal startingMonthWage = monthlyBaseSalary.multiply(payrollDayRatio);
        
        log.info("产假开始月({}年{}月)简化工资计算完成，该月总天数: {}，该月周末: {}，该月发薪日: {}，产假天数: {}，产假周末: {}，产假发薪日: {}，计薪日占比: {}，月基本工资: {}，产假工资: {}", 
                actualStartInStartingMonth.getYear(), actualStartInStartingMonth.getMonthValue(),
                totalDaysInMonth, weekendDaysInMonth, payrollDaysInMonth, maternityDaysInMonth, weekendDaysInMaternity, payrollDaysInMaternity, payrollDayRatio, monthlyBaseSalary, startingMonthWage);
        
        return startingMonthWage.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 简化的产假结束月工资计算（当无法获取工作日信息时使用）
     * 仅考虑周末为非工作日，不考虑调休
     */
    private BigDecimal calculateSimplifiedEndingMonthWage(LocalDate actualStartInEndingMonth,
                                                        LocalDate actualEndInEndingMonth,
                                                        LocalDate endingMonthStart,
                                                        LocalDate endingMonthEnd,
                                                        BigDecimal monthlyBaseSalary) {
        // 计算该月总天数
        int totalDaysInMonth = endingMonthEnd.getDayOfMonth();
        
        // 计算产假在该月的天数
        int maternityDaysInMonth = (int) (actualEndInEndingMonth.toEpochDay() - actualStartInEndingMonth.toEpochDay() + 1);
        
        // 计算该月的周末天数（周六和周日）
        int weekendDaysInMonth = 0;
        LocalDate currentDate = endingMonthStart;
        while (!currentDate.isAfter(endingMonthEnd)) {
            if (currentDate.getDayOfWeek().getValue() == 6 || currentDate.getDayOfWeek().getValue() == 7) {
                weekendDaysInMonth++;
            }
            currentDate = currentDate.plusDays(1);
        }
        
        // 计算产假期间的周末天数
        int weekendDaysInMaternity = 0;
        currentDate = actualStartInEndingMonth;
        while (!currentDate.isAfter(actualEndInEndingMonth)) {
            if (currentDate.getDayOfWeek().getValue() == 6 || currentDate.getDayOfWeek().getValue() == 7) {
                weekendDaysInMaternity++;
            }
            currentDate = currentDate.plusDays(1);
        }
        
        // 简化计算：发薪日 = 总天数 - 周末天数（不考虑调休）
        int payrollDaysInMonth = totalDaysInMonth - weekendDaysInMonth;
        int payrollDaysInMaternity = maternityDaysInMonth - weekendDaysInMaternity;
        
        // 避免除零错误
        if (payrollDaysInMonth == 0) {
            log.warn("产假结束月发薪日天数为0，返回0");
            return BigDecimal.ZERO;
        }
        
        // 计算产假期间计薪日占比
        BigDecimal payrollDayRatio = new BigDecimal(payrollDaysInMaternity)
            .divide(new BigDecimal(payrollDaysInMonth), 4, RoundingMode.HALF_UP);
        
        // 计算产假结束月的工资 = 月基本工资 * 产假计薪日占比
        BigDecimal endingMonthWage = monthlyBaseSalary.multiply(payrollDayRatio);
        
        log.info("产假结束月({}年{}月)简化工资计算完成，该月总天数: {}，该月周末: {}，该月发薪日: {}，产假天数: {}，产假周末: {}，产假发薪日: {}，计薪日占比: {}，月基本工资: {}，产假工资: {}", 
                actualEndInEndingMonth.getYear(), actualEndInEndingMonth.getMonthValue(),
                totalDaysInMonth, weekendDaysInMonth, payrollDaysInMonth, maternityDaysInMonth, weekendDaysInMaternity, payrollDaysInMaternity, payrollDayRatio, monthlyBaseSalary, endingMonthWage);
        
        return endingMonthWage.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 公共判断：是否跨过4月（用于基数4月调整场景），考虑跨年
     */
    public boolean crossesSalaryAdjustMonth(List<MonthlyWorkdayInfoDO> monthlyWorkdayList) {
        if (monthlyWorkdayList == null || monthlyWorkdayList.isEmpty()) {
            return false;
        }
        int firstYear = monthlyWorkdayList.get(0).getYear();
        int firstMonth = monthlyWorkdayList.get(0).getMonth();
        int lastYear = monthlyWorkdayList.get(monthlyWorkdayList.size() - 1).getYear();
        int lastMonth = monthlyWorkdayList.get(monthlyWorkdayList.size() - 1).getMonth();

        if (firstYear == lastYear) {
            return firstMonth < 4 && lastMonth >= 4;
        } else { // firstYear < lastYear
            return (firstMonth < 4) || (lastMonth >= 4) || (lastYear - firstYear > 1);
        }
    }

    /**
     * 公共判断：是否跨过7月（用于社保基数7月调整场景），考虑跨年
     */
    public boolean crossesSocialAdjustMonth(List<MonthlyWorkdayInfoDO> monthlyWorkdayList) {
        if (monthlyWorkdayList == null || monthlyWorkdayList.isEmpty()) {
            return false;
        }
        int firstYear = monthlyWorkdayList.get(0).getYear();
        int firstMonth = monthlyWorkdayList.get(0).getMonth();
        int lastYear = monthlyWorkdayList.get(monthlyWorkdayList.size() - 1).getYear();
        int lastMonth = monthlyWorkdayList.get(monthlyWorkdayList.size() - 1).getMonth();

        if (firstYear == lastYear) {
            return firstMonth < 7 && lastMonth >= 7;
        } else { // firstYear < lastYear
            return (firstMonth < 7) || (lastMonth >= 7) || (lastYear - firstYear > 1);
        }
    }

    /**
     * 公共判断：是否跨过月
     */
    @Override
    public boolean crossesMonth(List<MonthlyWorkdayInfoDO> monthlyWorkdayList, int month) {
        if (monthlyWorkdayList == null || monthlyWorkdayList.isEmpty()) {
            return false;
        }
        int firstYear = monthlyWorkdayList.get(0).getYear();
        int firstMonth = monthlyWorkdayList.get(0).getMonth();
        int lastYear = monthlyWorkdayList.get(monthlyWorkdayList.size() - 1).getYear();
        int lastMonth = monthlyWorkdayList.get(monthlyWorkdayList.size() - 1).getMonth();

        if (firstYear == lastYear) {
            return firstMonth < month && lastMonth >= month;
        } else { // firstYear < lastYear
            return (firstMonth < month) || (lastMonth >= month) || (lastYear - firstYear > 1);
        }
    }
}
