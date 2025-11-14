package com.hr.maternity.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;

/**
 * 通用 Excel 解析工具类
 * 支持 .xls 和 .xlsx 格式
 */
@Slf4j
public class ExcelParser {

    /**
     * 解析 Excel 文件为 Map 列表
     * 
     * @param inputStream Excel 文件输入流
     * @param fileName 文件名（用于判断文件类型）
     * @return 每行数据的 Map 列表，key 为列名，value 为单元格值
     * @throws IOException 文件读取异常
     */
    public static List<Map<String, Object>> parseToMapList(InputStream inputStream, String fileName) throws IOException {
        log.info("开始解析 Excel 文件: {}", fileName);
        
        Workbook workbook = createWorkbook(inputStream, fileName);
        List<Map<String, Object>> result = new ArrayList<>();
        
        try {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null || sheet.getPhysicalNumberOfRows() == 0) {
                log.warn("Excel 文件为空或第一个工作表不存在");
                return result;
            }
            
            // 读取表头
            Row headerRow = sheet.getRow(sheet.getFirstRowNum());
            if (headerRow == null) {
                log.warn("Excel 文件表头为空");
                return result;
            }
            
            List<String> headers = extractHeaders(headerRow);
            log.debug("解析到表头: {}", headers);
            
            // 读取数据行
            for (int i = sheet.getFirstRowNum() + 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isEmptyRow(row)) {
                    continue;
                }
                
                Map<String, Object> rowData = new LinkedHashMap<>();
                for (int j = 0; j < headers.size(); j++) {
                    String header = headers.get(j);
                    Cell cell = row.getCell(j, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                    Object cellValue = getCellValue(cell);
                    rowData.put(header, cellValue);
                }
                result.add(rowData);
            }
            
            log.info("Excel 解析完成，共解析 {} 行数据", result.size());
            
        } finally {
            workbook.close();
        }
        
        return result;
    }

    /**
     * 解析 Excel 文件为指定类型的对象列表
     * 
     * @param inputStream Excel 文件输入流
     * @param fileName 文件名
     * @param mapper 将 Map 转换为目标对象的函数
     * @param <T> 目标对象类型
     * @return 对象列表
     * @throws IOException 文件读取异常
     */
    public static <T> List<T> parseToObjectList(InputStream inputStream, String fileName, 
                                                  Function<Map<String, Object>, T> mapper) throws IOException {
        List<Map<String, Object>> mapList = parseToMapList(inputStream, fileName);
        List<T> result = new ArrayList<>();
        
        for (int i = 0; i < mapList.size(); i++) {
            try {
                T obj = mapper.apply(mapList.get(i));
                if (obj != null) {
                    result.add(obj);
                }
            } catch (Exception e) {
                log.error("转换第 {} 行数据失败: {}", i + 2, e.getMessage());
            }
        }
        
        return result;
    }

    /**
     * 根据文件名创建对应的 Workbook
     */
    private static Workbook createWorkbook(InputStream inputStream, String fileName) throws IOException {
        if (fileName.toLowerCase().endsWith(".xlsx")) {
            return new XSSFWorkbook(inputStream);
        } else if (fileName.toLowerCase().endsWith(".xls")) {
            return new HSSFWorkbook(inputStream);
        } else {
            throw new IllegalArgumentException("不支持的文件格式，仅支持 .xls 和 .xlsx 格式");
        }
    }

    /**
     * 提取表头
     */
    private static List<String> extractHeaders(Row headerRow) {
        List<String> headers = new ArrayList<>();
        for (Cell cell : headerRow) {
            String header = getCellValueAsString(cell);
            headers.add(header != null ? header.trim() : "");
        }
        return headers;
    }

    /**
     * 判断行是否为空
     */
    private static boolean isEmptyRow(Row row) {
        for (Cell cell : row) {
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String value = getCellValueAsString(cell);
                if (value != null && !value.trim().isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 获取单元格值（自动识别类型）
     */
    private static Object getCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    Date date = cell.getDateCellValue();
                    return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                } else {
                    double numericValue = cell.getNumericCellValue();
                    // 判断是否为整数
                    if (numericValue == Math.floor(numericValue)) {
                        return (long) numericValue;
                    }
                    return numericValue;
                }
            case BOOLEAN:
                return cell.getBooleanCellValue();
            case FORMULA:
                try {
                    return cell.getNumericCellValue();
                } catch (Exception e) {
                    return cell.getStringCellValue();
                }
            case BLANK:
                return null;
            default:
                return cell.toString();
        }
    }

    /**
     * 获取单元格值（字符串形式）
     */
    private static String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return null;
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    Date date = cell.getDateCellValue();
                    LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    return localDate.toString();
                } else {
                    double numericValue = cell.getNumericCellValue();
                    if (numericValue == Math.floor(numericValue)) {
                        return String.valueOf((long) numericValue);
                    }
                    return String.valueOf(numericValue);
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return String.valueOf(cell.getNumericCellValue());
                } catch (Exception e) {
                    return cell.getStringCellValue();
                }
            case BLANK:
                return null;
            default:
                return cell.toString();
        }
    }

    /**
     * 从 Map 中安全获取字符串值
     */
    public static String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString().trim() : null;
    }

    /**
     * 从 Map 中安全获取整数值
     */
    public static Integer getIntegerValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(value.toString().trim());
        } catch (NumberFormatException e) {
            log.warn("无法将值 '{}' 转换为整数", value);
            return null;
        }
    }

    /**
     * 从 Map 中安全获取布尔值
     */
    public static Boolean getBooleanValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        String strValue = value.toString().trim().toLowerCase();
        return "true".equals(strValue) || "是".equals(strValue) || "1".equals(strValue);
    }

    /**
     * 从 Map 中安全获取日期值
     */
    public static LocalDate getLocalDateValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDate) {
            return (LocalDate) value;
        }
        try {
            return LocalDate.parse(value.toString().trim());
        } catch (Exception e) {
            log.warn("无法将值 '{}' 转换为日期", value);
            return null;
        }
    }
}
