-- 创建角色表

-- 1. 删除旧的 roles 表（如果存在）
DROP TABLE IF EXISTS roles CASCADE;

-- 2. 创建 roles 表
CREATE TABLE roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE CHECK (name IN ('HR_ADMIN', 'HR_USER', 'EMPLOYEE')),
    normalized_name VARCHAR(50) NOT NULL UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255)
);

-- 3. 创建索引以提高查询性能
CREATE INDEX idx_roles_name ON roles USING btree (name);
CREATE INDEX idx_roles_normalized_name ON roles USING btree (normalized_name);

-- 4. 添加表注释
COMMENT ON TABLE roles IS '角色表，存储系统角色信息';
COMMENT ON COLUMN roles.id IS '主键ID';
COMMENT ON COLUMN roles.name IS '角色名称';
COMMENT ON COLUMN roles.normalized_name IS '规范化角色名称';
COMMENT ON COLUMN roles.created_at IS '创建时间';
COMMENT ON COLUMN roles.created_by IS '创建人';
COMMENT ON COLUMN roles.updated_at IS '更新时间';
COMMENT ON COLUMN roles.updated_by IS '更新人';

-- 5. 插入默认角色数据
INSERT INTO roles (name, normalized_name, created_by) VALUES 
('HR_ADMIN', 'hr_admin', 'SYSTEM'),
('HR_USER', 'hr_user', 'SYSTEM'),
('EMPLOYEE', 'employee', 'SYSTEM');
