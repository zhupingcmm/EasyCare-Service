package com.hr.maternity.dto;

import com.hr.maternity.entity.Holiday;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 节假日响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HolidayResponse {

    private Integer id;

    private LocalDate date;

    private String name;

    private Holiday.HolidayType type;

    private Boolean isStatutory;

    private Boolean isActive;

    private LocalDateTime createdAt;

    private String createdBy;

    private LocalDateTime updatedAt;

    private String updatedBy;
}
