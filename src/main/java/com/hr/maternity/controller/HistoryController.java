package com.hr.maternity.controller;

import com.hr.maternity.dto.HistoryDTO;
import com.hr.maternity.service.HistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 历史记录控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
@Tag(name = "历史记录", description = "产假和津贴历史记录查询接口")
public class HistoryController {

    private final HistoryService historyService;

    /**
     * 查询历史记录
     * 
     * @param lanId 员工工号
     * @return 历史记录列表
     */
    @GetMapping
    @Operation(summary = "查询历史记录", description = "根据员工工号查询产假和津贴历史记录")
    public ResponseEntity<List<HistoryDTO>> getHistory(
            @Parameter(description = "员工工号") @RequestParam String lanId) {
        
        log.info("查询历史记录，lanId: {}", lanId);
        
        List<HistoryDTO> historyList = historyService.findByLanId(lanId);
        
        return ResponseEntity.ok(historyList);
    }
}
