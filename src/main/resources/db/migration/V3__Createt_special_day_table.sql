-- 删除旧表
DROP TABLE IF EXISTS holiday CASCADE;
DROP TABLE IF EXISTS t_special_day CASCADE;

-- 创建特殊日期表
CREATE TABLE IF NOT EXISTS t_special_day (
    id SERIAL PRIMARY KEY,
    year INTEGER NOT NULL,
    region VARCHAR(10) DEFAULT 'CN',
    date DATE NOT NULL,
    name VARCHAR(100) NOT NULL,
    cn_name VARCHAR(100),
    en_name VARCHAR(100),
    type INTEGER NOT NULL CHECK (type IN (1, 2)),
    is_public_holiday BOOLEAN NOT NULL DEFAULT true,
    enabled BOOLEAN NOT NULL DEFAULT true,
    create_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by VARCHAR(100) DEFAULT 'system',
    update_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(100) DEFAULT 'system',
    
    -- 创建唯一约束，确保同一年份、地区、日期的记录唯一
    CONSTRAINT uk_special_day_year_region_date UNIQUE (year, region, date)
);

-- 创建索引以提高查询性能
CREATE INDEX idx_special_day_year ON t_special_day(year);
CREATE INDEX idx_special_day_region ON t_special_day(region);
CREATE INDEX idx_special_day_date ON t_special_day(date);
CREATE INDEX idx_special_day_type ON t_special_day(type);
CREATE INDEX idx_special_day_enabled ON t_special_day(enabled);
CREATE INDEX idx_special_day_year_region ON t_special_day(year, region);

-- 添加表注释
COMMENT ON TABLE t_special_day IS '特殊日期表，存储各年份的节假日和补班日信息';
COMMENT ON COLUMN t_special_day.id IS '主键ID';
COMMENT ON COLUMN t_special_day.year IS '年份';
COMMENT ON COLUMN t_special_day.region IS '地区代码，默认CN表示中国';
COMMENT ON COLUMN t_special_day.date IS '日期';
COMMENT ON COLUMN t_special_day.name IS '特殊日期名称';
COMMENT ON COLUMN t_special_day.cn_name IS '中文名称';
COMMENT ON COLUMN t_special_day.en_name IS '英文名称';
COMMENT ON COLUMN t_special_day.type IS '类型：1-节假日，2-补班';
COMMENT ON COLUMN t_special_day.is_public_holiday IS '是否国定假日';
COMMENT ON COLUMN t_special_day.enabled IS '是否启用';
COMMENT ON COLUMN t_special_day.create_date IS '创建时间';
COMMENT ON COLUMN t_special_day.create_by IS '创建人';
COMMENT ON COLUMN t_special_day.update_date IS '更新时间';
COMMENT ON COLUMN t_special_day.update_by IS '更新人';