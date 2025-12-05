package com.hr.maternity.controller;

import com.hr.maternity.common.ApiResponse;
import com.hr.maternity.dto.MaternityLeaveTypeRequest;
import com.hr.maternity.dto.MaternityLeaveTypeResponse;
import com.hr.maternity.service.MaternityLeaveTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 产假类型管理接口
 */
@Slf4j
@RestController
@RequestMapping("/api/maternity-leave-types")
@RequiredArgsConstructor
@Tag(name = "产假类型管理")
public class MaternityLeaveTypeController {

    private final MaternityLeaveTypeService maternityLeaveTypeService;

    /**
     * 创建产假类型
     */
    @PostMapping
    @Operation(summary = "创建产假类型", description = "创建新的产假类型")
    public ApiResponse<MaternityLeaveTypeResponse> createMaternityLeaveType(
            @Valid @RequestBody MaternityLeaveTypeRequest request) {
        log.info("收到创建产假类型请求，请求参数: {}", request);
        MaternityLeaveTypeResponse response = maternityLeaveTypeService.createMaternityLeaveType(request);
        return ApiResponse.success(response);
    }

    /**
     * 分页查询产假类型
     */
    @GetMapping
    @Operation(summary = "分页查询产假类型", description = "分页查询所有启用的产假类型")
    public ApiResponse<Page<MaternityLeaveTypeResponse>> listMaternityLeaveTypes(
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("收到分页查询产假类型请求，分页参数: {}", pageable);
        Page<MaternityLeaveTypeResponse> page = maternityLeaveTypeService.listMaternityLeaveTypes(pageable);
        return ApiResponse.success(page);
    }

    /**
     * 根据ID查询产假类型
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询产假类型详情", description = "根据ID查询产假类型详细信息")
    public ApiResponse<MaternityLeaveTypeResponse> getMaternityLeaveTypeById(@PathVariable Integer id) {
        log.info("收到查询产假类型详情请求，ID: {}", id);
        MaternityLeaveTypeResponse response = maternityLeaveTypeService.getMaternityLeaveTypeById(id);
        return ApiResponse.success(response);
    }

    /**
     * 更新产假类型
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新产假类型", description = "更新指定ID的产假类型信息")
    public ApiResponse<MaternityLeaveTypeResponse> updateMaternityLeaveType(
            @PathVariable Integer id,
            @Valid @RequestBody MaternityLeaveTypeRequest request) {
        log.info("收到更新产假类型请求，ID: {}, 请求参数: {}", id, request);
        MaternityLeaveTypeResponse response = maternityLeaveTypeService.updateMaternityLeaveType(id, request);
        return ApiResponse.success(response);
    }

    /**
     * 删除产假类型（逻辑删除）
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除产假类型", description = "逻辑删除指定ID的产假类型")
    public ApiResponse<Void> deleteMaternityLeaveType(@PathVariable Integer id) {
        log.info("收到删除产假类型请求，ID: {}", id);
        maternityLeaveTypeService.deleteMaternityLeaveType(id);
        return ApiResponse.success(null);
    }

    /**
     * 查询所有启用的产假类型（不分页）
     */
    @GetMapping("/enabled")
    @Operation(summary = "查询所有启用的产假类型", description = "查询所有启用的产假类型，不分页")
    public ApiResponse<List<MaternityLeaveTypeResponse>> listEnabledMaternityLeaveTypes() {
        log.info("收到查询所有启用的产假类型请求");
        List<MaternityLeaveTypeResponse> list = maternityLeaveTypeService.listEnabledMaternityLeaveTypes();
        return ApiResponse.success(list);
    }

    /**
     * 根据是否流产假查询
     */
    @GetMapping("/by-abortion")
    @Operation(summary = "根据是否流产假查询", description = "根据是否流产假查询产假类型列表")
    public ApiResponse<List<MaternityLeaveTypeResponse>> listByIsAbortion(
            @RequestParam Boolean isAbortion) {
        log.info("收到根据是否流产假查询请求，isAbortion: {}", isAbortion);
        List<MaternityLeaveTypeResponse> list = maternityLeaveTypeService.listByIsAbortion(isAbortion);
        return ApiResponse.success(list);
    }
}
