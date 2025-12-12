# 系统支持接口文档（统一返回包装）

- 控制器：`src/main/java/com/hr/maternity/controller/SupportController.java`
- 统一返回包装：`common.com.ocbc.ms.easy.care.ApiResponse`、`GlobalResponseAdvice`、`GlobalExceptionHandler`
- 相关实体/服务：
  - 城市实体：`src/main/java/com/hr/maternity/entity/City.java`
  - 节假日服务：`src/main/java/com/hr/maternity/service/HolidayService.java`
  - 节假日实现：`src/main/java/com/hr/maternity/service/impl/HolidayServiceImpl.java`

> 本项目所有接口外层统一返回 `{ code, message, data }`。控制器返回将由全局 `GlobalResponseAdvice` 自动包装。

---

## 1. 获取支持城市列表

- 路径：`GET /api/support/cities`
- 描述：从数据库查询启用的城市，按 `sortOrder` 升序返回
- 控制器方法：`SupportController.getSupportedCities()`
- 返回体（统一包装）：`{ code, message, data: City[] }`

### City 字段说明（来源 `City` 实体）

| 字段 | 类型 | 说明 |
|---|---|---|
| id | string(UUID) | 主键 |
| name | string | 城市名称 |
| code | string | 城市代码（唯一） |
| chineseName | string | 城市中文名称 |
| englishName | string | 城市英文名称 |
| province | string | 省份/地区 |
| countryCode | string | 国家代码（如 CN） |
| enabled | boolean | 是否启用（仅返回 enabled=true 的城市） |
| sortOrder | integer | 排序序号 |
| remark | object|null | 城市扩展备注（JSONB）。即使为 null 也会返回该字段，值为 null |
| createDate | string(datetime) | 创建时间 |
| createBy | string | 创建人 |
| updateDate | string(datetime) | 更新时间 |
| updateBy | string | 更新人 |

#### remark 字段结构（`dto.com.ocbc.ms.easy.care.CityRemark`）

| 字段 | 类型 | 说明 |
|---|---|---|
| allowanceToIndividual | boolean|null | 生育津贴是否发放至个人（true 个人，false 公司）。对于未配置城市，可为 null 或 false。无论是否为 null，都会返回该字段 |
| dystociaType | Option[]|null | 难产类型选项列表（仅广州适用）。其他城市为 null，但字段依然返回 |

Option 结构：

| 字段 | 类型 | 说明 |
|---|---|---|
| code | string | 代码，例如 `SEVERE_DYSTOCIA`、`ASSISTED_DELIVERY` |
| desc | string | 描述 |

### 成功响应示例
```json
{
  "code": 0,
  "message": "OK",
  "data": [
    {
      "id": "8ac2f2e2-7c12-4a7e-9b1a-8b2dfe7c3ab1",
      "name": "Shanghai",
      "code": "SH",
      "chineseName": "上海",
      "englishName": "Shanghai",
      "province": "上海",
      "countryCode": "CN",
      "enabled": true,
      "sortOrder": 1,
      "remark": {
        "allowanceToIndividual": false,
        "dystociaType": null
      },
      "createDate": "2025-01-01T10:00:00",
      "createBy": "system",
      "updateDate": "2025-01-02T12:00:00",
      "updateBy": "system"
    }
  ]
}
```

#### 广州（GZ）示例

```json
{
  "code": 0,
  "message": "OK",
  "data": [
    {
      "code": "GZ",
      "name": "Guangzhou",
      "remark": {
        "allowanceToIndividual": false,
        "dystociaType": [
          {"code": "SEVERE_DYSTOCIA", "desc": "难产（剖腹产、会阴Ⅲ度破裂）"},
          {"code": "ASSISTED_DELIVERY", "desc": "吸引产、钳产、臀位牵引产"}
        ]
      }
    }
  ]
}
```

---

## 2. 获取指定年份的中国公共假日

- 路径：`GET /api/support/holidays/{year}`
- 路径参数：
  - `year`（string，必填）：年份，例如 `2025`
- 描述：优先从数据库读取；若无数据，则从第三方源 `https://unpkg.com/holiday-calendar/data/CN/{year}.json` 获取并入库后返回
- 控制器方法：`SupportController.getPublicHolidays(String year)`
- 返回体（统一包装）：`{ code, message, data: HolidayItem[] }`

