package com.ocbc.ms.easy.care.rule;

import com.ocbc.ms.easy.care.dto.MaternityLeaveDaysResult;
import com.ocbc.ms.easy.care.dto.MaternityLeaveRequest;
import com.ocbc.ms.easy.care.dto.MaternityLeaveResponse;
import com.ocbc.ms.easy.care.dto.TimeScope;
import com.ocbc.ms.easy.care.entity.MaternityRules;
import com.ocbc.ms.easy.care.enums.MaternityLeaveTypeEnum;
import com.ocbc.ms.easy.care.helper.MaternityLeaveDateHelper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class MaternityLeaveRuleService {

    @Autowired
    private MaternityLeaveDateHelper maternityLeaveDateHelper;

    public MaternityLeaveResponse calcMaternityDuration(MaternityLeaveRequest maternityLeaveRequest,
                                                        List<MaternityRules> maternityRuleList) {
        maternityRuleList.sort(Comparator
                .comparingInt(r -> MaternityLeaveTypeEnum.fromId(r.getMaternityLeaveType().getId()).getPriority()));
        LocalDate startDate = maternityLeaveRequest.getExpectedDeliveryDate();
        int totalDays = 0;
        int totalAllowanceDays = 0;
        List<TimeScope> timeScopeList = new ArrayList<>();
        int index = 0;
        LocalDate innerStartDate = startDate;
        for (MaternityRules rule : maternityRuleList) {
            MaternityLeaveTypeEnum typeEnum = MaternityLeaveTypeEnum.fromId(rule.getMaternityLeaveType().getId());
            MaternityLeaveDaysResult segment = maternityLeaveDateHelper.calMaternityLeaveDay(
                    maternityLeaveRequest,
                    innerStartDate,
                    rule,
                    typeEnum
            );
            if (segment == null || segment.getAdjustEndDate() == null) {
                continue;
            }
            LocalDate adjustEndDate = segment.getAdjustEndDate();
            Integer  adjustLeaveDays = segment.getAdjustLeaveDays();

            TimeScope timeScope = TimeScope.builder()
                    .index(index++)
                    .name(typeEnum.getCode())
                    .startAt(innerStartDate)
                    .endAt(adjustEndDate)
                    .build();
//            int days = (int) ChronoUnit.DAYS.between(innerStartDate, adjustEndDate) + 1;
//            Integer allowanceFromStrategy = segment.getAllowanceDays();
//            int allowanceDays = allowanceFromStrategy == null ? 0 : allowanceFromStrategy;
            timeScope.setDays(adjustLeaveDays);
            timeScopeList.add(timeScope);
            totalDays += adjustLeaveDays;
            totalAllowanceDays += segment.getAllowanceDays();
            innerStartDate = adjustEndDate.plusDays(1);
        }
        return genMaternityLeaveResponse(maternityLeaveRequest, timeScopeList, totalDays, totalAllowanceDays);
    }

    public MaternityLeaveResponse genMaternityLeaveResponse(MaternityLeaveRequest maternityLeaveRequest,
                                                            List<TimeScope> timeScopeList, int totalDays, int totalAllowanceDays) {
        MaternityLeaveResponse resp = new MaternityLeaveResponse();
        BeanUtils.copyProperties(maternityLeaveRequest, resp);
        LocalDate startDate = maternityLeaveRequest.getExpectedDeliveryDate();
        LocalDate endDate = startDate.plusDays(totalDays - 1);
        resp.setStartDate(startDate);
        resp.setEndDate(endDate);
        resp.setTotalDays(totalDays);
        resp.setTotalAllowanceDays(totalAllowanceDays);
        resp.setTimeScopeList(timeScopeList);
        resp.setReturnToWorkDate(endDate.plusDays(1));
        // 按 code 聚合各段天数到响应字段
        if (timeScopeList != null) {
            for (TimeScope ts : timeScopeList) {
                if (ts == null || ts.getName() == null || ts.getDays() == null) {
                    continue;
                }
                String code = ts.getName();
                int days = ts.getDays();
                switch (code) {
                    case "base":
                        resp.setBaseDays((resp.getBaseDays() == null ? 0 : resp.getBaseDays()) + days);
                        break;
                    case "dystocia":
                        resp.setDystociaDays((resp.getDystociaDays() == null ? 0 : resp.getDystociaDays()) + days);
                        break;
                    case "multiple":
                        resp.setMultiBabyDays((resp.getMultiBabyDays() == null ? 0 : resp.getMultiBabyDays()) + days);
                        break;
                    case "award":
                        resp.setExtendedDays((resp.getExtendedDays() == null ? 0 : resp.getExtendedDays()) + days);
                        break;
                    case "miscarriage":
                        resp.setMiscarriageLeaveDays((resp.getMiscarriageLeaveDays() == null ? 0 : resp.getMiscarriageLeaveDays()) + days);
                        break;
                    default:
                        // 未知类型不计入细项
                        break;
                }
            }
        }
        return resp;
    }
}
