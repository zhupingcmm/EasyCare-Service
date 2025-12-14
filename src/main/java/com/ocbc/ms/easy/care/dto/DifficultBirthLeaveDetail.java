package com.ocbc.ms.easy.care.dto;

import com.ocbc.ms.easy.care.enums.DifficultBirthTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DifficultBirthLeaveDetail {

    /**
     * @see DifficultBirthTypeEnum
     */
    private String code;
}
