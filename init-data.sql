-- 使用数据库
USE SalarySystem;

-- 初始化角色表（使用显式 ID，防止自增偏移导致角色 ID 不一致）
INSERT INTO sys_role (role_id, role_name) VALUES (1, '系统管理员')
  ON DUPLICATE KEY UPDATE role_name = VALUES(role_name);
INSERT INTO sys_role (role_id, role_name) VALUES (2, '人事管理员')
  ON DUPLICATE KEY UPDATE role_name = VALUES(role_name);
INSERT INTO sys_role (role_id, role_name) VALUES (3, '财务管理员')
  ON DUPLICATE KEY UPDATE role_name = VALUES(role_name);
INSERT INTO sys_role (role_id, role_name) VALUES (4, '总经理')
  ON DUPLICATE KEY UPDATE role_name = VALUES(role_name);
INSERT INTO sys_role (role_id, role_name) VALUES (5, '审计管理员')
  ON DUPLICATE KEY UPDATE role_name = VALUES(role_name);


SET @pwd = 'c4039a5810e67a8b1eb72cc2b9d9f2e55ca0e2fd2b84f55e212ad3a23aec89bc';
INSERT INTO sys_user (emp_id, username, password, role_id, pwd_update_time, fail_count)
SELECT NULL, 'admin', @pwd, role_id, NOW(), 0 FROM sys_role WHERE role_name = '系统管理员';
INSERT INTO sys_user (emp_id, username, password, role_id, pwd_update_time, fail_count)
SELECT NULL, 'hr', @pwd, role_id, NOW(), 0 FROM sys_role WHERE role_name = '人事管理员';
INSERT INTO sys_user (emp_id, username, password, role_id, pwd_update_time, fail_count)
SELECT NULL, 'finance', @pwd, role_id, NOW(), 0 FROM sys_role WHERE role_name = '财务管理员';
INSERT INTO sys_user (emp_id, username, password, role_id, pwd_update_time, fail_count)
SELECT NULL, 'gm', @pwd, role_id, NOW(), 0 FROM sys_role WHERE role_name = '总经理';
INSERT INTO sys_user (emp_id, username, password, role_id, pwd_update_time, fail_count)
SELECT NULL, 'audit', @pwd, role_id, NOW(), 0 FROM sys_role WHERE role_name = '审计管理员';

INSERT IGNORE INTO sys_dept (dept_name, remark) VALUES
('技术部', '负责产品研发和技术支持'),
('财务部', '负责公司财务管理和薪资核算'),
('人事部', '负责员工招聘、培训和人事管理'),
('市场部', '负责市场推广和客户关系'),
('行政部', '负责日常行政事务');
