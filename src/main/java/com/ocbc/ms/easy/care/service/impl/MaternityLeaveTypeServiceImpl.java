package com.ocbc.ms.easy.care.service.impl;

import com.ocbc.ms.easy.care.dto.MaternityLeaveTypeRequest;
import com.ocbc.ms.easy.care.dto.MaternityLeaveTypeResponse;
import com.ocbc.ms.easy.care.entity.MaternityLeaveType;
import com.ocbc.ms.easy.care.repository.MaternityLeaveTypeRepository;
import com.ocbc.ms.easy.care.service.MaternityLeaveTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 产假类型服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MaternityLeaveTypeServiceImpl implements MaternityLeaveTypeService {

    private final MaternityLeaveTypeRepository maternityLeaveTypeRepository;

    @Override
    @Transactional
    public MaternityLeaveTypeResponse createMaternityLeaveType(MaternityLeaveTypeRequest request) {
        log.info("开始创建产假类型，请求参数: {}", request);

        // 检查代码是否已存在
        if (maternityLeaveTypeRepository.existsByCode(request.getCode())) {
            throw new IllegalArgumentException("产假类型代码已存在: " + request.getCode());
        }

        MaternityLeaveType maternityLeaveType = new MaternityLeaveType();
        maternityLeaveType.setCode(request.getCode());
        maternityLeaveType.setName(request.getName());
        maternityLeaveType.setRemark(request.getRemark());
        maternityLeaveType.setEnabled(request.getEnabled());

        MaternityLeaveType saved = maternityLeaveTypeRepository.save(maternityLeaveType);
        log.info("产假类型创建成功，ID: {}", saved.getId());

        return convertToResponse(saved);
    }

    @Override
    public Page<MaternityLeaveTypeResponse> listMaternityLeaveTypes(Pageable pageable) {
        log.info("分页查询产假类型，分页参数: {}", pageable);

        Page<MaternityLeaveType> page = maternityLeaveTypeRepository.findByEnabled(true, pageable);
        return page.map(this::convertToResponse);
    }

    @Override
    public MaternityLeaveTypeResponse getMaternityLeaveTypeById(Integer id) {
        log.info("根据ID查询产假类型，ID: {}", id);

        MaternityLeaveType maternityLeaveType = maternityLeaveTypeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("产假类型不存在，ID: " + id));

        return convertToResponse(maternityLeaveType);
    }

    @Override
    @Transactional
    public MaternityLeaveTypeResponse updateMaternityLeaveType(Integer id, MaternityLeaveTypeRequest request) {
        log.info("更新产假类型，ID: {}, 请求参数: {}", id, request);

        MaternityLeaveType maternityLeaveType = maternityLeaveTypeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("产假类型不存在，ID: " + id));

        // 如果修改了代码，检查新代码是否已存在
        if (!maternityLeaveType.getCode().equals(request.getCode()) &&
                maternityLeaveTypeRepository.existsByCode(request.getCode())) {
            throw new IllegalArgumentException("产假类型代码已存在: " + request.getCode());
        }

        maternityLeaveType.setCode(request.getCode());
        maternityLeaveType.setName(request.getName());
        maternityLeaveType.setRemark(request.getRemark());
        maternityLeaveType.setEnabled(request.getEnabled());

        MaternityLeaveType updated = maternityLeaveTypeRepository.save(maternityLeaveType);
        log.info("产假类型更新成功，ID: {}", updated.getId());

        return convertToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteMaternityLeaveType(Integer id) {
        log.info("逻辑删除产假类型，ID: {}", id);

        MaternityLeaveType maternityLeaveType = maternityLeaveTypeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("产假类型不存在，ID: " + id));

        maternityLeaveType.setEnabled(false);
        maternityLeaveTypeRepository.save(maternityLeaveType);
        log.info("产假类型逻辑删除成功，ID: {}", id);
    }

    @Override
    public List<MaternityLeaveTypeResponse> listEnabledMaternityLeaveTypes() {
        log.info("查询所有启用的产假类型");

        List<MaternityLeaveType> list = maternityLeaveTypeRepository.findByEnabledTrue();
        return list.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<MaternityLeaveTypeResponse> listByIsAbortion(Boolean isAbortion) {
        log.info("根据是否流产假查询，isAbortion: {}", isAbortion);
        log.warn("isAbortion字段已废弃，返回所有启用的产假类型");
        
        return listEnabledMaternityLeaveTypes();
    }

    /**
     * 转换为响应DTO
     */
    private MaternityLeaveTypeResponse convertToResponse(MaternityLeaveType maternityLeaveType) {
        return MaternityLeaveTypeResponse.builder()
                .id(maternityLeaveType.getId())
                .code(maternityLeaveType.getCode())
                .name(maternityLeaveType.getName())
                .remark(maternityLeaveType.getRemark())
                .enabled(maternityLeaveType.getEnabled())
                .createDate(maternityLeaveType.getCreateDate())
                .createBy(maternityLeaveType.getCreateBy())
                .updateDate(maternityLeaveType.getUpdateDate())
                .updateBy(maternityLeaveType.getUpdateBy())
                .build();
    }
}
