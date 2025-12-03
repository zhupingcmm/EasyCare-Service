# 1. 需求

# 2. 架构图

![](https://tcs-devops.aliyuncs.com/storage/113o0622d71a297c752aeba3be9f291ef09c?Signature=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJBcHBJRCI6IjVlNzQ4MmQ2MjE1MjJiZDVjN2Y5YjMzNSIsIl9hcHBJZCI6IjVlNzQ4MmQ2MjE1MjJiZDVjN2Y5YjMzNSIsIl9vcmdhbml6YXRpb25JZCI6IiIsImV4cCI6MTc2NTM2MjcyOCwiaWF0IjoxNzY0NzU3OTI4LCJyZXNvdXJjZSI6Ii9zdG9yYWdlLzExM28wNjIyZDcxYTI5N2M3NTJhZWJhM2JlOWYyOTFlZjA5YyJ9.blbc0HxNd2V-_6L3Cs8Bu1PD-7a2o_OrfutSuJihnl4&download=easycare.drawio.png "")



# 3. 流程图



![](https://tcs-devops.aliyuncs.com/storage/113o603c347214b29d636b921d1315c53aca?Signature=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJBcHBJRCI6IjVlNzQ4MmQ2MjE1MjJiZDVjN2Y5YjMzNSIsIl9hcHBJZCI6IjVlNzQ4MmQ2MjE1MjJiZDVjN2Y5YjMzNSIsIl9vcmdhbml6YXRpb25JZCI6IiIsImV4cCI6MTc2NTM2MjcyOCwiaWF0IjoxNzY0NzU3OTI4LCJyZXNvdXJjZSI6Ii9zdG9yYWdlLzExM282MDNjMzQ3MjE0YjI5ZDYzNmI5MjFkMTMxNWM1M2FjYSJ9.vJH8sNrYCxMEBCEXJmapxg6Uwv4L8zzu7xRNgfU0wao&download=easycare-flow.drawio.png "")



# 表结构设计

## ER图

![](https://tcs-devops.aliyuncs.com/storage/113o1ca0f6a8e2a851d86760a090546b843e?Signature=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJBcHBJRCI6IjVlNzQ4MmQ2MjE1MjJiZDVjN2Y5YjMzNSIsIl9hcHBJZCI6IjVlNzQ4MmQ2MjE1MjJiZDVjN2Y5YjMzNSIsIl9vcmdhbml6YXRpb25JZCI6IiIsImV4cCI6MTc2NTM2MjcyOCwiaWF0IjoxNzY0NzU3OTI4LCJyZXNvdXJjZSI6Ii9zdG9yYWdlLzExM28xY2EwZjZhOGUyYTg1MWQ4Njc2MGEwOTA1NDZiODQzZSJ9.MspikEFAvd9-MUeNkX376jihdtvpFaFCYbS-4ZnL9sk&download=%E6%9C%AA%E5%91%BD%E5%90%8D%E5%9B%BE%E8%A1%A8-2025-12-03T07-06-48.png "")



## 表数据样例

### t_city 城市表

| id  | code | cn_name | en_name   | province | enabled | sort_order | created_date        | created_by | updated_date        | updated_by |
| --- | ---- | ------- | --------- | -------- | ------- | ---------- | ------------------- | ---------- | ------------------- | ---------- |
| 1   | SH   | 上海      | Shanghai  | 上海       | true    | 2          | 2024-01-12 11:20:00 | admin      | 2024-02-05 15:10:00 | admin      |
| 3   | GZ   | 广州      | GuangZhou | 广东       | true    | 4          | 2024-01-18 09:30:00 | admin      | 2024-02-10 16:20:00 | admin      |
| 5   | CD   | 绍兴      | Shaoxing  | 浙江       | true    | 6          | 2024-01-25 10:10:00 | system     | 2024-02-18 09:30:00 | system     |



### t_maternity_leave_type 产假类型表

| id  | code | name  | remark      | enabled |
| --- | ---- | ----- | ----------- | ------- |
| 1   | 1001 | 法定产假  | 国家规定的基础产假   | true    |
| 2   | 1002 | 难产假   | 难产情形可额外增加天数 | true    |
| 3   | 1003 | 多胞胎产假 | 双胞胎或多胞胎额外假期 | true    |
| 4   | 1004 | 奖励假   |             |         |
| 5   | 1005 | 流产假   |             |         |



### t_maternity_rule 产假规则表

| id  | city_id | city_name | maternity_leave_type_id | maternity_leave_type_name | default_days | maternity_leave_ext                       | has_allowance | allowance_id | doctor_recommend_days |
| --- | ------- | --------- | ----------------------- | ------------------------- | ------------ | ----------------------------------------- | ------------- | ------------ | --------------------- |
| 1   | 1       | 上海        | 1                       | 法定产假                      | 98           |                                           | true          | 15           |                       |
| 2   | 3       | 广州        | 2                       | 难产假                       |              | [ {    "code":"dys_002",    "days":30  }] | true          | 11           | 20                    |
| 3   | 1       | 绍兴        | 4                       | 奖励假                       |              | [ {    "code":"awd_001",    "days":30  }] |               |              |                       |
| 4   | 3       | 广州        | 5                       | 流产假                       |              | [ {    "code":"mc_001",    "days":30  }]  | false         | null         |                       |



## t_special_day 特殊日期表

| id  | date       | region | type(1节假日；2补班) | public_holiday(是否国定) | enabled |
| --- | ---------- | ------ | -------------- | -------------------- | ------- |
| 1   | 2025-01-01 | cn     | 1              | true                 | true    |
| 2   | 2025-01-02 | cn     | 1              | true                 | true    |
| 3   | 2025-01-03 | cn     | 2              | false                | true    |



## t_maternity_leave_request 产假计算请求表

| id  | lan_id | employee_name | city_id | city_name | is_dystocia | dystocia_type | num_of_fetuses | num_of_kids | is_breast_feeding | is_miscarriage | miscarrage_code | miscarriage_ext | expected_delivery_date | maternity_leave_start_date |
| --- | ------ | ------------- | ------- | --------- | ----------- | ------------- | -------------- | ----------- | ----------------- | -------------- | --------------- | --------------- | ---------------------- | -------------------------- |
| 1   | L002   | Li Na         | 1       | 上海        | true        | 2             | 1              | 1           | false             | false          |                 | null            | 2024-06-02             | 2024-05-20                 |
| 2   | L003   | Wang Fang     | 3       | 广州        | false       | null          | 0              | 0           | false             | true           | mc_001          | {}              | null                   | 2024-02-10                 |

## t_maternity_leave_response  产假计算结果表

| id  | request_id | lan_id | employee_name | city_id | city_name | total_days | total_allowance_days | base_days | dystocia_days | multi_fetuses_days | awarded_days | miscarriage_leave_days | pub_holidays_count | start_date | end_date   | return_to_work_date |
| --- | ---------- | ------ | ------------- | ------- | --------- | ---------- | -------------------- | --------- | ------------- | ------------------ | ------------ | ---------------------- | ------------------ | ---------- | ---------- | ------------------- |
| 1   | 1          | L002   | Li Na         | 1       | 上海        | 158        | 128                  | 98        | 30            | 0                  | 30           | 0                      | 5                  | 2024-05-20 | 2024-10-24 | 2024-10-25          |
| 2   | 2          | L003   | Wang Fang     | 3       | 广州        | 14         | 0                    | 0         | 0             | 0                  | 0            | 14                     | 0                  | 2024-02-10 | 2024-02-23 | 2024-02-24          |



## t_maternity_allowance_request  津贴计算请求表

| id  | lan_id | employee_name | city_id | city_name | unit_monthly_average_salary | monthly_base_salary | adjusted_monthly_base_salary | adjust_salary_date | maternity_leave_days | maternity_leave_start_date | maternity_leave_end_date | maternity_leave_request_date | government_allowance | company_advance  | issue_allowance_date（津贴发放时间） | reuest_date         |
| --- | ------ | ------------- | ------- | --------- | --------------------------- | ------------------- | ---------------------------- | ------------------ | -------------------- | -------------------------- | ------------------------ | ---------------------------- | -------------------- | ---------------- | ---------------------------- | ------------------- |
| 2   | L002   | Li Na         | 1       | 上海        | 20000.00                    | 16000.00            | 16500.00                     | 2024-07-01         | 158                  | 2024-05-20                 | 2024-10-24               | 2024-05-01                   | 52000.00             | {"advance":8000} |                              | 2024-05-01 11:20:00 |
| 3   | L003   | Wang Fang     | 3       | 广州        | 12000.00                    | 10000.00            | 10000.00                     | 2024-07-01         | 14                   | 2024-02-10                 | 2024-02-23               | 2024-02-08                   | 0.00                 | {"advance":0}    |                              | 2024-02-08 14:30:00 |



## t_maternity_allowance_repsonse  津贴计算结果表

| id  | requestId | lan_id | employee_name | city_id | city_name | allowance_days | extra_allowance | maternity_allowance | compensation_amount | paid_maternity_wage | employee_refund_amount | allowance_compensation_details                                                            | refund_details | created_date        |
| --- | --------- | ------ | ------------- | ------- | --------- | -------------- | --------------- | ------------------- | ------------------- | ------------------- | ---------------------- | ----------------------------------------------------------------------------------------- | -------------- | ------------------- |
| 1   | 1         | L002   | Li Na         | 1       | 上海        | 128            | 2000.00         | 18500.00            | 1500.00             | 21000.00            | 0.00                   | {"base_days":98,"extra_days":30,"multiple_birth_bonus":2000,"calc_notes":"上海标准日薪 × 假期天数"} | {”"}           | 2025-01-12 10:30:00 |
| 2   | 2         | L003   | Wang Fang     | 3       | 广州        | 98             | 0.00            | 15000.00            | 3500.00             | 18500.00            | 500.00                 | {"base_days":98,"extra_days":0,"calc_notes":"广州市平均工资换算"}                                  | {}             | 2025-02-03 14:12:22 |

## 数据字典

### 产假相关

| code                | name | remark |
| ------------------- | ---- | ------ |
| statutoryLeave      | 法定产假 |        |
| dystociaLeave       |  难产假 |        |
| awardLeave          | 奖励假  |        |
| multipleLeave       | 多胞胎假 |        |
| extensionDays       | 顺延天数 |        |
| doctorRecommendDays | 医嘱天数 |        |
| pregencyDays        | 妊娠天数 |        |
| pregencyMonths      | 妊娠月数 |        |

### 难产

| code    | name         | remark |
| ------- | ------------ | ------ |
| dys_001 | 默认类型         |        |
| dys_002 | 剖腹产、会阴Ⅲ度破裂   |        |
| dys_003 | 吸引产、钳产、臀位牵引产 |        |

### 流产假

| code   | name                   | expression             | remark |
| ------ | ---------------------- | ---------------------- | ------ |
| mc_001 | 妊娠未满4个月                | pregencyMonths < 4     |        |
| mc_002 |  妊娠满4个月                | pregencyMonths >=4     |        |
| mc_003 |  妊娠4个月以上7个月以下          | 4 < pregencyMonths <7  |        |
| mc_004 |  妊娠怀孕4个月以上（含4个月）至7个月以下 | 4 <= pregencyMonths <7 |        |
| mc_005 | 怀孕满7个月                 | pregencyMonths >=7     |        |



### 奖励假

| code    | name | remark |
| ------- | ---- | ------ |
| awd_001 | 一孩   |        |
| awd_002 | 二孩   |        |
| awd_003 | 三孩   |        |

# 产假时间计算

## 产假天数 

totalDays = 法定产假天数 + (医嘱天数 || 难产假天数) + (胎儿个数 * 多胞胎奖励假天数) + 奖励假 + 顺延天数

```text
totalDays = statutoryLeaveDays + (doctorRecommendDays!=null? doctorRecommendDays:dystociaLeaveDays)+ (numOfFetuses * fetusRewardLeaveDays) + awardLeaveDays + extensionDays

```



## 产假结束时间计算

```text
// endDate 产假结束时间
while ( (endDate - startDate) contains 节假日){
        endDate  = endDate.add(节假日天数)
       startDate = endDate
}
return endDate
```

# 津贴计算

## 生育津贴

    公司员工平均工资计算津贴 = （单位上年度员工月平均工资 ÷ 30 天）× 产假天数

    员工个人平均工资计算津贴 = （员工产假前12个月平均工资 ÷ 30 天）× 产假天数

    政府发放的津贴：手动输入的

    实际津贴数额 = 以上三者就高原则 （应享受津贴数额 > 政府贴  才会产生补差）



## 公司已支付工资

    公司已支付的工资：根据基础工资和工作时间折算得到的，

        如果产假假期跨越了调薪月，那么调薪月之前月份已支付的工资用调薪之前的基础工资按工作日折算；调薪月以及调薪月之后的月份用调薪后基础工资按工作日折算。

```text
这个按工作日折算的逻辑：如果这个月工作日满足22天（当月计薪天数），就是支付一个月的工资？
            例如：
                调薪前基础工资：20000
                调薪后基础工资：21000
                调薪月是4月
                产假开始时间：2025-02-16
                产假结束时间：2025-07-24
就是说从 2025-02-16 --> 2025-02-28  公司已支付的工资是：（20000 / 当月计薪天数）* 10(个工作日)
                2025年3月支付 20000
                2025年4月支付 21000
                2025年5月支付 21000
                2025年6月支付 21000
                2025年7月支付 (21000 / 当月计薪天数) * 18(个工作日)

```

         

##  公司需补差额

    公司需补差额 = 实际津贴数额 - 公司已支付的工资 (- 政府已发放)?是不是政府已经发给公司了？

        如果政府津贴发到公司，则公司会支付工资，这个时候不需要员工返还金额

        如果政府津贴发放到个人的，则就意味着公司提前垫付了社保公积金，工会费，ESPP的，所以员工员工返还。



##  员工需返还金额

    员工需返还金额

        例如：

            调薪前基础工资：20000

            调薪后基础工资：21000

            调薪月是4月

            社保调整月是7月

            产假开始时间：2025-02-16

            产假结束时间：2025-07-28



            判断产假开始和产假结束的时间点上 是否已经缴纳了 社保，公积金，工会费以及 espp 的费用

            就是说，如果该员工在这个月内只工作了几天，那么这个月内公司为该员工缴纳的 社保，公积金，工会费以及 espp 的费用 是否需要返还？



            espp 页面输入多少就是多少就是多少，不跟调薪有关。

            espp 有可能在24月截至到了：

                例如：产假 2月  到   8月

                    但是espp到6月份截至了，就是说7月8月没有espp的值

                    espp有可能连续两个期，所以espp有可能出现两个变化



            2025-02-16 --> 2025-02-28  

                需要判断这个月内缴纳的 社保，公积金，工会费以及 espp 的费用 是否需要返还？

                    这个是看这个月的需要发放的工资是否能够覆盖掉社保，公积金，工会费，espp等，如果不够的话，差额就需要返还给公司

                    临界月都需要这样算

            2025年3月公司代缴纳

                以20000为基数缴纳的：社保，公积金，工会费，espp

            2025年4月公司代缴纳

                以20000为基数缴纳的：社保，公积金

                以21000为基数缴纳的：工会费，espp

            2025年5月公司代缴纳

                以20000为基数缴纳的：社保，公积金

                以21000为基数缴纳的：工会费，espp

            2025年6月公司代缴纳

                以20000为基数缴纳的：社保，公积金

                以21000为基数缴纳的：工会费，espp

            2025年7月支付 (21000 / 22) * 18(个工作日)

                需要判断这个月内缴纳的 社保，公积金，工会费以及 espp 的费用 是否需要返还？

                以21000为基数缴纳的：社保，公积金

                以21000为基数缴纳的：工会费，espp

# Apis

### 产假计算

API:  api/maternity-leave/calculate

method: POST

Request

```json
{
  "id": 1001,
  "lanId": "E50001",
  "employeeName": "王丽",
  "cityId": 1,
  "cityName": "上海",
  "isDystocia": true,
  "dystociaType": 2,
  "numOfFetuses": 2,
  "numOfKids": 0,
  "isBreastFeeding": true,
  "isMiscarriage": false,
  "pregencyDays": 280,
  "miscarriageExt": null,
  "expectedDeliveryDate": "2025-06-15T00:00:00",
  "maternityLeaveStartDate": "2025-06-01T00:00:00",
  "createdDte": "2025-03-01T10:00:00",
  "createdBy": "HR_Admin",
  "updatedDate": "2025-03-01T10:00:00",
  "updatedBy": "HR_Admin"
}


```

Response:

```text
{
  "id": 2001,
  "requestId": 1001,
  "lanId": "E50001",
  "employeeName": "王丽",
  "cityId": 310000,
  "cityName": "上海",
  "totalDays": 158,
  "totalAllowanceDays": 143,
  "baseDays": 128,
  "dystociaDays": 15,
  "multiFetusesDays": 10,
  "awardedDays": 5,
  "miscarriageLeaveDays": 0,
  "pubHolidaysCount": 2,
  "startDate": "2025-06-01T00:00:00",
  "endDate": "2025-11-06T00:00:00",
  "returnToWorkDate": "2025-11-07T00:00:00",
  "timeScopeList": [
    {
      "type": "base",
      "startDate": "2025-06-01T00:00:00",
      "endDate": "2025-10-06T00:00:00",
      "days": 128
    },
    {
      "type": "dystocia",
      "startDate": "2025-10-07T00:00:00",
      "endDate": "2025-10-21T00:00:00",
      "days": 15
    },
    {
      "type": "multiFetuses",
      "startDate": "2025-10-22T00:00:00",
      "endDate": "2025-11-01T00:00:00",
      "days": 10
    },
    {
      "type": "awarded",
      "startDate": "2025-11-02T00:00:00",
      "endDate": "2025-11-06T00:00:00",
      "days": 5
    }
  ],
  "createdDate": "2025-03-01T10:00:00",
  "createdBy": "HR_Admin",
  "updatedDate": "2025-03-01T10:00:00",
  "updatedBy": "HR_Admin"
}


```

### 津贴计算

API:  api/maternity-allowance/calculate

method: POST

Request

```json
{
  "id": 3001,
  "lanId": "E60001",
  "employeeName": "赵敏",
  "cityId": 310000,
  "cityName": "上海",
  "unitMonthlyAverageSalary": 23125.00,
  "monthlyBaseSalary": 25000.00,
  "adjustedMonthlyBaseSalary": 25500.00,
  "maternityLeaveDays": 128,
  "maternityLeaveStartDate": "2025-06-01T00:00:00",
  "maternityLeaveEndDate": "2025-10-06T00:00:00",
  "maternityLeaveRequestDate": "2025-03-15T09:00:00",
  "governmentAllowance": 29600.00,
  "companyAdvance": {
    "advanceAmount": 5000.00,
    "reason": "社保"
  },
  "createdDate": "2025-03-15T09:00:00",
  "createdBy": "HR_Admin",
  "updatedDate": "2025-03-15T09:00:00",
  "updatedBy": "HR_Admin"
}


```

Response



```json
{
  "id": 4001,
  "requestId": 3001,
  "lanId": "E60001",
  "employeeName": "赵敏",
  "cityId": 310000,
  "cityName": "上海",
  "allowanceDays": 128,
  "extraAllowance": 2000.00,
  "maternityAllowance": 29600.00,
  "compensationAmount": 1400.00,
  "paidMaternityWage": 31000.00,
  "employeeRefundAmount": 0.00,
  "allowanceCompensationDetails": null,
  "refundDetails": null,
  "createdDate": "2025-03-15T09:00:00",
  "createdBy": "HR_Admin",
  "updatedDate": "2025-03-15T09:00:00",
  "updatedBy": "HR_Admin"
}


```

### 历史记录查询

### 配置

