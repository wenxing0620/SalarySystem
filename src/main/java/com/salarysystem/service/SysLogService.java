package com.salarysystem.service;

import com.salarysystem.model.sysLog;

import java.sql.SQLException;

public interface SysLogService {
    void log(Integer userId, String actionType, String ipAddress) throws SQLException;
}

