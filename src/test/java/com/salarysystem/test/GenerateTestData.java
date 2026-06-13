package com.salarysystem.test;

import com.salarysystem.util.SmCryptoUtil;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.time.LocalDateTime;

/**
 * 验收测试数据生成器。
 *
 * 运行方式（二选一）：
 *   1. IDE 中直接运行 main() 方法
 *   2. mvn test -Dtest=GenerateTestData
 *
 * 前置条件：
 *   1. MySQL 已启动，数据库 SalarySystem 已创建
 *   2. 表结构已通过 create-tables.sql 初始化
 *   3. 基础数据已通过 init-data.sql 导入（角色 + 用户 + 部门）
 *
 * 生成内容：
 *   12 名员工 + 14 名家属 + 12 条专项扣除 + 12 条薪资记录 + 批量个税计算
 */
public class GenerateTestData {

    private static final String URL = "jdbc:mysql://localhost:3306/SalarySystem?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASS = "123456";

    private Connection conn;

    public static void main(String[] args) throws Exception {
        new GenerateTestData().generate();
    }

    @Test
    public void testGenerate() throws Exception {
        generate();
    }

    private void generate() throws Exception {
        conn = DriverManager.getConnection(URL, USER, PASS);
        conn.setAutoCommit(false);
        try {
            clearOldData();
            int[] empIds = createEmployees();
            createFamilies(empIds);
            createDeductions(empIds);
            createSalaries(empIds);
            calculateAllTaxes();
            linkUsers();
            conn.commit();
            System.out.println("\n✓ 验收测试数据全部生成完成！");
            System.out.println("  12 名员工 + 14 名家属 + 12 条扣除申报 + 12 条薪资记录（个税已计算）");
        } catch (Exception e) {
            conn.rollback();
            throw e;
        } finally {
            conn.close();
        }
    }

