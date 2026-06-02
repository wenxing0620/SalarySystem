package com.salarysystem.service.impl;

import com.salarysystem.dao.impl.SysLogDaoImpl;
import com.salarysystem.model.sysLog;
import com.salarysystem.model.PageResult;
import com.salarysystem.service.SysLogService;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

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

    @Override
    public PageResult<sysLog> findByFilters(Integer userId, String actionType,
            LocalDateTime startTime, LocalDateTime endTime,
            int pageNo, int pageSize) throws SQLException {
        return logDao.findByFilters(userId, actionType, startTime, endTime, pageNo, pageSize);
    }

    @Override
    public List<String> findAllActionTypes() throws SQLException {
        return logDao.findAllActionTypes();
    }

    @Override
    public List<sysLog> findRecent(int limit) throws SQLException {
        return logDao.findRecent(limit);
    }
}

