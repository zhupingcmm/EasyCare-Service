package com.easy.care.service.impl;

import com.easy.care.dto.*;
import com.easy.care.dto.*;
import com.easy.care.entity.CityDO;
import com.easy.care.entity.MaternityLeaveType;
import com.easy.care.entity.MaternityRules;
import com.easy.care.enums.DystociaLeaveEnum;
import com.easy.care.enums.MiscarriageLeaveEnum;
import com.easy.care.repository.CityRepository;
import com.easy.care.repository.MaternityLeaveTypeRepository;
import com.easy.care.repository.MaternityRulesRepository;
import com.easy.care.service.MaternityRulesService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
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
    private final CityRepository cityRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public MaternityRulesResponse createMaternityRules(MaternityRulesRequest request) {
        log.info("开始创建产假规则，请求参数: {}", request);

        // 查找城市
        CityDO city = cityRepository.findByCode(request.getCity())
                .orElseThrow(() -> new IllegalArgumentException("城市不存在，代码: " + request.getCity()));

        // 查找产假类型
        MaternityLeaveType maternityLeaveType = maternityLeaveTypeRepository.findByCode(request.getMaternityLeaveTypeCode())
                .orElseThrow(() -> new IllegalArgumentException("产假类型不存在，代码: " + request.getMaternityLeaveTypeCode()));

        MaternityRules maternityRules = new MaternityRules();
        maternityRules.setCity(city);
        maternityRules.setMaternityLeaveType(maternityLeaveType);
        maternityRules.setDefaultDays(request.getDefaultDays());
        maternityRules.setDoctorRecommendDays(request.getDoctorRecommendDays());
        
        // 将 maternityLeaveExt 序列化为 JSON 字符串
        String maternityLeaveExtJson = convertToJsonString(request.getMaternityLeaveExt());
        maternityRules.setMaternityLeaveExt(maternityLeaveExtJson);
        
        maternityRules.setHolidayExtend(request.getHolidayExtend());
        maternityRules.setHasAllowance(request.getHasAllowance());
        maternityRules.setPlanAllowanceDay(request.getPlanAllowanceDay());
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
            // 按城市代码过滤，需要先查找城市ID
            CityDO cityDO = cityRepository.findByCode(city)
                    .orElseThrow(() -> new IllegalArgumentException("城市不存在，代码: " + city));
            maternityRulesPage = maternityRulesRepository.findByCityIdAndEnabled(cityDO.getId(), true, pageable);
        } else {
            // 查询所有
            maternityRulesPage = maternityRulesRepository.findByEnabled(true, pageable);
        }
        return maternityRulesPage.map(this::convertToResponse);
    }

    @Override
    public List<MaternityRulesResponse> listAllMaternityRulesWithoutPage(String city) {
        log.info("查询所有产假规则（不分页），城市: {}", city);

        List<MaternityRules> maternityRulesList;
        if (city != null && !city.trim().isEmpty()) {
            // 按城市代码过滤，需要先查找城市ID
            CityDO cityDO = cityRepository.findByCode(city)
                    .orElseThrow(() -> new IllegalArgumentException("城市不存在，代码: " + city));
            maternityRulesList = maternityRulesRepository.findByCityIdAndEnabled(cityDO.getId(), true);
        } else {
            // 查询所有
            maternityRulesList = maternityRulesRepository.findByEnabled(true);
        }
        return maternityRulesList.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }


    @Override
    @Transactional
    public MaternityRulesResponse updateMaternityRules(Integer id, MaternityRulesRequest request) {
        log.info("更新产假规则，ID: {}, 请求参数: {}", id, request);

        MaternityRules maternityRules = maternityRulesRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("产假规则不存在，ID: " + id));

        // 查找城市
        CityDO city = cityRepository.findByCode(request.getCity())
                .orElseThrow(() -> new IllegalArgumentException("城市不存在，代码: " + request.getCity()));

        // 查找产假类型
        MaternityLeaveType maternityLeaveType = maternityLeaveTypeRepository.findByCode(request.getMaternityLeaveTypeCode())
                .orElseThrow(() -> new IllegalArgumentException("产假类型不存在，代码: " + request.getMaternityLeaveTypeCode()));

        maternityRules.setCity(city);
        maternityRules.setMaternityLeaveType(maternityLeaveType);
        maternityRules.setDefaultDays(request.getDefaultDays());
        maternityRules.setDoctorRecommendDays(request.getDoctorRecommendDays());
        
        // 将 maternityLeaveExt 序列化为 JSON 字符串
        String maternityLeaveExtJson = convertToJsonString(request.getMaternityLeaveExt());
        maternityRules.setMaternityLeaveExt(maternityLeaveExtJson);
        
        maternityRules.setHolidayExtend(request.getHolidayExtend());
        maternityRules.setHasAllowance(request.getHasAllowance());
        maternityRules.setPlanAllowanceDay(request.getPlanAllowanceDay());
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
        return 1;
    }

    @Override
    public List<MaternityPolicyResponse> findMaternityPolicyByCityCode(String cityCode) {
        log.info("根据城市代码查询产假政策，城市代码: {}", cityCode);

        if (cityCode == null || cityCode.trim().isEmpty()) {
            throw new IllegalArgumentException("城市代码不能为空");
        }

        // 根据城市代码查找城市
        CityDO city = cityRepository.findByCodeAndEnabledTrue(cityCode)
                .orElseThrow(() -> new IllegalArgumentException("城市不存在或未启用，代码: " + cityCode));

        // 查询该城市的所有启用的产假规则
        List<MaternityRules> maternityRulesList = maternityRulesRepository.findByCityIdAndEnabled(city.getId(), true);

        log.info("查询到 {} 条产假规则，城市: {}", maternityRulesList.size(), city.getChineseName());

        // 转换为键值对格式
        return convertToMaternityPolicyResponse(maternityRulesList);
    }

    /**
     * 将产假规则列表转换为产假政策键值对格式
     */
    private List<MaternityPolicyResponse> convertToMaternityPolicyResponse(List<MaternityRules> rulesList) {
        List<MaternityPolicyResponse> responses = new ArrayList<>();
        
        // 按产假类型分组
        Map<String, List<MaternityRules>> groupedRules = rulesList.stream()
                .collect(Collectors.groupingBy(rule -> rule.getMaternityLeaveType().getCode()));

        // 1. 法定产假 (baseDay) - code: 1001
        List<MaternityRules> baseRules = groupedRules.get("1001");
        if (baseRules != null && !baseRules.isEmpty()) {
            MaternityRules baseRule = baseRules.get(0);
            responses.add(MaternityPolicyResponse.builder()
                    .key("baseDay")
                    .value(String.valueOf(baseRule.getDefaultDays()))
                    .order(1)
                    .build());
        }

        // 2. 奖励假 (awardDay) - code: 1004
        List<MaternityRules> awardRules = groupedRules.get("1004");
        if (awardRules != null && !awardRules.isEmpty()) {
            List<MaternityPolicyDetailDTO> awardDetails = extractDetailsFromRules(awardRules);

            if (!awardDetails.isEmpty()) {
                Map<String, Object> awardExt = new HashMap<>();
                awardExt.put("detail", awardDetails);

                responses.add(MaternityPolicyResponse.builder()
                        .key("awardDay")
                        .order(2)
                        .ext(awardExt)
                        .build());
            }
        }

        // 3. 难产假 (dystociaDay) - code: 1002
        List<MaternityRules> dystociaRules = groupedRules.get("1002");
        if (dystociaRules != null && !dystociaRules.isEmpty()) {
            List<MaternityPolicyDetailDTO> dystociaDetails = extractDetailsFromRules(dystociaRules);

            if (!dystociaDetails.isEmpty()) {
                Map<String, Object> dystociaExt = new HashMap<>();
                dystociaExt.put("detail", dystociaDetails);

                responses.add(MaternityPolicyResponse.builder()
                        .key("dystociaDay")
                        .order(3)
                        .ext(dystociaExt)
                        .build());
            }
        }

        // 4. 多胞胎产假 (multipleDay) - code: 1003
        List<MaternityRules> multipleRules = groupedRules.get("1003");
        if (multipleRules != null && !multipleRules.isEmpty()) {
            MaternityRules multipleRule = multipleRules.get(0);
            responses.add(MaternityPolicyResponse.builder()
                    .key("multipleDay")
                    .value(String.valueOf(multipleRule.getDefaultDays()))
                    .order(4)
                    .build());
        }

        // 5. 奖励假是否有津贴 (awardDayHasAllowance)
        if (awardRules != null && !awardRules.isEmpty()) {
            MaternityRules awardRule = awardRules.get(0);
            responses.add(MaternityPolicyResponse.builder()
                    .key("awardDayHasAllowance")
                    .value(awardRule.getHasAllowance())
                    .order(5)
                    .build());
        }

        // 6. 产假是否顺延 (holidayExtend)
        if (awardRules != null && !awardRules.isEmpty()) {
            MaternityRules awardRule = awardRules.get(0);
            responses.add(MaternityPolicyResponse.builder()
                    .key("holidayExtend")
                    .value(awardRule.getHolidayExtend())
                    .order(6)
                    .build());
        }

        // 按order排序
        responses.sort(Comparator.comparing(MaternityPolicyResponse::getOrder));

        return responses;
    }

    @Override
    public DystociaMiscarriageResponse queryDystociaMiscarriageByCityCode(String cityCode) {
        log.info("根据城市代码查询难产和流产假信息，城市代码: {}", cityCode);

        if (cityCode == null || cityCode.trim().isEmpty()) {
            throw new IllegalArgumentException("城市代码不能为空");
        }

        // 根据城市代码查找城市
        CityDO city = cityRepository.findByCodeAndEnabledTrue(cityCode)
                .orElseThrow(() -> new IllegalArgumentException("城市不存在或未启用，代码: " + cityCode));

        // 查询该城市的所有启用的产假规则
        List<MaternityRules> maternityRulesList = maternityRulesRepository.findByCityIdAndEnabled(city.getId(), true);

        // 按产假类型分组
        Map<String, List<MaternityRules>> groupedRules = maternityRulesList.stream()
                .collect(Collectors.groupingBy(rule -> rule.getMaternityLeaveType().getCode()));

        // 构建流产假列表 (code: 1005)
        List<DystociaMiscarriageItemDTO> misCarriageList = extractMiscarriageList(groupedRules.get("1005"));

        // 构建难产假列表 (code: 1002)
        List<DystociaMiscarriageItemDTO> dysList = extractDystociaList(groupedRules.get("1002"));

        log.info("查询到流产假 {} 条，难产假 {} 条", misCarriageList.size(), dysList.size());

        return DystociaMiscarriageResponse.builder()
                .misCarriage(misCarriageList)
                .dys(dysList)
                .build();
    }

    /**
     * 从规则的 maternityLeaveExt JSONArray 中提取详情列表
     * JSONArray 格式: [{"code":"awd_001", "days":30}]
     */
    private List<MaternityPolicyDetailDTO> extractDetailsFromRules(List<MaternityRules> rules) {
        List<MaternityPolicyDetailDTO> details = new ArrayList<>();
        
        if (rules == null || rules.isEmpty()) {
            return details;
        }

        for (MaternityRules rule : rules) {
            Object ext = rule.getMaternityLeaveExt();
            if (ext != null && ext instanceof List) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> extList = (List<Map<String, Object>>) ext;
                
                for (Map<String, Object> item : extList) {
                    String code = item.get("code") != null ? item.get("code").toString() : null;
                    Object daysObj = item.get("days");
                    String value = daysObj != null ? daysObj.toString() : null;
                    String desc = item.get("desc") != null ? item.get("desc").toString() : null;
                    
                    if (code != null) {
                        details.add(MaternityPolicyDetailDTO.builder()
                                .code(code)
                                .value(value)
                                .desc(desc)
                                .build());
                    }
                }
            }
        }

        return details;
    }

    /**
     * 从流产假规则中提取列表数据
     * 从 JSONArray 中获取 code，然后通过 MiscarriageLeaveEnum 获取对应的 name
     */
    private List<DystociaMiscarriageItemDTO> extractMiscarriageList(List<MaternityRules> miscarriageRules) {
        List<DystociaMiscarriageItemDTO> misCarriageList = new ArrayList<>();
        
        if (miscarriageRules == null || miscarriageRules.isEmpty()) {
            return misCarriageList;
        }

        for (MaternityRules rule : miscarriageRules) {
            String ext = rule.getMaternityLeaveExt();
            
            // 先判断 ext 不为 null
            if (ext == null || ext.trim().isEmpty()) {
                continue;
            }
            
            try {
                // 将 JSON 字符串反序列化为 List
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> extList = objectMapper.readValue(ext, List.class);
                
                for (Map<String, Object> item : extList) {
                    String code = item.get("code") != null ? item.get("code").toString() : null;
                    
                    if (code != null) {
                        // 通过枚举获取对应的名称
                        MiscarriageLeaveEnum enumValue = MiscarriageLeaveEnum.getByCode(code);
                        if (enumValue != null) {
                            misCarriageList.add(DystociaMiscarriageItemDTO.builder()
                                    .code(code)
                                    .name(enumValue.getName())
                                    .build());
                        }
                    }
                }
            } catch (JsonProcessingException e) {
                log.error("解析流产假扩展信息失败，规则ID: {}, JSON: {}", rule.getId(), ext, e);
            }
        }

        return misCarriageList;
    }

    /**
     * 从难产假规则中提取列表数据
     * 从 JSONArray 中获取 code，然后通过 DystociaLeaveEnum 获取对应的 name
     */
    private List<DystociaMiscarriageItemDTO> extractDystociaList(List<MaternityRules> dystociaRules) {
        List<DystociaMiscarriageItemDTO> dysList = new ArrayList<>();
        
        if (dystociaRules == null || dystociaRules.isEmpty()) {
            return dysList;
        }

        for (MaternityRules rule : dystociaRules) {
            String ext = rule.getMaternityLeaveExt();
            
            // 先判断 ext 不为 null
            if (ext == null || ext.trim().isEmpty()) {
                continue;
            }
            
            try {
                // 将 JSON 字符串反序列化为 List
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> extList = objectMapper.readValue(ext, List.class);
                
                for (Map<String, Object> item : extList) {
                    String code = item.get("code") != null ? item.get("code").toString() : null;
                    
                    if (code != null) {
                        // 通过枚举获取对应的名称
                        DystociaLeaveEnum enumValue = DystociaLeaveEnum.getByCode(code);
                        if (enumValue != null) {
                            dysList.add(DystociaMiscarriageItemDTO.builder()
                                    .code(code)
                                    .name(enumValue.getName())
                                    .build());
                        }
                    }
                }
            } catch (JsonProcessingException e) {
                log.error("解析难产假扩展信息失败，规则ID: {}, JSON: {}", rule.getId(), ext, e);
            }
        }

        return dysList;
    }

    /**
     * 转换为响应DTO
     */
    private MaternityRulesResponse convertToResponse(MaternityRules maternityRules) {
        CityDO city = maternityRules.getCity();
        MaternityRulesResponse build = MaternityRulesResponse.builder()
                .id(maternityRules.getId())
                .cityId(city != null ? city.getId() : null)
                .cityCode(city != null ? city.getCode() : null)
                .cityName(city != null ? city.getChineseName() : null)
                .maternityLeaveType(convertLeaveTypeToResponse(maternityRules.getMaternityLeaveType()))
                .defaultDays(maternityRules.getDefaultDays())
                .doctorRecommendDays(maternityRules.getDoctorRecommendDays())
                .maternityLeaveExt(maternityRules.getMaternityLeaveExt())
                .holidayExtend(maternityRules.getHolidayExtend())
                .hasAllowance(maternityRules.getHasAllowance())
                .planAllowanceDay(maternityRules.getPlanAllowanceDay())
                .enabled(maternityRules.getEnabled())
                .createDate(maternityRules.getCreateDate())
                .createBy(maternityRules.getCreateBy())
                .updateDate(maternityRules.getUpdateDate())
                .updateBy(maternityRules.getUpdateBy())
                .build();
        return build;
    }

    /**
     * 将对象转换为 JSON 字符串
     */
    private String convertToJsonString(Object obj) {
        if (obj == null) {
            return null;
        }
        
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("转换为 JSON 字符串失败: {}", obj, e);
            throw new IllegalArgumentException("JSON 序列化失败: " + e.getMessage());
        }
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
                .remark(leaveType.getRemark())
                .enabled(leaveType.getEnabled())
                .createDate(leaveType.getCreateDate())
                .createBy(leaveType.getCreateBy())
                .updateDate(leaveType.getUpdateDate())
                .updateBy(leaveType.getUpdateBy())
                .build();
    }
}
