package com.ocbc.ms.easy.care.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 流产类型请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MiscarriageTypeRequest {

    /**
     * 类型名称（如：早期流产、晚期流产）
     */
    @NotBlank(message = "类型名称不能为空")
    private String typeName;

    /**
     * 是否启用
     */
    @NotNull(message = "是否启用不能为空")
    private Boolean isActive;
}
