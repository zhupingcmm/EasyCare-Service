package com.hr.maternity.service;

import com.hr.maternity.dto.MaternityRulesRequest;
import com.hr.maternity.dto.MaternityRulesResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

/**
 * 产假规则服务接口
 */
public interface MaternityRulesService {

    /**
     * 创建产假规则
     */
    MaternityRulesResponse createMaternityRules(MaternityRulesRequest request);


    /**
     * 查询所有产假规则（分页）
     * @param city 城市名称（可选，为null则查询所有）
     * @param pageable 分页参数
     */
    Page<MaternityRulesResponse> listAllMaternityRules(String city, Pageable pageable);




    /**
     * 更新产假规则
     */
    MaternityRulesResponse updateMaternityRules(Integer id, MaternityRulesRequest request);

    /**
     * 删除产假规则
     */
    void deleteMaternityRules(Integer id);

    /**
     * 批量导入产假规则
     */
    int batchImportMaternityRules(List<Map<String, Object>> dataList);
}
