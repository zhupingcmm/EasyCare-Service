# 产假规则管理 API 文档

## 基础信息

**Base URL**: `/api/maternity-rules`

**描述**: 产假规则的增删改查接口

---

## 接口列表

### 1. 创建产假规则

**接口**: `POST /api/maternity-rules`

**描述**: 创建新的产假规则

**请求参数**:

```json
{
  "city": "上海",
  "maternityLeaveType": "产假",
  "abortionLeaveType": null,
  "leaveDays": 158,
  "isExtendable": true,
  "hasAllowance": true,
  "isDefault": true,
  "radioGroup": 0,
  "isActive": true
}
```

**参数说明**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| city | String | 是 | 城市名称 |
| maternityLeaveType | String | 是 | 产假类型（如：产假、陪产假） |
| abortionLeaveType | String | 否 | 流产类型（如：早期流产、晚期流产） |
| leaveDays | Integer | 是 | 假期天数（必须大于0） |
| isExtendable | Boolean | 是 | 是否节假日顺延 |
| hasAllowance | Boolean | 是 | 是否有津贴 |
| isDefault | Boolean | 是 | 是否默认选择 |
| radioGroup | Integer | 是 | 单选分组标识 |
| isActive | Boolean | 否 | 是否启用（默认true） |

**响应示例**:

```json
{
  "code": 0,
  "message": "OK",
  "data": {
    "id": 1,
    "city": "上海",
    "maternityLeaveType": "产假",
    "abortionLeaveType": null,
    "leaveDays": 158,
    "isExtendable": true,
    "hasAllowance": true,
    "isDefault": true,
    "radioGroup": 0,
    "isActive": true,
    "createDate": "2025-11-30T23:00:00",
    "createBy": "system",
    "updateDate": "2025-11-30T23:00:00",
    "updateBy": "system"
  }
}
```

---

### 2. 分页查询产假规则

**接口**: `GET /api/maternity-rules`

**描述**: 分页查询所有产假规则，支持按城市过滤

**查询参数**:

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| city | String | 否 | - | 城市名称（用于过滤） |
| page | Integer | 否 | 0 | 页码（从0开始） |
| size | Integer | 否 | 10 | 每页数量 |
| sort | String | 否 | updateDate | 排序字段 |
| direction | String | 否 | DESC | 排序方向（ASC/DESC） |

**请求示例**:

```bash
# 查询所有产假规则
GET /api/maternity-rules?page=0&size=10

# 按城市过滤
GET /api/maternity-rules?city=上海&page=0&size=10
```

**响应示例**:

```json
{
  "code": 0,
  "message": "OK",
  "data": {
    "content": [
      {
        "id": 1,
        "city": "上海",
        "maternityLeaveType": "产假",
        "abortionLeaveType": null,
        "leaveDays": 158,
        "isExtendable": true,
        "hasAllowance": true,
        "isDefault": true,
        "radioGroup": 0,
        "isActive": true,
        "createDate": "2025-11-30T23:00:00",
        "createBy": "system",
        "updateDate": "2025-11-30T23:00:00",
        "updateBy": "system"
      },
      {
        "id": 2,
        "city": "上海",
        "maternityLeaveType": "陪产假",
        "abortionLeaveType": null,
        "leaveDays": 10,
        "isExtendable": false,
        "hasAllowance": true,
        "isDefault": false,
        "radioGroup": 1,
        "isActive": true,
        "createDate": "2025-11-30T23:00:00",
        "createBy": "system",
        "updateDate": "2025-11-30T23:00:00",
        "updateBy": "system"
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 10,
      "sort": {
        "sorted": true,
        "unsorted": false,
        "empty": false
      }
    },
    "totalElements": 2,
    "totalPages": 1,
    "last": true,
    "first": true,
    "size": 10,
    "number": 0,
    "numberOfElements": 2,
    "empty": false
  }
}
```

**说明**:

- 只返回 `isActive = true` 的数据
- `city` 参数为可选，不传则返回所有城市的数据
- 支持分页、排序功能

---

### 3. 下载导入模板

**接口**: `GET /api/maternity-rules/template/download`

**描述**: 下载产假规则批量导入的CSV模板文件

**请求示例**:

```bash
GET /api/maternity-rules/template/download
```

**响应**:

- Content-Type: `text/csv`
- Content-Disposition: `attachment; filename="产假规则导入模板.csv"`
- 直接下载CSV文件

**CSV模板格式**:

