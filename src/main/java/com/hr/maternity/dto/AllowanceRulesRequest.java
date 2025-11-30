package com.hr.maternity.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 津贴规则请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AllowanceRulesRequest {

    /**
     * 城市
     */
    @NotBlank(message = "城市不能为空")
    private String city;

    /**
     * 发放方式
     */
    @NotBlank(message = "发放方式不能为空")
    private String payoutMethod;
}
