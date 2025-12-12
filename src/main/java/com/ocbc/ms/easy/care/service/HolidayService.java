package com.ocbc.ms.easy.care.service;

import com.ocbc.ms.easy.care.dto.HolidayRequest;
import com.ocbc.ms.easy.care.dto.HolidayResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 节假日服务接口
 */
public interface HolidayService {
    
    /**
     * 获取指定年份的公共假日
     * @param year 年份
     * @return 公共假日列表
     */
    List<Map<String, Object>> getPublicHolidays(String year);

    void initHoliday(int year);

    Set<LocalDate> getAdditionalWorkDay();
    Set<LocalDate> getHoliday();
    Map<LocalDate, Map<String, Object>> getDayInfoMap();

    /**
     * 创建节假日
     */
    HolidayResponse createHoliday(HolidayRequest request);

    /**
     * 查询所有节假日（分页）
     */
    Page<HolidayResponse> listAllHolidays(Pageable pageable);



    /**
     * 更新特殊日期
     */
    HolidayResponse updateHoliday(UUID id, HolidayRequest request);

    /**
     * 禁用特殊日期
     */
    void deleteHoliday(UUID id);

    /**
     * 批量导入节假日
     * 
     * @param dataList CSV数据列表
     * @return 成功导入的数量
     */
    int batchImportHolidays(List<Map<String, Object>> dataList);

    /**
     * 从公网API获取节假日数据并生成CSV文件
     * 
     * @param year 年份
     * @return CSV文件字节数组
     */
    byte[] generateCsvFromPublicApi(String year) throws Exception;
}
