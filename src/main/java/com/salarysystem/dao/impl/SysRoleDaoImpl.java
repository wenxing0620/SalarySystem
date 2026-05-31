package com.salarysystem.dao.impl;

import com.salarysystem.dao.SysRoleDao;
import com.salarysystem.model.sysRole;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SysRoleDaoImpl implements SysRoleDao {

    @Override
    public int insert(sysRole role) throws SQLException {
        String sql = "INSERT INTO sys_role(role_name) VALUES(?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, role.getRoleName());
            int affected = ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    role.setRoleId(rs.getInt(1));
                }
            }
            return affected;
        }
    }

    @Override
    public int update(sysRole role) throws SQLException {
        String sql = "UPDATE sys_role SET role_name=? WHERE role_id=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, role.getRoleName());
            ps.setInt(2, role.getRoleId());
            return ps.executeUpdate();
        }
    }

    @Override
    public int deleteById(Integer roleId) throws SQLException {
        String sql = "DELETE FROM sys_role WHERE role_id=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roleId);
            return ps.executeUpdate();
        }
    }

    @Override
    public sysRole findById(Integer roleId) throws SQLException {
        String sql = "SELECT role_id, role_name FROM sys_role WHERE role_id=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roleId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    sysRole r = new sysRole();
                    r.setRoleId(rs.getInt("role_id"));
                    r.setRoleName(rs.getString("role_name"));
                    return r;
                }
            }
        }
        return null;
    }

    @Override
    public List<sysRole> findAll() throws SQLException {
        String sql = "SELECT role_id, role_name FROM sys_role";
        List<sysRole> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                sysRole r = new sysRole();
                r.setRoleId(rs.getInt("role_id"));
                r.setRoleName(rs.getString("role_name"));
                list.add(r);
            }
        }
        return list;
    }
}

