-- 创建产假申请表
CREATE TABLE maternity_leave_request (
    id BIGSERIAL PRIMARY KEY,
    
    -- 员工信息
    lan_id VARCHAR(50) NOT NULL,
    employee_name VARCHAR(100) NOT NULL,
    
    -- 城市信息
    city_code VARCHAR(50) NOT NULL,
    
    -- 预产期
    expected_delivery_date DATE NOT NULL,
    
    -- 多胞胎信息
    is_multiple_birth BOOLEAN NOT NULL DEFAULT false,
    number_of_babies INTEGER NOT NULL DEFAULT 1,
    
    -- 假期类型
    has_extended_days BOOLEAN NOT NULL DEFAULT false,
    is_difficult_birth BOOLEAN NOT NULL DEFAULT false,
    additional_dystocia_days INTEGER DEFAULT 0,
    is_breast_feeding BOOLEAN DEFAULT false,
    is_miscarriage BOOLEAN NOT NULL DEFAULT false,
    is_first_time_birth BOOLEAN,
    
    -- 流产假细节 (使用JSONB存储复杂对象)
    miscarriage_leave_detail JSONB,
    
    -- 审计字段
    create_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by VARCHAR(100) DEFAULT 'system',
    update_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(100) DEFAULT 'system'
);

-- 创建索引
CREATE INDEX idx_maternity_leave_request_lan_id ON maternity_leave_request(lan_id);
CREATE INDEX idx_maternity_leave_request_city_code ON maternity_leave_request(city_code);
CREATE INDEX idx_maternity_leave_request_expected_delivery_date ON maternity_leave_request(expected_delivery_date);
CREATE INDEX idx_maternity_leave_request_create_date ON maternity_leave_request(create_date);

-- 添加表注释
COMMENT ON TABLE maternity_leave_request IS '产假申请记录表';
COMMENT ON COLUMN maternity_leave_request.id IS '主键ID';
COMMENT ON COLUMN maternity_leave_request.lan_id IS '员工工号';
COMMENT ON COLUMN maternity_leave_request.employee_name IS '员工姓名';
COMMENT ON COLUMN maternity_leave_request.city_code IS '城市代码';
COMMENT ON COLUMN maternity_leave_request.expected_delivery_date IS '预产期';
COMMENT ON COLUMN maternity_leave_request.is_multiple_birth IS '是否多胞胎';
COMMENT ON COLUMN maternity_leave_request.number_of_babies IS '婴儿数量';
COMMENT ON COLUMN maternity_leave_request.has_extended_days IS '是否有晚育假/生育假/奖励假';
COMMENT ON COLUMN maternity_leave_request.is_difficult_birth IS '是否难产';
COMMENT ON COLUMN maternity_leave_request.additional_dystocia_days IS '难产额外假期天数';
COMMENT ON COLUMN maternity_leave_request.is_breast_feeding IS '是否母乳喂养(成都地区)';
COMMENT ON COLUMN maternity_leave_request.is_miscarriage IS '是否流产';
COMMENT ON COLUMN maternity_leave_request.is_first_time_birth IS '是否生育一孩(绍兴地区)';
COMMENT ON COLUMN maternity_leave_request.miscarriage_leave_detail IS '流产假细节(JSON格式: cityCode, index, days, needOverrideDays, description)';
COMMENT ON COLUMN maternity_leave_request.create_date IS '创建时间';
COMMENT ON COLUMN maternity_leave_request.create_by IS '创建人';
COMMENT ON COLUMN maternity_leave_request.update_date IS '更新时间';
COMMENT ON COLUMN maternity_leave_request.update_by IS '更新人';
