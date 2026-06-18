package com.salarysystem.service.impl;

import com.salarysystem.dao.impl.SalaryRecordDaoImpl;
import com.salarysystem.dao.impl.TaxDeductionDaoImpl;
import com.salarysystem.model.salaryRecord;
import com.salarysystem.model.taxDeduction;
import com.salarysystem.service.SalaryRecordService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.List;

public class SalaryRecordServiceImpl implements SalaryRecordService {

    private final SalaryRecordDaoImpl dao = new SalaryRecordDaoImpl();
    private final TaxDeductionDaoImpl taxDeductionDao = new TaxDeductionDaoImpl();
    private final SysLogServiceImpl logService = new SysLogServiceImpl();

    // 年度累计个税税率表
    private static final int[][] TAX_BRACKETS = {
        {0, 36000, 3, 0},            // ≤36,000: 3%, 速算扣除数 0
        {36000, 144000, 10, 2520},    // 36,001~144,000: 10%, 2520
        {144000, 300000, 20, 16920},  // 144,001~300,000: 20%, 16920
        {300000, 420000, 25, 31920},  // 300,001~420,000: 25%, 31920
        {420000, 660000, 30, 52920},  // 420,001~660,000: 30%, 52920
        {660000, 960000, 35, 85920},  // 660,001~960,000: 35%, 85920
        {960000, Integer.MAX_VALUE, 45, 181920} // >960,000: 45%, 181920
    };

    private static final BigDecimal MONTHLY_THRESHOLD = new BigDecimal("5000");

    @Override
    public void add(salaryRecord record) throws SQLException {
        dao.insert(record);
        try { logService.log(null, "ADD_SALARY", "SYSTEM"); } catch (SQLException ignored) {}
    }

    @Override
    public void update(salaryRecord record) throws SQLException {
        dao.update(record);
        try { logService.log(null, "UPDATE_SALARY", "SYSTEM"); } catch (SQLException ignored) {}
    }

    @Override
    public void delete(Long recordId) throws SQLException {
        dao.deleteById(recordId);
        try { logService.log(null, "DELETE_SALARY", "SYSTEM"); } catch (SQLException ignored) {}
    }

    @Override
    public salaryRecord findById(Long id) throws SQLException {
        return dao.findById(id);
    }

    @Override
    public salaryRecord findByEmpIdAndMonth(Integer empId, String salaryMonth) throws SQLException {
        return dao.findByEmpIdAndMonth(empId, salaryMonth);
    }

    @Override
    public List<salaryRecord> findByEmpId(Integer empId) throws SQLException {
        return dao.findByEmpId(empId);
    }

    @Override
    public List<salaryRecord> findAll() throws SQLException {
        return dao.findAll();
    }

    @Override
    public String calculateAllTaxForMonth(String salaryMonth) throws SQLException {
        int success = 0, skipped = 0, failed = 0;
        StringBuilder skipReasons = new StringBuilder();

        // 获取所有有当月薪资记录的员工
        List<salaryRecord> allRecords = dao.findAll();
        // 去重获取当月有记录的所有 empId
        java.util.Set<Integer> empIdSet = new java.util.LinkedHashSet<>();
        for (salaryRecord r : allRecords) {
            if (salaryMonth.equals(r.getSalaryMonth())) {
                empIdSet.add(r.getEmpId());
            }
        }

        for (Integer empId : empIdSet) {
            try {
                salaryRecord result = calculateTaxForEmployee(empId, salaryMonth);
                if (result != null) {
                    // 更新数据库中的 tax 和 actual_salary
                    dao.update(result);
                    success++;
                } else {
                    skipped++;
                }
            } catch (SkippedException e) {
                skipped++;
                skipReasons.append("员工ID=").append(empId).append(": ").append(e.getMessage()).append("\n");
            } catch (Exception e) {
                failed++;
                skipReasons.append("员工ID=").append(empId).append(": 计算异常\n");
            }
        }

        // 写入日志
        try {
            logService.log(null, "CALCULATE_TAX", "SYSTEM");
        } catch (SQLException ignored) {}

        String result = "计税完成：成功 " + success + " 人，跳过 " + skipped + " 人，失败 " + failed + " 人。";
        if (skipped > 0 && skipReasons.length() > 0) {
            // 只显示前几条跳过原因
            String[] lines = skipReasons.toString().split("\n");
            if (lines.length <= 3) {
                result += " 跳过原因: " + skipReasons.toString().trim();
            } else {
                result += " 跳过原因(前3条): " + lines[0] + "; " + lines[1] + "; " + lines[2] + "...";
            }
        }
        return result;
    }

