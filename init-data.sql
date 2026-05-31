-- 使用数据库
USE web2026;

-- 初始化角色表
INSERT INTO sys_role (role_name) VALUES ('系统管理员');
INSERT INTO sys_role (role_name) VALUES ('人事管理员');
INSERT INTO sys_role (role_name) VALUES ('财务管理员');
INSERT INTO sys_role (role_name) VALUES ('总经理');
INSERT INTO sys_role (role_name) VALUES ('审计管理员');

-- 初始化系统用户（密码为 Admin@123 的 SM3 哈希）
-- SM3-Hash of "Admin@123" = c4039a5810e67a8b1eb72cc2b9d9f2e55ca0e2fd2b84f55e212ad3a23aec89bc
INSERT INTO sys_user (emp_id, username, password, role_id, pwd_update_time, fail_count)
VALUES (NULL, 'admin', 'c4039a5810e67a8b1eb72cc2b9d9f2e55ca0e2fd2b84f55e212ad3a23aec89bc', 1, NOW(), 0);
INSERT INTO sys_user (emp_id, username, password, role_id, pwd_update_time, fail_count)
VALUES (NULL, 'hr', 'c4039a5810e67a8b1eb72cc2b9d9f2e55ca0e2fd2b84f55e212ad3a23aec89bc', 2, NOW(), 0);
INSERT INTO sys_user (emp_id, username, password, role_id, pwd_update_time, fail_count)
VALUES (NULL, 'finance', 'c4039a5810e67a8b1eb72cc2b9d9f2e55ca0e2fd2b84f55e212ad3a23aec89bc', 3, NOW(), 0);
INSERT INTO sys_user (emp_id, username, password, role_id, pwd_update_time, fail_count)
VALUES (NULL, 'audit', 'c4039a5810e67a8b1eb72cc2b9d9f2e55ca0e2fd2b84f55e212ad3a23aec89bc', 5, NOW(), 0);

-- 初始化员工表（示例）
-- 注意：占位符 [encrypted_name] 等不是真正的 SM4 密文，会导致解密失败
-- TODO: 请在应用中通过员工新增表单添加真实员工数据，或使用以下真实 SM4 密文
-- 如需直接注入，请先运行 GenerateTestData.java 生成加密数据

