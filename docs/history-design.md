# 历史记录（History）设计文档

## 1. 背景

前端在津贴 / 产假计算完成后，需要将完整的员工计算结果原样透传给后端，以便 HR 随时回放历史记录。本方案新增 `t_history` 表存储前端提交的 JSON 数据，并提供新增、查询、删除 3 个接口，所有接口均遵循统一的 `ApiResponse { code, message, data }` 包装。

## 2. 数据库设计

### 2.1 表结构概览

| 字段 | 类型 | 约束 | 说明 |
| --- | --- | --- | --- |
| id | bigserial | PK | 主键，自增 |
| hr_id | varchar(64) | not null, index | HR 账号/工号，用于隔离数据 |
| employee_id | varchar(64) | not null | 员工工号 |
| employee_data | jsonb | not null | 前端透传的完整 JSON 结果 |
| created_time | timestamptz | default now() | 创建时间 |
| updated_time | timestamptz | default now() | 更新时间（触发器维护） |

> 说明：虽然需求只要求 3 个业务字段，但增加 `created_time` / `updated_time` 便于审计与排序，不会影响前端透传逻辑。

### 2.2 建表 SQL

```sql
create table if not exists t_history (
    id bigserial primary key,
    hr_id varchar(64) not null,
    employee_id varchar(64) not null,
    employee_data jsonb not null,
    created_time timestamptz not null default now(),
    updated_time timestamptz not null default now()
);

create index if not exists idx_t_history_hr_id on t_history (hr_id);
create index if not exists idx_t_history_hr_employee on t_history (hr_id, employee_id);
```

- `jsonb` 类型可直接保存前端对象，支持高效查询及 GIN 索引（二期如需）。
- 建议在应用层新增 `t_history` 的 JPA 实体，字段即 `hrId / employeeId / employeeData`（使用 `Map<String, Object>` 或 `JsonNode` 承载）。

## 3. API 设计

基础路径：`/api/history`

所有接口均在 Service 层做透传，不改写 `employeeData`，只附带操作元信息。

### 3.1 POST `/api/history/add`

| 项 | 说明 |
| --- | --- |
| 描述 | 新增或覆盖指定 HR + 员工的历史记录 |
| 请求体 | `hrId`, `employeeId`, `employeeData`（object，来自前端） |
| 返回体 | 返回已保存的关键信息（不再额外加工） |

**Request**

```json
{
  "hrId": "A511111",
  "employeeId": "A522222",
  "employeeData": {
    "id": "A5312312_1760960995484",
    "employeeName": "2131211",
    "employeeId": "A5312312",
    "city": "SX",
    "cityName": "绍兴",
    "startDate": "2025-06-03",
    "endDate": "2025-11-08",
    "totalDays": 158,
    "companyCompensation": 0,
    "employeeCompensation": 3176.6712,
    "calculatedAt": "2025-10-20T11:49:55.485Z",
    "recordType": "allowance",
    "receivedTime": "2025-10-20",
    "vacationData": { "...": "..." }
  }
}
```

**Response**

```json
{
  "code": 0,
  "message": "OK",
  "data": {
    "hrId": "A511111",
    "employeeId": "A522222",
    "employeeData": {
      "...": "与请求一致"
    }
  }
}
```

### 3.2 POST `/api/history/query`

| 项 | 说明 |
| --- | --- |
| 描述 | 查询 HR 下所有历史记录，可追加 employeeIds 过滤 |
| 请求体 | `hrId`（必填），`employeeIds`（可选数组） |
| 返回体 | `employeeData` 数组（直接从 `jsonb` 取出） |

**Request**

```json
{
  "hrId": "A511111"
}
```

**Response**

```json
{
  "code": 0,
  "message": "OK",
  "data": [
    {
      "id": "A5312312_1760960995484",
      "employeeName": "2131211",
      "employeeId": "A5312312",
      "city": "SX",
      "cityName": "绍兴",
      "startDate": "2025-06-03",
      "endDate": "2025-11-08",
      "totalDays": 158,
      "companyCompensation": 0,
      "employeeCompensation": 3176.6712,
      "calculatedAt": "2025-10-20T11:49:55.485Z",
      "recordType": "allowance",
      "receivedTime": "2025-10-20",
      "vacationData": { "...": "..." },
      "allowanceData": { "...": "..." }
    }
  ]
}
```

> 默认按 `created_at desc` 返回，前端无需额外字段映射。

### 3.3 POST `/api/history/delete`

| 项 | 说明 |
| --- | --- |
| 描述 | 批量删除 HR 的指定员工历史记录 |
| 请求体 | `hrId`（必填）、`employeeIds`（必填数组） |
| 返回体 | `{ "status": "success" }` |

**Request**

```json
{
  "hrId": "A511111",
  "employeeIds": ["A522222", "A5333333"]
}
```

**Response**

```json
{
  "code": 0,
  "message": "OK",
  "data": {
    "status": "success"
  }
}
```

## 4. 开发要点

1. **Entity / Repository**
   - `HistoryDO` 对应 `t_history`，`employeeData` 使用 `@JdbcTypeCode(SqlTypes.JSON)` 或 Hibernate `@Type(JsonType.class)`。
   - 复合唯一约束（`hr_id + employee_id`）可在 JPA 层用 `@Table(uniqueConstraints = …)` 保证幂等写入。
2. **Service**
   - `/add`: 如果存在相同 `hrId + employeeId`，执行更新（`save` 覆盖 `employee_data`）。
   - `/query`: 直接返回 `employee_data` 列映射后的对象，确保顺序倒序。
   - `/delete`: `deleteByHrIdAndEmployeeIdIn` 批量删除。
3. **安全**
   - 所有接口需校验 `hrId` 与登录身份一致，避免越权。
   - `employee_data` 为透传内容，不做字段级验证，仅在入库前做 JSON schema 校验（可选）。

> 后续若需要多租户或审计扩展，可在该表追加 `tenant_id`、`deleted` 字段，并通过软删实现恢复能力。
