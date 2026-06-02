package com.salarysystem.service;

import com.salarysystem.model.sysLog;
import com.salarysystem.model.PageResult;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public interface SysLogService {
    void log(Integer userId, String actionType, String ipAddress) throws SQLException;

    PageResult<sysLog> findByFilters(Integer userId, String actionType,
        LocalDateTime startTime, LocalDateTime endTime,
        int pageNo, int pageSize) throws SQLException;

    List<String> findAllActionTypes() throws SQLException;

    /** 获取最近 N 条日志（供 Dashboard 使用） */
    List<sysLog> findRecent(int limit) throws SQLException;
}

