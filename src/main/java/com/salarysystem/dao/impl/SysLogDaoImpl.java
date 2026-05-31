package com.salarysystem.dao.impl;

import com.salarysystem.dao.SysLogDao;
import com.salarysystem.model.sysLog;
import com.salarysystem.util.SmCryptoUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SysLogDaoImpl implements SysLogDao {

    @Override
    public int insert(sysLog log) throws SQLException {
        // ensure createTime present
        if (log.getCreateTime() == null) log.setCreateTime(LocalDateTime.now());
        // compute HMAC over core fields
        String msg = (log.getUserId() == null ? "" : log.getUserId().toString()) + "|"
                + (log.getActionType() == null ? "" : log.getActionType()) + "|"
                + (log.getIpAddress() == null ? "" : log.getIpAddress()) + "|"
                + log.getCreateTime().toString();
        String hmac = SmCryptoUtil.hmacSm3Hex(msg);

        String sql = "INSERT INTO sys_log(user_id, action_type, ip_address, create_time, hmac) VALUES(?,?,?,?,?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            // Handle nullable userId
            if (log.getUserId() == null) {
                ps.setNull(1, java.sql.Types.INTEGER);
            } else {
                ps.setInt(1, log.getUserId());
            }
            ps.setString(2, log.getActionType());
            ps.setString(3, log.getIpAddress());
            ps.setTimestamp(4, log.getCreateTime() == null ? null : Timestamp.valueOf(log.getCreateTime()));
            ps.setString(5, hmac);
            int affected = ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    log.setLogId(rs.getLong(1));
                }
            }
            log.setHmac(hmac);
            log.setHmacValid(true);
            return affected;
        }
    }

    @Override
    public int deleteById(Long logId) throws SQLException {
        String sql = "DELETE FROM sys_log WHERE log_id=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, logId);
            return ps.executeUpdate();
        }
    }

    @Override
    public sysLog findById(Long logId) throws SQLException {
        String sql = "SELECT log_id, user_id, action_type, ip_address, create_time, hmac FROM sys_log WHERE log_id=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, logId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<sysLog> findByUserId(Integer userId) throws SQLException {
        String sql = "SELECT log_id, user_id, action_type, ip_address, create_time, hmac FROM sys_log WHERE user_id=? ORDER BY create_time DESC";
        List<sysLog> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    @Override
    public List<sysLog> findAll() throws SQLException {
        String sql = "SELECT log_id, user_id, action_type, ip_address, create_time, hmac FROM sys_log ORDER BY create_time DESC";
        List<sysLog> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    private sysLog mapRow(ResultSet rs) throws SQLException {
        sysLog l = new sysLog();
        l.setLogId(rs.getLong("log_id"));
        // Handle nullable userId
        Object userIdObj = rs.getObject("user_id");
        if (userIdObj != null) {
            l.setUserId((Integer) userIdObj);
        }
        l.setActionType(rs.getString("action_type"));
        l.setIpAddress(rs.getString("ip_address"));
        Timestamp t = rs.getTimestamp("create_time");
        l.setCreateTime(t == null ? null : t.toLocalDateTime());
        String hmac = rs.getString("hmac");
        l.setHmac(hmac);
        // verify HMAC
        String msg = (l.getUserId() == null ? "" : l.getUserId().toString()) + "|"
                + (l.getActionType() == null ? "" : l.getActionType()) + "|"
                + (l.getIpAddress() == null ? "" : l.getIpAddress()) + "|"
                + (l.getCreateTime() == null ? "" : l.getCreateTime().toString());
        String expected = SmCryptoUtil.hmacSm3Hex(msg);
        l.setHmacValid(hmac != null && hmac.equalsIgnoreCase(expected));
        return l;
    }
}

