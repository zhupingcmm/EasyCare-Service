# 产假天数接口文档（统一返回包装）

- 控制器：`src/main/java/com/hr/maternity/controller/MaternityLeaveController.java`
- 服务实现：`src/main/java/com/hr/maternity/service/impl/MaternityLeaveServiceImpl.java`
- 统一返回包装：`com.easy.care.common.ApiResponse`、`GlobalResponseAdvice`、`GlobalExceptionHandler`
- 城市策略：`src/main/java/com/hr/maternity/strategy/impl/leave/`
  - 示例：`ShanghaiMaternityLeaveStrategy`、`ShenzhenMaternityLeaveStrategy`、`GuangzhouMaternityLeaveStrategy` 等

> 命名与响应结构遵循项目命名规范：实体 DO 结尾、Controller 复数 REST 路径、PostgreSQL 表名与字段小写下划线、API 响应统一 `{ code, message, data }`。

## 接口概览

| 项 | 值 |
|---|---|
| 方法 | POST |
| 路径 | /api/maternity-leave/calculate |
| 描述 | 根据城市与个人情况计算产假天数 |
| 控制器方法 | `MaternityLeaveController.calculateMaternityLeave()` |
| 请求体 | `MaternityLeaveRequest`（JSON） |
| 返回体（统一包装） | `{ code, message, data: MaternityLeaveResponse }` |

## 请求参数（MaternityLeaveRequest）

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| lanId | string | 是 | 工号 |
| employeeName | string | 是 | 姓名 |
| cityCode | string | 是 | 城市代码（如：SH、SZ、GZ、BJ、TJ、QD、CQ、XM、ZH、FS、SX、SU、CD、WH 等） |
| expectedDeliveryDate | string(date) | 是 | 预产期，格式：YYYY-MM-DD（ISO-8601），示例：2025-10-01 |
| isMultipleBirth | boolean | 否 | 是否多胞胎（默认 false） |
| numberOfBabies | int | 否 | 生育胎数，>1 视为多胞胎（默认 1） |
| hasExtendedDays | boolean | 否 | 是否享受 4. 晚育假/生育假/奖励假（默认 false） |
| isDifficultBirth | boolean | 否 | 是否难产（默认 false） |
| isCesareanSection | boolean | 否 | 是否剖腹产（默认 false） |
| age | int | 否 | 年龄（>0） |

> 字段与 DTO `MaternityLeaveRequest` 一致：含 `isMultipleBirth`、`numberOfBabies`、`hasExtendedDays`、`isDifficultBirth`、`isCesareanSection`、`age` 等。
>
> 日期字段使用 ISO-8601 标准格式（YYYY-MM-DD）。
>
> 城市列表来源于数据库（通过 `/api/support/cities` 获取），项目已移除 `CityEnum`，策略选择由 `cityCode` 决定。

## 返回数据（MaternityLeaveResponse in data）

| 字段 | 类型 | 说明 |
|---|---|---|
| lanId | string | 工号 |
| employeeName | string | 姓名 |
| cityCode | string | 城市代码 |
| cityName | string | 城市名称（由服务层 `MaternityLeaveServiceImpl` 通过城市库填充） |
| totalDays | int | 产假总天数 |
| totalAllowanceDays | int | 津贴总天数 |
| baseDays | int | 1. 基础产假天数 |
| dystociaDays | int | 2. 难产天数 |
| multiBabyDays | int | 3. 多胞胎天数 |
| extendedDays | int | 4. 晚育/生育/奖励假天数 |
| pubHolidaysCount | int | 奖励假期间的公共节假日顺延天数 |
| startDate | string(date) | 产假开始日期（ISO-8601 格式） |
| endDate | string(date) | 产假结束日期（ISO-8601 格式） |
| returnToWorkDate | string(date) | 返岗日期（结束后第一个工作日） |
| timeScopeList | array | 各假期时间段详情（含起止日期） |

> 服务层实现：`MaternityLeaveServiceImpl.calculateMaternityLeave()` 会根据 `cityCode` 选择对应策略计算，并从城市表填充 `cityName`。

## 城市规则摘要（示例）

不同城市在基础 98 天基础上增加天数，常见规则：

