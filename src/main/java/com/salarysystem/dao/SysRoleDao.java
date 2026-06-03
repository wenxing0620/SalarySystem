package com.salarysystem.dao;

import com.salarysystem.model.sysRole;

import java.sql.SQLException;
import java.util.List;

public interface SysRoleDao extends BaseDao {
    int insert(sysRole role) throws SQLException;
    int update(sysRole role) throws SQLException;
    int deleteById(Integer roleId) throws SQLException;
    sysRole findById(Integer roleId) throws SQLException;
    List<sysRole> findAll() throws SQLException;
}

