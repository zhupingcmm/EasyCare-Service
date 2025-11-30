package com.hr.maternity.repository;

import com.hr.maternity.entity.AllowanceRules;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 津贴规则数据访问层
 */
@Repository
public interface AllowanceRulesRepository extends JpaRepository<AllowanceRules, Integer> {

    /**
     * 根据城市查询津贴规则
     */
    Optional<AllowanceRules> findByCityAndIsActiveTrue(String city);

    /**
     * 分页查询所有激活的津贴规则
     */
    Page<AllowanceRules> findByIsActiveTrue(Pageable pageable);
}
