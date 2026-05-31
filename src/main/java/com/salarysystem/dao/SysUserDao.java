package com.salarysystem.dao;

import com.salarysystem.model.sysUser;

import java.sql.SQLException;
import java.util.List;

public interface SysUserDao extends com.salarysystem.BaseDao {
    int insert(sysUser user) throws SQLException;
    int update(sysUser user) throws SQLException;
    int deleteById(Integer userId) throws SQLException;
    sysUser findById(Integer userId) throws SQLException;
    sysUser findByUsername(String username) throws SQLException;
    List<sysUser> findAll() throws SQLException;
}

