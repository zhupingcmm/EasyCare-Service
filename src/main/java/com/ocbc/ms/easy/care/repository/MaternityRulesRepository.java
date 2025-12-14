package com.ocbc.ms.easy.care.repository;

import com.ocbc.ms.easy.care.entity.MaternityRules;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 产假规则数据访问层
 */
@Repository
public interface MaternityRulesRepository extends JpaRepository<MaternityRules, Integer> {

    /**
     * 根据城市ID查询产假规则
     */
    List<MaternityRules> findByCityId(Integer cityId);

    /**
     * 分页查询启用状态的产假规则
     */
    Page<MaternityRules> findByEnabled(Boolean enabled, Pageable pageable);

    /**
     * 根据城市ID和启用状态分页查询产假规则
     */
    Page<MaternityRules> findByCityIdAndEnabled(Integer cityId, Boolean enabled, Pageable pageable);

    /**
     * 根据城市ID和启用状态查询产假规则列表
     */
    List<MaternityRules> findByCityIdAndEnabled(Integer cityId, Boolean enabled);

    /**
     * 根据启用状态查询产假规则列表
     */
    List<MaternityRules> findByEnabled(Boolean enabled);
}
