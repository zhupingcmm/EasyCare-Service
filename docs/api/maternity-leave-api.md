# 产假计算 API 文档

## 基础信息

- **基础路径**: `/api/maternity-leave`
- **Controller**: `MaternityLeaveController`
- **标签**: 产假计算

---

## API 列表

### 1. 计算产假天数

根据城市和个人情况计算产假天数。

#### 请求信息

- **URL**: `/api/maternity-leave/calculate`
- **方法**: `POST`
- **Content-Type**: `application/json`

#### 请求参数 (MaternityLeaveRequest)

| 字段名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| lanId | String | 是 | 工号 | "EMP001" |
| employeeName | String | 是 | 姓名 | "张三" |
| cityCode | String | 是 | 城市代码 | "SH" |
| expectedDeliveryDate | LocalDate | 是 | 预产期 | "2024-06-15" |
| doctorRecommendDays | Integer | 否 | 医嘱天数 | 15 |
| difficultBirthLeaveDetail | Object | 否 | 难产类型详情 | - |
| isMultipleBirth | Boolean | 否 | 是否多胞胎 | false |
| numberOfBabies | Integer | 是 | 婴儿数量（必须>0） | 1 |
| hasExtendedDays | Boolean | 是 | 是否有晚育假/生育假/奖励假 | false |
| isDifficultBirth | Boolean | 是 | 是否难产 | false |
| additionalDystociaDays | Integer | 否 | 难产假期天数（广州15/30天） | 0 |
| isBreastFeeding | Boolean | 否 | 是否母乳喂养（成都：母乳多一个月） | false |
| numberOfKids | Integer | 否 | 孩子个数 | 1 |
| isMiscarriage | Boolean | 是 | 是否流产 | false |
| isFirstTimeBirth | Boolean | 否 | 是否生育一孩（仅绍兴） | true |
| miscarriageLeaveDetail | Object | 否 | 流产假细节 | - |

#### 请求示例

```json
{
  "lanId": "EMP001",
  "employeeName": "张三",
  "cityCode": "SH",
  "expectedDeliveryDate": "2024-06-15",
  "doctorRecommendDays": 0,
  "isMultipleBirth": false,
  "numberOfBabies": 1,
  "hasExtendedDays": true,
  "isDifficultBirth": false,
  "additionalDystociaDays": 0,
  "isBreastFeeding": false,
  "isMiscarriage": false,
  "isFirstTimeBirth": false
}
```

#### 响应参数 (MaternityLeaveResponse)

| 字段名 | 类型 | 说明 |
|--------|------|------|
| requestId | Long | 申请记录ID |
| resultId | Long | 计算结果ID |
| lanId | String | 工号 |
| employeeName | String | 姓名 |
| cityCode | String | 城市代码 |
| cityName | String | 城市名称 |
| totalDays | Integer | 产假总天数 |
| totalAllowanceDays | Integer | 津贴总天数 |
| baseDays | Integer | 1. 基础产假天数 |
| dystociaDays | Integer | 2. 难产假天数 |
| multiBabyDays | Integer | 3. 多胞胎假天数 |
| extendedDays | Integer | 4. 晚育假/生育假/奖励假天数 |
| miscarriageLeaveDays | Integer | 流产假天数 |
| pubHolidaysCount | Integer | 公共节假日顺延天数（4 晚育假/生育假/奖励假期间的） |
| startDate | LocalDate | 产假开始日期 |
| endDate | LocalDate | 产假结束日期 |
| returnToWorkDate | LocalDate | 返岗日期（endDate之后的第一个工作日） |
| timeScopeList | List<TimeScope> | 每个假期时间段的详情 |

#### 响应示例

```json
{
  "requestId": 1001,
  "resultId": 2001,
  "lanId": "EMP001",
  "employeeName": "张三",
  "cityCode": "SH",
  "cityName": "上海",
  "totalDays": 158,
  "totalAllowanceDays": 158,
  "baseDays": 98,
  "dystociaDays": 0,
  "multiBabyDays": 0,
  "extendedDays": 60,
  "miscarriageLeaveDays": 0,
  "pubHolidaysCount": 0,
  "startDate": "2024-05-16",
  "endDate": "2024-10-20",
  "returnToWorkDate": "2024-10-21",
  "timeScopeList": [
    {
      "type": "BASE",
      "days": 98,
      "startDate": "2024-05-16",
      "endDate": "2024-08-21"
    },
    {
      "type": "EXTENDED",
      "days": 60,
      "startDate": "2024-08-22",
      "endDate": "2024-10-20"
    }
  ]
}
```

