package com.ocbc.ms.easy.care.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 产假天数结果，包含实际休假天数与可享受津贴天数。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaternityLeaveDaysResult {

    /**
     * 享有的产假天数
     */
    private Integer leaveDays;

    /**
     * 可计发津贴的天数
     */
    private Integer allowanceDays;

    private LocalDate originEndDate;

    /**
     * 遇节假日顺延天数
     */
    private Integer extendDays;

    private Integer adjustLeaveDays;

    private LocalDate adjustEndDate;


    public Integer getAdjustLeaveDays() {
        return leaveDays + extendDays;
    }
}
