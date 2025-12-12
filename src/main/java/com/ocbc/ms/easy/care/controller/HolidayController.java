package com.ocbc.ms.easy.care.controller;

import com.ocbc.ms.easy.care.common.ApiResponse;
import com.ocbc.ms.easy.care.dto.HolidayRequest;
import com.ocbc.ms.easy.care.dto.HolidayResponse;
import com.ocbc.ms.easy.care.service.HolidayService;
import com.ocbc.ms.easy.care.util.ExcelExporter;
import com.ocbc.ms.easy.care.util.ExcelParser;
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
 * 节假日控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/holidays")
@RequiredArgsConstructor
@Tag(name = "节假日管理", description = "节假日的增删改查接口")
public class HolidayController {

    private final HolidayService holidayService;

    /**
     * 创建节假日
     */
    @PostMapping
    @Operation(summary = "创建节假日", description = "创建新的节假日记录")
    public ApiResponse<HolidayResponse> createHoliday(@Valid @RequestBody HolidayRequest request) {
        log.info("收到创建节假日请求: {}", request);
        HolidayResponse response = holidayService.createHoliday(request);
        return ApiResponse.success(response);
    }

    /**
     * 查询所有节假日（分页）
     */
    @GetMapping
    @Operation(summary = "查询所有节假日", description = "分页查询所有节假日")
    public ApiResponse<Page<HolidayResponse>> listAllHolidays(
            @PageableDefault(page = 0, size = 10, sort = "updateDate", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("收到查询所有节假日请求，分页参数: {}", pageable);
        Page<HolidayResponse> page = holidayService.listAllHolidays(pageable);
        return ApiResponse.success(page);
    }

    /**
     * 下载节假日导入模板
     */
    @GetMapping("/template/download")
    @Operation(summary = "下载导入模板", description = "下载节假日批量导入的CSV模板文件")
    public ResponseEntity<byte[]> downloadTemplate() throws IOException {
        log.info("下载节假日导入模板");

        byte[] csvBytes = ExcelExporter.createHolidayTemplate();
        
        String fileName = "节假日导入模板.csv";
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", encodedFileName);
        headers.setContentLength(csvBytes.length);

        log.info("节假日导入模板下载成功，文件大小: {} bytes", csvBytes.length);
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(csvBytes);
    }

    /**
     * 从公网API生成节假日CSV文件
     */
    @GetMapping("/generate-csv/{year}")
    @Operation(summary = "生成节假日CSV", description = "从公网API获取指定年份的节假日数据并生成可导入的CSV文件")
    public ResponseEntity<byte[]> generateCsvFromApi(@PathVariable String year) {
        log.info("从公网API生成{}年节假日CSV文件", year);

        try {
            byte[] csvBytes = holidayService.generateCsvFromPublicApi(year);
            
            String fileName = year + "年节假日.csv";
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv"));
            headers.setContentDispositionFormData("attachment", encodedFileName);
            headers.setContentLength(csvBytes.length);

            log.info("{}年节假日CSV文件生成成功，文件大小: {} bytes", year, csvBytes.length);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(csvBytes);
        } catch (Exception e) {
            log.error("生成{}年节假日CSV文件失败", year, e);
            return ResponseEntity.badRequest()
                    .body(("生成CSV文件失败: " + e.getMessage()).getBytes(StandardCharsets.UTF_8));
        }
    }

    /**
     * 批量导入节假日
     */
    @PostMapping("/import")
    @Operation(summary = "批量导入", description = "通过CSV文件批量导入节假日，支持新增和更新")
    public ApiResponse<String> importHolidays(@RequestParam("file") MultipartFile file) throws IOException {
        log.info("收到批量导入节假日请求，文件名: {}", file.getOriginalFilename());
        
        if (file.isEmpty()) {
            return ApiResponse.error(400, "文件不能为空");
        }
        
        List<Map<String, Object>> dataList = ExcelParser.parseCsvToMapList(file.getInputStream());
        int totalCount = holidayService.batchImportHolidays(dataList);
        
        log.info("批量导入节假日完成，共处理 {} 条数据", totalCount);
        return ApiResponse.success("导入完成，共处理 " + totalCount + " 条数据（包含新增和更新）");
    }

    /**
     * 更新特殊日期
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新特殊日期", description = "更新指定ID的特殊日期信息")
    public ApiResponse<HolidayResponse> updateHoliday(
            @PathVariable java.util.UUID id,
            @Valid @RequestBody HolidayRequest request) {
        log.info("收到更新特殊日期请求，ID: {}, 请求参数: {}", id, request);
        HolidayResponse response = holidayService.updateHoliday(id, request);
        return ApiResponse.success(response);
    }

    /**
     * 禁用特殊日期
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "禁用特殊日期", description = "禁用指定ID的特殊日期")
    public ApiResponse<Void> deleteHoliday(@PathVariable java.util.UUID id) {
        log.info("收到禁用特殊日期请求，ID: {}", id);
        holidayService.deleteHoliday(id);
        return ApiResponse.success(null);
    }
}
