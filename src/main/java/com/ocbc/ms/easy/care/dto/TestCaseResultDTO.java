package com.ocbc.ms.easy.care.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 单个测试用例结果 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestCaseResultDTO {
    
    /** 测试用例编号 */
    private String caseNumber;
    
    /** 测试用例描述 */
    private String caseDescription;
    
    /** 城市代码 */
    private String cityCode;
    
    /** 是否成功 */
    private Boolean isSuccess;
    
    /** 输入数据 */
    private Map<String, Object> inputData;
    
    /** 期望结果 */
    private Object expectedResult;
    
    /** 实际结果 */
    private Object actualResult;
    
    /** 错误信息（如果失败） */
    private String errorMessage;
}
