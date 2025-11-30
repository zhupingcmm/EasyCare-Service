# ExcelExporter 使用说明

## 概述

`ExcelExporter` 是一个通用的 Excel 导出工具类，提供了创建 Excel 模板的公共方法，可以被项目中的其他部分复用。

## 核心方法

### 1. createExcelTemplate (公共方法)

创建通用 Excel 模板的核心方法，其他模块可以直接调用。

**方法签名：**
```java
public static byte[] createExcelTemplate(
    String sheetName,      // 工作表名称
    String[] headers,      // 表头数组
    String[][] exampleData, // 示例数据（可选，传null表示不需要）
    String[] notes,        // 说明文字数组（可选，传null表示不需要）
    int columnWidth        // 列宽（字符数）
) throws IOException
```

**参数说明：**
- `sheetName`: Excel 工作表的名称
- `headers`: 表头字段数组，如 `["姓名", "年龄", "城市"]`
- `exampleData`: 示例数据的二维数组，如 `[["张三", "25", "北京"], ["李四", "30", "上海"]]`
  - 传 `null` 表示不需要示例数据
- `notes`: 说明文字数组，每个元素占一行，如 `["说明：", "1. 姓名必填", "2. 年龄为数字"]`
  - 传 `null` 表示不需要说明
- `columnWidth`: 列宽度（字符数），会自动转换为 Excel 的列宽单位

**返回值：**
- `byte[]`: Excel 文件的字节数组

## 使用示例

### 示例 1：创建产假规则导入模板（已实现）

```java
public static byte[] createMaternityRulesTemplate() throws IOException {
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
    
    return createExcelTemplate("产假规则", headers, exampleData, notes, 20);
}
```

### 示例 2：创建员工信息导入模板

```java
public static byte[] createEmployeeTemplate() throws IOException {
    String[] headers = {
        "工号",
        "姓名",
        "部门",
        "职位",
        "入职日期"
    };
    
    String[][] exampleData = {
        {"E001", "张三", "技术部", "工程师", "2024-01-01"},
        {"E002", "李四", "人事部", "HR", "2024-02-01"}
    };
    
    String[] notes = {
        "填写说明：",
        "1. 工号：必填，格式为E+3位数字",
        "2. 姓名：必填",
        "3. 部门：必填",
        "4. 职位：必填",
        "5. 入职日期：必填，格式为YYYY-MM-DD"
    };
    
    return ExcelExporter.createExcelTemplate("员工信息", headers, exampleData, notes, 15);
}
```

### 示例 3：创建简单模板（无示例数据和说明）

```java
public static byte[] createSimpleTemplate() throws IOException {
    String[] headers = {"姓名", "年龄", "性别"};
    
    // 不需要示例数据和说明，传 null
    return ExcelExporter.createExcelTemplate("用户信息", headers, null, null, 15);
}
```

### 示例 4：在 Controller 中使用

```java
@GetMapping("/template/employee")
@Operation(summary = "下载员工导入模板")
public ResponseEntity<byte[]> downloadEmployeeTemplate() throws IOException {
    byte[] excelBytes = createEmployeeTemplate();
    
    String fileName = "员工信息导入模板.xlsx";
    String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                                      .replace("+", "%20");

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
    headers.setContentDispositionFormData("attachment", encodedFileName);
    headers.setContentLength(excelBytes.length);
    
    return ResponseEntity.ok()
            .headers(headers)
            .body(excelBytes);
}
```

## 特性说明

### 1. 表头样式
- 灰色背景（`GREY_25_PERCENT`）
- 加粗字体（12号）
- 居中对齐
- 带边框

### 2. 自动列宽
- 根据 `columnWidth` 参数自动设置列宽
- 单位为字符数，内部会转换为 Excel 的列宽单位（字符数 × 256）

### 3. 示例数据（可选）
- 支持多行示例数据
- 从第2行开始填充
- 如果不需要示例数据，传 `null` 即可

### 4. 说明文字（可选）
- 在示例数据后空一行开始
- 每个元素占一行
- 如果不需要说明，传 `null` 即可

## 注意事项

1. **文件格式**：生成的是 `.xlsx` 格式（Excel 2007+）
2. **字符编码**：支持中文，文件名需要使用 `URLEncoder` 编码
3. **异常处理**：方法会抛出 `IOException`，调用方需要处理
4. **内存管理**：使用 try-with-resources 自动关闭资源

## 扩展建议

如果需要更复杂的样式或功能，可以：
1. 添加更多样式参数（如字体颜色、背景色等）
2. 支持数据验证（下拉列表等）
3. 支持单元格合并
4. 支持公式
5. 支持多个工作表

可以在 `ExcelExporter` 类中添加新的公共方法来实现这些功能。
