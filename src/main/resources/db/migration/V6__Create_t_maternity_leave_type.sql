-- 删除旧表
DROP TABLE IF EXISTS t_maternity_leave_type CASCADE;

-- 创建产假类型表
CREATE TABLE IF NOT EXISTS t_maternity_leave_type (
    id SERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    is_abortion BOOLEAN NOT NULL DEFAULT false,
    remark VARCHAR(500),
    enabled BOOLEAN NOT NULL DEFAULT true,
    create_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by VARCHAR(100) DEFAULT 'system',
    update_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(100) DEFAULT 'system'
);

-- 创建索引
CREATE INDEX idx_maternity_leave_type_code ON t_maternity_leave_type(code);
CREATE INDEX idx_maternity_leave_type_enabled ON t_maternity_leave_type(enabled);

-- 添加表注释
COMMENT ON TABLE t_maternity_leave_type IS '产假类型表';
COMMENT ON COLUMN t_maternity_leave_type.id IS '主键ID';
COMMENT ON COLUMN t_maternity_leave_type.code IS '类型代码';
COMMENT ON COLUMN t_maternity_leave_type.name IS '类型名称';
COMMENT ON COLUMN t_maternity_leave_type.is_abortion IS '是否是流产假';
COMMENT ON COLUMN t_maternity_leave_type.remark IS '备注';
COMMENT ON COLUMN t_maternity_leave_type.enabled IS '是否启用';
COMMENT ON COLUMN t_maternity_leave_type.create_date IS '创建时间';
COMMENT ON COLUMN t_maternity_leave_type.create_by IS '创建人';
COMMENT ON COLUMN t_maternity_leave_type.update_date IS '更新时间';
COMMENT ON COLUMN t_maternity_leave_type.update_by IS '更新人';

-- 插入产假类型基础数据（非流产假）
INSERT INTO t_maternity_leave_type (code, name, is_abortion, enabled) VALUES
('1001', '法定产假', false, true),
('1002', '难产假', false, true),
('1003', '多胞胎产假', false, true),
('1004', '难产假（剖腹产、会阴Ⅲ度破裂）', false, true),
('1005', '晚育假/生育假/奖励假', false, true),
('1006', '奖励假', false, true);

-- 插入流产假类型数据
INSERT INTO t_maternity_leave_type (code, name, is_abortion, enabled) VALUES
('1007', '怀孕不满2个月流产', true, true),
('1008', '怀孕满2个月不满3个月流产', true, true),
('1009', '怀孕满3个月不满7个月流产', true, true),
('1010', '怀孕满7个月流产', true, true),
('1011', '宫外孕', true, true),
('1012', '妊娠不满12周流产', true, true),
('1013', '妊娠满12周不满28周流产', true, true),
('1014', '妊娠满28周流产', true, true),
('1015', '妊娠满4个月流产', true, true),
('1016', '妊娠满7个月流产', true, true),
('1017', '妊娠未满4个月流产', true, true);
