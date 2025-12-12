package com.ocbc.ms.easy.care.repository;

import com.ocbc.ms.easy.care.entity.MaternityLeaveResultDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * 产假计算结果数据访问层
 */
@Repository
public interface MaternityLeaveResultRepository extends JpaRepository<MaternityLeaveResultDO, Long> {



    List<MaternityLeaveResultDO> findByLanId(String lanId);

    List<MaternityLeaveResultDO> findByCityCode(String cityCode);

    List<MaternityLeaveResultDO> findByStartDateBetween(LocalDate startDate, LocalDate endDate);
}
