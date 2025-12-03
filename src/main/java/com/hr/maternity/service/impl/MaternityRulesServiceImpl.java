package com.hr.maternity.service.impl;

import com.hr.maternity.dto.MaternityLeaveTypeResponse;
import com.hr.maternity.dto.MaternityRulesRequest;
import com.hr.maternity.dto.MaternityRulesResponse;
import com.hr.maternity.entity.MaternityLeaveType;
import com.hr.maternity.entity.MaternityRules;
import com.hr.maternity.repository.MaternityLeaveTypeRepository;
import com.hr.maternity.repository.MaternityRulesRepository;
import com.hr.maternity.service.MaternityRulesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 产假规则服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MaternityRulesServiceImpl implements MaternityRulesService {

    private final MaternityRulesRepository maternityRulesRepository;
    private final MaternityLeaveTypeRepository maternityLeaveTypeRepository;

    @Override
    @Transactional
    public MaternityRulesResponse createMaternityRules(MaternityRulesRequest request) {
        log.info("开始创建产假规则，请求参数: {}", request);

        // 查找产假类型
        MaternityLeaveType maternityLeaveType = maternityLeaveTypeRepository.findById(request.getMaternityLeaveTypeId())
                .orElseThrow(() -> new IllegalArgumentException("产假类型不存在，ID: " + request.getMaternityLeaveTypeId()));

        MaternityRules maternityRules = new MaternityRules();
        maternityRules.setCity(request.getCity());
        maternityRules.setMaternityLeaveType(maternityLeaveType);
        maternityRules.setLeaveDays(request.getLeaveDays());
        maternityRules.setIsExtendable(request.getIsExtendable());
        maternityRules.setHasAllowance(request.getHasAllowance());
        maternityRules.setIsDefault(request.getIsDefault());
        maternityRules.setRadioGroup(request.getRadioGroup());
        maternityRules.setEnabled(request.getEnabled());

        MaternityRules saved = maternityRulesRepository.save(maternityRules);
        log.info("产假规则创建成功，ID: {}", saved.getId());

        return convertToResponse(saved);
    }



    @Override
    public Page<MaternityRulesResponse> listAllMaternityRules(String city, Pageable pageable) {
        log.info("分页查询产假规则，城市: {}, page: {}, size: {}", city, pageable.getPageNumber(), pageable.getPageSize());

        Page<MaternityRules> maternityRulesPage;
        if (city != null && !city.trim().isEmpty()) {
            // 按城市过滤
            maternityRulesPage = maternityRulesRepository.findByCityAndEnabled(city, true, pageable);
        } else {
            // 查询所有
            maternityRulesPage = maternityRulesRepository.findByEnabled(true, pageable);
        }
        return maternityRulesPage.map(this::convertToResponse);
    }


    @Override
    @Transactional
    public MaternityRulesResponse updateMaternityRules(Integer id, MaternityRulesRequest request) {
        log.info("更新产假规则，ID: {}, 请求参数: {}", id, request);

        MaternityRules maternityRules = maternityRulesRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("产假规则不存在，ID: " + id));

        // 查找产假类型
        MaternityLeaveType maternityLeaveType = maternityLeaveTypeRepository.findById(request.getMaternityLeaveTypeId())
                .orElseThrow(() -> new IllegalArgumentException("产假类型不存在，ID: " + request.getMaternityLeaveTypeId()));

        maternityRules.setCity(request.getCity());
        maternityRules.setMaternityLeaveType(maternityLeaveType);
        maternityRules.setLeaveDays(request.getLeaveDays());
        maternityRules.setIsExtendable(request.getIsExtendable());
        maternityRules.setHasAllowance(request.getHasAllowance());
        maternityRules.setIsDefault(request.getIsDefault());
        maternityRules.setRadioGroup(request.getRadioGroup());
        maternityRules.setEnabled(request.getEnabled());

        MaternityRules updated = maternityRulesRepository.save(maternityRules);
        log.info("产假规则更新成功，ID: {}", updated.getId());

        return convertToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteMaternityRules(Integer id) {
        log.info("逻辑删除产假规则，ID: {}", id);

        MaternityRules maternityRules = maternityRulesRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("产假规则不存在，ID: " + id));

        maternityRules.setEnabled(false);
        maternityRulesRepository.save(maternityRules);
        log.info("产假规则逻辑删除成功，ID: {}", id);
    }

    @Override
    @Transactional
    public int batchImportMaternityRules(List<Map<String, Object>> dataList) {
        log.info("开始批量导入产假规则，共 {} 条数据", dataList.size());
        
        int successCount = 0;
        int updateCount = 0;
        int skipCount = 0;
        
        for (Map<String, Object> data : dataList) {
            try {
                String city = (String) data.get("城市");
                String maternityLeaveType = (String) data.get("产假类型");
                String abortionLeaveType = (String) data.get("流产类型");
                String leaveDaysStr = (String) data.get("产假天数");
                String isExtendableStr = (String) data.get("是否遇法定节假日顺延");
                String hasAllowanceStr = (String) data.get("是否享受津贴");
                String isDefaultStr = (String) data.get("是否默认");
                
                // 验证必填字段
                if (city == null || city.trim().isEmpty() || 
                    maternityLeaveType == null || maternityLeaveType.trim().isEmpty() || 
                    leaveDaysStr == null || leaveDaysStr.trim().isEmpty()) {
                    log.warn("跳过不完整的数据行: {}", data);
                    skipCount++;
                    continue;
                }
                
                // 处理流产类型（如果是"无"则设为null）
                String finalAbortionLeaveType = null;
                if (abortionLeaveType != null && !abortionLeaveType.trim().isEmpty() 
                    && !"无".equals(abortionLeaveType.trim())) {
                    finalAbortionLeaveType = abortionLeaveType.trim();
                }
                
                // 检查是否存在相同的规则（城市+产假类型+流产类型）
                List<MaternityRules> existingRules = maternityRulesRepository.findByCityAndMaternityLeaveType(
                        city.trim(), maternityLeaveType.trim());
                
                final String searchAbortionType = finalAbortionLeaveType;
                MaternityRules maternityRules = existingRules.stream()
                        .filter(rule -> Objects.equals(rule.getAbortionLeaveType(), searchAbortionType))
                        .findFirst()
                        .orElse(null);
                
                boolean isUpdate = false;
                if (maternityRules == null) {
                    // 创建新规则
                    maternityRules = new MaternityRules();
                    maternityRules.setCity(city.trim());
                    maternityRules.setMaternityLeaveType(maternityLeaveType.trim());
                    maternityRules.setAbortionLeaveType(finalAbortionLeaveType);
                } else {
                    // 更新现有规则
                    isUpdate = true;
                }
                
                // 设置或更新字段
                maternityRules.setLeaveDays(Integer.parseInt(leaveDaysStr.trim()));
                maternityRules.setIsExtendable("是".equals(isExtendableStr));
                maternityRules.setHasAllowance("是".equals(hasAllowanceStr));
                maternityRules.setIsDefault("是".equals(isDefaultStr));
                maternityRules.setRadioGroup(0);
                maternityRules.setIsActive(true);
                
                maternityRulesRepository.save(maternityRules);
                
                if (isUpdate) {
                    updateCount++;
                    log.debug("更新产假规则: 城市={}, 产假类型={}", city.trim(), maternityLeaveType.trim());
                } else {
                    successCount++;
                    log.debug("创建产假规则: 城市={}, 产假类型={}", city.trim(), maternityLeaveType.trim());
                }
            } catch (Exception e) {
                log.error("导入数据失败: {}", data, e);
                skipCount++;
            }
        }
        
        log.info("批量导入产假规则完成，新增 {} 条，更新 {} 条，跳过 {} 条", successCount, updateCount, skipCount);
        return successCount + updateCount;
    }

    /**
     * 转换为响应DTO
     */
    private MaternityRulesResponse convertToResponse(MaternityRules maternityRules) {
        return MaternityRulesResponse.builder()
                .id(maternityRules.getId())
                .city(maternityRules.getCity())
                .maternityLeaveType(convertLeaveTypeToResponse(maternityRules.getMaternityLeaveType()))
                .leaveDays(maternityRules.getLeaveDays())
                .isExtendable(maternityRules.getIsExtendable())
                .hasAllowance(maternityRules.getHasAllowance())
                .isDefault(maternityRules.getIsDefault())
                .radioGroup(maternityRules.getRadioGroup())
                .enabled(maternityRules.getEnabled())
                .createDate(maternityRules.getCreateDate())
                .createBy(maternityRules.getCreateBy())
                .updateDate(maternityRules.getUpdateDate())
                .updateBy(maternityRules.getUpdateBy())
                .build();
    }

    /**
     * 转换产假类型为响应DTO
     */
    private MaternityLeaveTypeResponse convertLeaveTypeToResponse(MaternityLeaveType leaveType) {
        if (leaveType == null) {
            return null;
        }
        return MaternityLeaveTypeResponse.builder()
                .id(leaveType.getId())
                .code(leaveType.getCode())
                .name(leaveType.getName())
                .isAbortion(leaveType.getIsAbortion())
                .remark(leaveType.getRemark())
                .enabled(leaveType.getEnabled())
                .createDate(leaveType.getCreateDate())
                .createBy(leaveType.getCreateBy())
                .updateDate(leaveType.getUpdateDate())
                .updateBy(leaveType.getUpdateBy())
                .build();
    }
}
