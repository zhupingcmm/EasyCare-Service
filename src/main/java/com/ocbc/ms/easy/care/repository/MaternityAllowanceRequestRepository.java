package com.ocbc.ms.easy.care.repository;

import com.ocbc.ms.easy.care.entity.MaternityAllowanceRequestDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * 津贴申请记录数据访问层
 */
@Repository
public interface MaternityAllowanceRequestRepository extends JpaRepository<MaternityAllowanceRequestDO, Long> {

    List<MaternityAllowanceRequestDO> findByLanId(String lanId);

    List<MaternityAllowanceRequestDO> findByCityCode(String cityCode);


    List<MaternityAllowanceRequestDO> findByMaternityLeaveStartDateBetween(LocalDate startDate, LocalDate endDate);
}
