package com.easy.care.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 每月工作日详情 DO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyWorkdayInfoDO {
    /** 年，例如 2025 */
    private Integer year;
    /** 月，1-12 */
    private Integer month;
    /** 该月范围内的工作日天数 */
    private Integer workdays;
    /** 该月法定工作天数 */
    private Integer legalWorkdays;
    /** 该月法定工作天数 */
    private Integer paydays;
    /** 该月法定工作天数 */
    private Integer legalPaydays;
    /** 是否为完整自然月（该月整月都在查询范围内） */
    private Boolean fullMonth;
}
