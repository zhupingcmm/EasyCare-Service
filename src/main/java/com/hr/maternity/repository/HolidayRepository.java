package com.hr.maternity.repository;

import com.hr.maternity.entity.Holiday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 节假日数据访问接口
 */
@Repository
public interface HolidayRepository extends JpaRepository<Holiday, UUID> {

    /**
     * 根据年份和地区查询节假日列表
     * 
     * @param year 年份
     * @param region 地区代码
     * @return 节假日列表
     */
    List<Holiday> findByYearAndRegionOrderByDate(Integer year, String region);

    /**
     * 根据年份查询节假日列表（默认中国地区）
     * 
     * @param year 年份
     * @return 节假日列表
     */
    @Query("SELECT h FROM Holiday h WHERE h.year = :year AND h.region = 'CN' ORDER BY h.date")
    List<Holiday> findByYearOrderByDate(@Param("year") Integer year);

    /**
     * 检查指定年份和地区是否存在节假日数据
     * 
     * @param year 年份
     * @param region 地区代码
     * @return 是否存在数据
     */
    boolean existsByYearAndRegion(Integer year, String region);

    /**
     * 根据年份、地区和日期查询节假日
     * 
     * @param year 年份
     * @param region 地区代码
     * @param date 日期字符串 (YYYY-MM-DD)
     * @return 节假日信息
     */
    @Query("SELECT h FROM Holiday h WHERE h.year = :year AND h.region = :region AND TO_CHAR(h.date, 'YYYY-MM-DD') = :date")
    Optional<Holiday> findByYearAndRegionAndDateString(@Param("year") Integer year, 
                                                      @Param("region") String region, 
                                                      @Param("date") String date);

    /**
     * 删除指定年份和地区的所有节假日数据
     * 
     * @param year 年份
     * @param region 地区代码
     */
    void deleteByYearAndRegion(Integer year, String region);

    /**
     * 统计指定年份和地区的节假日数量
     * 
     * @param year 年份
     * @param region 地区代码
     * @return 节假日数量
     */
    long countByYearAndRegion(Integer year, String region);
}
