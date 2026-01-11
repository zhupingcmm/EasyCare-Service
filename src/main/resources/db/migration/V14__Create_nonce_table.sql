-- Create nonce table for preventing replay attacks
DROP TABLE IF EXISTS nonce CASCADE;

CREATE TABLE nonce (
    id VARCHAR(36) PRIMARY KEY,
    nonce_value VARCHAR(256) NOT NULL UNIQUE,
    user_id VARCHAR(36) NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    used_at TIMESTAMP,
    expires_at TIMESTAMP NOT NULL
);

-- Create indexes for performance
DROP INDEX IF EXISTS idx_nonce_value;
DROP INDEX IF EXISTS idx_user_id;
DROP INDEX IF EXISTS idx_created_at;
DROP INDEX IF EXISTS idx_expires_at;
DROP INDEX IF EXISTS idx_used;

CREATE INDEX idx_nonce_value ON nonce(nonce_value);
CREATE INDEX idx_user_id ON nonce(user_id);
CREATE INDEX idx_created_at ON nonce(created_at);
CREATE INDEX idx_expires_at ON nonce(expires_at);
CREATE INDEX idx_used ON nonce(used);

-- Add comments
COMMENT ON TABLE nonce IS '用于防止重放攻击的nonce记录表';
COMMENT ON COLUMN nonce.id IS '主键ID';
COMMENT ON COLUMN nonce.nonce_value IS 'nonce值，客户端生成的随机字符串';
COMMENT ON COLUMN nonce.user_id IS '用户ID';
COMMENT ON COLUMN nonce.used IS '是否已使用';
COMMENT ON COLUMN nonce.created_at IS '创建时间';
COMMENT ON COLUMN nonce.used_at IS '使用时间';
COMMENT ON COLUMN nonce.expires_at IS '过期时间';
