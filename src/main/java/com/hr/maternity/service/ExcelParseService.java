package com.hr.maternity.service;

import com.hr.maternity.dto.MaternityTestCaseRowDTO;
import com.hr.maternity.util.ExcelParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Excel 解析服务
 * 负责将 Excel 文件解析为 DTO 对象
 */
@Slf4j
@Service
public class ExcelParseService {

    /**
     * 解析产假津贴测试用例 Excel 文件
     * 
     * @param file Excel 文件
     * @return 测试用例行数据列表
     * @throws IOException 文件读取异常
     */
    public List<MaternityTestCaseRowDTO> parseMaternityTestCaseFile(MultipartFile file) throws IOException {
        log.info("开始解析产假津贴测试用例 Excel 文件: {}", file.getOriginalFilename());
        
        // 使用 ExcelParser 解析为 Map 列表
        List<Map<String, Object>> rawData = ExcelParser.parseToMapList(
            file.getInputStream(),
            file.getOriginalFilename()
        );
        
        log.debug("Excel 解析完成，共 {} 行原始数据", rawData.size());
        
        // 将 Map 转换为 DTO
        List<MaternityTestCaseRowDTO> testCases = rawData.stream()
            .map(rowData -> {
                try {
                    return MaternityTestCaseRowDTO.fromMap(rowData);
                } catch (Exception e) {
                    log.error("解析行数据失败: {}", rowData, e);
                    return null;
                }
            })
            .filter(dto -> dto != null)
            .collect(Collectors.toList());
        
        log.info("成功解析 {} 个测试用例", testCases.size());
        
        return testCases;
    }
    
    /**
     * 验证 Excel 文件格式
     * 
     * @param file 文件
     * @return 是否有效
     */
    public boolean isValidExcelFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            log.warn("文件为空");
            return false;
        }
        
        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            log.warn("文件名为空");
            return false;
        }
        
        boolean isValid = fileName.endsWith(".xls") || fileName.endsWith(".xlsx");
        if (!isValid) {
            log.warn("不支持的文件格式: {}", fileName);
        }
        
        return isValid;
    }
}
