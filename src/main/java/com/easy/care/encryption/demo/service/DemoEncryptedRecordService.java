package com.easy.care.encryption.demo.service;

import com.easy.care.encryption.demo.dto.DemoEncryptedRecordRequest;
import com.easy.care.encryption.demo.dto.DemoEncryptedRecordResponse;

import java.util.List;
import java.util.UUID;

public interface DemoEncryptedRecordService {

    DemoEncryptedRecordResponse createRecord(DemoEncryptedRecordRequest request);

    DemoEncryptedRecordResponse findById(UUID id);

    List<DemoEncryptedRecordResponse> findAll();
}
