package com.hr.maternity.repository;

import com.hr.maternity.entity.MaternityRules;
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
     * 根据城市查询产假规则
     */
    List<MaternityRules> findByCity(String city);

    /**
     * 分页查询启用状态的产假规则
     */
    Page<MaternityRules> findByEnabled(Boolean enabled, Pageable pageable);

    /**
     * 根据城市和启用状态分页查询产假规则
     */
    Page<MaternityRules> findByCityAndEnabled(String city, Boolean enabled, Pageable pageable);
}
