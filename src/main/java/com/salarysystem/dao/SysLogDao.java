package com.salarysystem.dao;

import com.salarysystem.model.sysLog;
import com.salarysystem.model.PageResult;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public interface SysLogDao extends BaseDao {
    int insert(sysLog log) throws SQLException;
    int deleteById(Long logId) throws SQLException;
    sysLog findById(Long logId) throws SQLException;
    List<sysLog> findByUserId(Integer userId) throws SQLException;
    List<sysLog> findAll() throws SQLException;

    /** 分页筛选日志 */
    PageResult<sysLog> findByFilters(Integer userId, String actionType,
        LocalDateTime startTime, LocalDateTime endTime,
        int pageNo, int pageSize) throws SQLException;

    /** 获取所有不重复的 action_type 列表（供筛选下拉框使用） */
    List<String> findAllActionTypes() throws SQLException;

    /** 获取最近 N 条日志（供 Dashboard 使用） */
    List<sysLog> findRecent(int limit) throws SQLException;
}

