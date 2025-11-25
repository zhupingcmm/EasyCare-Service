package com.hr.maternity.service.impl;

import com.hr.maternity.dto.HistoryDTO;
import com.hr.maternity.entity.HistoryDO;
import com.hr.maternity.entity.MaternityAllowanceResultDO;
import com.hr.maternity.entity.MaternityLeaveResultDO;
import com.hr.maternity.enums.RecordTypeEnum;
import com.hr.maternity.repository.HistoryRepository;
import com.hr.maternity.service.HistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 历史记录服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HistoryServiceImpl implements HistoryService {

    private final HistoryRepository historyRepository;

    @Override
    public List<HistoryDTO> findByLanId(String lanId) {
        log.info("查询员工历史记录，lanId: {}", lanId);
        
        List<HistoryDTO> historyList = new ArrayList<>();
        
        // 1. 从 history 表按照 lanId 查询所有记录
        List<HistoryDO> histories = historyRepository.findByLanId(lanId);
        log.info("从 history 表查询到 {} 条记录", histories.size());
        
        for (HistoryDO history : histories) {
            HistoryDTO dto = null;
            
            // 2. 根据 RecordType 进行不同的映射
            if (RecordTypeEnum.MATERNITY.equals(history.getRecordType())) {
                // 3. MATERNITY 类型的映射
                dto = convertMaternityHistory(history);
            } else if (RecordTypeEnum.ALLOWANCE.equals(history.getRecordType())) {
                // 4. ALLOWANCE 类型的映射
                dto = convertAllowanceHistory(history);
            }
            
            if (dto != null) {
                historyList.add(dto);
            }
        }
        
        // 按计算时间倒序排序
        historyList.sort(Comparator.comparing(HistoryDTO::getCalculatedAt, 
                Comparator.nullsLast(Comparator.reverseOrder())));
        
        log.info("转换后得到 {} 条历史记录", historyList.size());
        return historyList;
    }

    /**
     * 转换 MATERNITY 类型的历史记录
     * @param history 历史记录
     * @return HistoryDTO
     */
    private HistoryDTO convertMaternityHistory(HistoryDO history) {
        MaternityLeaveResultDO leaveResult = history.getMaternityLeaveResult();
        if (leaveResult == null) {
            log.warn("History ID {} 的 MaternityLeaveResult 为空", history.getId());
            return null;
        }
        
        return HistoryDTO.builder()
                .id(String.valueOf(history.getId()))  // HistoryDTO.id -> history.id
                .employeeName(leaveResult.getEmployeeName())  // -> MaternityLeaveResultDO.employeeName
                .employeeId(leaveResult.getLanId())  // -> MaternityLeaveResultDO.lanId
                .city(leaveResult.getCityCode())  // -> MaternityLeaveResultDO.cityCode
                .cityName(leaveResult.getCityName())  // -> MaternityLeaveResultDO.cityName
                .startDate(leaveResult.getStartDate())  // -> MaternityLeaveResultDO.startDate
                .endDate(leaveResult.getEndDate())  // -> MaternityLeaveResultDO.endDate
                .totalDays(leaveResult.getTotalDays())  // -> MaternityLeaveResultDO.totalDays
                .companyCompensation(BigDecimal.ZERO)  // -> 0
                .employeeCompensation(BigDecimal.ZERO)  // -> 0
                .calculatedAt(ZonedDateTime.now())  // -> 当前时间
                .recordType(RecordTypeEnum.MATERNITY.getCode())
                .build();
    }

    /**
     * 转换 ALLOWANCE 类型的历史记录
     * @param history 历史记录
     * @return HistoryDTO
     */
    private HistoryDTO convertAllowanceHistory(HistoryDO history) {
        MaternityAllowanceResultDO allowanceResult = history.getMaternityAllowanceResult();
        MaternityLeaveResultDO leaveResult = history.getMaternityLeaveResult();
        
        if (allowanceResult == null) {
            log.warn("History ID {} 的 MaternityAllowanceResult 为空", history.getId());
            return null;
        }
        
        if (leaveResult == null) {
            log.warn("History ID {} 的 MaternityLeaveResult 为空", history.getId());
            return null;
        }
        
        // 获取 submissionTime: MaternityAllowanceResultDO.updateDate
        LocalDateTime updateDate = allowanceResult.getUpdateDate();
        
        return HistoryDTO.builder()
                .id(String.valueOf(history.getId()))  // HistoryDTO.id -> history.id
                .employeeName(allowanceResult.getEmployeeName())  // -> MaternityAllowanceResultDO.employeeName
                .employeeId(allowanceResult.getLanId())  // -> MaternityAllowanceResultDO.lanId
                .city(allowanceResult.getCityCode())  // -> MaternityAllowanceResultDO.cityCode
                .cityName(allowanceResult.getCityName())  // -> MaternityAllowanceResultDO.cityName
                .startDate(leaveResult.getStartDate())  // -> MaternityLeaveResultDO.startDate
                .endDate(leaveResult.getEndDate())  // -> MaternityLeaveResultDO.endDate
                .totalDays(leaveResult.getTotalDays())  // -> MaternityLeaveResultDO.totalDays
                .companyCompensation(allowanceResult.getCompensationAmount())  // -> MaternityAllowanceResultDO.compensationAmount
                .employeeCompensation(allowanceResult.getEmployeeRefundAmount())  // -> MaternityAllowanceResultDO.employeeRefundAmount
                .calculatedAt(ZonedDateTime.now())  // -> 当前时间
                .submissionTime(updateDate != null ? updateDate.toLocalDate() : null)  // -> MaternityAllowanceResultDO.updateDate
                .recordType(RecordTypeEnum.ALLOWANCE.getCode())
                .build();
    }
}
