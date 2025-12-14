package com.ocbc.ms.easy.care.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

/**
 * Excel 导出工具类
 */
@Slf4j
public class ExcelExporter {

    /**
     * 创建长假类型导入模板
     * 
     * @return CSV 文件字节数组
     * @throws IOException IO异常
     */
    public static byte[] createLeaveTypeTemplate() throws IOException {
        log.info("开始创建长假类型导入模板");
        
        String[] headers = {
            "产假类型"
        };
        
        String[][] exampleData = {
            {"法定产假"},
            {"陪产假"}
        };
        
        String[] notes = {
            "说明：",
            "1. 产假类型：填写类型名称，如：产假、陪产假、育儿假等"
        };
        
        byte[] csvBytes = createCsvTemplate(headers, exampleData, notes);
        log.info("长假类型导入模板创建成功");
        
        return csvBytes;
    }

    /**
     * 创建流产类型导入模板
     * 
     * @return CSV 文件字节数组
     * @throws IOException IO异常
     */
    public static byte[] createMiscarriageTypeTemplate() throws IOException {
        log.info("开始创建流产类型导入模板");
        
        String[] headers = {
            "流产类型"
        };
        
        String[][] exampleData = {
            {"早期流产"},
            {"晚期流产"}
        };
        
        String[] notes = {
            "说明：",
            "1. 流产类型：填写类型名称，如：早期流产、晚期流产等"
        };
        
        byte[] csvBytes = createCsvTemplate(headers, exampleData, notes);
        log.info("流产类型导入模板创建成功");
        
        return csvBytes;
    }

    /**
     * 创建产假规则导入模板
     * 
     * @return CSV 文件字节数组
     * @throws IOException IO异常
     */
    public static byte[] createMaternityRulesTemplate() throws IOException {
        log.info("开始创建产假规则导入模板");
        
        String[] headers = {
            "城市",
            "产假类型",
            "流产类型",
            "产假天数",
            "是否遇法定节假日顺延",
            "是否享受津贴",
            "是否默认"
        };
        
        String[][] exampleData = {
            {"上海", "产假", "无", "158", "是", "是", "是"}
        };
        
        String[] notes = {
            "说明：",
            "1. 城市：填写城市名称，如：上海、北京",
            "2. 产假类型：填写产假类型名称，如：产假、陪产假",
            "3. 流产类型：填写流产类型名称，如：早期流产、晚期流产，无流产情况填写\"无\"",
            "4. 产假天数：填写数字，如：158",
            "5. 是否遇法定节假日顺延：填写\"是\"或\"否\"",
            "6. 是否享受津贴：填写\"是\"或\"否\"",
            "7. 是否默认：填写\"是\"或\"否\""
        };
        
        byte[] csvBytes = createCsvTemplate(headers, exampleData, notes);
        log.info("产假规则导入模板创建成功");
        
        return csvBytes;
    }

    /**
     * 创建津贴规则导入模板
     * 
     * @return CSV 文件字节数组
     * @throws IOException IO异常
     */
    public static byte[] createAllowanceRulesTemplate() throws IOException {
        log.info("开始创建津贴规则导入模板");
        
        String[] headers = {
            "城市",
            "津贴发放方式"
        };
        
        String[][] exampleData = {
            {"上海", "个人"}
        };
        
        String[] notes = {
            "说明：",
            "1. 城市：填写城市名称，如：上海、北京",
            "2. 津贴发放方式：填写发放方式，如：个人、企业"
        };
        
        byte[] csvBytes = createCsvTemplate(headers, exampleData, notes);
        log.info("津贴规则导入模板创建成功");
        
        return csvBytes;
    }
    
    /**
     * 创建节假日导入模板
     * 
     * @return CSV 文件字节数组
     * @throws IOException IO异常
     */
    public static byte[] createHolidayTemplate() throws IOException {
        log.info("开始创建节假日导入模板");
        
        String[] headers = {
            "日期",
            "节日名称",
            "类型",
            "是否为法定假日"
        };
        
        String[][] exampleData = {
            {"2025-01-01", "元旦", "public_holiday", "是"},
            {"2025-01-28", "春节", "public_holiday", "是"},
            {"2025-01-26", "春节调休", "transfer_workday", "否"}
        };
        
        String[] notes = {
            "说明：",
            "1. 日期：填写日期，格式：YYYY-MM-DD，如：2025-01-01",
            "2. 节日名称：填写节日名称",
            "3. 类型：填写类型，可选值：public_holiday（公共假日）、transfer_workday（调休工作日）",
            "4. 是否为法定假日：填写是或否"
        };
        
        byte[] csvBytes = createCsvTemplate(headers, exampleData, notes);
        log.info("节假日导入模板创建成功");
        
        return csvBytes;
    }
    
    /**
     * 创建通用 Excel 模板
     * 
     * @param sheetName 工作表名称
     * @param headers 表头数组
     * @param exampleData 示例数据（可选，传null表示不需要示例数据）
     * @param notes 说明文字数组（可选，传null表示不需要说明）
     * @param columnWidth 列宽（字符数）
     * @return Excel 文件字节数组
     * @throws IOException IO异常
     */
    public static byte[] createExcelTemplate(String sheetName, 
                                            String[] headers, 
                                            String[][] exampleData, 
                                            String[] notes, 
                                            int columnWidth) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet(sheetName);
            
            // 创建表头样式
            CellStyle headerStyle = createHeaderStyle(workbook);
            
            // 创建表头
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                
                // 设置列宽（字符数 * 256）
                sheet.setColumnWidth(i, columnWidth * 256);
            }
            
            int currentRowNum = 1;
            
            // 创建示例数据行（可选）
            if (exampleData != null) {
                for (String[] rowData : exampleData) {
                    Row exampleRow = sheet.createRow(currentRowNum++);
                    for (int i = 0; i < rowData.length && i < headers.length; i++) {
                        exampleRow.createCell(i).setCellValue(rowData[i]);
                    }
                }
            }
            
            // 添加说明行（可选）
            if (notes != null && notes.length > 0) {
                // 空一行
                currentRowNum++;
                
                for (String note : notes) {
                    Row noteRow = sheet.createRow(currentRowNum++);
                    noteRow.createCell(0).setCellValue(note);
                }
            }
            
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }
    
    /**
     * 创建通用 CSV 模板
     * 
     * @param headers 表头数组
     * @param exampleData 示例数据（可选，传null表示不需要示例数据）
     * @param notes 说明文字数组（可选，传null表示不需要说明）
     * @return CSV 文件字节数组
     * @throws IOException IO异常
     */
    public static byte[] createCsvTemplate(String[] headers, 
                                          String[][] exampleData, 
                                          String[] notes) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
            
            // 添加 UTF-8 BOM，确保 Excel 正确识别中文
            outputStream.write(0xEF);
            outputStream.write(0xBB);
            outputStream.write(0xBF);
            
            // 写入表头
            writer.write(String.join(",", headers));
            writer.write("\n");
            
            // 写入示例数据（可选）
            if (exampleData != null) {
                for (String[] rowData : exampleData) {
                    writer.write(String.join(",", rowData));
                    writer.write("\n");
                }
            }
            
            // 写入说明（可选）
            if (notes != null && notes.length > 0) {
                writer.write("\n");
                for (String note : notes) {
                    writer.write(note);
                    writer.write("\n");
                }
            }
            
            writer.flush();
            return outputStream.toByteArray();
        }
    }

    /**
     * 创建表头样式
     */
    private static CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        
        // 设置背景色
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        // 设置边框
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        
        // 设置字体
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        
        // 设置居中
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        
        return style;
    }
}
