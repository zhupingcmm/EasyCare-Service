-- 重构节假日表：删除旧表并创建简化的新表

-- 1. 删除旧的 holiday 表
DROP TABLE IF EXISTS holiday CASCADE;

-- 2. 创建新的简化版 holiday 表
CREATE TABLE holiday (
    id  SERIAL PRIMARY KEY,
    date DATE NOT NULL,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL CHECK (type IN ('public_holiday', 'transfer_workday')),
    is_statutory BOOLEAN NOT NULL DEFAULT true,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    
    -- 创建唯一约束，确保同一日期的记录唯一
    CONSTRAINT uk_holiday_date UNIQUE (date)
);

-- 3. 创建索引以提高查询性能
CREATE INDEX idx_holiday_date ON holiday (date);
CREATE INDEX idx_holiday_type ON holiday (type);
CREATE INDEX idx_holiday_is_statutory ON holiday (is_statutory);
CREATE INDEX idx_holiday_is_active ON holiday (is_active);

-- 4. 添加表注释
COMMENT ON TABLE holiday IS '节假日表，存储公共假日和调休工作日信息';
COMMENT ON COLUMN holiday.id IS '主键ID';
COMMENT ON COLUMN holiday.date IS '日期';
COMMENT ON COLUMN holiday.name IS '节假日名称';
COMMENT ON COLUMN holiday.type IS '类型：public_holiday-公共假日，transfer_workday-调休工作日';
COMMENT ON COLUMN holiday.is_statutory IS '是否为法定假日';
COMMENT ON COLUMN holiday.is_active IS '是否激活（逻辑删除标记）';
COMMENT ON COLUMN holiday.created_at IS '创建时间';
COMMENT ON COLUMN holiday.created_by IS '创建人';
COMMENT ON COLUMN holiday.updated_at IS '更新时间';
COMMENT ON COLUMN holiday.updated_by IS '更新人';
