package com.salarysystem.dao.impl;

import com.salarysystem.dao.EmpInfoDao;
import com.salarysystem.model.empInfo;
import com.salarysystem.util.SmCryptoUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmpInfoDaoImpl implements EmpInfoDao {

    @Override
    public int insert(empInfo emp) throws SQLException {
        // Prepare dataHash from core plaintext values before encryption
        String rawForHash = (emp.getEmpNo() == null ? "" : emp.getEmpNo())
                + (emp.getDeptName() == null ? "" : emp.getDeptName())
                + (emp.getPosition() == null ? "" : emp.getPosition())
                + (emp.getEmpName() == null ? "" : emp.getEmpName())
                + (emp.getIdCard() == null ? "" : emp.getIdCard())
                + (emp.getPhone() == null ? "" : emp.getPhone())
                + (emp.getAddress() == null ? "" : emp.getAddress());
        String dataHash = SmCryptoUtil.hashSm3(rawForHash);

        String sql = "INSERT INTO emp_info(emp_no, dept_name, position, emp_name, id_card, phone, address, data_hash) VALUES(?,?,?,?,?,?,?,?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, emp.getEmpNo());
            ps.setString(2, emp.getDeptName());
            ps.setString(3, emp.getPosition());
            // encrypt sensitive fields with SM4 before storing
            ps.setString(4, SmCryptoUtil.encryptSm4(emp.getEmpName()));
            ps.setString(5, SmCryptoUtil.encryptSm4(emp.getIdCard()));
            ps.setString(6, SmCryptoUtil.encryptSm4(emp.getPhone()));
            ps.setString(7, SmCryptoUtil.encryptSm4(emp.getAddress()));
            ps.setString(8, dataHash);
            int affected = ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    emp.setEmpId(rs.getInt(1));
                }
            }
            // set the computed dataHash back to model
            emp.setDataHash(dataHash);
            return affected;
        }
    }

    @Override
    public int update(empInfo emp) throws SQLException {
        // compute new data hash from plaintext inputs
        String rawForHash = (emp.getEmpNo() == null ? "" : emp.getEmpNo())
                + (emp.getDeptName() == null ? "" : emp.getDeptName())
                + (emp.getPosition() == null ? "" : emp.getPosition())
                + (emp.getEmpName() == null ? "" : emp.getEmpName())
                + (emp.getIdCard() == null ? "" : emp.getIdCard())
                + (emp.getPhone() == null ? "" : emp.getPhone())
                + (emp.getAddress() == null ? "" : emp.getAddress());
        String dataHash = SmCryptoUtil.hashSm3(rawForHash);

        String sql = "UPDATE emp_info SET emp_no=?, dept_name=?, position=?, emp_name=?, id_card=?, phone=?, address=?, data_hash=? WHERE emp_id=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, emp.getEmpNo());
            ps.setString(2, emp.getDeptName());
            ps.setString(3, emp.getPosition());
            ps.setString(4, SmCryptoUtil.encryptSm4(emp.getEmpName()));
            ps.setString(5, SmCryptoUtil.encryptSm4(emp.getIdCard()));
            ps.setString(6, SmCryptoUtil.encryptSm4(emp.getPhone()));
            ps.setString(7, SmCryptoUtil.encryptSm4(emp.getAddress()));
            ps.setString(8, dataHash);
            ps.setInt(9, emp.getEmpId());
            emp.setDataHash(dataHash);
            return ps.executeUpdate();
        }
    }

    @Override
    public int deleteById(Integer empId) throws SQLException {
        String sql = "DELETE FROM emp_info WHERE emp_id=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, empId);
            return ps.executeUpdate();
        }
    }

    @Override
    public empInfo findById(Integer empId) throws SQLException {
        String sql = "SELECT emp_id, emp_no, dept_name, position, emp_name, id_card, phone, address, data_hash FROM emp_info WHERE emp_id=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, empId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    empInfo e = new empInfo();
                    e.setEmpId(rs.getInt("emp_id"));
                    e.setEmpNo(rs.getString("emp_no"));
                    e.setDeptName(rs.getString("dept_name"));
                    e.setPosition(rs.getString("position"));
                    try {
                        e.setEmpName(SmCryptoUtil.decryptSm4(rs.getString("emp_name")));
                    } catch (Exception ex) {
                        e.setEmpName("[解密失败]");
                    }
                    try {
                        e.setIdCard(SmCryptoUtil.decryptSm4(rs.getString("id_card")));
                    } catch (Exception ex) {
                        e.setIdCard("[解密失败]");
                    }
                    try {
                        e.setPhone(SmCryptoUtil.decryptSm4(rs.getString("phone")));
                    } catch (Exception ex) {
                        e.setPhone("[解密失败]");
                    }
                    try {
                        e.setAddress(SmCryptoUtil.decryptSm4(rs.getString("address")));
                    } catch (Exception ex) {
                        e.setAddress("[解密失败]");
                    }
                    e.setDataHash(rs.getString("data_hash"));
                    return e;
                }
            }
        }
        return null;
    }

    @Override
    public empInfo findByEmpNo(String empNo) throws SQLException {
        String sql = "SELECT emp_id, emp_no, dept_name, position, emp_name, id_card, phone, address, data_hash FROM emp_info WHERE emp_no=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, empNo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    empInfo e = new empInfo();
                    e.setEmpId(rs.getInt("emp_id"));
                    e.setEmpNo(rs.getString("emp_no"));
                    e.setDeptName(rs.getString("dept_name"));
                    e.setPosition(rs.getString("position"));
                    try {
                        e.setEmpName(SmCryptoUtil.decryptSm4(rs.getString("emp_name")));
                    } catch (Exception ex) {
                        e.setEmpName("[解密失败]");
                    }
                    try {
                        e.setIdCard(SmCryptoUtil.decryptSm4(rs.getString("id_card")));
                    } catch (Exception ex) {
                        e.setIdCard("[解密失败]");
                    }
                    try {
                        e.setPhone(SmCryptoUtil.decryptSm4(rs.getString("phone")));
                    } catch (Exception ex) {
                        e.setPhone("[解密失败]");
                    }
                    try {
                        e.setAddress(SmCryptoUtil.decryptSm4(rs.getString("address")));
                    } catch (Exception ex) {
                        e.setAddress("[解密失败]");
                    }
                    e.setDataHash(rs.getString("data_hash"));
                    return e;
                }
            }
        }
        return null;
    }

    @Override
    public List<empInfo> findAll() throws SQLException {
        String sql = "SELECT emp_id, emp_no, dept_name, position, emp_name, id_card, phone, address, data_hash FROM emp_info";
        List<empInfo> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                empInfo e = new empInfo();
                e.setEmpId(rs.getInt("emp_id"));
                e.setEmpNo(rs.getString("emp_no"));
                e.setDeptName(rs.getString("dept_name"));
                e.setPosition(rs.getString("position"));
                // Safely decrypt with fallback to masked value if decryption fails
                try {
                    e.setEmpName(SmCryptoUtil.decryptSm4(rs.getString("emp_name")));
                } catch (Exception ex) {
                    e.setEmpName("[解密失败]");
                    System.err.println("Failed to decrypt emp_name: " + ex.getMessage());
                }
                try {
                    e.setIdCard(SmCryptoUtil.decryptSm4(rs.getString("id_card")));
                } catch (Exception ex) {
                    e.setIdCard("[解密失败]");
                    System.err.println("Failed to decrypt id_card: " + ex.getMessage());
                }
                try {
                    e.setPhone(SmCryptoUtil.decryptSm4(rs.getString("phone")));
                } catch (Exception ex) {
                    e.setPhone("[解密失败]");
                    System.err.println("Failed to decrypt phone: " + ex.getMessage());
                }
                try {
                    e.setAddress(SmCryptoUtil.decryptSm4(rs.getString("address")));
                } catch (Exception ex) {
                    e.setAddress("[解密失败]");
                    System.err.println("Failed to decrypt address: " + ex.getMessage());
                }
                e.setDataHash(rs.getString("data_hash"));
                list.add(e);
            }
        }
        return list;
    }
}

