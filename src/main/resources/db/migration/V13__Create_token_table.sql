-- 创建JWT令牌表

-- 1. 删除旧的 tokens 表（如果存在）
DROP TABLE IF EXISTS tokens CASCADE;

-- 2. 创建 tokens 表
CREATE TABLE tokens (
    id uuid NOT NULL,
    user_id TEXT NOT NULL,
    op_acc_token VARCHAR(2048) NOT NULL,
    op_ref_token VARCHAR(2048) NOT NULL,
    acc_token TEXT NOT NULL,
    ref_token TEXT NOT NULL,
    exp_time TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked BOOLEAN DEFAULT false NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    CONSTRAINT fk_tokens_users FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
)
PARTITION BY RANGE (exp_time);

-- 3. 创建分区表（示例分区，实际使用时应根据业务需求调整）
CREATE TABLE tokens_2024 PARTITION OF tokens
    FOR VALUES FROM ('2024-01-01 00:00:00+00') TO ('2025-01-01 00:00:00+00');

CREATE TABLE tokens_2025 PARTITION OF tokens
    FOR VALUES FROM ('2025-01-01 00:00:00+00') TO ('2026-01-01 00:00:00+00');

-- 4. 创建索引以提高查询性能
CREATE INDEX idx_tokens_user_id ON tokens USING btree (user_id);
CREATE INDEX idx_tokens_op_acc_token ON tokens USING btree (op_acc_token, exp_time);
CREATE INDEX idx_tokens_op_ref_token ON tokens USING btree (op_ref_token, exp_time);
CREATE INDEX idx_tokens_exp_time ON tokens USING btree (exp_time);

-- 5. 添加表注释
COMMENT ON TABLE tokens IS 'JWT令牌表，存储用户访问令牌和刷新令牌';
COMMENT ON COLUMN tokens.id IS '主键ID';
COMMENT ON COLUMN tokens.user_id IS '用户ID，外键关联users表';
COMMENT ON COLUMN tokens.op_acc_token IS '操作访问令牌';
COMMENT ON COLUMN tokens.op_ref_token IS '操作刷新令牌';
COMMENT ON COLUMN tokens.acc_token IS '访问令牌';
COMMENT ON COLUMN tokens.ref_token IS '刷新令牌';
COMMENT ON COLUMN tokens.exp_time IS '过期时间';
COMMENT ON COLUMN tokens.revoked IS '是否已撤销';
COMMENT ON COLUMN tokens.created_at IS '创建时间';
COMMENT ON COLUMN tokens.created_by IS '创建人';
COMMENT ON COLUMN tokens.updated_at IS '更新时间';
COMMENT ON COLUMN tokens.updated_by IS '更新人';
