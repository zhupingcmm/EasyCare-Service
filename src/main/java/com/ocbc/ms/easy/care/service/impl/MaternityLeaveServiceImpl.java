package com.ocbc.ms.easy.care.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.ocbc.ms.easy.care.dto.MaternityLeaveRequest;
import com.ocbc.ms.easy.care.dto.MaternityLeaveResponse;
import com.ocbc.ms.easy.care.entity.CityDO;
import com.ocbc.ms.easy.care.entity.HistoryDO;
import com.ocbc.ms.easy.care.entity.MaternityLeaveRequestDO;
import com.ocbc.ms.easy.care.entity.MaternityLeaveResultDO;
import com.ocbc.ms.easy.care.entity.MaternityRules;
import com.ocbc.ms.easy.care.enums.RecordTypeEnum;
import com.ocbc.ms.easy.care.repository.CityRepository;
import com.ocbc.ms.easy.care.repository.HistoryRepository;
import com.ocbc.ms.easy.care.repository.MaternityLeaveRequestRepository;
import com.ocbc.ms.easy.care.repository.MaternityLeaveResultRepository;
import com.ocbc.ms.easy.care.repository.MaternityRulesRepository;
import com.ocbc.ms.easy.care.rule.MaternityLeaveRuleService;
import com.ocbc.ms.easy.care.service.MaternityLeaveService;
import com.ocbc.ms.easy.care.strategy.MaternityLeaveStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
    private final MaternityRulesRepository maternityRulesRepository;

    @Autowired
    private MaternityLeaveRuleService maternityLeaveRuleService;

    @Value("${maternity.default.lan-id}")
    private String defaultLanId;

    public MaternityLeaveServiceImpl(
            @Qualifier("maternityLeaveStrategyMap") Map<String, MaternityLeaveStrategy> strategyMap,
            CityRepository cityRepository,
            MaternityLeaveRequestRepository requestRepository,
            MaternityLeaveResultRepository resultRepository,
            HistoryRepository historyRepository,
            MaternityRulesRepository maternityRulesRepository) {
        this.strategyMap = strategyMap;
        this.cityRepository = cityRepository;
        this.requestRepository = requestRepository;
        this.resultRepository = resultRepository;
        this.historyRepository = historyRepository;
        this.maternityRulesRepository = maternityRulesRepository;
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

    @Override
    public MaternityLeaveResponse calculateMaternityLeaveNew(MaternityLeaveRequest request) {
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

        String cityCode = request.getCityCode();
        if (cityCode == null || cityCode.trim().isEmpty()) {
            throw new IllegalArgumentException("参数不能为空");
        }
        CityDO city = cityRepository.findByCode(cityCode)
                .orElseThrow(() -> new IllegalArgumentException("城市不存在，代码: " + cityCode));
        Integer cityId = city.getId();

        List<MaternityRules> maternityRuleList = maternityRulesRepository.findByCityId(cityId);
        log.debug("根据城市查询到规则数量: {}", JSONObject.toJSONString(maternityRuleList));

        MaternityLeaveResponse resp = maternityLeaveRuleService.calcMaternityDuration(request, maternityRuleList);

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
        resp.setCityName(city.getChineseName());
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
