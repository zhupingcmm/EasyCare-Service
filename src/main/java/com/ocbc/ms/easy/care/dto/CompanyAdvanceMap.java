package com.ocbc.ms.easy.care.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ocbc.ms.easy.care.domain.MonthlyWorkdayInfoDO;
import com.ocbc.ms.easy.care.enums.AddDeleteItemEnum;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 公司垫付信息Map结构
 * 支持增加和删除操作
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CompanyAdvanceMap implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 增加项目的Map
     * key: 项目名称（如"五险一金", "补充公积金", "工会费", "ESPP"等）
     * value: 金额
     */
    private Map<String, BigDecimal> addItem;
    
    /**
     * 删除项目的Map
     * key: 项目名称
     * value: 金额
     */
    private Map<String, BigDecimal> deleteItem;
    
    /**
     * 验证addItem和deleteItem中的所有值都大于等于0
     * @throws IllegalArgumentException 如果存在负值
     */
    public void validateValues() {
        // 验证addItem中的值
        if (addItem != null) {
            addItem.entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .forEach(entry -> {
                    if (entry.getValue().compareTo(BigDecimal.ZERO) < 0) {
                        throw new IllegalArgumentException(
                            String.format("addItem中的项目'%s'的值不能为负数: %s", 
                                entry.getKey(), entry.getValue()));
                    }
                });
        }
        
        // 验证deleteItem中的值
        if (deleteItem != null) {
            deleteItem.entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .forEach(entry -> {
                    if (entry.getValue().compareTo(BigDecimal.ZERO) < 0) {
                        throw new IllegalArgumentException(
                            String.format("deleteItem中的项目'%s'的值不能为负数: %s", 
                                entry.getKey(), entry.getValue()));
                    }
                });
        }
    }
    
    /**
     * 计算公司垫付净额
     * 净额 = addItem中所有value的和 - deleteItem中所有value的和
     */
    public BigDecimal calculateNetCompanyAdvance() {
        // 先验证所有值都大于等于0
        validateValues();
        
        BigDecimal additionSum = BigDecimal.ZERO;
        BigDecimal deleteSum = BigDecimal.ZERO;
        
        // 计算addItem总和
        if (addItem != null) {
            additionSum = addItem.values().stream()
                .filter(value -> value != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        
        // 计算deleteItem总和
        if (deleteItem != null) {
            deleteSum = deleteItem.values().stream()
                .filter(value -> value != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        
        return additionSum.subtract(deleteSum);
    }
    
    /**
     * 计算公司垫付净额（工会费、ESPP和社保缴费基数需要乘以完整月份数）
     * 净额 = addItem中所有value的和（工会费、espp和社保缴费基数乘以完整月份数） - deleteItem中所有value的和
     * @param completeMonths 完整月份数，用于计算工会费、ESPP和社保缴费基数的月份倍数
     */
    public BigDecimal calculateNetCompanyAdvance(long completeMonths) {
        // 先验证所有值都大于等于0
        validateValues();
        
        BigDecimal additionSum = BigDecimal.ZERO;
        BigDecimal deleteSum = BigDecimal.ZERO;
        
        // 计算addItem总和（工会费、espp和社保缴费基数需要乘以完整月份数）
        if (addItem != null) {
            additionSum = addItem.entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .map(entry -> {
                    BigDecimal value = entry.getValue();
                    // 如果是工会费、ESPP或社保缴费基数，需要乘以完整月份数
                    if (isUnionFeeOrEsppOrSocialInsurance(entry.getKey())) {
                        return value.multiply(BigDecimal.valueOf(completeMonths));
                    }
                    return value;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        
        // 计算deleteItem总和
        if (deleteItem != null) {
            deleteSum = deleteItem.values().stream()
                .filter(value -> value != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        
        return additionSum.subtract(deleteSum);
    }
    
    /**
     * 计算公司垫付净额（根据月份使用不同的社保缴费基数计算）
     * 对于完整月份：7月之前的月份使用socialInsuranceBase，7月及之后的月份使用adjustedSocialInsuranceBase
     * @param monthlyWorkdayList 月工作日信息列表
     * @return 计算后的公司垫付净额
     */
    public BigDecimal calculateNetCompanyAdvanceWithMonthlyLogic(List<MonthlyWorkdayInfoDO> monthlyWorkdayList, boolean socialInsuranceBaseAdjusted, int month) {

        // 先验证所有值都大于等于0
        validateValues();
        
        BigDecimal additionSum = BigDecimal.ZERO;
        BigDecimal deleteSum = BigDecimal.ZERO;
        
        // 计算deleteItem总和
        if (deleteItem != null) {
            deleteSum = deleteItem.values().stream()
                .filter(value -> value != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        
        long completeMonths = monthlyWorkdayList.stream()
            .filter(MonthlyWorkdayInfoDO::getFullMonth)
            .count();

        // 计算addItem总和（根据月份使用不同的社保缴费基数）
        if (addItem != null) {
            additionSum = addItem.entrySet().stream()
                .filter(entry -> entry.getValue() != null && !AddDeleteItemEnum.ADJUSTED_SOCIAL_INSURANCE_BASE.getCode().equals(entry.getKey()))
                .map(entry -> {
                    BigDecimal value = entry.getValue();
                    String key = entry.getKey();
                    
                    // 如果是工会费或ESPP，需要乘以完整月份数
                    if (AddDeleteItemEnum.UNION_FEE.getCode().equals(key) || AddDeleteItemEnum.ESPP.getCode().equalsIgnoreCase(key)) {
                        return value.multiply(BigDecimal.valueOf(completeMonths));
                    }
                    
                    // 如果是社保缴费基数，需要根据月份分别计算
                    if (AddDeleteItemEnum.SOCIAL_INSURANCE_BASE.getCode().equals(key)) {
                        BigDecimal socialInsuranceBaseValue = BigDecimal.ZERO;
                        if (!socialInsuranceBaseAdjusted) {
                            socialInsuranceBaseValue = value.multiply(BigDecimal.valueOf(completeMonths));
                        } else {
                            socialInsuranceBaseValue = calculateSocialInsuranceBaseByMonth(value, monthlyWorkdayList, month);
                        }
                        return socialInsuranceBaseValue;
                    }

                    // 其他项目直接返回原值
                    return value;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        
        return additionSum.subtract(deleteSum);
    }
    
    public BigDecimal calculateSocialInsuranceBaseByMonth(BigDecimal value, List<MonthlyWorkdayInfoDO> monthlyWorkdayList, int adjustMonth) {
        if (monthlyWorkdayList == null || monthlyWorkdayList.isEmpty()) {
            return BigDecimal.ZERO;
        }

        List<MonthlyWorkdayInfoDO> fullMonthList = monthlyWorkdayList.stream()
            .filter(workday -> Boolean.TRUE.equals(workday.getFullMonth()))
            .toList();
        if (fullMonthList.isEmpty()) {
            return BigDecimal.ZERO;
        }

        int normalizedAdjustMonth = Math.min(Math.max(adjustMonth, 1), 12);
        YearMonth adjustThreshold = resolveAdjustThreshold(monthlyWorkdayList.get(0), normalizedAdjustMonth);
        BigDecimal adjustedValue = resolveAdjustedSocialInsuranceBase(value);

        BigDecimal socialInsuranceBaseValue = BigDecimal.ZERO;
        for (MonthlyWorkdayInfoDO workday : fullMonthList) {
            YearMonth current = YearMonth.of(workday.getYear(), workday.getMonth());
            BigDecimal monthBase = current.isBefore(adjustThreshold) ? value : adjustedValue;
            socialInsuranceBaseValue = socialInsuranceBaseValue.add(monthBase);
        }

        return socialInsuranceBaseValue;
    }

    private BigDecimal resolveAdjustedSocialInsuranceBase(BigDecimal defaultValue) {
        if (addItem == null) {
            return defaultValue;
        }
        return addItem.entrySet().stream()
            .filter(adjEntry -> AddDeleteItemEnum.ADJUSTED_SOCIAL_INSURANCE_BASE.getCode().equals(adjEntry.getKey()))
            .map(Map.Entry::getValue)
            .filter(Objects::nonNull)
            .findFirst()
            .filter(adjusted -> !BigDecimal.ZERO.equals(adjusted))
            .orElse(defaultValue);
    }

    private YearMonth resolveAdjustThreshold(MonthlyWorkdayInfoDO firstMonth, int adjustMonth) {
        YearMonth firstYearMonth = YearMonth.of(firstMonth.getYear(), firstMonth.getMonth());
        if (firstYearMonth.getMonthValue() <= adjustMonth) {
            return YearMonth.of(firstYearMonth.getYear(), adjustMonth);
        }
        return YearMonth.of(firstYearMonth.getYear() + 1, adjustMonth);
    }

    /**
     * 获取工会费金额
     */
    public BigDecimal getUnionFee() {
        if (addItem == null) {
            return BigDecimal.ZERO;
        }
        return addItem.entrySet().stream()
            .filter(entry -> AddDeleteItemEnum.UNION_FEE.getCode().equals(entry.getKey()))
            .map(Map.Entry::getValue)
            .filter(value -> value != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * 获取ESPP金额
     */
    public BigDecimal getEspp() {
        if (addItem == null) {
            return BigDecimal.ZERO;
        }
        return addItem.entrySet().stream()
            .filter(entry -> AddDeleteItemEnum.ESPP.getCode().equalsIgnoreCase(entry.getKey()))
            .map(Map.Entry::getValue)
            .filter(value -> value != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * 获取社保缴费基数
     */
    public BigDecimal getSocialInsuranceBase() {
        if (addItem == null) {
            return BigDecimal.ZERO;
        }
        return addItem.entrySet().stream()
            .filter(entry -> AddDeleteItemEnum.SOCIAL_INSURANCE_BASE.getCode().equals(entry.getKey()))
            .map(Map.Entry::getValue)
            .filter(value -> value != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 获取社保缴费基数
     */
    public BigDecimal getAdjustedSocialInsuranceBase() {
        if (addItem == null) {
            return BigDecimal.ZERO;
        }
        return addItem.entrySet().stream()
            .filter(entry -> AddDeleteItemEnum.ADJUSTED_SOCIAL_INSURANCE_BASE.getCode().equals(entry.getKey()))
            .map(Map.Entry::getValue)
            .filter(value -> value != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 判断是否为工会费、ESPP或社保缴费基数
     */
    private boolean isUnionFeeOrEsppOrSocialInsurance(String key) {
        if (key == null) {
            return false;
        }
        return AddDeleteItemEnum.UNION_FEE.getCode().equals(key) || AddDeleteItemEnum.ESPP.getCode().equalsIgnoreCase(key) || AddDeleteItemEnum.SOCIAL_INSURANCE_BASE.getCode().equals(key);
    }
}
