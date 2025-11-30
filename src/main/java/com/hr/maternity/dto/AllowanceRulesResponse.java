package com.hr.maternity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 津贴规则响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AllowanceRulesResponse {

    /**
     * 主键ID
     */
    private Integer id;

    /**
     * 城市
     */
    private String city;

    /**
     * 发放方式
     */
    private String payoutMethod;

    /**
     * 是否启用
     */
    private Boolean isActive;

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
