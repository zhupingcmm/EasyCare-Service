package com.hr.maternity.service;

import com.hr.maternity.domain.MonthlyWorkdayInfoDO;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 产假应付工资计算服务
 */
public interface MaternityWageCalculatorService {
    
    /**
     * 计算产假应付工资
     * 
     * @param maternityLeaveStartDate 产假开始时间
     * @param maternityLeaveEndDate 产假结束时间
     * @param monthlyBaseSalary 月基本工资
     * @param adjustedMonthlyBaseSalary 调整后的月基本工资（从4月份开始使用，可为null）
     * @return 产假应付工资总额
     */
    BigDecimal calculateMaternityWage(LocalDate maternityLeaveStartDate, 
                                     LocalDate maternityLeaveEndDate, 
                                     BigDecimal monthlyBaseSalary,
                                     BigDecimal adjustedMonthlyBaseSalary);
    
    /**
     * 计算产假结束月的产假天数和对应工资
     * 
     * @param maternityLeaveStartDate 产假开始时间
     * @param maternityLeaveEndDate 产假结束时间
     * @param monthlyBaseSalary 月基本工资
     * @return 产假结束月的工资金额
     */
    BigDecimal calculateEndingMonthMaternityWage(LocalDate maternityLeaveStartDate,
                                               LocalDate maternityLeaveEndDate,
                                               BigDecimal monthlyBaseSalary);
    
    /**
     * 计算产假开始月的产假天数和对应工资
     * 
     * @param maternityLeaveStartDate 产假开始时间
     * @param maternityLeaveEndDate 产假结束时间
     * @param monthlyBaseSalary 月基本工资
     * @return 产假开始月的工资金额
     */
    BigDecimal calculateStartingMonthMaternityWage(LocalDate maternityLeaveStartDate,
                                                 LocalDate maternityLeaveEndDate,
                                                 BigDecimal monthlyBaseSalary);

    /**
     * 公共判断：是否跨过4月（用于基数4月调整场景），考虑跨年
     * @param monthlyWorkdayList 产假期间的每月工作日信息
     * @return 是否跨过4月
     */
    boolean crossesSalaryAdjustMonth(List<MonthlyWorkdayInfoDO> monthlyWorkdayList);

     /**
     * 公共判断：是否跨过4月（用于基数4月调整场景），考虑跨年
     * @param monthlyWorkdayList 产假期间的每月工作日信息
     * @return 是否跨过4月
     */
    boolean crossesMonth(List<MonthlyWorkdayInfoDO> monthlyWorkdayList, int month);

    /**
     * 公共判断：是否跨过7月（用于社保基数7月调整场景），考虑跨年
     * @param monthlyWorkdayList 产假期间的每月工作日信息
     * @return 是否跨过7月
     */
    boolean crossesSocialAdjustMonth(List<MonthlyWorkdayInfoDO> monthlyWorkdayList);
    
}
