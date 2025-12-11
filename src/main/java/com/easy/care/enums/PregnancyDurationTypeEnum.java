package com.easy.care.enums;

/**
 * 妊娠时长类型
 * 包含：周 / 日 / 月
 */
public enum PregnancyDurationTypeEnum {
    WEEK("周"),
    DAY("日"),
    MONTH("月");

    private final String chineseName;

    PregnancyDurationTypeEnum(String chineseName) {
        this.chineseName = chineseName;
    }

    public String getChineseName() {
        return chineseName;
    }
}
