-- 使用数据库
USE web2026;

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

-- 初始化系统用户（密码为 Admin@123 的 SM3 哈希）
-- SM3-Hash of "Admin@123" = c4039a5810e67a8b1eb72cc2b9d9f2e55ca0e2fd2b84f55e212ad3a23aec89bc
-- 使用子查询获取正确的 role_id，不硬编码数字
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

-- 初始化员工表（示例）
-- 注意：占位符 [encrypted_name] 等不是真正的 SM4 密文，会导致解密失败
-- TODO: 请在应用中通过员工新增表单添加真实员工数据，或使用以下真实 SM4 密文
-- 如需直接注入，请先运行 GenerateTestData.java 生成加密数据
