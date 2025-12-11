package com.easy.care.dto.history;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
}
