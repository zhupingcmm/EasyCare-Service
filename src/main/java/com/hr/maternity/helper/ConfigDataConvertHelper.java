package com.hr.maternity.helper;

import com.ocbc.ms.easy.care.enums.AwardLeaveEnum;
import org.springframework.stereotype.Service;

@Service
public class ConfigDataConvertHelper {

    public static AwardLeaveEnum convertKidsToAwardLeaveEnum(Integer numOfKids) {
        if (numOfKids == null) {
            return null;
        }
        return switch (numOfKids) {
            case 1 -> AwardLeaveEnum.AWD_001;
            case 2 -> AwardLeaveEnum.AWD_002;
            case 3 -> AwardLeaveEnum.AWD_003;
            case 4 -> AwardLeaveEnum.AWD_004;
            case 5 -> AwardLeaveEnum.AWD_005;
            default -> null;
        };
    }
}
