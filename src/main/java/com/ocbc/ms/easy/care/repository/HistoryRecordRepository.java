package com.ocbc.ms.easy.care.repository;

import com.ocbc.ms.easy.care.entity.HistoryRecordDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 历史记录 Repository（对应 t_history）
 */
@Repository
public interface HistoryRecordRepository extends JpaRepository<HistoryRecordDO, Long> {

    Optional<HistoryRecordDO> findByHrIdAndEmployeeId(String hrId, String employeeId);

    Optional<HistoryRecordDO> findByHrIdAndEmployeeIdAndStartDate(String hrId, String employeeId, LocalDate startDate);

    List<HistoryRecordDO> findByHrIdOrderByCreatedTimeDesc(String hrId);

    List<HistoryRecordDO> findByHrIdAndEmployeeIdInOrderByCreatedTimeDesc(String hrId, List<String> employeeIds);

    @Modifying
    @Query("delete from HistoryRecordDO r where r.hrId = :hrId and r.employeeId in :employeeIds")
    int deleteByHrIdAndEmployeeIdIn(@Param("hrId") String hrId,
                                    @Param("employeeIds") List<String> employeeIds);
}
