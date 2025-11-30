package com.hr.maternity.repository;

import com.hr.maternity.entity.MaternityRules;
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
     * 根据城市和产假类型查询产假规则
     */
    List<MaternityRules> findByCityAndMaternityLeaveType(String city, String maternityLeaveType);
}
