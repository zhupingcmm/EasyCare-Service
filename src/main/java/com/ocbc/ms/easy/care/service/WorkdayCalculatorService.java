package com.ocbc.ms.easy.care.service;

import com.ocbc.ms.easy.care.calculator.PayrollDayCalculator;
import com.ocbc.ms.easy.care.domain.HolidayInfo;
import com.ocbc.ms.easy.care.domain.MonthlyWorkdayInfoDO;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

/**
 * 工作日计算服务
 *
 * 计算指定开始日期和结束日期之间，每个月内的工作日天数：
 * - 默认将周一至周五视为工作日，周六周日视为休息日；
 * - 结合 {@link HolidayService} 提供的公共假日与调休（补班）信息进行修正；
 * - 若某年份 {@link HolidayService} 无法返回公共假日/调休数据，则忽略该年份的节假日与调休，仅按自然周末计算。
 */
public interface WorkdayCalculatorService {

    /**
     * 计算并返回每个月的工作日详情列表，包含 yearMonth、workdays、fullMonth。
     * fullMonth 含义：若该月第一天不早于 start 且该月最后一天不晚于 end 则为 true。
     *
     * @param start 开始日期（包含）
     * @param end   结束日期（包含）
     * @return 每个月的工作日详情列表
     * @throws IllegalArgumentException 当 start 晚于 end 时抛出
     */
    List<MonthlyWorkdayInfoDO> calculateMonthlyWorkdays(LocalDate start, LocalDate end);

    /**
     * 计算指定日期至当月月末（含）的工作日数
     */
    int countWorkdaysFromDateToMonthEnd(LocalDate date);

    /**
     * 计算当月月初至指定日期（含）的工作日数
     */
    int countWorkdaysFromMonthStartToDate(LocalDate date);

    /**
     * 计算指定年月的发薪日天数
     * 发薪日 = 所有日期 - 非调休周末日期
     * 
     * @param yearMonth 年月
     * @return 该月发薪日天数
     * @throws IllegalArgumentException 当 yearMonth 为空时抛出
     */
    int calculatePayrollDaysInMonth(YearMonth yearMonth);

    /**
     * 计算指定日期范围内的发薪日天数
     * 发薪日 = 所有日期 - 非调休周末日期
     * 
     * @param start 开始日期（包含）
     * @param end 结束日期（包含）
     * @return 该范围内发薪日天数
     * @throws IllegalArgumentException 当开始/结束日期为空或结束日期早于开始日期时抛出
     */
    int calculatePayrollDaysInRange(LocalDate start, LocalDate end);
    
    /**
     * 计算月度工作日信息（使用提供的节假日数据）
     * 优化版本：避免重复查询数据库
     * 
     * @param start 开始日期
     * @param end 结束日期
     * @param holidayMap 节假日数据映射
     * @return 月度工作日信息列表
     */
    List<MonthlyWorkdayInfoDO> calculateMonthlyWorkdaysWithHolidayMap(
        LocalDate start, 
        LocalDate end,
        Map<LocalDate, HolidayInfo> holidayMap);

    MonthlyWorkdayInfoDO calculateSingleMonthWorkday(
            LocalDate start,
            LocalDate end,
            Map<LocalDate, HolidayInfo> holidayMap);

}
