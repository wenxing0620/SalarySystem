package com.salarysystem.dao.impl;

import com.salarysystem.dao.SalaryRecordDao;
import com.salarysystem.model.salaryRecord;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SalaryRecordDaoImpl implements SalaryRecordDao {

    @Override
    public int insert(salaryRecord record) throws SQLException {
        String sql = "INSERT INTO salary_record(emp_id, salary_month, expected_days, actual_days, basic_salary, position_allowance, lunch_allowance, overtime_salary, full_attend_salary, social_security, provident_fund, tax, absence_deduction, actual_salary) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, record.getEmpId());
            ps.setString(2, record.getSalaryMonth());
            ps.setInt(3, record.getExpectedDays());
            ps.setInt(4, record.getActualDays());
            ps.setBigDecimal(5, nonNull(record.getBasicSalary()));
            ps.setBigDecimal(6, nonNull(record.getPositionAllowance()));
            ps.setBigDecimal(7, nonNull(record.getLunchAllowance()));
            ps.setBigDecimal(8, nonNull(record.getOvertimeSalary()));
            ps.setBigDecimal(9, nonNull(record.getFullAttendSalary()));
            ps.setBigDecimal(10, nonNull(record.getSocialSecurity()));
            ps.setBigDecimal(11, nonNull(record.getProvidentFund()));
            ps.setBigDecimal(12, nonNull(record.getTax()));
            ps.setBigDecimal(13, nonNull(record.getAbsenceDeduction()));
            ps.setBigDecimal(14, nonNull(record.getActualSalary()));
            int affected = ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) record.setRecordId(rs.getLong(1));
            }
            return affected;
        }
    }

    @Override
    public int update(salaryRecord record) throws SQLException {
        String sql = "UPDATE salary_record SET emp_id=?, salary_month=?, expected_days=?, actual_days=?, basic_salary=?, position_allowance=?, lunch_allowance=?, overtime_salary=?, full_attend_salary=?, social_security=?, provident_fund=?, tax=?, absence_deduction=?, actual_salary=? WHERE record_id=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, record.getEmpId());
            ps.setString(2, record.getSalaryMonth());
            ps.setInt(3, record.getExpectedDays());
            ps.setInt(4, record.getActualDays());
            ps.setBigDecimal(5, nonNull(record.getBasicSalary()));
            ps.setBigDecimal(6, nonNull(record.getPositionAllowance()));
            ps.setBigDecimal(7, nonNull(record.getLunchAllowance()));
            ps.setBigDecimal(8, nonNull(record.getOvertimeSalary()));
            ps.setBigDecimal(9, nonNull(record.getFullAttendSalary()));
            ps.setBigDecimal(10, nonNull(record.getSocialSecurity()));
            ps.setBigDecimal(11, nonNull(record.getProvidentFund()));
            ps.setBigDecimal(12, nonNull(record.getTax()));
            ps.setBigDecimal(13, nonNull(record.getAbsenceDeduction()));
            ps.setBigDecimal(14, nonNull(record.getActualSalary()));
            ps.setLong(15, record.getRecordId());
            return ps.executeUpdate();
        }
    }

    @Override
    public int deleteById(Long recordId) throws SQLException {
        String sql = "DELETE FROM salary_record WHERE record_id=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, recordId);
            return ps.executeUpdate();
        }
    }

    @Override
    public salaryRecord findById(Long recordId) throws SQLException {
        String sql = "SELECT record_id, emp_id, salary_month, expected_days, actual_days, basic_salary, position_allowance, lunch_allowance, overtime_salary, full_attend_salary, social_security, provident_fund, tax, absence_deduction, actual_salary FROM salary_record WHERE record_id=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, recordId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    @Override
    public salaryRecord findByEmpIdAndMonth(Integer empId, String salaryMonth) throws SQLException {
        String sql = "SELECT record_id, emp_id, salary_month, expected_days, actual_days, basic_salary, position_allowance, lunch_allowance, overtime_salary, full_attend_salary, social_security, provident_fund, tax, absence_deduction, actual_salary FROM salary_record WHERE emp_id=? AND salary_month=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, empId);
            ps.setString(2, salaryMonth);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    @Override
    public List<salaryRecord> findByEmpId(Integer empId) throws SQLException {
        String sql = "SELECT record_id, emp_id, salary_month, expected_days, actual_days, basic_salary, position_allowance, lunch_allowance, overtime_salary, full_attend_salary, social_security, provident_fund, tax, absence_deduction, actual_salary FROM salary_record WHERE emp_id=? ORDER BY salary_month DESC";
        List<salaryRecord> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, empId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    @Override
    public List<salaryRecord> findAll() throws SQLException {
        String sql = "SELECT record_id, emp_id, salary_month, expected_days, actual_days, basic_salary, position_allowance, lunch_allowance, overtime_salary, full_attend_salary, social_security, provident_fund, tax, absence_deduction, actual_salary FROM salary_record ORDER BY salary_month DESC, emp_id ASC";
        List<salaryRecord> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    @Override
    public List<salaryRecord> findByEmpIdAndMonthRange(Integer empId, String startMonth, String endMonth) throws SQLException {
        String sql = "SELECT record_id, emp_id, salary_month, expected_days, actual_days, basic_salary, position_allowance, lunch_allowance, overtime_salary, full_attend_salary, social_security, provident_fund, tax, absence_deduction, actual_salary FROM salary_record WHERE emp_id=? AND salary_month >= ? AND salary_month <= ? ORDER BY salary_month ASC";
        List<salaryRecord> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, empId);
            ps.setString(2, startMonth);
            ps.setString(3, endMonth);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    @Override
    public java.math.BigDecimal sumTaxByEmpIdAndYearExcludeMonth(Integer empId, int year, String excludeMonth) throws SQLException {
        String sql = "SELECT COALESCE(SUM(tax), 0) FROM salary_record WHERE emp_id=? AND salary_month >= ? AND salary_month <= ? AND salary_month <> ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, empId);
            ps.setString(2, year + "-01");
            ps.setString(3, year + "-12");
            ps.setString(4, excludeMonth);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getBigDecimal(1);
            }
        }
        return java.math.BigDecimal.ZERO;
    }

    private salaryRecord mapRow(ResultSet rs) throws SQLException {
        salaryRecord r = new salaryRecord();
        r.setRecordId(rs.getLong("record_id"));
        r.setEmpId(rs.getInt("emp_id"));
        r.setSalaryMonth(rs.getString("salary_month"));
        r.setExpectedDays(rs.getInt("expected_days"));
        r.setActualDays(rs.getInt("actual_days"));
        r.setBasicSalary(rs.getBigDecimal("basic_salary"));
        r.setPositionAllowance(rs.getBigDecimal("position_allowance"));
        r.setLunchAllowance(rs.getBigDecimal("lunch_allowance"));
        r.setOvertimeSalary(rs.getBigDecimal("overtime_salary"));
        r.setFullAttendSalary(rs.getBigDecimal("full_attend_salary"));
        r.setSocialSecurity(rs.getBigDecimal("social_security"));
        r.setProvidentFund(rs.getBigDecimal("provident_fund"));
        r.setTax(rs.getBigDecimal("tax"));
        r.setAbsenceDeduction(rs.getBigDecimal("absence_deduction"));
        r.setActualSalary(rs.getBigDecimal("actual_salary"));
        return r;
    }

    private BigDecimal nonNull(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
