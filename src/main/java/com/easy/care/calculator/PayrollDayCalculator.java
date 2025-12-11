package com.easy.care.calculator;

import com.easy.care.domain.HolidayInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Map;

/**
 * 计薪日计算器
 * 负责计算工作日和计薪日天数
 */
@Slf4j
@RequiredArgsConstructor
public class PayrollDayCalculator {
    
    private final Map<LocalDate, HolidayInfo> holidayMap;
    
    /**
     * 计算日期范围内的计薪日天数
     * 计薪日规则：
     * 1. 排除周六、周日（非调休日）
     * 2. 排除节假日（type=public_holiday）
     * 3. 包含调休工作日（type=transfer_workday）
     * 4. 包含法定假日但需计薪的日期（isPublicHoliday=true && type=public_holiday）
     * 
     * @param start 开始日期（包含）
     * @param end 结束日期（包含）
     * @return 计薪日天数
     */
    public int calculatePayrollDays(LocalDate start, LocalDate end) {
        int count = 0;
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            if (isPayrollDay(date)) {
                count++;
            }
        }

        return count;
    }
    
    /**
     * 判断是否为计薪日
     */
    public boolean isPayrollDay(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        boolean isWeekend = (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY);
        
        HolidayInfo holiday = holidayMap.get(date);
        
        if (holiday == null) {
            // 无节假日信息，按周末规则
            return !isWeekend;
        }
        
        // 调休工作日：即使是周末也是计薪日
        if ("transfer_workday".equals(holiday.getType())) {
            return true;
        }
        
        // 法定假日且需计薪
        if (Boolean.TRUE.equals(holiday.getIsPublicHoliday()) && "public_holiday".equals(holiday.getType())) {
            return true;
        }
        
        // 普通节假日：不是计薪日
        if ("public_holiday".equals(holiday.getType())) {
            return false;
        }
        
        // 其他情况按周末规则
        return !isWeekend;
    }
    
    /**
     * 计算请假天数（工作日）
     * 工作日规则：
     * 1. 排除周六、周日（非调休日）
     * 2. 排除节假日（type=public_holiday）
     * 3. 包含调休工作日（type=transfer_workday）
     * 
     * @param start 开始日期（包含）
     * @param end 结束日期（包含）
     * @return 工作日天数
     */
    public int calculateLeaveDays(LocalDate start, LocalDate end) {
        int count = 0;
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            if (isWorkday(date)) {
                count++;
            }
        }

        return count;
    }
    
    /**
     * 判断是否为工作日
     */
    public boolean isWorkday(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        boolean isWeekend = (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY);
        
        HolidayInfo holiday = holidayMap.get(date);
        
        if (holiday == null) {
            return !isWeekend;
        }
        
        // 调休工作日：即使是周末也是工作日
        if ("transfer_workday".equals(holiday.getType())) {
            return true;
        }
        
        // 节假日：不是工作日
        if ("public_holiday".equals(holiday.getType())) {
            return false;
        }
        
        return !isWeekend;
    }
    
    /**
     * 计算整月的计薪日天数
     */
    public int calculateMonthPayrollDays(YearMonth yearMonth) {
        return calculatePayrollDays(yearMonth.atDay(1), yearMonth.atEndOfMonth());
    }
    
    /**
     * 计算整月的工作日天数
     */
    public int calculateMonthWorkdays(YearMonth yearMonth) {
        return calculateLeaveDays(yearMonth.atDay(1), yearMonth.atEndOfMonth());
    }
}
