package com.salarysystem.dao.impl;

import com.salarysystem.dao.SysDeptDao;
import com.salarysystem.model.sysDept;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SysDeptDaoImpl implements SysDeptDao {

    @Override
    public int insert(sysDept dept) throws SQLException {
        String sql = "INSERT INTO sys_dept(dept_name, remark) VALUES(?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, dept.getDeptName());
            ps.setString(2, dept.getRemark() == null ? "" : dept.getRemark());
            int affected = ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    dept.setDeptId(rs.getInt(1));
                }
            }
            return affected;
        }
    }

    @Override
    public int update(sysDept dept) throws SQLException {
        String sql = "UPDATE sys_dept SET dept_name=?, remark=? WHERE dept_id=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dept.getDeptName());
            ps.setString(2, dept.getRemark() == null ? "" : dept.getRemark());
            ps.setInt(3, dept.getDeptId());
            return ps.executeUpdate();
        }
    }

    @Override
    public int deleteById(Integer deptId) throws SQLException {
        String sql = "DELETE FROM sys_dept WHERE dept_id=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, deptId);
            return ps.executeUpdate();
        }
    }

    @Override
    public sysDept findById(Integer deptId) throws SQLException {
        String sql = "SELECT dept_id, dept_name, remark FROM sys_dept WHERE dept_id=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, deptId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    @Override
    public sysDept findByName(String deptName) throws SQLException {
        String sql = "SELECT dept_id, dept_name, remark FROM sys_dept WHERE dept_name=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, deptName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<sysDept> findAll() throws SQLException {
        String sql = "SELECT dept_id, dept_name, remark FROM sys_dept ORDER BY dept_id";
        List<sysDept> list = new ArrayList<>();
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
    public int countEmpByDeptName(String deptName) throws SQLException {
        String sql = "SELECT COUNT(*) FROM emp_info WHERE dept_name=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, deptName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    private sysDept mapRow(ResultSet rs) throws SQLException {
        sysDept d = new sysDept();
        d.setDeptId(rs.getInt("dept_id"));
        d.setDeptName(rs.getString("dept_name"));
        d.setRemark(rs.getString("remark"));
        return d;
    }
}
