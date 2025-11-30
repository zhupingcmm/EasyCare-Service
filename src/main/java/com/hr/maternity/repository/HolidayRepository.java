package com.hr.maternity.repository;

import com.hr.maternity.entity.Holiday;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

/**
 * 节假日数据访问接口
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
     * 分页查询未被逻辑删除的节假日
     * 
     * @param isActive 是否激活
     * @param pageable 分页参数
     * @return 节假日分页数据
     */
    Page<Holiday> findByIsActive(Boolean isActive, Pageable pageable);
}
