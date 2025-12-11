package com.easy.care.controller;

import com.easy.care.common.ApiResponse;
import com.easy.care.dto.DystociaMiscarriageResponse;
import com.easy.care.service.MaternityRulesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 难产和流产假控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/dystocia-miscarriage")
@RequiredArgsConstructor
@Tag(name = "难产和流产假查询", description = "根据城市代码查询难产和流产假信息")
public class DystociaMiscarriageController {

    private final MaternityRulesService maternityRulesService;

    /**
     * 根据城市代码查询难产和流产假信息
     */
    @GetMapping("/query")
    @Operation(summary = "查询难产和流产假", description = "根据城市代码查询该城市的难产和流产假信息")
    public ApiResponse<DystociaMiscarriageResponse> queryDystociaMiscarriage(
            @RequestParam String cityCode) {
        log.info("收到查询难产和流产假请求，城市代码: {}", cityCode);
        DystociaMiscarriageResponse response = maternityRulesService.queryDystociaMiscarriageByCityCode(cityCode);
        return ApiResponse.success(response);
    }
}
