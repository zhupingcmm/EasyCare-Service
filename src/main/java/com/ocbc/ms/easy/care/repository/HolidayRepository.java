package com.ocbc.ms.easy.care.repository;

import com.ocbc.ms.easy.care.entity.Holiday;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 特殊日期数据访问接口（节假日/补班）
 */
@Repository
public interface HolidayRepository extends JpaRepository<Holiday, Integer> {

    /**
     * 根据日期查询节假日
     * 
     * @param date 日期
     * @return 节假日信息
     */
    Optional<Holiday> findByDate(LocalDate date);

    /**
     * 根据日期和启用状态查询节假日
     * 
     * @param date 日期
     * @param enabled 是否启用
     * @return 节假日信息
     */
    Optional<Holiday> findByDateAndEnabled(LocalDate date, Boolean enabled);

    /**
     * 根据年份与区域查询节假日，按日期排序
     */
    List<Holiday> findByYearAndRegionOrderByDate(Integer year, String region);

    /**
     * 分页查询启用的特殊日期
     * 
     * @param enabled 是否启用
     * @param pageable 分页参数
     * @return 特殊日期分页数据
     */
    Page<Holiday> findByEnabled(Boolean enabled, Pageable pageable);
    
    /**
     * 按日期范围查询节假日
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 节假日列表
     */
    List<Holiday> findByDateBetweenOrderByDate(LocalDate startDate, LocalDate endDate);
}
