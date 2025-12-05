DROP TABLE IF EXISTS t_maternity_rules CASCADE;

-- 产假规则表（使用外键关联）
CREATE TABLE IF NOT EXISTS t_maternity_rules (
    id                    SERIAL PRIMARY KEY,
    city_id               INTEGER NOT NULL,
    maternity_leave_type_id  INTEGER NOT NULL,
    default_days          INTEGER NOT NULL CHECK (default_days > 0),
    doctor_recommend_days INTEGER,
    maternity_leave_ext   JSONB,
    is_extendable         BOOLEAN NOT NULL DEFAULT FALSE,
    holiday_extend        BOOLEAN NOT NULL DEFAULT FALSE,
    has_allowance         BOOLEAN NOT NULL DEFAULT TRUE,
    enabled               BOOLEAN NOT NULL DEFAULT TRUE,
    create_date           TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by             VARCHAR(100) DEFAULT 'system',
    update_date           TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_by             VARCHAR(100) DEFAULT 'system',
    
    -- 外键约束
    CONSTRAINT fk_city FOREIGN KEY (city_id) 
        REFERENCES t_city(id) ON DELETE RESTRICT,
    CONSTRAINT fk_maternity_leave_type FOREIGN KEY (maternity_leave_type_id) 
        REFERENCES t_maternity_leave_type(id) ON DELETE RESTRICT
);

-- 索引
CREATE UNIQUE INDEX idx_t_maternity_rules_unique ON t_maternity_rules (city_id, maternity_leave_type_id);
CREATE INDEX idx_t_maternity_rules_city ON t_maternity_rules (city_id);
CREATE INDEX idx_t_maternity_rules_maternity_type ON t_maternity_rules (maternity_leave_type_id);
CREATE INDEX idx_t_maternity_rules_enabled ON t_maternity_rules (enabled);

-- 注释
COMMENT ON TABLE t_maternity_rules IS '产假规则表';
COMMENT ON COLUMN t_maternity_rules.id IS '主键ID';
COMMENT ON COLUMN t_maternity_rules.city_id IS '城市ID（外键关联 t_city）';
COMMENT ON COLUMN t_maternity_rules.maternity_leave_type_id IS '产假类型ID（外键关联 t_maternity_leave_type）';
COMMENT ON COLUMN t_maternity_rules.default_days IS '默认假期天数';
COMMENT ON COLUMN t_maternity_rules.doctor_recommend_days IS '医嘱天数';
COMMENT ON COLUMN t_maternity_rules.maternity_leave_ext IS '产假扩展信息（JSON格式，存储产假对应天数等）';
COMMENT ON COLUMN t_maternity_rules.is_extendable IS '是否节假日顺延';
COMMENT ON COLUMN t_maternity_rules.holiday_extend IS '产假是否顺延';
COMMENT ON COLUMN t_maternity_rules.has_allowance IS '是否有津贴';
COMMENT ON COLUMN t_maternity_rules.enabled IS '是否启用';
COMMENT ON COLUMN t_maternity_rules.create_date IS '创建时间';
COMMENT ON COLUMN t_maternity_rules.create_by IS '创建人';
COMMENT ON COLUMN t_maternity_rules.update_date IS '更新时间';
COMMENT ON COLUMN t_maternity_rules.update_by IS '更新人';
