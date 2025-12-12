package com.ocbc.ms.easy.care.controller;

import com.ocbc.ms.easy.care.common.ApiResponse;
import com.ocbc.ms.easy.care.dto.BatchTestResultDTO;
import com.ocbc.ms.easy.care.service.BatchTestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


/**
 * 批量测试控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/batch-test")
@RequiredArgsConstructor
@Tag(name = "批量测试", description = "Excel 批量测试相关接口")
public class BatchTestController {

    private final BatchTestService batchTestService;

    /**
     * 运行批量测试（通用）
     * 
     * @param file Excel 文件（支持 .xls 和 .xlsx 格式）
     * @return 批量测试结果
     */
    @PostMapping(value = "/run", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "运行批量测试（通用）", description = "上传 Excel 文件进行批量测试，返回测试结果统计和详情")
    public ResponseEntity<ApiResponse<BatchTestResultDTO>> runBatchTest(
            @RequestParam("file") MultipartFile file) {
        
        log.info("接收到批量测试请求，文件名: {}, 大小: {} bytes", 
            file.getOriginalFilename(), file.getSize());
        
        // 验证文件
        if (file.isEmpty()) {
            log.warn("上传的文件为空");
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(400, "上传的文件为空"));
        }
        
        String fileName = file.getOriginalFilename();
        if (fileName == null || (!fileName.endsWith(".xls") && !fileName.endsWith(".xlsx"))) {
            log.warn("不支持的文件格式: {}", fileName);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(400, "不支持的文件格式，仅支持 .xls 和 .xlsx 格式"));
        }
        
        try {
            // 执行批量测试
            BatchTestResultDTO result = batchTestService.runBatchTest(file);
            
            log.info("批量测试执行成功，总数: {}, 成功: {}, 失败: {}", 
                result.getTotalCount(), result.getSuccessCount(), result.getFailureCount());
            
            return ResponseEntity.ok(ApiResponse.success(result));
            
        } catch (Exception e) {
            log.error("批量测试执行失败", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error(500, "批量测试执行失败: " + e.getMessage()));
        }
    }

}
