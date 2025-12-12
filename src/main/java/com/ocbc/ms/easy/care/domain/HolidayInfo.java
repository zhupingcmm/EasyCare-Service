package com.ocbc.ms.easy.care.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 节假日信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HolidayInfo {
    /** 日期 */
    private LocalDate date;
    
    /** 节假日名称 */
    private String name;
    
    /** 是否为法定假日 */
    private Boolean isPublicHoliday;
    
    /** 类型：public_holiday 或 transfer_workday */
    private String type;
}
