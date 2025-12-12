# 批量导入 API 实现指南

## 已完成的工作

### 1. CSV 解析功能
- ✅ 在 `ExcelParser` 中添加了 `parseCsvToMapList()` 方法
- ✅ 支持 UTF-8 BOM 标记
- ✅ 自动跳过空行和说明行

### 2. Service 接口
- ✅ `LeaveTypeService.batchImportLeaveTypes()`
- ✅ `MiscarriageTypeService.batchImportMiscarriageTypes()`
- ✅ `MaternityRulesService.batchImportMaternityRules()`

### 3. Service 实现
- ✅ `LeaveTypeServiceImpl.batchImportLeaveTypes()` 已实现

## 需要完成的工作

### 1. 实现 MiscarriageTypeServiceImpl.batchImportMiscarriageTypes()

```java
@Override
@Transactional
public int batchImportMiscarriageTypes(List<Map<String, Object>> dataList) {
    log.info("开始批量导入流产类型，共 {} 条数据", dataList.size());
    
    int successCount = 0;
    for (Map<String, Object> data : dataList) {
        try {
            String typeName = (String) data.get("流产类型");
            if (typeName == null || typeName.trim().isEmpty()) {
                log.warn("跳过空数据行");
                continue;
            }
            
            MiscarriageType miscarriageType = new MiscarriageType();
            miscarriageType.setTypeName(typeName.trim());
            miscarriageType.setIsActive(true);
            
            miscarriageTypeRepository.save(miscarriageType);
            successCount++;
        } catch (Exception e) {
            log.error("导入数据失败: {}", data, e);
        }
    }
    
    log.info("批量导入流产类型完成，成功 {} 条", successCount);
    return successCount;
}
```

**注意**: 需要在 `MiscarriageTypeServiceImpl` 中添加 `import java.util.Map;`

### 2. 实现 MaternityRulesServiceImpl.batchImportMaternityRules()

```java
@Override
@Transactional
public int batchImportMaternityRules(List<Map<String, Object>> dataList) {
    log.info("开始批量导入产假规则，共 {} 条数据", dataList.size());
    
    int successCount = 0;
    for (Map<String, Object> data : dataList) {
        try {
            String city = (String) data.get("城市");
            String leaveTypeName = (String) data.get("产假类型");
            String miscarriageTypeName = (String) data.get("流产类型");
            String leaveDaysStr = (String) data.get("产假天数");
            String isExtendableStr = (String) data.get("是否遇法定节假日顺延");
            String hasAllowanceStr = (String) data.get("是否享受津贴");
            String isDefaultStr = (String) data.get("是否默认");
            
            if (city == null || leaveTypeName == null || leaveDaysStr == null) {
                log.warn("跳过不完整的数据行: {}", data);
                continue;
            }
            
            MaternityRules maternityRules = new MaternityRules();
            maternityRules.setCity(city.trim());
            
            // 这里需要根据类型名称查找对应的ID
            // 简化实现：假设已经有对应的类型
            maternityRules.setLeaveTypeId(1); // TODO: 根据名称查找
            maternityRules.setMiscarriageTypeId("无".equals(miscarriageTypeName) ? null : 1); // TODO: 根据名称查找
            
            maternityRules.setLeaveDays(Integer.parseInt(leaveDaysStr.trim()));
            maternityRules.setIsExtendable("是".equals(isExtendableStr));
            maternityRules.setHasAllowance("是".equals(hasAllowanceStr));
            maternityRules.setIsDefault("是".equals(isDefaultStr));
            maternityRules.setRadioGroup(0);
            maternityRules.setIsActive(true);
            
            maternityRulesRepository.save(maternityRules);
            successCount++;
        } catch (Exception e) {
            log.error("导入数据失败: {}", data, e);
        }
    }
    
    log.info("批量导入产假规则完成，成功 {} 条", successCount);
    return successCount;
}
```

**注意**: 需要在 `MaternityRulesServiceImpl` 中添加 `import java.util.Map;`

### 3. 添加 Controller 导入 API

#### LeaveTypeController

```java
/**
 * 批量导入长假类型
 */
@PostMapping("/import")
@Operation(summary = "批量导入", description = "通过CSV文件批量导入长假类型")
public ApiResponse<String> importLeaveTypes(@RequestParam("file") MultipartFile file) throws IOException {
    log.info("收到批量导入长假类型请求，文件名: {}", file.getOriginalFilename());
    
    if (file.isEmpty()) {
        return ApiResponse.error("文件不能为空");
    }
    
    List<Map<String, Object>> dataList = ExcelParser.parseCsvToMapList(file.getInputStream());
    int successCount = leaveTypeService.batchImportLeaveTypes(dataList);
    
    return ApiResponse.success("导入完成，成功导入 " + successCount + " 条数据");
}
```

**需要添加的导入**:
```java
import com.ocbc.ms.easy.care.util.ExcelParser;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;
```

#### MiscarriageTypeController

```java
/**
 * 批量导入流产类型
 */
@PostMapping("/import")
@Operation(summary = "批量导入", description = "通过CSV文件批量导入流产类型")
public ApiResponse<String> importMiscarriageTypes(@RequestParam("file") MultipartFile file) throws IOException {
    log.info("收到批量导入流产类型请求，文件名: {}", file.getOriginalFilename());
    
    if (file.isEmpty()) {
        return ApiResponse.error("文件不能为空");
    }
    
    List<Map<String, Object>> dataList = ExcelParser.parseCsvToMapList(file.getInputStream());
    int successCount = miscarriageTypeService.batchImportMiscarriageTypes(dataList);
    
    return ApiResponse.success("导入完成，成功导入 " + successCount + " 条数据");
}
```

**需要添加的导入**:
```java
import com.ocbc.ms.easy.care.util.ExcelParser;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;
```

#### MaternityRulesController

```java
/**
 * 批量导入产假规则
 */
@PostMapping("/import")
@Operation(summary = "批量导入", description = "通过CSV文件批量导入产假规则")
public ApiResponse<String> importMaternityRules(@RequestParam("file") MultipartFile file) throws IOException {
    log.info("收到批量导入产假规则请求，文件名: {}", file.getOriginalFilename());
    
    if (file.isEmpty()) {
        return ApiResponse.error("文件不能为空");
    }
    
    List<Map<String, Object>> dataList = ExcelParser.parseCsvToMapList(file.getInputStream());
    int successCount = maternityRulesService.batchImportMaternityRules(dataList);
    
    return ApiResponse.success("导入完成，成功导入 " + successCount + " 条数据");
}
```

**需要添加的导入**:
```java
import com.ocbc.ms.easy.care.util.ExcelParser;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;
```

## API 使用方式

### 1. 下载模板
```bash
GET /api/leave-types/template/download
GET /api/miscarriage-types/template/download
GET /api/maternity-rules/template/download
```

### 2. 填写数据并导入
```bash
POST /api/leave-types/import
POST /api/miscarriage-types/import
POST /api/maternity-rules/import

Content-Type: multipart/form-data
Body: file=@长假类型导入模板.csv
```

## 测试流程

1. 调用下载模板 API，获取 CSV 模板
2. 在 CSV 文件中填写数据
3. 调用导入 API，上传填写好的 CSV 文件
4. 查看返回结果，确认导入成功的数据条数

## 注意事项

1. CSV 文件必须使用 UTF-8 编码
2. 表头必须与模板一致
3. 空行和说明行会自动跳过
4. 导入失败的行会记录日志，但不影响其他行的导入
5. 产假规则导入时，需要确保引用的长假类型和流产类型已存在
