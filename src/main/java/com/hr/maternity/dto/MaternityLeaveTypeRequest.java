package com.hr.maternity.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 产假类型请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaternityLeaveTypeRequest {

    @NotBlank(message = "类型代码不能为空")
    private String code;

    @NotBlank(message = "类型名称不能为空")
    private String name;

    private String remark;

    @Builder.Default
    private Boolean enabled = true;
}
