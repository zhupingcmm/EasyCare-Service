# 津贴规则管理 API 文档

## 基础信息

**Base URL**: `/api/allowance-rules`

**描述**: 津贴规则的增删改查接口

---

## 接口列表

### 1. 创建津贴规则

**接口**: `POST /api/allowance-rules`

**描述**: 创建新的津贴规则

**请求参数**:

```json
{
  "city": "上海",
  "payoutMethod": "个人"
}
```

**参数说明**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| city | String | 是 | 城市名称 |
| payoutMethod | String | 是 | 发放方式（如：个人、企业） |

**响应示例**:

```json
{
  "code": 0,
  "message": "OK",
  "data": {
    "id": 1,
    "city": "上海",
    "payoutMethod": "个人",
    "isActive": true,
    "createDate": "2025-11-30T23:00:00",
    "createBy": "system",
    "updateDate": "2025-11-30T23:00:00",
    "updateBy": "system"
  }
}
```

---

### 2. 根据ID查询津贴规则

**接口**: `GET /api/allowance-rules/{id}`

**描述**: 根据ID查询单个津贴规则

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Integer | 是 | 津贴规则ID |

**请求示例**:

```bash
GET /api/allowance-rules/1
```

**响应示例**:

```json
{
  "code": 0,
  "message": "OK",
  "data": {
    "id": 1,
    "city": "上海",
    "payoutMethod": "个人",
    "isActive": true,
    "createDate": "2025-11-30T23:00:00",
    "createBy": "system",
    "updateDate": "2025-11-30T23:00:00",
    "updateBy": "system"
  }
}
```

---

### 3. 分页查询津贴规则

**接口**: `GET /api/allowance-rules`

**描述**: 分页查询所有津贴规则列表，支持按城市过滤

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
# 查询所有津贴规则
GET /api/allowance-rules?page=0&size=10

# 按城市过滤
GET /api/allowance-rules?city=上海&page=0&size=10
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
        "payoutMethod": "个人",
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
    "totalElements": 1,
    "totalPages": 1,
    "last": true,
    "first": true,
    "size": 10,
    "number": 0,
    "numberOfElements": 1,
    "empty": false
  }
}
```

**说明**:

- 只返回 `isActive = true` 的数据
- `city` 参数为可选，不传则返回所有城市的数据
- 支持分页、排序功能

---

### 4. 更新津贴规则

**接口**: `PUT /api/allowance-rules/{id}`

**描述**: 根据ID更新津贴规则

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Integer | 是 | 津贴规则ID |

**请求参数**:

```json
{
  "city": "上海",
  "payoutMethod": "企业"
}
```

**参数说明**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| city | String | 是 | 城市名称 |
| payoutMethod | String | 是 | 发放方式 |

**响应示例**:

```json
{
  "code": 0,
  "message": "OK",
  "data": {
    "id": 1,
    "city": "上海",
    "payoutMethod": "企业",
    "isActive": true,
    "createDate": "2025-11-30T23:00:00",
    "createBy": "system",
    "updateDate": "2025-11-30T23:30:00",
    "updateBy": "admin"
  }
}
```

---

### 5. 删除津贴规则

**接口**: `DELETE /api/allowance-rules/{id}`

**描述**: 根据ID逻辑删除津贴规则（将 `isActive` 设置为 `false`）

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Integer | 是 | 津贴规则ID |

**请求示例**:

```bash
DELETE /api/allowance-rules/1
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

### 6. 下载导入模板

**接口**: `GET /api/allowance-rules/template/download`

**描述**: 下载津贴规则批量导入的CSV模板文件

**请求示例**:

```bash
GET /api/allowance-rules/template/download
```

**响应**:

- Content-Type: `text/csv`
- Content-Disposition: `attachment; filename="津贴规则导入模板.csv"`
- 直接下载CSV文件

**CSV模板格式**:

```csv
城市,津贴发放方式
上海,个人

说明：
1. 城市：填写城市名称，如：上海、北京
2. 津贴发放方式：填写发放方式，如：个人、企业
```

---

### 7. 批量导入津贴规则

**接口**: `POST /api/allowance-rules/import`

**描述**: 通过CSV文件批量导入津贴规则

**请求参数**:

- Content-Type: `multipart/form-data`
- 参数名: `file`
- 文件类型: CSV

**CSV文件格式**:

```csv
城市,津贴发放方式
上海,个人
北京,企业
深圳,个人
```

**字段说明**:

| 字段名 | 必填 | 说明 | 示例 |
|--------|------|------|------|
| 城市 | 是 | 城市名称 | 上海 |
| 津贴发放方式 | 是 | 发放方式 | 个人、企业 |

**请求示例** (cURL):

```bash
curl -X POST http://localhost:8080/api/allowance-rules/import \
  -F "file=@津贴规则数据.csv"
```

**响应示例**:

```json
{
  "code": 0,
  "message": "OK",
  "data": "导入完成，成功导入 3 条数据"
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
2. 验证必填字段（城市、津贴发放方式）
3. 根据城市判断是否存在记录
4. 存在则更新，不存在则创建
5. 自动设置 `isActive = true`
6. 返回成功导入的数量

---

## 数据模型

### AllowanceRulesRequest

```json
{
  "city": "String (必填)",
  "payoutMethod": "String (必填)"
}
```

### AllowanceRulesResponse

```json
{
  "id": "Integer",
  "city": "String",
  "payoutMethod": "String",
  "isActive": "Boolean",
  "createDate": "LocalDateTime",
  "createBy": "String",
  "updateDate": "LocalDateTime",
  "updateBy": "String"
}
```

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
GET /api/allowance-rules/template/download

# 2. 填写数据后上传
POST /api/allowance-rules/import
Content-Type: multipart/form-data
file: 津贴规则数据.csv

# 3. 查看导入结果
GET /api/allowance-rules?page=0&size=10
```

### 2. CRUD操作流程

```bash
# 1. 创建津贴规则
POST /api/allowance-rules
{
  "city": "上海",
  "payoutMethod": "个人"
}

# 2. 查询列表
GET /api/allowance-rules?city=上海

# 3. 更新津贴规则
PUT /api/allowance-rules/1
{
  "city": "上海",
  "payoutMethod": "企业"
}

# 4. 删除津贴规则
DELETE /api/allowance-rules/1
```

---

## 注意事项

1. **逻辑删除**: 删除操作不会物理删除数据，只是将 `isActive` 设置为 `false`
2. **城市过滤**: 查询接口支持按城市过滤，不传 `city` 参数则返回所有数据
3. **批量导入**: 支持新增和更新，根据城市判断是否已存在
4. **数据验证**: 所有必填字段都会进行验证
5. **审计字段**: 系统自动记录创建时间、创建人、更新时间、更新人
