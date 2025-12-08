-- 创建用户角色关联表

-- 1. 删除旧的 user_roles 表（如果存在）
DROP TABLE IF EXISTS user_roles CASCADE;

-- 2. 创建 user_roles 表
CREATE TABLE user_roles (
    user_id TEXT NOT NULL,
    role_id INTEGER NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    CONSTRAINT pk_user_roles PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_users FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_roles FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE
);

-- 3. 创建索引以提高查询性能
CREATE INDEX idx_user_roles_user_id ON user_roles USING btree (user_id);
CREATE INDEX idx_user_roles_role_id ON user_roles USING btree (role_id);

-- 4. 添加表注释
COMMENT ON TABLE user_roles IS '用户角色关联表，存储用户与角色的多对多关系';
COMMENT ON COLUMN user_roles.user_id IS '用户ID，外键关联users表';
COMMENT ON COLUMN user_roles.role_id IS '角色ID，外键关联roles表';
COMMENT ON COLUMN user_roles.created_at IS '创建时间';
COMMENT ON COLUMN user_roles.created_by IS '创建人';
COMMENT ON COLUMN user_roles.updated_at IS '更新时间';
COMMENT ON COLUMN user_roles.updated_by IS '更新人';
