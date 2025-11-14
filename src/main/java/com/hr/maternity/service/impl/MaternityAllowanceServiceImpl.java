package com.hr.maternity.service.impl;

import com.hr.maternity.dto.MaternityAllowanceRequest;
import com.hr.maternity.dto.MaternityAllowanceResponse;
import com.hr.maternity.entity.City;
import com.hr.maternity.repository.CityRepository;
import com.hr.maternity.service.MaternityAllowanceService;
import com.hr.maternity.strategy.MaternityAllowanceStrategy;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 生育津贴计算服务实现类
 */
@Service
public class MaternityAllowanceServiceImpl implements MaternityAllowanceService {

    private final Map<String, MaternityAllowanceStrategy> strategyMap;
    private final CityRepository cityRepository;

    public MaternityAllowanceServiceImpl(
            @Qualifier("maternityAllowanceStrategyMap") Map<String, MaternityAllowanceStrategy> strategyMap,
            CityRepository cityRepository) {
        this.strategyMap = strategyMap;
        this.cityRepository = cityRepository;
    }

    @Override
    public MaternityAllowanceResponse calculateMaternityAllowance(MaternityAllowanceRequest request) {
        // 仅校验：结束日期不得早于开始日期
        validateDateOrder(request);
        MaternityAllowanceStrategy strategy = strategyMap.get(request.getCityCode());
        if (strategy == null) {
            throw new IllegalArgumentException("不支持的城市代码: " + request.getCityCode());
        }
        MaternityAllowanceResponse resp = strategy.calculateMaternityAllowance(request);
        // 使用数据库中的城市名称和代码，确保与DB一致
        cityRepository.findByCode(request.getCityCode()).ifPresent(city -> fillCity(resp, city));
        return resp;
    }

    /**
     * 业务层校验：结束日期不得早于开始日期
     */
    private void validateDateOrder(MaternityAllowanceRequest req) {
        if (req.getMaternityLeaveStartDate() == null || req.getMaternityLeaveEndDate() == null) {
            return; // 字段级必填由 @NotNull 处理
        }
        if (req.getMaternityLeaveEndDate().isBefore(req.getMaternityLeaveStartDate())) {
            throw new IllegalArgumentException(
                    "产假结束时间(" + req.getMaternityLeaveEndDate() + ")不能早于开始时间(" + req.getMaternityLeaveStartDate() + ")"
            );
        }
    }

    private void fillCity(MaternityAllowanceResponse resp, City city) {
        resp.setCityCode(city.getCode());
        // 优先中文名，其次通用名
        resp.setCityName(city.getChineseName() != null ? city.getChineseName() : city.getName());
    }
}
