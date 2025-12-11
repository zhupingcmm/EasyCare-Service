package com.easy.care.controller;

import com.easy.care.dto.MaternityAllowanceRequest;
import com.easy.care.dto.MaternityAllowanceResponse;
import com.easy.care.service.MaternityAllowanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 生育津贴计算控制器
 */
@RestController
@RequestMapping("/api/maternity-allowance")
@RequiredArgsConstructor
@Tag(name = "生育津贴计算", description = "生育津贴金额计算相关接口")
public class MaternityAllowanceController {

    private final MaternityAllowanceService maternityAllowanceService;

    /**
     * 计算生育津贴
     * 
     * @param request 生育津贴计算请求
     * @return 生育津贴计算结果
     */
    @PostMapping("/calculate")
    @Operation(summary = "计算生育津贴", description = "根据城市和个人情况计算生育津贴金额")
    public ResponseEntity<MaternityAllowanceResponse> calculateMaternityAllowance(
            @Valid @RequestBody MaternityAllowanceRequest request) {
        MaternityAllowanceResponse response = maternityAllowanceService.calculateMaternityAllowance(request);
        return ResponseEntity.ok(response);
    }

}
