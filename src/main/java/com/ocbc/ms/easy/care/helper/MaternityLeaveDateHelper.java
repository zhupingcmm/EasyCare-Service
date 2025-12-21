package com.ocbc.ms.easy.care.helper;

import com.ocbc.ms.easy.care.dto.MaternityLeaveDaysResult;
import com.ocbc.ms.easy.care.dto.MaternityLeaveRequest;
import com.ocbc.ms.easy.care.dto.MaternityLeaveTypeEndDate;
import com.ocbc.ms.easy.care.entity.MaternityRules;
import com.ocbc.ms.easy.care.enums.MaternityLeaveTypeEnum;
import com.ocbc.ms.easy.care.strategy.leave.MaternityLeaveDaysStrategy;
import com.ocbc.ms.easy.care.util.EasyCareDateUtil;
import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;


@Service
public class MaternityLeaveDateHelper {

    @Autowired
    private EasyCareDateUtil easyCareDateUtil;

    private final Map<MaternityLeaveTypeEnum, DaysCalculator> calculators = new EnumMap<>(MaternityLeaveTypeEnum.class);

    @Autowired(required = false)
    private List<MaternityLeaveDaysStrategy> strategies;

    @PostConstruct
    private void initCalculators() {
        for (MaternityLeaveDaysStrategy s : strategies) {
            calculators.put(s.getType(), s::calculate);
        }
    }

    @FunctionalInterface
    private interface DaysCalculator {
        MaternityLeaveDaysResult apply(MaternityLeaveRequest request, MaternityRules rule);
    }

    public MaternityLeaveDaysResult calMaternityLeaveDay(MaternityLeaveRequest maternityLeaveRequest,
                                          LocalDate startDate,
                                          MaternityRules maternityRule,
                                          MaternityLeaveTypeEnum type) {
        if (type == null) {
            return null;
        }
        DaysCalculator calculator = calculators.get(type);
        MaternityLeaveDaysResult baseResult = calculator == null ? null : calculator.apply(maternityLeaveRequest, maternityRule);
        if (baseResult == null || baseResult.getLeaveDays() == null || baseResult.getLeaveDays() <= 0) {
            return null;
        }
        return computeLeaveDaysWithExtension(startDate, baseResult, maternityRule.getHolidayExtend());
    }

    /**
     * 计算单段产假的起止日期与节假日顺延信息，并写回到 MaternityLeaveDaysResult 中。
     */
    private MaternityLeaveDaysResult computeLeaveDaysWithExtension(LocalDate startDate,
                                                                   MaternityLeaveDaysResult baseResult,
                                                                   Boolean holidayExtend) {
        Integer days = baseResult == null ? null : baseResult.getLeaveDays();
        if (startDate == null || days == null || days <= 0) {
            return null;
        }

        // 复制一份，避免意外修改原对象（按需，可直接在 baseResult 上修改）
        MaternityLeaveDaysResult result = MaternityLeaveDaysResult.builder()
                .leaveDays(baseResult.getLeaveDays())
                .allowanceDays(baseResult.getAllowanceDays())
                .build();

        LocalDate originEndDate = startDate.plusDays(days - 1);
        result.setOriginEndDate(originEndDate);

        if (BooleanUtils.isNotTrue(holidayExtend)) {
            result.setExtendDays(0);
            result.setAdjustEndDate(originEndDate);
            return result;
        }

        LocalDate endDate = originEndDate;
        LocalDate loopStart = startDate;
        int totalExtend = 0;
        while (true) {
            int count = easyCareDateUtil.countExtensionDays(loopStart, endDate);
            if (count > 0) {
                endDate = endDate.plusDays(count);
                loopStart = endDate;
                totalExtend += count;
            } else {
                break;
            }
        }
        result.setExtendDays(totalExtend);
        result.setAdjustEndDate(endDate);
        return result;
    }
}

