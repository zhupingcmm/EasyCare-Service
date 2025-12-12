package com.ocbc.ms.easy.care.util;

import com.ocbc.ms.easy.care.repository.SpecialDayRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class EasyCareDateUtil {

    private final SpecialDayRepository specialDayRepository;

    public int countExtensionDays(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("结束日期不能早于开始日期");
        }

        log.debug("计算顺延天数，startDate={}, endDate={}", startDate, endDate);
        long count = specialDayRepository.countByDateBetweenAndTypeAndIsPublicHoliday(startDate, endDate, 1, true);
        return (int) count;
    }
}
