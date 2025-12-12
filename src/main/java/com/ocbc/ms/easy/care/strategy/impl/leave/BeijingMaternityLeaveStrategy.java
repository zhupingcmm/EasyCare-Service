package com.ocbc.ms.easy.care.strategy.impl.leave;

import com.ocbc.ms.easy.care.dto.MaternityLeaveRequest;
import com.ocbc.ms.easy.care.dto.MaternityLeaveResponse;

import com.ocbc.ms.easy.care.dto.TimeScope;
import com.ocbc.ms.easy.care.enums.LeaveArgsOfCityEnum;
import com.ocbc.ms.easy.care.strategy.MaternityLeaveStrategy;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class BeijingMaternityLeaveStrategy extends BaseMaternityLeave implements MaternityLeaveStrategy {

    @Override
    public MaternityLeaveResponse calculateMaternityLeave(MaternityLeaveRequest request) {
        MaternityLeaveResponse response = new MaternityLeaveResponse();
        List<TimeScope> timeScopeList = new ArrayList<>();
        LocalDate startDate = request.getExpectedDeliveryDate();
        super.init(startDate);
        int totalDays = 0;

        // 深圳产假规则：基础产假98天 + 奖励假80天 = 178天
        // SHENZHEN("深圳", "SZ", 98, 30, 15, 80)
        LeaveArgsOfCityEnum shArgs = LeaveArgsOfCityEnum.fromCode(request.getCityCode());
        int baseDays = shArgs.getBaseDays();        // 98
        int dystociaDays = shArgs.getDystociaDays();// 30
        int multiBabyDays = shArgs.getMultiBabyDaysFactor() * (request.getNumberOfBabies() - 1);    // * 15
        int extendedDays = shArgs.getExtendedDays();// 60 / 90
        int miscarriageLeaveDays = 0;

        LocalDate endDate = null;

        // 流产：只休流产假
        if (request.getIsMiscarriage()) {
            baseDays = 0;
            dystociaDays = 0;
            multiBabyDays = 0;
            extendedDays = 0;
            miscarriageLeaveDays = super.validateAndExtractDaysFromRequest(request.getMiscarriageLeaveDetail());
            totalDays += miscarriageLeaveDays;
            timeScopeList.add(TimeScope.builder()
                    .index(1)
                    .name("5. 流产假")
                    .days(miscarriageLeaveDays)
                    .startAt(startDate)
                    .endAt(startDate.plusDays(totalDays - 1))
                    .build());
        }
        // 未流产：休法定产假+难产假+多胞胎假+晚育假｜生育假｜奖励假
        else {
            // 1. 法定产假
            totalDays += baseDays;
            timeScopeList.add(TimeScope.builder()
                    .index(1)
                    .name("1. 法定产假")
                    .days(baseDays)
                    .startAt(startDate)
                    .endAt(startDate.plusDays(totalDays - 1))
                    .build());

            // 2. 难产假
            if (Boolean.TRUE.equals(request.getIsDifficultBirth())) {
                totalDays += dystociaDays;
                timeScopeList.add(TimeScope.builder()
                        .index(2)
                        .name("2. 难产假")
                        .days(dystociaDays)
                        .startAt(startDate.plusDays(totalDays - dystociaDays))
                        .endAt(startDate.plusDays(totalDays - 1))
                        .build());
            } else {
                dystociaDays = 0;
            }

            // 3. 多胞胎 每多一个婴儿增加15天
            if (request.getNumberOfBabies() > 1) {
                totalDays += multiBabyDays;
                timeScopeList.add(TimeScope.builder()
                        .index(3)
                        .name("3. 多胞胎")
                        .days(multiBabyDays)
                        .startAt(startDate.plusDays(totalDays - multiBabyDays))
                        .endAt(startDate.plusDays(totalDays - 1))
                        .build());
            } else {
                multiBabyDays = 0;
            }

            // 4. 晚育假/生育假/奖励假
            if (request.getHasExtendedDays()) {
                totalDays += extendedDays;
                timeScopeList.add(TimeScope.builder()
                        .index(4)
                        .name("4. 晚育假/生育假/奖励假")
                        .days(extendedDays)
                        .startAt(startDate.plusDays(totalDays - extendedDays))
                        .endAt(startDate.plusDays(totalDays - 1))
                        .build());
            } else {
                extendedDays = 0;
            }
        }

        endDate = startDate.plusDays(totalDays - 1);
        LocalDate returnToWorkDate = super.getNextWorkDay(endDate);
        response.setReturnToWorkDate(returnToWorkDate);

        response.setTotalDays(totalDays);
        // 【绍兴生育津贴包含奖励假 且节假日不顺延】
        int totalAllowanceDays = totalDays;
        response.setTotalAllowanceDays(totalAllowanceDays);
        response.setBaseDays(baseDays);
        response.setDystociaDays(dystociaDays);
        response.setMultiBabyDays(multiBabyDays);
        response.setExtendedDays(extendedDays);
        response.setMiscarriageLeaveDays(miscarriageLeaveDays);
        response.setLanId(request.getLanId());
        response.setEmployeeName(request.getEmployeeName());
        response.setCityCode(request.getCityCode());
        response.setStartDate(startDate);
        response.setEndDate(endDate);
        response.setTimeScopeList(timeScopeList);

        return response;
    }

    @Override
    public String getSupportedCityCode() {
        return "BJ";
    }
}
