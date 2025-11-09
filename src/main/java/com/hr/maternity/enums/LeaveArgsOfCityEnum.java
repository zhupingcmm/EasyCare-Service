package com.hr.maternity.enums;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 城市枚举
 */
@Getter
@AllArgsConstructor
public enum LeaveArgsOfCityEnum {
    SHANGHAI("上海", "SH", 98, 15, 15, 60),
    SHENZHEN("深圳", "SZ", 98, 30, 15, 80),
    GUANGZHOU("广州", "GZ", 98, 15, 15, 80),    // 难产
    TIANJIN("天津", "TJ", 98, 15, 15, 60),
    SHAOXING("绍兴", "SX", 98, 15, 15, 60),     // 晚育
    XIAMEN("厦门", "XM", 98, 15, 15, 60),       // 晚育 60/90
    CHENGDU("成都", "CD", 98, 15, 15, 60),      // 晚育 +1M
    SUZHOU("苏州", "SU", 98, 15, 15, 60),
    QINGDAO("青岛", "QD", 98, 15, 15, 60),
    BEIJING("北京", "BJ", 98, 15, 15, 60),
    CHONGQING("重庆", "CQ", 98, 15, 15, 80),
    ZHUHAI("珠海", "ZH", 98, 30, 15, 80),
    WUHAN("武汉", "WH", 98, 15, 15, 60),
    FOSHAN("佛山", "FS", 98, 30, 15, 80),
    ;

    private final String chineseName;
    private final String code;

    private final Integer baseDays;
    private final Integer dystociaDays;
    private final Integer multiBabyDaysFactor;
    private final Integer extendedDays;


    public String getChineseName() {
        return chineseName;
    }

    public String getCode() {
        return code;
    }

    public static LeaveArgsOfCityEnum fromCode(String code) {
        for (LeaveArgsOfCityEnum city : values()) {
            if (city.code.equals(code)) {
                return city;
            }
        }
        throw new IllegalArgumentException("未知的城市代码: " + code);
    }
}