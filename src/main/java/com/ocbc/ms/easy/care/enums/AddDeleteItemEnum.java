package com.ocbc.ms.easy.care.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 公司垫付项目枚举
 * 用于定义addItem和deleteItem中的标准项目名称
 */
@Getter
@AllArgsConstructor
public enum AddDeleteItemEnum {
    
    /**
     * 员工持股计划
     */
    ESPP("espp", "员工持股计划"),
    
    /**
     * 工会费
     */
    UNION_FEE("unionFee", "工会费"),
    
    /**
     * 弹性福利
     */
    FLEXIBLE_BENEFIT("flexibleBenefit", "弹性福利"),
    
    /**
     * Spot On
     */
    SPOT_ON("spotOn", "Spot On"),
    
    /**
     * 社保缴费基数
     */
    SOCIAL_INSURANCE_BASE("socialInsuranceBase", "社保缴费基数"),
    
    /**
     * 调整社保缴费基数
     */
    ADJUSTED_SOCIAL_INSURANCE_BASE("adjustedSocialInsuranceBase", "调整社保缴费基数");
    
    /**
     * 英文代码
     */
    private final String code;
    
    /**
     * 中文名称
     */
    private final String name;
    
    /**
     * 根据代码获取枚举
     * @param code 英文代码
     * @return 对应的枚举值，如果未找到则返回null
     */
    public static AddDeleteItemEnum getByCode(String code) {
        if (code == null) {
            return null;
        }
        for (AddDeleteItemEnum item : values()) {
            if (item.getCode().equalsIgnoreCase(code)) {
                return item;
            }
        }
        return null;
    }
    
    /**
     * 根据名称获取枚举
     * @param name 中文名称
     * @return 对应的枚举值，如果未找到则返回null
     */
    public static AddDeleteItemEnum getByName(String name) {
        if (name == null) {
            return null;
        }
        for (AddDeleteItemEnum item : values()) {
            if (item.getName().equals(name)) {
                return item;
            }
        }
        return null;
    }
}

