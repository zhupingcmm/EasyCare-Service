package com.ocbc.ms.easy.care.strategy.impl.leave;

import com.ocbc.ms.easy.care.dto.MaternityLeaveRequest;
import com.ocbc.ms.easy.care.entity.MaternityRules;
import com.ocbc.ms.easy.care.enums.AwardLeaveEnum;
import com.ocbc.ms.easy.care.enums.MaternityLeaveTypeEnum;
import com.ocbc.ms.easy.care.helper.ConfigDataConvertHelper;
import com.ocbc.ms.easy.care.strategy.leave.MaternityLeaveDaysStrategy;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Component;

@Component
public class AwardLeaveDaysStrategy implements MaternityLeaveDaysStrategy {
    @Override
    public MaternityLeaveTypeEnum getType() {
        return MaternityLeaveTypeEnum.AWARD;
    }

    @Override
    public Integer calculate(MaternityLeaveRequest request, MaternityRules rule) {
        // When miscarriage, skip non-miscarriage strategies
        if (Boolean.TRUE.equals(request.getIsMiscarriage())) {
            return 0;
        }
        if (BooleanUtils.isFalse(request.getHasExtendedDays())) {
            return 0;
        }
        AwardLeaveEnum awdCode = ConfigDataConvertHelper.convertKidsToAwardLeaveEnum(request.getNumberOfKids());
        if (awdCode == null) {
            return 0;
        }
        return findDaysFromExtOrDefault(rule, awdCode.getCode());
    }




}
