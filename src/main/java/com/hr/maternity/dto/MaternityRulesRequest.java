package com.hr.maternity.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 产假规则请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaternityRulesRequest {

    /**
     * 城市
     */
    @NotBlank(message = "城市不能为空")
    private String city;

    /**
     * 产假类型（如：产假、陪产假）
     */
    @NotBlank(message = "产假类型不能为空")
    private String maternityLeaveType;

    /**
     * 流产类型（如：早期流产、晚期流产，可为空）
     */
    private String abortionLeaveType;

    /**
     * 假期天数
     */
    @NotNull(message = "假期天数不能为空")
    @Min(value = 1, message = "假期天数必须大于0")
    private Integer leaveDays;

    /**
     * 是否节假日顺延
     */
    @NotNull(message = "是否节假日顺延不能为空")
    private Boolean isExtendable;

    /**
     * 是否有津贴
     */
    @NotNull(message = "是否有津贴不能为空")
    private Boolean hasAllowance;

    /**
     * 是否默认选择
     */
    @NotNull(message = "是否默认选择不能为空")
    private Boolean isDefault;

    /**
     * 单选分组标识
     */
    @NotNull(message = "单选分组标识不能为空")
    private Integer radioGroup;

    /**
     * 是否启用
     */
    private Boolean isActive;
}
