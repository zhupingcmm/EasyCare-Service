package com.hr.maternity.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 节假日服务接口
 */
public interface HolidayService {
    
    /**
     * 获取指定年份的公共假日
     * @param year 年份
     * @return 公共假日列表
     */
    List<Map<String, Object>> getPublicHolidays(String year);

    void initHoliday(int year);

    Set<LocalDate> getAdditionalWorkDay();
    Set<LocalDate> getHoliday();
    Map<LocalDate, Map<String, Object>> getDayInfoMap();

}
