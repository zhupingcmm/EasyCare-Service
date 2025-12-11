package com.easy.care.controller;

import com.easy.care.common.ApiResponse;
import com.easy.care.dto.MaternityPolicyResponse;
import com.easy.care.service.MaternityRulesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 产假政策控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/maternity-policy")
@RequiredArgsConstructor
@Tag(name = "产假政策查询", description = "根据城市代码查询产假政策")
public class MaternityPolicyController {

    private final MaternityRulesService maternityRulesService;

    /**
     * 根据城市代码查询产假政策
     */
    @GetMapping
    @Operation(summary = "查询产假政策", description = "根据城市代码查询该城市的所有产假政策规则（键值对格式）")
    public ApiResponse<List<MaternityPolicyResponse>> getMaternityPolicyByCityCode(
            @RequestParam String cityCode) {
        log.info("收到查询产假政策请求，城市代码: {}", cityCode);
        List<MaternityPolicyResponse> policies = maternityRulesService.findMaternityPolicyByCityCode(cityCode);
        return ApiResponse.success(policies);
    }
}
