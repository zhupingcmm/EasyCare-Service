package com.easy.care.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.easy.care.dto.history.HistoryAddRequest;
import com.easy.care.dto.history.HistoryDeleteRequest;
import com.easy.care.dto.history.HistoryOperationStatusDTO;
import com.easy.care.dto.history.HistoryQueryRequest;
import com.easy.care.dto.history.HistoryRecordDTO;

import java.util.List;

/**
 * HR 历史记录服务（t_history）
 */
public interface HistoryRecordService {

    /**
     * 保存或更新历史记录
     */
    HistoryRecordDTO saveHistory(HistoryAddRequest request);

    /**
     * 查询历史记录 JSON
     */
    List<JsonNode> queryHistory(HistoryQueryRequest request);

    /**
     * 删除历史记录
     */
    HistoryOperationStatusDTO deleteHistory(HistoryDeleteRequest request);
}
