# 系统支持 API 文档

## 基础信息

- **基础路径**: `/api/support`
- **Controller**: `SupportController`
- **标签**: 系统支持

---

## API 列表

### 1. 获取支持的城市

获取系统支持的所有城市列表。

#### 请求信息

- **URL**: `/api/support/cities`
- **方法**: `GET`

#### 请求参数

无

#### 请求示例

```http
GET /api/support/cities
```

#### 响应参数 (List<CityDO>)

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | Integer | 主键ID |
| code | String | 城市代码 |
| chineseName | String | 城市中文名称 |
| englishName | String | 城市英文名称 |
| province | String | 省份/地区 |
| enabled | Boolean | 是否启用 |
| sortOrder | Integer | 排序序号 |
| remark | CityRemark | 备注（JSONB格式） |
| createDate | LocalDateTime | 创建时间 |
| createBy | String | 创建人 |
| updateDate | LocalDateTime | 更新时间 |
| updateBy | String | 更新人 |

#### 响应示例

```json
[
  {
    "id": 1,
    "code": "SH",
    "chineseName": "上海",
    "englishName": "Shanghai",
    "province": "上海市",
    "enabled": true,
    "sortOrder": 1,
    "remark": null,
    "createDate": "2024-01-01T10:00:00",
    "createBy": "system",
    "updateDate": "2024-01-01T10:00:00",
    "updateBy": "system"
  },
  {
    "id": 2,
    "code": "BJ",
    "chineseName": "北京",
    "englishName": "Beijing",
    "province": "北京市",
    "enabled": true,
    "sortOrder": 2,
    "remark": null,
    "createDate": "2024-01-01T10:00:00",
    "createBy": "system",
    "updateDate": "2024-01-01T10:00:00",
    "updateBy": "system"
  }
]
```

---

### 2. 获取公共假日

获取指定年份的中国公共假日列表。

#### 请求信息

- **URL**: `/api/support/holidays/{year}`
- **方法**: `GET`

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| year | String | 是 | 年份 | "2024" |

#### 请求示例

```http
GET /api/support/holidays/2024
```

#### 响应示例

```json
{
  "2024-01-01": {
    "date": "2024-01-01",
    "name": "元旦",
    "isPublicHoliday": true,
    "type": "public_holiday"
  },
  "2024-02-10": {
    "date": "2024-02-10",
    "name": "春节",
    "isPublicHoliday": true,
    "type": "public_holiday"
  }
}
```

---

### 3. 计算区间内每月工作日天数

考虑公共假日与调休（若可用），计算指定日期范围内各月的工作日天数。

#### 请求信息

- **URL**: `/api/support/workdays`
- **方法**: `GET`

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| start | String | 是 | 开始日期（格式：yyyy-MM-dd） | "2024-01-01" |
| end | String | 是 | 结束日期（格式：yyyy-MM-dd） | "2024-03-31" |

#### 业务规则

- 周一至周五为工作日，周末休息
- 若能获取对应年份的公共假日与调休（补班），会进行修正
- 若对应年份无法获取公共假日/调休，则忽略，仅按周末计算

#### 请求示例

```http
GET /api/support/workdays?start=2024-01-01&end=2024-03-31
```

#### 响应示例

```json
[
  {
    "yearMonth": "2024-01",
    "workdays": 20
  },
  {
    "yearMonth": "2024-02",
    "workdays": 18
  },
  {
    "yearMonth": "2024-03",
    "workdays": 21
  }
]
```

---

### 4. 按日期范围查询节假日

查询指定日期范围内的节假日信息。

#### 请求信息

- **URL**: `/api/support/holidays`
- **方法**: `GET`

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| start | String | 是 | 开始日期（格式：yyyy-MM-dd） | "2024-11-01" |
| end | String | 是 | 结束日期（格式：yyyy-MM-dd） | "2025-04-25" |

#### 请求示例

```http
GET /api/support/holidays?start=2024-11-01&end=2025-04-25
```

#### 响应参数

| 字段名 | 类型 | 说明 |
|--------|------|------|
| code | Integer | 响应码（0表示成功） |
| message | String | 响应消息 |
| data | List<HolidayDetail> | 节假日列表 |

**HolidayDetail 对象结构**:

