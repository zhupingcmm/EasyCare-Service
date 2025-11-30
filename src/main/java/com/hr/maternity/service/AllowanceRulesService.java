package com.hr.maternity.service;

import com.hr.maternity.dto.AllowanceRulesRequest;
import com.hr.maternity.dto.AllowanceRulesResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

/**
 * 津贴规则服务接口
 */
public interface AllowanceRulesService {

    /**
     * 创建津贴规则
     */
    AllowanceRulesResponse createAllowanceRules(AllowanceRulesRequest request);

    /**
     * 根据ID查询津贴规则
     */
    AllowanceRulesResponse getAllowanceRulesById(Integer id);

    /**
     * 分页查询所有津贴规则
     */
    Page<AllowanceRulesResponse> listAllAllowanceRules(Pageable pageable);

    /**
     * 更新津贴规则
     */
    AllowanceRulesResponse updateAllowanceRules(Integer id, AllowanceRulesRequest request);

    /**
     * 删除津贴规则（逻辑删除）
     */
    void deleteAllowanceRules(Integer id);

    /**
     * 批量导入津贴规则
     */
    int batchImportAllowanceRules(List<Map<String, Object>> dataList);
}
