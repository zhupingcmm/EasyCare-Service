package com.hr.maternity.service.impl;

import com.hr.maternity.dto.MaternityLeaveRequest;
import com.hr.maternity.dto.MaternityLeaveResponse;
import com.hr.maternity.entity.CityDO;
import com.hr.maternity.entity.HistoryDO;
import com.hr.maternity.entity.MaternityLeaveRequestDO;
import com.hr.maternity.entity.MaternityLeaveResultDO;
import com.hr.maternity.enums.RecordTypeEnum;
import com.hr.maternity.repository.CityRepository;
import com.hr.maternity.repository.HistoryRepository;
import com.hr.maternity.repository.MaternityLeaveRequestRepository;
import com.hr.maternity.repository.MaternityLeaveResultRepository;
import com.hr.maternity.service.MaternityLeaveService;
import com.hr.maternity.strategy.MaternityLeaveStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * 产假计算服务实现类
 */
@Slf4j
@Service
public class MaternityLeaveServiceImpl implements MaternityLeaveService {

    private final Map<String, MaternityLeaveStrategy> strategyMap;
    private final CityRepository cityRepository;
    private final MaternityLeaveRequestRepository requestRepository;
    private final MaternityLeaveResultRepository resultRepository;
    private final HistoryRepository historyRepository;

    @Value("${maternity.default.lan-id}")
    private String defaultLanId;

    public MaternityLeaveServiceImpl(
            @Qualifier("maternityLeaveStrategyMap") Map<String, MaternityLeaveStrategy> strategyMap,
            CityRepository cityRepository,
            MaternityLeaveRequestRepository requestRepository,
            MaternityLeaveResultRepository resultRepository,
            HistoryRepository historyRepository) {
        this.strategyMap = strategyMap;
        this.cityRepository = cityRepository;
        this.requestRepository = requestRepository;
        this.resultRepository = resultRepository;
        this.historyRepository = historyRepository;
    }

    @Override
    @Transactional
    public MaternityLeaveResponse calculateMaternityLeave(MaternityLeaveRequest request) {
        log.info("开始计算产假，请求参数: {}", request);
        
        // 1. 保存申请记录
        MaternityLeaveRequestDO requestEntity = convertToRequestEntity(request);
        requestEntity = requestRepository.save(requestEntity);
        log.info("保存申请记录成功，ID: {}", requestEntity.getId());
        
        // 2. 创建历史记录并保存申请记录ID
        HistoryDO history = new HistoryDO();
        history.setLanId(defaultLanId);
        history.setRecordType(RecordTypeEnum.MATERNITY);
        history.setMaternityLeaveRequestId(requestEntity.getId());
        history = historyRepository.save(history);
        log.info("保存历史记录(申请)成功，ID: {}", history.getId());
        
        // 3. 计算产假
        MaternityLeaveStrategy strategy = strategyMap.get(request.getCityCode());
        if (strategy == null) {
            throw new IllegalArgumentException("不支持的城市代码: " + request.getCityCode());
        }
        MaternityLeaveResponse resp = strategy.calculateMaternityLeave(request);
        cityRepository.findByCode(request.getCityCode()).ifPresent(city -> fillCity(resp, city));
        
        // 4. 保存计算结果
        MaternityLeaveResultDO resultEntity = convertToResultEntity(resp, requestEntity.getId());
        resultEntity = resultRepository.save(resultEntity);
        log.info("保存计算结果成功，ID: {}", resultEntity.getId());
        
        // 5. 更新历史记录，保存计算结果ID
        history.setMaternityLeaveResultId(resultEntity.getId());
        historyRepository.save(history);
        log.info("更新历史记录(结果)成功，ID: {}", history.getId());
        
        // 6. 设置ID到响应中
        resp.setRequestId(requestEntity.getId());
        resp.setResultId(resultEntity.getId());
        
        return resp;
    }

    private void fillCity(MaternityLeaveResponse resp, CityDO city) {
        resp.setCityCode(city.getCode());
        resp.setCityName(city.getChineseName() != null ? city.getChineseName() : city.getName());
    }

    /**
     * 将 DTO 转换为申请记录实体
     */
    private MaternityLeaveRequestDO convertToRequestEntity(MaternityLeaveRequest dto) {
        MaternityLeaveRequestDO entity = new MaternityLeaveRequestDO();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }

    /**
     * 将响应 DTO 转换为结果实体
     */
    private MaternityLeaveResultDO convertToResultEntity(MaternityLeaveResponse response, Long requestId) {
        MaternityLeaveResultDO entity = new MaternityLeaveResultDO();
//        entity.setRequestId(requestId);
        entity.setLanId(response.getLanId());
        entity.setEmployeeName(response.getEmployeeName());
        entity.setCityCode(response.getCityCode());
        entity.setCityName(response.getCityName());
        entity.setTotalDays(response.getTotalDays());
        entity.setTotalAllowanceDays(response.getTotalAllowanceDays());
        entity.setBaseDays(response.getBaseDays());
        entity.setDystociaDays(response.getDystociaDays());
        entity.setMultiBabyDays(response.getMultiBabyDays());
        entity.setExtendedDays(response.getExtendedDays());
        entity.setMiscarriageLeaveDays(response.getMiscarriageLeaveDays());
        entity.setPubHolidaysCount(response.getPubHolidaysCount());
        entity.setStartDate(response.getStartDate());
        entity.setEndDate(response.getEndDate());
        entity.setReturnToWorkDate(response.getReturnToWorkDate());
        entity.setTimeScopeList(response.getTimeScopeList());
        return entity;
    }
}