| 字段名 | 类型 | 说明 |
|--------|------|------|
| date | String | 日期（yyyy-MM-dd格式） |
| name | String | 节假日名称 |
| isPublicHoliday | Boolean | 是否为法定假日 |
| type | String | 类型（public_holiday 或 transfer_workday） |
| name_cn | String | 中文名称 |
| name_en | String | 英文名称 |

#### 响应示例

```json
{
  "code": 0,
  "message": "OK",
  "data": [
    {
      "date": "2024-12-25",
      "name": "圣诞节",
      "isPublicHoliday": false,
      "type": "public_holiday",
      "name_cn": "圣诞节",
      "name_en": "圣诞节"
    },
    {
      "date": "2025-01-01",
      "name": "元旦",
      "isPublicHoliday": true,
      "type": "public_holiday",
      "name_cn": "元旦",
      "name_en": "元旦"
    },
    {
      "date": "2025-01-28",
      "name": "春节",
      "isPublicHoliday": true,
      "type": "public_holiday",
      "name_cn": "春节",
      "name_en": "春节"
    },
    {
      "date": "2025-02-08",
      "name": "调休补班",
      "isPublicHoliday": false,
      "type": "transfer_workday",
      "name_cn": "调休补班",
      "name_en": "调休补班"
    }
  ]
}
```

---

## 数据模型

### CityDO

城市实体对象

```json
{
  "id": 1,
  "code": "SH",
  "chineseName": "上海",
  "englishName": "Shanghai",
  "province": "上海市",
  "enabled": true,
  "sortOrder": 1,
  "remark": null,
  "createDate": "2024-01-01T10:00:00",
  "createBy": "system",
  "updateDate": "2024-01-01T10:00:00",
  "updateBy": "system"
}
```

### HolidayInfo

节假日信息对象

```json
{
  "date": "2024-01-01",
  "name": "元旦",
  "isPublicHoliday": true,
  "type": "public_holiday"
}
```

**type 字段说明**:
- `public_holiday`: 法定公共假日
- `transfer_workday`: 调休补班日

---

## 业务规则说明

### 工作日计算规则

1. **基础规则**: 周一至周五为工作日，周六、周日为休息日
2. **公共假日修正**: 如果某工作日是法定假日，则不计入工作日
3. **调休补班修正**: 如果某休息日是调休补班日，则计入工作日
4. **降级处理**: 若无法获取某年份的假日数据，则仅按周末规则计算

### 城市列表规则

- 仅返回 `enabled=true` 的城市
- 按 `sortOrder` 字段升序排列

### 节假日查询规则

- 返回结果按日期升序排列
- 包含指定日期范围内的所有节假日和调休补班日

---

## 错误码说明

| 错误码 | 说明 |
|--------|------|
| 0 | 成功 |
| 400 | 参数校验失败 |
| 500 | 服务器内部错误 |

---

## 注意事项

1. 所有日期格式使用 ISO 8601 标准: `yyyy-MM-dd`
2. 日期范围查询为闭区间，包含起始和结束日期
3. 工作日计算会自动考虑公共假日和调休，无需额外处理
4. 城市列表仅返回已启用的城市
5. 节假日数据来源于中国法定节假日安排

---

## 使用场景

### 场景1: 获取城市列表用于下拉选择

```javascript
// 前端调用示例
fetch('/api/support/cities')
  .then(res => res.json())
  .then(cities => {
    // 填充城市下拉框
    cities.forEach(city => {
      console.log(city.code, city.chineseName);
    });
  });
```

### 场景2: 计算产假期间的工作日天数

```javascript
// 计算2024年1月到3月的工作日
fetch('/api/support/workdays?start=2024-01-01&end=2024-03-31')
  .then(res => res.json())
  .then(data => {
    const totalWorkdays = data.reduce((sum, item) => sum + item.workdays, 0);
    console.log('总工作日:', totalWorkdays);
  });
```

### 场景3: 查询节假日用于日历展示

```javascript
// 查询某个时间段的节假日
fetch('/api/support/holidays?start=2024-11-01&end=2025-04-25')
  .then(res => res.json())
  .then(response => {
    const holidays = response.data;
    // 在日历上标记节假日
    holidays.forEach(holiday => {
      if (holiday.isPublicHoliday) {
        console.log('法定假日:', holiday.date, holiday.name);
      }
    });
  });
```

---

## 版本历史

| 版本 | 日期 | 说明 |
|------|------|------|
| 1.0 | 2026-01-03 | 初始版本 |
