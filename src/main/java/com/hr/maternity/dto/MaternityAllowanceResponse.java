package com.hr.maternity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 生育津贴计算响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaternityAllowanceResponse {
    
    /**
     * 工号
     */
    private String lanId;
    
    /**
     * 姓名
     */
    private String employeeName;
    
    /**
     * 城市代码
     */
    private String cityCode;
    
    /**
     * 城市名称
     */
    private String cityName;

    /**
     * 享受津贴天数
     */
    private Integer allowanceDays;
    
    /**
     * 额外补贴（多胞胎等）
     */
    private BigDecimal extraAllowance;
    
    /**
     * 生育津贴金额
     */
    private BigDecimal maternityAllowance;
    
    /**
     * 补差金额
     */
    private BigDecimal compensationAmount;
    
    /**
     * 产假应付工资
     */
    private BigDecimal paidMaternityWage;
    
    /**
     * 员工返还金额
     */
    private BigDecimal employeeRefundAmount;

    /**
     * 津贴补差计算详情
     * 支持String类型（单个详情描述）或List<String>类型（多个详情项）
     */
    private Object allowanceCompensationDetails;

    /**
     * 返还计算详情
     * 支持String类型（单个详情描述）或List<String>类型（多个详情项）
     */
    private Object refundDetails;
}

