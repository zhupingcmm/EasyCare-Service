package com.hr.maternity.strategy;

import com.hr.maternity.dto.MaternityLeaveRequest;
import com.hr.maternity.dto.MaternityLeaveResponse;
/**
 * 产假计算策略接口
 */
public interface MaternityLeaveStrategy {
    
    /**
     * 计算产假
     * @param request 产假计算请求
     * @return 产假计算结果
     */
    MaternityLeaveResponse calculateMaternityLeave(MaternityLeaveRequest request);
    
    /**
     * 获取支持的城市代码
     * @return 城市代码
     */
    String getSupportedCityCode();

}
