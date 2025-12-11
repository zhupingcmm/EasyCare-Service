package com.easy.care.repository;

import com.easy.care.entity.CityDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 城市数据访问层
 */
@Repository
public interface CityRepository extends JpaRepository<CityDO, UUID> {

    /**
     * 根据城市代码查找城市
     * @param code 城市代码
     * @return 城市信息
     */
    Optional<CityDO> findByCode(String code);

    /**
     * 根据城市代码查询启用城市
     * @param code 城市代码
     * @return 启用状态的城市信息
     */
    Optional<CityDO> findByCodeAndEnabledTrue(String code);


    /**
     * 查找所有启用的城市
     * @return 启用的城市列表
     */
    List<CityDO> findByEnabledTrueOrderBySortOrder();


}
