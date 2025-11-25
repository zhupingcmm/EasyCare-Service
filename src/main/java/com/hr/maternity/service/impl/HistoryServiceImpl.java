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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

/**
 * 历史记录服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HistoryServiceImpl implements HistoryService {

    private final HistoryRepository historyRepository;

    @Override
    public Page<HistoryDTO> findByLanId(String lanId, Pageable pageable) {
        log.info("分页查询员工历史记录，lanId: {}, page: {}, size: {}", 
                lanId, pageable.getPageNumber(), pageable.getPageSize());
        
        // 1. 分页查询历史记录
        Page<HistoryDO> historyPage = historyRepository.findByLanId(lanId, pageable);
        log.info("从 history 表查询到 {} 条记录，共 {} 页", 
                historyPage.getTotalElements(), historyPage.getTotalPages());
        
        // 2. 转换为DTO列表
        List<HistoryDTO> dtos = new ArrayList<>();
        for (HistoryDO history : historyPage.getContent()) {
            HistoryDTO dto = convertToDto(history);
            if (dto != null) {
                dtos.add(dto);
            }
        }
        
        // 3. 返回分页结果
        return new PageImpl<>(dtos, pageable, historyPage.getTotalElements());
    }
    
    @Override
    public List<HistoryDTO> findByLanId(String lanId) {
        log.info("查询员工历史记录，lanId: {}", lanId);
        
        List<HistoryDTO> historyList = new ArrayList<>();
        
        // 1. 从 history 表按照 lanId 查询所有记录
        List<HistoryDO> histories = historyRepository.findByLanId(lanId);
        log.info("从 history 表查询到 {} 条记录", histories.size());
        
        for (HistoryDO history : histories) {
            HistoryDTO dto = convertToDto(history);
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
     * 将 HistoryDO 转换为 HistoryDTO
     * @param history 历史记录实体
     * @return 历史记录DTO
     */
    private HistoryDTO convertToDto(HistoryDO history) {
        if (history == null) {
            return null;
        }
        
        // 根据 RecordType 进行不同的映射
        if (RecordTypeEnum.MATERNITY.equals(history.getRecordType())) {
            return convertMaternityHistory(history);
        } else if (RecordTypeEnum.ALLOWANCE.equals(history.getRecordType())) {
            return convertAllowanceHistory(history);
        }
        
        return null;
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
