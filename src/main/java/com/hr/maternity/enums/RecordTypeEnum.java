package com.hr.maternity.enums;

import lombok.Getter;

/**
 * 历史记录类型枚举
 */
@Getter
public enum RecordTypeEnum {
    
    /**
     * 产假记录
     */
    MATERNITY("maternity", "产假"),
    
    /**
     * 津贴记录
     */
    ALLOWANCE("allowance", "津贴");
    
    /**
     * 数据库存储值
     */
    private final String code;
    
    /**
     * 描述
     */
    private final String description;
    
    RecordTypeEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    /**
     * 根据 code 获取枚举
     * @param code 代码
     * @return 枚举值
     */
    public static RecordTypeEnum fromCode(String code) {
        for (RecordTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的记录类型: " + code);
    }
    
    /**
     * 判断是否为产假类型
     * @return true-产假，false-其他
     */
    public boolean isMaternity() {
        return this == MATERNITY;
    }
    
    /**
     * 判断是否为津贴类型
     * @return true-津贴，false-其他
     */
    public boolean isAllowance() {
        return this == ALLOWANCE;
    }
}
