package com.hr.maternity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * 产假规则响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaternityRulesResponse {

    /**
     * 主键ID
     */
    private Integer id;

    /**
     * 城市ID
     */
    private Integer cityId;

    /**
     * 城市名称
     */
    private String cityName;

    /**
     * 产假类型
     */
    private MaternityLeaveTypeResponse maternityLeaveType;

    /**
     * 默认假期天数
     */
    private Integer defaultDays;

    /**
     * 医嘱天数
     */
    private Integer doctorRecommendDays;

    /**
     * 产假扩展信息（可以是JSONArray或JSONObject）
     */
    private Object maternityLeaveExt;

    /**
     * 产假是否顺延
     */
    private Boolean holidayExtend;

    /**
     * 是否有津贴
     */
    private Boolean hasAllowance;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 创建时间
     */
    private LocalDateTime createDate;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 更新时间
     */
    private LocalDateTime updateDate;

    /**
     * 更新人
     */
    private String updateBy;
}