```csv
城市,产假类型,流产类型,产假天数,是否遇法定节假日顺延,是否享受津贴,是否默认
上海,产假,无,158,是,是,是

说明：
1. 城市：填写城市名称，如：上海、北京
2. 产假类型：填写产假类型名称，如：产假、陪产假
3. 流产类型：填写流产类型名称，如：早期流产、晚期流产，无流产情况填写"无"
4. 产假天数：填写数字，如：158
5. 是否遇法定节假日顺延：填写"是"或"否"
6. 是否享受津贴：填写"是"或"否"
7. 是否默认：填写"是"或"否"
```

---

### 4. 批量导入产假规则

**接口**: `POST /api/maternity-rules/import`

**描述**: 通过CSV文件批量导入产假规则，支持新增和更新

**请求参数**:

- Content-Type: `multipart/form-data`
- 参数名: `file`
- 文件类型: CSV

**CSV文件格式**:

```csv
城市,产假类型,流产类型,产假天数,是否遇法定节假日顺延,是否享受津贴,是否默认
上海,产假,无,158,是,是,是
上海,陪产假,无,10,否,是,否
北京,产假,无,128,是,是,是
上海,产假,早期流产,15,否,是,否
```

**字段说明**:

| 字段名 | 必填 | 说明 | 示例 |
|--------|------|------|------|
| 城市 | 是 | 城市名称 | 上海 |
| 产假类型 | 是 | 产假类型名称 | 产假、陪产假 |
| 流产类型 | 否 | 流产类型名称，无流产填"无" | 早期流产、晚期流产、无 |
| 产假天数 | 是 | 数字 | 158 |
| 是否遇法定节假日顺延 | 是 | "是"或"否" | 是 |
| 是否享受津贴 | 是 | "是"或"否" | 是 |
| 是否默认 | 是 | "是"或"否" | 是 |

**请求示例** (cURL):

```bash
curl -X POST http://localhost:8080/api/maternity-rules/import \
  -F "file=@产假规则数据.csv"
```

**响应示例**:

```json
{
  "code": 0,
  "message": "OK",
  "data": "导入完成，共处理 10 条数据（包含新增和更新）"
}
```

**错误响应**:

```json
{
  "code": 400,
  "message": "文件不能为空",
  "data": null
}
```

**导入逻辑**:

1. 解析CSV文件
2. 验证必填字段（城市、产假类型、产假天数）
3. 根据 `城市 + 产假类型 + 流产类型` 判断是否存在记录
4. 存在则更新，不存在则创建
5. 自动设置 `isActive = true`
6. 返回成功处理的数量

---

### 5. 更新产假规则

**接口**: `PUT /api/maternity-rules/{id}`

**描述**: 根据ID更新产假规则

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Integer | 是 | 产假规则ID |

**请求参数**:

```json
{
  "city": "上海",
  "maternityLeaveType": "产假",
  "abortionLeaveType": null,
  "leaveDays": 160,
  "isExtendable": true,
  "hasAllowance": true,
  "isDefault": true,
  "radioGroup": 0,
  "isActive": true
}
```

**参数说明**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| city | String | 是 | 城市名称 |
| maternityLeaveType | String | 是 | 产假类型 |
| abortionLeaveType | String | 否 | 流产类型 |
| leaveDays | Integer | 是 | 假期天数 |
| isExtendable | Boolean | 是 | 是否节假日顺延 |
| hasAllowance | Boolean | 是 | 是否有津贴 |
| isDefault | Boolean | 是 | 是否默认选择 |
| radioGroup | Integer | 是 | 单选分组标识 |
| isActive | Boolean | 否 | 是否启用 |

**响应示例**:

```json
{
  "code": 0,
  "message": "OK",
  "data": {
    "id": 1,
    "city": "上海",
    "maternityLeaveType": "产假",
    "abortionLeaveType": null,
    "leaveDays": 160,
    "isExtendable": true,
    "hasAllowance": true,
    "isDefault": true,
    "radioGroup": 0,
    "isActive": true,
    "createDate": "2025-11-30T23:00:00",
    "createBy": "system",
    "updateDate": "2025-11-30T23:30:00",
    "updateBy": "admin"
  }
}
```

---

### 6. 删除产假规则

**接口**: `DELETE /api/maternity-rules/{id}`

**描述**: 逻辑删除产假规则（将 `isActive` 设置为 `false`）

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Integer | 是 | 产假规则ID |

**请求示例**:

```bash
DELETE /api/maternity-rules/1
```

**响应示例**:

```json
{
  "code": 0,
  "message": "OK",
  "data": null
}
```

**说明**:

