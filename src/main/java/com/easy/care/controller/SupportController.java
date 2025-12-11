package com.easy.care.controller;

import com.easy.care.common.ApiResponse;
import com.easy.care.domain.HolidayInfo;
import com.easy.care.entity.CityDO;
import com.easy.care.repository.CityRepository;
import com.easy.care.service.HolidayService;
import com.easy.care.service.WorkdayCalculatorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 系统支持功能控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/support")
@RequiredArgsConstructor
@Tag(name = "系统支持", description = "系统支持相关接口")
public class SupportController {

    private final HolidayService holidayService;
    private final CityRepository cityRepository;
    private final WorkdayCalculatorService workdayCalculatorService;

    /**
     * 获取所有支持的城市列表
     * 
     * @return 支持的城市列表
     */
    @GetMapping("/cities")
    @Operation(summary = "获取支持的城市", description = "获取系统支持的所有城市列表")
    public ResponseEntity<List<CityDO>> getSupportedCities() {
        List<CityDO> cities = cityRepository.findByEnabledTrueOrderBySortOrder();
        return ResponseEntity.ok(cities);
    }

    /**
     * 获取指定年份的公共假日
     * 
     * @param year 年份
     * @return 公共假日列表
     */
    @GetMapping("/holidays/{year}")
    @Operation(summary = "获取公共假日", description = "获取指定年份的中国公共假日列表")
    public ResponseEntity<?> getPublicHolidays(@PathVariable String year) {
        return ResponseEntity.ok(holidayService.getPublicHolidays(year));
    }

    /**
     * 计算两个日期区间（含）内每个月的工作日天数
     * - 周一至周五为工作日；周末休息；
     * - 若能获取对应年份的公共假日与调休（补班），会进行修正；
     * - 若对应年份无法获取公共假日/调休，则忽略，仅按周末计算。
     *
     * @param start 开始日期（格式：yyyy-MM-dd）
     * @param end   结束日期（格式：yyyy-MM-dd）
     * @return 列表形式返回，每项包含 yearMonth(yyyy-MM) 与 workdays
     */
    @GetMapping("/workdays")
    @Operation(summary = "计算区间内每月工作日天数", description = "考虑公共假日与调休（若可用），计算[start, end]范围内各月的工作日天数")
    public ResponseEntity<?> getMonthlyWorkdays(
            @RequestParam("start") String start,
            @RequestParam("end") String end
    ) {
        LocalDate startDate = LocalDate.parse(start);
        LocalDate endDate = LocalDate.parse(end);
        return ResponseEntity.ok(workdayCalculatorService.calculateMonthlyWorkdays(startDate, endDate));
    }
    
    /**
     * 按日期范围查询节假日
     * 
     * @param start 开始日期（格式：yyyy-MM-dd）
     * @param end 结束日期（格式：yyyy-MM-dd）
     * @return 节假日列表
     */
    @GetMapping("/holidays")
    @Operation(summary = "按日期范围查询节假日", description = "查询指定日期范围内的节假日信息")
    public ApiResponse<List<Map<String, Object>>> getHolidaysByDateRange(
            @Parameter(description = "开始日期", example = "2024-11-01") @RequestParam String start,
            @Parameter(description = "结束日期", example = "2025-04-25") @RequestParam String end) {
        
        log.info("查询节假日，开始日期: {}, 结束日期: {}", start, end);
        
        LocalDate startDate = LocalDate.parse(start);
        LocalDate endDate = LocalDate.parse(end);
        
        Map<LocalDate, HolidayInfo> holidayMap = holidayService.getHolidaysByDateRange(startDate, endDate);
        
        List<Map<String, Object>> result = holidayMap.values().stream()
                .map(this::convertToMap)
                .sorted(Comparator.comparing(m -> (String) m.get("date")))
                .collect(Collectors.toList());
        
        return ApiResponse.success(result);
    }
    
    /**
     * 转换HolidayInfo为Map
     */
    private Map<String, Object> convertToMap(HolidayInfo info) {
        Map<String, Object> map = new HashMap<>();
        map.put("date", info.getDate().toString());
        map.put("name", info.getName());
        map.put("isPublicHoliday", info.getIsPublicHoliday());
        map.put("type", info.getType());
        map.put("name_cn", info.getName());
        map.put("name_en", info.getName());
        return map;
    }
}
