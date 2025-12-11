package com.hr.maternity.service.impl;

import com.hr.maternity.entity.CityDO;
import com.hr.maternity.repository.CityRepository;
import com.hr.maternity.service.CityService;
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
