package com.hr.maternity.service.impl;

import com.hr.maternity.entity.Holiday;
import com.hr.maternity.repository.HolidayRepository;
import com.hr.maternity.service.HolidayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.*;

/**
 * 节假日服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HolidayServiceImpl implements HolidayService {

    private final RestTemplate restTemplate;
    private final HolidayRepository holidayRepository;

    private Set<LocalDate> additionalWorkDay = new HashSet<>();
    private Set<LocalDate> holiday = new HashSet<>();
    private Map<LocalDate, Map<String, Object>> dayInfoMap = new HashMap<>();


    @Override
    public Set<LocalDate> getAdditionalWorkDay() {
        return additionalWorkDay;
    }

    @Override
    public Set<LocalDate> getHoliday() {
        return holiday;
    }

    @Override
    public Map<LocalDate, Map<String, Object>> getDayInfoMap() {
        return dayInfoMap;
    }

    @Override
    // data from API: http://122.152.220.47:9016/api/support/holidays/2025
    public void initHoliday(int year) {
//        if (!dayInfoMap.isEmpty()) {
//            return;
//        }
        List<Map<String, Object>> publicHolidays = this.getPublicHolidays(String.valueOf(year));
        try {
            List<Map<String, Object>> publicHolidays2 = this.getPublicHolidays(String.valueOf(year + 1));
            publicHolidays.addAll(publicHolidays2);
        } catch (Exception e) {
            log.error("{} 还没有公布假期: {}", year + 1, e.getMessage());
        }

        log.info("publicHolidays= {}", publicHolidays);
        publicHolidays.forEach(currDate -> {
            Object type = currDate.get("type");
            String date = String.valueOf(currDate.get("date"));
            Object name = currDate.get("name");
            LocalDate parseDay = LocalDate.parse(date);
            if ("public_holiday".equals(type)) {
                holiday.add(parseDay);
            }
            else if ("transfer_workday".equals(type)) {
                additionalWorkDay.add(parseDay);
            }
            dayInfoMap.put(parseDay, currDate);
        });
        log.info("holiday={}", holiday);
        log.info("additionalWorkDay={}", additionalWorkDay);
    }

    @Override
    @Transactional
    public List<Map<String, Object>> getPublicHolidays(String year) {
        Integer yearInt = Integer.parseInt(year);
        String region = "CN";

        log.info("获取{}年节假日数据", year);

        // 1. 首先从数据库查询
        List<Holiday> dbHolidays = holidayRepository.findByYearAndRegionOrderByDate(yearInt, region);
        if (!dbHolidays.isEmpty()) {
            log.info("从数据库获取到{}年节假日数据，共{}条", year, dbHolidays.size());
            return convertHolidaysToMap(dbHolidays);
        }

        // 2. 数据库没有数据，从第三方API获取
        log.info("数据库中没有{}年节假日数据，从第三方API获取", year);
        List<Map<String, Object>> apiHolidays = fetchHolidaysFromApi(year);

        // 3. 将API数据保存到数据库
        if (!apiHolidays.isEmpty()) {
            saveHolidaysToDatabase(apiHolidays, yearInt, region);
            log.info("已将{}年节假日数据保存到数据库，共{}条", year, apiHolidays.size());
        }

        return apiHolidays;
    }

    /**
     * 从第三方API获取节假日数据
     */
    private List<Map<String, Object>> fetchHolidaysFromApi(String year) {
        String url = "https://unpkg.com/holiday-calendar/data/CN/" + year + ".json";
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response != null && response.containsKey("dates")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> holidays = (List<Map<String, Object>>) response.get("dates");
                return holidays;
            }
            log.warn("节假日数据格式不正确，返回空列表");
            return new ArrayList<>();
        } catch (Exception e) {
            log.error("从第三方API获取{}年节假日数据失败: {}，返回空列表", year, e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 将节假日数据保存到数据库
     */
    private void saveHolidaysToDatabase(List<Map<String, Object>> apiHolidays, Integer year, String region) {
        List<Holiday> holidays = new ArrayList<>();

        for (Map<String, Object> apiHoliday : apiHolidays) {
            try {
                Holiday holiday = new Holiday();
                holiday.setYear(year);
                holiday.setRegion(region);
                holiday.setDate(LocalDate.parse((String) apiHoliday.get("date")));
                holiday.setName((String) apiHoliday.get("name"));
                holiday.setNameCn((String) apiHoliday.get("name_cn"));
                holiday.setNameEn((String) apiHoliday.get("name_en"));

                String typeStr = (String) apiHoliday.get("type");
                holiday.setType(Holiday.HolidayType.valueOf(typeStr));

                holidays.add(holiday);
            } catch (Exception e) {
                log.warn("解析节假日数据失败，跳过该条记录: {}", apiHoliday, e);
            }
        }

        if (!holidays.isEmpty()) {
            holidayRepository.saveAll(holidays);
            log.info("成功保存{}条节假日数据到数据库", holidays.size());
        }
    }

    /**
     * 将Holiday实体转换为Map格式
     */
    private List<Map<String, Object>> convertHolidaysToMap(List<Holiday> holidays) {
        List<Map<String, Object>> result = new ArrayList<>();

        for (Holiday holiday : holidays) {
            Map<String, Object> holidayMap = new HashMap<>();
            holidayMap.put("date", holiday.getDate().toString());
            holidayMap.put("name", holiday.getName());
            holidayMap.put("name_cn", holiday.getNameCn());
            holidayMap.put("name_en", holiday.getNameEn());
            holidayMap.put("type", holiday.getType().name());
            result.add(holidayMap);
        }

        return result;
    }
}
