package com.hr.maternity.repository;

import com.hr.maternity.entity.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 城市数据访问层
 */
@Repository
public interface CityRepository extends JpaRepository<City, UUID> {

    /**
     * 根据城市代码查找城市
     * @param code 城市代码
     * @return 城市信息
     */
    Optional<City> findByCode(String code);

    /**
     * 根据城市名称查找城市
     * @param name 城市名称
     * @return 城市信息
     */
    Optional<City> findByName(String name);

    /**
     * 根据中文名称查找城市
     * @param chineseName 中文名称
     * @return 城市信息
     */
    Optional<City> findByChineseName(String chineseName);

    /**
     * 查找所有启用的城市
     * @return 启用的城市列表
     */
    List<City> findByEnabledTrueOrderBySortOrder();

    /**
     * 根据省份查找城市
     * @param province 省份
     * @return 城市列表
     */
    List<City> findByProvinceAndEnabledTrueOrderBySortOrder(String province);

    /**
     * 根据国家代码查找城市
     * @param countryCode 国家代码
     * @return 城市列表
     */
    List<City> findByCountryCodeAndEnabledTrueOrderBySortOrder(String countryCode);

    /**
     * 检查城市代码是否存在
     * @param code 城市代码
     * @return 是否存在
     */
    boolean existsByCode(String code);

    /**
     * 检查城市名称是否存在
     * @param name 城市名称
     * @return 是否存在
     */
    boolean existsByName(String name);

    /**
     * 根据关键字搜索城市（名称或代码）
     * @param keyword 关键字
     * @return 城市列表
     */
    @Query("SELECT c FROM City c WHERE c.enabled = true AND " +
           "(LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.chineseName) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY c.sortOrder")
    List<City> searchByKeyword(@Param("keyword") String keyword);
}
