package com.easy.care.service;

import com.easy.care.dto.HistoryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 历史记录服务接口
 */
public interface HistoryService {
    
    /**
     * 根据员工工号查询历史记录（分页）
     * @param lanId 员工工号
     * @param pageable 分页参数
     * @return 分页历史记录
     */
    Page<HistoryDTO> findByLanId(String lanId, Pageable pageable);
    
    /**
     * 根据员工工号查询历史记录（不分页）
     * @param lanId 员工工号
     * @return 历史记录列表
     */
    List<HistoryDTO> findByLanId(String lanId);
}
