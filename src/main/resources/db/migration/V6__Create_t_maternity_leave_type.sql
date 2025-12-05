-- 删除旧表
DROP TABLE IF EXISTS t_maternity_leave_type CASCADE;

-- 创建产假类型表
CREATE TABLE IF NOT EXISTS t_maternity_leave_type (
    id SERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
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
COMMENT ON COLUMN t_maternity_leave_type.remark IS '备注';
COMMENT ON COLUMN t_maternity_leave_type.enabled IS '是否启用';
COMMENT ON COLUMN t_maternity_leave_type.create_date IS '创建时间';
COMMENT ON COLUMN t_maternity_leave_type.create_by IS '创建人';
COMMENT ON COLUMN t_maternity_leave_type.update_date IS '更新时间';
COMMENT ON COLUMN t_maternity_leave_type.update_by IS '更新人';

-- 插入产假类型基础数据（非流产假）
INSERT INTO t_maternity_leave_type (code, name, remark, enabled) VALUES
('1001', '法定产假','国家规定的基础产假', true),
('1002', '难产假','难产情形可额外增加天数', true),
('1003', '多胞胎产假', '双胞胎或多胞胎额外假期', true),
('1004', '奖励假', '', true),
('1005', '流产假', '', true);


