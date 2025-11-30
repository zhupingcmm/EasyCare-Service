# 长假类型管理 API 文档

## 基础信息

**Base URL**: `/api/leave-types`

**描述**: 长假类型的增删改查接口

---

## 接口列表

### 1. 创建长假类型

**接口**: `POST /api/leave-types`

**描述**: 创建新的长假类型

**请求参数**:

```json
{
  "typeName": "产假",
  "isActive": true
}
```

**参数说明**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| typeName | String | 是 | 类型名称（如：产假、陪产假） |
| isActive | Boolean | 是 | 是否启用 |

**响应示例**:

```json
{
  "code": 0,
  "message": "OK",
  "data": {
    "id": 1,
    "typeName": "产假",
    "isActive": true,
    "createDate": "2025-11-30T22:00:00",
    "createBy": null,
    "updateDate": "2025-11-30T22:00:00",
    "updateBy": null
  }
}
```

---

### 2. 查询所有长假类型

**接口**: `GET /api/leave-types`

**描述**: 查询所有长假类型列表（包含已启用和未启用的）

**请求示例**:

```http
GET /api/leave-types
```

**响应示例**:

```json
{
  "code": 0,
  "message": "OK",
  "data": [
    {
      "id": 1,
      "typeName": "产假",
      "isActive": true,
      "createDate": "2025-11-30T22:00:00",
      "createBy": null,
      "updateDate": "2025-11-30T22:00:00",
      "updateBy": null
    },
    {
      "id": 2,
      "typeName": "陪产假",
      "isActive": true,
      "createDate": "2025-11-30T22:00:00",
      "createBy": null,
      "updateDate": "2025-11-30T22:00:00",
      "updateBy": null
    }
  ]
}
```

**说明**:
- 返回所有长假类型，不分页
- 包含已启用（`isActive=true`）和未启用（`isActive=false`）的数据

---

### 3. 更新长假类型

**接口**: `PUT /api/leave-types/{id}`

**描述**: 根据ID更新长假类型

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Integer | 是 | 长假类型ID |

**请求参数**:

```json
{
  "typeName": "产假（更新）",
  "isActive": true
}
```

**请求示例**:

```http
PUT /api/leave-types/1
```

**响应示例**:

```json
{
  "code": 0,
  "message": "OK",
  "data": {
    "id": 1,
    "typeName": "产假（更新）",
    "isActive": true,
    "createDate": "2025-11-30T22:00:00",
    "createBy": null,
    "updateDate": "2025-11-30T22:30:00",
    "updateBy": null
  }
}
```

**错误响应**:

```json
{
  "code": 500,
  "message": "长假类型不存在，ID: 999",
  "data": null
}
```

---

### 4. 删除长假类型（逻辑删除）

**接口**: `DELETE /api/leave-types/{id}`

**描述**: 根据ID逻辑删除长假类型（将 isActive 设置为 false）

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Integer | 是 | 长假类型ID |

**请求示例**:

```http
DELETE /api/leave-types/1
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
  "message": "长假类型不存在，ID: 999",
  "data": null
}
```

**说明**:
- 这是逻辑删除，不会物理删除数据
- 删除后 `isActive` 字段会被设置为 `false`
- 删除后的数据仍会出现在列表查询中（与节假日不同）

---

### 5. 下载长假类型导入模板

**接口**: `GET /api/leave-types/template/download`

**描述**: 下载长假类型批量导入的CSV模板文件

**请求示例**:

```http
GET /api/leave-types/template/download
```

**响应**:
- Content-Type: `text/csv`
- Content-Disposition: `attachment; filename="长假类型导入模板.csv"`

**CSV模板格式**:

```csv
类型名称,是否启用
产假,是
陪产假,是
病假,否
```

**字段说明**:

| 字段名 | 说明 | 示例 |
|--------|------|------|
| 类型名称 | 长假类型名称 | 产假 |
| 是否启用 | "是" 或 "否" | 是 |

---

### 6. 批量导入长假类型

**接口**: `POST /api/leave-types/import`

**描述**: 通过CSV文件批量导入长假类型

**请求参数**:

- Content-Type: `multipart/form-data`
- 参数名: `file`
- 文件类型: CSV

**CSV文件格式**:

```csv
类型名称,是否启用
产假,是
陪产假,是
病假,否
```

**请求示例** (cURL):

