package com.easy.care.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * City remark DTO
 * - allowanceToIndividual: 是否将生育津贴发放至个人
 * - dystociaType: 难产类型选项列表（仅广州使用），元素包含 code/desc
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.ALWAYS)
public class CityRemark {

    @Schema(description = "生育津贴是否发放至个人（true 个人，false 公司）")
    private Boolean allowanceToIndividual;

    @Schema(description = "难产类型选项列表（仅广州适用，每项包含 code 与 desc）")
    private List<Option> dystociaType;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "难产类型选项")
    public static class Option {
        @Schema(description = "代码，如 SEVERE_DYSTOCIA/ASSISTED_DELIVERY")
        private String code;
        @Schema(description = "描述")
        private String desc;
    }
}
