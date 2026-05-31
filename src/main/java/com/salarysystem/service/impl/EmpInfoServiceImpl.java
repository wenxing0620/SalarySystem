package com.salarysystem.service.impl;

import com.salarysystem.dao.impl.EmpInfoDaoImpl;
import com.salarysystem.model.empInfo;
import com.salarysystem.service.EmpInfoService;

import java.sql.SQLException;
import java.util.List;

public class EmpInfoServiceImpl implements EmpInfoService {

    private final EmpInfoDaoImpl dao = new EmpInfoDaoImpl();
    private final SysLogServiceImpl logService = new SysLogServiceImpl();

    @Override
    public void create(empInfo emp) throws SQLException {
        dao.insert(emp);
        // audit
        try {
            logService.log(null, "ADD_EMP", "SYSTEM");
        } catch (SQLException ignored) {}
    }

    @Override
    public void update(empInfo emp) throws SQLException {
        dao.update(emp);
        try {
            logService.log(null, "UPDATE_EMP", "SYSTEM");
        } catch (SQLException ignored) {}
    }

    @Override
    public void delete(Integer empId) throws SQLException {
        dao.deleteById(empId);
        try {
            logService.log(null, "DELETE_EMP", "SYSTEM");
        } catch (SQLException ignored) {}
    }

    @Override
    public empInfo findById(Integer empId) throws SQLException {
        return dao.findById(empId);
    }

    @Override
    public empInfo findByEmpNo(String empNo) throws SQLException {
        return dao.findByEmpNo(empNo);
    }

    @Override
    public List<empInfo> findAll() throws SQLException {
        return dao.findAll();
    }
}


