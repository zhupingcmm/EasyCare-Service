package com.ocbc.ms.easy.care.dto.history;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class HistoryAddRequest {

    @NotBlank(message = "hrId 不能为空")
    private String hrId;

    @NotBlank(message = "employeeId 不能为空")
    private String employeeId;

    @NotNull(message = "employeeData 不能为空")
    private JsonNode employeeData;
}
