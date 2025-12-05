package com.hr.maternity.repository;

import com.hr.maternity.entity.MaternityLeaveType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 产假类型数据访问接口
 */
@Repository
public interface MaternityLeaveTypeRepository extends JpaRepository<MaternityLeaveType, Integer> {

    /**
     * 根据代码查询产假类型
     * 
     * @param code 类型代码
     * @return 产假类型信息
     */
    Optional<MaternityLeaveType> findByCode(String code);

    /**
     * 分页查询启用的产假类型
     * 
     * @param enabled 是否启用
     * @param pageable 分页参数
     * @return 产假类型分页数据
     */
    Page<MaternityLeaveType> findByEnabled(Boolean enabled, Pageable pageable);

    /**
     * 查询所有启用的产假类型
     * 
     * @return 产假类型列表
     */
    List<MaternityLeaveType> findByEnabledTrue();

    /**
     * 检查代码是否存在
     * 
     * @param code 类型代码
     * @return 是否存在
     */
    boolean existsByCode(String code);
}
