package com.easy.care.enums;

/**
 * 难产类型枚举
 * code 与名称映射：
 * dys_002 -> 剖腹产、会阴Ⅲ度破裂
 * dys_003 -> 吸引产、钳产、臀位牵引产
 */
public enum DifficultBirthTypeEnum {
    UNKNOWN("unknown", "未知类型"),
    DYS_002("dys_001", "剖腹产、会阴Ⅲ度破裂"),
    DYS_003("dys_002", "吸引产、钳产、臀位牵引产");

    private final String code;
    private final String name;

    DifficultBirthTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static DifficultBirthTypeEnum fromCode(String code) {
        if (code == null) {
            return UNKNOWN;
        }
        for (DifficultBirthTypeEnum t : values()) {
            if (t.code.equalsIgnoreCase(code)) {
                return t;
            }
        }
        return UNKNOWN;
    }
}
