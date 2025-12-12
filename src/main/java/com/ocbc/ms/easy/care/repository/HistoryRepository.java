package com.ocbc.ms.easy.care.repository;

import com.ocbc.ms.easy.care.entity.HistoryDO;
import com.ocbc.ms.easy.care.enums.RecordTypeEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 历史记录数据访问层
 */
@Repository
public interface HistoryRepository extends JpaRepository<HistoryDO, Long> {

    /**
     * 根据员工工号查询历史记录（分页）
     * @param lanId 员工工号
     * @param pageable 分页参数
     * @return 分页历史记录
     */
    Page<HistoryDO> findByLanId(String lanId, Pageable pageable);
    
    /**
     * 根据员工工号查询历史记录（不分页）
     * @param lanId 员工工号
     * @return 历史记录列表
     */
    List<HistoryDO> findByLanId(String lanId);

    /**
     * 根据产假申请ID查询历史记录
     * @param maternityLeaveRequestId 产假申请ID
     * @return 历史记录列表
     */
    List<HistoryDO> findByMaternityLeaveRequestId(Long maternityLeaveRequestId);

    /**
     * 根据产假结果ID查询历史记录
     * @param maternityLeaveResultId 产假结果ID
     * @return 历史记录列表
     */
    List<HistoryDO> findByMaternityLeaveResultId(Long maternityLeaveResultId);

    /**
     * 根据津贴申请ID查询历史记录
     * @param maternityAllowanceRequestId 津贴申请ID
     * @return 历史记录列表
     */
    List<HistoryDO> findByMaternityAllowanceRequestId(Long maternityAllowanceRequestId);

    /**
     * 根据津贴结果ID查询历史记录
     * @param maternityAllowanceResultId 津贴结果ID
     * @return 历史记录列表
     */
    List<HistoryDO> findByMaternityAllowanceResultId(Long maternityAllowanceResultId);

    /**
     * 根据员工工号和记录类型查询历史记录
     * @param lanId 员工工号
     * @param recordType 记录类型
     * @return 历史记录列表
     */
    List<HistoryDO> findByLanIdAndRecordType(String lanId, RecordTypeEnum recordType);

    /**
     * 根据记录类型查询历史记录
     * @param recordType 记录类型
     * @return 历史记录列表
     */
    List<HistoryDO> findByRecordType(RecordTypeEnum recordType);
}
