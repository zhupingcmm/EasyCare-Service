package com.hr.maternity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
     * 城市
     */
    private String city;

    /**
     * 产假类型
     */
    private MaternityLeaveTypeResponse maternityLeaveType;

    /**
     * 假期天数
     */
    private Integer leaveDays;

    /**
     * 是否节假日顺延
     */
    private Boolean isExtendable;

    /**
     * 是否有津贴
     */
    private Boolean hasAllowance;

    /**
     * 是否默认选择
     */
    private Boolean isDefault;

    /**
     * 单选分组标识
     */
    private Integer radioGroup;

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