#### 错误响应

```json
{
  "code": 400,
  "message": "参数校验失败: 工号不能为空",
  "data": null
}
```

---

### 2. 查询流产假规则

查询所有城市关于流产假的休假天数的规则列表。

#### 请求信息

- **URL**: `/api/maternity-leave/ref-data/miscarriage-rules`
- **方法**: `GET`

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| cityCode | String | 否 | 城市代码（用于过滤特定城市的规则） | "SH" |

#### 请求示例

```
GET /api/maternity-leave/ref-data/miscarriage-rules?cityCode=SH
```

#### 响应参数 (List<MiscarriageLeaveDetail>)

| 字段名 | 类型 | 说明 |
|--------|------|------|
| cityCode | String | 城市代码 |
| code | String | 规则代码 |
| days | Integer | 休假天数 |
| needOverrideDays | Boolean | 是否需要前端覆盖天数值 |
| description | String | 规则描述 |

#### 响应示例

```json
[
  {
    "cityCode": "SH",
    "code": "MISCARRIAGE_4M",
    "days": 15,
    "needOverrideDays": false,
    "description": "怀孕满4个月流产"
  },
  {
    "cityCode": "SH",
    "code": "MISCARRIAGE_7M",
    "days": 42,
    "needOverrideDays": false,
    "description": "怀孕满7个月流产"
  }
]
```

---

## 数据模型

### MiscarriageLeaveDetail

流产假详情对象

```java
{
  "cityCode": "SH",           // 城市代码
  "code": "MISCARRIAGE_4M",   // 规则代码
  "days": 15,                 // 休假天数
  "needOverrideDays": false,  // 是否需要前端覆盖天数值
  "description": "怀孕满4个月流产"  // 规则描述
}
```

### DifficultBirthLeaveDetail

难产假详情对象（结构待补充）

---

## 业务规则说明

### 产假计算规则

产假总天数由以下部分组成：

1. **基础产假天数 (baseDays)**: 根据城市规定的基础产假天数
2. **难产假天数 (dystociaDays)**: 如果是难产，额外增加的天数
3. **多胞胎假天数 (multiBabyDays)**: 多胞胎情况下，每多一个婴儿增加的天数
4. **晚育假/生育假/奖励假 (extendedDays)**: 符合条件时的额外奖励假期
5. **流产假天数 (miscarriageLeaveDays)**: 流产情况下的假期天数

**计算公式**:
```
totalDays = baseDays + dystociaDays + multiBabyDays + extendedDays + miscarriageLeaveDays
```

### 特殊城市规则

- **广州**: 难产假可选15天或30天 (`additionalDystociaDays`)
- **成都**: 母乳喂养可额外增加一个月假期 (`isBreastFeeding`)
- **绍兴**: 需要区分是否生育一孩 (`isFirstTimeBirth`)

### 公共节假日顺延

在晚育假/生育假/奖励假期间遇到的公共节假日会顺延，计入 `pubHolidaysCount`。

---

## 错误码说明

| 错误码 | 说明 |
|--------|------|
| 0 | 成功 |
| 400 | 参数校验失败 |
| 500 | 服务器内部错误 |

---

## 注意事项

1. 所有日期格式使用 ISO 8601 标准: `YYYY-MM-DD`
2. 必填字段不能为空，否则返回 400 错误
3. `numberOfBabies` 必须大于 0
4. 如果 `isMiscarriage=true`，需要传入 `miscarriageLeaveDetail` 对象
5. 返岗日期 (`returnToWorkDate`) 为产假结束日期后的第一个工作日

---

## 版本历史

| 版本 | 日期 | 说明 |
|------|------|------|
| 1.0 | 2026-01-03 | 初始版本 |
