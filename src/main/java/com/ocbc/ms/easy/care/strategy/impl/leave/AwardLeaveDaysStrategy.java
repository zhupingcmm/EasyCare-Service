package com.ocbc.ms.easy.care.strategy.impl.leave;

import com.ocbc.ms.easy.care.dto.MaternityLeaveDaysResult;
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
    public MaternityLeaveDaysResult calculate(MaternityLeaveRequest request, MaternityRules rule) {
        // When miscarriage, skip non-miscarriage strategies
        if (Boolean.TRUE.equals(request.getIsMiscarriage())) {
            return null;
        }
        if (BooleanUtils.isFalse(request.getHasExtendedDays())) {
            return buildResult(0, 0);
        }
        AwardLeaveEnum awdCode = ConfigDataConvertHelper.convertKidsToAwardLeaveEnum(request.getNumberOfKids());
        if (awdCode == null) {
            return buildResult(0, 0);
        }
        int days = findDaysFromExtOrDefault(rule, awdCode.getCode());
        int allowanceDays = BooleanUtils.isTrue(rule.getHasAllowance())? days: 0;
        return buildResult(days, allowanceDays);
    }
}
