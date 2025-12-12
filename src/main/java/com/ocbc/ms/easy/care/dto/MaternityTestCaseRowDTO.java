package com.ocbc.ms.easy.care.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/**
 * 产假津贴测试用例行数据 DTO
 * 用于存储从 Excel 中解析出的每一行数据
 * 
 * 字段分组：
 * 1. 员工基本信息 - 用例编号、用例描述、员工工号、员工姓名、城市代码
 * 2. 产假计算信息 - 生育方式、胎数、奖励假、广州难产类型、产假开始时间
 * 3. 津贴计算信息 - 工资、社保、福利、扣除项等
 * 4. 测试期望结果 - 产假结束日期、总产假天数、津贴金额等
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaternityTestCaseRowDTO {
    
    // ========== 主类变量：用例和员工基本信息 ==========
    /** 用例编号 */
    private String caseNumber;
    
    /** 用例描述 */
    private String caseDescription;
    
    /** 员工工号 */
    private String employeeId;
    
    /** 员工姓名 */
    private String employeeName;
    
    /** 城市代码 */
    private String cityCode;

    // ========== 分组子类 ==========
    /** 产假计算信息 */
    private MaternityCalcInfo maternityCalcInfo;
    
    /** 津贴计算信息 */
    private AllowanceCalcInfo allowanceCalcInfo;
    
    /** 测试期望结果 */
    private ExpectedResult expectedResult;
    
    /**
     * 从 Map 中构建 DTO
     * 
     * @param rowData Excel 行数据
     * @return MaternityTestCaseRowDTO
     */
    public static MaternityTestCaseRowDTO fromMap(Map<String, Object> rowData) {
        // 构建产假计算信息
        MaternityCalcInfo maternityCalcInfo = MaternityCalcInfo.builder()
            .deliveryMethod(getStringValue(rowData, "生育方式"))
            .numberOfBabies(getIntegerValue(rowData, "胎数"))
            .hasRewardLeave(getBooleanValue(rowData, "是否有奖励假"))
            .guangzhouDifficultType1(getStringValue(rowData, "广州难产类型1  剖腹产、会阴III度破裂"))
            .guangzhouDifficultType2(getStringValue(rowData, "广州难产类型2  吸引产、钳产、臀位牵引产"))
            .leaveStartDate(getLocalDateValue(rowData, "产假开始时间"))
            .build();

        // 构建津贴计算信息
        AllowanceCalcInfo allowanceCalcInfo = AllowanceCalcInfo.builder()
            .avgSalaryBefore12Months(getBigDecimalValue(rowData, "员工产前12个月的月平均工资"))
            .governmentAllowance(getBigDecimalValue(rowData, "政府发放津贴"))
            .declaredAvgSalaryLastYear(getBigDecimalValue(rowData, "单位申报上年度月平均工资"))
            .hasSalaryAdjustmentInApril(getBooleanValue(rowData, "是否跨4月调薪"))
            .salaryBeforeAdjustment(getBigDecimalValue(rowData, "调薪前工资"))
            .salaryAfterAdjustment(getBigDecimalValue(rowData, "调薪后工资"))
            .baseSalary(getBigDecimalValue(rowData, "基本工资"))
            .paymentDate(getLocalDateValue(rowData, "发放时间"))
            .submissionDate(getLocalDateValue(rowData, "提交核定表时间"))
            .hasSocialSecurityAdjustmentInJuly(getBooleanValue(rowData, "是否跨7月社保调整"))
            .socialSecurityBeforeAdjustment(getBigDecimalValue(rowData, "调整前个人社保公积金合计"))
            .socialSecurityAfterAdjustment(getBigDecimalValue(rowData, "调整后个人社保公积金合计"))
            .monthlySocialSecurity(getBigDecimalValue(rowData, "月度个人社保公积金合计"))
            .flexibleBenefit(getBigDecimalValue(rowData, "弹性福利"))
            .espp(getBigDecimalValue(rowData, "ESPP"))
            .unionFee(getBigDecimalValue(rowData, "个人工会费"))
            .otherRewards(getBigDecimalValue(rowData, "其他奖励项目"))
            .otherDeductions(getBigDecimalValue(rowData, "其他扣除项"))
            .spotOn(getBigDecimalValue(rowData, "Spot on"))
            .build();

        // 构建测试期望结果
        ExpectedResult expectedResult = ExpectedResult.builder()
            .expectedLeaveEndDate(getLocalDateValue(rowData, "产假结束日期"))
            .expectedTotalLeaveDays(getIntegerValue(rowData, "总产假天数"))
            .expectedReturnDate(getLocalDateValue(rowData, "预计返岗日期"))
            .expectedAllowanceDays(getIntegerValue(rowData, "津贴天数"))
            .expectedTotalAllowance(getBigDecimalValue(rowData, "应享受津贴"))
            .expectedSupplementAmount(getBigDecimalValue(rowData, "需补差金额"))
            .expectedRefundAmount(getBigDecimalValue(rowData, "返还金额"))
            .build();

        // 构建主 DTO
        return MaternityTestCaseRowDTO.builder()
            .caseNumber(getStringValue(rowData, "用例编号"))
            .caseDescription(getStringValue(rowData, "用例描述"))
            .employeeId(getStringValue(rowData, "员工工号"))
            .employeeName(getStringValue(rowData, "员工姓名"))
            .cityCode(getStringValue(rowData, "城市代码"))
            .maternityCalcInfo(maternityCalcInfo)
            .allowanceCalcInfo(allowanceCalcInfo)
            .expectedResult(expectedResult)
            .build();
    }
    
    // ========== 辅助方法 ==========
    
    private static String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString().trim() : null;
    }
    
    private static Integer getIntegerValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(value.toString().trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    private static Boolean getBooleanValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        String strValue = value.toString().trim().toLowerCase();
        return "true".equals(strValue) || "是".equals(strValue) || "1".equals(strValue);
    }
    
    private static LocalDate getLocalDateValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDate) {
            return (LocalDate) value;
        }
        try {
            return LocalDate.parse(value.toString().trim());
        } catch (Exception e) {
            return null;
        }
    }
    
    private static BigDecimal getBigDecimalValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }
        try {
            return new BigDecimal(value.toString().trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // ========== 分组子类定义 ==========
    
    /**
     * 产假计算信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MaternityCalcInfo {
        private String deliveryMethod;
        private Integer numberOfBabies;
        private Boolean hasRewardLeave;
        private String guangzhouDifficultType1;
        private String guangzhouDifficultType2;
        private LocalDate leaveStartDate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AllowanceCalcInfo {
        private BigDecimal avgSalaryBefore12Months;
        private BigDecimal governmentAllowance;
        private BigDecimal declaredAvgSalaryLastYear;
        private Boolean hasSalaryAdjustmentInApril;
        private BigDecimal salaryBeforeAdjustment;
        private BigDecimal salaryAfterAdjustment;
        private BigDecimal baseSalary;
        private LocalDate paymentDate;
        private LocalDate submissionDate;
        private Boolean hasSocialSecurityAdjustmentInJuly;
        private BigDecimal socialSecurityBeforeAdjustment;
        private BigDecimal socialSecurityAfterAdjustment;
        private BigDecimal monthlySocialSecurity;
        private BigDecimal flexibleBenefit;
        private BigDecimal espp;
        private BigDecimal unionFee;
        private BigDecimal otherRewards;
        private BigDecimal otherDeductions;
        private BigDecimal spotOn;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExpectedResult {
        private LocalDate expectedLeaveEndDate;
        private Integer expectedTotalLeaveDays;
        private LocalDate expectedReturnDate;
        private Integer expectedAllowanceDays;
        private BigDecimal expectedTotalAllowance;
        private BigDecimal expectedSupplementAmount;
        private BigDecimal expectedRefundAmount;
    }
}
