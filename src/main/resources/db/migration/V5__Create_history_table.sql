DROP TABLE IF EXISTS history;
DROP TABLE IF EXISTS maternity_leave_request;
DROP TABLE IF EXISTS maternity_leave_result;
DROP TABLE IF EXISTS maternity_allowance_request;
DROP TABLE IF EXISTS maternity_allowance_result;


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

-- 创建产假计算结果表
CREATE TABLE maternity_leave_result (
                                        id BIGSERIAL PRIMARY KEY,

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
                                        update_by VARCHAR(100) DEFAULT 'system'


);

-- 创建索引
CREATE INDEX idx_maternity_leave_result_lan_id ON maternity_leave_result(lan_id);
CREATE INDEX idx_maternity_leave_result_city_code ON maternity_leave_result(city_code);
CREATE INDEX idx_maternity_leave_result_start_date ON maternity_leave_result(start_date);
CREATE INDEX idx_maternity_leave_result_end_date ON maternity_leave_result(end_date);
CREATE INDEX idx_maternity_leave_result_create_date ON maternity_leave_result(create_date);

-- 添加表注释
COMMENT ON TABLE maternity_leave_result IS '产假计算结果表';
COMMENT ON COLUMN maternity_leave_result.id IS '主键ID';
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

-- 创建津贴申请表
CREATE TABLE maternity_allowance_request (
                                             id BIGSERIAL PRIMARY KEY,


    -- 员工信息
                                             lan_id VARCHAR(50) NOT NULL,
                                             employee_name VARCHAR(100) NOT NULL,

    -- 城市信息
                                             city_code VARCHAR(50) NOT NULL,

    -- 工资信息
                                             unit_monthly_average_salary TEXT,
                                             monthly_base_salary TEXT,
                                             adjusted_monthly_base_salary TEXT,
                                             average_salary_past_12_months TEXT NOT NULL,

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
                                             update_by VARCHAR(100) DEFAULT 'system'
);

-- 创建索引
CREATE INDEX idx_maternity_allowance_request_lan_id ON maternity_allowance_request(lan_id);
CREATE INDEX idx_maternity_allowance_request_city_code ON maternity_allowance_request(city_code);
CREATE INDEX idx_maternity_allowance_request_start_date ON maternity_allowance_request(maternity_leave_start_date);
CREATE INDEX idx_maternity_allowance_request_create_date ON maternity_allowance_request(create_date);

-- 添加表注释
COMMENT ON TABLE maternity_allowance_request IS '津贴申请记录表';
COMMENT ON COLUMN maternity_allowance_request.id IS '主键ID';
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

        -- 创建津贴计算结果表
CREATE TABLE maternity_allowance_result (
                                            id BIGSERIAL PRIMARY KEY,


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
                                            update_by VARCHAR(100) DEFAULT 'system'
);

-- 创建索引
CREATE INDEX idx_maternity_allowance_result_lan_id ON maternity_allowance_result(lan_id);
CREATE INDEX idx_maternity_allowance_result_city_code ON maternity_allowance_result(city_code);
CREATE INDEX idx_maternity_allowance_result_create_date ON maternity_allowance_result(create_date);

-- 添加表注释
COMMENT ON TABLE maternity_allowance_result IS '津贴计算结果表';
COMMENT ON COLUMN maternity_allowance_result.id IS '主键ID';
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