```bash
curl -X POST http://localhost:8080/api/leave-types/import \
  -F "file=@长假类型数据.csv"
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
- 根据类型名称判断是否存在记录
- 如果存在，更新记录
- 如果不存在，创建新记录
- "是否启用"字段：`是` → `true`，`否` → `false`

---

## 数据模型

### LeaveTypeRequest (请求DTO)

```json
{
  "typeName": "产假",
  "isActive": true
}
```

**字段说明**:

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| typeName | String | 是 | 类型名称（如：产假、陪产假） |
| isActive | Boolean | 是 | 是否启用 |

### LeaveTypeResponse (响应DTO)

```json
{
  "id": 1,
  "typeName": "产假",
  "isActive": true,
  "createDate": "2025-11-30T22:00:00",
  "createBy": null,
  "updateDate": "2025-11-30T22:00:00",
  "updateBy": null
}
```

**字段说明**:

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | Integer | 主键ID |
| typeName | String | 类型名称 |
| isActive | Boolean | 是否启用 |
| createDate | LocalDateTime | 创建时间 |
| createBy | String | 创建人 |
| updateDate | LocalDateTime | 更新时间 |
| updateBy | String | 更新人 |

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

### 场景1：手动创建长假类型

1. 调用 `POST /api/leave-types` 创建长假类型
2. 调用 `GET /api/leave-types` 查看列表

**示例**:

```bash
# 1. 创建产假类型
curl -X POST http://localhost:8080/api/leave-types \
  -H "Content-Type: application/json" \
  -d '{
    "typeName": "产假",
    "isActive": true
  }'

# 2. 查询所有类型
curl -X GET http://localhost:8080/api/leave-types
```

### 场景2：批量导入长假类型

1. 调用 `GET /api/leave-types/template/download` 下载模板
2. 填写模板数据
3. 调用 `POST /api/leave-types/import` 上传文件
4. 调用 `GET /api/leave-types` 查看导入结果

**示例**:

```bash
# 1. 下载模板
curl -X GET http://localhost:8080/api/leave-types/template/download \
  -o 长假类型导入模板.csv

# 2. 编辑模板文件（手动操作）

# 3. 导入数据
curl -X POST http://localhost:8080/api/leave-types/import \
  -F "file=@长假类型数据.csv"

# 4. 查看结果
curl -X GET http://localhost:8080/api/leave-types
```

### 场景3：更新和删除

1. 调用 `PUT /api/leave-types/{id}` 更新长假类型信息
2. 调用 `DELETE /api/leave-types/{id}` 删除长假类型
3. 调用 `GET /api/leave-types` 验证

**示例**:

```bash
# 1. 更新类型
curl -X PUT http://localhost:8080/api/leave-types/1 \
  -H "Content-Type: application/json" \
  -d '{
    "typeName": "产假（更新）",
    "isActive": true
  }'

# 2. 删除类型（逻辑删除）
curl -X DELETE http://localhost:8080/api/leave-types/1

# 3. 查看结果（已删除的数据 isActive=false）
curl -X GET http://localhost:8080/api/leave-types
```

---

## 常见长假类型

以下是常见的长假类型供参考：

| 类型名称 | 说明 |
|----------|------|
| 产假 | 女性员工生育假期 |
| 陪产假 | 男性员工陪产假期 |
| 哺乳假 | 哺乳期假期 |
| 病假 | 疾病治疗假期 |
| 事假 | 个人事务假期 |
| 年假 | 带薪年假 |
| 婚假 | 结婚假期 |
| 丧假 | 直系亲属丧葬假期 |

---

## 注意事项

1. **类型名称唯一性**: 建议同一类型名称只创建一条记录
2. **逻辑删除**: 删除操作是逻辑删除，数据仍保留在数据库中
3. **列表查询**: 查询接口返回所有数据（包含已删除的），前端需要根据 `isActive` 字段过滤
4. **CSV编码**: 所有CSV文件使用UTF-8编码（包含BOM）
5. **批量导入**: 支持新增和更新，根据类型名称判断
6. **启用状态**: `isActive` 字段控制类型是否可用，建议在业务逻辑中使用

---

## 与其他模块的关系

长假类型模块与以下模块相关联：

- **产假计算模块**: 使用长假类型来区分不同的假期类型
- **产假规则模块**: 根据长假类型配置不同的假期规则
- **津贴计算模块**: 不同长假类型可能有不同的津贴计算方式

---

## 更新日志

- **2025-11-30**: 初始版本
  - 实现基础CRUD功能
  - 支持CSV批量导入
  - 实现逻辑删除
  - 列表查询返回所有数据（包含已删除）
