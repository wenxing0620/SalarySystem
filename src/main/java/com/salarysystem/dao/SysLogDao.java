package com.salarysystem.dao;

import com.salarysystem.model.sysLog;

import java.sql.SQLException;
import java.util.List;

public interface SysLogDao extends com.salarysystem.BaseDao {
    int insert(sysLog log) throws SQLException;
    int deleteById(Long logId) throws SQLException;
    sysLog findById(Long logId) throws SQLException;
    List<sysLog> findByUserId(Integer userId) throws SQLException;
    List<sysLog> findAll() throws SQLException;
}

