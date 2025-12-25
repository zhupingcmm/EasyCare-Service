package com.ocbc.ms.easy.care.dto.history;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 历史记录 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoryRecordDTO {

    private Long id;

    private String hrId;

    private String employeeId;

    private JsonNode employeeData;

    private LocalDate startDate;
}
