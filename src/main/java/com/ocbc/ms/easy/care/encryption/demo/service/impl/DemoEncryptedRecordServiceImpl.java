package com.ocbc.ms.easy.care.encryption.demo.service.impl;

import com.ocbc.ms.easy.care.encryption.demo.dto.DemoEncryptedRecordRequest;
import com.ocbc.ms.easy.care.encryption.demo.dto.DemoEncryptedRecordResponse;
import com.ocbc.ms.easy.care.encryption.demo.entity.DemoEncryptedRecord;
import com.ocbc.ms.easy.care.encryption.demo.repository.DemoEncryptedRecordRepository;
import com.ocbc.ms.easy.care.encryption.demo.service.DemoEncryptedRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DemoEncryptedRecordServiceImpl implements DemoEncryptedRecordService {

    private final DemoEncryptedRecordRepository repository;

    @Override
    public DemoEncryptedRecordResponse createRecord(DemoEncryptedRecordRequest request) {
        log.info("创建加密 Demo 记录, plainKeyword={}", request.getPlainKeyword());
        DemoEncryptedRecord record = DemoEncryptedRecord.builder()
                .plainKeyword(request.getPlainKeyword())
                .nationalId(request.getNationalId())
                .monthlySalary(request.getMonthlySalary())
                .hireDate(request.getHireDate())
                .childCount(request.getChildCount())
                .hasAllowance(request.getHasAllowance())
                .retirementAccount(request.getRetirementAccount())
                .newAccount(request.getNewAccount())
                .build();
        DemoEncryptedRecord saved = repository.save(record);
        return toResponse(saved);
    }

    @Override
    public DemoEncryptedRecordResponse findById(UUID id) {
        DemoEncryptedRecord record = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("记录不存在: " + id));
        return toResponse(record);
    }

    @Override
    public List<DemoEncryptedRecordResponse> findAll() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    private DemoEncryptedRecordResponse toResponse(DemoEncryptedRecord record) {
        return DemoEncryptedRecordResponse.builder()
                .id(record.getId())
                .plainKeyword(record.getPlainKeyword())
                .nationalId(record.getNationalId())
                .monthlySalary(record.getMonthlySalary())
                .hireDate(record.getHireDate())
                .childCount(record.getChildCount())
                .hasAllowance(record.getHasAllowance())
                .retirementAccount(record.getRetirementAccount())
                .newAccount(record.getNewAccount())
                .build();
    }
}
