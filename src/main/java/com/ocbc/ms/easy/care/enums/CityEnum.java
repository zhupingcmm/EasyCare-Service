package com.ocbc.ms.easy.care.enums;

/**
 * 城市枚举
 */
public enum CityEnum {
    SHANGHAI("上海", "SH"),
    SHENZHEN("深圳", "SZ"),
    GUANGZHOU("广州", "GZ"),
    TIANJIN("天津", "TJ"),
    SHAOXING("绍兴", "SX"),
    XIAMEN("厦门", "XM"),
    CHENGDU("成都", "CD"),
    SUZHOU("苏州", "SU"),
    QINGDAO("青岛", "QD"),
    BEIJING("北京", "BJ"),
    CHONGQING("重庆", "CQ"),
    ZHUHAI("珠海", "ZH"),
    WUHAN("武汉", "WH"),
    FOSHAN("佛山", "FS");

    private final String chineseName;
    private final String code;

    CityEnum(String chineseName, String code) {
        this.chineseName = chineseName;
        this.code = code;
    }

    public String getChineseName() {
        return chineseName;
    }

    public String getCode() {
        return code;
    }

    public static CityEnum fromCode(String code) {
        for (CityEnum city : values()) {
            if (city.code.equals(code)) {
                return city;
            }
        }
        throw new IllegalArgumentException("未知的城市代码: " + code);
    }
}