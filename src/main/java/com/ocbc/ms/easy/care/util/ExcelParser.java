package com.ocbc.ms.easy.care.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
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
     * 解析 CSV 文件为 Map 列表
     * 
     * @param inputStream CSV 文件输入流
     * @return 每行数据的 Map 列表，key 为列名，value 为单元格值
     * @throws IOException 文件读取异常
     */
    public static List<Map<String, Object>> parseCsvToMapList(InputStream inputStream) throws IOException {
        log.info("开始解析 CSV 文件");
        
        List<Map<String, Object>> result = new ArrayList<>();
        
        // 先读取文件内容到字节数组，以便尝试不同编码
        byte[] bytes = inputStream.readAllBytes();
        
        // 尝试检测编码：UTF-8 with BOM, UTF-8, GBK
        String content = null;
        if (bytes.length >= 3 && bytes[0] == (byte)0xEF && bytes[1] == (byte)0xBB && bytes[2] == (byte)0xBF) {
            // UTF-8 with BOM
            content = new String(bytes, 3, bytes.length - 3, StandardCharsets.UTF_8);
            log.debug("检测到 UTF-8 BOM 编码");
        } else {
            // 尝试 UTF-8
            String utf8Content = new String(bytes, StandardCharsets.UTF_8);
            // 检查是否包含中文乱码特征
            if (utf8Content.contains("�") || !isValidUtf8(bytes)) {
                // 尝试 GBK
                try {
                    content = new String(bytes, "GBK");
                    log.debug("使用 GBK 编码解析");
                } catch (Exception e) {
                    content = utf8Content;
                    log.debug("使用 UTF-8 编码解析");
                }
            } else {
                content = utf8Content;
                log.debug("使用 UTF-8 编码解析");
            }
        }
        
        // 按行分割
        String[] lines = content.split("\\r?\\n");
        if (lines.length == 0) {
            log.warn("CSV 文件为空");
            return result;
        }
        
        // 读取表头
        String headerLine = lines[0];
        if (headerLine.trim().isEmpty()) {
            log.warn("CSV 文件表头为空");
            return result;
        }
        
        String[] headers = headerLine.split(",");
        log.debug("解析到表头: {}", Arrays.toString(headers));
        
        // 读取数据行
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i];
            // 跳过空行和说明行
            if (line.trim().isEmpty() || line.startsWith("说明") || line.matches("^\\d+\\..*")) {
                continue;
            }
            
            String[] values = line.split(",", -1); // -1 保留空字段
            if (values.length > 0) {
                Map<String, Object> rowData = new LinkedHashMap<>();
                for (int j = 0; j < headers.length; j++) {
                    String header = headers[j].trim();
                    String value = j < values.length ? values[j].trim() : "";
                    rowData.put(header, value.isEmpty() ? null : value);
                }
                result.add(rowData);
            }
        }
        
        log.info("CSV 解析完成，共解析 {} 行数据", result.size());
        return result;
    }
    
    /**
     * 检查字节数组是否是有效的 UTF-8 编码
     */
    private static boolean isValidUtf8(byte[] bytes) {
        try {
            String str = new String(bytes, StandardCharsets.UTF_8);
            byte[] reEncoded = str.getBytes(StandardCharsets.UTF_8);
            return Arrays.equals(bytes, reEncoded);
        } catch (Exception e) {
            return false;
        }
    }

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