| 城市 | 基础天数 | 常规奖励 | 难产加天 | 多胞胎加天 | 备注 |
|---|---:|---:|---:|---:|---|
| 上海（SH） | 98 | +60 | +15 | 每多1胎 +15 | `ShanghaiMaternityLeaveStrategy` |
| 深圳（SZ） | 98 | +80 | +15 | 每多1胎 +15 | `ShenzhenMaternityLeaveStrategy` |
| 广州（GZ） | 98 | +80 | +15 | 每多1胎 +15 | `GuangzhouMaternityLeaveStrategy` |
| 北京（BJ） | 98 | +60 | +15 | 每多1胎 +15 | `BeijingMaternityLeaveStrategy` |
| 天津（TJ） | 98 | +60 | +15 | 每多1胎 +15 | `TianjinMaternityLeaveStrategy` |
| 厦门（XM） | 98 | +80 | +15 | 每多1胎 +15 | `XiamenMaternityLeaveStrategy` |
| 重庆（CQ） | 98 | +80 | +15 | 每多1胎 +15 | `ChongqingMaternityLeaveStrategy` |
| 珠海（ZH） | 98 | +80 | +30 | 每多1胎 +15 | `ZhuhaiMaternityLeaveStrategy` |
| 佛山（FS） | 98 | +80 | +30 | 每多1胎 +15 | `FoshanMaternityLeaveStrategy` |
| 绍兴（SX） | 98 | +60 | +15 | 每多1胎 +15 | `ShaoxingMaternityLeaveStrategy` |
| 苏州（SU） | 98 | +30 | +15 | 每多1胎 +15 | `SuzhouMaternityLeaveStrategy` |
| 青岛（QD） | 98 | +60 | +15 | 每多1胎 +15 | `QingdaoMaternityLeaveStrategy` |
| 成都（CD） | 98 | +60 | +15 | 每多1胎 +15 | `ChengduMaternityLeaveStrategy` |
| 武汉（WH） | 98 | +60 | +15 | 每多1胎 +15 | `WuhanMaternityLeaveStrategy` |

> 以上来源于各城市策略类实现；如地方法规调整，请以策略实现为准。

## 统一错误返回

| 场景 | HTTP | 响应体（统一包装） |
|---|---|---|
| 参数校验失败（@Valid） | 400 | `{ "code": 400, "message": "字段错误1; 字段错误2", "data": null }` |
| 非法参数（业务异常） | 422 | `{ "code": 422, "message": "不支持的城市代码: XXX", "data": null }` |
| 服务器内部错误 | 500 | `{ "code": 500, "message": "服务器内部错误", "data": null }` |

## 示例

### 请求示例（深圳 SZ，难产且多胞胎，并享受奖励假）
```json
{
  "lanId": "A12345",
  "employeeName": "李四",
  "cityCode": "SZ",
  "expectedDeliveryDate": "2025-11-01",
  "isMultipleBirth": true,
  "numberOfBabies": 2,
  "hasExtendedDays": true,
  "isDifficultBirth": true,
  "isCesareanSection": false,
  "age": 30
}
```

### 成功响应（统一包装）
```json
{
  "code": 0,
  "message": "OK",
  "data": {
    "lanId": "A12345",
    "employeeName": "李四",
    "cityCode": "SZ",
    "cityName": "深圳",
    "totalDays": 193,
    "totalAllowanceDays": 193,
    "baseDays": 98,
    "dystociaDays": 30,
    "multiBabyDays": 15,
    "extendedDays": 50,
    "pubHolidaysCount": 0,
    "startDate": "2025-11-01",
    "endDate": "2026-05-12",
    "returnToWorkDate": "2026-05-13",
    "timeScopeList": [
      { "type": "BASE", "start": "2025-11-01", "end": "2026-02-06", "days": 98 },
      { "type": "EXTENDED", "start": "2026-02-07", "end": "2026-03-28", "days": 50 },
      { "type": "DYSTOCIA", "start": "2026-03-29", "end": "2026-04-27", "days": 30 },
      { "type": "MULTI_BABY", "start": "2026-04-28", "end": "2026-05-12", "days": 15 }
    ]
  }
}
```

> 说明：示例中的 `totalDays/baseDays/bonusDays` 仅为演示，实际结果以对应策略计算为准（难产、多胞胎天数叠加规则见上表）。

## 调用说明

- Header：`Content-Type: application/json`
- URL：`POST /api/maternity-leave/calculate`
- 统一返回由 `GlobalResponseAdvice` 自动包装，无需在控制器内手动构造。
