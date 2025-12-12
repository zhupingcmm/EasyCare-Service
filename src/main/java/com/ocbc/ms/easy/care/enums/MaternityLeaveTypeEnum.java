package com.ocbc.ms.easy.care.enums;

import lombok.Getter;

/**
 * 产假类型枚举（根据 t_maternity_leave_type 生成，常量名全大写）。
 * 使用 Integer id 对应数据库主键。
 */
@Getter
public enum MaternityLeaveTypeEnum {
    UNKNOWN(-1, "unknown", "未知类型", Integer.MAX_VALUE),
    BASE(1, "base", "法定产假", 1),
    DIFFICULT_BIRTH(2, "dystocia", "难产假", 2),
    MULTI_BABIES(3, "multiple", "多胞胎假", 3),
    AWARD(4, "award", "奖励假", 4),
    MISCARRIAGE(5, "miscarriage", "流产假", Integer.MAX_VALUE);

    private final Integer id;
    private final String code;
    private final String name;
    private final Integer priority;

    MaternityLeaveTypeEnum(Integer id, String code, String name, Integer priority) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.priority = priority;
    }

    public static MaternityLeaveTypeEnum fromId(Integer id) {
        if (id == null) {
            return UNKNOWN;
        }
        for (MaternityLeaveTypeEnum type : values()) {
            if (type.id.equals(id)) {
                return type;
            }
        }
        return UNKNOWN;
    }
}
