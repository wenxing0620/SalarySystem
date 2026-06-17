-- 创建数据库
CREATE DATABASE IF NOT EXISTS SalarySystem CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE SalarySystem;

-- 1. 角色表
CREATE TABLE sys_role (
    role_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '角色ID',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称（如：系统管理员、人事管理员、财务管理员、总经理、审计管理员）'
) ENGINE=InnoDB COMMENT='角色表';

-- 2. 用户登录表
CREATE TABLE sys_user (
    user_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '用户唯一ID',
    emp_id INT UNIQUE COMMENT '关联员工表 emp_info，系统管理员可为空',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '登录账号',
    password VARCHAR(128) NOT NULL COMMENT '密码（存 SM3 哈希值）',
    role_id INT NOT NULL COMMENT '角色ID',
    pwd_update_time DATETIME NOT NULL COMMENT '密码最后修改时间（用于校验 90 天过期）',
    fail_count INT DEFAULT 0 COMMENT '连续登录失败次数',
    lock_time DATETIME COMMENT '账号锁定到期时间戳'
) ENGINE=InnoDB COMMENT='用户登录表';

-- 3. 系统日志表
CREATE TABLE sys_log (
    log_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
    user_id INT NOT NULL COMMENT '操作人ID',
    action_type VARCHAR(50) NOT NULL COMMENT '动作（如：LOGIN, EXPORT_SALARY, ADD_EMP）',
    ip_address VARCHAR(50) NOT NULL COMMENT '操作IP',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    hmac VARCHAR(128) DEFAULT NULL COMMENT 'HMAC-SM3 校验值（hex）'
) ENGINE=InnoDB COMMENT='系统日志表（供审计管理员查看）';

-- 4. 员工主表
CREATE TABLE emp_info (
    emp_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '员工内码',
    emp_no VARCHAR(20) NOT NULL UNIQUE COMMENT '员工编号（明文）',
    dept_name VARCHAR(50) NOT NULL COMMENT '部门名称（明文）',
    position VARCHAR(50) NOT NULL COMMENT '岗位/职务（明文）',
    emp_name VARCHAR(255) NOT NULL COMMENT '员工姓名（存 SM4 密文）',
    id_card VARCHAR(255) NOT NULL UNIQUE COMMENT '身份证号（存 SM4 密文）',
    phone VARCHAR(255) NOT NULL COMMENT '手机号（存 SM4 密文）',
    address VARCHAR(500) NOT NULL COMMENT '住址（存 SM4 密文）',
    data_hash VARCHAR(128) NOT NULL COMMENT '数据完整性校验（SM3 散列值）'
) ENGINE=InnoDB COMMENT='员工主表';

-- 5. 家属信息表
CREATE TABLE emp_family (
    family_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID',
    emp_id INT NOT NULL COMMENT '所属员工ID',
    relation VARCHAR(20) NOT NULL COMMENT '关系（如：父亲、母亲、儿子、女儿）',
    name VARCHAR(255) NOT NULL COMMENT '家属姓名（存 SM4 密文）',
    id_card VARCHAR(255) NOT NULL COMMENT '家属身份证号（存 SM4 密文）'
) ENGINE=InnoDB COMMENT='家属信息表（子女和老人）';

-- 6. 专项附加扣除申报表
CREATE TABLE tax_deduction (
    deduction_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID',
    emp_id INT NOT NULL COMMENT '员工ID',
    declare_year INT NOT NULL COMMENT '申报年份（如：2023）',
    child_edu DECIMAL(10,2) DEFAULT 0.00 COMMENT '子女教育扣除额',
    cont_edu DECIMAL(10,2) DEFAULT 0.00 COMMENT '继续教育扣除额',
    major_med DECIMAL(10,2) DEFAULT 0.00 COMMENT '大病医疗扣除额',
    housing_loan DECIMAL(10,2) DEFAULT 0.00 COMMENT '住房贷款利息扣除额',
    housing_rent DECIMAL(10,2) DEFAULT 0.00 COMMENT '住房租金扣除额',
    support_elderly DECIMAL(10,2) DEFAULT 0.00 COMMENT '赡养老人扣除额',
    baby_care DECIMAL(10,2) DEFAULT 0.00 COMMENT '婴幼儿照护扣除额',
    UNIQUE KEY `uk_emp_year` (`emp_id`, `declare_year`) COMMENT '每个员工每年只有一条申报记录'
) ENGINE=InnoDB COMMENT='专项附加扣除申报表';

-- 7. 月度薪资主表
CREATE TABLE salary_record (
    record_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '薪资流水ID',
    emp_id INT NOT NULL COMMENT '员工ID',
    salary_month VARCHAR(7) NOT NULL COMMENT '计薪月份（格式：YYYY-MM）',
    expected_days INT NOT NULL COMMENT '本月应出勤天数',
    actual_days INT NOT NULL COMMENT '实际出勤天数',
    basic_salary DECIMAL(10,2) NOT NULL COMMENT '基本工资',
    position_allowance DECIMAL(10,2) NOT NULL COMMENT '岗位津贴',
    lunch_allowance DECIMAL(10,2) NOT NULL COMMENT '午餐补贴',
    overtime_salary DECIMAL(10,2) DEFAULT 0.00 COMMENT '加班工资',
    full_attend_salary DECIMAL(10,2) DEFAULT 0.00 COMMENT '全勤工资',
    social_security DECIMAL(10,2) NOT NULL COMMENT '扣社保（五险）',
    provident_fund DECIMAL(10,2) NOT NULL COMMENT '扣公积金（一金）',
    tax DECIMAL(10,2) NOT NULL COMMENT '扣税（系统计算）',
    absence_deduction DECIMAL(10,2) DEFAULT 0.00 COMMENT '迟到、请假等扣除',
    actual_salary DECIMAL(10,2) NOT NULL COMMENT '实发工资',
    UNIQUE KEY `uk_emp_month` (`emp_id`, `salary_month`) COMMENT '每个员工每月只有一条薪资记录'
) ENGINE=InnoDB COMMENT='月度薪资主表';

--8. 部门表
CREATE TABLE IF NOT EXISTS sys_dept (
    dept_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '部门ID',
    dept_name VARCHAR(50) NOT NULL UNIQUE COMMENT '部门名称',
    remark VARCHAR(200) DEFAULT '' COMMENT '备注'
    ) ENGINE=InnoDB COMMENT='部门表';

-- 添加外键约束
ALTER TABLE sys_user ADD CONSTRAINT fk_user_role FOREIGN KEY (role_id) REFERENCES sys_role(role_id);
ALTER TABLE emp_family ADD CONSTRAINT fk_family_emp FOREIGN KEY (emp_id) REFERENCES emp_info(emp_id) ON DELETE CASCADE;
ALTER TABLE tax_deduction ADD CONSTRAINT fk_tax_emp FOREIGN KEY (emp_id) REFERENCES emp_info(emp_id) ON DELETE CASCADE;
ALTER TABLE salary_record ADD CONSTRAINT fk_salary_emp FOREIGN KEY (emp_id) REFERENCES emp_info(emp_id) ON DELETE CASCADE;

-- 创建索引便利查询
CREATE INDEX idx_sys_log_user ON sys_log(user_id);
CREATE INDEX idx_sys_log_time ON sys_log(create_time);
CREATE INDEX idx_emp_no ON emp_info(emp_no);
CREATE INDEX idx_salary_month ON salary_record(salary_month);
CREATE INDEX idx_salary_emp ON salary_record(emp_id);

-- 提示用户
SELECT '✓ 数据库初始化完成' AS 'Status';

