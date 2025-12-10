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

import lombok.RequiredArgsConstructor;
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
    private final MaternityAllowanceStrategy maternityAllowanceStrategy;
    private final CityRepository cityRepository;
    private final MaternityAllowanceRequestRepository allowanceRequestRepository;
    private final MaternityAllowanceResultRepository allowanceResultRepository;
    private final HistoryRepository historyRepository;

    @Value("${maternity.default.lan-id}")
    private String defaultLanId;

    public MaternityAllowanceServiceImpl(
            @Qualifier("baseMaternityAllowanceStrategy") MaternityAllowanceStrategy maternityAllowanceStrategy,
            CityRepository cityRepository,
            MaternityAllowanceRequestRepository allowanceRequestRepository,
            MaternityAllowanceResultRepository allowanceResultRepository,
            HistoryRepository historyRepository) {
        this.maternityAllowanceStrategy = maternityAllowanceStrategy;
        this.cityRepository = cityRepository;
        this.allowanceRequestRepository = allowanceRequestRepository;
        this.allowanceResultRepository = allowanceResultRepository;
        this.historyRepository = historyRepository;
    }

    @Override
    public MaternityAllowanceResponse calculateMaternityAllowance(MaternityAllowanceRequest request) {
        validateDateOrder(request);
        MaternityAllowanceResponse resp = maternityAllowanceStrategy.calculateMaternityAllowance(request);
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
