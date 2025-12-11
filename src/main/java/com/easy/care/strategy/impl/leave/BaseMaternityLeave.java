package com.easy.care.strategy.impl.leave;

import com.easy.care.constant.MiscarriageLeaveRulesConstants;
import com.easy.care.constant.PublicHolidayConstants;
import com.easy.care.dto.MiscarriageLeaveDetail;
import com.easy.care.service.HolidayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Map;

/**
 * 多个城市相同的计算逻辑，可以继承自此类。
 *      有特殊计算，可以override
 */
@Slf4j
public class BaseMaternityLeave {

    @Autowired
    HolidayService holidayService;

    public void init(LocalDate startDate) {
        holidayService.initHoliday(startDate.getYear());
    }

    public LocalDate getNextWorkDay(LocalDate currDay) {
        holidayService.initHoliday(currDay.getYear());

        log.info("isHoliday {}", holidayService.getHoliday());
        log.info("isAdditionalWorkDay {}", holidayService.getAdditionalWorkDay());
        while (true) {
            LocalDate nextDay = currDay.plusDays(1);
            boolean isHoliday = holidayService.getHoliday().contains(nextDay);  // 公共节假日
            boolean isAdditionalWorkDay = holidayService.getAdditionalWorkDay().contains(nextDay);  // 补班
            boolean isWeekend = DayOfWeek.SUNDAY.equals(nextDay.getDayOfWeek())
                    || DayOfWeek.SATURDAY.equals(nextDay.getDayOfWeek());       // 周末
            if ( !isWeekend || isAdditionalWorkDay) {
                if (! isHoliday) {
                    currDay = nextDay;
                    break;
                }
            }
            currDay = currDay.plusDays(1);
            log.info("nextDay={}", nextDay);
        }
        return currDay;
    }

    /**
     * 校验请求体中的 MiscarriageLeaveDetail 。
     * 如果合法，返回days。如果非法，返回days=0。
     * @param reqData
     * @return
     */
    public int validateAndExtractDaysFromRequest(MiscarriageLeaveDetail reqData) {
        int result = 0;
        String key = reqData.getCityCode() + "_" + reqData.getCode();
        MiscarriageLeaveDetail cache = MiscarriageLeaveRulesConstants.MISCARRIAGE_LEAVE_RULE_MAP.get(key);
        if (cache != null) {
            if (cache.getNeedOverrideDays()) {
                // 用请求中的days覆盖天数
                result = reqData.getDays();
            } else {
                if (cache.getDays().equals(reqData.getDays())) {
                    result = cache.getDays();
                } else {
                    log.error("reqData.getDays() 非法输入 {}", reqData.getDays());
                    result = cache.getDays();
                }
            }
        }
        log.info("finalDays={}\treqData={}\tcache={}", result, reqData, cache);
        return result;
    }

    protected int getDayPlusForExtendedLeave(int extendedDays, LocalDate centerDate, Map<String, String> outDetails) {
        int plusDay = 0;
        int notCostDay = 0;
        for (; extendedDays > 0; plusDay++) {
            if (PublicHolidayConstants.HOLIDAY.contains(centerDate)) {
                Map<String, Object> stringObjectMap = holidayService.getDayInfoMap().get(centerDate);
                String infoStr = String.format("%s %s", stringObjectMap.get("name_cn"), stringObjectMap.get("name_en"));
                outDetails.put(centerDate.toString(), infoStr);
                notCostDay++;
            }
            else {
                extendedDays--;
            }
            centerDate = centerDate.plusDays(1);
        }
        System.out.println("要加的天数：plusDay=" + plusDay);
        System.out.println("顺延的天数：notCostDay=" + notCostDay);
        return plusDay;
    }

}
