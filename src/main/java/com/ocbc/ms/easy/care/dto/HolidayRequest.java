package com.ocbc.ms.easy.care.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 特殊日期请求DTO（节假日/补班）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HolidayRequest {

    @NotNull(message = "年份不能为空")
    private Integer year;

    @Builder.Default
    private String region = "CN";

    @NotNull(message = "日期不能为空")
    private LocalDate date;

    @NotBlank(message = "名称不能为空")
    private String name;

    @NotBlank(message = "中文名称不能为空")
    private String cnName;

    @NotBlank(message = "英文名称不能为空")
    private String enName;

    @NotNull(message = "类型不能为空")
    private Integer type;

    @NotNull(message = "是否为国定假日不能为空")
    private Boolean isPublicHoliday;

    @Builder.Default
    private Boolean enabled = true;
}
