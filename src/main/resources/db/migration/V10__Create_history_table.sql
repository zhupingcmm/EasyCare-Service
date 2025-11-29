-- 创建历史记录表
CREATE TABLE history (
    id BIGSERIAL PRIMARY KEY,
    
    -- 员工工号
    lan_id VARCHAR(50) NOT NULL,
    
    -- 关联的产假申请记录ID
    maternity_leave_request_id BIGINT,
    
    -- 关联的产假结果记录ID
    maternity_leave_result_id BIGINT,
    
    -- 关联的津贴申请记录ID
    maternity_allowance_request_id BIGINT,
    
    -- 关联的津贴结果记录ID
    maternity_allowance_result_id BIGINT,
    
    -- 记录类型：maternity(产假) 或 allowance(津贴)
    record_type VARCHAR(20) NOT NULL DEFAULT 'maternity',
    
    -- 审计字段
    create_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by VARCHAR(100) DEFAULT 'system',
    update_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(100) DEFAULT 'system',
    
    -- 外键约束
    CONSTRAINT fk_history_leave_request 
        FOREIGN KEY (maternity_leave_request_id) 
        REFERENCES maternity_leave_request(id) 
        ON DELETE CASCADE,
    
    CONSTRAINT fk_history_leave_result 
        FOREIGN KEY (maternity_leave_result_id) 
        REFERENCES maternity_leave_result(id) 
        ON DELETE CASCADE,
    
    CONSTRAINT fk_history_allowance_request 
        FOREIGN KEY (maternity_allowance_request_id) 
        REFERENCES maternity_allowance_request(id) 
        ON DELETE CASCADE,
    
    CONSTRAINT fk_history_allowance_result 
        FOREIGN KEY (maternity_allowance_result_id) 
        REFERENCES maternity_allowance_result(id) 
        ON DELETE CASCADE
);

-- 创建索引
CREATE INDEX idx_history_lan_id ON history(lan_id);
CREATE INDEX idx_history_leave_request_id ON history(maternity_leave_request_id);
CREATE INDEX idx_history_leave_result_id ON history(maternity_leave_result_id);
CREATE INDEX idx_history_allowance_request_id ON history(maternity_allowance_request_id);
CREATE INDEX idx_history_allowance_result_id ON history(maternity_allowance_result_id);
CREATE INDEX idx_history_record_type ON history(record_type);
CREATE INDEX idx_history_create_date ON history(create_date);

-- 添加表注释
COMMENT ON TABLE history IS '历史记录表，关联产假和津贴相关记录';
COMMENT ON COLUMN history.id IS '主键ID';
COMMENT ON COLUMN history.lan_id IS '员工工号';
COMMENT ON COLUMN history.maternity_leave_request_id IS '关联的产假申请记录ID';
COMMENT ON COLUMN history.maternity_leave_result_id IS '关联的产假结果记录ID';
COMMENT ON COLUMN history.maternity_allowance_request_id IS '关联的津贴申请记录ID';
COMMENT ON COLUMN history.maternity_allowance_result_id IS '关联的津贴结果记录ID';
COMMENT ON COLUMN history.record_type IS '记录类型：maternity(产假) 或 allowance(津贴)';
COMMENT ON COLUMN history.create_date IS '创建时间';
COMMENT ON COLUMN history.create_by IS '创建人';
COMMENT ON COLUMN history.update_date IS '更新时间';
COMMENT ON COLUMN history.update_by IS '更新人';
