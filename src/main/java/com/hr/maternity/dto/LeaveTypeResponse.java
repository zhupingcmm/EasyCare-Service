package com.hr.maternity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 长假类型响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveTypeResponse {

    /**
     * 主键ID
     */
    private Integer id;

    /**
     * 类型名称（如：产假、陪产假）
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
