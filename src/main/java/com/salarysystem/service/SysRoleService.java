package com.salarysystem.service;

import com.salarysystem.model.sysRole;

import java.sql.SQLException;
import java.util.List;

public interface SysRoleService {
    void add(sysRole role) throws SQLException;
    void update(sysRole role) throws SQLException;
    void delete(Integer roleId) throws SQLException;
    sysRole findById(Integer roleId) throws SQLException;
    List<sysRole> findAll() throws SQLException;
}

