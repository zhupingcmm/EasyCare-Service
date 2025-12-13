package com.ocbc.ms.easy.care.service.impl;

import com.ocbc.ms.easy.care.calculator.PayrollDayCalculator;
import com.ocbc.ms.easy.care.constant.PublicHolidayConstants;
import com.ocbc.ms.easy.care.domain.HolidayInfo;
import com.ocbc.ms.easy.care.service.HolidayService;
import com.ocbc.ms.easy.care.service.WorkdayCalculatorService;
import com.ocbc.ms.easy.care.domain.MonthlyWorkdayInfoDO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

/**
 * 工作日计算实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkdayCalculatorServiceImpl implements WorkdayCalculatorService {

    private final HolidayService holidayService;

    @Override
    public List<MonthlyWorkdayInfoDO> calculateMonthlyWorkdays(LocalDate start, LocalDate end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("开始/结束日期不能为空");
        }
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("结束日期不能早于开始日期");
        }

        // 收集跨越年份的节假日与调休数据（若无法获取则忽略该年）
        Map<Integer, YearHolidayInfo> holidayInfoByYear = loadHolidayInfoByYear(start, end);

        List<MonthlyWorkdayInfoDO> result = new ArrayList<>();
        LocalDate cursor = start;
        while (!cursor.isAfter(end)) {
            YearMonth ym = YearMonth.from(cursor);
            // 当月范围的开始与结束（裁剪到 [start, end] 范围内）
            LocalDate monthStart = cursor.withDayOfMonth(1);
            LocalDate monthEnd = ym.atEndOfMonth();
            if (monthStart.isBefore(start)) {
                monthStart = start;
            }
            if (monthEnd.isAfter(end)) {
                monthEnd = end;
            }

            int workdays = countWorkdaysInRange(monthStart, monthEnd, holidayInfoByYear);
            int legalWorkdays = countLegalWorkdaysInMonth(ym, holidayInfoByYear);
            int paydays = calculatePayrollDaysInRange(monthStart, monthEnd);
            int legalPaydays = calculatePayrollDaysInMonth(ym);
            boolean fullMonth = !ym.atDay(1).isBefore(start) && !ym.atEndOfMonth().isAfter(end);
            result.add(MonthlyWorkdayInfoDO.builder()
                    .year(ym.getYear())
                    .month(ym.getMonthValue())
                    .workdays(workdays)
                    .legalWorkdays(legalWorkdays)
                    .paydays(paydays)
                    .legalPaydays(legalPaydays)
                    .fullMonth(fullMonth)
                    .build());

            // 跳到下个月第一天
            cursor = ym.atEndOfMonth().plusDays(1);
        }
        return result;
    }

    @Override
    public int countWorkdaysFromDateToMonthEnd(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("日期不能为空");
        }
        LocalDate monthEnd = date.withDayOfMonth(date.lengthOfMonth());
        Map<Integer, YearHolidayInfo> holidayInfoByYear = loadHolidayInfoByYear(date, monthEnd);
        return countWorkdaysInRange(date, monthEnd, holidayInfoByYear);
    }

    @Override
    public int countWorkdaysFromMonthStartToDate(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("日期不能为空");
        }
        LocalDate monthStart = date.withDayOfMonth(1);
        Map<Integer, YearHolidayInfo> holidayInfoByYear = loadHolidayInfoByYear(monthStart, date);
        return countWorkdaysInRange(monthStart, date, holidayInfoByYear);
    }

    private int countWorkdaysInRange(LocalDate from, LocalDate to, Map<Integer, YearHolidayInfo> holidayInfoByYear) {
        int count = 0;
        for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
            DayOfWeek dow = d.getDayOfWeek();
            boolean isWeekend = (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY);

            YearHolidayInfo info = holidayInfoByYear.get(d.getYear());
            if (info == null) {
                // 无法获取该年的节假日/调休数据：按周末规则计算
                if (!isWeekend) {
                    count++;
                }
                continue;
            }

            boolean isTransferWorkday = info.transferWorkdays.contains(d);
            boolean isPublicHoliday = info.publicHolidays.contains(d);

            if (isWeekend) {
                // 周末但为调休工作日 => 计为工作日
                if (isTransferWorkday) {
                    count++;
                }
            } else {
                // 工作日但为公共假日 => 不计工作日；否则计为工作日
                if (!isPublicHoliday) {
                    count++;
                }
            }
        }
        return count;
    }

    private int countLegalWorkdaysInMonth(YearMonth ym, Map<Integer, YearHolidayInfo> holidayInfoByYear) {
        int count = 0;
        for (int day = 1; day <= ym.lengthOfMonth(); day++) {
            LocalDate d = ym.atDay(day);
            DayOfWeek dow = d.getDayOfWeek();
            boolean isWeekend = (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY);

            YearHolidayInfo info = holidayInfoByYear.get(d.getYear());
            if (info == null) {
                // 无法获取该年的节假日/调休数据：按周末规则计算
                if (!isWeekend) {
                    count++;
                }
                continue;
            }

            boolean isTransferWorkday = info.transferWorkdays.contains(d);
            boolean isPublicHoliday = info.publicHolidays.contains(d);

            if (isWeekend) {
                // 周末但为调休工作日 => 计为工作日
                if (isTransferWorkday) {
                    count++;
                }
            } else {
                // 工作日但为公共假日 => 不计工作日；否则计为工作日
                if (!isPublicHoliday) {
                    count++;
                }
            }
        }
        return count;
    }

    private Map<Integer, YearHolidayInfo> loadHolidayInfoByYear(LocalDate start, LocalDate end) {
        Map<Integer, YearHolidayInfo> map = new HashMap<>();
        for (int y = start.getYear(); y <= end.getYear(); y++) {
            try {
                List<Map<String, Object>> items = holidayService.getPublicHolidays(Integer.toString(y));
                if (items == null || items.isEmpty()) {
                    log.warn("{}年节假日接口无数据，忽略节假日/调休，仅按周末计算", y);
                    continue;
                }
                YearHolidayInfo info = new YearHolidayInfo();
                for (Map<String, Object> item : items) {
                    Object dateObj = item.get("date");
                    Object typeObj = item.get("type");
                    if (dateObj == null || typeObj == null) {
                        continue;
                    }
                    LocalDate d;
                    try {
                        d = LocalDate.parse(String.valueOf(dateObj));
                    } catch (Exception e) {
                        continue;
                    }
                    String type = String.valueOf(typeObj);
                    // 约定："public_holiday" 为节假日；"transfer_workday" 为周末调休到工作日
                    if ("public_holiday".equalsIgnoreCase(type)) {
                        info.publicHolidays.add(d);

                    } else if ("transfer_workday".equalsIgnoreCase(type)) {
                        info.transferWorkdays.add(d);
                    }
                }
                map.put(y, info);
            } catch (Exception ex) {
                // 接口异常或不可用：忽略该年节假日信息
                log.warn("加载{}年节假日数据失败：{}，将按周末规则计算该年", y, ex.getMessage());
            }
        }
        return map;
    }

    private static class YearHolidayInfo {
        final Set<LocalDate> publicHolidays = new HashSet<>();
        final Set<LocalDate> transferWorkdays = new HashSet<>();
        final Set<LocalDate> legalHolidays = new HashSet<>();
    }

    /**
     * 计算指定月份的发薪日天数
     * 发薪日 = 所有日期 - 非调休周末日期
     * 例如：2025年4月，非发薪日为5,6,12,13,19,20,26日（周末且非调休），其余均为发薪日
     * 
     * @param yearMonth 年月
     * @return 该月发薪日天数
     */
    public int calculatePayrollDaysInMonth(YearMonth yearMonth) {
        if (yearMonth == null) {
            throw new IllegalArgumentException("年月不能为空");
        }
        
        // 获取该年的节假日与调休数据
        Map<Integer, YearHolidayInfo> holidayInfoByYear = loadHolidayInfoByYear(
            yearMonth.atDay(1), yearMonth.atEndOfMonth());
        
        int payrollDays = 0;
        int totalDays = yearMonth.lengthOfMonth();
        for (int day = 1; day <= totalDays; day++) {
            LocalDate date = yearMonth.atDay(day);
            DayOfWeek dow = date.getDayOfWeek();
            boolean isWeekend = (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY);

            YearHolidayInfo info = holidayInfoByYear.get(date.getYear());
            if (info == null) {
                // 无法获取该年的节假日/调休数据：按周末规则计算
                if (!isWeekend) {
                    payrollDays++;
                } else {
                    // 周末为非发薪日，不计入
                }
            } else {
                boolean isTransferWorkday = info.transferWorkdays.contains(date);
                boolean isPublicHoliday = info.publicHolidays.contains(date);
                boolean isLegalHoliday = PublicHolidayConstants.HOLIDAY.contains(date);

                if (isWeekend) {
                    // 周末但为调休工作日 => 计为发薪日
                    if (isTransferWorkday || isLegalHoliday) {
                        payrollDays++;
                    }
                    // 否则周末为非发薪日，不计入
                } else {
                    if (!isPublicHoliday || isLegalHoliday) {
                        // 非周末工作日和法定假日为发薪日
                        payrollDays++;
                    }
                }
            }
        }

        log.debug("{}年{}月发薪日计算完成，总天数: {}，发薪日: {}",
                yearMonth.getYear(), yearMonth.getMonthValue(), totalDays, payrollDays);
        
        return payrollDays;
    }
    
    /**
     * 计算指定日期范围内的发薪日天数
     * 发薪日 = 所有日期 - 非调休周末日期
     * 
     * @param start 开始日期（包含）
     * @param end 结束日期（包含）
     * @return 该范围内发薪日天数
     */
    public int calculatePayrollDaysInRange(LocalDate start, LocalDate end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("开始/结束日期不能为空");
        }
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("结束日期不能早于开始日期");
        }
        
        // 获取跨越年份的节假日与调休数据
        Map<Integer, YearHolidayInfo> holidayInfoByYear = loadHolidayInfoByYear(start, end);
        
        int payrollDays = 0;
        
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            DayOfWeek dow = date.getDayOfWeek();
            boolean isWeekend = (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY);
            
            YearHolidayInfo info = holidayInfoByYear.get(date.getYear());
            if (info == null) {
                // 无法获取该年的节假日/调休数据：按周末规则计算
                if (!isWeekend) {
                    payrollDays++;
                }
                // 周末为非发薪日，不计入
            } else {
                boolean isTransferWorkday = info.transferWorkdays.contains(date);
                boolean isPublicHoliday = info.publicHolidays.contains(date);
                boolean isLegalHoliday = PublicHolidayConstants.HOLIDAY.contains(date);
                
                if (isWeekend) {
                    // 周末但为调休工作日 => 计为发薪日
                    if (isTransferWorkday || isLegalHoliday) {
                        payrollDays++;
                    }
                    // 否则周末为非发薪日，不计入
                } else {
                    if (!isPublicHoliday || isLegalHoliday) {
                        // 非周末工作日和法定假日为发薪日
                        payrollDays++;
                    }
                }
            }
        }
        
        log.debug("{}到{}发薪日计算完成，发薪日: {}", start, end, payrollDays);
        
        return payrollDays;
    }
    
    @Override
    public List<MonthlyWorkdayInfoDO> calculateMonthlyWorkdaysWithHolidayMap(
            LocalDate start, 
            LocalDate end,
            Map<LocalDate, HolidayInfo> holidayMap) {
        
        if (start == null || end == null) {
            throw new IllegalArgumentException("开始/结束日期不能为空");
        }
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("结束日期不能早于开始日期");
        }
        
        PayrollDayCalculator calculator = new PayrollDayCalculator(holidayMap);
        List<MonthlyWorkdayInfoDO> result = new ArrayList<>();
        
        LocalDate cursor = start;
        int monthIndex = 0;
        
        // 先计算总月数，用于判断首月和尾月
        YearMonth startYm = YearMonth.from(start);
        YearMonth endYm = YearMonth.from(end);
        int totalMonths = (int) startYm.until(endYm, java.time.temporal.ChronoUnit.MONTHS) + 1;
        
        while (!cursor.isAfter(end)) {
            YearMonth ym = YearMonth.from(cursor);
            
            // 当月范围的开始与结束（裁剪到 [start, end] 范围内）
            LocalDate monthStart = ym.atDay(1);
            LocalDate monthEnd = ym.atEndOfMonth();
            LocalDate rangeStart = monthStart.isBefore(start) ? start : monthStart;
            LocalDate rangeEnd = monthEnd.isAfter(end) ? end : monthEnd;
            
            // 计算工作日和计薪日
            int workdays = calculator.calculateLeaveDays(rangeStart, rangeEnd);
            int legalWorkdays = calculator.calculateLeaveDays(monthStart, monthEnd);
            int paydays = calculator.calculatePayrollDays(rangeStart, rangeEnd);
            int legalPaydays = calculator.calculatePayrollDays(monthStart, monthEnd);
            
            // 完整月的定义：
            // 1. 首月（monthIndex == 0）：从该月1号开始
            // 2. 尾月（monthIndex == totalMonths - 1）：到该月最后一天结束
            // 3. 中间月：一定是完整月（产假是连续的）
            boolean fullMonth;
            if (totalMonths == 1) {
                // 只有一个月：必须从1号开始且到月末结束
                fullMonth = monthStart.equals(start) && monthEnd.equals(end);
            } else if (monthIndex == 0) {
                // 首月：从1号开始
                fullMonth = monthStart.equals(start);
            } else if (monthIndex == totalMonths - 1) {
                // 尾月：到月末结束
                fullMonth = monthEnd.equals(end);
            } else {
                // 中间月：一定是完整月
                fullMonth = true;
            }
            
            result.add(MonthlyWorkdayInfoDO.builder()
                .year(ym.getYear())
                .month(ym.getMonthValue())
                .workdays(workdays)
                .legalWorkdays(legalWorkdays)
                .paydays(paydays)
                .legalPaydays(legalPaydays)
                .fullMonth(fullMonth)
                .build());
            
            cursor = monthEnd.plusDays(1);
            monthIndex++;
        }
        
        log.debug("计算月度工作日信息完成，共{}个月", result.size());
        return result;
    }
    
    /**
     * 计算单月的工作日信息
     * 
     * @param start 开始日期
     * @param end 结束日期
     * @param holidayMap 节假日映射
     * @return 单月工作日信息
     */
    public MonthlyWorkdayInfoDO calculateSingleMonthWorkday(
            LocalDate start,
            LocalDate end,
            Map<LocalDate, HolidayInfo> holidayMap) {
        
        // 参数处理：如果start为null，end不为null，则start为end所在月第一天
        if (start == null && end != null) {
            start = end.withDayOfMonth(1);
        }
        // 如果end为null，start不为null，则end为start所在月最后一天
        else if (end == null && start != null) {
            end = start.withDayOfMonth(start.lengthOfMonth());
        }
        // 如果end和start同时为null，则抛出异常
        else if (start == null && end == null) {
            throw new IllegalArgumentException("开始日期和结束日期不能同时为空");
        }
        
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("结束日期不能早于开始日期");
        }
        
        YearMonth ym = YearMonth.from(start);
        PayrollDayCalculator calculator = new PayrollDayCalculator(holidayMap);
        
        // 当月范围的开始与结束
        LocalDate monthStart = ym.atDay(1);
        LocalDate monthEnd = ym.atEndOfMonth();
        LocalDate rangeStart = monthStart.isBefore(start) ? start : monthStart;
        LocalDate rangeEnd = monthEnd.isAfter(end) ? end : monthEnd;
        
        // 计算工作日和计薪日
        int workdays = calculator.calculateLeaveDays(rangeStart, rangeEnd);
        int legalWorkdays = calculator.calculateLeaveDays(monthStart, monthEnd);
        int paydays = calculator.calculatePayrollDays(rangeStart, rangeEnd);
        int legalPaydays = calculator.calculatePayrollDays(monthStart, monthEnd);
        
        // 判断是否为完整月：从1号开始且到月末结束
        boolean fullMonth = paydays == legalPaydays;
        
        return MonthlyWorkdayInfoDO.builder()
                .year(ym.getYear())
                .month(ym.getMonthValue())
                .workdays(workdays)
                .legalWorkdays(legalWorkdays)
                .paydays(paydays)
                .legalPaydays(legalPaydays)
                .fullMonth(fullMonth)
                .build();
    }
}
