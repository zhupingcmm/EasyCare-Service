package com.easy.care.repository;

import com.easy.care.entity.MaternityAllowanceResultDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 津贴计算结果数据访问层
 */
@Repository
public interface MaternityAllowanceResultRepository extends JpaRepository<MaternityAllowanceResultDO, Long> {



    List<MaternityAllowanceResultDO> findByLanId(String lanId);

    List<MaternityAllowanceResultDO> findByCityCode(String cityCode);
}
