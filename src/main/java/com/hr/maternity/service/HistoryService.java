package com.hr.maternity.service;

import com.hr.maternity.dto.HistoryDTO;

import java.util.List;

/**
 * 历史记录服务接口
 */
public interface HistoryService {
    
    /**
     * 根据员工工号查询历史记录
     * @param lanId 员工工号
     * @return 历史记录列表
     */
    List<HistoryDTO> findByLanId(String lanId);
}
