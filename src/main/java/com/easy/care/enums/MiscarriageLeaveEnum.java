package com.easy.care.enums;

import lombok.Getter;

/**
 * 流产假枚举
 */
@Getter
public enum MiscarriageLeaveEnum {

    MC_001("mc_001", "宫外孕"),
    MC_002("mc_002", "怀孕不满2个月流产"),
    MC_003("mc_003", "怀孕满2个月不满3个月流产"),
    MC_004("mc_004", "怀孕满3个月不满7个月流产"),
    MC_005("mc_005", "怀孕满7个月流产"),
    MC_006("mc_006", "妊娠不满12周流产"),
    MC_007("mc_007", "妊娠满12周不满28周流产"),
    MC_008("mc_008", "妊娠满28周流产"),
    MC_009("mc_009", "妊娠满4个月流产"),
    MC_010("mc_010", "妊娠满7个月流产"),
    MC_011("mc_011", "妊娠未满4个月流产");

    /**
     * 代码
     */
    private final String code;

    /**
     * 名称
     */
    private final String name;

    MiscarriageLeaveEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * 根据代码获取枚举
     * @param code 代码
     * @return 枚举值
     */
    public static MiscarriageLeaveEnum getByCode(String code) {
        if (code == null) {
            return null;
        }
        for (MiscarriageLeaveEnum value : values()) {
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
    public static MiscarriageLeaveEnum getByName(String name) {
        if (name == null) {
            return null;
        }
        for (MiscarriageLeaveEnum value : values()) {
            if (value.getName().equals(name)) {
                return value;
            }
        }
        return null;
    }
}
