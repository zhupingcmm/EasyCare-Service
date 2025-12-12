package com.ocbc.ms.easy.care.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 产假规则请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaternityRulesRequest {

    /**
     * 城市代码
     */
//    @NotNull(message = "城市代码不能为空")
    private String cityCode;

    /**
     * 产假类型代码
     */
//    @NotNull(message = "产假类型代码不能为空")
    private String maternityLeaveTypeCode;

    /**
     * 默认假期天数
     */
//    @NotNull(message = "默认假期天数不能为空")
    @Min(value = 1, message = "默认假期天数必须大于0")
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
//    @NotNull(message = "产假是否顺延不能为空")
    private Boolean holidayExtend;

    /**
     * 是否有津贴
     */
//    @NotNull(message = "是否有津贴不能为空")
    private Boolean hasAllowance;

    /**
     * 津贴计发天数
     */
    private Integer planAllowanceDay;

    /**
     * 是否启用
     */
    @Builder.Default
    private Boolean enabled = true;
}
