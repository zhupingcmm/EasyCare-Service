package com.easy.care.service;

import com.easy.care.dto.MaternityAllowanceRequest;
import com.easy.care.dto.MaternityAllowanceResponse;


/**
 * 生育津贴计算服务接口
 */
public interface MaternityAllowanceService {
    
    /**
     * 计算生育津贴
     * @param request 生育津贴计算请求
     * @return 生育津贴计算结果
     */
    MaternityAllowanceResponse calculateMaternityAllowance(MaternityAllowanceRequest request);
    
}
