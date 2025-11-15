-- 创建津贴申请表
CREATE TABLE maternity_allowance_request (
    id BIGSERIAL PRIMARY KEY,
    
    -- 关联产假申请记录ID (外键)
    maternity_leave_request_id BIGINT,
    
    -- 员工信息
    lan_id VARCHAR(50) NOT NULL,
    employee_name VARCHAR(100) NOT NULL,
    
    -- 城市信息
    city_code VARCHAR(50) NOT NULL,
    
    -- 工资信息
    unit_monthly_average_salary DECIMAL(15, 2),
    monthly_base_salary DECIMAL(15, 2),
    adjusted_monthly_base_salary DECIMAL(15, 2),
    average_salary_past_12_months DECIMAL(15, 2) NOT NULL,
    
    -- 产假信息
    maternity_leave_days INTEGER NOT NULL,
    maternity_leave_start_date DATE NOT NULL,
    maternity_leave_end_date DATE NOT NULL,
    maternity_leave_request_date DATE,
    
    -- 公司垫付信息 (使用JSONB存储CompanyAdvanceMap)
    company_advance JSONB,
    
    -- 政府发放金额
    government_allowance DECIMAL(15, 2) NOT NULL,
    
    -- 审计字段
    create_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by VARCHAR(100) DEFAULT 'system',
    update_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(100) DEFAULT 'system',
    
    -- 外键约束
    CONSTRAINT fk_maternity_allowance_request_leave 
        FOREIGN KEY (maternity_leave_request_id) 
        REFERENCES maternity_leave_request(id) 
        ON DELETE SET NULL
);

-- 创建索引
CREATE INDEX idx_maternity_allowance_request_leave_id ON maternity_allowance_request(maternity_leave_request_id);
CREATE INDEX idx_maternity_allowance_request_lan_id ON maternity_allowance_request(lan_id);
CREATE INDEX idx_maternity_allowance_request_city_code ON maternity_allowance_request(city_code);
CREATE INDEX idx_maternity_allowance_request_start_date ON maternity_allowance_request(maternity_leave_start_date);
CREATE INDEX idx_maternity_allowance_request_create_date ON maternity_allowance_request(create_date);

-- 添加表注释
COMMENT ON TABLE maternity_allowance_request IS '津贴申请记录表';
COMMENT ON COLUMN maternity_allowance_request.id IS '主键ID';
COMMENT ON COLUMN maternity_allowance_request.maternity_leave_request_id IS '关联的产假申请记录ID';
COMMENT ON COLUMN maternity_allowance_request.lan_id IS '员工工号';
COMMENT ON COLUMN maternity_allowance_request.employee_name IS '员工姓名';
COMMENT ON COLUMN maternity_allowance_request.city_code IS '城市代码';
COMMENT ON COLUMN maternity_allowance_request.unit_monthly_average_salary IS '单位月平均工资';
COMMENT ON COLUMN maternity_allowance_request.monthly_base_salary IS '月基本工资';
COMMENT ON COLUMN maternity_allowance_request.adjusted_monthly_base_salary IS '调整月基本工资';
COMMENT ON COLUMN maternity_allowance_request.average_salary_past_12_months IS '产前12个月的月均工资';
COMMENT ON COLUMN maternity_allowance_request.maternity_leave_days IS '产假天数';
COMMENT ON COLUMN maternity_allowance_request.maternity_leave_start_date IS '产假开始时间';
COMMENT ON COLUMN maternity_allowance_request.maternity_leave_end_date IS '产假结束时间';
COMMENT ON COLUMN maternity_allowance_request.maternity_leave_request_date IS '产假申请日期';
COMMENT ON COLUMN maternity_allowance_request.company_advance IS '公司垫付信息(JSON格式: addItem, deleteItem)';
COMMENT ON COLUMN maternity_allowance_request.government_allowance IS '政府发放金额';
COMMENT ON COLUMN maternity_allowance_request.create_date IS '创建时间';
COMMENT ON COLUMN maternity_allowance_request.create_by IS '创建人';
COMMENT ON COLUMN maternity_allowance_request.update_date IS '更新时间';
COMMENT ON COLUMN maternity_allowance_request.update_by IS '更新人';
