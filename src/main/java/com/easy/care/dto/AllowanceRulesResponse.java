package com.easy.care.dto;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.poi.hpsf.Decimal;

import java.math.BigDecimal;
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
     * 城市代码
     */
    private String cityCode;

    /**
     * 发放方式
     */
    private Integer payoutMethod;

    /**
     * 是否启用（用于逻辑删除）
     */
    private Boolean enabled;

    /**
     * 是否需要补差
     */
    private Boolean needCompensation;

    /**
     * 薪资调整月份
     */
    private Integer salaryAdjustMonth;

    /**
     * 社保调整月份
     */
    private Integer socialAdjustMonth;

    /**
     *  一个月的天数，计算日薪资时使用
     */
    private BigDecimal monthDays;

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
