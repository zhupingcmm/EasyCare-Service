package com.ocbc.ms.easy.care.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 批量测试结果 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchTestResultDTO {
    
    /** 总测试用例数 */
    private Integer totalCount;
    
    /** 成功数量 */
    private Integer successCount;
    
    /** 失败数量 */
    private Integer failureCount;
    
    /** 成功率 */
    private Double successRate;
    
    /** 详细结果列表 */
    private List<TestCaseResultDTO> details;
    
    /** 执行耗时（毫秒） */
    private Long executionTimeMs;
}