    @Override
    public salaryRecord calculateTaxForEmployee(Integer empId, String salaryMonth) throws SQLException {
        // 解析年份和月份
        String[] parts = salaryMonth.split("-");
        if (parts.length != 2) {
            throw new SkippedException("月份格式错误");
        }
        int year = Integer.parseInt(parts[0]);
        int month = Integer.parseInt(parts[1]);

        // 1. 检查当月薪资记录是否存在
        salaryRecord currentRecord = dao.findByEmpIdAndMonth(empId, salaryMonth);
        if (currentRecord == null) {
            throw new SkippedException("当月无薪资记录");
        }

        // 2. 检查当年专项附加扣除申报（必须有，否则无法计算个税）
        taxDeduction deduction = taxDeductionDao.findByEmpIdAndYear(empId, year);
        if (deduction == null) {
            throw new SkippedException("无" + year + "年专项附加扣除申报");
        }

        // 3. 计算当月税前收入（5项合计）
        // 月薪 = 基本工资 + 岗位津贴 + 午餐补贴 + 加班工资 + 全勤工资
        BigDecimal monthlyGross = BigDecimal.ZERO
            .add(nonNull(currentRecord.getBasicSalary()))
            .add(nonNull(currentRecord.getPositionAllowance()))
            .add(nonNull(currentRecord.getLunchAllowance()))
            .add(nonNull(currentRecord.getOvertimeSalary()))
            .add(nonNull(currentRecord.getFullAttendSalary()));

        // 4. 计算当月专项附加扣除（直接使用年度申报金额）
        BigDecimal monthlySpecialDeduction = BigDecimal.ZERO
            .add(nonNull(deduction.getChildEdu()))
            .add(nonNull(deduction.getContEdu()))
            .add(nonNull(deduction.getMajorMed()))
            .add(nonNull(deduction.getHousingLoan()))
            .add(nonNull(deduction.getHousingRent()))
            .add(nonNull(deduction.getSupportElderly()))
            .add(nonNull(deduction.getBabyCare()));

        // 5. 计算应纳税所得额
        // = 月薪 - 社保 - 公积金 - 5000(起征点) - 当月专项附加扣除
        // 注意：迟到扣款不计入计税基数，只在最后实发工资时减去
        BigDecimal taxableIncome = monthlyGross
            .subtract(nonNull(currentRecord.getSocialSecurity()))
            .subtract(nonNull(currentRecord.getProvidentFund()))
            .subtract(MONTHLY_THRESHOLD)
            .subtract(monthlySpecialDeduction);

        // 6. 计算个税（年化法）
        BigDecimal currentMonthTax;
        if (taxableIncome.compareTo(BigDecimal.ZERO) <= 0) {
            currentMonthTax = BigDecimal.ZERO;
        } else {
            // 换算为年化应纳税所得额
            BigDecimal annualizedIncome = taxableIncome.multiply(new BigDecimal("12"));

            // 查税率表
            int[] bracket = findTaxBracket(annualizedIncome);
            int rate = bracket[2];
            int quickDeduction = bracket[3];

            // 年化应纳税额
            BigDecimal annualizedTax = annualizedIncome
                .multiply(new BigDecimal(rate))
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP)
                .subtract(new BigDecimal(quickDeduction));

            if (annualizedTax.compareTo(BigDecimal.ZERO) < 0) {
                annualizedTax = BigDecimal.ZERO;
            }

            // 月应纳税额 = 年化应纳税额 / 12
            currentMonthTax = annualizedTax.divide(new BigDecimal("12"), 2, RoundingMode.HALF_UP);
        }

        // 7. 计算实发工资
        // 实发 = 月薪 - 社保 - 公积金 - 个税 - 迟到扣款
        BigDecimal actualSalary = monthlyGross
            .subtract(nonNull(currentRecord.getSocialSecurity()))
            .subtract(nonNull(currentRecord.getProvidentFund()))
            .subtract(currentMonthTax)
            .subtract(nonNull(currentRecord.getAbsenceDeduction()));

        // 8. 更新记录
        currentRecord.setTax(currentMonthTax);
        currentRecord.setActualSalary(actualSalary);
        return currentRecord;
    }

    /**
     * 根据年化应纳税所得额查找适用的税率档位
     */
    private int[] findTaxBracket(BigDecimal annualizedIncome) {
        int incomeInCents = annualizedIncome.multiply(new BigDecimal("100")).intValue();
        for (int[] bracket : TAX_BRACKETS) {
            if (incomeInCents <= bracket[1] * 100) {
                return bracket;
            }
        }
        // 理论上不会到这里，返回最高档位
        return TAX_BRACKETS[TAX_BRACKETS.length - 1];
    }

    private BigDecimal nonNull(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    /**
     * 跳过异常：用于标记可忽略的跳过情况
     */
    public static class SkippedException extends RuntimeException {
        public SkippedException(String message) {
            super(message);
        }
    }
}
