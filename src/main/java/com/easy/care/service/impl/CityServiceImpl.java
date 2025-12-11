package com.easy.care.service.impl;

import com.easy.care.entity.CityDO;
import com.easy.care.repository.CityRepository;
import com.easy.care.service.CityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CityServiceImpl implements CityService {

    private final CityRepository cityRepository;

    @Override
    public String getEnabledCityChineseName(String code) {
        CityDO city = cityRepository.findByCodeAndEnabledTrue(code)
                .orElseThrow(() -> new IllegalArgumentException("启用状态的城市不存在: " + code));
        return city.getChineseName();
    }
}
