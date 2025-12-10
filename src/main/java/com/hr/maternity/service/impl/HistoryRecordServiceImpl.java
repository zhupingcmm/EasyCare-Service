package com.hr.maternity.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.hr.maternity.dto.history.HistoryAddRequest;
import com.hr.maternity.dto.history.HistoryDeleteRequest;
import com.hr.maternity.dto.history.HistoryOperationStatusDTO;
import com.hr.maternity.dto.history.HistoryQueryRequest;
import com.hr.maternity.dto.history.HistoryRecordDTO;
import com.hr.maternity.entity.HistoryRecordDO;
import com.hr.maternity.repository.HistoryRecordRepository;
import com.hr.maternity.service.HistoryRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HistoryRecordServiceImpl implements HistoryRecordService {

    private final HistoryRecordRepository historyRecordRepository;

    @Override
    @Transactional
    public HistoryRecordDTO saveHistory(HistoryAddRequest request) {
        log.info("保存历史记录，hrId: {}, employeeId: {}", request.getHrId(), request.getEmployeeId());
        HistoryRecordDO record = historyRecordRepository.findByHrIdAndEmployeeId(
                        request.getHrId(),
                        request.getEmployeeId())
                .orElseGet(HistoryRecordDO::new);
        record.setHrId(request.getHrId());
        record.setEmployeeId(request.getEmployeeId());
        record.setEmployeeData(request.getEmployeeData());
        HistoryRecordDO saved = historyRecordRepository.save(record);
        return HistoryRecordDTO.builder()
                .id(saved.getId())
                .hrId(saved.getHrId())
                .employeeId(saved.getEmployeeId())
                .employeeData(saved.getEmployeeData())
                .build();
    }

    @Override
    public List<JsonNode> queryHistory(HistoryQueryRequest request) {
        log.info("查询历史记录，hrId: {}, employeeIds: {}", request.getHrId(), request.getEmployeeIds());
        List<HistoryRecordDO> result;
        if (request.getEmployeeIds() == null || request.getEmployeeIds().isEmpty()) {
            result = historyRecordRepository.findByHrIdOrderByCreatedTimeDesc(request.getHrId());
        } else {
            result = historyRecordRepository.findByHrIdAndEmployeeIdInOrderByCreatedTimeDesc(
                    request.getHrId(),
                    request.getEmployeeIds());
        }
        return result.stream()
                .map(HistoryRecordDO::getEmployeeData)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public HistoryOperationStatusDTO deleteHistory(HistoryDeleteRequest request) {
        log.info("删除历史记录，hrId: {}, employeeIds: {}", request.getHrId(), request.getEmployeeIds());
        long affected = historyRecordRepository.deleteByHrIdAndEmployeeIdIn(
                request.getHrId(),
                request.getEmployeeIds());
        return HistoryOperationStatusDTO.builder()
                .affected(affected)
                .build();
    }
}
