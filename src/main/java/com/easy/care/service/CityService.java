package com.easy.care.service;

/**
 * 城市相关业务接口
 */
public interface CityService {

    /**
     * 根据城市代码获取启用城市的中文名称
     *
     * @param code 城市代码
     * @return 城市中文名
     */
    String getEnabledCityChineseName(String code);
}