    // ── 清理旧数据 ──
    private void clearOldData() throws SQLException {
        System.out.println("── 清理旧测试数据...");
        String[] nos = {"EMP001","EMP002","EMP003","EMP004","EMP005","EMP006",
                        "EMP007","EMP008","EMP009","EMP010","EMP011","EMP012"};
        for (String no : nos) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT emp_id FROM emp_info WHERE emp_no = ?")) {
                ps.setString(1, no);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    int id = rs.getInt(1);
                    // 级联删除家属、扣除、薪资（外键 ON DELETE CASCADE）
                    try (PreparedStatement del = conn.prepareStatement("DELETE FROM emp_info WHERE emp_id = ?")) {
                        del.setInt(1, id);
                        del.executeUpdate();
                    }
                    System.out.println("  已删除: " + no);
                }
            } catch (SQLException ignored) {}
        }
    }

    // ── 1. 创建员工 ──
    private int[] createEmployees() throws SQLException {
        System.out.println("\n── 创建 12 名员工...");
        Object[][] data = {
            {"EMP001","张建国","技术部","技术总监","320102197805151234","13805151234","江苏省南京市玄武区丹凤街88号1203室"},
            {"EMP002","李明辉","技术部","高级工程师","320103198506201234","13905161234","江苏省南京市鼓楼区湖南路66号501室"},
            {"EMP003","王芳",  "技术部","中级工程师","320104199203081234","13705171234","江苏省南京市秦淮区中山南路300号802室"},
            {"EMP004","陈小龙","技术部","初级工程师","320105199810251234","13605181234","江苏省南京市建邺区江东中路100号1502室"},
            {"EMP005","赵雅婷","财务部","财务经理","320106198203141234","13505191234","江苏省南京市雨花台区软件大道168号701室"},
            {"EMP006","钱小华","财务部","会计",    "320107199507091234","13405201234","江苏省南京市栖霞区仙林大道169号303室"},
            {"EMP007","孙丽萍","人事部","人事经理","320108198803211234","13305211234","江苏省南京市江宁区双龙大道200号1101室"},
            {"EMP008","周文博","人事部","招聘专员","320109199601121234","13205221234","江苏省南京市浦口区大桥北路50号403室"},
            {"EMP009","吴海涛","市场部","市场总监","320110197912081234","13105231234","江苏省南京市六合区新华路88号902室"},
            {"EMP010","郑晓燕","市场部","销售专员","320111199405061234","13005241234","江苏省南京市溧水区交通路12号202室"},
            {"EMP011","冯志远","行政部","行政主管","320112198911301234","18905251234","江苏省南京市高淳区镇兴路33号601室"},
            {"EMP012","何小娟","行政部","前台文员","320113199808221234","18805261234","江苏省南京市玄武区北京东路15号104室"},
        };

        String sql = "INSERT INTO emp_info(emp_no, dept_name, position, emp_name, id_card, phone, address, data_hash) VALUES(?,?,?,?,?,?,?,?)";
        int[] ids = new int[data.length];
        for (int i = 0; i < data.length; i++) {
            Object[] d = data[i];
            String empNo = (String)d[0], name = (String)d[1], dept = (String)d[2], pos = (String)d[3];
            String idCard = (String)d[4], phone = (String)d[5], addr = (String)d[6];
            String hash = SmCryptoUtil.hashSm3(empNo + dept + pos + name + idCard + phone + addr);

            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, empNo);
                ps.setString(2, dept);
                ps.setString(3, pos);
                ps.setString(4, SmCryptoUtil.encryptSm4(name));
                ps.setString(5, SmCryptoUtil.encryptSm4(idCard));
                ps.setString(6, SmCryptoUtil.encryptSm4(phone));
                ps.setString(7, SmCryptoUtil.encryptSm4(addr));
                ps.setString(8, hash);
                ps.executeUpdate();
                ResultSet rs = ps.getGeneratedKeys();
                rs.next();
                ids[i] = rs.getInt(1);
                System.out.printf("  [%2d] %s %s - %s%n", ids[i], empNo, name, dept);
            }
        }
        return ids;
    }

    // ── 2. 创建家属 ──
    private void createFamilies(int[] empIds) throws SQLException {
        System.out.println("\n── 创建家属...");
        Object[][] data = {
            {0, "儿子", "张子轩", "320102200508151234"},
            {0, "父亲", "张德厚", "320102195203101234"},
            {1, "女儿", "李思雨", "320103201506201234"},
            {2, "儿子", "王小宝", "320104202301081234"},
            {2, "女儿", "王小贝", "320104202301082345"},
            {4, "儿子", "赵浩然", "320106200603141234"},
            {5, "母亲", "钱秀英", "320107195507091234"},
            {6, "儿子", "孙浩然", "320108201203211234"},
            {6, "女儿", "孙雨桐", "320108201503212345"},
            {8, "儿子", "吴明哲", "320110200512081234"},
            {8, "父亲", "吴德胜", "320110195112081234"},
            {9, "女儿", "郑小萌", "320111202405061234"},
            {10,"儿子", "冯宇轩", "320112201311301234"},
            {10,"母亲", "冯刘氏", "320112195811301234"},
        };

        String sql = "INSERT INTO emp_family(emp_id, relation, name, id_card) VALUES(?,?,?,?)";
        for (Object[] d : data) {
            int empId = empIds[(int)d[0]];
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, empId);
                ps.setString(2, (String)d[1]);
                ps.setString(3, SmCryptoUtil.encryptSm4((String)d[2]));
                ps.setString(4, SmCryptoUtil.encryptSm4((String)d[3]));
                ps.executeUpdate();
                System.out.printf("  %s - %s %s%n", d[1], d[2], d[3]);
            }
        }
    }

    // ── 3. 创建专项附加扣除 ──
    private void createDeductions(int[] empIds) throws SQLException {
        System.out.println("\n── 创建 2026 年专项附加扣除申报...");
        // childEdu, contEdu, majorMed, housingLoan, housingRent, supportElderly, babyCare
        Object[][] data = {
            {0,  12000, 0,     0,     0,     0,     12000, 0},      // 张建国：子女教育+赡养
            {1,  12000, 0,     0, 12000, 0,     0,     0},          // 李明辉：子女教育+房贷
            {2,  0,     0,     0,     0,     0,     0, 24000},       // 王芳：双胞胎婴幼儿
            {3,  0,  3600,     0,     0,     0,     0,     0},      // 陈小龙：继续教育
            {4,  12000, 0, 20000,     0,     0,     0,     0},      // 赵雅婷：子女教育+大病
            {5,  0,     0,     0,     0, 18000, 12000, 0},          // 钱小华：租房+赡养
            {6,  24000, 0,     0,     0,     0,     0,     0},      // 孙丽萍：子女教育×2
            {7,  0,     0,     0,     0,     0,     0,     0},      // 周文博：无
            {8,  12000, 0,     0, 12000, 0, 24000, 0},              // 吴海涛：子女+房贷+赡养(独生)
            {9,  0,     0,     0,     0,     0,     0, 12000},       // 郑晓燕：婴幼儿
            {10, 12000, 0,     0, 12000, 0, 12000, 0},              // 冯志远：子女+房贷+赡养
            {11, 0,     0,     0,     0, 18000, 0,     0},          // 何小娟：租房
        };

        String sql = "INSERT INTO tax_deduction(emp_id, declare_year, child_edu, cont_edu, major_med, housing_loan, housing_rent, support_elderly, baby_care) VALUES(?,2026,?,?,?,?,?,?,?)";
        for (Object[] d : data) {
            int empId = empIds[(int)d[0]];
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, empId);
                for (int j = 1; j <= 7; j++) ps.setBigDecimal(j+1, bd(d[j]));
                ps.executeUpdate();
                BigDecimal total = bd(d[1]).add(bd(d[2])).add(bd(d[3])).add(bd(d[4])).add(bd(d[5])).add(bd(d[6])).add(bd(d[7]));
                System.out.printf("  员工[%d] 年扣除总额=%.0f 月均=%.2f%n", empId, total, total.divide(new BigDecimal("12"), 2, RoundingMode.HALF_UP));
            }
        }
    }

    // ── 4. 创建薪资记录 ──
    private void createSalaries(int[] empIds) throws SQLException {
        System.out.println("\n── 创建 2026-06 薪资记录...");
        // exD, acD, basic, pos, lunch, overtime, fullAttend, ss, pf, absence
        Object[][] data = {
            {0,  22,22, 18000,5000,600,0,   1000, 2100,1800,0},    // 张建国
            {1,  22,22, 14000,3000,500,0,    800, 1600,1400,0},    // 李明辉
            {2,  22,22, 10000,1500,500,0,    500, 1200,1000,0},    // 王芳
            {3,  22,22,  7000, 800,400,800,  300,  900, 700,0},    // 陈小龙
            {4,  22,22, 13000,2500,500,0,    800, 1500,1300,0},    // 赵雅婷
            {5,  22,22,  7000,1200,400,0,    400,  850, 650,0},    // 钱小华
            {6,  22,22, 12000,2500,500,0,    800, 1450,1200,0},    // 孙丽萍
            {7,  22,20,  6000, 800,400,200,    0,  750, 550,100},  // 周文博（迟到2天）
            {8,  22,22, 16000,4000,600,2000,1000, 2100,1800,0},    // 吴海涛（加班）
            {9,  22,22,  6000,1000,400,500,  300,  780, 600,0},    // 郑晓燕（加班）
            {10, 22,22,  8500,1500,400,0,    500, 1050, 850,0},    // 冯志远
            {11, 22,22,  4000, 500,400,0,    200,  500, 350,0},    // 何小娟
        };

        String sql = "INSERT INTO salary_record(emp_id, salary_month, expected_days, actual_days, basic_salary, position_allowance, lunch_allowance, overtime_salary, full_attend_salary, social_security, provident_fund, tax, absence_deduction, actual_salary) VALUES(?,?,?,?,?,?,?,?,?,?,?,0,?,0)";
        for (Object[] d : data) {
            int empId = empIds[(int)d[0]];
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, empId);
                ps.setString(2, "2026-06");
                ps.setInt(3, (int)d[1]);
                ps.setInt(4, (int)d[2]);
                ps.setBigDecimal(5, bd(d[3]));
                ps.setBigDecimal(6, bd(d[4]));
                ps.setBigDecimal(7, bd(d[5]));
                ps.setBigDecimal(8, bd(d[6]));
                ps.setBigDecimal(9, bd(d[7]));
                ps.setBigDecimal(10, bd(d[8]));
                ps.setBigDecimal(11, bd(d[9]));
                ps.setBigDecimal(12, bd(d[10]));
                ps.executeUpdate();
                BigDecimal gross = bd(d[3]).add(bd(d[4])).add(bd(d[5])).add(bd(d[6])).add(bd(d[7]));
                System.out.printf("  员工[%d] 月薪总额=%.0f%n", empId, gross);
            }
        }
    }

    // ── 5. 批量计算个税 ──
    private void calculateAllTaxes() throws SQLException {
        System.out.println("\n── 批量计算 2026-06 个税...");

        // 获取当月所有薪资记录
        var records = new java.util.ArrayList<long[]>();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT record_id, emp_id FROM salary_record WHERE salary_month = '2026-06'")) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) records.add(new long[]{rs.getLong(1), rs.getInt(2)});
        }

        int success = 0, skipped = 0;
        for (long[] rec : records) {
            long recordId = rec[0];
            int empId = (int) rec[1];
            try {
                BigDecimal[] taxResult = calcTax(empId);
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE salary_record SET tax = ?, actual_salary = ? WHERE record_id = ?")) {
                    ps.setBigDecimal(1, taxResult[0]);
                    ps.setBigDecimal(2, taxResult[1]);
                    ps.setLong(3, recordId);
                    ps.executeUpdate();
                }
                success++;
                System.out.printf("  员工[%d] 个税=%.2f 实发=%.2f%n", empId, taxResult[0], taxResult[1]);
            } catch (SkippedException e) {
                skipped++;
                System.out.println("  员工[" + empId + "] 跳过: " + e.getMessage());
            }
        }
        System.out.printf("  计税完成: 成功 %d 人, 跳过 %d 人%n", success, skipped);
    }

    // ── 个税计算逻辑（与 SalaryRecordServiceImpl.calculateTaxForEmployee 一致） ──
    private static final int[][] TAX_BRACKETS = {
        {0, 36000, 3, 0},
        {36000, 144000, 10, 2520},
        {144000, 300000, 20, 16920},
        {300000, 420000, 25, 31920},
        {420000, 660000, 30, 52920},
        {660000, 960000, 35, 85920},
        {960000, Integer.MAX_VALUE, 45, 181920}
    };
    private static final BigDecimal MONTHLY_THRESHOLD = new BigDecimal("5000");

    private BigDecimal[] calcTax(int empId) throws SQLException {
        // 读取当月薪资
        salaryRow sr = null;
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT basic_salary, position_allowance, lunch_allowance, overtime_salary, full_attend_salary, social_security, provident_fund, absence_deduction FROM salary_record WHERE emp_id = ? AND salary_month = '2026-06'")) {
            ps.setInt(1, empId);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) throw new SkippedException("当月无薪资记录");
            sr = new salaryRow(rs);
        }

        // 读取年度专项扣除
        BigDecimal annualDeduction = BigDecimal.ZERO;
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT child_edu, cont_edu, major_med, housing_loan, housing_rent, support_elderly, baby_care FROM tax_deduction WHERE emp_id = ? AND declare_year = 2026")) {
            ps.setInt(1, empId);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) throw new SkippedException("无2026年专项附加扣除申报");
            for (int i = 1; i <= 7; i++) annualDeduction = annualDeduction.add(rs.getBigDecimal(i));
        }

        BigDecimal monthlySpecial = annualDeduction.divide(new BigDecimal("12"), 2, RoundingMode.HALF_UP);
        BigDecimal monthlyGross = sr.basic.add(sr.position).add(sr.lunch).add(sr.overtime).add(sr.fullAttend);
        BigDecimal taxableIncome = monthlyGross.subtract(sr.ss).subtract(sr.pf).subtract(MONTHLY_THRESHOLD).subtract(monthlySpecial);

        BigDecimal tax;
        if (taxableIncome.compareTo(BigDecimal.ZERO) <= 0) {
            tax = BigDecimal.ZERO;
        } else {
            BigDecimal annualized = taxableIncome.multiply(new BigDecimal("12"));
            int cents = annualized.multiply(new BigDecimal("100")).intValue();
            int rate = 3, quick = 0;
            for (int[] b : TAX_BRACKETS) {
                if (cents <= b[1] * 100) { rate = b[2]; quick = b[3]; break; }
            }
            BigDecimal annualizedTax = annualized.multiply(new BigDecimal(rate)).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP).subtract(new BigDecimal(quick));
            if (annualizedTax.compareTo(BigDecimal.ZERO) < 0) annualizedTax = BigDecimal.ZERO;
            tax = annualizedTax.divide(new BigDecimal("12"), 2, RoundingMode.HALF_UP);
        }

        BigDecimal actualSalary = monthlyGross.subtract(sr.ss).subtract(sr.pf).subtract(tax).subtract(sr.absence);
        return new BigDecimal[]{tax, actualSalary};
    }

    private static class salaryRow {
        final BigDecimal basic, position, lunch, overtime, fullAttend, ss, pf, absence;
        salaryRow(ResultSet rs) throws SQLException {
            basic = rs.getBigDecimal(1); position = rs.getBigDecimal(2); lunch = rs.getBigDecimal(3);
            overtime = rs.getBigDecimal(4); fullAttend = rs.getBigDecimal(5);
            ss = rs.getBigDecimal(6); pf = rs.getBigDecimal(7); absence = rs.getBigDecimal(8);
        }
    }

    private static class SkippedException extends RuntimeException {
        SkippedException(String msg) { super(msg); }
    }

    // ── 6. 关联用户到员工 ──
    private void linkUsers() throws SQLException {
        System.out.println("\n── 关联系统用户到员工...");
        String[][] links = {{"hr","EMP007"}, {"finance","EMP005"}};
        for (String[] link : links) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE sys_user u JOIN emp_info e ON e.emp_no = ? SET u.emp_id = e.emp_id WHERE u.username = ?")) {
                ps.setString(1, link[1]);
                ps.setString(2, link[0]);
                int n = ps.executeUpdate();
                if (n > 0) System.out.printf("  用户 %s → 员工 %s%n", link[0], link[1]);
            }
        }
    }

    private static BigDecimal bd(Object v) {
        if (v instanceof Integer) return new BigDecimal((int)v);
        if (v instanceof Double) return new BigDecimal((double)v);
        if (v instanceof String) return new BigDecimal((String)v);
        return BigDecimal.ZERO;
    }
}
