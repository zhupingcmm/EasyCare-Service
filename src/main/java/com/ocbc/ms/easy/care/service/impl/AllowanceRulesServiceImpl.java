package com.ocbc.ms.easy.care.service.impl;

import com.ocbc.ms.easy.care.dto.AllowanceRulesRequest;
import com.ocbc.ms.easy.care.dto.AllowanceRulesResponse;
import com.ocbc.ms.easy.care.entity.AllowanceRules;
import com.ocbc.ms.easy.care.entity.CityDO;
import com.ocbc.ms.easy.care.repository.AllowanceRulesRepository;
import com.ocbc.ms.easy.care.repository.CityRepository;
import com.ocbc.ms.easy.care.service.AllowanceRulesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 津贴规则服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AllowanceRulesServiceImpl implements AllowanceRulesService {

    private final AllowanceRulesRepository allowanceRulesRepository;
    private final CityRepository cityRepository;

    @Override
    @Transactional
    public AllowanceRulesResponse createAllowanceRules(AllowanceRulesRequest request) {
        log.info("开始创建津贴规则，请求参数: {}", request);

        // 通过城市代码查找城市
        CityDO cityDO = cityRepository.findByCode(request.getCityCode())
                .orElseThrow(() -> new IllegalArgumentException("城市不存在，代码: " + request.getCityCode()));

        AllowanceRules allowanceRules = new AllowanceRules();
        allowanceRules.setCity(cityDO);
        allowanceRules.setPayoutMethod(request.getPayoutMethod());
        allowanceRules.setNeedCompensation(request.getNeedCompensation());
        allowanceRules.setSalaryAdjustMonth(request.getSalaryAdjustMonth());
        allowanceRules.setSocialAdjustMonth(request.getSocialAdjustMonth());
        allowanceRules.setMonthDays(request.getMonthDays());
        allowanceRules.setEnabled(true);

        AllowanceRules saved = allowanceRulesRepository.save(allowanceRules);
        log.info("津贴规则创建成功，ID: {}", saved.getId());

        return convertToResponse(saved);
    }

    @Override
    public AllowanceRulesResponse getAllowanceRulesById(Integer id) {
        log.info("根据ID查询津贴规则，ID: {}", id);

        AllowanceRules allowanceRules = allowanceRulesRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("津贴规则不存在，ID: " + id));

        return convertToResponse(allowanceRules);
    }

    @Override
    public Page<AllowanceRulesResponse> listAllAllowanceRules(String city, Pageable pageable) {
        log.info("分页查询津贴规则，城市代码: {}, page: {}, size: {}", city, pageable.getPageNumber(), pageable.getPageSize());

        Page<AllowanceRules> allowanceRulesPage;
        if (city != null && !city.trim().isEmpty()) {
            // 通过城市代码查找城市
            CityDO cityDO = cityRepository.findByCode(city)
                    .orElseThrow(() -> new IllegalArgumentException("城市不存在，代码: " + city));
            // 按城市对象过滤
            allowanceRulesPage = allowanceRulesRepository.findByCityAndEnabledTrue(cityDO, pageable);
        } else {
            // 查询所有
            allowanceRulesPage = allowanceRulesRepository.findByEnabledTrue(pageable);
        }
        return allowanceRulesPage.map(this::convertToResponse);
    }

    @Override
    public List<AllowanceRulesResponse> listAllAllowanceRulesWithoutPage(String city) {
        log.info("查询所有津贴规则（不分页），城市代码: {}", city);

        List<AllowanceRules> allowanceRulesList;
        if (city != null && !city.trim().isEmpty()) {
            // 通过城市代码查找城市
            CityDO cityDO = cityRepository.findByCode(city)
                    .orElseThrow(() -> new IllegalArgumentException("城市不存在，代码: " + city));
            // 按城市对象过滤
            allowanceRulesList = allowanceRulesRepository.findAllByCityAndEnabledTrue(cityDO);
        } else {
            // 查询所有
            allowanceRulesList = allowanceRulesRepository.findAllByEnabledTrue();
        }
        return allowanceRulesList.stream()
                .map(this::convertToResponse)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    @Transactional
    public AllowanceRulesResponse updateAllowanceRules(Integer id, AllowanceRulesRequest request) {
        log.info("更新津贴规则，ID: {}, 请求参数: {}", id, request);

        AllowanceRules allowanceRules = allowanceRulesRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("津贴规则不存在，ID: " + id));

        // 通过城市代码查找城市
        CityDO cityDO = cityRepository.findByCode(request.getCityCode())
                .orElseThrow(() -> new IllegalArgumentException("城市不存在，代码: " + request.getCityCode()));

        allowanceRules.setCity(cityDO);
        allowanceRules.setPayoutMethod(request.getPayoutMethod());
        allowanceRules.setNeedCompensation(request.getNeedCompensation());
        allowanceRules.setSalaryAdjustMonth(request.getSalaryAdjustMonth());
        allowanceRules.setSocialAdjustMonth(request.getSocialAdjustMonth());
        allowanceRules.setMonthDays(request.getMonthDays());

        AllowanceRules updated = allowanceRulesRepository.save(allowanceRules);
        log.info("津贴规则更新成功，ID: {}", updated.getId());

        return convertToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteAllowanceRules(Integer id) {
        log.info("逻辑删除津贴规则，ID: {}", id);

        AllowanceRules allowanceRules = allowanceRulesRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("津贴规则不存在，ID: " + id));

        allowanceRules.setEnabled(false);
        allowanceRulesRepository.save(allowanceRules);
        log.info("津贴规则逻辑删除成功，ID: {}", id);
    }

    @Override
    public AllowanceRulesResponse getEnabledAllowanceRulesByCity(String cityCode) {
        log.info("根据城市代码查询启用津贴规则，城市代码: {}", cityCode);
        CityDO cityDO = cityRepository.findByCode(cityCode)
                .orElseThrow(() -> new IllegalArgumentException("城市不存在，代码: " + cityCode));
        AllowanceRules allowanceRules = allowanceRulesRepository.findByCityAndEnabledTrue(cityDO)
                .orElseThrow(() -> new IllegalArgumentException("该城市无启用的津贴规则: " + cityCode));
        return convertToResponse(allowanceRules);
    }

    @Override
    @Transactional
    public int batchImportAllowanceRules(List<Map<String, Object>> dataList) {
        log.info("开始批量导入津贴规则，共 {} 条数据", dataList.size());
        
        int successCount = 0;
        for (Map<String, Object> data : dataList) {
            try {
                String cityCode = (String) data.get("城市代码");
                Integer payoutMethod = (Integer) data.get("津贴发放方式");
                
                // 验证必填字段
                if (cityCode == null || cityCode.trim().isEmpty() || 
                    payoutMethod == null) {
                    log.warn("跳过不完整的数据行: {}", data);
                    continue;
                }
                
                // 查找城市
                CityDO cityDO = cityRepository.findByCode(cityCode.trim())
                        .orElseThrow(() -> new IllegalArgumentException("城市不存在，代码: " + cityCode.trim()));
                
                // 检查该城市是否已存在激活的规则
                allowanceRulesRepository.findByCityAndEnabledTrue(cityDO)
                    .ifPresent(existing -> {
                        // 如果存在，先逻辑删除旧规则
                        existing.setEnabled(false);
                        allowanceRulesRepository.save(existing);
                        log.info("城市 {} 已存在规则，已逻辑删除旧规则", cityCode.trim());
                    });
                
                // 创建新的津贴规则
                AllowanceRules allowanceRules = new AllowanceRules();
                allowanceRules.setCity(cityDO);
                allowanceRules.setPayoutMethod(payoutMethod);
                allowanceRules.setEnabled(true);
                
                allowanceRulesRepository.save(allowanceRules);
                successCount++;
            } catch (Exception e) {
                log.error("导入数据失败: {}", data, e);
            }
        }
        
        log.info("批量导入津贴规则完成，成功 {} 条", successCount);
        return successCount;
    }

    /**
     * 转换为响应DTO
     */
    private AllowanceRulesResponse convertToResponse(AllowanceRules allowanceRules) {
        CityDO city = allowanceRules.getCity();
        String cityName = city != null ? city.getChineseName() : null;
        String cityCode = city != null ? city.getCode() : null;

        return AllowanceRulesResponse.builder()
                .id(allowanceRules.getId())
                .city(cityName)
                .cityCode(cityCode)
                .payoutMethod(allowanceRules.getPayoutMethod())
                .needCompensation(allowanceRules.getNeedCompensation())
                .enabled(allowanceRules.getEnabled())
                .salaryAdjustMonth(allowanceRules.getSalaryAdjustMonth())
                .socialAdjustMonth(allowanceRules.getSocialAdjustMonth())
                .monthDays(allowanceRules.getMonthDays())
                .createDate(allowanceRules.getCreateDate())
                .createBy(allowanceRules.getCreateBy())
                .updateDate(allowanceRules.getUpdateDate())
                .updateBy(allowanceRules.getUpdateBy())
                .build();
    }
}
