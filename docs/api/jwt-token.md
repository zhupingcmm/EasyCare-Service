# JWT令牌认证 API 文档

## 基础信息

**Base URL**: `/api/auth`

**描述**: 用户认证、登录、登出、令牌管理相关接口

---

## JWT Token Payload 结构

JWT令牌包含以下标准和自定义声明：

```json
{
  "sub": "API",
  "aud": "OCBC",
  "unique_name": "AXXXXXXX",
  "nbf": 1764854416,
  "iss": "HR",
  "exp": 1764854416,
  "iat": 1764854416,
  "user_id": "550e8400-e29b-41d4-a716-446655440000"
}
```

**字段说明**:

| 字段名 | 类型 | 说明 |
|--------|------|------|
| sub | String | 主体，固定值 "API" |
| aud | String | 受众，固定值 "OCBC" |
| unique_name | String | 用户LAN账号，来自user表的lanId字段 |
| nbf | Long | 令牌生效时间（Unix时间戳） |
| iss | String | 签发者，固定值 "HR" |
| exp | Long | 令牌过期时间（Unix时间戳） |
| iat | Long | 令牌签发时间（Unix时间戳） |
| user_id | String | 用户ID，来自user表的id字段 |

**响应示例**:

```json
{
  "code": 0,
  "message": "OK",
  "data": {
    "id": 1,
    "date": "2025-01-01",
    "name": "元旦",
    "type": "public_holiday",
    "isStatutory": true,
    "isActive": true,
    "createdAt": "2025-11-30T22:00:00",
    "createdBy": null,
    "updatedAt": "2025-11-30T22:00:00",
    "updatedBy": null
  }
}
```

---

### 2. 查询所有节假日（分页）

**接口**: `GET /api/holidays`

**描述**: 分页查询所有未删除的节假日

**查询参数**:

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| page | Integer | 否 | 0 | 页码（从0开始） |
| size | Integer | 否 | 10 | 每页数量 |
| sort | String | 否 | updateDate | 排序字段 |
| direction | String | 否 | DESC | 排序方向：ASC/DESC |

**请求示例**:

```
GET /api/holidays?page=0&size=10&sort=updateDate&direction=DESC
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
        "date": "2025-01-01",
        "name": "元旦",
        "type": "public_holiday",
        "isStatutory": true,
        "isActive": true,
        "createdAt": "2025-11-30T22:00:00",
        "createdBy": null,
        "updatedAt": "2025-11-30T22:00:00",
        "updatedBy": null
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
- 已删除的数据不会出现在列表中

---

### 3. 下载节假日导入模板

**接口**: `GET /api/holidays/template/download`

**描述**: 下载节假日批量导入的CSV模板文件

**请求示例**:

```
GET /api/holidays/template/download
```

**响应**:
- Content-Type: `text/csv`
- Content-Disposition: `attachment; filename="节假日导入模板.csv"`

**CSV模板格式**:

```csv
日期,节假日名称,类型,是否为法定假日
2025-01-01,元旦,public_holiday,是
2025-01-26,春节调休,transfer_workday,否
```

---

### 4. 从公网API生成节假日CSV文件

**接口**: `GET /api/holidays/generate-csv/{year}`

**描述**: 从公网API获取指定年份的节假日数据并生成可导入的CSV文件

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| year | String | 是 | 年份，如：2025 |

**请求示例**:

```
GET /api/holidays/generate-csv/2025
```

**响应**:
- Content-Type: `text/csv`
- Content-Disposition: `attachment; filename="2025年节假日.csv"`

**数据来源**:
- 公网API：`https://unpkg.com/holiday-calendar/data/CN/{year}.json`

**生成的CSV格式**:

```csv
日期,节假日名称,类型,是否为法定假日
2025-01-01,元旦,public_holiday,是
2025-01-28,春节,public_holiday,是
2025-01-26,春节调休,transfer_workday,否
```

**错误响应**:

```
HTTP 400 Bad Request
生成CSV文件失败: 未能获取到2025年的节假日数据
```

---

### 5. 批量导入节假日

**接口**: `POST /api/holidays/import`

**描述**: 通过CSV文件批量导入节假日，支持新增和更新

**请求参数**:

- Content-Type: `multipart/form-data`
- 参数名: `file`
- 文件类型: CSV

**CSV文件格式**:

```csv
日期,节假日名称,类型,是否为法定假日
2025-01-01,元旦,public_holiday,是
2025-01-28,春节,public_holiday,是
2025-01-26,春节调休,transfer_workday,否
```

**字段说明**:

| 字段名 | 说明 | 示例 |
|--------|------|------|
| 日期 | 日期格式：yyyy-MM-dd | 2025-01-01 |
| 节假日名称 | 节假日名称 | 元旦 |
| 类型 | public_holiday 或 transfer_workday | public_holiday |
| 是否为法定假日 | "是" 或 "否" | 是 |

