package com.easy.care.strategy.impl.leave;

import com.easy.care.dto.MaternityLeaveRequest;
import com.easy.care.dto.MaternityLeaveResponse;

import com.easy.care.dto.TimeScope;
import com.easy.care.enums.LeaveArgsOfCityEnum;
import com.easy.care.strategy.MaternityLeaveStrategy;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class SuzhouMaternityLeaveStrategy extends BaseMaternityLeave implements MaternityLeaveStrategy {

    @Override
    public MaternityLeaveResponse calculateMaternityLeave(MaternityLeaveRequest request) {
        MaternityLeaveResponse response = new MaternityLeaveResponse();
        LocalDate startDate = request.getExpectedDeliveryDate();
        super.init(startDate);
        List<TimeScope> timeScopeList = new ArrayList<>();
        int totalDays = 0;

        // 法定产假：98 日历日
        // 难产假：15
        // 多胞胎：每多生育一胞胎，多加15天
        // 晚育假｜生育假｜奖励假：60日历日 【遇法定节假日顺延】
        // 流产假：1. 妊娠未满4个月流产，15天； 2. 妊娠满4个月流产，42天；

        // 苏州产假规则：基础产假98天 + 生育假60天 = 158天
        LeaveArgsOfCityEnum shArgs = LeaveArgsOfCityEnum.fromCode(request.getCityCode());
        int baseDays = shArgs.getBaseDays();
        int dystociaDays = shArgs.getDystociaDays();
        int multiBabyDays = shArgs.getMultiBabyDaysFactor() * (request.getNumberOfBabies() - 1);
        int extendedDays = shArgs.getExtendedDays();
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
                LocalDate centerDate = startDate.plusDays(totalDays);
                Map<String, String> details = new LinkedHashMap<>();
                int dayPlusForExtendedLeave = this.getDayPlusForExtendedLeave(extendedDays, centerDate, details);

                totalDays += dayPlusForExtendedLeave;
                response.setPubHolidaysCount(dayPlusForExtendedLeave - extendedDays);

                String additionalInfo = String.format("生育假%s天 = %s天 + 顺延%s天", dayPlusForExtendedLeave, extendedDays, dayPlusForExtendedLeave - extendedDays);
                timeScopeList.add(TimeScope.builder()
                        .index(4)
                        .name("4. 晚育假/生育假/奖励假")
                        .additionalInfo(additionalInfo)
                        .days(extendedDays)
                        .startAt(startDate.plusDays(totalDays - dayPlusForExtendedLeave))
                        .endAt(startDate.plusDays(totalDays - 1))
                        .details(details)
                        .build());
            } else {
                extendedDays = 0;
            }
        }

        endDate = startDate.plusDays(totalDays - 1);
        LocalDate returnToWorkDate = super.getNextWorkDay(endDate);
        response.setReturnToWorkDate(returnToWorkDate);

        response.setTotalDays(totalDays - response.getPubHolidaysCount());
        int totalAllowanceDays = totalDays - response.getPubHolidaysCount();
        response.setTotalAllowanceDays(totalAllowanceDays);
        response.setBaseDays(baseDays);
        response.setDystociaDays(dystociaDays);
        response.setMultiBabyDays(multiBabyDays);
        response.setExtendedDays(extendedDays);
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
        return "SU";
    }
}
