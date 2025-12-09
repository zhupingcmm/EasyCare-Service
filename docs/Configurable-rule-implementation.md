# 产假规则配置化实现文档

## 概述
- 产假规则配置化，产假政策变化，只需修改产假规则，无需修改代码
- 产假和津贴计算，支持通过配置，支持新增城市的产假和津贴计算


## 实现内容
### 1. 实现功能
- 实现产假规则 t_maternity_rules 的增、删、改、查 以及 批量导入、导出 5个API
- 查询API， payload 输入条件为城市，可以查出对应城市的所有规则 


### 2. 产假规则表结构

```postgres-sql
CREATE TABLE t_maternity_rules (
    id                SERIAL PRIMARY KEY, 
    city              VARCHAR(50) NOT NULL,  
    leave_type        VARCHAR(64) NOT NULL,
    leave_subtype     VARCHAR(64) DEFAULT NULL,
    leave_days        INTEGER NOT NULL CHECK (leave_days > 0),
    is_extendable     BOOLEAN NOT NULL DEFAULT FALSE,
    has_allowance     BOOLEAN NOT NULL DEFAULT TRUE,
    is_default        BOOLEAN NOT NULL DEFAULT FALSE,
    radio_group       INTEGER NOT NULL DEFAULT 0
);
CREATE UNIQUE INDEX idx_t_maternity_rules_unique ON t_maternity_rules (city, leave_type, COALESCE(leave_subtype, ''));

-- 注释
COMMENT ON TABLE t_maternity_rules IS '产假规则表';
COMMENT ON COLUMN t_maternity_rules.id IS '主键ID';
COMMENT ON COLUMN t_maternity_rules.city IS '城市';
COMMENT ON COLUMN t_maternity_rules.leave_type IS '假期类型';
COMMENT ON COLUMN t_maternity_rules.leave_subtype IS '假期子类型';
COMMENT ON COLUMN t_maternity_rules.leave_days IS '假期天数';
COMMENT ON COLUMN t_maternity_rules.is_extendable IS '是否节假日顺延';
COMMENT ON COLUMN t_maternity_rules.has_allowance IS '是否有津贴';
COMMENT ON COLUMN t_maternity_rules.is_default IS '是否默认选择';
COMMENT ON COLUMN t_maternity_rules.radio_group IS '单选分组标识';
```

### 3. API接口
- 产假详情API接口说明

```json
POST /api/leave/info
Content-Type: application/json

request
{"city"："广州"}

Response
{
    "city": "广州",
    "miscarrage": [

        {"key":
         value:"妊娠未满4个月流产",
        "妊娠满4个月流产",
        "怀孕满7个月终止妊娠"，
    ],
    "birth": [
        {
            "type": "难产",
            "subType": [
                "吸引产、钳产、臀位牵引产",
                "剖腹产、会阴Ⅲ度破裂"
            ]
        },
        {
            "type": "奖励假",
            "subtype": [
                "生育一孩",
                "生育二孩、三孩"
            ]
        },
        {
            "type": "多胞胎",
            "subtype": [
                "双胞胎",
                "三胞胎",
                "四胞胎"
            ]
        }
    ]
}
```
### 4. 津贴生育津贴计算
#### 基数差异
- 通用： 用人单位上年度月平均缴费工资/30*产假天数
- 成都： 用人单位上年度月平均缴费工资/*12/365*产假天数
- 天津： 用人单位上年度月平均缴费工资/30.4*产假天数
#### 计算分类
- 津贴发放到个人账户，需要计算返还
- 津贴发放到企业账户，无需计算返还

#### 需澄清
- 津贴发放到企业，每月正常发放工资，已发工资是HR根据已发工资填写，还是需要工具重新计算
- 公积金，社保，工资，ESPP 的变更日期，可配置
- 

### 4. 津贴生育津贴计算基数


### 5. 津贴规则表结构

```postgres-psql
-- Table: t_allowance_rules
CREATE TABLE t_allowance_rules (
    id                      SERIAL PRIMARY KEY,
    city                    VARCHAR(50) NOT NULL,
    payout_method           VARCHAR(20) NOT NULL,
    is_active               BOOLEAN NOT NULL DEFAULT true,
    is_need_compensation    BOOLEAN DEFAULT true,
    salary_adjust_month     INTEGER DEFAULT 4,
    social_adjust_month     INTEGER DEFAULT 7,
    month_days              INTEGER DEFAULT 7,
    create_date             TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by               VARCHAR(100),
    update_date             TIMESTAMP,
    update_by               VARCHAR(100)
);

CREATE UNIQUE INDEX idx_t_allowance_rules_city ON t_allowance_rules (city);
```

### 6. 津贴规则表结构


