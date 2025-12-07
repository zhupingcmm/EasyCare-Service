package com.hr.maternity.controller;

import com.hr.maternity.constant.MiscarriageLeaveRulesConstants;
import com.hr.maternity.dto.MaternityLeaveRequest;
import com.hr.maternity.dto.MaternityLeaveResponse;
import com.hr.maternity.dto.MiscarriageLeaveDetail;
import com.hr.maternity.service.MaternityLeaveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 产假计算控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/maternity-leave")
@RequiredArgsConstructor
@Tag(name = "产假计算", description = "产假天数计算相关接口")
public class MaternityLeaveController {

    private final MaternityLeaveService maternityLeaveService;

    /**
     * 计算产假天数
     * 
     * @param request 产假计算请求
     * @return 产假计算结果
     */
    @PostMapping("/calculate")
    @Operation(summary = "计算产假天数", description = "根据城市和个人情况计算产假天数")
    public ResponseEntity<MaternityLeaveResponse> calculateMaternityLeave(
            @Valid @RequestBody MaternityLeaveRequest request) {
        MaternityLeaveResponse response = maternityLeaveService.calculateMaternityLeaveNew(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/ref-data/miscarriage-rules")
    @Operation(summary = "查询流产假规则", description = "查询所有城市关于流产假的休假天数的规则列表")
    public ResponseEntity<List<MiscarriageLeaveDetail>> getMiscarriageRules(String cityCode) {
        List<MiscarriageLeaveDetail> list = MiscarriageLeaveRulesConstants.MISCARRIAGE_LEAVE_RULE_MAP.values().stream().toList();
        if (cityCode != null) {
            list = list.stream().filter(item -> cityCode.equals(item.getCityCode())).toList();
        }
//        if (list.isEmpty()) {
//            list = MiscarriageLeaveRulesConstants.map.values().stream().toList();
//        }
        return ResponseEntity.ok(list);
    }

}
