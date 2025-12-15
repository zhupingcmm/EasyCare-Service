package com.ocbc.ms.easy.care.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class MaternityLeaveTypeEndDate {
    private LocalDate originEndDate;
    private Integer extendDays;
    private LocalDate adjustEndDate;
}
