-- 部门管理表
-- 执行方式：mysql -u root -p web2026 < alter-dept-table.sql

USE web2026;

CREATE TABLE IF NOT EXISTS sys_dept (
    dept_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '部门ID',
    dept_name VARCHAR(50) NOT NULL UNIQUE COMMENT '部门名称',
    remark VARCHAR(200) DEFAULT '' COMMENT '备注'
) ENGINE=InnoDB COMMENT='部门表';

-- 初始化一些示例部门（可选）
INSERT IGNORE INTO sys_dept (dept_name, remark) VALUES
('技术部', '负责产品研发和技术支持'),
('财务部', '负责公司财务管理和薪资核算'),
('人事部', '负责员工招聘、培训和人事管理'),
('市场部', '负责市场推广和客户关系'),
('行政部', '负责日常行政事务');

SELECT '✓ 部门表创建完成' AS 'Status';
