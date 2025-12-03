package com.hr.maternity.controller;

import com.hr.maternity.entity.CityDO;
import com.hr.maternity.repository.CityRepository;
import com.hr.maternity.service.HolidayService;
import com.hr.maternity.service.WorkdayCalculatorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * 系统支持功能控制器
 */
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
}
