package com.hr.maternity.repository;

import com.hr.maternity.entity.MaternityLeaveResultDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 产假计算结果数据访问层
 */
@Repository
public interface MaternityLeaveResultRepository extends JpaRepository<MaternityLeaveResultDO, Long> {



    List<MaternityLeaveResultDO> findByLanId(String lanId);

    List<MaternityLeaveResultDO> findByCityCode(String cityCode);

    List<MaternityLeaveResultDO> findByStartDateBetween(LocalDate startDate, LocalDate endDate);
}
