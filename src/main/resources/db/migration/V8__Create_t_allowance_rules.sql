DROP TABLE IF EXISTS t_allowance_rules;
CREATE TABLE t_allowance_rules (
    id                  SERIAL PRIMARY KEY,
    city_id             INTEGER NOT NULL DEFAULT 1,
    city_name           VARCHAR(50) NOT NULL,
    payout_method       INTEGER NOT NULL DEFAULT 1,
    enabled             BOOLEAN NOT NULL DEFAULT TRUE,
    need_compensation   BOOLEAN DEFAULT TRUE,
    salary_adjust_month INTEGER DEFAULT 4,
    social_adjust_month INTEGER DEFAULT 7,
    month_days          INTEGER DEFAULT 30,
    create_date         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by           VARCHAR(100),
    update_date         TIMESTAMP,
    update_by           VARCHAR(100)
);

-- 添加注释
COMMENT ON TABLE t_allowance_rules IS '津贴规则表';
COMMENT ON COLUMN t_allowance_rules.id IS '主键ID';
COMMENT ON COLUMN t_allowance_rules.city_id IS '城市';
COMMENT ON COLUMN t_allowance_rules.city_name IS '城市名称';
COMMENT ON COLUMN t_allowance_rules.payout_method IS '发放方式: 1 个人账户，2 单位账户';
COMMENT ON COLUMN t_allowance_rules.enabled IS '是否激活（用于逻辑删除）';
COMMENT ON COLUMN t_allowance_rules.need_compensation IS '是否需要补差';
COMMENT ON COLUMN t_allowance_rules.salary_adjust_month IS '薪资调整月份';
COMMENT ON COLUMN t_allowance_rules.social_adjust_month IS '社保调整月份';
COMMENT ON COLUMN t_allowance_rules.month_days IS '每个月的天数，用于计算日薪';
COMMENT ON COLUMN t_allowance_rules.create_date IS '创建时间';
COMMENT ON COLUMN t_allowance_rules.create_by IS '创建人';
COMMENT ON COLUMN t_allowance_rules.update_date IS '更新时间';
COMMENT ON COLUMN t_allowance_rules.update_by IS '更新人';