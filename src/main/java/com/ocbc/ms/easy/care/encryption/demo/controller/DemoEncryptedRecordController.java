package com.ocbc.ms.easy.care.encryption.demo.controller;

import com.ocbc.ms.easy.care.common.ApiResponse;
import com.ocbc.ms.easy.care.encryption.demo.dto.DemoEncryptedRecordRequest;
import com.ocbc.ms.easy.care.encryption.demo.dto.DemoEncryptedRecordResponse;
import com.ocbc.ms.easy.care.encryption.demo.service.DemoEncryptedRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/demo/encrypted-records")
@RequiredArgsConstructor
@Tag(name = "加密字段 Demo", description = "演示 Base64 字段级加密的入库与出库")
public class DemoEncryptedRecordController {

    private final DemoEncryptedRecordService demoEncryptedRecordService;

    @PostMapping
    @Operation(summary = "创建 Demo 记录")
    public ApiResponse<DemoEncryptedRecordResponse> create(@Valid @RequestBody DemoEncryptedRecordRequest request) {
        log.info("收到 Demo 加密记录创建请求");
        return ApiResponse.success(demoEncryptedRecordService.createRecord(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据 ID 查询 Demo 记录")
    public ApiResponse<DemoEncryptedRecordResponse> findById(@PathVariable UUID id) {
        return ApiResponse.success(demoEncryptedRecordService.findById(id));
    }

    @GetMapping
    @Operation(summary = "查询全部 Demo 记录")
    public ApiResponse<List<DemoEncryptedRecordResponse>> findAll() {
        return ApiResponse.success(demoEncryptedRecordService.findAll());
    }
}
