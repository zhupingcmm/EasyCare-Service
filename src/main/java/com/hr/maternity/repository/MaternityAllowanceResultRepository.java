package com.hr.maternity.repository;

import com.hr.maternity.entity.MaternityAllowanceResultDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 津贴计算结果数据访问层
 */
@Repository
public interface MaternityAllowanceResultRepository extends JpaRepository<MaternityAllowanceResultDO, Long> {

    List<MaternityAllowanceResultDO> findByAllowanceRequestId(Long allowanceRequestId);

    Optional<MaternityAllowanceResultDO> findFirstByAllowanceRequestIdOrderByCreateDateDesc(Long allowanceRequestId);

    List<MaternityAllowanceResultDO> findByLanId(String lanId);

    List<MaternityAllowanceResultDO> findByCityCode(String cityCode);
}
