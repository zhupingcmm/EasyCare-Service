package com.ocbc.ms.easy.care.repository;

import com.ocbc.ms.easy.care.entity.MaternityAllowanceResultDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 津贴计算结果数据访问层
 */
@Repository
public interface MaternityAllowanceResultRepository extends JpaRepository<MaternityAllowanceResultDO, Long> {



    List<MaternityAllowanceResultDO> findByLanId(String lanId);

    List<MaternityAllowanceResultDO> findByCityCode(String cityCode);
}