**请求示例** (cURL):

```bash
curl -X POST http://localhost:8080/api/holidays/import \
  -F "file=@节假日数据.csv"
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
- 根据日期判断是否存在记录
- 如果存在，更新记录
- 如果不存在，创建新记录
- 导入的数据自动设置 `isActive = true`

---

### 6. 更新节假日

**接口**: `PUT /api/holidays/{id}`

**描述**: 更新指定ID的节假日信息

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Integer | 是 | 节假日ID |

**请求参数**:

```json
{
  "date": "2025-01-01",
  "name": "元旦（更新）",
  "type": "public_holiday",
  "isStatutory": true
}
```

**请求示例**:

```
PUT /api/holidays/1
```

**响应示例**:

```json
{
  "code": 0,
  "message": "OK",
  "data": {
    "id": 1,
    "date": "2025-01-01",
    "name": "元旦（更新）",
    "type": "public_holiday",
    "isStatutory": true,
    "isActive": true,
    "createdAt": "2025-11-30T22:00:00",
    "createdBy": null,
    "updatedAt": "2025-11-30T22:30:00",
    "updatedBy": null
  }
}
```

**错误响应**:

```json
{
  "code": 500,
  "message": "节假日不存在，ID: 999",
  "data": null
}
```

---

### 7. 删除节假日（逻辑删除）

**接口**: `DELETE /api/holidays/{id}`

**描述**: 逻辑删除指定ID的节假日（设置 isActive = false）

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Integer | 是 | 节假日ID |

**请求示例**:

```
DELETE /api/holidays/1
```

**响应示例**:

```json
{
  "code": 0,
  "message": "OK",
  "data": null
}
```

**错误响应**:

```json
{
  "code": 500,
  "message": "节假日不存在，ID: 999",
  "data": null
}
```

**说明**:
- 这是逻辑删除，不会物理删除数据
- 删除后 `isActive` 字段会被设置为 `false`
- 删除后的数据不会出现在列表查询中

---

## 数据模型

### HolidayRequest (请求DTO)

```json
{
  "date": "2025-01-01",
  "name": "元旦",
  "type": "public_holiday",
  "isStatutory": true
}
```

### HolidayResponse (响应DTO)

```json
{
  "id": 1,
  "date": "2025-01-01",
  "name": "元旦",
  "type": "public_holiday",
  "isStatutory": true,
  "isActive": true,
  "createdAt": "2025-11-30T22:00:00",
  "createdBy": null,
  "updatedAt": "2025-11-30T22:00:00",
  "updatedBy": null
}
```

### 枚举类型

**HolidayType**:
- `public_holiday` - 公共假日
- `transfer_workday` - 调休工作日

---

## 统一响应格式

所有接口（除文件下载外）都使用统一的响应格式：

```json
{
  "code": 0,
  "message": "OK",
  "data": {}
}
```

**字段说明**:

| 字段 | 类型 | 说明 |
|------|------|------|
| code | Integer | 状态码，0表示成功，非0表示失败 |
| message | String | 提示信息 |
| data | Object | 数据载体 |

---

## 错误码

| 错误码 | 说明 |
|--------|------|
| 0 | 成功 |
| 400 | 请求参数错误 |
| 500 | 服务器内部错误 |

---

## 使用流程示例

### 场景1：手动创建节假日

1. 调用 `POST /api/holidays` 创建节假日
2. 调用 `GET /api/holidays` 查看列表

### 场景2：批量导入节假日

1. 调用 `GET /api/holidays/template/download` 下载模板
2. 填写模板数据
3. 调用 `POST /api/holidays/import` 上传文件
4. 调用 `GET /api/holidays` 查看导入结果

### 场景3：从公网API导入节假日

1. 调用 `GET /api/holidays/generate-csv/2025` 生成CSV文件
2. 下载生成的CSV文件
3. 调用 `POST /api/holidays/import` 导入CSV文件
4. 调用 `GET /api/holidays` 查看导入结果

### 场景4：更新和删除

1. 调用 `PUT /api/holidays/{id}` 更新节假日信息
2. 调用 `DELETE /api/holidays/{id}` 删除节假日
3. 调用 `GET /api/holidays` 验证（已删除的不会显示）

---

## 注意事项

1. **日期唯一性**: 同一日期只能有一条记录（数据库唯一约束）
2. **逻辑删除**: 删除操作是逻辑删除，数据仍保留在数据库中
3. **分页查询**: 默认按更新时间倒序排列
4. **CSV编码**: 所有CSV文件使用UTF-8编码（包含BOM）
5. **批量导入**: 支持新增和更新，根据日期判断
6. **公网API**: 依赖第三方API，可能存在网络问题

---

## 更新日志

- **2025-11-30**: 初始版本
  - 实现基础CRUD功能
  - 支持CSV批量导入
  - 支持从公网API生成CSV
  - 实现逻辑删除
  - 列表查询只返回未删除数据
