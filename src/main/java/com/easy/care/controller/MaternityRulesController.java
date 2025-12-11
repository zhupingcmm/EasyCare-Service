package com.easy.care.controller;

import com.easy.care.common.ApiResponse;
import com.easy.care.dto.MaternityRulesRequest;
import com.easy.care.dto.MaternityRulesResponse;
import com.easy.care.service.MaternityRulesService;
import com.easy.care.util.ExcelExporter;
import com.easy.care.util.ExcelParser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
 * 产假规则控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/maternity-rules")
@RequiredArgsConstructor
@Tag(name = "产假规则管理", description = "产假规则的增删改查接口")
public class MaternityRulesController {

    private final MaternityRulesService maternityRulesService;

    /**
     * 创建产假规则
     */
    @PostMapping
    @Operation(summary = "创建产假规则", description = "创建新的产假规则")
    public ApiResponse<MaternityRulesResponse> createMaternityRules(@Valid @RequestBody MaternityRulesRequest request) {
        log.info("收到创建产假规则请求: {}", request);
        MaternityRulesResponse response = maternityRulesService.createMaternityRules(request);
        return ApiResponse.success(response);
    }

    /**
     * 查询所有产假规则（支持分页和不分页）
     * 当不传入page和size参数时，返回所有数据
     * 当传入page和size参数时，返回分页数据
     */
    @GetMapping
    @Operation(summary = "查询产假规则", description = "查询所有产假规则，支持按城市过滤。不传page/size参数返回全部，传入则返回分页数据")
    public ApiResponse<?> listAllMaternityRules(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false, defaultValue = "updateDate") String sort,
            @RequestParam(required = false, defaultValue = "DESC") String direction) {
        
        // 判断是否传入了分页参数
        if (page != null || size != null) {
            // 有分页参数，返回分页数据
            int pageNum = page != null ? page : 0;
            int pageSize = size != null ? size : 10;
            Sort.Direction sortDirection = "ASC".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
            Pageable pageable = org.springframework.data.domain.PageRequest.of(pageNum, pageSize, Sort.by(sortDirection, sort));
            
            log.info("收到分页查询产假规则请求，城市: {}, 分页参数: page={}, size={}", city, pageNum, pageSize);
            Page<MaternityRulesResponse> pageResult = maternityRulesService.listAllMaternityRules(city, pageable);
            return ApiResponse.success(pageResult);
        } else {
            // 无分页参数，返回所有数据
            log.info("收到查询所有产假规则请求（不分页），城市: {}", city);
            List<MaternityRulesResponse> list = maternityRulesService.listAllMaternityRulesWithoutPage(city);
            return ApiResponse.success(list);
        }
    }

    /**
     * 下载产假规则导入模板
     */
    @GetMapping("/template/download")
    @Operation(summary = "下载导入模板", description = "下载产假规则批量导入的CSV模板文件")
    public ResponseEntity<byte[]> downloadTemplate() throws IOException {
        log.info("下载产假规则导入模板");

        byte[] csvBytes = ExcelExporter.createMaternityRulesTemplate();
        
        String fileName = "产假规则导入模板.csv";
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", encodedFileName);
        headers.setContentLength(csvBytes.length);

        log.info("产假规则导入模板下载成功，文件大小: {} bytes", csvBytes.length);
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(csvBytes);
    }

    /**
     * 批量导入产假规则
     */
    @PostMapping("/import")
    @Operation(summary = "批量导入", description = "通过CSV文件批量导入产假规则，支持新增和更新")
    public ApiResponse<String> importMaternityRules(@RequestParam("file") MultipartFile file) throws IOException {
        log.info("收到批量导入产假规则请求，文件名: {}", file.getOriginalFilename());
        
        if (file.isEmpty()) {
            return ApiResponse.error(400, "文件不能为空");
        }
        
        List<Map<String, Object>> dataList = ExcelParser.parseCsvToMapList(file.getInputStream());
        int totalCount = maternityRulesService.batchImportMaternityRules(dataList);
        
        log.info("批量导入产假规则完成，共处理 {} 条数据", totalCount);
        return ApiResponse.success("导入完成，共处理 " + totalCount + " 条数据（包含新增和更新）");
    }

    /**
     * 更新产假规则
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新产假规则", description = "根据ID更新产假规则")
    public ApiResponse<MaternityRulesResponse> updateMaternityRules(
            @PathVariable Integer id,
            @Valid @RequestBody MaternityRulesRequest request) {
        log.info("收到更新产假规则请求，ID: {}, 请求参数: {}", id, request);
        MaternityRulesResponse response = maternityRulesService.updateMaternityRules(id, request);
        return ApiResponse.success(response);
    }

    /**
     * 删除产假规则（逻辑删除）
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除产假规则", description = "逻辑删除产假规则（将isActive设置为false）")
    public ApiResponse<Void> deleteMaternityRules(@PathVariable Integer id) {
        log.info("收到删除产假规则请求，ID: {}", id);
        maternityRulesService.deleteMaternityRules(id);
        return ApiResponse.success(null);
    }
}
