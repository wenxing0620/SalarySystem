公司人员工资管理系统
某公司每个月发放的薪水包括基本工资、岗位津贴、午餐补贴、加班工资、全勤工资等项目组成，扣除社保、公积金、个人所得税、及迟到请假等，才是到手的实发工资，下表示某公司1月份的工资表。

请你为该公司设计开发一套人员工资管理系统，包括部门管理、人员管理、工资管理、员工专项附加扣除管理、月工资导入或录入、历史工资查询等功能，历史工资查询可以按部门、姓名、时间段（如2023年1月-2023年12月）等条件进行查询。其中人员信息包括姓名、员工编号、部门、岗位、职务、身份证号、手机号、住址等信息。个人所得税的计算方法和个人专项附加扣除可以查询百度百科。要求对每个员工每年的个人专项附加扣除（指个人所得税法规定的子女教育、继续教育、大病医疗、住房贷款利息、住房租金和赡养老人、婴幼儿照护等七项专项附加扣除）进行每年申报管理，并根据员工个人每年填报的个人专项附加扣除，直接计算每个月的扣税金额。子女和老人的信息包括姓名、身份证号、关系等。
要求按等保三级和国密算法对敏感信息进行隐私保护，具体要求如下：
（1）身份鉴别
后端管理的密码复杂度要求，长度8位以上，包含数字、大小字母、特殊字符等混合组合；定期要求90天以上需更换一次密码；登录失败5次锁定30分钟，超时30分钟自动退出；要求对密码采用基于国产密码算法SM3进行加密保存。
（2）访问控制
配置人事管理员、财务管理员、总经理、系统管理员和审计员。其中，系统管理员可以设置用户的角色和权限以及人事管理员、财务管理员、总经理和审计管理员，审计管理员只允许查看系统的日志信息。
（3）数据完整性和保密性
对员工或子女和老人信息中涉及密码、身份证号、手机号、住址等重要数据存储时，采用基于国产密码算法SM系列的校验技术或密码技术，保证其在存储过程中数据的完整性和保密性，如密码用国密SM3加密存储，姓名等用SM2或SM4加密存储，并在页面上对关键信息进行脱敏显示（部分内容用*代替）。
（4）安全审计
对所有用户的登录、查看员工或子女和老人等重要的操作日志保存至数据库的日志表，审计管理员可对日志进行查询、查看等审计操作，日志保存半年以上。选做：采用基于国产密码算法对日志记录进行完整性保护，可对日志记录进行 HMAC-SM3运算，并定期比对日志记录和HMAC值。
# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

```bash
# Build WAR
./mvnw clean package        # macOS/Linux

# Deploy: drop target/SalarySystem-1.0-SNAPSHOT.war into Tomcat/webapps
# Or configure IntelliJ with a local Tomcat server

# Initialize database (run once, in this order)
mysql -u root -p < create-tables.sql
mysql -u root -p < init-data.sql

# Run test data generator
./mvnw test -Dtest=GenerateTestData
```

Default test accounts (password: `Admin@123`): `admin`, `hr`, `finance`, `audit`.

## Architecture

Traditional **Servlet + JSP** layered webapp, NO Spring. Java 21, Jakarta EE 11, Maven WAR, MySQL 8, deployed on Tomcat.

```
Servlet (@WebServlet) → Service (manual new) → DAO (implements BaseDao) → JDBC → MySQL
                                         JSP (server-side rendering)
```

- **DAO**: `BaseDao` interface provides `getConnection()` via JNDI lookup `java:comp/env/jdbc/SalarySystem`. JNDI config in `src/main/webapp/META-INF/context.xml`. All DAOs implement their interface and use raw JDBC `PreparedStatement`.
- **Service**: Manually instantiated in servlets (no DI). Auth/password logic lives in `SysUserServiceImpl`; SM4 encrypt/decrypt happens in DAO layer, not service layer.
- **Models**: Lombok `@Data` on all model classes. Naming uses lowercase camelCase (`empInfo`, `sysUser`), not standard Java conventions.
- **Views**: JSP with embedded Java scriptlets and inline CSS. Session attribute `currentUser` holds the logged-in `sysUser`. All pages must check this session or redirect to login.

## Key Implementation Details

### Chinese Cryptographic Algorithms (SM series)
- `SmCryptoUtil` wraps Hutool's SM3/SM4 and BouncyCastle's HMAC-SM3.
- **SM4 key is HARDCODED** (`1234567890abcdef`) — do not use in production.
- SM3 hashes passwords for `sys_user.password`.
- SM4 encrypts PII fields at rest: `emp_name`, `id_card`, `phone`, `address`, and family member `name`/`id_card`. DAO layer encrypts on write, decrypts on read with try-catch fallback to `[解密失败]`.
- `data_hash` column (SM3) provides integrity verification — computed from concatenated plaintext of all core fields.

### Authentication
- `SysUserServiceImpl.authenticate()`: checks lock, hashes input with SM3, compares to stored hash, logs login attempts to `sys_log`.
- Lockout: 5 consecutive failures → 30-minute lock. Password expires after 90 days (not enforced on login yet).
- Password complexity: ≥8 chars, must include upper, lower, digit, special.

### Audit Logging
- `sys_log` table records all actions (LOGIN, ADD_EMP, EXPORT_SALARY, etc.) with user ID, IP, timestamp, and optional HMAC-SM3 integrity hash.
- `SysLogServiceImpl.log()` is called throughout services and servlets — often wrapped in try-catch to prevent log failures from breaking business flow.

### Excel Import/Export
- Apache POI (`poi-ooxml`) for `.xlsx` read/write. `SalaryExportServlet` and `SalaryImportServlet` handle these.

### Data Desensitization
- `DesensitizeUtil` masks phone (保留前3后4), ID card (保留前3后4), name (首尾保留), address (前6字) for display purposes on certain pages.

## Database

- Database name: `web2026` (per `create-tables.sql`), but JNDI URL points to `SalarySystem` — ensure they match.
- JNDI config: `src/main/webapp/META-INF/context.xml`, credentials `root` / `123456`.
- Foreign keys with `ON DELETE CASCADE` on `emp_family`, `tax_deduction`, `salary_record` linked to `emp_info`.

## Recent Work Context

The current working tree has uncommitted changes across most layers — DAO, Service, Servlet, and JSP files. New files added include `PageResult.java` (generic pagination model), `CalculateTaxServlet.java`, `FamilyServlet.java`, `SalaryImportServlet.java`, and several new JSP pages (`family.jsp`, `emp-add.jsp`, `emp-edit.jsp`). This represents active development of the family management, tax calculation, salary import, and employee CRUD features.
