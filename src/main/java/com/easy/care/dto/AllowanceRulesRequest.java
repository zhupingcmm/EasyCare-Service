package com.easy.care.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 津贴规则请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AllowanceRulesRequest {

    /**
     * 城市代码
     */
//    @NotBlank(message = "城市代码不能为空")
    private String cityCode;

    /**
     * 发放方式
     */
//    @NotBlank(message = "发放方式不能为空")
    private Integer payoutMethod;

    /**
     * 是否需要补偿
     */
    private Boolean needCompensation;

    /**
     * 工资调整月份
     */
    private Integer salaryAdjustMonth;

    /**
     * 社保调整月份
     */
    private Integer socialAdjustMonth;

    /**
     * 每月天数
     */
    private BigDecimal monthDays;

}
