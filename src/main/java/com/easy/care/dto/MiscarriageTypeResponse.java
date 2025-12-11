package com.easy.care.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 流产类型响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MiscarriageTypeResponse {

    /**
     * 主键ID
     */
    private Integer id;

    /**
     * 类型名称（如：早期流产、晚期流产）
     */
    private String typeName;

    /**
     * 是否启用
     */
    private Boolean isActive;

    /**
     * 创建时间
     */
    private LocalDateTime createDate;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 更新时间
     */
    private LocalDateTime updateDate;

    /**
     * 更新人
     */
    private String updateBy;
}
