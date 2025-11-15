package com.hr.maternity.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 生育津贴计算请求DTO
 */
@Data
public class MaternityAllowanceRequest {
    
    /**
     * 产假申请记录ID（可选，用于关联产假申请）
     */
    private Long requestId;
    
    @NotBlank(message = "工号不能为空")
    private String lanId;
    
    @NotBlank(message = "姓名不能为空")
    private String employeeName;
    
    @NotBlank(message = "城市代码不能为空")
    private String cityCode;

    @PositiveOrZero(message = "单位月平均工资必须大于等于0")
    private BigDecimal unitMonthlyAverageSalary;

    /**
     * 月基本工资（可选）
     */
    @PositiveOrZero(message = "月基本工资必须大于等于0")
    private BigDecimal monthlyBaseSalary;
    
    /**
     * 调整月基本工资（可选）
     */
    @PositiveOrZero(message = "调整月基本工资必须大于等于0")
    private BigDecimal adjustedMonthlyBaseSalary;
    
    @NotNull(message = "产假天数不能为空")
    @Positive(message = "产假天数必须大于0")
    private Integer maternityLeaveDays;
    
    /**
     * 产假开始时间
     */
    @NotNull(message = "产假开始时间不能为空")
    private LocalDate maternityLeaveStartDate;
    
    /**
     * 产假结束时间
     */
    @NotNull(message = "产假结束时间不能为空")
    private LocalDate maternityLeaveEndDate;
    
    /**
     * 产假申请日期（可选，为空时默认为产假开始日期）
     */
    private LocalDate maternityLeaveRequestDate;
    
    /**
     * 公司垫付信息（可选）
     * 支持增加和删除操作的Map结构
     * addition: 增加项目的Map，key为项目名称，value为金额
     * deleteItem: 删除项目的Map，key为项目名称，value为金额
     */
    private CompanyAdvanceMap companyAdvance;
    
    /**
     * 政府发放金额
     */
    @NotNull(message = "政府发放金额不能为空")
    @PositiveOrZero(message = "政府发放金额必须大于等于0")
    private BigDecimal governmentAllowance;
    
    /**
     * 产前12个月的月均工资
     */
    @NotNull(message = "产前12个月的月均工资不能为空")
    @PositiveOrZero(message = "产前12个月的月均工资必须大于等于0")
    private BigDecimal averageSalaryPast12Months;
    
    /**
     * 复合校验：结束时间不得早于开始时间
     */
    @AssertTrue(message = "产假结束时间不能早于开始时间")
    private boolean isMaternityLeaveDatesValid() {
        if (maternityLeaveStartDate == null || maternityLeaveEndDate == null) {
            // 交由字段级 @NotNull 处理
            return true;
        }
        return !maternityLeaveEndDate.isBefore(maternityLeaveStartDate);
    }
    
    /**
     * 获取产假申请日期，如果为空则返回产假开始日期
     */
    public LocalDate getMaternityLeaveRequestDate() {
        return maternityLeaveRequestDate != null ? maternityLeaveRequestDate : maternityLeaveStartDate;
    }
}

