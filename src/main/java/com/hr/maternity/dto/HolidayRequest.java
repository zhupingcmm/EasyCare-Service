package com.hr.maternity.dto;

import com.hr.maternity.entity.Holiday;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 节假日请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HolidayRequest {

    @NotNull(message = "日期不能为空")
    private LocalDate date;

    @NotBlank(message = "节假日名称不能为空")
    private String name;

    @NotNull(message = "类型不能为空")
    private Holiday.HolidayType type;

    @NotNull(message = "是否为法定假日不能为空")
    private Boolean isStatutory;
}
