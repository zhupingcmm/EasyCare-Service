package com.hr.maternity.service.impl;

import com.hr.maternity.dto.MaternityAllowanceRequest;
import com.hr.maternity.dto.MaternityAllowanceResponse;
import com.hr.maternity.entity.City;
import com.hr.maternity.entity.MaternityAllowanceRequestDO;
import com.hr.maternity.entity.MaternityAllowanceResultDO;
import com.hr.maternity.repository.CityRepository;
import com.hr.maternity.repository.MaternityAllowanceRequestRepository;
import com.hr.maternity.repository.MaternityAllowanceResultRepository;
import com.hr.maternity.service.MaternityAllowanceService;
import com.hr.maternity.strategy.MaternityAllowanceStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * 生育津贴计算服务实现类
 */
@Slf4j
@Service
public class MaternityAllowanceServiceImpl implements MaternityAllowanceService {

    private final Map<String, MaternityAllowanceStrategy> strategyMap;
    private final CityRepository cityRepository;
    private final MaternityAllowanceRequestRepository allowanceRequestRepository;
    private final MaternityAllowanceResultRepository allowanceResultRepository;

    public MaternityAllowanceServiceImpl(
            @Qualifier("maternityAllowanceStrategyMap") Map<String, MaternityAllowanceStrategy> strategyMap,
            CityRepository cityRepository,
            MaternityAllowanceRequestRepository allowanceRequestRepository,
            MaternityAllowanceResultRepository allowanceResultRepository) {
        this.strategyMap = strategyMap;
        this.cityRepository = cityRepository;
        this.allowanceRequestRepository = allowanceRequestRepository;
        this.allowanceResultRepository = allowanceResultRepository;
    }

    @Override
    @Transactional
    public MaternityAllowanceResponse calculateMaternityAllowance(MaternityAllowanceRequest request) {
        log.info("开始计算津贴，请求参数: {}", request);
        
        // 1. 校验日期
        validateDateOrder(request);
        
        // 2. 保存津贴申请记录
        MaternityAllowanceRequestDO requestEntity = convertToAllowanceRequestEntity(request);
        requestEntity = allowanceRequestRepository.save(requestEntity);
        log.info("保存津贴申请记录成功，ID: {}", requestEntity.getId());
        
        // 3. 计算津贴
        MaternityAllowanceStrategy strategy = strategyMap.get(request.getCityCode());
        if (strategy == null) {
            throw new IllegalArgumentException("不支持的城市代码: " + request.getCityCode());
        }
        MaternityAllowanceResponse resp = strategy.calculateMaternityAllowance(request);
        cityRepository.findByCode(request.getCityCode()).ifPresent(city -> fillCity(resp, city));
        
        // 4. 保存计算结果
        MaternityAllowanceResultDO resultEntity = convertToAllowanceResultEntity(resp, requestEntity.getId());
        resultEntity = allowanceResultRepository.save(resultEntity);
        log.info("保存津贴计算结果成功，ID: {}", resultEntity.getId());
        
        // 5. 设置ID到响应中
        resp.setRequestId(requestEntity.getId());
        resp.setResultId(resultEntity.getId());
        
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

    /**
     * 将 DTO 转换为津贴申请记录实体
     */
    private MaternityAllowanceRequestDO convertToAllowanceRequestEntity(MaternityAllowanceRequest dto) {
        MaternityAllowanceRequestDO entity = new MaternityAllowanceRequestDO();
        BeanUtils.copyProperties(dto, entity);
        // 将 requestId 映射到 maternityLeaveRequestId（关联产假申请记录）
        entity.setMaternityLeaveRequestId(dto.getRequestId());
        return entity;
    }

    /**
     * 将响应 DTO 转换为津贴结果实体
     */
    private MaternityAllowanceResultDO convertToAllowanceResultEntity(MaternityAllowanceResponse response, Long allowanceRequestId) {
        MaternityAllowanceResultDO entity = new MaternityAllowanceResultDO();
        entity.setAllowanceRequestId(allowanceRequestId);
        entity.setLanId(response.getLanId());
        entity.setEmployeeName(response.getEmployeeName());
        entity.setCityCode(response.getCityCode());
        entity.setCityName(response.getCityName());
        entity.setAllowanceDays(response.getAllowanceDays());
        entity.setExtraAllowance(response.getExtraAllowance());
        entity.setMaternityAllowance(response.getMaternityAllowance());
        entity.setCompensationAmount(response.getCompensationAmount());
        entity.setPaidMaternityWage(response.getPaidMaternityWage());
        entity.setEmployeeRefundAmount(response.getEmployeeRefundAmount());
        entity.setAllowanceCompensationDetails(response.getAllowanceCompensationDetails());
        entity.setRefundDetails(response.getRefundDetails());
        return entity;
    }
}
