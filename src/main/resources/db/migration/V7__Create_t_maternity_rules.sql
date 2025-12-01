DROP TABLE IF EXISTS t_maternity_rules;
-- =============================================
-- 产假规则表（使用字符串字段，不使用外键）
-- =============================================
CREATE TABLE t_maternity_rules (
    id                    SERIAL PRIMARY KEY,
    city                  VARCHAR(50) NOT NULL,
    maternity_leave_type  VARCHAR(100) NOT NULL,
    abortion_leave_type   VARCHAR(100) DEFAULT NULL,
    leave_days            INTEGER NOT NULL CHECK (leave_days > 0),
    is_extendable         BOOLEAN NOT NULL DEFAULT FALSE,
    has_allowance         BOOLEAN NOT NULL DEFAULT TRUE,
    is_default            BOOLEAN NOT NULL DEFAULT FALSE,
    radio_group           INTEGER NOT NULL DEFAULT 0,
    is_active             BOOLEAN NOT NULL DEFAULT TRUE,
    -- 审计字段
    create_date           TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by             VARCHAR(100) DEFAULT 'system',
    update_date           TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_by             VARCHAR(100) DEFAULT 'system'
);

-- 索引
CREATE UNIQUE INDEX idx_t_maternity_rules_unique ON t_maternity_rules (city, maternity_leave_type, COALESCE(abortion_leave_type, ''));
CREATE INDEX idx_t_maternity_rules_maternity_type ON t_maternity_rules (maternity_leave_type);
CREATE INDEX idx_t_maternity_rules_abortion_type ON t_maternity_rules (abortion_leave_type);

-- 注释
COMMENT ON TABLE t_maternity_rules IS '产假规则表';
COMMENT ON COLUMN t_maternity_rules.id IS '主键ID';
COMMENT ON COLUMN t_maternity_rules.city IS '城市';
COMMENT ON COLUMN t_maternity_rules.maternity_leave_type IS '产假类型（如：产假、陪产假）';
COMMENT ON COLUMN t_maternity_rules.abortion_leave_type IS '流产类型（如：早期流产、晚期流产，可为空）';
COMMENT ON COLUMN t_maternity_rules.leave_days IS '假期天数';
COMMENT ON COLUMN t_maternity_rules.is_extendable IS '是否节假日顺延';
COMMENT ON COLUMN t_maternity_rules.has_allowance IS '是否有津贴';
COMMENT ON COLUMN t_maternity_rules.is_default IS '是否默认选择';
COMMENT ON COLUMN t_maternity_rules.radio_group IS '单选分组标识';
COMMENT ON COLUMN t_maternity_rules.is_active IS '是否启用';
COMMENT ON COLUMN t_maternity_rules.create_date IS '创建时间';
COMMENT ON COLUMN t_maternity_rules.create_by IS '创建人';
COMMENT ON COLUMN t_maternity_rules.update_date IS '更新时间';
COMMENT ON COLUMN t_maternity_rules.update_by IS '更新人';
