package com.easy.care.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.easy.care.common.ApiResponse;
import com.easy.care.dto.history.HistoryAddRequest;
import com.easy.care.dto.history.HistoryDeleteRequest;
import com.easy.care.dto.history.HistoryOperationStatusDTO;
import com.easy.care.dto.history.HistoryQueryRequest;
import com.easy.care.dto.history.HistoryRecordDTO;
import com.easy.care.service.HistoryRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * HR 透传历史记录接口
 */
@Slf4j
@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
@Tag(name = "历史记录透传", description = "HR 历史记录透传接口")
public class HistoryRecordController {

    private final HistoryRecordService historyRecordService;

    @PostMapping("/add")
    @Operation(summary = "新增历史记录", description = "保存或更新 HR 旗下员工历史记录")
    public ApiResponse<HistoryRecordDTO> addHistory(@Valid @RequestBody HistoryAddRequest request) {
        log.info("新增历史记录接口，hrId: {}, employeeId: {}", request.getHrId(), request.getEmployeeId());
        HistoryRecordDTO saved = historyRecordService.saveHistory(request);
        return ApiResponse.success(saved);
    }

    @PostMapping("/query")
    @Operation(summary = "查询历史记录", description = "根据 HR 查询历史记录，可选 employeeIds 过滤")
    public ApiResponse<List<JsonNode>> queryHistory(@Valid @RequestBody HistoryQueryRequest request) {
        List<JsonNode> records = historyRecordService.queryHistory(request);
        return ApiResponse.success(records);
    }

    @PostMapping("/delete")
    @Operation(summary = "删除历史记录", description = "批量删除 HR 的员工历史记录")
    public ApiResponse<HistoryOperationStatusDTO> deleteHistory(@Valid @RequestBody HistoryDeleteRequest request) {
        HistoryOperationStatusDTO result = historyRecordService.deleteHistory(request);
        return ApiResponse.success(result);
    }
}
