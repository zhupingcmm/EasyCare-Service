package com.hr.maternity.controller;

import com.hr.maternity.common.ApiResponse;
import com.hr.maternity.dto.AllowanceRulesRequest;
import com.hr.maternity.dto.AllowanceRulesResponse;
import com.hr.maternity.service.AllowanceRulesService;
import com.hr.maternity.util.ExcelExporter;
import com.hr.maternity.util.ExcelParser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * 津贴规则控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/allowance-rules")
@RequiredArgsConstructor
@Tag(name = "津贴规则管理", description = "津贴规则的增删改查接口")
public class AllowanceRulesController {

    private final AllowanceRulesService allowanceRulesService;

    /**
     * 创建津贴规则
     */
    @PostMapping
    @Operation(summary = "创建津贴规则", description = "创建新的津贴规则")
    public ApiResponse<AllowanceRulesResponse> createAllowanceRules(@Valid @RequestBody AllowanceRulesRequest request) {
        log.info("收到创建津贴规则请求: {}", request);
        return ApiResponse.success(allowanceRulesService.createAllowanceRules(request));
    }

    /**
     * 根据ID查询津贴规则
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询津贴规则", description = "根据ID查询单个津贴规则")
    public ApiResponse<AllowanceRulesResponse> getAllowanceRulesById(@PathVariable Integer id) {
        log.info("根据ID查询津贴规则，ID: {}", id);
        return ApiResponse.success(allowanceRulesService.getAllowanceRulesById(id));
    }

    /**
     * 查询所有津贴规则（分页）
     */
    @GetMapping
    @Operation(summary = "查询所有津贴规则", description = "分页查询所有津贴规则列表，支持按城市过滤")
    public ApiResponse<Page<AllowanceRulesResponse>> listAllAllowanceRules(
            @RequestParam(required = false) String city,
            @PageableDefault(page = 0, size = 10, sort = "updateDate", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("分页查询津贴规则，城市: {}, page: {}, size: {}", city, pageable.getPageNumber(), pageable.getPageSize());
        return ApiResponse.success(allowanceRulesService.listAllAllowanceRules(city, pageable));
    }

    /**
     * 更新津贴规则
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新津贴规则", description = "根据ID更新津贴规则")
    public ApiResponse<AllowanceRulesResponse> updateAllowanceRules(
            @PathVariable Integer id,
            @Valid @RequestBody AllowanceRulesRequest request) {
        log.info("更新津贴规则，ID: {}, 请求参数: {}", id, request);
        return ApiResponse.success(allowanceRulesService.updateAllowanceRules(id, request));
    }

    /**
     * 删除津贴规则（逻辑删除）
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除津贴规则", description = "根据ID逻辑删除津贴规则（将isActive设置为false）")
    public ApiResponse<Void> deleteAllowanceRules(@PathVariable Integer id) {
        log.info("删除津贴规则，ID: {}", id);
        allowanceRulesService.deleteAllowanceRules(id);
        return ApiResponse.success(null);
    }

    /**
     * 下载津贴规则导入模板
     */
    @GetMapping("/template/download")
    @Operation(summary = "下载导入模板", description = "下载津贴规则批量导入的CSV模板文件")
    public ResponseEntity<byte[]> downloadTemplate() throws IOException {
        log.info("下载津贴规则导入模板");

        byte[] csvBytes = ExcelExporter.createAllowanceRulesTemplate();
        
        String fileName = "津贴规则导入模板.csv";
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", encodedFileName);
        headers.setContentLength(csvBytes.length);

        log.info("津贴规则导入模板下载成功，文件大小: {} bytes", csvBytes.length);
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(csvBytes);
    }

    /**
     * 批量导入津贴规则
     */
    @PostMapping("/import")
    @Operation(summary = "批量导入", description = "通过CSV文件批量导入津贴规则")
    public ApiResponse<String> importAllowanceRules(@RequestParam("file") MultipartFile file) throws IOException {
        log.info("收到批量导入津贴规则请求，文件名: {}", file.getOriginalFilename());
        
        if (file.isEmpty()) {
            return ApiResponse.error(400, "文件不能为空");
        }
        
        List<Map<String, Object>> dataList = ExcelParser.parseCsvToMapList(file.getInputStream());
        int successCount = allowanceRulesService.batchImportAllowanceRules(dataList);
        
        log.info("批量导入津贴规则完成，成功 {} 条", successCount);
        return ApiResponse.success("导入完成，成功导入 " + successCount + " 条数据");
    }
}
