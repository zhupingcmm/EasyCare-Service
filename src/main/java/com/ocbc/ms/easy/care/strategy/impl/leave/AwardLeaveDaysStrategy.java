package com.ocbc.ms.easy.care.strategy.impl.leave;

import com.alibaba.fastjson2.JSONArray;
import com.ocbc.ms.easy.care.dto.MaternityLeaveExtDTO;
import com.ocbc.ms.easy.care.dto.MaternityLeaveRequest;
import com.ocbc.ms.easy.care.entity.MaternityRules;
import com.ocbc.ms.easy.care.enums.AwardLeaveEnum;
import com.ocbc.ms.easy.care.enums.MaternityLeaveTypeEnum;
import com.ocbc.ms.easy.care.helper.ConfigDataConvertHelper;
import com.ocbc.ms.easy.care.strategy.leave.MaternityLeaveDaysStrategy;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;

@Component
public class AwardLeaveDaysStrategy implements MaternityLeaveDaysStrategy {
    @Override
    public MaternityLeaveTypeEnum getType() {
        return MaternityLeaveTypeEnum.AWARD;
    }

    @Override
    public Integer calculate(MaternityLeaveRequest request, MaternityRules rule) {
        if (BooleanUtils.isFalse(request.getHasExtendedDays())) {
            return 0;
        }
        AwardLeaveEnum awdCode = ConfigDataConvertHelper.convertKidsToAwardLeaveEnum(request.getNumberOfKids());
        if (awdCode == null) {
            return 0;
        }
        return findDaysFromExtOrDefault(rule, awdCode.getCode());
    }

    private Integer findDaysFromExtOrDefault(MaternityRules rule, String matchCode) {
        Object ext = rule.getMaternityLeaveExt();
        if (ext == null) {
            return rule.getDefaultDays();
        }
        List<MaternityLeaveExtDTO> extList = JSONArray.parseArray((String) ext, MaternityLeaveExtDTO.class);
        if (CollectionUtils.isEmpty(extList)) {
            return rule.getDefaultDays();
        }
        for (MaternityLeaveExtDTO dto : extList) {
            if (dto != null && Objects.equals(dto.getCode(), matchCode)) {
                return dto.getDays();
            }
        }
        return rule.getDefaultDays();
    }
}
