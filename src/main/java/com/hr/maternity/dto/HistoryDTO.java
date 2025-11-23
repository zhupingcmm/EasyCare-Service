package com.hr.maternity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * 历史记录 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoryDTO {
    
    /** 记录ID */
    private String id;
    
    /** 员工姓名 */
    private String employeeName;
    
    /** 员工工号 */
    private String employeeId;
    
    /** 城市代码 */
    private String city;
    
    /** 城市名称 */
    private String cityName;
    
    /** 开始日期 */
    private LocalDate startDate;
    
    /** 结束日期 */
    private LocalDate endDate;
    
    /** 总天数 */
    private Integer totalDays;
    
    /** 公司补偿金额 */
    private BigDecimal companyCompensation;
    
    /** 员工补偿金额 */
    private BigDecimal employeeCompensation;
    
    /** 计算时间 */
    private ZonedDateTime calculatedAt;
    
    /** 记录类型 */
    private String recordType;
    
    /** 接收时间 */
    private LocalDate receivedTime;

    /** 接收时间 */
    private LocalDate submissionTime;
    
    /** 产假数据 */
    private VacationDataDTO vacationData;
    
    /** 津贴数据 */
    private AllowanceDataDTO allowanceData;
    
    /**
     * 津贴数据 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AllowanceDataDTO {
        
        /** 城市代码 */
        private String city;
        
        /** 政府金额 */
        private BigDecimal governmentAmount;
        
        /** 预付输入方式 */
        private String advanceInputMethod;
        
        /** 员工保险 */
        private BigDecimal employeeInsurance;
        
        /** 社保基数 */
        private BigDecimal socialInsuranceBase;
        
        /** 已付工资 */
        private BigDecimal paidSalary;
        
        /** 申报工资 */
        private BigDecimal declaredSalary;
        
        /** 12个月平均工资 */
        private BigDecimal avgSalary12Months;
        
        /** 是否跨7月 */
        private Boolean crossJuly;
        
        /** 是否跨4月 */
        private Boolean crossApril;
        
        /** 月基本工资 */
        private BigDecimal monthlyBaseSalary;
        
        /** 调整后月基本工资 */
        private BigDecimal adjustedMonthlyBaseSalary;
        
        /** 是否银行员工 */
        private Boolean isBankEmployee;
        
        /** 接收时间 */
        private LocalDate receivedTime;
        
        /** 产假提交日期 */
        private LocalDate maternitySubmissionDate;

        /** 雇主ESPP */
        private BigDecimal employerESPP;
        
        /** 自定义输入项 */
        private List<CustomInputDTO> customInputs;
        
        /** 自定义扣除项 */
        private List<CustomDeductionDTO> customDeductions;
        
        /** 政府补贴 */
        private BigDecimal governmentSubsidy;
        
        /** 月平均工资 */
        private BigDecimal monthlyAverageSalary;
        
        /** 已付产假工资 */
        private BigDecimal paidMaternityWage;

    }
    
    /**
     * 自定义输入项 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomInputDTO {
        
        /** 项目名称 */
        private String name;
        
        /** 金额 */
        private BigDecimal amount;
    }
    
    /**
     * 自定义扣除项 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomDeductionDTO {
        
        /** 项目名称 */
        private String name;
        
        /** 金额 */
        private BigDecimal amount;
    }
}
