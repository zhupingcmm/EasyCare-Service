package com.hr.maternity.service.impl;

import com.hr.maternity.dto.MaternityLeaveRequest;
import com.hr.maternity.dto.MaternityLeaveResponse;
import com.hr.maternity.entity.City;
import com.hr.maternity.repository.CityRepository;
import com.hr.maternity.service.MaternityLeaveService;
import com.hr.maternity.strategy.MaternityLeaveStrategy;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 产假计算服务实现类
 */
@Service
public class MaternityLeaveServiceImpl implements MaternityLeaveService {

    private final Map<String, MaternityLeaveStrategy> strategyMap;
    private final CityRepository cityRepository;

    public MaternityLeaveServiceImpl(
            @Qualifier("maternityLeaveStrategyMap") Map<String, MaternityLeaveStrategy> strategyMap,
            CityRepository cityRepository) {
        this.strategyMap = strategyMap;
        this.cityRepository = cityRepository;
    }

    @Override
    public MaternityLeaveResponse calculateMaternityLeave(MaternityLeaveRequest request) {
        MaternityLeaveStrategy strategy = strategyMap.get(request.getCityCode());
        if (strategy == null) {
            throw new IllegalArgumentException("不支持的城市代码: " + request.getCityCode());
        }
        MaternityLeaveResponse resp = strategy.calculateMaternityLeave(request);
        cityRepository.findByCode(request.getCityCode()).ifPresent(city -> fillCity(resp, city));
        return resp;
    }

    private void fillCity(MaternityLeaveResponse resp, City city) {
        resp.setCityCode(city.getCode());
        resp.setCityName(city.getChineseName() != null ? city.getChineseName() : city.getName());
    }
}
