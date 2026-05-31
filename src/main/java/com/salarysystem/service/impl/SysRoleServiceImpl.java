package com.salarysystem.service.impl;

import com.salarysystem.dao.impl.SysRoleDaoImpl;
import com.salarysystem.model.sysRole;
import com.salarysystem.service.SysRoleService;

import java.sql.SQLException;
import java.util.List;

public class SysRoleServiceImpl implements SysRoleService {

    private final SysRoleDaoImpl dao = new SysRoleDaoImpl();
    private final SysLogServiceImpl logService = new SysLogServiceImpl();

    @Override
    public void add(sysRole role) throws SQLException {
        dao.insert(role);
        try { logService.log(null, "ADD_ROLE", "SYSTEM"); } catch (SQLException ignored) {}
    }

    @Override
    public void update(sysRole role) throws SQLException {
        dao.update(role);
        try { logService.log(null, "UPDATE_ROLE", "SYSTEM"); } catch (SQLException ignored) {}
    }

    @Override
    public void delete(Integer roleId) throws SQLException {
        dao.deleteById(roleId);
        try { logService.log(null, "DELETE_ROLE", "SYSTEM"); } catch (SQLException ignored) {}
    }

    @Override
    public sysRole findById(Integer roleId) throws SQLException {
        return dao.findById(roleId);
    }

    @Override
    public List<sysRole> findAll() throws SQLException {
        return dao.findAll();
    }
}

