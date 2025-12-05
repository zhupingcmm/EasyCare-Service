-- 产假计算系统数据库表创建脚本
-- 1. 删除旧的 holiday 表
DROP TABLE IF EXISTS t_city CASCADE;
-- 城市表
CREATE TABLE IF NOT EXISTS t_city (
    id SERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    cn_name VARCHAR(100),
    en_name VARCHAR(100),
    province VARCHAR(100),
    enabled BOOLEAN NOT NULL DEFAULT true,
    sort_order INTEGER,
    remark VARCHAR(500),
    create_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by VARCHAR(100) DEFAULT 'system',
    update_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(100) DEFAULT 'system'
);

-- 创建索引
CREATE INDEX idx_city_code ON t_city(code);
CREATE INDEX idx_city_enabled ON t_city(enabled);
