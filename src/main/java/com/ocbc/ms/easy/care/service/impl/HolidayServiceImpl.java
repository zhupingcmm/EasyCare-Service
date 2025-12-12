package com.ocbc.ms.easy.care.service.impl;

import com.ocbc.ms.easy.care.domain.HolidayInfo;
import com.ocbc.ms.easy.care.dto.HolidayRequest;
import com.ocbc.ms.easy.care.dto.HolidayResponse;
import com.ocbc.ms.easy.care.entity.Holiday;
import com.ocbc.ms.easy.care.repository.HolidayRepository;
import com.ocbc.ms.easy.care.service.HolidayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

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

    private List<Map<String, Object>> convertHolidaysToMap(List<Holiday> holidays) {
        return holidays.stream()
                .map(this::convertHolidayToMap)
                .collect(Collectors.toList());
    }

    private Map<String, Object> convertHolidayToMap(Holiday holiday) {
        Map<String, Object> map = new HashMap<>();
        map.put("date", holiday.getDate().toString());
        map.put("name", holiday.getName());
        map.put("name_cn", holiday.getCnName());
        map.put("name_en", holiday.getEnName());
        map.put("type", resolveHolidayType(holiday));
        map.put("isPublicHoliday", holiday.getIsPublicHoliday());
        return map;
    }

    private String resolveHolidayType(Holiday holiday) {
        Integer type = holiday.getType();
        if (Objects.equals(type, Holiday.SpecialDayType.HOLIDAY)) {
            return "public_holiday";
        }
        if (Objects.equals(type, Holiday.SpecialDayType.WORKDAY)) {
            return "transfer_workday";
        }
        return Boolean.TRUE.equals(holiday.getIsPublicHoliday()) ? "public_holiday" : "transfer_workday";
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
                LocalDate date = LocalDate.parse((String) apiHoliday.get("date"));
                holiday.setDate(date);
                holiday.setYear(date.getYear());
                holiday.setRegion("CN");
                
                String name = (String) apiHoliday.get("name");
                holiday.setName(name);
                holiday.setCnName(name);
                holiday.setEnName(name);

                String typeStr = (String) apiHoliday.get("type");
                // 将字符串类型转换为整数：public_holiday=1, transfer_workday=2
                Integer type = "public_holiday".equals(typeStr) ? Holiday.SpecialDayType.HOLIDAY : Holiday.SpecialDayType.WORKDAY;
                holiday.setType(type);
                holiday.setIsPublicHoliday("public_holiday".equals(typeStr));
                holiday.setEnabled(true);

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


    @Override
    @Transactional
    public HolidayResponse createHoliday(HolidayRequest request) {
        log.info("开始创建特殊日期，请求参数: {}", request);

        Holiday holiday = new Holiday();
        holiday.setYear(request.getYear());
        holiday.setRegion(request.getRegion());
        holiday.setDate(request.getDate());
        holiday.setName(request.getName());
        holiday.setCnName(request.getCnName());
        holiday.setEnName(request.getEnName());
        holiday.setType(request.getType());
        holiday.setIsPublicHoliday(request.getIsPublicHoliday());
        holiday.setEnabled(request.getEnabled());

        Holiday saved = holidayRepository.save(holiday);
        log.info("特殊日期创建成功，ID: {}", saved.getId());

        return convertToResponse(saved);
    }

    @Override
    public Page<HolidayResponse> listAllHolidays(Pageable pageable) {
        log.info("查询所有启用的特殊日期，分页参数: {}", pageable);

        // 只查询启用的数据
        Page<Holiday> holidayPage = holidayRepository.findByEnabled(true, pageable);
        
        return holidayPage.map(this::convertToResponse);
    }

    @Override
    @Transactional
    public HolidayResponse updateHoliday(UUID id, HolidayRequest request) {
        log.info("更新特殊日期，ID: {}, 请求参数: {}", id, request);

        Holiday holiday = holidayRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("特殊日期不存在，ID: " + id));

        holiday.setYear(request.getYear());
        holiday.setRegion(request.getRegion());
        holiday.setDate(request.getDate());
        holiday.setName(request.getName());
        holiday.setCnName(request.getCnName());
        holiday.setEnName(request.getEnName());
        holiday.setType(request.getType());
        holiday.setIsPublicHoliday(request.getIsPublicHoliday());
        holiday.setEnabled(request.getEnabled());

        Holiday updated = holidayRepository.save(holiday);
        log.info("特殊日期更新成功，ID: {}", updated.getId());

        return convertToResponse(updated);
    }


    @Override
    @Transactional
    public void deleteHoliday(UUID id) {
        log.info("禁用特殊日期，ID: {}", id);

        Holiday holiday = holidayRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("特殊日期不存在，ID: " + id));

        holiday.setEnabled(false);
        holidayRepository.save(holiday);
        log.info("特殊日期禁用成功，ID: {}", id);
    }

    @Override
    @Transactional
    public int batchImportHolidays(List<Map<String, Object>> dataList) {
        log.info("开始批量导入节假日，共 {} 条数据", dataList.size());
        
        int successCount = 0;
        int updateCount = 0;
        int skipCount = 0;
        
        for (Map<String, Object> data : dataList) {
            try {
                String dateStr = (String) data.get("日期");
                String name = (String) data.get("节假日名称");
                String cnName = (String) data.get("中文名称");
                String enName = (String) data.get("英文名称");
                String typeStr = (String) data.get("类型");
                String isPublicHolidayStr = (String) data.get("是否国定假日");
                
                // 验证必填字段
                if (dateStr == null || dateStr.trim().isEmpty() ||
                    name == null || name.trim().isEmpty() ||
                    typeStr == null || typeStr.trim().isEmpty()) {
                    log.warn("跳过不完整的数据行: {}", data);
                    skipCount++;
                    continue;
                }
                
                // 解析数据
                LocalDate date = LocalDate.parse(dateStr.trim());
                
                Integer type;
                try {
                    type = Integer.parseInt(typeStr.trim());
                    if (type != 1 && type != 2) {
                        throw new IllegalArgumentException("类型必须为1或2");
                    }
                } catch (Exception e) {
                    log.warn("无效的类型: {}，跳过该数据行", typeStr);
                    skipCount++;
                    continue;
                }
                
                Boolean isPublicHoliday = "是".equals(isPublicHolidayStr);
                
                // 检查是否存在相同的记录（根据日期）
                Optional<Holiday> existingHoliday = holidayRepository.findByDate(date);
                
                Holiday holiday;
                boolean isUpdate = false;
                
                if (existingHoliday.isPresent()) {
                    // 更新现有记录
                    holiday = existingHoliday.get();
                    isUpdate = true;
                } else {
                    // 创建新记录
                    holiday = new Holiday();
                    holiday.setDate(date);
                }
                
                // 设置或更新字段
                holiday.setYear(date.getYear());
                holiday.setRegion("CN");
                holiday.setName(name.trim());
                holiday.setCnName(cnName != null ? cnName.trim() : name.trim());
                holiday.setEnName(enName != null ? enName.trim() : name.trim());
                holiday.setType(type);
                holiday.setIsPublicHoliday(isPublicHoliday);
                holiday.setEnabled(true);
                
                holidayRepository.save(holiday);
                
                if (isUpdate) {
                    updateCount++;
                    log.debug("更新节假日: 日期={}, 名称={}", date, name);
                } else {
                    successCount++;
                    log.debug("创建节假日: 日期={}, 名称={}", date, name);
                }
            } catch (Exception e) {
                log.error("导入数据失败: {}", data, e);
                skipCount++;
            }
        }
        
        log.info("批量导入节假日完成，新增 {} 条，更新 {} 条，跳过 {} 条", successCount, updateCount, skipCount);
        return successCount + updateCount;
    }

    @Override
    public byte[] generateCsvFromPublicApi(String year) throws Exception {
        log.info("从公网API获取{}年节假日数据并生成CSV", year);
        
        // 1. 从公网API获取数据
        List<Map<String, Object>> apiHolidays = fetchHolidaysFromApi(year);
        
        if (apiHolidays.isEmpty()) {
            throw new IllegalArgumentException("未能获取到" + year + "年的节假日数据");
        }
        
        // 2. 构建CSV内容
        StringBuilder csv = new StringBuilder();
        
        // 添加UTF-8 BOM
        csv.append('\ufeff');
        
        // 添加表头
        csv.append("日期,节假日名称,类型,是否为法定假日\n");
        
        // 添加数据行
        for (Map<String, Object> holiday : apiHolidays) {
            String date = (String) holiday.get("date");
            String name = (String) holiday.get("name");
            String type = (String) holiday.get("type");
            
            // 判断是否为法定假日（public_holiday为法定假日）
            String isStatutory = "public_holiday".equals(type) ? "是" : "否";
            
            csv.append(date).append(",")
               .append(name).append(",")
               .append(type).append(",")
               .append(isStatutory).append("\n");
        }
        
        log.info("成功生成CSV文件，共{}条数据", apiHolidays.size());
        return csv.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    /**
     * 转换为响应DTO
     */
    private HolidayResponse convertToResponse(Holiday holiday) {
        return HolidayResponse.builder()
                .id(holiday.getId())
                .year(holiday.getYear())
                .region(holiday.getRegion())
                .date(holiday.getDate())
                .name(holiday.getName())
                .cnName(holiday.getCnName())
                .enName(holiday.getEnName())
                .type(holiday.getType())
                .isPublicHoliday(holiday.getIsPublicHoliday())
                .enabled(holiday.getEnabled())
                .createDate(holiday.getCreateDate())
                .createBy(holiday.getCreateBy())
                .updateDate(holiday.getUpdateDate())
                .updateBy(holiday.getUpdateBy())
                .build();
    }
    
    @Override
    public Map<LocalDate, HolidayInfo> getHolidaysByDateRange(LocalDate startDate, LocalDate endDate) {
        LocalDate firstDateInStartMonth = startDate.withDayOfMonth(1);
        LocalDate lastDateInEndMonth = endDate.withDayOfMonth(endDate.lengthOfMonth());

        log.info("获取日期范围内的节假日数据: {} 到 {}", firstDateInStartMonth, lastDateInEndMonth);
        
        // 从数据库查询
        List<Holiday> holidays = holidayRepository.findByDateBetweenOrderByDate(firstDateInStartMonth, lastDateInEndMonth);
        
        // 转换为Map
        Map<LocalDate, HolidayInfo> holidayMap = holidays.stream()
                .collect(Collectors.toMap(
                        Holiday::getDate,
                        this::convertToHolidayInfo
                ));
        
        log.info("获取到{}条节假日数据", holidayMap.size());
        return holidayMap;
    }
    
    /**
     * 转换为HolidayInfo
     */
    private HolidayInfo convertToHolidayInfo(Holiday holiday) {
        return HolidayInfo.builder()
                .date(holiday.getDate())
                .name(holiday.getName())
                .isPublicHoliday(holiday.getIsPublicHoliday())
                .type(resolveHolidayType(holiday))
                .build();
    }
}
