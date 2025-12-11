package com.easy.care.service;

import com.easy.care.dto.DystociaMiscarriageResponse;
import com.easy.care.dto.MaternityPolicyResponse;
import com.easy.care.dto.MaternityRulesRequest;
import com.easy.care.dto.MaternityRulesResponse;
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
     * 查询所有产假规则（不分页）
     * @param city 城市名称（可选，为null则查询所有）
     */
    List<MaternityRulesResponse> listAllMaternityRulesWithoutPage(String city);




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

    /**
     * 根据城市代码查询产假政策
     * @param cityCode 城市代码
     * @return 产假政策键值对列表
     */
    List<MaternityPolicyResponse> findMaternityPolicyByCityCode(String cityCode);

    /**
     * 根据城市代码查询难产和流产假信息
     * @param cityCode 城市代码
     * @return 难产和流产假信息
     */
    DystociaMiscarriageResponse queryDystociaMiscarriageByCityCode(String cityCode);
}
