package com.easy.care.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 产假政策响应DTO（键值对格式）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaternityPolicyResponse {

    /**
     * 键名
     */
    private String key;

    /**
     * 值
     */
    private Object value;

    /**
     * 排序序号
     */
    private Integer order;

    /**
     * 扩展信息
     */
    private Map<String, Object> ext;
}
