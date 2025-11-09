-- 产假计算系统数据库表创建脚本

-- 城市表
CREATE TABLE IF NOT EXISTS city (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    chinese_name VARCHAR(100),
    english_name VARCHAR(100),
    province VARCHAR(100),
    country_code VARCHAR(10),
    enabled BOOLEAN NOT NULL DEFAULT true,
    sort_order INTEGER,
    remark VARCHAR(500),
    create_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by VARCHAR(100) DEFAULT 'system',
    update_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(100) DEFAULT 'system'
);

-- 创建索引
CREATE INDEX idx_city_code ON city(code);
CREATE INDEX idx_city_name ON city(name);
CREATE INDEX idx_city_enabled ON city(enabled);
CREATE INDEX idx_city_country_code ON city(country_code);
