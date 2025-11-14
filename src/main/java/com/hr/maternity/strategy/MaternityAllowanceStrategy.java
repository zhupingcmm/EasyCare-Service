package com.hr.maternity.strategy;

import com.hr.maternity.dto.MaternityAllowanceRequest;
import com.hr.maternity.dto.MaternityAllowanceResponse;
/**
 * 生育津贴计算策略接口
 */
public interface MaternityAllowanceStrategy {
    
    /**
     * 计算生育津贴
     * @param request 生育津贴计算请求
     * @return 生育津贴计算结果
     */
    MaternityAllowanceResponse calculateMaternityAllowance(MaternityAllowanceRequest request);
    
    /**
     * 获取支持的城市代码
     * @return 城市代码
     */
    String getSupportedCityCode();
}
