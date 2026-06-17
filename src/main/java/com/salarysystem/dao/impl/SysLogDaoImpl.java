package com.salarysystem.dao.impl;

import com.salarysystem.dao.SysLogDao;
import com.salarysystem.model.PageResult;
import com.salarysystem.model.sysLog;
import com.salarysystem.util.SmCryptoUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class SysLogDaoImpl implements SysLogDao {

    // 统一时间格式（精确到秒），避免写入/读出精度不一致导致 HMAC 校验失败
    private static final DateTimeFormatter HMAC_TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public int insert(sysLog log) throws SQLException {
        // ensure createTime present and truncated to seconds for consistent HMAC
        if (log.getCreateTime() == null) {
            log.setCreateTime(LocalDateTime.now());
        }
        log.setCreateTime(log.getCreateTime().withNano(0));
        String hmac = SmCryptoUtil.hmacSm3Hex(buildHmacMessage(log));

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
        // verify HMAC —— 使用与写入时相同的时间格式
        if (l.getCreateTime() != null) {
            l.setCreateTime(l.getCreateTime().withNano(0));
        }
        String expected = SmCryptoUtil.hmacSm3Hex(buildHmacMessage(l));
        l.setHmacValid(hmac != null && hmac.equalsIgnoreCase(expected));
        return l;
    }

    /**
     * 构建 HMAC 消息原文（时间精确到秒，保证读写一致性）
     */
    private String buildHmacMessage(sysLog log) {
        return (log.getUserId() == null ? "" : log.getUserId().toString()) + "|"
                + (log.getActionType() == null ? "" : log.getActionType()) + "|"
                + (log.getIpAddress() == null ? "" : log.getIpAddress()) + "|"
                + (log.getCreateTime() == null ? "" : log.getCreateTime().format(HMAC_TIME_FMT));
    }

    @Override
    public PageResult<sysLog> findByFilters(
            Integer userId, String actionType,
            LocalDateTime startTime, LocalDateTime endTime,
            int pageNo, int pageSize) throws SQLException {

        StringBuilder whereClause = new StringBuilder("WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (userId != null) {
            whereClause.append(" AND user_id = ?");
            params.add(userId);
        }
        if (actionType != null && !actionType.isEmpty()) {
            whereClause.append(" AND action_type = ?");
            params.add(actionType);
        }
        if (startTime != null) {
            whereClause.append(" AND create_time >= ?");
            params.add(Timestamp.valueOf(startTime));
        }
        if (endTime != null) {
            whereClause.append(" AND create_time <= ?");
            params.add(Timestamp.valueOf(endTime));
        }

        // Count total
        String countSql = "SELECT COUNT(*) FROM sys_log " + whereClause;
        long total = 0;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(countSql)) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) total = rs.getLong(1);
            }
        }

        // Query page
        int offset = (pageNo - 1) * pageSize;
        String sql = "SELECT log_id, user_id, action_type, ip_address, create_time, hmac FROM sys_log "
                + whereClause + " ORDER BY create_time DESC LIMIT ? OFFSET ?";

        List<sysLog> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int idx = 1;
            for (int i = 0; i < params.size(); i++) ps.setObject(idx++, params.get(i));
            ps.setInt(idx++, pageSize);
            ps.setInt(idx++, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }

        return new com.salarysystem.model.PageResult<>(list, pageNo, pageSize, total);
    }

    @Override
    public List<String> findAllActionTypes() throws SQLException {
        String sql = "SELECT DISTINCT action_type FROM sys_log ORDER BY action_type";
        List<String> types = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) types.add(rs.getString("action_type"));
        }
        return types;
    }

    @Override
    public List<sysLog> findRecent(int limit) throws SQLException {
        String sql = "SELECT log_id, user_id, action_type, ip_address, create_time, hmac FROM sys_log ORDER BY create_time DESC LIMIT ?";
        List<sysLog> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }
}

