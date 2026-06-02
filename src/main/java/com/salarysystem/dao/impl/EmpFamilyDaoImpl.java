package com.salarysystem.dao.impl;

import com.salarysystem.dao.EmpFamilyDao;
import com.salarysystem.model.empFamily;
import com.salarysystem.util.SmCryptoUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmpFamilyDaoImpl implements EmpFamilyDao {

    @Override
    public int insert(empFamily family) throws SQLException {
        String sql = "INSERT INTO emp_family(emp_id, relation, name, id_card) VALUES(?,?,?,?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, family.getEmpId());
            ps.setString(2, family.getRelation());
            ps.setString(3, SmCryptoUtil.encryptSm4(family.getName()));
            ps.setString(4, SmCryptoUtil.encryptSm4(family.getIdCard()));
            int affected = ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    family.setFamilyId(rs.getInt(1));
                }
            }
            return affected;
        }
    }

    @Override
    public int update(empFamily family) throws SQLException {
        String sql = "UPDATE emp_family SET emp_id=?, relation=?, name=?, id_card=? WHERE family_id=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, family.getEmpId());
            ps.setString(2, family.getRelation());
            ps.setString(3, SmCryptoUtil.encryptSm4(family.getName()));
            ps.setString(4, SmCryptoUtil.encryptSm4(family.getIdCard()));
            ps.setInt(5, family.getFamilyId());
            return ps.executeUpdate();
        }
    }

    @Override
    public int deleteById(Integer familyId) throws SQLException {
        String sql = "DELETE FROM emp_family WHERE family_id=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, familyId);
            return ps.executeUpdate();
        }
    }

    @Override
    public empFamily findById(Integer familyId) throws SQLException {
        String sql = "SELECT family_id, emp_id, relation, name, id_card FROM emp_family WHERE family_id=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, familyId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    empFamily f = new empFamily();
                    f.setFamilyId(rs.getInt("family_id"));
                    f.setEmpId(rs.getInt("emp_id"));
                    f.setRelation(rs.getString("relation"));
                    try {
                        f.setName(SmCryptoUtil.decryptSm4(rs.getString("name")));
                    } catch (Exception ex) {
                        f.setName("[解密失败]");
                    }
                    try {
                        f.setIdCard(SmCryptoUtil.decryptSm4(rs.getString("id_card")));
                    } catch (Exception ex) {
                        f.setIdCard("[解密失败]");
                    }
                    return f;
                }
            }
        }
        return null;
    }

    @Override
    public List<empFamily> findByEmpId(Integer empId) throws SQLException {
        String sql = "SELECT family_id, emp_id, relation, name, id_card FROM emp_family WHERE emp_id=?";
        List<empFamily> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, empId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    empFamily f = new empFamily();
                    f.setFamilyId(rs.getInt("family_id"));
                    f.setEmpId(rs.getInt("emp_id"));
                    f.setRelation(rs.getString("relation"));
                    try {
                        f.setName(SmCryptoUtil.decryptSm4(rs.getString("name")));
                    } catch (Exception ex) {
                        f.setName("[解密失败]");
                    }
                    try {
                        f.setIdCard(SmCryptoUtil.decryptSm4(rs.getString("id_card")));
                    } catch (Exception ex) {
                        f.setIdCard("[解密失败]");
                    }
                    list.add(f);
                }
            }
        }
        return list;
    }

    @Override
    public List<empFamily> findAll() throws SQLException {
        String sql = "SELECT family_id, emp_id, relation, name, id_card FROM emp_family ORDER BY emp_id ASC, family_id ASC";
        List<empFamily> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                empFamily f = new empFamily();
                f.setFamilyId(rs.getInt("family_id"));
                f.setEmpId(rs.getInt("emp_id"));
                f.setRelation(rs.getString("relation"));
                try {
                    f.setName(SmCryptoUtil.decryptSm4(rs.getString("name")));
                } catch (Exception ex) {
                    f.setName("[解密失败]");
                }
                try {
                    f.setIdCard(SmCryptoUtil.decryptSm4(rs.getString("id_card")));
                } catch (Exception ex) {
                    f.setIdCard("[解密失败]");
                }
                list.add(f);
            }
        }
        return list;
    }
}

