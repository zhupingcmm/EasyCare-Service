package com.easy.care.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 产假数据 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VacationDataDTO {
    
    /** 员工姓名 */
    private String employeeName;
    
    /** 员工工号 */
    private String employeeId;
    
    /** 城市代码 */
    private String city;
    
    /** 开始日期 */
    private LocalDate startDate;
    
    /** 生育类型 */
    private String birthType;
    
    /** 是否有延长天数 */
    private Boolean hasExtendedDays;
    
    /** 是否有奖励假 */
    private Boolean rewardLeave;
    
    /** 分娩方式 */
    private String deliveryMethod;
    
    /** 计算天数 */
    private Integer calculatedDays;
    
    /** 总津贴天数 */
    private Integer totalAllowanceDays;
    
    /** 产假开始日期 */
    private LocalDate maternityLeaveStartDate;
    
    /** 产假结束日期 */
    private LocalDate maternityLeaveEndDate;
}
