package com.salarysystem.service.impl;

import com.salarysystem.dao.impl.SysLogDaoImpl;
import com.salarysystem.model.sysLog;
import com.salarysystem.service.SysLogService;

import java.sql.SQLException;
import java.time.LocalDateTime;

public class SysLogServiceImpl implements SysLogService {

    private final SysLogDaoImpl logDao = new SysLogDaoImpl();

    @Override
    public void log(Integer userId, String actionType, String ipAddress) throws SQLException {
        sysLog l = new sysLog();
        l.setUserId(userId);
        l.setActionType(actionType);
        l.setIpAddress(ipAddress);
        l.setCreateTime(LocalDateTime.now());
        logDao.insert(l);
    }
}

