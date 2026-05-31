package com.salarysystem.dao.impl;

import com.salarysystem.dao.SysUserDao;
import com.salarysystem.model.sysUser;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SysUserDaoImpl implements SysUserDao {

    @Override
    public int insert(sysUser user) throws SQLException {
        String sql = "INSERT INTO sys_user(emp_id, username, password, role_id, pwd_update_time, fail_count, lock_time) VALUES(?,?,?,?,?,?,?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (user.getEmpId() == null) ps.setNull(1, Types.INTEGER); else ps.setInt(1, user.getEmpId());
            ps.setString(2, user.getUsername());
            // store the password as-is (already hashed in service layer)
            ps.setString(3, user.getPassword());
            ps.setInt(4, user.getRoleId());
            ps.setTimestamp(5, user.getPwdUpdateTime() == null ? null : Timestamp.valueOf(user.getPwdUpdateTime()));
            if (user.getFailCount() == null) ps.setNull(6, Types.INTEGER); else ps.setInt(6, user.getFailCount());
            ps.setTimestamp(7, user.getLockTime() == null ? null : Timestamp.valueOf(user.getLockTime()));
            int affected = ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    user.setUserId(rs.getInt(1));
                }
            }
            return affected;
        }
    }

    @Override
    public int update(sysUser user) throws SQLException {
        String sql = "UPDATE sys_user SET emp_id=?, username=?, password=?, role_id=?, pwd_update_time=?, fail_count=?, lock_time=? WHERE user_id=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (user.getEmpId() == null) ps.setNull(1, Types.INTEGER); else ps.setInt(1, user.getEmpId());
            ps.setString(2, user.getUsername());
            // update with stored password (already hashed in service layer)
            ps.setString(3, user.getPassword());
            ps.setInt(4, user.getRoleId());
            ps.setTimestamp(5, user.getPwdUpdateTime() == null ? null : Timestamp.valueOf(user.getPwdUpdateTime()));
            if (user.getFailCount() == null) ps.setNull(6, Types.INTEGER); else ps.setInt(6, user.getFailCount());
            ps.setTimestamp(7, user.getLockTime() == null ? null : Timestamp.valueOf(user.getLockTime()));
            ps.setInt(8, user.getUserId());
            return ps.executeUpdate();
        }
    }

    @Override
    public int deleteById(Integer userId) throws SQLException {
        String sql = "DELETE FROM sys_user WHERE user_id=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate();
        }
    }

    @Override
    public sysUser findById(Integer userId) throws SQLException {
        String sql = "SELECT user_id, emp_id, username, password, role_id, pwd_update_time, fail_count, lock_time FROM sys_user WHERE user_id=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    @Override
    public sysUser findByUsername(String username) throws SQLException {
        String sql = "SELECT user_id, emp_id, username, password, role_id, pwd_update_time, fail_count, lock_time FROM sys_user WHERE username=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<sysUser> findAll() throws SQLException {
        String sql = "SELECT user_id, emp_id, username, password, role_id, pwd_update_time, fail_count, lock_time FROM sys_user";
        List<sysUser> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    private sysUser mapRow(ResultSet rs) throws SQLException {
        sysUser u = new sysUser();
        u.setUserId(rs.getInt("user_id"));
        int empId = rs.getInt("emp_id");
        if (!rs.wasNull()) u.setEmpId(empId);
        u.setUsername(rs.getString("username"));
        u.setPassword(rs.getString("password"));
        u.setRoleId(rs.getInt("role_id"));
        Timestamp t1 = rs.getTimestamp("pwd_update_time");
        u.setPwdUpdateTime(t1 == null ? null : t1.toLocalDateTime());
        int fail = rs.getInt("fail_count");
        if (!rs.wasNull()) u.setFailCount(fail);
        Timestamp t2 = rs.getTimestamp("lock_time");
        u.setLockTime(t2 == null ? null : t2.toLocalDateTime());
        return u;
    }
}

