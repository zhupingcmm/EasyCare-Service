package com.hr.maternity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MiscarriageLeaveDetail {

    //
    private String cityCode;
    private Integer index;
    private Integer days = 0;

    /**
     * if overrideDays==true, then need MFE pass truly days value to MS.
     */
    private Boolean needOverrideDays = false;
    private String description;
}
