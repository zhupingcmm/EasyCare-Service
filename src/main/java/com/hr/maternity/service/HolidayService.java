package com.hr.maternity.service;

import com.hr.maternity.domain.HolidayInfo;
import com.hr.maternity.dto.HolidayRequest;
import com.hr.maternity.dto.HolidayResponse;
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
    
    /**
     * 按日期范围获取节假日数据
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 节假日数据映射（日期 -> 节假日信息）
     */
    Map<LocalDate, HolidayInfo> getHolidaysByDateRange(LocalDate startDate, LocalDate endDate);
}
