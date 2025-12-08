package com.hr.maternity.service.impl;

import com.hr.maternity.dto.MaternityAllowanceRequest;
import com.hr.maternity.dto.MaternityAllowanceResponse;
import com.hr.maternity.entity.CityDO;
import com.hr.maternity.entity.HistoryDO;
import com.hr.maternity.entity.MaternityAllowanceRequestDO;
import com.hr.maternity.entity.MaternityAllowanceResultDO;
import com.hr.maternity.enums.RecordTypeEnum;
import com.hr.maternity.repository.CityRepository;
import com.hr.maternity.repository.HistoryRepository;
import com.hr.maternity.repository.MaternityAllowanceRequestRepository;
import com.hr.maternity.repository.MaternityAllowanceResultRepository;
import com.hr.maternity.service.MaternityAllowanceService;
import com.hr.maternity.strategy.MaternityAllowanceStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 生育津贴计算服务实现类
 */
@Slf4j
@Service
public class MaternityAllowanceServiceImpl implements MaternityAllowanceService {
    @Qualifier("BaseMaternityAllowanceStrategy")
    private MaternityAllowanceStrategy maternityAllowanceStrategy;
    private final CityRepository cityRepository;
    private final MaternityAllowanceRequestRepository allowanceRequestRepository;
    private final MaternityAllowanceResultRepository allowanceResultRepository;
    private final HistoryRepository historyRepository;

    @Value("${maternity.default.lan-id}")
    private String defaultLanId;

    public MaternityAllowanceServiceImpl(
            CityRepository cityRepository,
            MaternityAllowanceRequestRepository allowanceRequestRepository,
            MaternityAllowanceResultRepository allowanceResultRepository,
            HistoryRepository historyRepository) {
        this.cityRepository = cityRepository;
        this.allowanceRequestRepository = allowanceRequestRepository;
        this.allowanceResultRepository = allowanceResultRepository;
        this.historyRepository = historyRepository;
    }

    @Override
    @Transactional
    public MaternityAllowanceResponse calculateMaternityAllowance(MaternityAllowanceRequest request) {
        log.info("开始计算津贴，请求参数: {}", request);
        
        // 1. 校验日期
        validateDateOrder(request);
        
        // 2. 根据 requestId 和 defaultLanId 从 history 表获取记录
        Long requestId = request.getRequestId();
        List<HistoryDO> histories = historyRepository.findByMaternityLeaveRequestId(requestId);
        HistoryDO history = histories.stream()
                .filter(h -> defaultLanId.equals(h.getLanId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "未找到对应的历史记录，requestId: " + requestId + ", lanId: " + defaultLanId));
        log.info("找到历史记录，ID: {}", history.getId());
        
        // 3. 保存津贴申请记录
        MaternityAllowanceRequestDO requestEntity = convertToAllowanceRequestEntity(request);
        requestEntity = allowanceRequestRepository.save(requestEntity);
        log.info("保存津贴申请记录成功，ID: {}", requestEntity.getId());
        
        // 4. 更新历史记录，保存津贴申请ID
        history.setMaternityAllowanceRequestId(requestEntity.getId());
        historyRepository.save(history);
        log.info("更新历史记录(津贴申请)成功，ID: {}", history.getId());
        
        // 5. 计算津贴
        MaternityAllowanceResponse resp = maternityAllowanceStrategy.calculateMaternityAllowance(request);
        cityRepository.findByCode(request.getCityCode()).ifPresent(city -> fillCity(resp, city));
        
        // 6. 保存计算结果
        MaternityAllowanceResultDO resultEntity = convertToAllowanceResultEntity(resp, requestEntity.getId());
        resultEntity = allowanceResultRepository.save(resultEntity);
        log.info("保存津贴计算结果成功，ID: {}", resultEntity.getId());
        
        // 7. 更新历史记录，保存津贴结果ID并更新记录类型为津贴
        history.setMaternityAllowanceResultId(resultEntity.getId());
        history.setRecordType(RecordTypeEnum.ALLOWANCE);
        historyRepository.save(history);
        log.info("更新历史记录(津贴结果)成功，记录类型已更新为ALLOWANCE，ID: {}", history.getId());
        
        // 8. 设置ID到响应中
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

    private void fillCity(MaternityAllowanceResponse resp, CityDO city) {
        resp.setCityCode(city.getCode());
        resp.setCityName(city.getChineseName());
    }

    /**
     * 将 DTO 转换为津贴申请记录实体
     */
    private MaternityAllowanceRequestDO convertToAllowanceRequestEntity(MaternityAllowanceRequest dto) {
        MaternityAllowanceRequestDO entity = new MaternityAllowanceRequestDO();
        BeanUtils.copyProperties(dto, entity);
        // 将 requestId 映射到 maternityLeaveRequestId（关联产假申请记录）
//        entity.setMaternityLeaveRequestId(dto.getRequestId());
        return entity;
    }

    /**
     * 将响应 DTO 转换为津贴结果实体
     */
    private MaternityAllowanceResultDO convertToAllowanceResultEntity(MaternityAllowanceResponse response, Long allowanceRequestId) {
        MaternityAllowanceResultDO entity = new MaternityAllowanceResultDO();
//        entity.setAllowanceRequestId(allowanceRequestId);
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
