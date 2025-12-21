package com.ocbc.ms.easy.care.strategy.impl.leave;

import com.ocbc.ms.easy.care.dto.MaternityLeaveDaysResult;
import com.ocbc.ms.easy.care.dto.MaternityLeaveRequest;
import com.ocbc.ms.easy.care.dto.MiscarriageLeaveDetail;
import com.ocbc.ms.easy.care.entity.MaternityRules;
import com.ocbc.ms.easy.care.enums.MaternityLeaveTypeEnum;
import com.ocbc.ms.easy.care.strategy.leave.MaternityLeaveDaysStrategy;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class MiscarriageLeaveDaysStrategy implements MaternityLeaveDaysStrategy {

    @Override
    public MaternityLeaveTypeEnum getType() {
        return MaternityLeaveTypeEnum.MISCARRIAGE;
    }

    @Override
    public MaternityLeaveDaysResult calculate(MaternityLeaveRequest request, MaternityRules rule) {
        if (BooleanUtils.isFalse(request.getIsMiscarriage())) {
            return null;
        }
        MiscarriageLeaveDetail detail = request.getMiscarriageLeaveDetail();
        if (detail == null || StringUtils.isBlank(detail.getCode())) {
            throw new IllegalArgumentException("misCarriage param invalid");
        }
        int days = findDaysFromExtOrDefault(rule, detail.getCode());
        return buildResult(days, days);
    }
}
