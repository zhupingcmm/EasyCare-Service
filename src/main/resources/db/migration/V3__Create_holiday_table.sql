-- 创建节假日表
CREATE TABLE holiday (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    year INTEGER NOT NULL,
    region VARCHAR(10) NOT NULL DEFAULT 'CN',
    date DATE NOT NULL,
    name VARCHAR(100) NOT NULL,
    name_cn VARCHAR(100) NOT NULL,
    name_en VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL CHECK (type IN ('public_holiday', 'transfer_workday')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    -- 创建唯一约束，确保同一年份、地区、日期的记录唯一
    CONSTRAINT uk_holiday_year_region_date UNIQUE (year, region, date)
);

-- 创建索引以提高查询性能
CREATE INDEX idx_holiday_year ON holiday (year);
CREATE INDEX idx_holiday_region ON holiday (region);
CREATE INDEX idx_holiday_date ON holiday (date);
CREATE INDEX idx_holiday_type ON holiday (type);
CREATE INDEX idx_holiday_year_region ON holiday (year, region);

-- 添加表注释
COMMENT ON TABLE holiday IS '节假日表，存储各年份的公共假日和调休工作日信息';
COMMENT ON COLUMN holiday.id IS '主键ID';
COMMENT ON COLUMN holiday.year IS '年份';
COMMENT ON COLUMN holiday.region IS '地区代码，默认CN表示中国';
COMMENT ON COLUMN holiday.date IS '日期';
COMMENT ON COLUMN holiday.name IS '节假日名称';
COMMENT ON COLUMN holiday.name_cn IS '中文名称';
COMMENT ON COLUMN holiday.name_en IS '英文名称';
COMMENT ON COLUMN holiday.type IS '类型：public_holiday-公共假日，transfer_workday-调休工作日';
COMMENT ON COLUMN holiday.created_at IS '创建时间';
COMMENT ON COLUMN holiday.updated_at IS '更新时间';
