package com.hr.maternity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 产假政策详情DTO（用于ext.detail）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaternityPolicyDetailDTO {

    /**
     * 代码
     */
    private String code;

    /**
     * 值
     */
    private String value;

    /**
     * 描述
     */
    private String desc;
}
