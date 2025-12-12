package com.ocbc.ms.easy.care.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 特殊日期响应DTO（节假日/补班）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HolidayResponse {

    private Integer id;

    private Integer year;

    private String region;

    private LocalDate date;

    private String name;

    private String cnName;

    private String enName;

    private Integer type;

    private Boolean isPublicHoliday;

    private Boolean enabled;

    private LocalDateTime createDate;

    private String createBy;

    private LocalDateTime updateDate;

    private String updateBy;
}
