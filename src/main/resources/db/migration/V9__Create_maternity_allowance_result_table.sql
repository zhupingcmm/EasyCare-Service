-- 创建津贴计算结果表
CREATE TABLE maternity_allowance_result (
    id BIGSERIAL PRIMARY KEY,
    
    -- 关联的津贴申请记录ID (外键)
    allowance_request_id BIGINT NOT NULL,
    
    -- 员工信息 (冗余字段)
    lan_id VARCHAR(50) NOT NULL,
    employee_name VARCHAR(100) NOT NULL,
    
    -- 城市信息
    city_code VARCHAR(50) NOT NULL,
    city_name VARCHAR(100),
    
    -- 津贴计算结果
    allowance_days INTEGER,
    extra_allowance DECIMAL(15, 2),
    maternity_allowance DECIMAL(15, 2),
    compensation_amount DECIMAL(15, 2),
    paid_maternity_wage DECIMAL(15, 2),
    employee_refund_amount DECIMAL(15, 2),
    
    -- 计算详情 (使用JSONB存储)
    allowance_compensation_details JSONB,
    refund_details JSONB,
    
    -- 审计字段
    create_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by VARCHAR(100) DEFAULT 'system',
    update_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(100) DEFAULT 'system',
    
    -- 外键约束
    CONSTRAINT fk_maternity_allowance_result_request 
        FOREIGN KEY (allowance_request_id) 
        REFERENCES maternity_allowance_request(id) 
        ON DELETE CASCADE
);

-- 创建索引
CREATE INDEX idx_maternity_allowance_result_request_id ON maternity_allowance_result(allowance_request_id);
CREATE INDEX idx_maternity_allowance_result_lan_id ON maternity_allowance_result(lan_id);
CREATE INDEX idx_maternity_allowance_result_city_code ON maternity_allowance_result(city_code);
CREATE INDEX idx_maternity_allowance_result_create_date ON maternity_allowance_result(create_date);

-- 添加表注释
COMMENT ON TABLE maternity_allowance_result IS '津贴计算结果表';
COMMENT ON COLUMN maternity_allowance_result.id IS '主键ID';
COMMENT ON COLUMN maternity_allowance_result.allowance_request_id IS '关联的津贴申请记录ID';
COMMENT ON COLUMN maternity_allowance_result.lan_id IS '员工工号';
COMMENT ON COLUMN maternity_allowance_result.employee_name IS '员工姓名';
COMMENT ON COLUMN maternity_allowance_result.city_code IS '城市代码';
COMMENT ON COLUMN maternity_allowance_result.city_name IS '城市名称';
COMMENT ON COLUMN maternity_allowance_result.allowance_days IS '享受津贴天数';
COMMENT ON COLUMN maternity_allowance_result.extra_allowance IS '额外补贴';
COMMENT ON COLUMN maternity_allowance_result.maternity_allowance IS '生育津贴金额';
COMMENT ON COLUMN maternity_allowance_result.compensation_amount IS '补差金额';
COMMENT ON COLUMN maternity_allowance_result.paid_maternity_wage IS '产假应付工资';
COMMENT ON COLUMN maternity_allowance_result.employee_refund_amount IS '员工返还金额';
COMMENT ON COLUMN maternity_allowance_result.allowance_compensation_details IS '津贴补差计算详情';
COMMENT ON COLUMN maternity_allowance_result.refund_details IS '返还计算详情';
COMMENT ON COLUMN maternity_allowance_result.create_date IS '创建时间';
COMMENT ON COLUMN maternity_allowance_result.create_by IS '创建人';
COMMENT ON COLUMN maternity_allowance_result.update_date IS '更新时间';
COMMENT ON COLUMN maternity_allowance_result.update_by IS '更新人';
