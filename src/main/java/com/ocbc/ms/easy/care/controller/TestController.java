package com.ocbc.ms.easy.care.controller;

import com.ocbc.ms.easy.care.service.HolidayService;
import com.ocbc.ms.easy.care.strategy.impl.leave.BaseMaternityLeave;
import com.ocbc.ms.easy.care.strategy.impl.leave.ShanghaiMaternityLeaveStrategy;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/v1/test")
public class TestController {

    @Autowired
    private ShanghaiMaternityLeaveStrategy baseMaternityLeave;

    @GetMapping("/getNextWorkDayOf")
    public Map<String, Object> getNextWorkDayOf(@NonNull LocalDate day) {
        log.info("day={}", day);
        LocalDate nextWorkDay = baseMaternityLeave.getNextWorkDay(day);
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("enddate", day);
        map.put("nextWorkDay", nextWorkDay);
        return map;
    }
}
