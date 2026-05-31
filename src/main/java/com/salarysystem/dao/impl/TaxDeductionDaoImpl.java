package com.salarysystem.dao.impl;

import com.salarysystem.dao.TaxDeductionDao;
import com.salarysystem.model.taxDeduction;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TaxDeductionDaoImpl implements TaxDeductionDao {

    @Override
    public int insert(taxDeduction deduction) throws SQLException {
        String sql = "INSERT INTO tax_deduction(emp_id, declare_year, child_edu, cont_edu, major_med, housing_loan, housing_rent, support_elderly, baby_care) VALUES(?,?,?,?,?,?,?,?,?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, deduction.getEmpId());
            ps.setInt(2, deduction.getDeclareYear());
            ps.setBigDecimal(3, nonNull(deduction.getChildEdu()));
            ps.setBigDecimal(4, nonNull(deduction.getContEdu()));
            ps.setBigDecimal(5, nonNull(deduction.getMajorMed()));
            ps.setBigDecimal(6, nonNull(deduction.getHousingLoan()));
            ps.setBigDecimal(7, nonNull(deduction.getHousingRent()));
            ps.setBigDecimal(8, nonNull(deduction.getSupportElderly()));
            ps.setBigDecimal(9, nonNull(deduction.getBabyCare()));
            int affected = ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) deduction.setDeductionId(rs.getInt(1));
            }
            return affected;
        }
    }

    @Override
    public int update(taxDeduction deduction) throws SQLException {
        String sql = "UPDATE tax_deduction SET emp_id=?, declare_year=?, child_edu=?, cont_edu=?, major_med=?, housing_loan=?, housing_rent=?, support_elderly=?, baby_care=? WHERE deduction_id=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, deduction.getEmpId());
            ps.setInt(2, deduction.getDeclareYear());
            ps.setBigDecimal(3, nonNull(deduction.getChildEdu()));
            ps.setBigDecimal(4, nonNull(deduction.getContEdu()));
            ps.setBigDecimal(5, nonNull(deduction.getMajorMed()));
            ps.setBigDecimal(6, nonNull(deduction.getHousingLoan()));
            ps.setBigDecimal(7, nonNull(deduction.getHousingRent()));
            ps.setBigDecimal(8, nonNull(deduction.getSupportElderly()));
            ps.setBigDecimal(9, nonNull(deduction.getBabyCare()));
            ps.setInt(10, deduction.getDeductionId());
            return ps.executeUpdate();
        }
    }

    @Override
    public int deleteById(Integer deductionId) throws SQLException {
        String sql = "DELETE FROM tax_deduction WHERE deduction_id=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, deductionId);
            return ps.executeUpdate();
        }
    }

    @Override
    public taxDeduction findById(Integer deductionId) throws SQLException {
        String sql = "SELECT deduction_id, emp_id, declare_year, child_edu, cont_edu, major_med, housing_loan, housing_rent, support_elderly, baby_care FROM tax_deduction WHERE deduction_id=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, deductionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    @Override
    public taxDeduction findByEmpIdAndYear(Integer empId, Integer year) throws SQLException {
        String sql = "SELECT deduction_id, emp_id, declare_year, child_edu, cont_edu, major_med, housing_loan, housing_rent, support_elderly, baby_care FROM tax_deduction WHERE emp_id=? AND declare_year=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, empId);
            ps.setInt(2, year);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    @Override
    public List<taxDeduction> findByEmpId(Integer empId) throws SQLException {
        String sql = "SELECT deduction_id, emp_id, declare_year, child_edu, cont_edu, major_med, housing_loan, housing_rent, support_elderly, baby_care FROM tax_deduction WHERE emp_id=?";
        List<taxDeduction> list = new ArrayList<>();
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
    public List<taxDeduction> findAll() throws SQLException {
        String sql = "SELECT deduction_id, emp_id, declare_year, child_edu, cont_edu, major_med, housing_loan, housing_rent, support_elderly, baby_care FROM tax_deduction ORDER BY declare_year DESC, emp_id ASC";
        List<taxDeduction> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    private taxDeduction mapRow(ResultSet rs) throws SQLException {
        taxDeduction t = new taxDeduction();
        t.setDeductionId(rs.getInt("deduction_id"));
        t.setEmpId(rs.getInt("emp_id"));
        t.setDeclareYear(rs.getInt("declare_year"));
        t.setChildEdu(rs.getBigDecimal("child_edu"));
        t.setContEdu(rs.getBigDecimal("cont_edu"));
        t.setMajorMed(rs.getBigDecimal("major_med"));
        t.setHousingLoan(rs.getBigDecimal("housing_loan"));
        t.setHousingRent(rs.getBigDecimal("housing_rent"));
        t.setSupportElderly(rs.getBigDecimal("support_elderly"));
        t.setBabyCare(rs.getBigDecimal("baby_care"));
        return t;
    }

    private BigDecimal nonNull(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}