### HolidayItem 字段说明（来源 `HolidayServiceImpl`）

| 字段 | 类型 | 说明 |
|---|---|---|
| date | string(yyyy-MM-dd) | 日期 |
| name | string | 名称（原文） |
| name_cn | string | 中文名 |
| name_en | string | 英文名 |
| type | string | 类型，枚举字符串（对应 `Holiday.HolidayType`） |

> 若从数据库读取，`Holiday` 实体将被转换为上表字段返回；若从第三方获取，将直接透传 `dates` 数组中的同名字段。

### 成功响应示例
```json
{
  "code": 0,
  "message": "OK",
  "data": [
    {
      "date": "2025-01-01",
      "name": "New Year's Day",
      "name_cn": "元旦",
      "name_en": "New Year's Day",
      "type": "HOLIDAY"
    },
    {
      "date": "2025-02-01",
      "name": "Spring Festival",
      "name_cn": "春节",
      "name_en": "Spring Festival",
      "type": "HOLIDAY"
    }
  ]
}
```

---

## 错误响应（统一包装）

| 场景 | HTTP | 响应体 |
|---|---|---|
| 非法请求/参数错误 | 400/422 | `{ "code": 400/422, "message": "错误信息", "data": null }` |
| 服务器内部错误 | 500 | `{ "code": 500, "message": "服务器内部错误", "data": null }` |

> 全局异常由 `GlobalExceptionHandler` 处理，统一由 `GlobalResponseAdvice` 封装为 `{ code, message, data }` 结构。

---

## 3. 计算区间内每月工作日天数

- 路径：`GET /api/support/workdays`
- 描述：考虑周末（默认休息）、公共假日（工作日转休息）与调休补班（周末转工作日），计算 `[start, end]` 区间内每个月的工作日天数。若某年份无法获取公共假日/调休数据，则忽略该年的节假日规则，仅按周末规则计算。
- 控制器方法：`SupportController.getMonthlyWorkdays(String start, String end)`
- 返回体（统一包装）：`{ code, message, data: WorkdaysItem[] }`

### 请求参数

| 名称 | 类型 | 必填 | 示例 | 说明 |
|---|---|---|---|---|
| start | string(yyyy-MM-dd) | 是 | `2025-01-15` | 开始日期（包含） |
| end | string(yyyy-MM-dd) | 是 | `2025-03-10` | 结束日期（包含） |

### WorkdaysItem 字段说明

| 字段 | 类型 | 说明 |
|---|---|---|
| year | integer | 年份（例如 `2025`） |
| month | integer | 月份（1-12） |
| workdays | integer | 该月在指定区间内的工作日天数 |
| fullMonth | boolean | 是否为完整月：若该月第一天不早于 `start` 且该月最后一天不晚于 `end` 则为 `true`，否则为 `false` |

> 计算规则：
> - 周一至周五为工作日；周六、周日为休息日；
> - 若能获取公共假日/调休数据：`public_holiday` 将对应工作日设为休息；`transfer_workday` 将对应周末设为工作日；
> - 若无法获取对应年份的公共假日/调休数据，则忽略该年节假日规则，仅按周末计算；
> - 对于不满整月的月份，仅计算区间内日期。

### 成功响应示例

```json
{
  "code": 0,
  "message": "OK",
  "data": [
    { "year": 2025, "month": 1, "workdays": 13, "fullMonth": false },
    { "year": 2025, "month": 2, "workdays": 20, "fullMonth": true },
    { "year": 2025, "month": 3, "workdays": 6,  "fullMonth": false }
  ]
}
```

> 提示：上述数值仅为示例，实际返回取决于 `[start, end]`、当年周末分布及公共假日/调休数据。

---

## 调用说明

- Header：`Content-Type: application/json`
- GET `/api/support/cities`：无请求体
- GET `/api/support/holidays/{year}`：路径参数 `year` 必填
- GET `/api/support/workdays`：查询参数 `start`、`end` 必填，格式 `yyyy-MM-dd`
- 响应统一包装：控制器返回的 `ResponseEntity` 会被全局 Advice 包装为 `{ code, message, data }`
