DROP TABLE IF EXISTS t_maternity_rules CASCADE;

-- 产假规则表（使用外键关联）
CREATE TABLE IF NOT EXISTS t_maternity_rules (
    id                    SERIAL PRIMARY KEY,
    city_id               INTEGER,
    maternity_leave_type_id  INTEGER,
    default_days          INTEGER NOT NULL CHECK (default_days > 0),
    doctor_recommend_days INTEGER,
    maternity_leave_ext   JSONB,
    holiday_extend        BOOLEAN  DEFAULT FALSE,
    has_allowance         BOOLEAN  DEFAULT TRUE,
    plan_allowance_day    INTEGER,
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
-- CREATE UNIQUE INDEX idx_t_maternity_rules_unique ON t_maternity_rules (city_id, maternity_leave_type_id);
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
COMMENT ON COLUMN t_maternity_rules.holiday_extend IS '产假是否顺延';
COMMENT ON COLUMN t_maternity_rules.has_allowance IS '是否有津贴';
COMMENT ON COLUMN t_maternity_rules.plan_allowance_day IS '津贴计发天数';
COMMENT ON COLUMN t_maternity_rules.enabled IS '是否启用';
COMMENT ON COLUMN t_maternity_rules.create_date IS '创建时间';
COMMENT ON COLUMN t_maternity_rules.create_by IS '创建人';
COMMENT ON COLUMN t_maternity_rules.update_date IS '更新时间';
COMMENT ON COLUMN t_maternity_rules.update_by IS '更新人';

-- 插入产假规则数据
INSERT INTO t_maternity_rules (city_id, maternity_leave_type_id, default_days, maternity_leave_ext, holiday_extend, has_allowance) VALUES
-- 上海
((SELECT id FROM t_city WHERE code = 'SH'), (SELECT id FROM t_maternity_leave_type WHERE code = '1001'), 98, NULL, false, true),
((SELECT id FROM t_city WHERE code = 'SH'), (SELECT id FROM t_maternity_leave_type WHERE code = '1002'), 15, NULL, false, true),
((SELECT id FROM t_city WHERE code = 'SH'), (SELECT id FROM t_maternity_leave_type WHERE code = '1003'), 15, NULL, false, true),
((SELECT id FROM t_city WHERE code = 'SH'), (SELECT id FROM t_maternity_leave_type WHERE code = '1004'), 60, NULL, true, true),
((SELECT id FROM t_city WHERE code = 'SH'), (SELECT id FROM t_maternity_leave_type WHERE code = '1005'), 15, '[{"code":"mc_008","days":15},{"code":"mc_009","days":42}]'::jsonb, false, true),

-- 深圳
((SELECT id FROM t_city WHERE code = 'SZ'), (SELECT id FROM t_maternity_leave_type WHERE code = '1001'), 98, NULL, false, true),
((SELECT id FROM t_city WHERE code = 'SZ'), (SELECT id FROM t_maternity_leave_type WHERE code = '1002'), 30, NULL, false, true),
((SELECT id FROM t_city WHERE code = 'SZ'), (SELECT id FROM t_maternity_leave_type WHERE code = '1003'), 15, NULL, false, true),
((SELECT id FROM t_city WHERE code = 'SZ'), (SELECT id FROM t_maternity_leave_type WHERE code = '1004'), 80, NULL, false, false),
((SELECT id FROM t_city WHERE code = 'SZ'), (SELECT id FROM t_maternity_leave_type WHERE code = '1005'), 15, '[{"code":"mc_008","days":15},{"code":"mc_010","days":42},{"code":"mc_011","days":75}]'::jsonb, false, true),

-- 广州
((SELECT id FROM t_city WHERE code = 'GZ'), (SELECT id FROM t_maternity_leave_type WHERE code = '1001'), 98, NULL, false, true),
((SELECT id FROM t_city WHERE code = 'GZ'), (SELECT id FROM t_maternity_leave_type WHERE code = '1002'), 30, '[{"code":"dys_001","days":30},{"code":"dys_002","days":15}]'::jsonb, false, true),
((SELECT id FROM t_city WHERE code = 'GZ'), (SELECT id FROM t_maternity_leave_type WHERE code = '1003'), 15, NULL, false, true),
((SELECT id FROM t_city WHERE code = 'GZ'), (SELECT id FROM t_maternity_leave_type WHERE code = '1004'), 80, NULL, false, true),
((SELECT id FROM t_city WHERE code = 'GZ'), (SELECT id FROM t_maternity_leave_type WHERE code = '1005'), 15, '[{"code":"mc_008","days":15},{"code":"mc_010","days":42},{"code":"mc_011","days":75}]'::jsonb, false, true),

-- 天津
((SELECT id FROM t_city WHERE code = 'TJ'), (SELECT id FROM t_maternity_leave_type WHERE code = '1001'), 98, NULL, false, true),
((SELECT id FROM t_city WHERE code = 'TJ'), (SELECT id FROM t_maternity_leave_type WHERE code = '1002'), 15, NULL, false, true),
((SELECT id FROM t_city WHERE code = 'TJ'), (SELECT id FROM t_maternity_leave_type WHERE code = '1003'), 15, NULL, false, true),
((SELECT id FROM t_city WHERE code = 'TJ'), (SELECT id FROM t_maternity_leave_type WHERE code = '1004'), 60, NULL, false, true),
((SELECT id FROM t_city WHERE code = 'TJ'), (SELECT id FROM t_maternity_leave_type WHERE code = '1005'), 15, '[{"code":"mc_008","days":15},{"code":"mc_009","days":42}]'::jsonb, false, true),

-- 绍兴
((SELECT id FROM t_city WHERE code = 'SX'), (SELECT id FROM t_maternity_leave_type WHERE code = '1001'), 98, NULL, false, true),
((SELECT id FROM t_city WHERE code = 'SX'), (SELECT id FROM t_maternity_leave_type WHERE code = '1002'), 15, NULL, false, true),
((SELECT id FROM t_city WHERE code = 'SX'), (SELECT id FROM t_maternity_leave_type WHERE code = '1003'), 15, NULL, false, true),
((SELECT id FROM t_city WHERE code = 'SX'), (SELECT id FROM t_maternity_leave_type WHERE code = '1004'), 60, '[{"code":"awd_001","days":60},{"code":"awd_002","days":90},{"code":"awd_002","days":90}]'::jsonb, false, true),
((SELECT id FROM t_city WHERE code = 'SX'), (SELECT id FROM t_maternity_leave_type WHERE code = '1005'), 15, '[{"code":"mc_008","days":15},{"code":"mc_009","days":42}]'::jsonb, false, true),

-- 厦门
((SELECT id FROM t_city WHERE code = 'XM'), (SELECT id FROM t_maternity_leave_type WHERE code = '1001'), 98, NULL, false, true),
((SELECT id FROM t_city WHERE code = 'XM'), (SELECT id FROM t_maternity_leave_type WHERE code = '1002'), 15, NULL, false, true),
((SELECT id FROM t_city WHERE code = 'XM'), (SELECT id FROM t_maternity_leave_type WHERE code = '1003'), 15, NULL, false, true),
((SELECT id FROM t_city WHERE code = 'XM'), (SELECT id FROM t_maternity_leave_type WHERE code = '1004'), 60, NULL, false, true),
((SELECT id FROM t_city WHERE code = 'XM'), (SELECT id FROM t_maternity_leave_type WHERE code = '1005'), 15, '[{"code":"mc_005","days":15},{"code":"mc_006","days":42},{"code":"mc_007","days":98}]'::jsonb, false, true),

-- 成都
((SELECT id FROM t_city WHERE code = 'CD'), (SELECT id FROM t_maternity_leave_type WHERE code = '1001'), 98, NULL, false, true),
((SELECT id FROM t_city WHERE code = 'CD'), (SELECT id FROM t_maternity_leave_type WHERE code = '1002'), 15, NULL, false, true),
((SELECT id FROM t_city WHERE code = 'CD'), (SELECT id FROM t_maternity_leave_type WHERE code = '1003'), 15, NULL, false, true),
((SELECT id FROM t_city WHERE code = 'CD'), (SELECT id FROM t_maternity_leave_type WHERE code = '1004'), 60, NULL, false, true),
((SELECT id FROM t_city WHERE code = 'CD'), (SELECT id FROM t_maternity_leave_type WHERE code = '1005'), 15, '[{"code":"mc_008","days":15},{"code":"mc_009","days":42}]'::jsonb, false, true),

-- 苏州
((SELECT id FROM t_city WHERE code = 'SU'), (SELECT id FROM t_maternity_leave_type WHERE code = '1001'), 98, NULL, false, true),
((SELECT id FROM t_city WHERE code = 'SU'), (SELECT id FROM t_maternity_leave_type WHERE code = '1002'), 15, NULL, false, true),
((SELECT id FROM t_city WHERE code = 'SU'), (SELECT id FROM t_maternity_leave_type WHERE code = '1003'), 15, NULL, false, true),
((SELECT id FROM t_city WHERE code = 'SU'), (SELECT id FROM t_maternity_leave_type WHERE code = '1004'), 60, NULL, true, true),
((SELECT id FROM t_city WHERE code = 'SU'), (SELECT id FROM t_maternity_leave_type WHERE code = '1005'), 15, '[{"code":"mc_002","days":20},{"code":"mc_003","days":30},{"code":"mc_004","days":42},{"code":"mc_011","days":98}]'::jsonb, false, true),

-- 青岛
((SELECT id FROM t_city WHERE code = 'QD'), (SELECT id FROM t_maternity_leave_type WHERE code = '1001'), 98, NULL, false, true),
((SELECT id FROM t_city WHERE code = 'QD'), (SELECT id FROM t_maternity_leave_type WHERE code = '1002'), 15, NULL, false, true),
((SELECT id FROM t_city WHERE code = 'QD'), (SELECT id FROM t_maternity_leave_type WHERE code = '1003'), 15, NULL, false, true),
((SELECT id FROM t_city WHERE code = 'QD'), (SELECT id FROM t_maternity_leave_type WHERE code = '1004'), 60, NULL, false, true),
((SELECT id FROM t_city WHERE code = 'QD'), (SELECT id FROM t_maternity_leave_type WHERE code = '1005'), 15, '[{"code":"mc_008","days":15},{"code":"mc_009","days":42}]'::jsonb, false, true),

-- 北京
((SELECT id FROM t_city WHERE code = 'BJ'), (SELECT id FROM t_maternity_leave_type WHERE code = '1001'), 98, NULL, false, true),
((SELECT id FROM t_city WHERE code = 'BJ'), (SELECT id FROM t_maternity_leave_type WHERE code = '1002'), 15, NULL, false, true),
((SELECT id FROM t_city WHERE code = 'BJ'), (SELECT id FROM t_maternity_leave_type WHERE code = '1003'), 15, NULL, false, true),
((SELECT id FROM t_city WHERE code = 'BJ'), (SELECT id FROM t_maternity_leave_type WHERE code = '1004'), 60, NULL, false, true),
((SELECT id FROM t_city WHERE code = 'BJ'), (SELECT id FROM t_maternity_leave_type WHERE code = '1005'), 15, '[{"code":"mc_008","days":15},{"code":"mc_010","days":42},{"code":"mc_011","days":98}]'::jsonb, false, true),

-- 重庆
((SELECT id FROM t_city WHERE code = 'CQ'), (SELECT id FROM t_maternity_leave_type WHERE code = '1001'), 98, NULL, false, true),
((SELECT id FROM t_city WHERE code = 'CQ'), (SELECT id FROM t_maternity_leave_type WHERE code = '1002'), 15, NULL, false, true),
((SELECT id FROM t_city WHERE code = 'CQ'), (SELECT id FROM t_maternity_leave_type WHERE code = '1003'), 15, NULL, false, true),
((SELECT id FROM t_city WHERE code = 'CQ'), (SELECT id FROM t_maternity_leave_type WHERE code = '1004'), 80, NULL, false, true),
((SELECT id FROM t_city WHERE code = 'CQ'), (SELECT id FROM t_maternity_leave_type WHERE code = '1005'), 15, '[{"code":"mc_008","days":15},{"code":"mc_010","days":42},{"code":"mc_001","days":42}]'::jsonb, false, true),

-- 珠海
((SELECT id FROM t_city WHERE code = 'ZH'), (SELECT id FROM t_maternity_leave_type WHERE code = '1001'), 98, NULL, false, true),
((SELECT id FROM t_city WHERE code = 'ZH'), (SELECT id FROM t_maternity_leave_type WHERE code = '1002'), 15, NULL, false, true),
((SELECT id FROM t_city WHERE code = 'ZH'), (SELECT id FROM t_maternity_leave_type WHERE code = '1003'), 15, NULL, false, true),
((SELECT id FROM t_city WHERE code = 'ZH'), (SELECT id FROM t_maternity_leave_type WHERE code = '1004'), 80, NULL, false, true),
((SELECT id FROM t_city WHERE code = 'ZH'), (SELECT id FROM t_maternity_leave_type WHERE code = '1005'), 15, '[{"code":"mc_008","days":15},{"code":"mc_010","days":42},{"code":"mc_011","days":75}]'::jsonb, false, true),

-- 佛山
((SELECT id FROM t_city WHERE code = 'FS'), (SELECT id FROM t_maternity_leave_type WHERE code = '1001'), 98, NULL, false, true),
((SELECT id FROM t_city WHERE code = 'FS'), (SELECT id FROM t_maternity_leave_type WHERE code = '1002'), 15, NULL, false, true),
((SELECT id FROM t_city WHERE code = 'FS'), (SELECT id FROM t_maternity_leave_type WHERE code = '1003'), 15, NULL, false, true),
((SELECT id FROM t_city WHERE code = 'FS'), (SELECT id FROM t_maternity_leave_type WHERE code = '1004'), 80, NULL, false, true),
((SELECT id FROM t_city WHERE code = 'FS'), (SELECT id FROM t_maternity_leave_type WHERE code = '1005'), 15, '[{"code":"mc_008","days":15},{"code":"mc_010","days":42},{"code":"mc_011","days":75}]'::jsonb, false, true),

-- 武汉
((SELECT id FROM t_city WHERE code = 'WH'), (SELECT id FROM t_maternity_leave_type WHERE code = '1001'), 98, NULL, false, true),
((SELECT id FROM t_city WHERE code = 'WH'), (SELECT id FROM t_maternity_leave_type WHERE code = '1002'), 15, NULL, false, true),
((SELECT id FROM t_city WHERE code = 'WH'), (SELECT id FROM t_maternity_leave_type WHERE code = '1003'), 15, NULL, false, true),
((SELECT id FROM t_city WHERE code = 'WH'), (SELECT id FROM t_maternity_leave_type WHERE code = '1004'), 60, NULL, false, true),
((SELECT id FROM t_city WHERE code = 'WH'), (SELECT id FROM t_maternity_leave_type WHERE code = '1005'), 30, '[{"code":"mc_005","days":30},{"code":"mc_006","days":45},{"code":"mc_007","days":98}]'::jsonb, false, true);
