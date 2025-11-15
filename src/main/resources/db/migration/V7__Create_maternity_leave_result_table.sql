-- 创建产假计算结果表
CREATE TABLE maternity_leave_result (
    id BIGSERIAL PRIMARY KEY,
    
    -- 关联的申请记录ID (外键)
    request_id BIGINT NOT NULL,
    
    -- 员工信息 (冗余字段，便于查询)
    lan_id VARCHAR(50) NOT NULL,
    employee_name VARCHAR(100) NOT NULL,
    
    -- 城市信息
    city_code VARCHAR(50) NOT NULL,
    city_name VARCHAR(100),
    
    -- 总天数
    total_days INTEGER NOT NULL,
    total_allowance_days INTEGER NOT NULL,
    
    -- 各类假期天数
    base_days INTEGER NOT NULL DEFAULT 0,
    dystocia_days INTEGER DEFAULT 0,
    multi_baby_days INTEGER DEFAULT 0,
    extended_days INTEGER DEFAULT 0,
    miscarriage_leave_days INTEGER DEFAULT 0,
    pub_holidays_count INTEGER DEFAULT 0,
    
    -- 日期信息
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    return_to_work_date DATE,
    
    -- 时间段详情 (使用JSONB存储List<TimeScope>)
    time_scope_list JSONB,
    
    -- 审计字段
    create_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by VARCHAR(100) DEFAULT 'system',
    update_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(100) DEFAULT 'system',
    
    -- 外键约束
    CONSTRAINT fk_maternity_leave_result_request 
        FOREIGN KEY (request_id) 
        REFERENCES maternity_leave_request(id) 
        ON DELETE CASCADE
);

-- 创建索引
CREATE INDEX idx_maternity_leave_result_request_id ON maternity_leave_result(request_id);
CREATE INDEX idx_maternity_leave_result_lan_id ON maternity_leave_result(lan_id);
CREATE INDEX idx_maternity_leave_result_city_code ON maternity_leave_result(city_code);
CREATE INDEX idx_maternity_leave_result_start_date ON maternity_leave_result(start_date);
CREATE INDEX idx_maternity_leave_result_end_date ON maternity_leave_result(end_date);
CREATE INDEX idx_maternity_leave_result_create_date ON maternity_leave_result(create_date);

-- 添加表注释
COMMENT ON TABLE maternity_leave_result IS '产假计算结果表';
COMMENT ON COLUMN maternity_leave_result.id IS '主键ID';
COMMENT ON COLUMN maternity_leave_result.request_id IS '关联的申请记录ID';
COMMENT ON COLUMN maternity_leave_result.lan_id IS '员工工号';
COMMENT ON COLUMN maternity_leave_result.employee_name IS '员工姓名';
COMMENT ON COLUMN maternity_leave_result.city_code IS '城市代码';
COMMENT ON COLUMN maternity_leave_result.city_name IS '城市名称';
COMMENT ON COLUMN maternity_leave_result.total_days IS '产假总天数';
COMMENT ON COLUMN maternity_leave_result.total_allowance_days IS '津贴总天数';
COMMENT ON COLUMN maternity_leave_result.base_days IS '基础产假天数';
COMMENT ON COLUMN maternity_leave_result.dystocia_days IS '难产假天数';
COMMENT ON COLUMN maternity_leave_result.multi_baby_days IS '多胞胎假天数';
COMMENT ON COLUMN maternity_leave_result.extended_days IS '晚育假/生育假/奖励假天数';
COMMENT ON COLUMN maternity_leave_result.miscarriage_leave_days IS '流产假天数';
COMMENT ON COLUMN maternity_leave_result.pub_holidays_count IS '公共节假日顺延天数';
COMMENT ON COLUMN maternity_leave_result.start_date IS '产假开始日期';
COMMENT ON COLUMN maternity_leave_result.end_date IS '产假结束日期';
COMMENT ON COLUMN maternity_leave_result.return_to_work_date IS '返岗日期';
COMMENT ON COLUMN maternity_leave_result.time_scope_list IS '时间段详情列表(JSON格式: index, name, additionalInfo, days, startAt, endAt, details)';
COMMENT ON COLUMN maternity_leave_result.create_date IS '创建时间';
COMMENT ON COLUMN maternity_leave_result.create_by IS '创建人';
COMMENT ON COLUMN maternity_leave_result.update_date IS '更新时间';
COMMENT ON COLUMN maternity_leave_result.update_by IS '更新人';
