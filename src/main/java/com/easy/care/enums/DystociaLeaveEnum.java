package com.easy.care.enums;

import lombok.Getter;

/**
 * 难产假枚举
 */
@Getter
public enum DystociaLeaveEnum {

    DAY_001("day_001", "剖腹产、会阴Ⅲ度破裂"),
    DAY_002("day_002", "吸引产、钳产、臀位牵引产");

    /**
     * 代码
     */
    private final String code;

    /**
     * 名称
     */
    private final String name;

    DystociaLeaveEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * 根据代码获取枚举
     * @param code 代码
     * @return 枚举值
     */
    public static DystociaLeaveEnum getByCode(String code) {
        if (code == null) {
            return null;
        }
        for (DystociaLeaveEnum value : values()) {
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
    public static DystociaLeaveEnum getByName(String name) {
        if (name == null) {
            return null;
        }
        for (DystociaLeaveEnum value : values()) {
            if (value.getName().equals(name)) {
                return value;
            }
        }
        return null;
    }
}
