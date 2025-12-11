package com.hr.maternity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 产假计算响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaternityLeaveResponse {
    
    /**
     * 申请记录ID
     */
    private Long requestId;
    
    /**
     * 计算结果ID
     */
    private Long resultId;
    
    /**
     * 工号
     */
    private String lanId;
    
    /**
     * 姓名
     */
    private String employeeName;
    
    /**
     * 城市代码
     */
    private String cityCode;
    
    /**
     * 城市名称
     */
    private String cityName;
    
    /**
     * 产假总天数
     */
    private Integer totalDays;
    
    /**
     * 津贴总天数
     */
    private Integer totalAllowanceDays;
    
    /**
     * 1. 基础产假天数
     */
    private Integer baseDays;

    /**
     * 2 难产假
     */
    private Integer dystociaDays;

    /**
     * 3 多胞胎
     */
    private Integer multiBabyDays;

    /**
     * 4 晚育假/生育假/奖励假
     */
    private Integer extendedDays;

    /**
     *
     */
    private Integer miscarriageLeaveDays;

    /**
     * 公共节假日顺延天数（4 晚育假/生育假/奖励假 期间的）
     */
    private Integer pubHolidaysCount = 0;

    /**
     * 产假开始日期
     */
    private LocalDate startDate;
    
    /**
     * 产假结束日期
     */
    private LocalDate endDate;

    /**
     * 返岗日期： endDate之后的第一个工作日
     */
    private LocalDate returnToWorkDate;

    /**
     * 每个假期时间段的详情
     */
    private List<TimeScope> timeScopeList;
}
