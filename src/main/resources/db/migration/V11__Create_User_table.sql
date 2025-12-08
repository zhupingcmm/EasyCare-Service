-- 创建用户表

-- 1. 删除旧的 users 表（如果存在）
DROP TABLE IF EXISTS users CASCADE;

-- 2. 创建 users 表
CREATE TABLE users (
    id TEXT NOT NULL PRIMARY KEY,
    lan_id VARCHAR(256) NOT NULL UNIQUE,
    user_name VARCHAR(256) NOT NULL,
    normalized_user_name VARCHAR(256) NOT NULL UNIQUE,
    email VARCHAR(256),
    normalized_email VARCHAR(256),
    display_name VARCHAR(256),
    is_active BOOLEAN DEFAULT true NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255)
);

-- 3. 创建索引以提高查询性能
CREATE INDEX idx_users_lan_id ON users USING btree (lan_id);
CREATE INDEX idx_users_user_name ON users USING btree (user_name);
CREATE INDEX idx_users_normalized_user_name ON users USING btree (normalized_user_name);
CREATE INDEX idx_users_email ON users USING btree (email);

-- 4. 添加表注释
COMMENT ON TABLE users IS '用户表，存储系统用户基本信息';
COMMENT ON COLUMN users.id IS '主键ID，使用UUID';
COMMENT ON COLUMN users.lan_id IS 'LAN账号ID';
COMMENT ON COLUMN users.user_name IS '用户名';
COMMENT ON COLUMN users.normalized_user_name IS '规范化用户名';
COMMENT ON COLUMN users.email IS '邮箱地址';
COMMENT ON COLUMN users.normalized_email IS '规范化邮箱地址';
COMMENT ON COLUMN users.display_name IS '显示名称';
COMMENT ON COLUMN users.is_active IS '是否激活（逻辑删除标记）';
COMMENT ON COLUMN users.created_at IS '创建时间';
COMMENT ON COLUMN users.created_by IS '创建人';
COMMENT ON COLUMN users.updated_at IS '更新时间';
COMMENT ON COLUMN users.updated_by IS '更新人';
