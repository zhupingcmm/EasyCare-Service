package com.easy.care.enums;

import lombok.Getter;

/**
 * 奖励假枚举
 */
@Getter
public enum AwardLeaveEnum {

    AWD_001("awd_001", "一孩"),
    AWD_002("awd_002", "二孩"),
    AWD_003("awd_003", "三孩"),
    AWD_004("awd_004", "四孩"),
    AWD_005("awd_005", "五孩");

    /**
     * 代码
     */
    private final String code;

    /**
     * 名称
     */
    private final String name;

    AwardLeaveEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * 根据代码获取枚举
     * @param code 代码
     * @return 枚举值
     */
    public static AwardLeaveEnum getByCode(String code) {
        if (code == null) {
            return null;
        }
        for (AwardLeaveEnum value : values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }

    /**
     * 根据名称获取枚举
     * @param name 名称
     * @return 枚举值
     */
    public static AwardLeaveEnum getByName(String name) {
        if (name == null) {
            return null;
        }
        for (AwardLeaveEnum value : values()) {
            if (value.getName().equals(name)) {
                return value;
            }
        }
        return null;
    }
}