- 这是逻辑删除，不会物理删除数据
- 删除后的数据 `isActive` 字段会被设置为 `false`
- 删除后的数据不会在列表查询中显示

---

## 数据模型

### MaternityRulesRequest

```json
{
  "city": "String (必填)",
  "maternityLeaveType": "String (必填)",
  "abortionLeaveType": "String (可选)",
  "leaveDays": "Integer (必填, >0)",
  "isExtendable": "Boolean (必填)",
  "hasAllowance": "Boolean (必填)",
  "isDefault": "Boolean (必填)",
  "radioGroup": "Integer (必填)",
  "isActive": "Boolean (可选)"
}
```

### MaternityRulesResponse

```json
{
  "id": "Integer",
  "city": "String",
  "maternityLeaveType": "String",
  "abortionLeaveType": "String",
  "leaveDays": "Integer",
  "isExtendable": "Boolean",
  "hasAllowance": "Boolean",
  "isDefault": "Boolean",
  "radioGroup": "Integer",
  "isActive": "Boolean",
  "createDate": "LocalDateTime",
  "createBy": "String",
  "updateDate": "LocalDateTime",
  "updateBy": "String"
}
```

---

## 字段说明

### 产假类型 (maternityLeaveType)

常见值：
- `产假` - 女性员工生育假期
- `陪产假` - 男性员工陪产假期
- `育儿假` - 父母育儿假期

### 流产类型 (abortionLeaveType)

常见值：
- `null` 或 `无` - 无流产情况
- `早期流产` - 怀孕4个月以下流产
- `晚期流产` - 怀孕4个月以上流产

### 单选分组 (radioGroup)

用于前端单选框分组：
- `0` - 基础产假组
- `1` - 陪产假组
- `2` - 其他假期组

---

## 错误码说明

| 错误码 | 说明 |
|--------|------|
| 0 | 成功 |
| 400 | 请求参数错误 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

---

## 使用流程示例

### 1. 批量导入流程

```bash
# 1. 下载模板
GET /api/maternity-rules/template/download

# 2. 填写数据后上传
POST /api/maternity-rules/import
Content-Type: multipart/form-data
file: 产假规则数据.csv

# 3. 查看导入结果
GET /api/maternity-rules?page=0&size=10
```

### 2. CRUD操作流程

```bash
# 1. 创建产假规则
POST /api/maternity-rules
{
  "city": "上海",
  "maternityLeaveType": "产假",
  "abortionLeaveType": null,
  "leaveDays": 158,
  "isExtendable": true,
  "hasAllowance": true,
  "isDefault": true,
  "radioGroup": 0
}

# 2. 查询列表（按城市过滤）
GET /api/maternity-rules?city=上海

# 3. 更新产假规则
PUT /api/maternity-rules/1
{
  "city": "上海",
  "maternityLeaveType": "产假",
  "leaveDays": 160,
  ...
}

# 4. 删除产假规则
DELETE /api/maternity-rules/1
```

### 3. 按城市查询不同类型的产假

```bash
# 查询上海的所有产假规则
GET /api/maternity-rules?city=上海&page=0&size=20

# 返回结果包含：
# - 产假（158天）
# - 陪产假（10天）
# - 早期流产假（15天）
# - 晚期流产假（42天）
```

---

## 注意事项

1. **逻辑删除**: 删除操作不会物理删除数据，只是将 `isActive` 设置为 `false`
2. **城市过滤**: 查询接口支持按城市过滤，不传 `city` 参数则返回所有数据
3. **批量导入**: 支持新增和更新，根据 `城市 + 产假类型 + 流产类型` 判断是否已存在
4. **数据验证**: 所有必填字段都会进行验证，假期天数必须大于0
5. **审计字段**: 系统自动记录创建时间、创建人、更新时间、更新人
6. **唯一性**: 同一城市下，`产假类型 + 流产类型` 的组合必须唯一
7. **流产类型**: 可以为空，表示非流产情况的产假规则

---

## 业务规则说明

### 产假计算规则

1. **基础产假**: 根据城市和产假类型确定基础天数
2. **流产假**: 根据流产类型确定额外假期
3. **节假日顺延**: 如果 `isExtendable = true`，遇到法定节假日会顺延
4. **津贴发放**: 如果 `hasAllowance = true`，员工可享受生育津贴

### 前端展示规则

1. **单选分组**: 使用 `radioGroup` 字段将规则分组展示
2. **默认选择**: `isDefault = true` 的规则会被默认选中
3. **城市筛选**: 前端应先选择城市，再展示该城市的产假规则
