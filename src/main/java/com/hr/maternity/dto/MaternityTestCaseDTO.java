package com.hr.maternity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 产假津贴测试用例 DTO
 * 映射 Excel 中的测试用例字段
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaternityTestCaseDTO {
    
    // ========== 基本信息 ==========
    /** 用例编号 */
    private String caseNumber;
    
    /** 用例描述 */
    private String caseDescription;
    
    /** 员工工号 */
    private String employeeId;
    
    /** 员工姓名 */
    private String employeeName;
    
    // ========== 产假相关 ==========
    /** 城市代码 */
    private String cityCode;
    
    /** 生育方式 */
    private String deliveryMethod;
    
    /** 胎数 */
    private Integer numberOfBabies;
    
    /** 是否有奖励假 */
    private Boolean hasRewardLeave;
    
    /** 广州难产类型1：剖腹产、会阴III度破裂 */
    private String guangzhouDifficultType1;
    
    /** 广州难产类型2：吸引产、钳产、臀位牵引产 */
    private String guangzhouDifficultType2;
    
    /** 产假开始时间 */
    private LocalDate leaveStartDate;
    
    // ========== 工资相关 ==========
    /** 员工产前12个月的月平均工资 */
    private BigDecimal avgSalaryBefore12Months;
    
    /** 政府发放津贴 */
    private BigDecimal governmentAllowance;
    
    /** 单位申报上年度月平均工资 */
    private BigDecimal declaredAvgSalaryLastYear;
    
    /** 是否跨4月调薪 */
    private Boolean hasSalaryAdjustmentInApril;
    
    /** 调薪前工资 */
    private BigDecimal salaryBeforeAdjustment;
    
    /** 调薪后工资 */
    private BigDecimal salaryAfterAdjustment;
    
    /** 基本工资 */
    private BigDecimal baseSalary;
    
    /** 发放时间 */
    private LocalDate paymentDate;
    
    /** 提交核定表时间 */
    private LocalDate submissionDate;
    
    // ========== 社保相关 ==========
    /** 是否跨7月社保调整 */
    private Boolean hasSocialSecurityAdjustmentInJuly;
    
    /** 调整前个人社保公积金合计 */
    private BigDecimal socialSecurityBeforeAdjustment;
    
    /** 调整后个人社保公积金合计 */
    private BigDecimal socialSecurityAfterAdjustment;
    
    /** 月度个人社保公积金合计 */
    private BigDecimal monthlySocialSecurity;
    
    // ========== 其他项目 ==========
    /** 弹性福利 */
    private BigDecimal flexibleBenefits;
    
    /** ESPP */
    private BigDecimal espp;
    
    /** 个人工会费 */
    private BigDecimal unionFee;
    
    /** 其他奖励项目 */
    private BigDecimal otherRewards;
    
    /** Spot on */
    private BigDecimal spotOn;
    
    /** 其他扣除项 */
    private BigDecimal otherDeductions;
    
    // ========== 期望结果 ==========
    /** 产假结束日期 */
    private LocalDate expectedLeaveEndDate;
    
    /** 总产假天数 */
    private Integer expectedTotalLeaveDays;
    
    /** 预计返岗日期 */
    private LocalDate expectedReturnDate;
    
    /** 津贴天数 */
    private Integer expectedAllowanceDays;
    
    /** 应享受津贴 */
    private BigDecimal expectedTotalAllowance;
    
    /** 需补差金额 */
    private BigDecimal expectedSupplementAmount;
    
    /** 返还金额 */
    private BigDecimal expectedRefundAmount;
}
