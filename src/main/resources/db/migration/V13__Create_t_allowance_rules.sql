CREATE TABLE t_allowance_rules (
    id                      SERIAL PRIMARY KEY,
    city                    VARCHAR(50) NOT NULL,
    payout_method           VARCHAR(20) NOT NULL,
    is_active               BOOLEAN NOT NULL DEFAULT true,
    create_date             TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by               VARCHAR(100),
    update_date             TIMESTAMP,
    update_by               VARCHAR(100)
);

-- 创建唯一索引
CREATE UNIQUE INDEX idx_t_allowance_rules_city ON t_allowance_rules (city) WHERE is_active = true;

-- 添加注释
COMMENT ON TABLE t_allowance_rules IS '津贴规则表';
COMMENT ON COLUMN t_allowance_rules.id IS '主键ID';
COMMENT ON COLUMN t_allowance_rules.city IS '城市';
COMMENT ON COLUMN t_allowance_rules.payout_method IS '发放方式';
COMMENT ON COLUMN t_allowance_rules.is_active IS '是否激活（用于逻辑删除）';
COMMENT ON COLUMN t_allowance_rules.create_date IS '创建时间';
COMMENT ON COLUMN t_allowance_rules.create_by IS '创建人';
COMMENT ON COLUMN t_allowance_rules.update_date IS '更新时间';
COMMENT ON COLUMN t_allowance_rules.update_by IS '更新人';