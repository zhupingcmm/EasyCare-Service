package com.hr.maternity.encryption.demo.service;

import com.hr.maternity.encryption.demo.dto.DemoEncryptedRecordRequest;
import com.hr.maternity.encryption.demo.dto.DemoEncryptedRecordResponse;

import java.util.List;
import java.util.UUID;

public interface DemoEncryptedRecordService {

    DemoEncryptedRecordResponse createRecord(DemoEncryptedRecordRequest request);

    DemoEncryptedRecordResponse findById(UUID id);

    List<DemoEncryptedRecordResponse> findAll();
}
