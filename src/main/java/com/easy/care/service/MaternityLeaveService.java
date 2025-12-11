package com.easy.care.service;

import com.easy.care.dto.MaternityLeaveRequest;
import com.easy.care.dto.MaternityLeaveResponse;

/**
 * 产假计算服务接口
 */
public interface MaternityLeaveService {
    
    /**
     * 计算产假天数
     * @param request 产假计算请求
     * @return 产假计算结果
     */
    MaternityLeaveResponse calculateMaternityLeave(MaternityLeaveRequest request);

    MaternityLeaveResponse calculateMaternityLeaveNew(MaternityLeaveRequest request);

}
