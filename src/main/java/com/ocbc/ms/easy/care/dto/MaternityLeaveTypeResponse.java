package com.ocbc.ms.easy.care.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 产假类型响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaternityLeaveTypeResponse {

    private Integer id;

    private String code;

    private String name;

    private String remark;

    private Boolean enabled;

    private LocalDateTime createDate;

    private String createBy;

    private LocalDateTime updateDate;

    private String updateBy;
}
