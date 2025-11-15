package com.hr.maternity.repository;

import com.hr.maternity.entity.MaternityLeaveRequestDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * 产假申请记录数据访问层
 */
@Repository
public interface MaternityLeaveRequestRepository extends JpaRepository<MaternityLeaveRequestDO, Long> {

    List<MaternityLeaveRequestDO> findByLanId(String lanId);

    List<MaternityLeaveRequestDO> findByCityCode(String cityCode);

    List<MaternityLeaveRequestDO> findByLanIdAndCityCode(String lanId, String cityCode);

    List<MaternityLeaveRequestDO> findByExpectedDeliveryDateBetween(LocalDate startDate, LocalDate endDate);
}
