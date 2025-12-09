package com.hr.maternity.repository;

import com.hr.maternity.entity.Holiday;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 特殊日期数据访问接口（节假日/补班）
 */
@Repository
public interface HolidayRepository extends JpaRepository<Holiday, UUID> {

    /**
     * 根据日期查询节假日
     * 
     * @param date 日期
     * @return 节假日信息
     */
    Optional<Holiday> findByDate(LocalDate date);

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
}
